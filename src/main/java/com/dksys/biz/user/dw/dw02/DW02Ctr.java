package com.dksys.biz.user.dw.dw02;


import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.user.dw.dw02.service.DW02Svc;
import com.dksys.biz.util.MessageUtils;

@Controller
@Transactional(rollbackFor = Exception.class)
@RequestMapping("/user/dw/dw02/")
public class DW02Ctr {

	private Logger logger = LoggerFactory.getLogger(DW02Ctr.class);

	@Autowired
	MessageUtils messageUtils;

	@Autowired
	DW02Svc dw02Svc;
	
	@PostMapping(value = "/drawingAuditsList")
	public String drawingAuditsList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = dw02Svc.drawingAuditsListCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);

		List<Map<String, String>> resultList = dw02Svc.drawingAuditsList(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
	}
	


	// 제품그룹 트리 리스트 조회
    @PostMapping("/selectDrawDocTreeList")
	public String selectDrawDocTreeList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> docTreeList = dw02Svc.selectDrawDocTreeList(paramMap);
		model.addAttribute("docTreeList", docTreeList);
		return "jsonView";
	}

	//카테고리별 파일정보 리스트 조회
	@PostMapping("/selectDrawTreeFileList")
	public String selectDrawTreeFileList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = dw02Svc.selectDrawTreeFileListCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		
		List<Map<String, String>> fileList = dw02Svc.selectDrawTreeFileList(paramMap);
		model.addAttribute("fileList", fileList);
		return "jsonView";
    }
	


    // 도면번호 등록 정보 조회
    @PostMapping("/selectDrawDocItemInfo")
    public String selectDrawDocItemInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	Map<String, Object> drawInfo = dw02Svc.selectDrawDocItemInfo(paramMap);
    	model.addAttribute("drawInfo", drawInfo);
        return "jsonView";
    }
    

    @PutMapping(value = "/insertDrawItem")
    public String insertDrawItem(@RequestBody Map<String, String> paramMap, ModelMap model) {
  		try {
  			if (dw02Svc.insertDrawItem(paramMap) != 0 ) {
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

    @PutMapping(value = "/updateDrawItem")
    public String updateDrawItem(@RequestBody Map<String, String> paramMap, ModelMap model)  {
  	  	try {
  			if (dw02Svc.updateDrawItem(paramMap) != 0 ) {
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
    

    // DELETE
    @PutMapping(value = "/deleteDrawDocItem")
    public String deleteDrawDocItem(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
        try {
            if (dw02Svc.deleteDrawDocItem(paramMap) != 0) {
                model.addAttribute("resultCode", 200);
                model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
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