package com.dksys.biz.user.cr.cr02.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;



@Mapper
public interface CR02Mapper {

	int selectOrdrsCount(Map<String, String> param);

	List<Map<String, Object>> selectOrdrsList(Map<String, String> param);


	Map<String, Object> selectOrdrsInfo(Map<String, String> paramMap);

	List<Map<String, Object>> selectPmntPlan(Map<String, String> paramMap);

	// 추가 필요한 메서드들을 여기에 선언해주세요.
}