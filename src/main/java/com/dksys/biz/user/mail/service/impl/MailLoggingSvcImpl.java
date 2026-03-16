package com.dksys.biz.user.mail.service.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.dksys.biz.user.mail.mapper.MailMapper;
import com.dksys.biz.user.mail.service.MailLoggingSvc;

@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class MailLoggingSvcImpl implements MailLoggingSvc {

    @Autowired
    MailMapper mailMapper;

    @Override
    public int insertMailLog(Map<String, String> paramMap) {
        return mailMapper.insertMail(paramMap);
    }
    
    @Override
    public int updateMailErrorStatus(Map<String, String> paramMap) {
        return mailMapper.updateMailError(paramMap);
    }
}
