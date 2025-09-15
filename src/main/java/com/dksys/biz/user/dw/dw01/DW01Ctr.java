package com.dksys.biz.user.dw.dw01;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.dksys.biz.user.dw.dw01.model.AuditIngestReq;
import com.dksys.biz.user.dw.dw01.service.DW01Svc;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class DW01Ctr {

    private final DW01Svc dw01Svc;

    // PowerShell 수집기 수신 엔드포인트
    @PostMapping("/api/audit-ingest")
    public ResponseEntity<?> ingestAudit(@RequestBody AuditIngestReq req,
                                         @RequestHeader(value = "Authorization", required = false) String authorization,
                                         @RequestHeader(value = "X-API-TOKEN", required = false) String xApiToken) {
        // TODO: 필요 시 토큰 검증(Authorization: Bearer ... 또는 X-API-TOKEN) 추가
    	dw01Svc.insertHistory(req);
        dw01Svc.ingestAudit(req);
        return ResponseEntity.ok().build();
    }
}