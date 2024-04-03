package com.dksys.biz.user.pm.pm06.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dksys.biz.user.pm.pm06.mapper.PM06Mapper;
import com.dksys.biz.user.pm.pm06.service.PM06Svc;

@Service
@Transactional(rollbackFor = Exception.class)
public class PM06Svcimpl implements PM06Svc {

  @Autowired
  PM06Mapper pm06Mapper;

  @Override
  public int selectTripRptListCount(Map<String, String> paramMap) {
    return pm06Mapper.selectTripRptListCount(paramMap);
  }

  @Override
  public List<Map<String, String>> selectTripRptList(Map<String, String> paramMap) {
    return pm06Mapper.selectTripRptList(paramMap);
  }

  
}