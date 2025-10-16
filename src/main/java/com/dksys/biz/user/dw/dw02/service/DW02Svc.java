package com.dksys.biz.user.dw.dw02.service;

import java.util.List;
import java.util.Map;

public interface DW02Svc {

	int drawingAuditsListCount(Map<String, String> paramMap);

	List<Map<String, String>> drawingAuditsList(Map<String, String> paramMap);

	List<Map<String, String>> selectDrawDocTreeList(Map<String, String> paramMap);

	int selectDrawTreeFileListCount(Map<String, String> paramMap);

	List<Map<String, String>> selectDrawTreeFileList(Map<String, String> paramMap);

    // DATA DELETE
    int deleteDrawDocItem(Map<String, String> paramMap) throws Exception;

	Map<String, Object> selectDrawDocItemInfo(Map<String, String> paramMap);

	int insertDrawItem(Map<String, String> paramMap);

	int updateDrawItem(Map<String, String> paramMap);

}
