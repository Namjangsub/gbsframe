package com.dksys.biz.user.bm.bm14;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.user.bm.bm14.service.BM14Svc;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/bm/bm14")
public class BM14Ctr {

    @Autowired
    MessageUtils messageUtils;

    @Autowired
    BM14Svc bm14svc;

 // BOM 전체 정전개 트리 조회
    @PostMapping("/selectBomTreeList")
    public String selectBomTreeList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	List<Map<String, String>> result = bm14svc.selectBomTreeList(paramMap);
    	model.addAttribute("result", result);
        return "jsonView";
    }
    
    // 한레벨 아래의 자식 트리 리스트 조회
    @PostMapping("/selectBomlevelList")
    public String selectBomlevelList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	List<Map<String, String>> result = bm14svc.selectBomlevelList(paramMap);
    	model.addAttribute("result", result);
    	return "jsonView";
    }
    
    // 자식 트리 전체 리스트 조회
    @PostMapping("/selectBomAllLevelList")
    public String selectBomAllLevelList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	List<Map<String, String>> result = bm14svc.selectBomAllLevelList(paramMap);
    	model.addAttribute("result", result);
    	return "jsonView";
    }
    
    // BOM 조회
    @PostMapping("/selectBomTreInfo")
    public String selectBomTreInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	Map<String, String> deptInfo = bm14svc.selectBomTreInfo(paramMap);
    	model.addAttribute("deptInfo", deptInfo);
    	return "jsonView";
    }
    
    // BOM 중복확인
    @PostMapping("/checkBomId")
    public String checkBomId(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	int deptCount = bm14svc.checkBomId(paramMap);
    	model.addAttribute("deptCount", deptCount);
        return "jsonView";
    }
	
	@PostMapping(value = "/insertBomTree")
	public String insertBomTree(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
		try {
			// Map<String, String> delmatrbom = bm14svc.deleteMatrbomChk(paramMap);
			// String delyn = delmatrbom.get("delyn");
			// if("N".equals(delyn)) {
        	// 	model.addAttribute("resultCode", 500);
			// 	model.addAttribute("resultMessage", " 구매BOM정보가 변경되어 삭제할 수 없습니다.");
        	// 	// model.addAttribute("resultMessage", delmatrbom.get("matrCd") + " : 발주정보가 존재하여 삭제할 수 없습니다.");
        	// } else {
        	//  	if (bm14svc.deleteBom(paramMap) != 0 ) {
			// 		model.addAttribute("resultCode", 200);
			// 		model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
			// 	} else {
			// 		model.addAttribute("resultCode", 500);
			// 		model.addAttribute("resultMessage", messageUtils.getMessage("fail"));    			
			// 	}
        	// }

			if (bm14svc.insertBomTree(paramMap, mRequest) != 0 ) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			};
		}catch(Exception e){
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", "자식노드에 구매BOM이 등록되면 수정이나 삭제 할 수 없습니다.");
			model.addAttribute("resultErrorMessage", e.getMessage());
		}

		// try {
		// 	if (bm14svc.insertBomTree(paramMap, mRequest) != 0 ) {
		// 		model.addAttribute("resultCode", 200);
		// 		model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
		// 	} else {
		// 		model.addAttribute("resultCode", 500);
		// 		model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
		// 	};
		// }catch(Exception e){
		// 	model.addAttribute("resultCode", 900);
		// 	model.addAttribute("resultMessage", e.getMessage());
		// }
		return "jsonView";
	}
    
    // BOM이동
    @PostMapping("/moveBom")
    public String moveBom(@RequestBody List<Map<String, String>> paramList, ModelMap model) {
    	try {
    		if (bm14svc.moveBom(paramList) != 0 ) {
	    		model.addAttribute("resultCode", 200);
	    		model.addAttribute("resultMessage", messageUtils.getMessage("move"));
    		} else {
    			  model.addAttribute("resultCode", 500);
      			  model.addAttribute("resultMessage", messageUtils.getMessage("fail"));    			
    		}
    	}catch(Exception e) {
    		  model.addAttribute("resultCode", 900);
      		  model.addAttribute("resultMessage", e.getMessage());
    	}
        return "jsonView";
    }

    // BOM 삭제
    @PostMapping("/deleteBom")
    public String deleteBom(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	try {
			Map<String, String> delmatrbom = bm14svc.deleteMatrbomChk(paramMap);
			String delyn = delmatrbom.get("delyn");
			if("N".equals(delyn)) {
        		model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", " 구매BOM정보가 변경되어 삭제할 수 없습니다.");
        		// model.addAttribute("resultMessage", delmatrbom.get("matrCd") + " : 발주정보가 존재하여 삭제할 수 없습니다.");
        	} else {
        	 	if (bm14svc.deleteBom(paramMap) != 0 ) {
					model.addAttribute("resultCode", 200);
					model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
				} else {
					model.addAttribute("resultCode", 500);
					model.addAttribute("resultMessage", messageUtils.getMessage("fail"));    			
				}
        	}
    	}catch(Exception e){
    		model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
    	}
    	return "jsonView";
		
		// try {
    	// 	if (bm14svc.deleteBom(paramMap) != 0 ) {
    	// 		model.addAttribute("resultCode", 200);
    	// 		model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
    	// 	} else {
    	// 		model.addAttribute("resultCode", 500);
    	// 		model.addAttribute("resultMessage", messageUtils.getMessage("fail"));    			
    	// 	}
    	// }catch(Exception e) {
    	// 	model.addAttribute("resultCode", 900);
    	// 	model.addAttribute("resultMessage", e.getMessage());
    	// }
    	// return "jsonView";
    }

    // BOM Node 복사
    @PostMapping("/copyBomTree")
    public String copyBomTree(@RequestBody List<Map<String, String>> paramList, ModelMap model) {
    	try {
    		if (bm14svc.copyBomTree(paramList) != 0 ) {
    			model.addAttribute("resultCode", 200);
    			model.addAttribute("resultMessage", messageUtils.getMessage("copy"));
    		} else {
    			model.addAttribute("resultCode", 500);
    			model.addAttribute("resultMessage", messageUtils.getMessage("fail"));    			
    		}
    	}catch(Exception e) {
    		model.addAttribute("resultCode", 900);
    		model.addAttribute("resultMessage", e.getMessage());
    	}
    	return "jsonView";
    }
    
    // BOM Sales Code 단위 복사
    @PostMapping("/copyBomRootSalesCdTree")
    public String copyBomRootSalesCdTree(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	try {
    		if (bm14svc.copyBomRootSalesCdTree(paramMap) != 0 ) {
    			model.addAttribute("resultCode", 200);
    			model.addAttribute("resultMessage", messageUtils.getMessage("copy"));
    		} else {
    			model.addAttribute("resultCode", 500);
    			model.addAttribute("resultMessage", messageUtils.getMessage("fail"));    			
    		}
    	}catch(Exception e) {
    		model.addAttribute("resultCode", 900);
    		model.addAttribute("resultMessage", e.getMessage());
    	}
    	return "jsonView";
    }
    
	@PostMapping("/checkBomInfo")
    public String checkBomInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
		try {
		    int resultCount = bm14svc.checkBomInfo(paramMap);
		    model.addAttribute("resultCount", resultCount);
		    model.addAttribute("resultCode", 200);
		    model.addAttribute("resultMessage", messageUtils.getMessage("check"));
		} catch (Exception e) {
			model.addAttribute("resultCount", 0);
		    model.addAttribute("resultCode", 900);
		    model.addAttribute("resultMessage", e.getMessage());
		}
        return "jsonView";
    }
	
    // 엑셀업로드 점검용 자식 트리 전체 리스트 조회
    @PostMapping("/selectBomAllLevelTempList")
    public String selectBomAllLevelTempList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	List<Map<String, String>> result = bm14svc.selectBomAllLevelTempList(paramMap);
    	model.addAttribute("result", result);
    	return "jsonView";
    }
	
    // 엑셀업로드 점검용 temp테이블 입력
    @PostMapping("/insertTempBom")
    public String insertTempBom(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) {
    	try {
    		String rtnStr = bm14svc.insertTempBom(paramMap);
    		if (!rtnStr.isEmpty()) {
    			model.addAttribute("rtnStr", rtnStr);
    			model.addAttribute("resultCode", 200);
    			model.addAttribute("resultMessage", "업로드 되었습니다.");
    		} else {
    			model.addAttribute("resultCode", 500);
    			model.addAttribute("resultMessage", messageUtils.getMessage("fail"));    			
    		}
    	}catch(Exception e) {
    		model.addAttribute("resultCode", 900);
    		model.addAttribute("resultMessage", e.getMessage());
    	}
    	return "jsonView";
    }
    
    // 업로드 내용 적용 프로시져 호출
    @PostMapping("/callDraftTempBom")
    public String callDraftTempBom(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	Map<String, String> result = bm14svc.callDraftTempBom(paramMap);
    	model.addAttribute("result", result);
    	return "jsonView";
    }
	
    // 엑셀업로드 점검용 자식 트리 전체 리스트 조회
    @PostMapping("/selectBomAllEnterList")
    public String selectBomAllEnterList(@RequestBody Map<String, String> paramMap, ModelMap model) {
        int totalCnt = bm14svc.selectBomAllEnterListCount(paramMap);
        PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
        model.addAttribute("paginationInfo", paginationInfo);

    	List<Map<String, String>> result = bm14svc.selectBomAllEnterList(paramMap);
    	model.addAttribute("result", result);
    	return "jsonView";
    }

    // BOM 작업완료, 작업완료취소 설정하기
    @PostMapping("/confirmBom")
    public String confirmBom(@RequestBody Map<String, String> paramMap, ModelMap model) {
		 try {
    	 	if (bm14svc.confirmBom(paramMap) != 0 ) {
    	 		model.addAttribute("resultCode", 200);
    	 		model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
    	 	} else {
    	 		model.addAttribute("resultCode", 500);
    	 		model.addAttribute("resultMessage", messageUtils.getMessage("fail"));    			
    	 	}
    	 }catch(Exception e) {
    	 	model.addAttribute("resultCode", 900);
    	 	model.addAttribute("resultMessage", e.getMessage());
    	 }
    	 return "jsonView";
    }

    // BOM 추천Part지정, 취소 설정하기
    @PostMapping("/recommendBom")
    public String recommendBom(@RequestBody Map<String, String> paramMap, ModelMap model) {
		 try {
    	 	if (bm14svc.recommendBom(paramMap) != 0 ) {
    	 		model.addAttribute("resultCode", 200);
    	 		model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
    	 	} else {
    	 		model.addAttribute("resultCode", 500);
    	 		model.addAttribute("resultMessage", messageUtils.getMessage("fail"));    			
    	 	}
    	 }catch(Exception e) {
    	 	model.addAttribute("resultCode", 900);
    	 	model.addAttribute("resultMessage", e.getMessage());
    	 }
    	 return "jsonView";
    }
    
}