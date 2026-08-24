package com.dksys.biz.user.gm.gm01.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dksys.biz.user.gm.gm01.mapper.GM01Mapper;
import com.dksys.biz.user.gm.gm01.service.GM01Svc;

@Service
@Transactional(rollbackFor = Exception.class)
public class GM01SvcImpl implements GM01Svc {

	@Autowired
	GM01Mapper gm01Mapper;

	@Override
	public int selectSecUsbCount(Map<String, String> paramMap) {
		return gm01Mapper.selectSecUsbCount(paramMap);
	}

	@Override
	public List<Map<String, String>> selectSecUsbList(Map<String, String> paramMap) {
		return gm01Mapper.selectSecUsbList(paramMap);
	}

	@Override
	@SuppressWarnings("unchecked")
	public int insertSecUsbOut(Map<String, Object> paramMap) {
		List<Map<String, Object>> dtlList = (List<Map<String, Object>>) paramMap.get("dtlList");
		if (dtlList == null || dtlList.isEmpty()) {
			throw new RuntimeException("상세 정보는 최소 1건 이상이어야 합니다.");
		}

		Map<String, String> headerMap = new HashMap<>();
		for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
			if (!"dtlList".equals(entry.getKey()) && entry.getValue() != null) {
				headerMap.put(entry.getKey(), String.valueOf(entry.getValue()));
			}
		}

		String nextMngNo = gm01Mapper.selectSecUsbNextMngNo(headerMap);
		headerMap.put("mngNo", nextMngNo);
		int result = gm01Mapper.insertSecUsbOut(headerMap);

		if (result > 0) {
			for (int i = 0; i < dtlList.size(); i++) {
				Map<String, Object> dtl = dtlList.get(i);
				Map<String, String> dtlMap = new HashMap<>();
				dtlMap.put("coCd", headerMap.get("coCd"));
				dtlMap.put("mngNo", nextMngNo);
				dtlMap.put("dtlSeq", String.format("%04d", i + 1));
				dtlMap.put("salesCd", dtl.get("salesCd") != null ? String.valueOf(dtl.get("salesCd")) : "");
				dtlMap.put("clntPjt", dtl.get("clntPjt") != null ? String.valueOf(dtl.get("clntPjt")) : "");
				dtlMap.put("clntPjtNm", dtl.get("clntPjtNm") != null ? String.valueOf(dtl.get("clntPjtNm")) : "");
				dtlMap.put("eqpNm", dtl.get("eqpNm") != null ? String.valueOf(dtl.get("eqpNm")) : "");
				dtlMap.put("userId", headerMap.get("userId"));
				gm01Mapper.insertSecUsbDtl(dtlMap);
			}
		}

		return result;
	}

	@Override
	@SuppressWarnings("unchecked")
	public int updateSecUsbOut(Map<String, Object> paramMap) {
		List<Map<String, Object>> dtlList = (List<Map<String, Object>>) paramMap.get("dtlList");
		if (dtlList == null || dtlList.isEmpty()) {
			throw new RuntimeException("상세 정보는 최소 1건 이상이어야 합니다.");
		}

		Map<String, String> headerMap = new HashMap<>();
		for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
			if (!"dtlList".equals(entry.getKey()) && entry.getValue() != null) {
				headerMap.put(entry.getKey(), String.valueOf(entry.getValue()));
			}
		}

		int result = gm01Mapper.updateSecUsbOut(headerMap);

		if (result > 0) {
			gm01Mapper.deleteSecUsbDtlAll(headerMap);
			for (int i = 0; i < dtlList.size(); i++) {
				Map<String, Object> dtl = dtlList.get(i);
				Map<String, String> dtlMap = new HashMap<>();
				dtlMap.put("coCd", headerMap.get("coCd"));
				dtlMap.put("mngNo", headerMap.get("mngNo"));
				dtlMap.put("dtlSeq", String.format("%04d", i + 1));
				dtlMap.put("salesCd", dtl.get("salesCd") != null ? String.valueOf(dtl.get("salesCd")) : "");
				dtlMap.put("clntPjt", dtl.get("clntPjt") != null ? String.valueOf(dtl.get("clntPjt")) : "");
				dtlMap.put("clntPjtNm", dtl.get("clntPjtNm") != null ? String.valueOf(dtl.get("clntPjtNm")) : "");
				dtlMap.put("eqpNm", dtl.get("eqpNm") != null ? String.valueOf(dtl.get("eqpNm")) : "");
				dtlMap.put("userId", headerMap.get("userId"));
				gm01Mapper.insertSecUsbDtl(dtlMap);
			}
		}

		return result;
	}

	@Override
	public int updateSecUsbIn(Map<String, String> paramMap) {
		return gm01Mapper.updateSecUsbIn(paramMap);
	}

	@Override
	public int cancelSecUsbIn(Map<String, String> paramMap) {
		return gm01Mapper.updateSecUsbInCancel(paramMap);
	}

	@Override
	public int deleteSecUsb(Map<String, String> paramMap) {
		gm01Mapper.deleteSecUsbDtlAll(paramMap);
		return gm01Mapper.deleteSecUsb(paramMap);
	}

	@Override
	public List<Map<String, String>> selectSecUsbDtlList(Map<String, String> paramMap) {
		return gm01Mapper.selectSecUsbDtlList(paramMap);
	}

}
