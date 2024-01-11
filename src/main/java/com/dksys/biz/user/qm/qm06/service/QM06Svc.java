package com.dksys.biz.user.qm.qm06.service;

import java.util.List;
import java.util.Map;

import org.springframework.ui.ModelMap;
import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface QM06Svc {

	  int selectQualityReqListCount(Map<String, String> paramMap);
	  
	  List<Map<String, String>> selectQualityReqList(Map<String, String> paramMap);
	  
}
