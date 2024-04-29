package com.dksys.biz.admin.cm.cm14.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CM14Mapper {
	
	int insertBoard(Map<String, String> paramMap);

	List<Map<String, String>> selectBoardList(Map<String, String> paramMap);

	int selectBoardCount(Map<String, String> paramMap);

	Map<String, String> selectBoardInfo(Map<String, String> paramMap);

	int updateBoard(Map<String, String> paramMap);

	List<String> selectBoardPopList();

	int deleteBoard(Map<String, String> paramMap);
}
