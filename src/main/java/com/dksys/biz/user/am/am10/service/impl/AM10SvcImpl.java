package com.dksys.biz.user.am.am10.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dksys.biz.user.am.am10.mapper.AM10Mapper;
import com.dksys.biz.user.am.am10.service.AM10Svc;

@Service
@Transactional(rollbackFor = Exception.class)
public class AM10SvcImpl implements AM10Svc {

    @Autowired
    private AM10Mapper am10Mapper;

    @Override
    public Map<String, Object> selectApprovalDashboardCount(Map<String, Object> paramMap) {
        return am10Mapper.selectApprovalDashboardCount(paramMap);
    }

    @Override
    public List<Map<String, Object>> selectDashboardWaitList(Map<String, Object> paramMap) {
        return am10Mapper.selectDashboardWaitList(paramMap);
    }
}
