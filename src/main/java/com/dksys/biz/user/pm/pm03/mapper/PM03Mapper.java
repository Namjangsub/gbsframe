package com.dksys.biz.user.pm.pm03.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PM03Mapper {

  int selectDeliveryPageCount(Map<String, String> paramMap);

  List<Map<String, String>> selectDeliveryPageList(Map<String, String> paramMap);

  List<Map<String, String>> selectDeliveryList(Map<String, String> paramMap);

  List<Map<String, String>> selectSelesCdList(Map<String, String> paramMap);

  Map<String, String> selectDeliveryMastInfo(Map<String, String> paramMap);

  String selectOutNoNext(Map<String, String> paramMap);

  int insertDeliveryMast(Map<String, String> paramMap);

  int updateDeliveryMast(Map<String, String> paramMap);

  int deleteDeliveryMast(Map<String, String> paramMap);

  int deleteLgistSalesCdAll(Map<String, String> paramMap);

  int deleteLgistSalesCd(Map<String, String> paramMap);

  int updateLgistSalesCd(Map<String, String> paramMap);

  int insertLgistSalesCd(Map<String, String> paramMap);

}