package com.dksys.biz.user.cr.cr50;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.user.cr.cr50.service.CR50Svc;
import com.dksys.biz.util.MessageUtils;
import java.util.HashMap;

@Controller
@RequestMapping("/user/cr/cr50")
public class CR50Ctr {
    
    private Logger logger = LoggerFactory.getLogger(CR50Ctr.class);

    @Autowired
    MessageUtils messageUtils;

    @Autowired
    CR50Svc cr50Svc;
    
    @Autowired
    CM08Svc cm08Svc;
    

    
    @PostMapping(value = "/selectPfuList") 
    public String selectPfuList(@RequestBody Map<String, String> paramMap, ModelMap model) {
        int totalCnt = cr50Svc.selectPfuListCount(paramMap);
        PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
        model.addAttribute("paginationInfo", paginationInfo);

        List<Map<String, String>> resultList = cr50Svc.selectPfuList(paramMap);
        model.addAttribute("resultList", resultList);
        return "jsonView";
    }
    
    @PostMapping(value = "/selectPFUAreaItemList") 
    public String selectPFUAreaItemList(@RequestBody Map<String, String> paramMap, ModelMap model) {
        List<Map<String, String>> result = cr50Svc.selectPFUAreaItemList(paramMap);
        model.addAttribute("result", result);

        return "jsonView";
    }
    
    @PostMapping(value = "/selectPFUAreaRetriveList") 
    public String selectPFUAreaRetriveList(@RequestBody Map<String, String> paramMap, ModelMap model) {
        List<Map<String, String>> result = cr50Svc.selectPFUAreaRetriveList(paramMap);
        model.addAttribute("result", result);

        return "jsonView";
    }
    
    @PostMapping(value = "/selectPFUChangedList") 
    public String selectPFUChangedList(@RequestBody Map<String, String> paramMap, ModelMap model) {
        List<Map<String, String>> result = cr50Svc.selectPFUChangedList(paramMap);
        model.addAttribute("result", result);

        return "jsonView";
    }


    @PostMapping(value = "/selectPfuIsThereListCount") 
    public String selectPfuIsThereListCount(@RequestBody Map<String, String> paramMap, ModelMap model) {
        int totalCnt = cr50Svc.selectPfuIsThereListCount(paramMap);
        model.addAttribute("resultList", totalCnt);
        return "jsonView";
    }
    
    
    @PostMapping(value = "/selectStdPfuClobInfo") 
    public String selectStdPfuClobInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
        List<Map<String, String>> resultImg = cr50Svc.selectStdPfuClobInfo(paramMap);
        model.addAttribute("resultImg", resultImg);

        return "jsonView";
    }
    
    // salesCd 정보 조회
    @PostMapping(value = "/selectSalesCdInfo")
    public String selectSalesCdInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
        Map<String, String> result = cr50Svc.selectSalesCdInfo(paramMap);
        model.addAttribute("result", result);

//      String pfuCode = result.get("pfuCode");
//      paramMap.put("prdtDiv", pfuCode);
//      List<Map<String, String>> resultImg = cr50Svc.selectStdPfuClobInfo(paramMap);
//      model.addAttribute("resultImg", resultImg);
      return "jsonView";
    }


    // PFU 정보 조회
    @PostMapping(value = "/selectPfuInfo")
    public String selectPfuInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
      Map<String, String> result = cr50Svc.selectPfuInfo(paramMap);
      model.addAttribute("result", result);
      return "jsonView";
    }

    @PostMapping(value = "/selectPfuInfoSalesCdList")
    public String selectPfuInfoSalesCdList(@RequestBody Map<String, String> paramMap, ModelMap model) {
        List<Map<String, String>> result = cr50Svc.selectPfuInfoSalesCdList(paramMap);
        model.addAttribute("result", result);

        return "jsonView";
    }

    // PFU 정보 조회
