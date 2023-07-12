package com.dksys.biz.user.cr.cr10.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CR10Mapper {

  int selectLgistReqPageCount(Map<String, String> paramMap);

  List<Map<String, String>> selectLgistReqPageList(Map<String, String> paramMap);
  
  List<Map<String, String>> selectBomDetailList(Map<String, String> paramMap);

  List<Map<String, String>> selectBuyBomList(Map<String, String> paramMap);

  List<Map<String, String>> selectBomMatrList(Map<String, String> paramMap);
  
  Map<String, String> selectBomMatrInfo(Map<String, String> paramMap);
  
  int selectPrjctSeqNext(Map<String, String> paramMap);
  
  int insertBom(Map<String, String> paramMap);

  int updateBom(Map<String, String> paramMap);

  int deleteBomAll(Map<String, String> paramMap);

  int deleteBom(Map<String, String> paramMap);
  
  int deleteBomMatrAll(Map<String, String> paramMap);

  int deleteBomMatr(Map<String, String> paramMap);
  
  int updateBomMatr(Map<String, String> paramMap);
  
  int insertBomMatr(Map<String, String> paramMap);
  
}