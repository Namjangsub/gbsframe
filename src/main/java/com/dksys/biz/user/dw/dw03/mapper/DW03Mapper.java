package com.dksys.biz.user.dw.dw03.mapper;

import java.util.List;
import java.util.Map;

public interface DW03Mapper {

	List<Map<String, String>> selectDrawDocTreeList(Map<String, String> paramMap);

	int selectDrawTreeFileListCount(Map<String, String> paramMap);

	List<Map<String, String>> selectDrawTreeFileList(Map<String, String> paramMap);

	List<Map<String, String>> select_dw03_detailList(Map<String, String> paramMap);
	
}
