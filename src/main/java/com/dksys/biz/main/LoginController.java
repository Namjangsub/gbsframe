package com.dksys.biz.main;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dksys.biz.HomeController;
import com.dksys.biz.admin.cm.cm01.service.CM01Svc;
import com.dksys.biz.admin.cm.cm06.service.CM06Svc;
import com.dksys.biz.config.RequestUtils;
import com.dksys.biz.main.service.LoginService;
import com.dksys.biz.main.vo.User;
import com.dksys.biz.util.MessageUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class LoginController {

    private final PasswordEncoder passwordEncoder;

    @Autowired
	MessageUtils messageUtils;
    
    @Autowired
    LoginService loginService;
    
    @Autowired
    CM01Svc cm01Svc;
    
    @Autowired
    CM06Svc cm06Svc;

    @Autowired
    private HomeController homeController;
    enum FormFactor { DESKTOP, PHONE, TABLET, UNKNOWN }

	@Value("${security.jwt.signing-key}")
	private String signingKey;
//    // 회원가입
//    @PostMapping("/join")
//    public String join(@RequestBody Map<String, String> user, ModelMap model) {
//    	String id = userRepository.save(User.builder()
//        		.id(user.get("email").split("@")[0])
//                .email(user.get("email"))
//                .password(passwordEncoder.encode(user.get("password")))
//                .roles(Collections.singletonList("ROLE_USER")) // 최초 가입시 USER 로 설정
//                .build()).getId();
//    	model.addAttribute("id", id);
//        model.addAttribute("msg", "가입 되었습니다.");
//        return "jsonView";
//    }

    // 로그인
    @RequestMapping("/login")
    public String login(@RequestBody Map<String, String> param, ModelMap model) {
    	User user = loginService.selectUserInfo(param);

    	if(user == null) {
    		model.addAttribute("msg", "ID를 확인해주세요.");
    	}else {
    		if("N".equals(user.getUseYn())) {
    			model.addAttribute("msg", "비활성화된 계정입니다 관리자에게 문의하세요.\n담당자 연락처: 010-XXXX-XXXX");
    		}else {
    			if(!passwordEncoder.matches(param.get("password"), user.getPassword())) {
    				model.addAttribute("msg", "비밀번호를 확인해주세요.");
    	    		param.put("isPwErr", "Y");
    	    		param.put("userId", param.get("id"));
    	    		model.addAttribute("usrInfo", cm06Svc.updatePwErrCnt(param));
    	    	} else {
    	    		loginService.insertUserHistory(user);
    	    		model.addAttribute("msg", "success");
    	    		param.put("isPwErr", "N");
    	    		param.put("userId", param.get("id"));
					Map<String, String> usrInfo = cm06Svc.updatePwErrCnt(param);
					model.addAttribute("usrInfo", usrInfo);
    	    	}
    		}
    	}
        return "jsonView";
    }
    
//    @GetMapping(value = "/logout")
//    public String logoutPage(HttpServletRequest request, HttpServletResponse response) {
//      new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());
//      return "redirect:/";
//    }
//    

	// 쿠키 삭제 처리 (로그아웃 시)

	// 토큰 갱신 클라이언트 흐름 (요약)
	// 1. 브라우저에서 API 호출
	// 2. AccessToken 만료 → 401 Unauthorized 발생
	// 3. JS에서 자동 `/oauth/token` POST 요청:
	// - grant_type=refresh_token
	// - 쿠키의 refresh_token 자동 포함 (withCredentials: true)
	// 4. 서버에서 쿠키에서 refresh_token 추출 후 검증
	// 5. 최근 로그인 24시간 초과 시 `InvalidGrantException` 반환 (재로그인 유도)
	// 6. 통과하면 새로운 access_token 발급
	// 7. 클라이언트는 다시 원래 요청 재시도 (retry mechanism)
    @GetMapping("/customLogout")
//	public String logout(HttpServletRequest request, HttpServletResponse response) {
	 public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
		
		//토큰 필요없이 userId만 있음면 됨. (logout)
    	try {
//    	    Map<String, String[]> map = request.getParameterMap();
//    	    for (String key : map.keySet()) {
//    	        String[] values = map.get(key);
//    	        System.out.println("PARAM " + key + " = " + Arrays.toString(values));
//    	    }
    	    
    		String username = request.getParameter("userId");
    		if (!"undefined".equals(username)) {
	    		// 1. 클라이언트 access_token 헤더에서 추출
	//    	    String accessToken = resolveAccessToken(request);
	//    	    String username = null;
	    		String userAgent = RequestUtils.getUserAgent();
	            String deviceType = RequestUtils.detectDeviceType(userAgent);
	            com.dksys.biz.HomeController.FormFactor ff = homeController.detectFormFactor(request);
	            deviceType = ff.name();
	    		String clientIp = RequestUtils.getClientIp();
	    		loginService.updateLogoutTime(username, userAgent, clientIp, deviceType);
    		}
		} catch (Exception e) {
			// 오류 처리하지 않음
		}
		
//		// 2. 토큰이 있다면 블랙리스트 등록
//	    if (accessToken != null && !accessToken.isEmpty()) {
//			try {
//				Claims claims = parseJwt(accessToken);
//				username = claims.get("user_name", String.class); // 또는 "userId"
//				Date expiration = claims.getExpiration();
//				long remainingMillis = expiration.getTime() - System.currentTimeMillis();
//				if (remainingMillis > 0) {
//	                // 블랙리스트 처리
////    				loginService.blacklistAccessToken(accessToken, remainingMillis);
//				}
//				loginService.updateLogoutTime(username, userAgent, clientIp);
//			} catch (JwtException e) {
//				// 만료 또는 잘못된 토큰 → 무시
//				// 무시하고 refresh_token에서 시도
//			}
//		}
	    
//	 // 2. accessToken에서 username을 못 얻었다면 → refresh_token 시도
//	    if (username == null) {
//	        String refreshToken = extractRefreshToken(request);
//	        if (refreshToken != null && !refreshToken.isEmpty()) {
//	            try {
//	                Claims refreshClaims = parseJwt(refreshToken);
//	                username = refreshClaims.get("user_name", String.class); // 또는 "userId"
//	            } catch (JwtException e) {
//	                // refresh_token도 만료 또는 손상 → 무시
//	            	System.out.println("refresh_token JwtException 처리 불가~~"+e);
//	            } catch (Exception e) {
//	                // refresh_token도 만료 또는 손상 → 무시
//	            	System.out.println("refresh_token Exception 처리 불가~~"+e);
//	            }
//	        }
//	    }
	    
		// 3. 쿠키 삭제
	    RequestUtils.clearCookie("access_token", response);
	    RequestUtils.clearCookie("refresh_token", response);
		// 4. SecurityContext 강제 초기화 (auth == null 일 수 있어도 상관 없음)
		SecurityContextHolder.clearContext();
		System.out.println("-------->  로그아웃 처리 완료됨~~");
//		return "redirect:/";

        Map<String, Object> result = new HashMap<>();
        result.put("msg", "로그아웃 완료");
        return ResponseEntity.ok().body(result);
	}


//    private void clearCookie(String name, HttpServletResponse response) {
//        ResponseCookie deleteCookie = ResponseCookie.from(name, "")
//            .path("/")
//            .httpOnly(true)
//            .secure(true)
//            .maxAge(0)
//            .sameSite("None")
//            .build();
//        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
//    }

	private String resolveAccessToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

	private Claims parseJwt(String token) {
        return Jwts.parser()
            .setSigningKey(signingKey.getBytes(StandardCharsets.UTF_8))
            .parseClaimsJws(token)
            .getBody();
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
}