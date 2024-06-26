package com.dksys.biz.user.zz.zz01.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ZZ01Mapper {
	
	List<Map<String, String>> selectBomTreeList(Map<String, String> paramMap);

	List<Map<String, String>> selectBomlevelList(Map<String, String> paramMap);
	
	Map<String, String> selectBomTreInfo(Map<String, String> paramMap);
	
	Map<String, String> selectBomRootSalesCdTree(Map<String, String> paramMap);

	int selectBomSeqNext(Map<String, String> paramMap);
	
	int checkBomId(Map<String, String> paramMap);

	int insertBomTree(Map<String, String> paramMap);

	int updateBom(Map<String, String> paramMap);
	
	int moveBom(Map<String, String> paramMap);
	
	int deleteBom(Map<String, String> paramMap);
	
	int copyBomTree(Map<String, String> paramMap);

	Map<String, String> selectBomInfo(Map<String, String> paramMap);
	
	List<Map<String, String>> selectBomAllLevelList(Map<String, String> paramMap);
	
	int checkBomInfo(Map<String, String> paramMap);
}