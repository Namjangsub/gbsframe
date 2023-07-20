package com.dksys.biz;

import org.apache.poi.sl.usermodel.ObjectMetaData.Application;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class BizApplication {

	public static void main(String[] args) {
		System.setProperty("spring.profiles.active", "local");
		SpringApplication.run(BizApplication.class, args);
	}
}
//@SpringBootApplication
//public class BizApplication extends SpringBootServletInitializer {
//	
//	public static void main(String[] args) {
//		System.setProperty("spring.profiles.active", "local");
//		SpringApplication.run(BizApplication.class, args);
//	}
//	
//	@Override
//	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
//		return builder.sources(Application.class);
//	}
//	
//}