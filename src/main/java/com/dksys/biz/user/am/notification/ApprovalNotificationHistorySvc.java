package com.dksys.biz.user.am.notification;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.dksys.biz.user.am.am11.mapper.AM11Mapper;

/** 이력 저장 실패가 발송 결과나 큐 트랜잭션까지 롤백시키지 않도록 격리한다. */
@Service
public class ApprovalNotificationHistorySvc {
    @Autowired
    private AM11Mapper am11Mapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public int save(Map<String, String> param) {
        return am11Mapper.mergeNotificationSendLog(param);
    }
}
