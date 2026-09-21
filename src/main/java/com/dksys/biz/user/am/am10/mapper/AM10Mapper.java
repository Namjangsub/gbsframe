package com.dksys.biz.user.am.am10.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AM10Mapper {

    Map<String, Object> selectApprovalDashboardCount(Map<String, Object> paramMap);

    List<Map<String, Object>> selectDashboardWaitList(Map<String, Object> paramMap);
}
