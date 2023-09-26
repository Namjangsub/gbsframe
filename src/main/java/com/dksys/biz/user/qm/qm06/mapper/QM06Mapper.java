package com.dksys.biz.user.qm.qm06.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QM06Mapper {
	
	List<Map<String, String>> selectQualityReqList(Map<String, String> paramMap);

}
