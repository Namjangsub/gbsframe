package com.dksys.biz.user.cr.cr10.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CR10Mapper {

  int selectLgistReqPageCount(Map<String, String> paramMap);

  List<Map<String, String>> selectLgistReqPageList(Map<String, String> paramMap);

  List<Map<String, String>> selectLgistReqList(Map<String, String> paramMap);

  List<Map<String, String>> selectSelesCdList(Map<String, String> paramMap);

  List<Map<String, String>> selectSelesCdViewList(Map<String, String> paramMap);

  List<Map<String, String>> selectTodoAppReqList(Map<String, String> paramMap);

  Map<String, String> selectLgistMastInfo(Map<String, String> paramMap);

  String selectLgistAppCount(Map<String, String> paramMap);

  List<Map<String, String>> selectLgistAppList(Map<String, String> paramMap);

  String selectLgistNoNext(Map<String, String> paramMap);

  int selectTodoAppCount(Map<String, String> paramMap);

  int insertLgistMast(Map<String, String> paramMap);

  int updateLgistMast(Map<String, String> paramMap);

  int deleteLgistMast(Map<String, String> paramMap);

  int deleteLgistDetailAll(Map<String, String> paramMap);

  int deleteLgistDetail(Map<String, String> paramMap);

  int updateLgistDetail(Map<String, String> paramMap);

  int insertLgistDetail(Map<String, String> paramMap);

  int insertTodoAppList(Map<String, String> paramMap);

  int updateLgistMastTodoApp(Map<String, String> paramMap);

  int updateTodoAppConfirm(Map<String, String> paramMap);

}