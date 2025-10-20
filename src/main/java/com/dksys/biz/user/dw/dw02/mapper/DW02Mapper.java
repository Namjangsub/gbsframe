package com.dksys.biz.user.dw.dw02.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DW02Mapper {

	int drawingAuditsListCount(Map<String, String> paramMap);

	List<Map<String, String>> drawingAuditsList(Map<String, String> paramMap);

	List<Map<String, String>> selectDrawDocTreeList(Map<String, String> paramMap);

	int selectDrawTreeFileListCount(Map<String, String> paramMap);

	List<Map<String, String>> selectDrawTreeFileList(Map<String, String> paramMap);

	int deleteDrawDocItem(Map<String, String> paramMap);

	Map<String, Object> selectDrawDocItemInfo(Map<String, String> paramMap);

	int insertDrawItem(Map<String, String> paramMap);

	int updateDrawItem(Map<String, String> paramMap);

	int updateRelDtNew(Map<String, String> paramMap);

	int updateRelDtInit(Map<String, String> paramMap);
	
}