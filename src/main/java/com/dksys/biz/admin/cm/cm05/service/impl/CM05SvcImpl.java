package com.dksys.biz.admin.cm.cm05.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dksys.biz.admin.cm.cm05.mapper.CM05Mapper;
import com.dksys.biz.admin.cm.cm05.service.CM05Svc;

@Service
public class CM05SvcImpl implements CM05Svc {

    @Autowired
    CM05Mapper cm05Mapper;

    @Override
	public int selectCodeCount(Map<String, String> param) {
		return cm05Mapper.selectCodeCount(param);
	}

	@Override
	public List<Map<String, String>> selectCodeList(Map<String, String> param) {
		return cm05Mapper.selectCodeList(param);
	}

    @Override
	public int selectPdskCodeCount(Map<String, String> param) {
		return cm05Mapper.selectPdskCodeCount(param);
	}

	@Override
	public List<Map<String, String>> selectPdskCodeList(Map<String, String> param) {
		return cm05Mapper.selectPdskCodeList(param);
	}

	@Override
	public List<Map<String, String>> selectChildCodeList(Map<String, String> param) {
		return cm05Mapper.selectChildCodeList(param);
	}

	@Override
	public List<Map<String, String>> selectComboCodeList(Map<String, String> param) {
    	String kindStr = param.get("codeKindList");
    	if(kindStr != null && !"".equals(kindStr)) {
    		String[] codeKindArray = kindStr.split(",");
    		String kindList = "";
    		for(int i=0; i< codeKindArray.length; i++){
    			if(i==0) {
    				kindList += "'"+codeKindArray[i].toString()+"'";
    			} else {
    				kindList += ",'"+codeKindArray[i].toString()+"'";
    			}
    		}
    		param.put("codeKindList", kindList);
    	}

    	List<Map<String, String>> resultList = cm05Mapper.selectComboCodeList(param);
		return resultList;
	}

	@Override
	public List<Map<String, String>> selectPtchildCodeList(Map<String, String> param) {
		return cm05Mapper.selectPtchildCodeList(param);
	}

	@Override
	public Map<String, String> selectCodeInfo(Map<String, String> param) {
		return cm05Mapper.selectCodeInfo(param);
	}

	@Override
	public List<Map<String, String>> selectCodeInfoList(Map<String, String> param) {
		return cm05Mapper.selectCodeInfoList(param);
	}

	@Override
	public int insertCode(Map<String, String> param) throws Exception {
		//프로젝트 코드일경우 자동 채번으로 번호 등록함, 최종 변호는 dzCode에 저장함
		if (("PRJCTCD".equals(param.get("codeKind"))) && ("C".equals(param.get("actionType")))) {
			// 중복프로잭트명이 있는지 체크하여 한건이라도 있으면 오류 발생 시킴
			int dupCount = cm05Mapper.selectPrjectCodeDupCheck(param);
			if (dupCount > 0 ) {
				
			}
			Map<String, String> resultList = cm05Mapper.selectProjectCodeLastNoInfo(param);
			param.put("codeId", "PRJCT" + resultList.get("lastNo").trim());
			param.put("dzCode", resultList.get("lastNo"));
		}
		return cm05Mapper.insertCode(param);
	}

	@Override
	public int deleteCode(Map<String, String> param) {
		return cm05Mapper.deleteCode(param);
	}

	@Override
	public List<Map<String, String>> selectDocTreeList(Map<String, String> param) {
		return cm05Mapper.selectDocTreeList(param);
	}

	@Override
	public List<Map<String, String>> selectDocTreeListAuth(Map<String, String> param) {
		return cm05Mapper.selectDocTreeListAuth(param);
	}

	@Override
	public List<Map<String, String>> selectCheckboxList(Map<String, String> param) {
		return cm05Mapper.selectCheckboxList(param);
	}
	
	@Override
	public List<Map<String, String>> selectMonthCloseChkList(Map<String, String> param) {
		return cm05Mapper.selectMonthCloseChkList(param);
	}

	@Override
	public Map<String, String> selectProjectCodeLastNoInfo(Map<String, String> param) {
		return cm05Mapper.selectProjectCodeLastNoInfo(param);
	}

	@Override
	public int prdtDivInsert(Map<String, String> param) {
		Map<String, String> resultList = cm05Mapper.selectPrdtCodeLastNoInfo(param);
		param.put("codeId", param.get("codeKind") + resultList.get("lastNo").trim());
		param.put("dzCode", resultList.get("lastNo"));
		return cm05Mapper.insertCode(param);
	}

    @Override
	public int selectPrjectCodeDupCheck(Map<String, String> param) {
		return cm05Mapper.selectPrjectCodeDupCheck(param);
	}

	
}