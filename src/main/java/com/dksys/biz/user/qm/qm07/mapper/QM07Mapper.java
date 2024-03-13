package com.dksys.biz.user.qm.qm07.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QM07Mapper {

	int selectQualityReqSCDSTSListCount(Map<String, String> paramMap);
	
	List<Map<String, String>> selectQualityReqSCDSTSList(Map<String, String> paramMap);

}
