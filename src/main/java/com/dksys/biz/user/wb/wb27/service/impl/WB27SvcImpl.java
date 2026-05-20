package com.dksys.biz.user.wb.wb27.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dksys.biz.user.wb.wb27.mapper.WB27Mapper;
import com.dksys.biz.user.wb.wb27.service.WB27Svc;

@Service
@Transactional(rollbackFor = Exception.class)
public class WB27SvcImpl implements WB27Svc {

    @Autowired
    WB27Mapper wb27Mapper;

    @Override
    public List<Map<String, Object>> selectResourceKpi(Map<String, String> paramMap) {
        return wb27Mapper.selectResourceKpi(paramMap);
    }

    @Override
    public Map<String, Object> selectGaugeKpi(Map<String, String> paramMap) {
        return wb27Mapper.selectGaugeKpi(paramMap);
    }

    @Override
    public List<Map<String, Object>> selectIssueKpi(Map<String, String> paramMap) {
        return wb27Mapper.selectIssueKpi(paramMap);
    }

    @Override
    public List<Map<String, Object>> selectBreakdownKpi(Map<String, String> paramMap) {
        return wb27Mapper.selectBreakdownKpi(paramMap);
    }

}
