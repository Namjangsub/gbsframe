package com.dksys.biz.admin.cm.cm12.service;

import java.util.List;
import java.util.Map;

public interface CM12Svc {
	
    public List<Map<String, String>> selectSolarLunarEventHolidaysList(Map<String, String> paramMap);

	public List<Map<String, String>> selectDocFileTreeList(Map<String, String> param);

	public int selectDocCustTreeFileCount(Map<String, String> param);

	public List<Map<String, String>> selectDocCustTreeFileList(Map<String, String> param);
	
}