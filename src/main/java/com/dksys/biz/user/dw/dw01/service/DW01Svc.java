package com.dksys.biz.user.dw.dw01.service;

import com.dksys.biz.user.dw.dw01.model.AuditIngestReq;

public interface DW01Svc {
    void insertHistory(AuditIngestReq req);
    void ingestAudit(AuditIngestReq req);
}