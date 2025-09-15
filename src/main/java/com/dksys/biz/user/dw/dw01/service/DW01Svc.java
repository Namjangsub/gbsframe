package com.dksys.biz.user.dw.dw01.service;

import java.util.List;
import java.util.Map;

public interface DW01Svc {
	
	List<Map<String, String>> selectDrawDocTreeList(Map<String, String> paramMap);

	int selectDrawTreeFileListCount(Map<String, String> paramMap);

	List<Map<String, String>> selectDrawTreeFileList(Map<String, String> paramMap);
}
