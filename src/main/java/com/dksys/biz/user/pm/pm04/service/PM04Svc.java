package com.dksys.biz.user.pm.pm04.service;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface PM04Svc {
	
	 List<Map<String, String>> selectDailyWorkMainList(Map<String, String> paramMap);
	 
	 List<Map<String, String>> select_all_name(Map<String, String> paramMap);

}