//    @PostMapping(value = "/selectPfuClobInfo")
//    public String selectPfuClobInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
//      Map<String, String> resultClob = cr50Svc.selectPfuClobInfo(paramMap);
//      model.addAttribute("resultClob", resultClob);
//      return "jsonView";
//    }



    @PostMapping(value = "/insertPfu")
    public String insertPfu(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
        try {
            if (cr50Svc.insertPfu(paramMap, mRequest) != 0) {
                model.addAttribute("resultCode", 200);
                model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
				model.addAttribute("fileTrgtKey", paramMap.get("fileTrgtKey"));

                // PDF 생성 및 서버 저장 트리거
                try {
                    Map<String, String> reportParam = new HashMap<>(paramMap);
                    reportParam.put("file", "CR5001M01.jrf");
                    reportParam.put("fileTrgtTyp", "CR5001P01");
                    reportParam.put("comonCd", "FITR0202"); // 영업-PFU최종 폴더
                    reportParam.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
                    reportParam.put("exportFileName", "PFU_" + paramMap.get("ordrsNo") + "_" + paramMap.get("fileTrgtKey") + ".pdf");
                    
                    String arg = "coCd#" + paramMap.get("coCd") 
                               + "#partNo#" + "B" 
                               + "#pfuNo#" + paramMap.get("fileTrgtKey") 
                               + "#fromSalesCd#" + paramMap.get("fromSalesCd") 
                               + "#ordrsNo#" + paramMap.get("ordrsNo") 
                               + "#userId#" + paramMap.get("userId") 
                               + "#";
                    reportParam.put("arg", arg);

                    int newFileKey = cm08Svc.saveUbiReport(reportParam);
                    
                    // 이전 보고서 파일 삭제 (Cleanup)
                    Map<String, String> deleteParam = new HashMap<>();
                    deleteParam.put("coCd", paramMap.get("coCd"));
                    deleteParam.put("fileTrgtTyp", "CR5001P01");
                    deleteParam.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
                    deleteParam.put("userId", paramMap.get("userId"));
                    List<Map<String, String>> fileList = cm08Svc.selectFileList(deleteParam);
                    for (Map<String, String> file : fileList) {
                        String fKey = String.valueOf(file.get("fileKey"));
                        String fName = file.get("fileName");
                        if (fName.equals("PFU_" + paramMap.get("ordrsNo") + "_" + paramMap.get("fileTrgtKey") + ".pdf") && !fKey.equals(String.valueOf(newFileKey))) {
                            cm08Svc.deleteFile(fKey);
                        }
                    }
                    
                    model.addAttribute("reportResultCode", 200);
                    model.addAttribute("reportResultMessage", "리포트가 서버에 성공적으로 저장되었습니다.");
                } catch (Exception re) {
                    logger.error("PFU 데이터는 저장되었으나 리포트 생성 중 오류 발생", re);
                    model.addAttribute("reportResultCode", 500);
                    model.addAttribute("reportResultMessage", "리포트 생성 오류: " + re.getMessage());
                }
            } else {
                model.addAttribute("resultCode", 500);
                model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
            }
        } catch (Exception e) {
            model.addAttribute("resultCode", 900);
            if (e.getCause() instanceof SQLException) {
                model.addAttribute("resultDBError", ((SQLException) e.getCause()).getSQLState());
            }
            model.addAttribute("resultMessage", e.getMessage());
        }
        return "jsonView";
    }

    @PostMapping(value = "/updatePfu")
    public String updatePfu(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
        try {
            if (cr50Svc.updatePfu(paramMap, mRequest) != 0) {
                model.addAttribute("resultCode", 200);
                model.addAttribute("resultMessage", messageUtils.getMessage("update"));

                // PDF 생성 및 서버 저장 트리거
                try {
                    Map<String, String> reportParam = new HashMap<>(paramMap);
                    reportParam.put("file", "CR5001M01.jrf");
                    reportParam.put("fileTrgtTyp", "CR5001P01");
                    reportParam.put("comonCd", "FITR0202"); // 영업-PFU최종 폴더
                    reportParam.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
                    reportParam.put("exportFileName", "PFU_" + paramMap.get("ordrsNo") + "_" + paramMap.get("fileTrgtKey") + ".pdf");
                    
                    String arg = "coCd#" + paramMap.get("coCd") 
                               + "#partNo#" + "B" 
                               + "#pfuNo#" + paramMap.get("fileTrgtKey") 
                               + "#fromSalesCd#" + paramMap.get("fromSalesCd") 
                               + "#ordrsNo#" + paramMap.get("ordrsNo") 
                               + "#userId#" + paramMap.get("userId") 
                               + "#";
                    reportParam.put("arg", arg);

                    int newFileKey = cm08Svc.saveUbiReport(reportParam);
                    
                    // 이전 보고서 파일 삭제 (Cleanup)
                    Map<String, String> deleteParam = new HashMap<>();
                    deleteParam.put("coCd", paramMap.get("coCd") );
                    deleteParam.put("fileTrgtTyp",  "CR5001P01");
                    deleteParam.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
                    deleteParam.put("userId", paramMap.get("userId"));
                    List<Map<String, String>> fileList = cm08Svc.selectFileList(deleteParam);
                    for (Map<String, String> file : fileList) {
                        String fKey = String.valueOf(file.get("fileKey"));
                        String fName = file.get("fileName");
                        if (fName.equals("PFU_" + paramMap.get("ordrsNo") + "_" + paramMap.get("fileTrgtKey") + ".pdf") && !fKey.equals(String.valueOf(newFileKey))) {
                            cm08Svc.deleteFile(fKey);
                        }
                    }
                    
                    model.addAttribute("reportResultCode", 200);
                    model.addAttribute("reportResultMessage", "리포트가 서버에 성공적으로 저장되었습니다.");
                } catch (Exception re) {
                    logger.error("PFU 데이터는 수정되었으나 리포트 생성 중 오류 발생", re);
                    model.addAttribute("reportResultCode", 500);
                    model.addAttribute("reportResultMessage", "리포트 생성 오류: " + re.getMessage());
                }
            } else {
                model.addAttribute("resultCode", 500);
                model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
            }
        } catch (Exception e) {
            model.addAttribute("resultCode", 900);
            model.addAttribute("resultMessage", e.getMessage());
        }
        return "jsonView";
    }

    @PutMapping(value = "/deletePfuNo")
    public String deletePfuNo(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
        try {
            if (cr50Svc.deletePfuNo(paramMap) != 0) {
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



    @PostMapping(value = "/copy_cr50")
    public String copy_cr50(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
        try {
            if (cr50Svc.copy_cr50(paramMap, mRequest) != 0) {
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

    
    @PostMapping(value = "/selectPfuCopyTargetList") 
    public String selectPfuCopyTargetList(@RequestBody Map<String, String> paramMap, ModelMap model) {
        int totalCnt = cr50Svc.selectPfuCopyTargetListCount(paramMap);
        PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
        model.addAttribute("paginationInfo", paginationInfo);

        List<Map<String, String>> resultList = cr50Svc.selectPfuCopyTargetList(paramMap);
        model.addAttribute("resultList", resultList);
        return "jsonView";
    }

    @PostMapping(value = "/selectTagetSalesCodeList")
    public String selectTagetSalesCodeList(@RequestBody Map<String, String> paramMap, ModelMap model) {
        List<Map<String, String>> result = cr50Svc.selectTagetSalesCodeList(paramMap);
        model.addAttribute("result", result);
        return "jsonView";
    }

    @PostMapping(value = "/selectPfuReferenceTargetList")
    public String selectPfuReferenceTargetList(@RequestBody Map<String, String> paramMap, ModelMap model) {
        int totalCnt = cr50Svc.selectPfuReferenceTargetListCount(paramMap);
        PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
        model.addAttribute("paginationInfo", paginationInfo);
        List<Map<String, String>> resultList = cr50Svc.selectPfuReferenceTargetList(paramMap);
        model.addAttribute("resultList", resultList);
        return "jsonView";
    }

    @PostMapping(value = "/selectIssueReferenceList")
    public String selectIssueReferenceList(@RequestBody Map<String, String> paramMap, ModelMap model) {
        List<Map<String, String>> result = cr50Svc.selectIssueReferenceList(paramMap);
        model.addAttribute("result", result);
        return "jsonView";
    }

    @PostMapping(value = "/selectImprovementReferenceList")
    public String selectImprovementReferenceList(@RequestBody Map<String, String> paramMap, ModelMap model) {
        List<Map<String, String>> result = cr50Svc.selectImprovementReferenceList(paramMap);
        model.addAttribute("result", result);
        return "jsonView";
    }

    @PutMapping(value = "/updatePfuVersionUpProcess")
    public String updatePfuVersionUpProcess(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
        try {
            paramMap.put("fileTrgtKey", paramMap.get("pfuNo"));
            if (cr50Svc.updatePfuVersionUpProcess(paramMap) != 0) {
                Map<String, String> result = cr50Svc.selectPfuInfo(paramMap);
                model.addAttribute("result", result);
                
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