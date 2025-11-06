package com.dksys.biz.user.sm.sm50.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dksys.biz.user.sm.sm50.mapper.SM50Mapper;
import com.dksys.biz.user.sm.sm50.service.SM50Svc;

@Service
@Transactional(rollbackFor = Exception.class)
public class SM50SvcImpl implements SM50Svc {
    
    @Autowired
    SM50Mapper bm50Mapper;
    
    @Autowired
    SM50Svc sm50Svc;

    
    @Override
    public List<Map<String, String>> selectBomCostTreeList(Map<String, String> paramMap) {

        //Top SalesCd 정보 가져오기
//      Map<String, String> pcostInfo = sm50Svc.selectBomTrgtPchsPcostInfo(paramMap);
        
        // BOM비용 산정 프로시져 호출
        bm50Mapper.callBomTempUpd(paramMap);
        
        return bm50Mapper.selectBomCostTreeList(paramMap);
    }

    @Override
    public List<Map<String, String>> selectBomlevelList(Map<String, String> paramMap) {
        return bm50Mapper.selectBomlevelList(paramMap);
    }
    
    @Override
    public List<Map<String, String>> selectBomAllCostList(Map<String, String> paramMap) {
        // BOM비용 산정 프로시져 호출
//      Map<String, String> resultTemp = sm50Svc.callBomTempUpd(paramMap);
        
        return bm50Mapper.selectBomAllCostList(paramMap);
    }

    
    
    @Override
    public List<Map<String, String>> selectBomAllLevelTempList(Map<String, String> paramMap) {
        return bm50Mapper.selectBomAllLevelTempList(paramMap);
    }
    
    
    @Override
    public Map<String, String> callBomTempUpd(Map<String, String> paramMap) {
        bm50Mapper.callBomTempUpd(paramMap);
        return paramMap;
    }
    
    @Override
    public List<Map<String, String>> selectBomAllEnterList(Map<String, String> paramMap) {
        return bm50Mapper.selectBomAllEnterList(paramMap);
    }
    

    
    @Override
    public Map<String, String> selectBomTrgtPchsPcostInfo(Map<String, String> paramMap) {
        return bm50Mapper.selectBomTrgtPchsPcostInfo(paramMap);
    }

	@Override
	public int insertUpdateBomRemark(Map<String, String> paramMap) throws Exception {
		return bm50Mapper.insertUpdateBomRemark(paramMap);
	}

	@Override
	public Map<String, String> selectSalesCdSearchSm50Info(Map<String, String> paramMap) {
		return bm50Mapper.selectSalesCdSearchSm50Info(paramMap);
	}

	@Override
	public Map<String, String> selectOrdrsSearchSm50Info(Map<String, String> paramMap) {
		return bm50Mapper.selectOrdrsSearchSm50Info(paramMap);
	}

	@Override
	public List<Map<String, String>> selectOrdrsBomCostTreeList(Map<String, String> paramMap) {

		// 수주번호로 설비(salesCd)를 가져와서 반복해서 BOM비용산정 프로시저 호출
		List<Map<String, String>> dtlList = bm50Mapper.selectSalesCdListByOrdrsNo(paramMap);
		if (dtlList.size() > 0) {
			
			for (Map<String, String> dtl : dtlList) {
				Map<String,String> param = new HashMap<>(paramMap);
				param.put("salesCd", dtl.get("salesCd"));
				// BOM비용 산정 프로시져 호출
				bm50Mapper.callBomTempUpd(param);
			}
		}
		return bm50Mapper.selectOrdrsBomCostTreeList(paramMap);
	}

	@Override
	public List<Map<String, String>> selectOrdrsBomAllCostList(Map<String, String> paramMap) {
		return bm50Mapper.selectOrdrsBomAllCostList(paramMap);
	}

	@Override
	public Map<String, String> selectOrdrsBomTrgtPchsPcostInfo(Map<String, String> paramMap) {
		return bm50Mapper.selectOrdrsBomTrgtPchsPcostInfo(paramMap);
	}

}