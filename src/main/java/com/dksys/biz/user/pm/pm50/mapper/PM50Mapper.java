package com.dksys.biz.user.pm.pm50.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PM50Mapper {
	int select_pm50_ListCount(Map<String, String> paramMap);

    List<Map<String, Object>> select_pm50_List(Map<String, String> paramMap);

	int selectSendFileCount(Map<String, String> paramMap);
	
	List<Map<String, String>> selectSendFileList(Map<String, String> paramMap);

	Map<String, String> select_pm50_Info(Map<String, String> paramMap);

	Map<String, String> select_salesCd_info(Map<String, String> paramMap);

	List<Map<String, String>> selectBfuFileRows(Map<String, String> paramMap);

	List<Map<String, String>> selectBfuAllFileRows(Map<String, String> paramMap);

	int insert_pm50_main(Map<String, String> paramMap);

	int insert_pm50_d01(Map<String, String> fileMap);

	String fileTrgtKeySeqNext(Map<String, String> paramMap);
	
	int update_pm50_main(Map<String, String> paramMap);

	int update_pm50_d01(Map<String, String> tripMap);

	int update_issue_pm50_d01(Map<String, Object> tripMap);

	int update_issue_reset_pm50_d01(Map<String, String> tripMap);

	int delete_pm50_main(Map<String, String> paramMap);

	int delete_pm50_d01(Map<String, String> tripMap);

	int delete_file_approval(Map<String, String> paramMap);

	
}
