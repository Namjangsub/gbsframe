package com.dksys.biz.admin.bm.bm16.service;


import java.util.List;
import java.util.Map;

public interface BM16Svc {

	int selectPrjctCount(Map<String, String> paramMap);

	List<Map<String, String>> selectPrjctList(Map<String, String> paramMap);

}