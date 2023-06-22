package com.dksys.biz.user.sm.sm04;

import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.user.cr.cr01.service.CR01Svc;
import com.dksys.biz.user.sm.sm04.service.SM04Svc;
import com.dksys.biz.util.MessageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
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


    @PostMapping("/maxEst")
    public String showEstimationForm(@RequestBody Map<String, String> param, ModelMap model) {
        String maxEstNo = sm04svc.selectMaxEstNo(param);
        model.addAttribute("maxEstNo", maxEstNo);
        return "jsonView";
    }

    @PostMapping("/selectMaxEstDeg")
    public String selectMaxEstDeg(@RequestBody Map<String, String> param, ModelMap model) {
        String maxEstDeg = sm04svc.selectMaxEstDeg(param);
        model.addAttribute("maxEstDeg", maxEstDeg);
        return "jsonView";
    }

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
    
    @PostMapping(value = "/insert_sm04Info")
	public String insert_sm04Info(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) {
		try {
			int newBmMstr = sm04svc.insert_sm04Info(paramMap);
			model.addAttribute("resultCode", 200);
			model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
			model.addAttribute("newBmMstr", newBmMstr);
		} catch (Exception e) {
			model.addAttribute("resultCode", 500);
			model.addAttribute("resultMessage", e.getLocalizedMessage());
		}
		
    	return "jsonView";
		
	}
    @PostMapping(value = "/insertEstDeg")
    public String insertEstDeg(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) {
        try {
            Map<String, Object> newEstMap = sm04svc.insertEstDeg(paramMap, mRequest);
            System.out.println(newEstMap + "최종");
            model.addAttribute("resultCode", 200);
            model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
            model.addAttribute("param", newEstMap);
        } catch (Exception e) {
            model.addAttribute("resultCode", 500);
            model.addAttribute("resultMessage", e.getLocalizedMessage());
        }
        return "jsonView";
    }


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