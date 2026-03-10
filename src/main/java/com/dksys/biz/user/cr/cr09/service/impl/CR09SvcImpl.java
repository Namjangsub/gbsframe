package com.dksys.biz.user.cr.cr09.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dksys.biz.user.cr.cr09.mapper.CR09Mapper;
import com.dksys.biz.user.cr.cr09.service.CR09Svc;

@Service
@Transactional(rollbackFor = Exception.class)
public class CR09SvcImpl implements CR09Svc {

    @Autowired
    private CR09Mapper cr09Mapper;

    @Override
    public List<Map<String, Object>> selectLgistPlanList(Map<String, Object> paramMap) {
        return cr09Mapper.selectLgistPlanList(paramMap);
    }

    @Override
    public void updateLgistCompl(List<Map<String, Object>> paramList) throws Exception {
        for (Map<String, Object> item : paramList) {
            cr09Mapper.updateLgistCompl(item);
        }
    }

    @Override
    public void cancelLgistCompl(List<Map<String, Object>> paramList) throws Exception {
        for (Map<String, Object> item : paramList) {
            cr09Mapper.cancelLgistCompl(item);
        }
    }
}
