package com.dksys.biz.admin.cm.cm05;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dksys.biz.admin.cm.cm05.service.CM05Svc;
import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.util.MessageUtils;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/admin/cm/cm05")
public class CM05Ctr {

	@Autowired
	MessageUtils messageUtils;

    @Autowired
    CM05Svc cm05Svc;

    // 공통코드 리스트 조회
    @PostMapping("/selectCodeList")
    public String selectCodeList(@RequestBody Map<String, String> param, ModelMap model) {
    	int totalCnt = cm05Svc.selectCodeCount(param);
    	PaginationInfo paginationInfo = new PaginationInfo(param, totalCnt);
    	model.addAttribute("paginationInfo", paginationInfo);

    	List<Map<String, String>> codeList = cm05Svc.selectCodeList(param);
    	model.addAttribute("codeList", codeList);
        return "jsonView";
    }

    // 공통코드 리스트 조회
    @PostMapping("/selectPdskCodeList")
    public String selectPdskCodeList(@RequestBody Map<String, String> param, ModelMap model) {
    	int totalCnt = cm05Svc.selectPdskCodeCount(param);
    	PaginationInfo paginationInfo = new PaginationInfo(param, totalCnt);
    	model.addAttribute("paginationInfo", paginationInfo);

    	List<Map<String, String>> codeList = cm05Svc.selectPdskCodeList(param);
    	model.addAttribute("codeList", codeList);
        return "jsonView";
    }

    // 하위코드 리스트 조회
    @PostMapping("/selectChildCodeList")
    public String selectChildCodeList(@RequestBody Map<String, String> param, ModelMap model) {
    	List<Map<String, String>> childCodeList = cm05Svc.selectChildCodeList(param);
    	model.addAttribute("childCodeList", childCodeList);
        return "jsonView";
    }

    // 코드 리스트 조회
    @PostMapping("/selectComboCodeList")
    public String selectComboCodeList(@RequestBody Map<String, String> param, ModelMap model) {
    	List<Map<String, String>> resultList = cm05Svc.selectComboCodeList(param);
    	model.addAttribute("resultList", resultList);
        return "jsonView";
    }

    // 하위코드 리스트 조회2
    @PostMapping("/selectPtchildCodeList")
    public String selectPtchildCodeList(@RequestBody Map<String, String> param, ModelMap model) {
    	List<Map<String, String>> PtchildCodeList = cm05Svc.selectPtchildCodeList(param);
    	model.addAttribute("PtchildCodeList", PtchildCodeList);
        return "jsonView";
    }

    // 공통코드 정보 조회
    @PostMapping("/selectCodeInfo")
    public String selectCodeInfo(@RequestBody Map<String, String> param, ModelMap model) {
    	Map<String, String> codeInfo = cm05Svc.selectCodeInfo(param);
    	model.addAttribute("codeInfo", codeInfo);
        return "jsonView";
    }

    // 공통코드 정보 리스트 조회
    @PostMapping("/selectCodeInfoList")
    public String selectCodeInfoList(@RequestBody Map<String, String> param, ModelMap model) {
    	List<Map<String, String>> codeInfoList = cm05Svc.selectCodeInfoList(param);
    	model.addAttribute("codeInfoList", codeInfoList);
        return "jsonView";
    }

    // 공통코드 등록/수정
    @PostMapping("/insertCode")
    public String insertCode(@RequestBody Map<String, String> param, ModelMap model) {
    	try {
    		cm05Svc.insertCode(param);
    		model.addAttribute("resultCode", 200);
    		model.addAttribute("resultMessage", "C".equals(param.get("actionType")) ? messageUtils.getMessage("insert") : messageUtils.getMessage("update"));
    	} catch(Exception e) {
    		model.addAttribute("resultCode", 500);
    		model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
    	}
    	return "jsonView";
    }

    // 공통코드삭제
    @PutMapping("/deleteCode")
    public String deleteCode(@RequestBody Map<String, String> param, ModelMap model) {
    	cm05Svc.deleteCode(param);
    	model.addAttribute("resultCode", 200);
    	model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
    	return "jsonView";
    }

