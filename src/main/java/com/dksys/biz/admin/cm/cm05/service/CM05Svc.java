package com.dksys.biz.admin.cm.cm05.service;

import java.util.List;
import java.util.Map;

public interface CM05Svc {

	public int selectCodeCount(Map<String, String> param);

    public List<Map<String, String>> selectCodeList(Map<String, String> param);

	public int selectPdskCodeCount(Map<String, String> param);

    public List<Map<String, String>> selectPdskCodeList(Map<String, String> param);

    public List<Map<String, String>> selectChildCodeList(Map<String, String> param);

    public List<Map<String, String>> selectComboCodeList(Map<String, String> param);

    public List<Map<String, String>> selectPtchildCodeList(Map<String, String> param);

    public Map<String, String> selectCodeInfo(Map<String, String> param);

    public List<Map<String, String>> selectCodeInfoList(Map<String, String> param);

	public int insertCode(Map<String, String> param) throws Exception;

	public int deleteCode(Map<String, String> param);

	public List<Map<String, String>> selectDocTreeList(Map<String, String> param);

	public List<Map<String, String>> selectDocTreeListAuth(Map<String, String> param);

    public List<Map<String, String>> selectCheckboxList(Map<String, String> param);
    
    public List<Map<String, String>> selectMonthCloseChkList(Map<String, String> param);

    public Map<String, String> selectProjectCodeLastNoInfo(Map<String, String> param);

	public int prdtDivInsert(Map<String, String> param);

	public int selectPrjectCodeDupCheck(Map<String, String> param);
}