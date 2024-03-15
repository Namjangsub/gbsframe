package com.dksys.biz.user.qm.qm07.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dksys.biz.user.qm.qm07.mapper.QM07Mapper;
import com.dksys.biz.user.qm.qm07.service.QM07Svc;

@Service
@Transactional(rollbackFor = Exception.class)
public class QM07SvcImpl implements QM07Svc {

  @Autowired
  QM07Mapper QM07Mapper;

  @Override
  public int selectQualityReqSCDSTSListCount(Map<String, String> paramMap) {
    return QM07Mapper.selectQualityReqSCDSTSListCount(paramMap);
  }
  
  @Override
  public List<Map<String, String>> selectQualityReqSCDSTSList(Map<String, String> paramMap) {
    return QM07Mapper.selectQualityReqSCDSTSList(paramMap);
  }
  
  @Override
  public List<Map<String, String>> selectQualityReqSCDSTSDept(Map<String, String> paramMap) {
    return QM07Mapper.selectQualityReqSCDSTSDept(paramMap);
  }
  
  @Override
  public List<Map<String, String>> selectQualityReqSCDSTSClnt(Map<String, String> paramMap) {
    return QM07Mapper.selectQualityReqSCDSTSClnt(paramMap);
  }
  
  @Override
  public List<Map<String, String>> selectQualityReqSCDSTSPrjct(Map<String, String> paramMap) {
    return QM07Mapper.selectQualityReqSCDSTSPrjct(paramMap);
  }
}
