package com.dksys.biz.advice;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class ExceptionAdvice {

	private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

	@ExceptionHandler({IllegalArgumentException.class, RuntimeException.class})
	public ModelAndView illegalArgumentException(HttpServletRequest req, Exception e) {
		// 기존에는 예외를 잡기만 하고 로그를 남기지 않아 콘솔에서 원인 파악이 불가능했음.
		// 스택트레이스를 ERROR로 남겨 서버 콘솔에서 실제 원인을 확인할 수 있게 한다.
		logger.error("요청 처리 중 예외 발생: url={}, msg={}", req.getRequestURL(), e.getMessage(), e);
		ModelAndView mav = new ModelAndView("jsonView");
		mav.addObject("url", req.getRequestURL());
		mav.addObject("msg", e.getMessage());
		return mav;
	}

}