package com.dksys.biz.user.wb.wb30.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dksys.biz.user.wb.wb30.mapper.WB30Mapper;
import com.dksys.biz.user.wb.wb30.service.WB30Svc;

@Service
@Transactional(rollbackFor = Exception.class)
public class WB30SvcImpl implements WB30Svc {

	@Autowired
	WB30Mapper wb30Mapper;

	@Override
	public List<Map<String, Object>> selectWbsTmplatList(Map<String, String> paramMap) {
		return wb30Mapper.selectWbsTmplatList(paramMap);
	}

	@Override
	public List<Map<String, Object>> selectWbsTmplatDetailList(Map<String, String> paramMap) {
		return wb30Mapper.selectWbsTmplatDetailList(paramMap);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int saveWbsTmplat(Map<String, Object> paramMap) throws Exception {
		int result = 0;
		// 1. 마스터 정보 수정 시도 -> 실패(0건) 시 신규 등록
		result = wb30Mapper.updateWbsTmplatMaster(paramMap);
		if (result == 0) {
			result = wb30Mapper.insertWbsTmplatMaster(paramMap);
		}

		// 2. 상세 단계 목록 저장 (전달된 목록이 있는 경우 기존 상세 삭제 후 재등록)
		if (paramMap.containsKey("detailList")) {
			Map<String, Object> delParam = new HashMap<String, Object>();
			delParam.put("coCd", paramMap.get("coCd"));
			delParam.put("tmplatId", paramMap.get("tmplatId"));
			wb30Mapper.deleteWbsTmplatDetail(delParam);

			@SuppressWarnings("unchecked")
			List<Map<String, Object>> detailList = (List<Map<String, Object>>) paramMap.get("detailList");
			if (detailList != null && !detailList.isEmpty()) {
				int sortOrd = 1;
				for (Map<String, Object> detail : detailList) {
					detail.put("coCd", paramMap.get("coCd"));
					detail.put("tmplatId", paramMap.get("tmplatId"));
					detail.put("userId", paramMap.get("userId"));
					detail.put("pgmId", paramMap.get("pgmId"));
					if (!detail.containsKey("sortOrd") || detail.get("sortOrd") == null) {
						detail.put("sortOrd", sortOrd++);
					}
					wb30Mapper.insertWbsTmplatDetail(detail);
				}
			}
		}

		return result;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int deleteWbsTmplat(Map<String, Object> paramMap) throws Exception {
		wb30Mapper.deleteWbsTmplatDetail(paramMap);
		return wb30Mapper.deleteWbsTmplatMaster(paramMap);
	}

}
