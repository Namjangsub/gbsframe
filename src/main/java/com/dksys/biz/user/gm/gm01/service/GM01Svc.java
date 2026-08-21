package com.dksys.biz.user.gm.gm01.service;

import java.util.List;
import java.util.Map;

public interface GM01Svc {

	int selectSecUsbCount(Map<String, String> paramMap);

	List<Map<String, String>> selectSecUsbList(Map<String, String> paramMap);

	int insertSecUsbOut(Map<String, String> paramMap);

	int updateSecUsbOut(Map<String, String> paramMap);

	int updateSecUsbIn(Map<String, String> paramMap);

	int cancelSecUsbIn(Map<String, String> paramMap);

	int deleteSecUsb(Map<String, String> paramMap);

}
