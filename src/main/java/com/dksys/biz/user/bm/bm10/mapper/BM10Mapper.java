package com.dksys.biz.user.bm.bm10.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Mapper
public interface BM10Mapper {

	int selectBmPrdtMstrCount(Map<String, String> paramMap);

	List<Map<String, String>> selectBmPrdtMstrList(Map<String, String> paramMap);

	List<Map<String, String>> selectBmProdSearchList(Map<String, String> paramMap);

	String insertBmPrdtMstr(Map<String, String> paramMap, MultipartHttpServletRequest mRequest);

	int updateBmPrdtMstr(Map<String, String> paramMap);

}
