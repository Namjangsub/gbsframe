package com.dksys.biz.user.mail.service;

import java.util.Map;

public interface MailLoggingSvc {

    public int insertMailLog(Map<String, String> paramMap);

    public int updateMailErrorStatus(Map<String, String> paramMap);
}
