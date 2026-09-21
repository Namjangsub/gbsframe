package com.dksys.biz.user.am.am10.service;

import java.util.List;
import java.util.Map;

public interface AM10Svc {

    Map<String, Object> selectApprovalDashboardCount(Map<String, Object> paramMap);

    List<Map<String, Object>> selectDashboardWaitList(Map<String, Object> paramMap);
}
