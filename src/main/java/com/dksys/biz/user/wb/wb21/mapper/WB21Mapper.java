package com.dksys.biz.user.wb.wb21.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Mapper
public interface WB21Mapper {
	List<Map<String, String>> selectMaxSjNo(Map<String, String> paramMap);
	
	List<Map<String, String>> selectCodeList(Map<String, String> paramMap);
	
	int selectSjListCount(Map<String, String> paramMap);
	
	List<Map<String, String>> selectSjList(Map<String, String> paramMap);
	
	List<Map<String, String>> deleteSjListChk(Map<String, String> paramMap);
	
	int deleteSjList(Map<String, String> paramMap);
	
	int selectSjSeqNext(Map<String, String> paramMap);
	
	int insertSjDtlList(Map<String, String> paramMap);
	
    int sjInsert(Map<String, String> paramMap);
    
    int sjUpdate(Map<String, String> paramMap);
	
	List<Map<String, String>> selectSjDtlList(Map<String, String> paramMap);
	
	Map<String, String> selectSjInfo(Map<String, String> paramMap);
	
	int sjConfirmY(Map<String, String> paramMap);
	
	int sjCloseY(Map<String, String> paramMap);
	
    int sjConfirmN(Map<String, String> paramMap);
	
	int sjCloseN(Map<String, String> paramMap);
	
	
}