package com.dksys.biz.auth.service.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.dksys.biz.auth.mapper.ClientCertMapper;
import com.dksys.biz.auth.service.ClientCertService;

@Service
@Transactional
public class ClientCertServiceImpl implements ClientCertService {

    @Autowired
    ClientCertMapper clientCertMapper;

	
    public Map<String, String> findByCertSerial(Map<String, String> clientCertParm) {
    	String certSerial = clientCertParm.get("serialHex");
        if (!StringUtils.hasText(certSerial)) {
            return null;
        }

        // Hex Serial 을 대문자로 normalize (DB도 대문자로 관리한다고 가정)
        String normalized = certSerial.trim().toUpperCase();
        clientCertParm.put("certSerial", normalized);

        // 조회 성공여부 상관 없이 인증서 Serial 접근 이력 남기기
        clientCertMapper.updateByCertSerial(clientCertParm);
        
        return clientCertMapper.selectByCertSerial(clientCertParm);
    }
}