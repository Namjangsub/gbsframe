package com.dksys.biz.user.wb.wb03;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.exc.LogicException;
import com.dksys.biz.util.MessageUtils;
import com.dksys.biz.user.wb.wb03.service.WB03Svc;

@Controller
@RequestMapping("/user/wb/wb03")
public class WB03Ctr {
	
	private Logger logger = LoggerFactory.getLogger(WB03Ctr.class);
	
	@Autowired
	MessageUtils messageUtils;
    
    @Autowired
    WB03Svc wb03Svc;
	

	 
    
}