package com.dksys.biz.user.wb.wb22;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.user.wb.wb22.service.WB22Svc;
import com.dksys.biz.util.MessageUtils;

@Controller
// @Transactional(rollbackFor = Exception.class)
@RequestMapping("/user/wb/wb22")
public class WB22Ctr {

	private Logger logger = LoggerFactory.getLogger(WB22Ctr.class);

	@Autowired
	MessageUtils messageUtils;

	@Autowired
	WB22Svc wb22Svc;

	@PostMapping(value = "/selectWbsSjList")
	public String selectWbsSjList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = wb22Svc.selectWbsSjListCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);

		List<Map<String, String>> fileList = wb22Svc.selectWbsSjList(paramMap);
		model.addAttribute("fileList", fileList);
		return "jsonView";
	}

	@PostMapping(value = "/selectSjInfo")
	public String selectSjInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
		Map<String, String> resultList = wb22Svc.selectSjInfo(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
	}

	// 이력조회
	@PostMapping(value = "/selectHistWBS1Level")
	public String selectHistWBS1Level(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> fileList = wb22Svc.selectHistWBS1Level(paramMap);
		model.addAttribute("fileList", fileList);
		return "jsonView";
	}

	@PostMapping(value = "/selectWBS1Level")
	public String selectWBS1Level(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> fileList = wb22Svc.selectWBS1Level(paramMap);
		model.addAttribute("fileList", fileList);
		return "jsonView";
	}

	@PostMapping(value = "/wbsLevel1Insert")
	public String wbsLevel1Insert(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
		try {
			if (wb22Svc.wbsLevel1Insert(paramMap, mRequest) != 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("save"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			}
			;
		} catch (Exception e) {
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}

	@PostMapping(value = "/wbsLevel1Update")
	public String wbsLevel1Update(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
		try {
			if (wb22Svc.wbsLevel1Update(paramMap, mRequest) != 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("save"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			}
			;
		} catch (Exception e) {
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}

	@PostMapping(value = "/selectWBS2Level")
	public String selectWBS2Level(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> fileList = wb22Svc.selectWBS2Level(paramMap);
		model.addAttribute("fileList", fileList);
		return "jsonView";
	}

	@PostMapping(value = "/wbsLevel2Insert")
	public String wbsLevel2Insert(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
		try {
			if (wb22Svc.wbsLevel2Insert(paramMap, mRequest) != 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("save"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			}
			;
		} catch (Exception e) {
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}

	@PostMapping(value = "/selectVerNoNext")
	public String selectSjVerNoNext(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> result = wb22Svc.selectVerNoNext(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	@PostMapping(value = "/wbsVerUpInsert")
	public String sjVerUpInsert(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
		try {
			if (wb22Svc.wbsVerUpInsert(paramMap, mRequest) != 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("save"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			}
			;
		} catch (Exception e) {
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}

	@PostMapping(value = "/wbsLevel1confirm")
	public String wbsLevel1confirm(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model)
			throws Exception {
		try {
			if (wb22Svc.wbsLevel1confirm(paramMap, mRequest) != 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("save"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			}
			;
		} catch (Exception e) {
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}

	@PostMapping(value = "/wbsLevel2confirm")
	public String wbsLevel2confirm(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model)
			throws Exception {
		try {
			if (wb22Svc.wbsLevel2confirm(paramMap, mRequest) != 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("save"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			}
			;
		} catch (Exception e) {
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}

	@PostMapping(value = "/selectRsltsSharngList")
	public String selectRsltsSharngList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> resultList = wb22Svc.selectRsltsSharngList(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
	}

	@PostMapping(value = "/selectRsltsApprovalList")
	public String selectRsltsApprovalList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> resultList = wb22Svc.selectRsltsApprovalList(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
	}

	@PostMapping(value = "/wbsRsltsInsert")
	public String wbsRsltsInsert(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) {
		try {
			if (wb22Svc.wbsRsltsInsert(paramMap, mRequest) != 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("save"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			}
			;
		} catch (Exception e) {
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}

	@PostMapping(value = "/wbsRsltsUpdate")
	public String wbsRsltsUpdate(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) {
		try {
			if (wb22Svc.wbsRsltsUpdate(paramMap, mRequest) != 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("save"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			}
			;
		} catch (Exception e) {
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}

	@PostMapping(value = "/wbsRsltsconfirm")
	public String wbsRsltsconfirm(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
		try {
			if (wb22Svc.wbsRsltsconfirm(paramMap, mRequest) != 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("save"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			}
			;
		} catch (Exception e) {
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}

	@PostMapping(value = "/selectTodoRsltsView")
	public String selectTodoRsltsView(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> resultList = wb22Svc.selectTodoRsltsView(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
	}

	// TODO 미완료 현황 대쉬보드 오른쪽 하단 WBS 계획정보
	@PostMapping(value = "/selectIncompleteJob")
	public String selectIncompleteJob(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> resultList = wb22Svc.selectIncompleteJob(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
	}

	// 복사기능 호출
	@PostMapping(value = "/callCopyWbsPlan")
	public String callCopyWbsPlan(@RequestBody Map<String, String> paramMap, ModelMap model) {
		wb22Svc.callCopyWbsPlan(paramMap);
		model.addAttribute("paramMap", paramMap);
		return "jsonView";
	}

	// TASK 템플릿 조회
	@PostMapping(value = "/selectWbsTaskTempletList")
	public String selectWbsTaskTempletList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = wb22Svc.selectWbsTaskTempletCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);

		List<Map<String, String>> result = wb22Svc.selectWbsTaskTempletList(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	@PostMapping(value = "/saveWbsTaskTempletList")
	public String saveWbsTaskTempletList(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model)
			throws Exception {
		try {
			if (wb22Svc.saveWbsTaskTempletList(paramMap, mRequest) != 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("save"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			}
			;
		} catch (Exception e) {
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}

	// 유저별 템플릿 controller
	// ---------------------------------------------------------------------------------------------------------------------------------------------
	// 유저별 TASK 템플릿 조회
	@PostMapping(value = "/selectWbsUserTaskTempletList")
	public String selectWbsUserTaskTempletList(@RequestBody Map<String, String> paramMap, ModelMap model) {
//		int totalCnt = wb22Svc.selectWbsUserTaskTempletCount(paramMap);
//		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
//		model.addAttribute("paginationInfo", paginationInfo);

		List<Map<String, String>> result = wb22Svc.selectWbsUserTaskTempletList(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	@PostMapping(value = "/saveWbsUserTaskTempletList")
	public String saveWbsUserTaskTempletList(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model)
			throws Exception {
		try {
			if (wb22Svc.saveWbsUserTaskTempletList(paramMap, mRequest) != 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("save"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			}
			;
		} catch (Exception e) {
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}

//---------------------------------------------------------------------------------------------------------------------------------------------
	// 일괄복사부분
	@PostMapping(value = "/ModalwbsPlanconfirmList")
	public String ModalsjnoconfirmList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = wb22Svc.ModalwbsPlanconfirmListCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);

		List<Map<String, String>> resultList = wb22Svc.ModalwbsPlanconfirmList(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
	}

	@PostMapping(value = "/confirm_copy")
	public String confirm_copy(@RequestBody Map<String, String> paramMap, ModelMap model) {
		try {
			if (wb22Svc.confirm_copy(paramMap) != 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("save"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			}
			;
		} catch (Exception e) {
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}

	@PostMapping(value = "/selectWbcPlanTodoList")
	public String selectWbcPlanTodoList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> resultList = wb22Svc.selectWbcPlanTodoList(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
	}

	@PostMapping(value = "/selectWbcPlanUpdteTodoList")
	public String selectWbcPlanUpdteTodoList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> resultList = wb22Svc.selectWbcPlanUpdteTodoList(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
	}

	@PostMapping(value = "/wbcPlanTodoInsert")
	public String wbsIssInsert(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
		try {
			if (wb22Svc.wbcPlanTodoInsert(paramMap, mRequest) != 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			}
			;
		} catch (Exception e) {
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}

	// 일괄확정부분
	@PostMapping(value = "/Modalwb22noconfirmList")
	public String Modalwb22noconfirmList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = wb22Svc.Modalwb22noconfirmListCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);

		List<Map<String, String>> resultList = wb22Svc.Modalwb22noconfirmList(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
	}

	@PostMapping(value = "/confirm_wb22")
	public String confirm_wb22(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
		try {
			if (wb22Svc.confirm_wb22(paramMap, mRequest) != 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("update"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			}
			;
		} catch (Exception e) {
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}
	// 일괄확정부분 끝

	@PostMapping(value = "/wbsResultLastVerNoSearch")
	public String wbsResultLastVerNoSearch(@RequestBody Map<String, String> paramMap, ModelMap model) {
		Map<String, String> resultList = wb22Svc.wbsResultLastVerNoSearch(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
	}

	// wbs계획 관리 변경사항 저장
	@PostMapping(value = "updateWbsChanges")
	public String saveWbsChanges(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
		try {
			if (wb22Svc.updateWbsChanges(paramMap, mRequest) != 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("update"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			}
			;
		} catch (Exception e) {
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}

	// QR에 sortUrl 생성 (일정관리에서 QR을 생성)
	@PostMapping(value = "/generateShortUrl")
    public String generateShortUrl(@RequestBody Map<String, String> paramMap, Model model) {
    	String longUrl = paramMap.get("longUrl");
    	Map<String, String> returnUrl = wb22Svc.generateShortUrl(paramMap);
        model.addAttribute("longUrl", longUrl);
        model.addAttribute("shortUrl", returnUrl.get("shortUrl"));
        model.addAttribute("chkCode", returnUrl.get("chkCode"));
        model.addAttribute("resultCode", 200);
    	return "jsonView";
    }
	


	@PostMapping(value = "/wbsLevel1GantUpdate")
	public String wbsLevel1GantUpdate(@RequestBody Map<String, String> paramMap,ModelMap model) throws Exception {
		try {
			if (wb22Svc.wbsLevel1GantUpdate(paramMap) != 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("update"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			}
			;
		} catch (Exception e) {
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}



	@PostMapping(value = "/wbsLevel2GantInsert")
	public String wbsLevel2GantInsert(@RequestBody Map<String, String> paramMap, ModelMap model) {
		try {
			if (wb22Svc.wbsLevel2GantInsert(paramMap) != 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("save"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			}
			;
		} catch (Exception e) {
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}



	@PostMapping(value = "/wbsRsltsGantInsert")
	public String wbsRsltsGantInsert(@RequestBody Map<String, String> paramMap, ModelMap model) {
		try {
			if (wb22Svc.wbsRsltsGantInsert(paramMap) != 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("fileTrgtKey", paramMap.get("fileTrgtKey"));
				model.addAttribute("resultMessage", messageUtils.getMessage("save"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			}
			;
		} catch (Exception e) {
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}


	@PostMapping(value = "/wbsRsltsGantDelete")
	public String wbsRsltsGantDelete(@RequestBody Map<String, String> paramMap, ModelMap model) {
		try {
			if (wb22Svc.wbsRsltsGantDelete(paramMap) != 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("save"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			}
			;
		} catch (Exception e) {
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}

	@PostMapping(value = "/updateWbsPlan")
	public String updateWbsPlan(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
		try {
			if (wb22Svc.updateWbsPlan(paramMap, mRequest) != 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("update"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			}
			;
		} catch (Exception e) {
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}

	// 실적등록여부
	@PostMapping(value = "/wbsRsltsChkExist")
	public String wbsRsltsChkExist(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> result = wb22Svc.wbsRsltsChkExist(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	@PostMapping(value = "/wb22OrdrsNoVersionUp")
	public String wb22OrdrsNoVersionUp(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
		try {
			if (wb22Svc.wb22OrdrsNoVersionUp(paramMap, mRequest) != 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("save"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			}
			;
		} catch (Exception e) {
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}
}