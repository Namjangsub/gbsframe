//package com.dksys.biz.config;
//
//import java.io.IOException;
//
//import javax.servlet.FilterChain;
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import com.dksys.biz.main.vo.User;
//
//@Component
//public class LoginTimeValidationFilter extends OncePerRequestFilter {
//
////	@Autowired
////	private UserLoginLogService userLoginLogService;
////
////	@Override
////	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
////			throws ServletException, IOException {
////
////		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
////
////		if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof User) {
////
////			User user = (User) authentication.getPrincipal();
////			String userAgent = RequestUtils.getUserAgent();
////			String clientIp = RequestUtils.getClientIp();
////			if (!userLoginLogService.isLoginWithin24Hours(user.getId(), userAgent, clientIp)) {
////				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
////				response.setContentType("application/json;charset=UTF-8");
////				response.getWriter().write("{\"error\": \"세션 만료: 마지막 로그인 24시간 초과\"}");
////				return;
////			}
////		}
////
////		filterChain.doFilter(request, response);
////	}
//}