    // 공통코드 정보 리스트 조회
    @PostMapping("/selectDocTreeList")
    public String selectDocTreeList(@RequestBody Map<String, String> param, ModelMap model) {
    	List<Map<String, String>> docTreeList = cm05Svc.selectDocTreeList(param);
    	model.addAttribute("docTreeList", docTreeList);
        return "jsonView";
    }

    // 공통코드 정보 리스트 조회
    @PostMapping("/selectDocTreeListAuth")
    public String selectDocTreeListAuth(@RequestBody Map<String, String> param, ModelMap model) {
    	List<Map<String, String>> docTreeList = cm05Svc.selectDocTreeListAuth(param);
    	model.addAttribute("docTreeList", docTreeList);
    	return "jsonView";
    }

    // 작업일보 checkbox하위 코드 리스트 조회
    @PostMapping("/selectCheckboxList")
    public String selectCheckboxList(@RequestBody Map<String, String> param, ModelMap model) {
    	List<Map<String, String>> childCodeList = cm05Svc.selectCheckboxList(param);
    	model.addAttribute("childCodeList", childCodeList);
        return "jsonView";
    }

    // 월마감 리스트 조회
    @PostMapping("/selectMonthCloseChk")
    public String selectMonthCloseChkList(@RequestBody Map<String, String> param, ModelMap model) {
    	
    	String chkMonth = "";
    	chkMonth = param.get("chkValue") == "" ? "" : param.get("chkValue").substring(0, 6);
    	
    	param.put("chkMonth", chkMonth);
    	List<Map<String, String>> resultList = cm05Svc.selectMonthCloseChkList(param);
    	
    	String rtnVal = "N";
    	String rtnHolVal = "N";     //영업일마감여부
    	String menuUrl = (String)param.get("menuUrl").toString();
    	
    	String closeYn = "N";       //마감년월여부
    	String pchsCloseYn = "N";   //메입마감여부
    	String sellCloseYn = "N";   //매출마감여부
    	String prdctnCloseYn = "N"; //생산마감여부
    	
    	String workDay = "";
    	String pchsCloseDt = "N";
    	
    	if(resultList != null) {

        	closeYn = resultList.get(0).get("closeYn").toString();
        	pchsCloseYn = resultList.get(0).get("pchsCloseYn").toString();
        	sellCloseYn = resultList.get(0).get("sellCloseYn").toString();
        	prdctnCloseYn = resultList.get(0).get("prdctnCloseYn").toString();
        	
        	workDay = resultList.get(0).get("workDay").toString();
        	pchsCloseDt = resultList.get(0).get("pchsCloseDt").toString();
    	}
    	
    	if("CR0201M01".equals(menuUrl)) {  //수주관리
    		
	    	rtnVal = closeYn;
    	}else if("PM0101M01".equals(menuUrl)) {  //작업일보

	    	rtnVal = prdctnCloseYn;
    	}else if("CR0701M01".equals(menuUrl) || "CR0801M01".equals(menuUrl) || "CR0501M01".equals(menuUrl) || "CR1001M01".equals(menuUrl)) {
    		//매출확정, 매출계산서, 수금, 물류진행요청

	        rtnVal = sellCloseYn;
    	}else if("SM0301M01".equals(menuUrl) || "SM1401M01".equals(menuUrl) || "SM1001M01".equals(menuUrl) || "SM0601M01".equals(menuUrl)) {
    		//입고, 매입확정 및 계산서, 구매비용, 반품

	        rtnVal = pchsCloseYn;
    	}else if("SM0201M01".equals(menuUrl) || "QM0101M01".equals(menuUrl) || "WB2401M01".equals(menuUrl)) {
    		//발주, 요인별 발주 및 출장요청, WBS이슈관리

    		rtnHolVal = "Y";
    	}
    	
    	model.addAttribute("workDay", workDay);
    	model.addAttribute("pchsCloseDt", pchsCloseDt);
    	model.addAttribute("rtnHolVal", rtnHolVal);
    	model.addAttribute("rtnVal", rtnVal);
    	model.addAttribute("resultList", resultList);
        return "jsonView";
    }
}