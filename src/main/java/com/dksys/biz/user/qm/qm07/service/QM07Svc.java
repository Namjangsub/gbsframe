package com.dksys.biz.user.qm.qm07.service;

import java.util.List;
import java.util.Map;

public interface QM07Svc {

	  int selectQualityReqSCDSTSListCount(Map<String, String> paramMap);
	  
	  List<Map<String, String>> selectQualityReqSCDSTSList(Map<String, String> paramMap);
}
