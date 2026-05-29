package com.dksys.biz.user.wb.wb27.service;

import java.util.List;
import java.util.Map;

public interface WB27Svc {

    List<Map<String, Object>> selectResourceKpi(Map<String, String> paramMap);

    Map<String, Object> selectGaugeKpi(Map<String, String> paramMap);

    List<Map<String, Object>> selectIssueKpi(Map<String, String> paramMap);

    List<Map<String, Object>> selectBreakdownKpi(Map<String, String> paramMap);

    List<Map<String, Object>> selectPrdtGrpList(Map<String, String> paramMap);

}
