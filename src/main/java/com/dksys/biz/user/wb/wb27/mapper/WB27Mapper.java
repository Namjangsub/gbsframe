package com.dksys.biz.user.wb.wb27.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WB27Mapper {

    List<Map<String, Object>> selectResourceKpi(Map<String, String> paramMap);

    Map<String, Object> selectGaugeKpi(Map<String, String> paramMap);

    List<Map<String, Object>> selectIssueKpi(Map<String, String> paramMap);

    List<Map<String, Object>> selectBreakdownKpi(Map<String, String> paramMap);

}
