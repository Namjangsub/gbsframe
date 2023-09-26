package com.dksys.biz.user.qm.qm06.service.impl;

import java.lang.reflect.Type;
import java.text.Format.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bouncycastle.asn1.ocsp.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.user.qm.qm06.mapper.QM06Mapper;
import com.dksys.biz.user.qm.qm06.service.QM06Svc;
import com.dksys.biz.admin.cm.cm15.service.CM15Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class QM06SvcImpl implements QM06Svc {

  @Autowired
  QM06Mapper QM06Mapper;

  @Autowired
  CM15Svc cm15Svc;
  
  @Override
  public List<Map<String, String>> selectQualityReqList(Map<String, String> paramMap) {
    return QM06Mapper.selectQualityReqList(paramMap);
  }
}
