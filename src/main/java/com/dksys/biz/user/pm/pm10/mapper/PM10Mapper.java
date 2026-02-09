package com.dksys.biz.user.pm.pm10.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PM10Mapper {

	List<Map<String, String>> selectMnList(Map<String, String> paramMap);

	List<Map<String, String>> select_p10_d02_List(Map<String, String> paramMap);

	int pm10_main_update(Map<String, String> param);

	int pm10_d01_update(Map<String, String> param);

	int pm10_d03_update(Map<String, String> param);

	int deleteMnM01(Map<String, String> paramMap);

	int deleteMnD01(Map<String, String> paramMap);

	int deleteMnD02(Map<String, String> paramMap);

	int deleteMnD03(Map<String, String> paramMap);

	int deleteMnFile(Map<String, String> paramMap);

	int pm10_d01_sortNo_update(Map<String, Object> param);

	int pm10_d02_update(Map<String, Object> param);

	int pm10_d02_delete(Map<String, Object> param);

	int lockD01Cell(Map<String, String> param);

	int lockD03Cell(Map<String, String> param);

	int unlockD01Cell(Map<String, String> param);

	int unlockD03Cell(Map<String, String> param);

	int unlockD01ByUser(Map<String, String> param);

	int unlockD03ByUser(Map<String, String> param);

	Map<String, String> selectD01Cell(Map<String, String> param);

	Map<String, String> selectD03Cell(Map<String, String> param);

}
