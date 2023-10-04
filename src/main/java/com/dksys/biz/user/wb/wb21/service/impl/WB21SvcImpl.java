package com.dksys.biz.user.wb.wb21.service.impl;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.user.wb.wb21.mapper.WB21Mapper;
import com.dksys.biz.user.wb.wb21.service.WB21Svc;
import com.dksys.biz.util.ExceptionThrower;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class WB21SvcImpl implements WB21Svc {
    
    @Autowired
    WB21Mapper wb21Mapper;

    
    @Autowired
    WB21Svc wb21Svc;
    

    @Autowired
    ExceptionThrower thrower;
    
    @Override
    public List<Map<String, String>> selectMaxSjNo(Map<String, String> paramMap) {
  		return wb21Mapper.selectMaxSjNo(paramMap);
    }
    
    @Override
    public List<Map<String, String>> selectSalesCodeCheck(Map<String, String> paramMap) {
  		return wb21Mapper.selectSalesCodeCheck(paramMap);
    }
    
    @Override
    public List<Map<String, String>> selectCodeList(Map<String, String> paramMap) {
  		return wb21Mapper.selectCodeList(paramMap);
    }
    
    @Override
	public int selectSjListCount(Map<String, String> paramMap) {
		return wb21Mapper.selectSjListCount(paramMap);
	}

	@Override
	public List<Map<String, String>> selectSjList(Map<String, String> paramMap) {
		return wb21Mapper.selectSjList(paramMap);
	}
	
	@Override
	public List<Map<String, String>> deleteSjListChk(Map<String, String> paramMap) {
		return wb21Mapper.deleteSjListChk(paramMap);
	}
	
	@Override
	public int deleteSjList(Map<String, String> paramMap) {
		int result = wb21Mapper.deleteSjList(paramMap);
		return result;
	}
	
	@Override
	public int sjInsert(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
	
		int fileTrgtKey = wb21Mapper.selectSjSeqNext(paramMap);
		paramMap.put("fileTrgtKey", Integer.toString(fileTrgtKey));
		
		Gson gson = new Gson(); 
		Type stringList = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowListArr"), stringList);
		if (sharngArr != null && sharngArr.size() > 0 ) {
	        for (Map<String, String> sharngMap : sharngArr) {
	            try {
            	    wb21Mapper.insertSjDtlList(sharngMap);
	            } catch (Exception e) {
	                System.out.println("error2"+e.getMessage());
	            }
	        }
		}

		int result = wb21Mapper.sjInsert(paramMap);
	    return result;
	}
    
	@Override
	public int sjUpdate(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		Gson gson = new Gson(); 
		Type stringList = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowListArr"), stringList);
		if (sharngArr != null && sharngArr.size() > 0 ) {
	        for (Map<String, String> sharngMap : sharngArr) {
	            try {
	            	wb21Mapper.insertSjDtlList(sharngMap);
	            } catch (Exception e) {
	                System.out.println("error2"+e.getMessage());
	            }
	        }
		}

		int result = wb21Mapper.sjUpdate(paramMap);
	    return result;
	}
	
	@Override
	public List<Map<String, String>> selectSjDtlList(Map<String, String> paramMap) {
		return wb21Mapper.selectSjDtlList(paramMap);
	}
	
	@Override
	public Map<String, String> selectSjInfo(Map<String, String> paramMap) {
		return wb21Mapper.selectSjInfo(paramMap);
	}
	
	@Override
	public int sjConfirmY(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		int result = wb21Mapper.sjConfirmY(paramMap);
		result = wb21Mapper.sjCloseY(paramMap);
	    return result;
	}
	
	@Override
	public int sjConfirmN(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		int result = wb21Mapper.sjConfirmN(paramMap);
		result = wb21Mapper.sjCloseN(paramMap);
	    return result;
	}
	
	@Override
	public int sjVerUpInsert(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
	
		int fileTrgtKey = wb21Mapper.selectSjSeqNext(paramMap);
		paramMap.put("fileTrgtKey", Integer.toString(fileTrgtKey));
		
		Gson gson = new Gson(); 
		Type stringList = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowListArr"), stringList);
		if (sharngArr != null && sharngArr.size() > 0 ) {
	        for (Map<String, String> sharngMap : sharngArr) {
	            try {
            	    wb21Mapper.insertSjDtlList(sharngMap);
	            } catch (Exception e) {
	                System.out.println("error2"+e.getMessage());
	            }
	        }
		}

		int result = wb21Mapper.sjInsert(paramMap);
		if(result != 0) {
			return fileTrgtKey;
		}else {
			return result;
		}
	}
	
	@Override
    public List<Map<String, String>> selectSjVerNoNext(Map<String, String> paramMap) {
  		return wb21Mapper.selectSjVerNoNext(paramMap);
    }
	
}