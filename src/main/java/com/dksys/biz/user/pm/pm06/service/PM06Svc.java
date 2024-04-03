package com.dksys.biz.user.pm.pm06.service;

import java.util.List;
import java.util.Map;

public interface PM06Svc {

  int selectTripRptListCount(Map<String, String> paramMap);

  List<Map<String, String>> selectTripRptList(Map<String, String> paramMap);

}