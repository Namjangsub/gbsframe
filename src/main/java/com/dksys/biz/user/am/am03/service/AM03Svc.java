package com.dksys.biz.user.am.am03.service;

import java.util.List;
import java.util.Map;

public interface AM03Svc {

    List<Map<String, Object>> selectLinePresetList(Map<String, Object> paramMap);

    List<Map<String, Object>> selectLinePresetDetail(Map<String, Object> paramMap);

    Map<String, Object> saveLinePreset(Map<String, Object> paramMap);

    Map<String, Object> deleteLinePreset(Map<String, Object> paramMap);

    List<Map<String, Object>> selectDelegateList(Map<String, Object> paramMap);

    Map<String, Object> saveDelegate(Map<String, Object> paramMap);

    Map<String, Object> updateDelegateStatus(Map<String, Object> paramMap);
}
