package com.dksys.biz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
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