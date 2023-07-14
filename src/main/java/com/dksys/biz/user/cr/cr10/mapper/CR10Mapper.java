package com.dksys.biz.user.cr.cr10.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CR10Mapper {

  int selectLgistReqPageCount(Map<String, String> paramMap);

  List<Map<String, String>> selectLgistReqPageList(Map<String, String> paramMap);
  
  List<Map<String, String>> selectSelesCdList(Map<String, String> paramMap);

  List<Map<String, String>> selectBuyBomList(Map<String, String> paramMap);

  List<Map<String, String>> selectBomMatrList(Map<String, String> paramMap);
  
  Map<String, String> selectBomMatrInfo(Map<String, String> paramMap);
  
  String selectFileTrgtKeyNext(Map<String, String> paramMap);
  
  int insertLgistMast(Map<String, String> paramMap);

  int updateLgistMast(Map<String, String> paramMap);

  int deleteBomAll(Map<String, String> paramMap);

  int deleteBom(Map<String, String> paramMap);
  
  int deleteBomMatrAll(Map<String, String> paramMap);

  int deleteBomMatr(Map<String, String> paramMap);
  
  int updateLgistMastMatr(Map<String, String> paramMap);
  
  int insertLgistMastMatr(Map<String, String> paramMap);
  
}