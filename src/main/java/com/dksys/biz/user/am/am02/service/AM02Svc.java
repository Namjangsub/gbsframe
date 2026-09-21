package com.dksys.biz.user.am.am02.service;

import java.util.List;
import java.util.Map;

public interface AM02Svc {

    int selectFormCount(Map<String, Object> paramMap);

    List<Map<String, Object>> selectFormList(Map<String, Object> paramMap);

    Map<String, Object> selectFormDetail(Map<String, Object> paramMap);

    Map<String, Object> saveForm(Map<String, Object> paramMap);
}
