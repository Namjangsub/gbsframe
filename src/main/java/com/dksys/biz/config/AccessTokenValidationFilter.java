package com.dksys.biz.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.oauth2.jwt.JwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.dksys.biz.main.service.LoginService;
import com.dksys.biz.auth.service.ClientCertService;
import com.dksys.biz.auth.service.DnParser;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;

@Component
public class AccessTokenValidationFilter extends OncePerRequestFilter {

    @Value("${security.jwt.signing-key}")
    private String signingKey;

	@Autowired
	private UserLoginLogService userLoginLogService;

    @Autowired
    LoginService loginService;
    
	@Autowired
    private TokenService tokenService;
	
	@Autowired
	private ClientCertService clientCertService;   // 인증서 Serial → 사용자/단말 매핑용

    private static final AntPathMatcher pathMatcher = new AntPathMatcher();
    private static final List<String> WHITELIST = Arrays.asList(
        "/login", "/oauth/**", "/customLogout", "/error",
        "/static/**", "/download/**", "/s/**", "/favicon.ico", "/index.html",
        "/api/audit-ingest" 
    );

    // mTLS "검사를 생략"할 URL 패턴들
    private static final String[] EXCLUDE_PATTERNS = {
            "/login",
            "/customLogout",
            "/error",
            "/favicon.ico", 
            "/index.html",
            "/api/audit-ingest",
            "/static/redirectChkCode.html", //UBI REPORT View 실행
            "/s/**"   // 발주소조회 URL
    };
    // CCV 토큰 유효기간 (초) : 페이지 체류 시간 기준으로 5~10분 권장
    private static final long CCV_TTL_SECONDS = 300L; // 5분

    private String createCcvToken(String userId,
                                  String deviceId,
                                  String certSerialHex,
                                  Date accessTokenExp) {

        long nowMillis = System.currentTimeMillis();
        long nowSec    = nowMillis / 1000L;

        long ccvExpSecByTtl = nowSec + CCV_TTL_SECONDS;

        // access_token exp (Unix time)와 비교해서, CCV가 더 오래 살지 않게 제어
        long accessExpSec = (accessTokenExp != null)
                ? (accessTokenExp.getTime() / 1000L)
                : ccvExpSecByTtl;

        long finalExpSec = Math.min(ccvExpSecByTtl, accessExpSec);

        Date iat = new Date(nowMillis);
        Date exp = new Date(finalExpSec * 1000L);

        return Jwts.builder()
                .setSubject(userId)                // uid
                .claim("dev", deviceId)            // deviceId
                .claim("csn", certSerialHex)       // cert serial
                .claim("typ", "CCV")               // 토큰 타입 구분
                .setIssuedAt(iat)
                .setExpiration(exp)
                .signWith(io.jsonwebtoken.SignatureAlgorithm.HS256,
                          signingKey.getBytes(StandardCharsets.UTF_8))
                .compact();
    }

