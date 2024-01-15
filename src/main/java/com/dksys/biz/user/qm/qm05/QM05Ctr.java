package com.dksys.biz.user.qm.qm05;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.user.qm.qm05.service.QM05Svc;
import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.util.MessageUtils;
import com.dksys.biz.util.ObjectUtil;

@Controller
@RequestMapping("/user/qm/qm05")
public class QM05Ctr {

	 	@Autowired
	    MessageUtils messageUtils;

	    @Autowired
	    QM05Svc qm05svc;

		//리스트 조회
		@PostMapping(value = "/selectMainGridList")
		public String selectMainGridList(@RequestBody Map<String, String> paramMap, ModelMap model) {
			paramMap.put("deptIdCd", ObjectUtil.sqlInCodeGen(paramMap.get("deptIdCd")));
			int totalCnt = qm05svc.selectMainGridListCount(paramMap);
			PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
			model.addAttribute("paginationInfo", paginationInfo);
			List<Map<String, String>> result = qm05svc.selectMainGridList(paramMap);
			model.addAttribute("result", result);

//		   	List<Map<String, String>> resultDeptId = qm05svc.select_dept_code(paramMap);
//		   	model.addAttribute("resultDeptId", resultDeptId);
			return "jsonView";
		}
}
