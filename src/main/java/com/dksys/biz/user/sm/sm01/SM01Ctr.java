package com.dksys.biz.user.sm.sm01;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.NotOLE2FileException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.cmn.service.CmnService;
import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.user.sm.sm01.service.SM01Svc;
import com.dksys.biz.util.DateUtil;
import com.dksys.biz.util.MessageUtils;
import com.dksys.biz.util.ObjectUtil;

@Controller
@RequestMapping("/user/sm/sm01")
public class SM01Ctr {

  @Autowired
  MessageUtils messageUtils;

  @Autowired
  SM01Svc sm01Svc;

  @Autowired
  CmnService cmnService;

  @Value("${file.uploadDir}")
  private String uploadDir;

  private HSSFWorkbook workbook;

	// 엑셀업로드
	@PostMapping("/uploadExcelFile")
	public String uploadExcelFile(MultipartHttpServletRequest request, ModelMap model) {
		String dateTime = DateUtil.getCurrentDateTime();
		String path = uploadDir + File.separator + "tmp_excel" + File.separator;
		boolean isSaveExists = false;
		MultipartFile file = null;
		File saveFile = null;
		Iterator<String> iterator = request.getFileNames();
		String resultMessage = "";
		
		if (iterator.hasNext()) {
			file = request.getFile(iterator.next());
			try {
				String saveFileName = dateTime;
				File f = new File(path);
				if (!f.isDirectory()) {
					f.mkdirs();
				}
				saveFile = new File(path + saveFileName);
				saveFile.createNewFile();
				file.transferTo(saveFile);
				isSaveExists = saveFile.exists();
			} catch (IllegalStateException e) {
				if (isSaveExists) {
					saveFile.delete();
				}
				resultMessage = "파일 저장 실패 하였습니다. IllegalStateException";
				e.printStackTrace();
			} catch (IOException e) {
				if (isSaveExists) {
					saveFile.delete();
				}
				resultMessage = "파일 저장 실패 하였습니다. IOException";
				e.printStackTrace();
			}
		}
		
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		try (FileInputStream fis = new FileInputStream(saveFile)) {
			// WorkbookFactory.create()가 .xls와 .xlsx 모두 자동으로 감지하여 읽어줍니다.
			Workbook workbook = WorkbookFactory.create(fis);
			Sheet sheet = workbook.getSheetAt(0);
			int lastRowNum = sheet.getLastRowNum();
			for (int i = 0; i < lastRowNum + 1; i++) {
				Map<String, String> map = new HashMap<String, String>();
				Row row = sheet.getRow(i);
				if (null == row) {
					continue;
				}
				int lastCellNum = row.getLastCellNum();
				for (int j = 0; j < lastCellNum; j++) {
					Cell cell = row.getCell(j);
					// 셀 값이 null일 경우 빈 문자열로 처리
					String cellValue = (cell == null) ? "" : cell.toString();
					map.put("key" + (j + 1), cellValue);
				}
				list.add(map);
			}
		} catch (NotOLE2FileException e) {
			if (isSaveExists) {
				saveFile.delete();
			}
			resultMessage = "파일읽기 실패 하였습니다. 정상적인 엑셀 파일이 아닙니다.";
			e.printStackTrace();
		} catch (IOException e) {
			if (isSaveExists) {
				saveFile.delete();
			}
			resultMessage = "파일읽기 실패 하였습니다. IOException";
			e.printStackTrace();
		} catch (Exception e) {
			if (isSaveExists) {
				saveFile.delete();
			}
			resultMessage = "파일읽기 실패 하였습니다. Exception";
			e.printStackTrace();
		}
		
		if (isSaveExists) {
			saveFile.delete();
		}
		
		model.addAttribute("resultList", list);
		model.addAttribute("resultMessage", resultMessage);
		return "jsonView";
	}

