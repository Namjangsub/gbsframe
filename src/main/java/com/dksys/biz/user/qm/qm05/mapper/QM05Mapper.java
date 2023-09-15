package com.dksys.biz.user.qm.qm05.mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Mapper
public interface QM05Mapper {
	
	//그리드 리스트
	List<Map<String, String>> selectMainGridList(Map<String, String> paramMap);

}
