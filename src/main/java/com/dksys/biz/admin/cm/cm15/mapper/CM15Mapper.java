package com.dksys.biz.admin.cm.cm15.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
 
@Mapper
public interface CM15Mapper {

	int updateFileAuth(Map<String, String> detailMap);

	int selectTreeAuthCount(Map<String, String> paramMap);
	
	List<Map<String, String>> selectTreeAuthList(Map<String, String> paramMap);

	Map<String, String> selectFileAuthInfo(Map<String, String> paramMap);

	int selectFileAuthCheck(String fileKey);

	int deleteFileAuthInfo(Map<String, String> detailMap);
}