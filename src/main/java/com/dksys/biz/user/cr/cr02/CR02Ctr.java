package com.dksys.biz.user.cr.cr02;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.user.cr.cr01.service.CR01Svc;
import com.dksys.biz.user.cr.cr02.service.CR02Svc;
import com.dksys.biz.util.MessageUtils;
@Controller
@RequestMapping("/user/cr/cr02")
public class CR02Ctr {

	@Autowired
	MessageUtils messageUtils;
	@Autowired
	CR01Svc cr01Svc;
	@Autowired
	CR02Svc cr02Svc;

	@PostMapping("/selectOrdrsList")
	public String selectOrdrsList(@RequestBody Map<String, String> param, ModelMap model) {
		int totalCnt = cr02Svc.selectOrdrsCount(param);
		PaginationInfo paginationInfo = new PaginationInfo(param, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, Object>> ordrsList = cr02Svc.selectOrdrsList(param);
		model.addAttribute("ordrsList", ordrsList);
		return "jsonView";
	}

	@PostMapping("/selectOrdrsListPop")
	public String selectOrdrsListPop(@RequestBody Map<String, String> param, ModelMap model) {
		int totalCnt = cr02Svc.selectOrdrsListPopCount(param);
		PaginationInfo paginationInfo = new PaginationInfo(param, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, Object>> ordrsList = cr02Svc.selectOrdrsListPop(param);
		model.addAttribute("ordrsList", ordrsList);
		return "jsonView";
	}

	@PostMapping(value = "/selectOrdrsInfo")
	public String selectOrdrsInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
		Map<String, Object> ordrsInfo = cr02Svc.selectOrdrsInfo(paramMap);
		model.addAttribute("ordrsInfo", ordrsInfo);
		return "jsonView";
	}
	@PostMapping(value = "/selectOrdrsWithEst")
	public String selectOrdrsWithEst(@RequestBody Map<String, String> params,ModelMap model) {
		model.addAttribute("info", cr02Svc.selectOrdrsWithEst(params));
		return "jsonView";
	}

	@PostMapping(value = "/selectClntInfo")
	public String selectClntInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
		Map<String, Object> estInfo = cr01Svc.selectEstInfo(paramMap);
		System.out.println(estInfo+"error");
		model.addAttribute("clntInfo", estInfo.get("estClntCd"));
		return "jsonView";
	}
	// ...

	@PostMapping(value = "/ItemDivEtc")
	public String getItemDivEtc(@RequestBody Map<String, String> param, ModelMap model) {
		String itemDivEtc = cr02Svc.selectItemDivEtc(param);
		model.addAttribute("itemDivEtc", itemDivEtc);
		return "jsonView";
	}

	@PostMapping(value = "/insertOrdrs")
	public String insertOrdrs(@RequestParam Map<String, String> param, MultipartHttpServletRequest mRequest, ModelMap model) {

		try {
			int rtnInt = cr02Svc.selectOrdrsKey(param);
			if(rtnInt == 0) {
				Map<String, String> rtnResult = cr02Svc.insertOrdrs(param,mRequest);
				model.addAttribute("ordrsNo", rtnResult.get("ordrsNo").toString());
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
		    }else {
		    	model.addAttribute("resultCode", 900);
		    	model.addAttribute("resultMessage", "이미 등록된 건양수주번호가 있습니다.");
		    }
		}catch(Exception e) {
			model.addAttribute("resultCode", 500);
			model.addAttribute("resultMessage", e.getLocalizedMessage());
		}


		return "jsonView";
	}