    private Claims parseCcv(String ccvToken) {
        return Jwts.parser()
                .setSigningKey(signingKey.getBytes(StandardCharsets.UTF_8))
                .parseClaimsJws(ccvToken)
                .getBody();
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return WHITELIST.stream()
                .anyMatch(p -> pathMatcher.match(p, request.getServletPath()));
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

    	/*********************************************************************************************************
    	 * 핵심 처리내용 CCV = Client-Cert Verified Token (Page-level Session Token)
    	 * 첫 성공 요청에서 발급
    	 * 내용: 
    	 *    - uid : userId
    	 *    - dev : deviceId
    	 *    - csn : certSerial
    	 *    - typ : "CCV"
    	 *    - iat/exp : 발급/만료 시간
    	 *    - (선택) roles, mappingVersion 등
    	 *    
    	 * 짧은 TTL (5~10분)
    	 * 만료 시간은 access_token의 exp를 넘지 않도록(min)
    	 * 
    	 * 이후 요청에서는:
    	 *      1단계: mTLS 인증서만 기본 체크 (존재 + 유효기간)
    	 *      2단계: CCV 쿠키가 유효하면
    	 *             CCV 안의 uid/dev/csn 과
    	 *             요청의 인증서(serialHex, rUserId, rDevice)를 비교
    	 *             바로 Authentication 생성 → access_token / refresh_token / DB 조회 모두 스킵
    	 *             
    	 *      3단계: CCV가 없거나 실패 시
    	 *             지금 있는 기존 로직(인증서 DB 매핑 + access/refresh) 수행
    	 *             성공하면 새 CCV 발급
    	 *             
    	 * 이렇게 하면 
    	 *     - 한 페이지에서 처음 한 번 full 검증, 이후 몇 분간은 CCV로만 빠르게 처리가 됩니다.
    	 *     - access_token 관점에서도, access_token을 매번 다시 파싱하지 않고 CCV에 정제된 정보(uid, 권한)를 담아 재사용하는 구조가 됨
    	 **********************************************************************************************************/
    	
        // 요청 쿠키 확인 로그
//        System.out.println("▶ Request Cookie: " + request.getHeader("Cookie"));
        if (shouldNotFilter(request)) {             // 화이트리스트면 패스
            chain.doFilter(request, response);
            return;
        }
        
        // mTLS 예외 URL 여부
        boolean mtlsExcluded = isMtlsExcluded(request);

        // 아래에서 사용할 변수는 미리 선언
        Map<String, String> mapping = null;
        String serialHex = null;

        // ==========================
        // 1. mTLS 인증서 기본 체크
        // ==========================
        
        /* ==========================
         * CLIENT_CERT_REQUIRED : 인증서 자체가 없음
         * CLIENT_CERT_NOT_REGISTERED : 인증서는 있지만 매핑(등록) 없음
         * CLIENT_CERT_INACTIVE : 등록은 있으나 비활성(USE_YN != Y 또는 EXP_DT 만료)
         * CLIENT_CERT_USER_MISMATCH : 인증서 CN에서 파싱한 userId ≠ DB userId
         * CLIENT_CERT_DEVICE_MISMATCH : 인증서 CN에서 파싱한 device ≠ DB deviceId/deviceType
         * CLIENT_CERT_REVOKED : 폐기/차단
         * CLIENT_CERT_SERIAL_MISMATCH : 시리얼 불일치
         * CLIENT_CERT_SUBJECT_MISMATCH : Subject DN 불일치
         * ============================== */
        if (!mtlsExcluded) {
	        X509Certificate[] certs =
	            (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
	
	        if (certs == null || certs.length == 0) {
	            // mTLS를 필수로 강제한 경우: 인증서 없으면 바로 차단
	            reject(response, "CLIENT_CERT_REQUIRED",  "클라이언트 인증서가 필요합니다.");
	            return;
	        }
	
	        X509Certificate clientCert = certs[0];
	
	        String subjectDN = clientCert.getSubjectX500Principal().getName();
	//        String serialHex = clientCert.getSerialNumber().toString(16);
	        serialHex = clientCert.getSerialNumber().toString(16);
	        Date   notBefore = clientCert.getNotBefore();
	        Date   notAfter  = clientCert.getNotAfter();
	
	        // (옵션) 인증서 유효기간 체크
	        Date now = new Date();
	        if (now.before(notBefore) || now.after(notAfter)) {
	            reject(response, "CLIENT_CERT_EXPIRED", "만료되었거나 아직 유효하지 않은 클라이언트 인증서입니다.");
	            return;
	        }
	
	        //String subjectDN = "CN=js_nam_desktop,OU=RND,O=GunyangITT";
	        Map<String, String> r = DnParser.parse(subjectDN);
			String rUserId = r.get("userId");       // js_nam
			String rDevice = r.get("deviceType");   // desktop
			String rOrg    = r.get("orgName");      // GunyangITT
	        
	
	        // ==========================
	        // 1-1. CCV Shortcut (페이지 단위 캐시)
	        // ==========================
	        String ccvToken = extractCookie(request, "CCV");   // 아래에 helper 추가
	        if (ccvToken != null) {
	            try {
	//                // (1) Authorization 헤더 존재 여부만 확인
	//                String authHeader = request.getHeader("Authorization");
	//                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
	//                    throw new JwtException("Authorization header missing for CCV shortcut");
	//                }
	                
	                Claims ccvClaims = parseCcv(ccvToken);
	
	                // typ 이 CCV 인지 확인
	                String typ = ccvClaims.get("typ", String.class);
	                if (!"CCV".equals(typ)) {
	                    throw new JwtException("Not a CCV token");
	                }
	
	                String ccvUser  = ccvClaims.getSubject();              // uid
	                String ccvDev   = ccvClaims.get("dev", String.class);  // deviceId
	                String ccvCsn   = ccvClaims.get("csn", String.class);  // cert serial
	
	                // 인증서에서 파싱된 값과 CCV 내용이 모두 일치해야 신뢰
	                if (rUserId != null && rUserId.equals(ccvUser)
	                        && rDevice != null && rDevice.equals(ccvDev)
	                        && serialHex.equalsIgnoreCase(ccvCsn)) {
	
	                    // page-level 캐시를 신뢰하고, 여기서 Authentication 바로 세팅
	                    // 권한은 단순 ROLE_USER 정도면 충분 (필요하면 roles claim 추가)
	                    List<GrantedAuthority> authorities =
	                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
	                    Authentication auth =
	                            new UsernamePasswordAuthenticationToken(ccvUser, null, authorities);
	                    SecurityContextHolder.getContext().setAuthentication(auth);
	
	                    // 이 요청은 DB 매핑 / access_token / refresh_token 로직 모두 스킵
	                    chain.doFilter(request, response);
	                    return;
	                }
	//            } catch (JwtException e) {
	            } catch (io.jsonwebtoken.ExpiredJwtException ex) {
	                // CCV 만료 -> 그냥 캐시 없다고 보고 full 검증으로 진행
	                // System.out.println("[CCV] expired, fall back to full verification");
	                SecurityContextHolder.clearContext();
	            } catch (JwtException ex) {
	                // 기타 JJWT 예외(서명 오류 등) -> 마찬가지로 full 검증
	                // System.out.println("[CCV] invalid token, fall back to full verification");
	                SecurityContextHolder.clearContext();
	            }
	        }
	
	        // ==========================
	        // 2. 여기부터는 "CCV 없음/실패" -> 기존 full 검증 로직
	        //    (인증서 DB 매핑 + access/refresh)
	        // ==========================
	
	        // (핵심) 인증서 Serial 등으로 사전 등록된 단말/사용자인지 확인
	        Map<String, String> clientCertParm = new HashMap<>();
	        clientCertParm.put("serialHex"	, serialHex);
	        clientCertParm.put("subjectDN"	, subjectDN);
	        clientCertParm.put("userId"		, rUserId); 
	        clientCertParm.put("deviceType"	, rDevice);
	        clientCertParm.put("orgName"	, rOrg);
	        
	        mapping = clientCertService.findByCertSerial(clientCertParm);
	        
	
	        // DB 쿼리에서 이미 USE_YN, EXP_DT 조건을 거르지만,
	        // VO에도 isActive() 가 있으니 한 번 더 방어 로직을 둬도 됨.
	        if (mapping == null) {
	            reject(response, "CLIENT_CERT_NOT_REGISTERED", "등록되지 않은 단말/사용자입니다.");
	            return;
	        }
	        if (!rUserId.equals(mapping.get("userId"))) {
	            reject(response, "CLIENT_CERT_USER_MISMATCH", "등록된 사용자와 인증서 사용자가 일치하지 않습니다.");
	            return;
	        }
	        if (!rDevice.equals(mapping.get("deviceId"))) {
	            reject(response, "CLIENT_CERT_DEVICE_MISMATCH", "등록된 단말정보와 인증서 단말정보가 일치하지 않습니다.");
	            return;
	        }
	
	        // 필요 시, 이후 로직에서 쓸 수 있게 request에 심어두기
	        request.setAttribute("CERT_USER_ID",   mapping.get("userId"));
	        request.setAttribute("CERT_DEVICE_ID", mapping.get("deviceId"));
	        request.setAttribute("CERT_SUBJECT_DN", subjectDN);
        }

        /* ==============================
         * 2. 기존 JWT / Refresh Token 처리
         * ============================== */
        
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ") || header.length() <= 7) {
        	// 쿠키에서 refresh_token 추출
            String refreshToken = extractRefreshTokenFromCookie(request);

            if (refreshToken != null) {
                try {
                    Claims refreshClaims = parseJwt(refreshToken);
                    String username = refreshClaims.get("user_name", String.class);
                    String userAgent = RequestUtils.getUserAgent();
                    String deviceType = RequestUtils.detectDeviceType(userAgent);
                    String clientIp = RequestUtils.getClientIp();

                    if (!userLoginLogService.isLoginWithin24Hours(username, userAgent, clientIp, deviceType)) {
                        loginService.updateLogoutTime(username, userAgent, clientIp, deviceType);
                        RequestUtils.clearCookie("access_token", response);
                        RequestUtils.clearCookie("refresh_token", response);
                        SecurityContextHolder.clearContext();
//                        reject(response, "로그인 24시간 초과");
						String location = "/static/index.html";
						response.sendRedirect(location);
						response.setStatus(303); 
						response.setHeader("Location", location);
                        return;
                    }

                    // access_token 재발급
                    String newAccessToken = tokenService.reissueAccessToken(refreshClaims);

                    // SecurityContext 인증 객체 주입
                    Authentication authentication = buildAuthentication(refreshClaims);
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    // 응답 헤더에 새 토큰 전달
                    response.setHeader("X-New-Access-Token", newAccessToken);


                    // [추가] CCV 발급 (newAccessToken 만료시간 기반)
                    Claims newAccessClaims = parseJwt(newAccessToken);
                    Date newAccessExp = newAccessClaims.getExpiration();

                    String ccv = createCcvToken(
                            username,
                            mapping.get("deviceId"),
                            serialHex,
                            newAccessExp
                    );
                    ResponseCookie ccvCookie = ResponseCookie.from("CCV", ccv)
                            .httpOnly(true)
                            .secure(true)
                            .path("/")
                            .maxAge(CCV_TTL_SECONDS)
                            .sameSite("None")
                            .build();
                    response.addHeader("Set-Cookie", ccvCookie.toString());
//                    System.out.println("▶ Set-Cookie Header: " + response.getHeader("Set-Cookie"));
                    
                    chain.doFilter(request, response);
                    return;
                } catch (JwtException e) {
                    SecurityContextHolder.clearContext();
//                    reject(response, "refresh_token 유효성 실패");
                    chain.doFilter(request, response);
                    return;
                }
            } else {
//                reject(response, "Authorization 헤더와 Refresh Token 모두 없음");
                chain.doFilter(request, response);
                return;
            }
        }

        String token = header.substring(7).trim();
//        if (token.isEmpty()) {
//            chain.doFilter(request, response);
//            return;
//        }

        Claims claims;
        try {
            claims = parseJwt(token);
            // 권한 설정
            String username = (String) claims.get("userId");
            List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
            Authentication authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);


            // [추가] access_token 유효 + 인증서 DB 매핑까지 끝난 시점 → CCV 발급
            Date accessExp = claims.getExpiration();

	         // mTLS 제외 URL이면 CCV 발급 X
	         if (!mtlsExcluded && mapping != null && serialHex != null) {
	            String ccv = createCcvToken(
	                    username,
	                    mapping.get("deviceId"),   // 위에서 얻어둔 mapping
	                    serialHex,
	                    accessExp
	            );
	
	            ResponseCookie ccvCookie = ResponseCookie.from("CCV", ccv)
	                    .httpOnly(true)
	                    .secure(true)
	                    .path("/")           // 필요 시 /mtls 등으로 축소
	                    .maxAge(CCV_TTL_SECONDS)
	                    .sameSite("None")
	                    .build();
	            response.addHeader("Set-Cookie", ccvCookie.toString());
	         }
	         
            chain.doFilter(request, response);
        } catch (ExpiredJwtException ex) {
            // access token 만료 → refresh 로 재발급
            String refreshToken = extractRefreshToken(request);
            if (refreshToken != null) {
                try {
                    Claims refreshClaims = parseJwt(refreshToken);
                    String username = refreshClaims.get("user_name", String.class);
					String userAgent = RequestUtils.getUserAgent();
                    String deviceType = RequestUtils.detectDeviceType(userAgent);
					String clientIp = RequestUtils.getClientIp();
					if (!userLoginLogService.isLoginWithin24Hours(username, userAgent, clientIp, deviceType)) {
						loginService.updateLogoutTime(username, userAgent, clientIp, deviceType); // 로그아웃 처리

						RequestUtils.clearCookie("access_token", response);
						RequestUtils.clearCookie("refresh_token", response);
						// 4. SecurityContext 강제 초기화 (auth == null 일 수 있어도 상관 없음)
						SecurityContextHolder.clearContext();
						
//						reject(response, "로그인 24시간 초과");
						String location = request.getContextPath() + "/static/index.html";
						response.sendRedirect(location); //status 가 기본 302
						return;
					}

				    // access_token 재발급
                    String newAccessToken = tokenService.reissueAccessToken(refreshClaims);

                    // newAccessToken 의 claims 얻기 (만료시간 위해)
                    Claims newAccessClaims = parseJwt(newAccessToken);
                    Date newAccessExp = newAccessClaims.getExpiration();
                    
                    // SecurityContext 인증 객체 주입
                    Authentication authentication = buildAuthentication(refreshClaims);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    // 응답 헤더에 새 토큰 전달
                    response.setHeader("X-New-Access-Token", newAccessToken);
//                    System.out.println("인증 완료 후 context: {}"+ SecurityContextHolder.getContext().getAuthentication());


                    // [추가] CCV 발급

                    if (!mtlsExcluded && mapping != null && serialHex != null) {
	                    String ccv = createCcvToken(
	                            username,
	                            mapping.get("deviceId"),
	                            serialHex,
	                            newAccessExp
	                    );
	                    ResponseCookie ccvCookie = ResponseCookie.from("CCV", ccv)
	                            .httpOnly(true)
	                            .secure(true)
	                            .path("/")
	                            .maxAge(CCV_TTL_SECONDS)
	                            .sameSite("None")
	                            .build();
	                    response.addHeader("Set-Cookie", ccvCookie.toString());
                    }
//                    System.out.println("▶ Set-Cookie Header: " + response.getHeader("Set-Cookie"));
                    
                    // 반드시 체인 흐름 재개
                    chain.doFilter(request, response);

                    return;
                } catch (JwtException e) {
                	SecurityContextHolder.clearContext();  // 안전하게 context 비우기
                    reject(response, "", "refresh_token 유효성 실패");
                }
            } else {
                reject(response, "", "access_token 만료 + refresh_token 없음");
            }
        } catch (JwtException e) {
            // 기타 오류 (손상된 토큰 등)
            SecurityContextHolder.clearContext();
            reject(response, "", "access_token 유효성 실패");
        }

    }
    
    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refresh_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private String extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refresh_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }


    private void reject(HttpServletResponse response, String errCode, String message) throws IOException {
    	response.reset();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("X-ERR-CODE", errCode);
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }

    private Authentication buildAuthentication(Claims claims) {
        String username = claims.get("user_name", String.class);
        UserDetails userDetails = new User(username, "", AuthorityUtils.createAuthorityList("ROLE_USER"));
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
    

	private Claims parseJwt(String token) {
        return Jwts.parser()
            .setSigningKey(signingKey.getBytes(StandardCharsets.UTF_8))
            .parseClaimsJws(token)
            .getBody();
	}

	/**
	 * mapping 예시 키:
	 * - useYn : "Y"/"N"
	 * - expDt : "2025-12-31 23:59:59" 또는 "20251231235959" 등 (DB 포맷에 맞춰 파싱)
	 */
	private boolean isActive(Map<String, String> mapping) {
	    String useYn = mapping.get("useYn");
	    if (!"Y".equalsIgnoreCase(useYn)) {
	        return false;
	    }
	
	    // 만료일이 없으면 활성
	    String expDtStr = mapping.get("expDt");
	    if (expDtStr == null || expDtStr.trim().isEmpty()) {
	        return true;
	    }
	
	    // 1) Timestamp 문자열(yyyy-MM-dd HH:mm:ss[.S]) 케이스
	    try {
	        java.sql.Timestamp ts = java.sql.Timestamp.valueOf(expDtStr.trim());
	        return ts.after(new java.util.Date());
	    } catch (Exception ignore) { }
	
	    // 2) 숫자형 문자열(yyyyMMddHHmmss) 케이스
	    try {
	        java.text.SimpleDateFormat f = new java.text.SimpleDateFormat("yyyyMMddHHmmss");
	        f.setLenient(false);
	        java.util.Date exp = f.parse(expDtStr.trim());
	        return exp.after(new java.util.Date());
	    } catch (Exception ignore) { }
	
	    // 파싱 불가면 방어적으로 비활성 처리(운영 안전)
	    return false;
	}
	
	private String extractCookie(HttpServletRequest request, String name) {
	    if (request.getCookies() == null) return null;
	    for (Cookie c : request.getCookies()) {
	        if (name.equals(c.getName())) {
	            return c.getValue();
	        }
	    }
	    return null;
	}
	
	// mTLS 검사 생략 URL 클래스
	private boolean isMtlsExcluded(HttpServletRequest request) {
	    String path = request.getServletPath(); // 또는 getRequestURI()
	    for (String pattern : EXCLUDE_PATTERNS) {
	        if (pathMatcher.match(pattern, path)) {
	            return true;
	        }
	    }
	    return false;
	}
}
