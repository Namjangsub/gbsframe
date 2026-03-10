package com.dksys.biz.user.cr.cr09.service;

import java.util.List;
import java.util.Map;

public interface CR09Svc {

    /**
     * 물류진행 예정정보 현황 목록 조회
     * @param paramMap 조회 조건
     * @return 리스트
     */
    List<Map<String, Object>> selectLgistPlanList(Map<String, Object> paramMap);

    /**
     * 물류완료 처리
     * @param paramList 처리대상 리스트
     * @throws Exception
     */
    void updateLgistCompl(List<Map<String, Object>> paramList) throws Exception;

    /**
     * 물류완료 취소 처리
     * @param paramList 처리대상 리스트
     * @throws Exception
     */
    void cancelLgistCompl(List<Map<String, Object>> paramList) throws Exception;
}