	@PostMapping(value = "/updateOrdrs")
	public String updateOrdrs(@RequestParam Map<String, String> param,MultipartHttpServletRequest mRequest, ModelMap model) {

		try {
			if (cr02Svc.updateOrdrs(param,mRequest) != 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("update"));
			} else {
				model.addAttribute("resultCode", 900);
				model.addAttribute("resultMessage", "fail");
			}
		}catch(Exception e) {
			model.addAttribute("resultCode", 500);
			model.addAttribute("resultMessage", e.getLocalizedMessage());
		}
		return "jsonView";
	}

	@PostMapping(value = "/updateOrdrsPmntPlanProcess")
	public String updateOrdrsPmntPlanProcess(@RequestParam Map<String, String> param,MultipartHttpServletRequest mRequest, ModelMap model) {

		try {
			cr02Svc.updateOrdrsPmntPlanProcess(param,mRequest);
			model.addAttribute("resultCode", 200);
			model.addAttribute("resultMessage", messageUtils.getMessage("update"));
		}catch(Exception e) {
			model.addAttribute("resultCode", 500);
			model.addAttribute("resultMessage", e.getLocalizedMessage());
		}
		return "jsonView";
	}

	@DeleteMapping(value = "/deleteOrdrs")
	public String deleteOrdrs(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {

		try {
			cr02Svc.deleteOrdrs(paramMap);
			model.addAttribute("resultCode", 200);
			model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
		}catch(Exception e) {
			model.addAttribute("resultCode", 500);
			model.addAttribute("resultMessage", e.getLocalizedMessage());
		}
		return "jsonView";

	}


	@PostMapping("/selectOrdrsPlanHis")
	public String selectOrdrsPlanHis(@RequestBody Map<String, String> param, ModelMap model) {
		int totalCnt = cr02Svc.selectOrdrsPlanHisCount(param);
		PaginationInfo paginationInfo = new PaginationInfo(param, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, Object>> ordrsPlanHisList = cr02Svc.selectOrdrsPlanHis(param);
		model.addAttribute("ordrsPlanHisList", ordrsPlanHisList);
		return "jsonView";
	}

	//SalesCode Search modal폼 사용 조회
	@PostMapping(value = "/selectWbsLeftSalesCodeTreeList")
	public String selectWbsLeftSalesCodeTreeList(@RequestBody Map<String, String> param, ModelMap model) {
		List<Map<String, Object>> fileList = cr02Svc.selectWbsLeftSalesCodeTreeList(param);
		model.addAttribute("fileList", fileList);
		return "jsonView";
	}

	//SalesCode Search사용 조회
	@PostMapping(value = "/selectItemSalesCodeTreeList2")
	public String selectItemSalesCodeTreeList2(@RequestBody Map<String, String> param, ModelMap model) {
		List<Map<String, Object>> fileList = cr02Svc.selectItemSalesCodeTreeList2(param);
		model.addAttribute("fileList", fileList);
		return "jsonView";
	}

	//복사기능 호출
	@PostMapping(value = "/callCopyOrdrs")
	public String callCopyOrdrs(@RequestBody Map<String, String> paramMap, ModelMap model) {
		cr02Svc.callCopyOrdrs(paramMap);
		model.addAttribute("paramMap", paramMap);
		return "jsonView";
	}

	@PostMapping("/selectNoSalesCdOrdrsListPop")
	public String selectNoSalesCdOrdrsListPop(@RequestBody Map<String, String> param, ModelMap model) {
		int totalCnt = cr02Svc.selectNoSalesCdOrdrsListPopCount(param);
		PaginationInfo paginationInfo = new PaginationInfo(param, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, Object>> ordrsList = cr02Svc.selectNoSalesCdOrdrsListPop(param);
		model.addAttribute("ordrsList", ordrsList);
		return "jsonView";
	}

	@PostMapping(value = "/selectJunmooApproval")
	public String selectJunmooApproval(@RequestBody Map<String, String> param, ModelMap model) {
		int JunmooApproval = cr02Svc.selectJunmooApproval(param);
		model.addAttribute("JunmooApproval", JunmooApproval);
		return "jsonView";
	}

	//설비원가정보 삭제
	@DeleteMapping(value = "/deleteOrdrsDetail")
	public String deleteOrdrsDetail(@RequestBody Map<String, String> param, ModelMap model) {
		try {
			cr02Svc.deleteOrdrsDetail(param);
			model.addAttribute("resultCode", 200);
			model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
		}
		catch(Exception e) {
			model.addAttribute("resultCode", 500);
			model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
		}
		return "jsonView";
	}

	//SalesCode Search modal폼 사용 조회
	@PostMapping(value = "/selectOrdrsDetails")
	public String selectOrdrsDetails(@RequestBody Map<String, String> param, ModelMap model) {
		List<Map<String, Object>> fileList = cr02Svc.selectOrdrsDetails(param);
		model.addAttribute("fileList", fileList);
		return "jsonView";
	}

	// salesCd 정보 조회
	@PostMapping(value = "/salesCdSearchOrderInfo")
	public String salesCdSearchOrderInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
		Map<String, String> result = cr02Svc.salesCdSearchOrderInfo(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	// 수주변경 타이틀
	@PostMapping(value = "/selectOrderChangeTitle")
	public String selectOrderChangeTitle(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> result = cr02Svc.selectOrderChangeTitle(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	//수금관리사항 수정 처리
	@PostMapping(value = "/clmnPlanRmkUpdate")
	public String clmnPlanRmkUpdate(@RequestParam Map<String, String> paramMap, ModelMap model) throws Exception {
		try {
			if (cr02Svc.clmnPlanRmkUpdate(paramMap) != 0 ) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("update"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			};
		}catch(Exception e){
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}

	// 수주정보 삭제 시 작업일보, 문제현황, 일정현황, 발주요청에 등록되어 있는지 체크
	@PostMapping(value = "/ordrsDeleteChk")
	public String ordrsDeleteChk(@RequestBody Map<String, String> paramMap, ModelMap model) {
		Map<String, String> result = cr02Svc.ordrsDeleteChk(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	// 설비삭제 가능여부 체크 (작업일보, 문제현황, 일정현황, 발주요청)
	@PostMapping(value = "/deleteDetailChk")
	public String deleteDetailChk(@RequestBody Map<String, String> paramMap, ModelMap model) {
		Map<String, String> result = cr02Svc.deleteDetailChk(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	// 수주구분 수주취소 가능여부 체크(발주요청서, 구매발주서, 외주관리)
	@PostMapping(value = "/ordrsDivChangeChk")
	public String ordrsDivChangeChk(@RequestBody Map<String, String> paramMap, ModelMap model) {
		Map<String, String> result = cr02Svc.ordrsDivChangeChk(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	//  체크
	@PostMapping(value = "/unsettledAmtCreditChk")
	public String unsettledAmtCreditChk(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, Object>> result = cr02Svc.unsettledAmtCreditChk(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}
	
	/****************************************************************************
	 *  특정 수주번호+순번으로 추후정산 발생금액 및 정산상계, 잔액 추출하기
	 *  입력 : coCd, ordrsNo (해당상계처리 등록 수주번호) - 동일 상계처리 수주번호는 1개만 가능
	 *         unsettledOrdrsNo+unsettledOrdrsSeq (발생 수주번호+순번)
	 *  사용 : 수주등록프로그램 프론트엔드에서 서버에 전달하기전에 점검하기 위함.
	 ****************************************************************************/
	@PostMapping(value = "/settledAmtCreditTotalAmt")
	public String settledAmtCreditTotalAmt(@RequestBody Map<String, String> paramMap, ModelMap model) {
		Map<String, Object> result = cr02Svc.settledAmtCreditTotalAmt(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}
	
	
	// 추후정산 및 정산상계처리 현황 LIST
	@PostMapping(value = "/selectUnsettledAmtSalesCodeList")
	public String selectUnsettledAmtSalesCodeList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, Object>> result = cr02Svc.selectUnsettledAmtSalesCodeList(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

}