	@PostMapping("/newUploadExcelFile")
    public String newUploadExcelFile(MultipartHttpServletRequest request, ModelMap model) {
        // 1) 파일명용 타임스탬프
        String dateTime = DateUtil.getCurrentDateTime();
        String path = uploadDir + File.separator + "tmp_excel" + File.separator;
        boolean isSaveExists = false;
        File saveFile = null;
        String resultMessage = "";

        // 2) 파일 저장
        Iterator<String> iterator = request.getFileNames();
        if (iterator.hasNext()) {
            try {
                MultipartHttpServletRequest multipart = request;
                saveFile = new File(path + dateTime);
                if (!saveFile.getParentFile().isDirectory()) {
                    saveFile.getParentFile().mkdirs();
                }
                multipart.getFile(iterator.next()).transferTo(saveFile);
                isSaveExists = saveFile.exists();
            } catch (Exception e) {
                if (isSaveExists) saveFile.delete();
                resultMessage = "파일 저장 실패: " + e.getClass().getSimpleName();
                e.printStackTrace();
            }
        }

        // 3) POI 셀 읽기 + 포맷팅
        List<Map<String, String>> list = new ArrayList<>();
        DataFormatter dataFormatter = new DataFormatter(Locale.KOREA);
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

        try (FileInputStream fis = new FileInputStream(saveFile)) {
            Workbook workbook = WorkbookFactory.create(fis);
            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();

            for (int i = 0; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Map<String, String> map = new HashMap<>();
                int lastCellNum = row.getLastCellNum();

                for (int j = 0; j < lastCellNum; j++) {
                    Cell cell = row.getCell(j);
                    String cellValue = "";

                    if (cell != null) {
                        CellType type = cell.getCellType();
                        switch (type) {
                            case STRING:
                                cellValue = cell.getStringCellValue();
                                break;
                            case BOOLEAN:
                                cellValue = Boolean.toString(cell.getBooleanCellValue());
                                break;
                            case FORMULA:
                                cellValue = dataFormatter.formatCellValue(cell);
                                break;
                            case NUMERIC:
                                // 날짜 셀 감지 (POI DateUtil)
                                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                                    cellValue = dateFormatter.format(cell.getDateCellValue());
                                } else {
                                    double d = cell.getNumericCellValue();
                                    long l = (long) d;
                                    if (d == l) {
                                        cellValue = String.valueOf(l);
                                    } else {
                                        cellValue = BigDecimal.valueOf(d)
                                                            .stripTrailingZeros()
                                                            .toPlainString();
                                    }
                                }
                                break;
                            default:
                                cellValue = "";
                        }
                    }
                    map.put("key" + (j + 1), cellValue);
                }
                list.add(map);
            }
        } catch (NotOLE2FileException e) {
            if (isSaveExists) saveFile.delete();
            resultMessage = "파일읽기 실패: 정상적인 엑셀 파일이 아닙니다.";
            e.printStackTrace();
        } catch (IOException e) {
            if (isSaveExists) saveFile.delete();
            resultMessage = "파일읽기 실패: IOException";
            e.printStackTrace();
        } catch (Exception e) {
            if (isSaveExists) saveFile.delete();
            resultMessage = "파일읽기 실패: " + e.getClass().getSimpleName();
            e.printStackTrace();
        } finally {
            if (isSaveExists) saveFile.delete();
        }

