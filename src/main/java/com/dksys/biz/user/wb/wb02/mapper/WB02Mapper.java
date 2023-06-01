package com.dksys.biz.user.wb.wb02.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Mapper
public interface WB02Mapper {

	List<Map<String, String>> selectWbsRsltsPlanList(Map<String, String> paramMap);

}