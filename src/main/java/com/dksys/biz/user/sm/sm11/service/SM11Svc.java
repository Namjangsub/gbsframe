package com.dksys.biz.user.sm.sm11.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface SM11Svc {

  int selectContractCount(Map<String, String> paramMap);

  List<Map<String, String>> selectContractList(Map<String, String> paramMap);

  Map<String, String> selectContractInfo(Map<String, String> paramMap);

  int insertContract(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

  int updateContract(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

  int deleteContract(Map<String, String> paramMap) throws Exception;

  List<Map<String, String>> selectTurnKeySalesCodeList(Map<String, String> paramMap);

  int etcPchsOrderMasterReport(Map<String, String> paramMap, MultipartHttpServletRequest mRequest);

  int updateMailEtcPchsOrderConfirm(Map<String, String> param);
  
  int selectContractLPayCount(Map<String, String> paramMap);
  
  List<Map<String, String>> selectContractLPayList(Map<String, String> paramMap);

  int createContractBill(Map<String, String> paramMap) throws Exception;
  
  int deleteContractBill(Map<String, String> paramMap) throws Exception;
  
}