        // 4) 결과 반환
        model.addAttribute("resultList", list);
        model.addAttribute("resultMessage", resultMessage);
        return "jsonView";
    }

	// BOM 전체 정전개 트리 조회
    @PostMapping("/selectBomSalesTreeList")
    public String selectBomSalesTreeList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	List<Map<String, String>> result = sm01Svc.selectBomSalesTreeList(paramMap);
    	model.addAttribute("result", result);
        return "jsonView";
    }

	//구매BOM관리 Master 조회
	@PostMapping(value = "/selectBomSalesList")
	public String selectBomSalesList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		paramMap.put("clntPjt", ObjectUtil.sqlInCodeGen(paramMap.get("clntPjt")));
		int totalCnt = sm01Svc.selectBomSalesCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = sm01Svc.selectBomSalesList(paramMap);
		model.addAttribute("resultList", result);

	   	List<Map<String, String>> resultPrjct = sm01Svc.select_prjct_code(paramMap);
	   	model.addAttribute("resultPrjct", resultPrjct);
		return "jsonView";
	}
	
	// BOM내역상세 조회
	@PostMapping(value = "/selectBomDetailList")
	public String selectBomDetailList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> resultList = sm01Svc.selectBomDetailList(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
	}

	//Bom 리스트
	@PostMapping("/selectBuyBomList")
	public String selectBuyBomList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> resultList = sm01Svc.selectBuyBomList(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
	}

	// 자재 리스트 조회
	@PostMapping("/selectBomMatrList")
	public String selectBomMatrList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> resultList = sm01Svc.selectBomMatrList(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
	}

	// Bom 자재 Map 조회
	@PostMapping(value = "/selectBomMatrInfo")
		public String selectBomMatrInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
		Map<String, String> result = sm01Svc.selectBomMatrInfo(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	// BOM Tree 조회
	@PostMapping(value = "/selectBomMatrTreeList")
	public String selectBomMatrTreeList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> resultList = sm01Svc.selectBomMatrTreeList(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
	}

	@PostMapping(value = "/insertBom")
	public String insertBom(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
		try {
			if (sm01Svc.insertBom(paramMap, mRequest) != 0 ) {
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

	@PostMapping(value = "/updateBom")
	public String updateBom(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
	  	try {
	  			model.addAttribute("resultCode", 200);
			if (sm01Svc.updateBom(paramMap, mRequest) != 0 ) {
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

	@PutMapping(value = "/deleteBomAll")
	public String deleteBomAll(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
	  	try {
			if (sm01Svc.deleteBomAll(paramMap) != 0 ) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
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

	@PostMapping(value = "/insertBomMatr")
	public String insertBomMatr(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
	  try {
		  if (sm01Svc.insertBomMatr(paramMap, mRequest) != 0 ) {
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

	@PostMapping(value = "/updateBomMatr")
	public String updateBomMatr(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
	  try {
		  if (sm01Svc.updateBomMatr(paramMap, mRequest) != 0 ) {
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

	@PutMapping(value = "/deleteBomMatr")
	public String deleteBomMatr(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
	  	try {
			if (sm01Svc.deleteBomMatr(paramMap) != 0 ) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
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

	@PostMapping(value = "/insertCrudMatrAndBom")
	public String insertCrudMatrAndBom(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
		try {
			if (sm01Svc.insertCrudMatrAndBom(paramMap, mRequest) != 0 ) {
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

	@PostMapping(value = "/insertUploadBom")
	public String insertUploadBom(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
		try {
			if (sm01Svc.insertUploadBom(paramMap, mRequest) != 0 ) {
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

	@PostMapping("/bomTreeList")
	public String bomTreeList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> resultList = sm01Svc.bomTreeList(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
	}

	@PostMapping(value = "/callCopyBom")
	public String callCopyBom(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
		try {
			sm01Svc.callCopyBom(paramMap, mRequest);
			
			model.addAttribute("resultCode", 200);
			model.addAttribute("resultMessage", paramMap.get("errMsg"));
		}catch(Exception e){
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}


	@PostMapping("/nextPrcsnNmList")
	public String nextPrcsnNmList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> resultList = sm01Svc.nextPrcsnNmList(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
	}

	// // BOM 동기화
    // @PostMapping("/syncBom")
    // public String syncBom(@RequestBody Map<String, String> paramMap, ModelMap model) {
    // 	try {
	// 		Map<String, String> delmatrbom = sm01Svc.deleteMatrbomChk(paramMap);
	// 		String delyn = delmatrbom.get("delyn");
	// 		if("N".equals(delyn)) {
    //     		model.addAttribute("resultCode", 500);
	// 			model.addAttribute("resultMessage", " 구매BOM정보가 변경되어 삭제할 수 없습니다.");
    //     		// model.addAttribute("resultMessage", delmatrbom.get("matrCd") + " : 발주정보가 존재하여 삭제할 수 없습니다.");
    //     	} else {
    //     	 	if (sm01Svc.deleteBom(paramMap) != 0 ) {
	// 				model.addAttribute("resultCode", 200);
	// 				model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
	// 			} else {
	// 				model.addAttribute("resultCode", 500);
	// 				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));    			
	// 			}
    //     	}
    // 	}catch(Exception e){
    // 		model.addAttribute("resultCode", 900);
	// 		model.addAttribute("resultMessage", e.getMessage());
    // 	}
    // 	return "jsonView";
    // }

	// 업로드 내용 적용 프로시져 호출
    @PostMapping("/syncBom")
    public String syncBom(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	Map<String, String> result = sm01Svc.syncBom(paramMap);
    	model.addAttribute("result", result);
    	return "jsonView";
    }
    
    


    // BOM Tree Node 복사
    @PostMapping("/copyMatrBomTree")
    public String copyMatrBomTree(@RequestBody List<Map<String, String>> paramList, ModelMap model) {
    	try {
    		if (sm01Svc.copyMatrBomTree(paramList) != 0 ) {
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


    // BOM Tree Node 이동
    @PostMapping("/moveMatrBom")
    public String moveMatrBom(@RequestBody List<Map<String, String>> paramList, ModelMap model) {
    	try {
    		if (sm01Svc.moveMatrBom(paramList) != 0 ) {
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
    
}