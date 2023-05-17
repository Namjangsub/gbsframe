package com.dksys.biz.admin.cm.cm08.mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CM08Mapper {

	int insertFile(HashMap<String, String> param);
	int insertTreeFile(HashMap<String, String> param);
	int insertTreeFileWithDeg(HashMap<String, String> param);
	String selectNextFileTrgtKey();
	List<Map<String, String>> selectFileList(Map<String, String> paramMap);

	int selectTreeFileCount(Map<String, String> paramMap);
	
	List<Map<String, String>> selectTreeFileList(Map<String, String> paramMap);

	List<Map<String, String>> selectTreeFileModule(Map<String, String> paramMap);

	String selectNextFileKey() ;


	Map<String, String> selectFileInfo(String fileKey);

	int deleteFileInfo(String fileKey);

	int selectConfirmCount(Map<String, String> paramMap);
	
	int moveFile(Map<String, String> paramMap);

	int deleteFileCall(Map<String, String> paramMap);
	
	Map<String, String> selectFileInfoUser(Map<String, String> paramMap);

}