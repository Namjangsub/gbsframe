package com.dksys.biz.user.sm.sm50.service;

import java.util.List;
import java.util.Map;

public interface SM50Svc {

	List<Map<String, String>> selectBomCostTreeList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectBomlevelList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectBomAllCostList(Map<String, String> paramMap);

	List<Map<String, String>> selectBomAllLevelTempList(Map<String, String> paramMap);

	Map<String, String> callBomTempUpd(Map<String, String> paramMap);

	List<Map<String, String>> selectBomAllEnterList(Map<String, String> paramMap);

	Map<String, String> selectBomTrgtPchsPcostInfo(Map<String, String> paramMap);

	int insertUpdateBomRemark(Map<String, String> paramMap) throws Exception;

	Map<String, String> selectSalesCdSearchSm50Info(Map<String, String> paramMap);

	Map<String, String> selectOrdrsSearchSm50Info(Map<String, String> paramMap);

	List<Map<String, String>> selectOrdrsBomCostTreeList(Map<String, String> paramMap);

	List<Map<String, String>> selectOrdrsBomAllCostList(Map<String, String> paramMap);

	Map<String, String> selectOrdrsBomTrgtPchsPcostInfo(Map<String, String> paramMap);

	List<Map<String, String>> selectSalesCdBomRemarkList(Map<String, String> paramMap);
}