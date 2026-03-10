package com.dksys.biz.user.cr.cr09.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CR09Mapper {

    /**
     * 물류진행 예정정보 현황 목록 조회
     * @param paramMap 조회 조건 (회사, 사업부, 시작일, 종료일, 고객사, Sales CD, 상태 등)
     * @return 물류진행 예정정보 리스트
     */
    List<Map<String, Object>> selectLgistPlanList(Map<String, Object> paramMap);

    /**
     * 물류완료 처리
     * @param paramMap (lgistNo, userId 등)
     * @return 처리 건수
     */
    int updateLgistCompl(Map<String, Object> paramMap);

    /**
     * 물류완료 취소 처리
     * @param paramMap (lgistNo, userId 등)
     * @return 처리 건수
     */
    int cancelLgistCompl(Map<String, Object> paramMap);
}
