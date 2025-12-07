package com.dksys.biz.auth.service;


import java.util.Map;

public interface ClientCertService {

    /**
     * 클라이언트 인증서 Serial 기준으로 등록된 사용자/단말 매핑 조회.
     *  - USE_YN = 'Y'
     *  - EXP_DT >= 오늘 또는 NULL
     */
	Map<String, String> findByCertSerial(Map<String, String> clientCertParm);
	
}
