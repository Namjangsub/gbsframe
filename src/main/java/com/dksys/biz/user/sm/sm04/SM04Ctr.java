package com.dksys.biz.user.sm.sm04;

import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.user.sm.sm04.service.SM04Svc;
import com.dksys.biz.util.MessageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user/sm/sm04")
public class SM04Ctr {

    @Autowired
    MessageUtils messageUtils;

    @Autowired
    SM04Svc sm04svc;


    @PostMapping("/selectIoList")
    public String selectIoList(@RequestBody Map<String, String> param, ModelMap model) {
        int totalCnt = sm04svc.selectIoCount(param);
        System.out.println(totalCnt + "총로우");
        PaginationInfo paginationInfo = new PaginationInfo(param, totalCnt);
        model.addAttribute("paginationInfo", paginationInfo);

        List<Map<String, Object>> ioList = sm04svc.selectIoList(param);
        model.addAttribute("ioList", ioList );
        
        return "jsonView";
    }
    

    @PostMapping("/selectIoDetail")
    public String selectIoDetail(@RequestBody Map<String, String> param, ModelMap model) {
       // int totalCnt = sm04svc.selectIoDetailCount(param);
       // System.out.println(totalCnt + "총로우");
       // PaginationInfo paginationInfo = new PaginationInfo(param, totalCnt);
       // model.addAttribute("paginationInfo", paginationInfo);

        List<Map<String, Object>> ioDetailList = sm04svc.selectIoDetail(param);
        model.addAttribute("ioDetailList", ioDetailList );
        
        return "jsonView";
    }

    @PostMapping("/selectStInfo")
    public String selectStInfo(@RequestBody Map<String, String> param, ModelMap model) {
        //int totalCnt = sm04svc.selectIoCount(param);
        //PaginationInfo paginationInfo = new PaginationInfo(param, totalCnt);
        //model.addAttribute("paginationInfo", paginationInfo);
        List<Map<String, Object>> stList = sm04svc.selectStInfo(param);
        model.addAttribute("stList", stList);
        return "jsonView";
    }

    @PostMapping("/selectWhCd")
    public String selectWhCd(@RequestBody Map<String, String> param, ModelMap model) {
        List<Map<String, Object>> whList = sm04svc.selectWhCd(param);
        model.addAttribute("whList", whList);
        return "jsonView";
    }
    

	//정보 조회
	@PostMapping(value = "/select_sm04_info")
	public String select_sm04_info(@RequestBody Map<String, String> paramMap, ModelMap model) {
		Map<String, String> result = sm04svc.select_sm04_info(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

    

	//기본정보 & 불출정보 등록
    @PostMapping(value = "/insert_sm04")
    public String insert_sm04(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
		try {
			if (sm04svc.insert_sm04(paramMap, mRequest) != 0 ) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
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
    
    

	//출고창고 재고정보 등록
    @PostMapping(value = "/insert_sm04_Info")
    public String insert_sm04_Info(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model)  {
		try {
			if (sm04svc.insert_sm04_Info(paramMap, mRequest) != 0 ) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
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
    
	/*
	 * @PostMapping(value = "/insert_sm04_Info") public String
	 * insert_sm04_Info(@RequestParam Map<String, String> paramMap,
	 * MultipartHttpServletRequest mRequest, ModelMap model) { try { int newBmMstr =
	 * sm04svc.insert_sm04_Info(paramMap); model.addAttribute("resultCode", 200);
	 * model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
	 * model.addAttribute("newBmMstr", newBmMstr); } catch (Exception e) {
	 * model.addAttribute("resultCode", 500); model.addAttribute("resultMessage",
	 * e.getLocalizedMessage()); }
	 * 
	 * return "jsonView";
	 * 
	 * }
	 */
    
    

	/*
	 * @PutMapping(value = "/updateEst") public String updateEst(@RequestParam
	 * Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap
	 * model) { try { Map<String, Object> updateEstMap = sm04svc.updateEst(paramMap,
	 * mRequest); model.addAttribute("resultCode", updateEstMap.get("resultCode"));
	 * model.addAttribute("resultMessage", messageUtils.getMessage("update"));
	 * model.addAttribute("param", updateEstMap); } catch (Exception e) {
	 * System.out.println(e.getMessage()); model.addAttribute("resultCode", 500);
	 * model.addAttribute("resultMessage", messageUtils.getMessage("fail")); }
	 * return "jsonView"; }
	 * 
	 */

}