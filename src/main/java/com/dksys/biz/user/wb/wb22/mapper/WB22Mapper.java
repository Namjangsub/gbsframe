package com.dksys.biz.user.wb.wb22.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Mapper
public interface WB22Mapper {
	List<Map<String, String>> selectWbsLeftSalesCodeList(Map<String, String> paramMap);
	
	Map<String, String> selectSjInfo(Map<String, String> paramMap);
	
	List<Map<String, String>> selectWBS1Level(Map<String, String> paramMap);
	
	List<Map<String, String>> selectMaxWbsPlanNo(Map<String, String> paramMap);
	
	int selectWbsSeqNext(Map<String, String> paramMap);
	
	int wbsLevel1Insert(Map<String, String> paramMap);
	
	int wbsLevel1Update(Map<String, String> paramMap);
	
	List<Map<String, String>> selectWBS2Level(Map<String, String> paramMap);
	
	int wbsLevel2Insert(Map<String, String> paramMap);
	
	List<Map<String, String>> wbsPlanListChk(Map<String, String> paramMap);
	
	int deleteWbsPlanlist(Map<String, String> paramMap);
	
}