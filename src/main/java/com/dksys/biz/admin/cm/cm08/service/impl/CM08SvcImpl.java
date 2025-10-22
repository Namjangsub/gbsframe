package com.dksys.biz.admin.cm.cm08.service.impl;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.admin.cm.cm08.mapper.CM08Mapper;
import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.util.DateUtil;

@Service
public class CM08SvcImpl implements CM08Svc {
    private static final Logger logger = LoggerFactory.getLogger(CM08SvcImpl.class);

    @Autowired
    CM08Mapper cm08Mapper;

    @Value("${file.uploadDir}")
    private String uploadDir;

    @Override
    public int uploadFile(String fileTrgtTyp, String fileTrgtKey, MultipartHttpServletRequest mRequest) {
        List<MultipartFile> fileList =  new ArrayList<>();
        //PM0101M01_M ==> 작업일보에서 출장경비용 첨부파일인경우, 그외 해당 화면의 프로그램명으로 들어옴
        if ("PM0101M01_M".equals(fileTrgtTyp)) {
        	// 작업일보번호 : DM20240402-3615-1  마지막 일련번호 추출
        	String str = fileTrgtKey;
        	String lastPart = "rptTripFiles";
            int lastIndex = str.lastIndexOf('-'); // 마지막 '-'의 위치
            if (lastIndex != -1 && lastIndex < str.length() - 1) { // 마지막 '-' 문자뒤에 있나?
                lastPart += str.substring(lastIndex + 1); 
            }
        	
        	//rptTripFiles1, rptTripFiles2, rptTripFiles3으로 담겨져 넘어옴
//        	fileList = mRequest.getFiles("rptTripFiles1");
        	fileList = mRequest.getFiles(lastPart);
        } else if ("PM5001M01".equals(fileTrgtTyp)) {
			String str = fileTrgtKey;
			String lastPart = "bfuFiles";
			int lastIndex = str.lastIndexOf('-'); // 마지막 '-'의 위치
			if (lastIndex != -1 && lastIndex < str.length() - 1) { // 마지막 '-' 문자뒤에 있나?
				lastPart += str.substring(lastIndex + 1); 
			}
			fileList = mRequest.getFiles(lastPart);
		} else {
            fileList = mRequest.getFiles("files");
        }
        
        if (fileList != null && !fileList.isEmpty()) {
        	//저장할 파일이 있다면 여기
        } else { //첨부 파이이 없으므로 작업 종료함.
        	return 0;
        }
        String year = DateUtil.getCurrentYyyy();
        String month = DateUtil.getCurrentMm();
        String path = uploadDir + File.separator + fileTrgtTyp + File.separator + year + File.separator + month + File.separator;
        for (MultipartFile mf : fileList) {
            String originFileName = mf.getOriginalFilename(); // 원본 파일 명
            // long fileSize = mf.getSize(); // 파일 사이즈

            HashMap<String, String> param = new HashMap<String, String>();
            param.put("fileSize", String.valueOf(mf.getSize()));
            if (originFileName != null) {
                // originFileName으로 작업 수행
                param.put("fileType", originFileName.split("\\.")[originFileName.split("\\.").length - 1]);
                param.put("fileName", originFileName);
            } else {
                // originFileName이 null인 경우 처리
                param.put("fileType", "err");
                param.put("fileName", "error");
            }
            param.put("filePath", path);
            param.put("fileTrgtTyp", fileTrgtTyp);
            param.put("fileTrgtKey", fileTrgtKey);
            param.put("userId", mRequest.getParameter("userId"));
            param.put("pgmId", fileTrgtTyp);
            param.put("coCd", mRequest.getParameter("coCd"));
            param.put("comonCd", mRequest.getParameter("comonCd"));
            if ("TB_BM02M01".equals(fileTrgtTyp)) {
            	param.put("clntCd", fileTrgtKey);
            } else {
        		Map<String, String> chk = fetchAllowedDataMap(param);
        		param.put("coCd",    nz(chk,"coCd")    == null ? "" : nz(chk,"coCd"));
        		param.put("clntCd",  nz(chk,"clntCd")  == null ? "" : nz(chk,"clntCd"));
        		param.put("prjctCd", nz(chk,"prjctCd") == null ? "" : nz(chk,"prjctCd"));
        		param.put("ordrsNo", nz(chk,"ordrsNo") == null ? "" : nz(chk,"ordrsNo"));
        		param.put("itemCd",  nz(chk,"itemDiv") == null ? "" : nz(chk,"itemDiv")); 
        		param.put("prdtCd",  nz(chk,"prdtCd")  == null ? "" : nz(chk,"prdtCd"));
        		param.put("salesCd", nz(chk,"salesCd") == null ? "" : nz(chk,"salesCd"));
            }
            try {
                cm08Mapper.insertFile(param);
                String saveFile = param.get("fileKey") + "_" + originFileName;
                File f = new File(path);
                if (!f.isDirectory()) f.mkdirs();

                // if(fileTrgtTyp.equals("TB_OD01M01") || fileTrgtTyp.equals("TB_BM02M01") || fileTrgtTyp.equals("TB_OD02M01") || fileTrgtTyp.equals("TB_AR14M01")) {
                mf.transferTo(new File(path + saveFile));
                // }

            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    //CLNT_CD,PRDT_CD,ITEM_CD,SALES_CD,PRJCT_CD,CO_CD,COMON_CD-->paramMap 으로 처리
    //화일관리 추가 구현
    @Override
    public int uploadFile(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) {
		String fileTrgtTyp = String.valueOf(paramMap.get("fileTrgtTyp"));
    	String fileTrgtKey = String.valueOf(paramMap.get("fileTrgtKey"));
    	
    	List<MultipartFile> fileList = mRequest.getFiles("files");
    	String year = DateUtil.getCurrentYyyy();
    	String month = DateUtil.getCurrentMm();
    	String path = uploadDir + File.separator + fileTrgtTyp + File.separator + year + File.separator + month + File.separator;
    	for (MultipartFile mf : fileList) {
    		String originFileName = mf.getOriginalFilename(); // 원본 파일 명
    		// long fileSize = mf.getSize(); // 파일 사이즈
    		
    		HashMap<String, String> param = new HashMap<String, String>(paramMap);
    		
    		param.put("fileSize", String.valueOf(mf.getSize()));
    		if (originFileName != null) {
    			// originFileName으로 작업 수행
    			param.put("fileType", originFileName.split("\\.")[originFileName.split("\\.").length - 1]);
    			param.put("fileName", originFileName);
    		} else {
    			// originFileName이 null인 경우 처리
    			param.put("fileType", "err");
    			param.put("fileName", "error");
    		}
    		param.put("filePath", path);
    		param.put("fileTrgtTyp", fileTrgtTyp);
    		param.put("fileTrgtKey", fileTrgtKey);
    		param.put("pgmId", fileTrgtTyp);
       		if (!"".equals(mRequest.getParameter("userId"))) {
       			param.put("userId", mRequest.getParameter("userId"));
    		}
    		if (!"".equals(mRequest.getParameter("coCd"))) {
    			param.put("coCd", mRequest.getParameter("coCd"));
    		}
    		if (!"".equals(mRequest.getParameter("clntCd"))) {
    			param.put("clntCd", mRequest.getParameter("clntCd"));
    		}
    		if (!"".equals(mRequest.getParameter("prdtCd"))) {
    			param.put("prdtCd", mRequest.getParameter("prdtCd"));
    		}
    		if (!"".equals(mRequest.getParameter("itemCd"))) {
    			param.put("itemCd", mRequest.getParameter("itemCd"));
    		}
    		if (!"".equals(mRequest.getParameter("salesCd"))) {
    			param.put("salesCd", mRequest.getParameter("salesCd"));
    		}
    		if (!"".equals(mRequest.getParameter("prjctCd"))) {
    			param.put("prjctCd", mRequest.getParameter("prjctCd"));
    		}
    		
    		 		
    		Map<String, String> chk = fetchAllowedDataMap(param);
    		param.put("coCd",    nz(chk,"coCd")    == null ? "" : nz(chk,"coCd"));
    		param.put("clntCd",  nz(chk,"clntCd")  == null ? "" : nz(chk,"clntCd"));
    		param.put("prjctCd", nz(chk,"prjctCd") == null ? "" : nz(chk,"prjctCd"));
    		param.put("ordrsNo", nz(chk,"ordrsNo") == null ? "" : nz(chk,"ordrsNo"));
    		param.put("itemCd", nz(chk,"itemDiv") == null ? "" : nz(chk,"itemDiv")); 
    		param.put("prdtCd",  nz(chk,"prdtCd")  == null ? "" : nz(chk,"prdtCd"));
    		param.put("salesCd", nz(chk,"salesCd") == null ? "" : nz(chk,"salesCd"));
    		
            try {
    			cm08Mapper.insertFile(param);
    			String saveFile = param.get("fileKey") + "_" + originFileName;
    			File f = new File(path);
    			if (!f.isDirectory()) f.mkdirs();
    			
    			// if(fileTrgtTyp.equals("TB_OD01M01") || fileTrgtTyp.equals("TB_BM02M01") || fileTrgtTyp.equals("TB_OD02M01") || fileTrgtTyp.equals("TB_AR14M01")) {
    			mf.transferTo(new File(path + saveFile));
    			// }
                paramMap.put("fileKey", param.get("fileKey"));
    		} catch (IllegalStateException e) {
    			e.printStackTrace();
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    	}
    	return 0;
    }
    @Override
    public  int uploadTreeFile(String fileTrgtTyp,String fileTrgtKey, MultipartHttpServletRequest mRequest) {
        return 1;
    }
    @Override
    public  int uploadTreeFile(String fileTrgtTyp,Map<String, String> paramMap, MultipartHttpServletRequest mRequest) {
        List<MultipartFile> fileList = mRequest.getFiles("files");
        String year = DateUtil.getCurrentYyyy();
        String month = DateUtil.getCurrentMm();
        String path = uploadDir + File.separator + fileTrgtTyp + File.separator + year + File.separator + month + File.separator;
        for (int i = 0; i < fileList.size(); i++) {
            try {
                MultipartFile mf = fileList.get(i);
                String originFileName = mf.getOriginalFilename(); // 원본 파일 명
                HashMap<String, String> param = new HashMap<String, String>();
                param.put("fileSize", String.valueOf(mf.getSize()));
                if (originFileName != null) {
                    // originFileName으로 작업 수행
                    param.put("fileType", originFileName.split("\\.")[originFileName.split("\\.").length - 1]);
                    param.put("fileName", originFileName);
                } else {
                    // originFileName이 null인 경우 처리
                    param.put("fileType", "err");
                    param.put("fileName", "error");
                }
                param.put("filePath", path);
                param.put("fileTrgtTyp", fileTrgtTyp);
                param.put("userId", mRequest.getParameter("userId"));
                param.put("pgmId", fileTrgtTyp);
                param.put("coCd", mRequest.getParameter("coCd"));
                String comonCdKey = "comonCd_" + i;
                String comonCd = mRequest.getParameter(comonCdKey);
                param.put("comonCd", comonCd);
                param.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
                String userId = mRequest.getParameter("userId");
                param.put("creatId", userId);
                param.put("creatPgm", fileTrgtTyp);
                cm08Mapper.insertFile(param);
                String saveFile = param.get("fileKey") + "_" + originFileName;
                File f = new File(path);
                if (!f.isDirectory()) f.mkdirs();
                // if(fileTrgtTyp.equals("TB_OD01M01") || fileTrgtTyp.equals("TB_BM02M01") || fileTrgtTyp.equals("TB_OD02M01") || fileTrgtTyp.equals("TB_AR14M01")) {
                mf.transferTo(new File(path + saveFile));
                // }

            } catch (IllegalStateException e) {
                logger.error("IllegalStateException occurred: ", e);
            } catch (IOException e) {
                logger.error("IOException occurred: ", e);
            }
        }
        return 0;
    }
    @Override
    public int copyTreeFile(String fileTrgtTyp, Map<String, String> paramMap, MultipartHttpServletRequest mRequest) {
        System.out.println("copyTreeFile 실행");
        String year = DateUtil.getCurrentYyyy();
        String month = DateUtil.getCurrentMm();
        String path = uploadDir + File.separator + fileTrgtTyp + File.separator + year + File.separator + month + File.separator;

        int i = 0;
        while (true) {
            System.out.println("카운트: "+i);
            String comonCdKey = "comonCd_" + i;
            String filePathKey = "filePath_" + i;
            String fileNameKey = "fileName_" + i;
            String fileKey = "fileKey_" + i;

            String comonCd = mRequest.getParameter(comonCdKey);
            String filePath = mRequest.getParameter(filePathKey);
            String fileName = mRequest.getParameter(fileNameKey);
            String filePKey = mRequest.getParameter(fileKey);

            System.out.println("카운트: "+i +"||"+comonCd+"||"+filePath+"||"+fileName);
            if (comonCd == null || filePath == null || fileName == null) {
                break; // 해당 키로부터 정보를 얻지 못한 경우, 더 이상의 처리를 멈춥니다.
            }

            // 복사된 파일에 대한 정보를 DB에 저장
            HashMap<String, String> param = new HashMap<>();
            param.put("fileSize", "0");
            param.put("fileType", fileName.substring(fileName.lastIndexOf(".") + 1));
            param.put("fileName", fileName);
            param.put("filePath", path);
            param.put("fileTrgtTyp", fileTrgtTyp);
            param.put("userId", mRequest.getParameter("userId"));
            param.put("pgmId", fileTrgtTyp);
            param.put("coCd", mRequest.getParameter("coCd"));
            param.put("comonCd", comonCd);
            param.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
            String userId = mRequest.getParameter("userId");
            param.put("creatId", userId);
            param.put("creatPgm", fileTrgtTyp);
            cm08Mapper.insertFile(param);
            String nowFileKey = param.get("fileKey");

            File sourceFile = new File(filePath +filePKey+"_"+fileName);
            String saveFile = nowFileKey + "_" + fileName;
            File targetFile = new File(path + saveFile);

            if (!targetFile.getParentFile().exists()) { // 타겟 경로가 존재하지 않으면 생성
                targetFile.getParentFile().mkdirs();
            }

            try {
                Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
                return -1; // 파일 복사에 실패한 경우 -1을 반환
            }


            i++;
        }

        return 0;
    }
    @Override
    public List<Map<String, String>> selectFileList(Map<String, String> paramMap) {
        return cm08Mapper.selectFileList(paramMap);
    }
	@Override
	public int  selectTreeFileCount(Map<String, String> paramMap) {
		return cm08Mapper.selectTreeFileCount(paramMap);
	}
    @Override
    public List<Map<String, String>> selectTreeFileList(Map<String, String> paramMap) {
        return cm08Mapper.selectTreeFileList(paramMap);
    }
    @Override
    public List<Map<String, String>> selectTreeFileModule(Map<String, String> paramMap) {
        System.out.println(paramMap+"트리Log");
        return cm08Mapper.selectTreeFileModule(paramMap);
    }
    @Override
    public void setDisposition(HttpServletRequest request, HttpServletResponse response, String fileName) {
        try {
            String browser = getBrowser(request);
            String dispositionPrefix = "attachment; filename=";
            String encodedFilename = null;
            if (browser.equals("MSIE")) {
                encodedFilename = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            } else if (browser.equals("Trident")) { // IE11 문자열 깨짐 방지
                encodedFilename = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            } else if (browser.equals("Firefox")) {
                encodedFilename = "\"" + new String(fileName.getBytes("UTF-8"), "8859_1") + "\"";
            } else if (browser.equals("Opera")) {
                encodedFilename = "\"" + new String(fileName.getBytes("UTF-8"), "8859_1") + "\"";
            } else if (browser.equals("Chrome")) {
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < fileName.length(); i++) {
                    char c = fileName.charAt(i);
                    if (c > '~') {
                        sb.append(URLEncoder.encode("" + c, "UTF-8"));
                    } else {
                        sb.append(c);
                    }
                }
                encodedFilename = "\"" + sb.toString() + "\"";
            } else {
                throw new IOException("Not supported browser");
            }
            response.setHeader("Content-Disposition", dispositionPrefix + encodedFilename);
            if ("Opera".equals(browser)) {
                response.setContentType("application/octet-stream;charset=UTF-8");
            }
            response.setHeader("Content-Transfer-Encoding", "binary");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public String getBrowser(HttpServletRequest request) {
        String header = request.getHeader("User-Agent");
        if (header.indexOf("MSIE") > -1) {
            return "MSIE";
        } else if (header.indexOf("Trident") > -1) { // IE11 문자열 깨짐 방지
            return "Trident";
        } else if (header.indexOf("Chrome") > -1) {
            return "Chrome";
        } else if (header.indexOf("Opera") > -1) {
            return "Opera";
        }
        return "Firefox";
    }
    @Override
    public Map<String, String> selectFileInfo(String fileKey) {
        return cm08Mapper.selectFileInfo(fileKey);
    }
    @Override
    public int deleteFile(String fileKey) {
        Map<String, String> fileInfo = selectFileInfo(fileKey);
        String filePath = fileInfo.get("filePath") + fileKey + "_" + fileInfo.get("fileName");
        int result = cm08Mapper.deleteFile(fileKey);
        try {
            File f = new File(filePath);
            f.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    @Override
    public String excelDownload(List<Map<String, String>> list, String fileName) {
        if (list.size() == 0) return "noData";
        try (XSSFWorkbook xWorkbook = new XSSFWorkbook()) {
            XSSFCellStyle numberCellStyle = xWorkbook.createCellStyle();
            XSSFDataFormat numberDataFormat = xWorkbook.createDataFormat();
            numberCellStyle.setDataFormat(numberDataFormat.getFormat("#,##0"));
            XSSFSheet sheet = xWorkbook.createSheet("sheet1");
            Row row = null;
            int j = 0;
            //컬럼 생성
            row = sheet.createRow(0);
            Map<String, String> map = list.get(0);
            //컬럼 생성
            for (String key : map.keySet()) {
                Cell cell = row.createCell(j);
                cell.setCellValue(key);
                j++;
            }
            //행 데이터 생성
            for (int i = 0; i < list.size(); i++) {
                j = 0;
                map = list.get(i);
                row = sheet.createRow(i + 1);
                for (String key : map.keySet()) {
                    Cell cell = row.createCell(j);
                    cell.setCellValue(map.get(key));
                    j++;
                }
            }
            FileOutputStream fileOut = null;
            String path = "C:\\upload\\" + fileName;
            File f = new File(path);
//        	if(!f.isDirectory()) f.mkdirs();
            fileOut = new FileOutputStream(f);
            xWorkbook.write(fileOut);
            f.deleteOnExit();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileName;
    }
	@Override
	public int selectConfirmCount(Map<String, String> paramMap) {
		return cm08Mapper.selectConfirmCount(paramMap);
	}
	@Override
	public int moveFile(Map<String, String> paramMap) {
		// update
		int result = cm08Mapper.moveFile(paramMap);
		return result;
	}
	@Override
	public int deleteFileCall(Map<String, String> paramMap) {
		String fileKey = paramMap.get("fileKey");
        Map<String, String> fileInfo = selectFileInfo(fileKey);
        String filePath = fileInfo.get("filePath") + fileKey + "_" + fileInfo.get("fileName");
        int result = cm08Mapper.deleteFile(fileKey);
        try {
            File f = new File(filePath);
            f.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;		
	}
	
    @Override
    public Map<String, String> selectFileInfoUser(Map<String, String> paramMap) {
        return cm08Mapper.selectFileInfoUser(paramMap);
    }

	@Override
	public List<Map<String, String>> selectFileListAll(Map<String, String> paramMap) {
		return cm08Mapper.selectFileListAll(paramMap);
	}
    
	// 유틸: null/빈/문자열 "null"/"undefined" → null
	private static String nz(Map<String, String> m, String k) {
	    if (m == null) return null;
	    String v = m.get(k);
	    if (v == null) return null;
	    v = v.trim();
	    if (v.isEmpty()) return null;
	    if ("null".equalsIgnoreCase(v) || "undefined".equalsIgnoreCase(v)) return null;
	    return v;
	}


    private static Map<String, String> outMap() {
        // 순서 보장
        return new LinkedHashMap<String, String>(8);
    }

    private static boolean anyEqualsIgnoreCase(String s, String... arr) {
        if (s == null) return false;
        for (String a : arr) {
            if (s.equalsIgnoreCase(a)) return true;
        }
        return false;
    }

    /** 공통 아웃맵 채우기 (null -> "") */
    private static void fillOut(Map<String, String> out,
                                String coCd, String clntCd, String prjctCd,
                                String ordrsNo, String itemDiv, String prdtCd, String salesCd) {
        out.put("coCd",   coCd   == null ? "" : coCd);
        out.put("clntCd", clntCd == null ? "" : clntCd);
        out.put("prjctCd",prjctCd== null ? "" : prjctCd);
        out.put("ordrsNo",ordrsNo== null ? "" : ordrsNo);
        out.put("itemDiv",itemDiv== null ? "" : itemDiv);
        out.put("prdtCd", prdtCd == null ? "" : prdtCd);
        out.put("salesCd",salesCd== null ? "" : salesCd);
    }
    
    // fileTrgtTyp: SALES_CD | ORDRS_NO | CLNT_PJT | CLNT_CD | M.CO_CD
	/*******************************************************************************
	 * 문서등록업무명	        : 발생업무코드	     |coCd clntCd prjctCd salesCd prdtCd itemCd
	 * ----------------------------------------------------------------------------
     * 자재마스터				: BM0501P01			O					
     * 메뉴얼					: TB_BM99P01        O					
     * 공지사항				: TB_CM09M01        O					
     * 자료실					: TB_CM14M01        O					
     * 임팀장 주간회의 자료		: PM1002M01         O					
     * 업무요청관리				: CM1601M01         O					
     * 거래처 관리				: TB_BM02M01        O	O				
     * 프로젝트 이슈관리			: BM1601P02     	O	O	O			
     * 견적서관리				: CR0101M01         O	O	O			
     * 수주 등록				: CR0202P01         O	O	O			
     * 수금 등록				: CR0501P01         O	O	O			
     * 매출계산서 등록			: CR0801P01         O	O	O	
     * 개선 제안서 등록			: IM0101P01         O	O	O					
     * 물류진행요청 등록			: CR1001P01     	O	O	O	O	O	O
     * PFU관리				: CR5001P01         O	O	O	O	O	O
     * 작업일보관리				: PM0101M01         O	O	O	O	O	O
     * 작업일보관리				: PM0101M01_M       O	O	O	O	O	O
     * 작업일보관리				: PM0101P01         O	O	O	O	O	O
     * 출장자 사진 Upload작업	: PM5001M01         O	O	O	O	O	O
     * 출장자 사진 Upload작업	: PM5001P01_M       O	O	O	O	O	O
     * 요인별 발주 및 출장요청		: QM0101P01     	O	O	O	O	O	O
     * 요인별 발주 및 출장결과		: QM0101P03     	O	O	O	O	O	O
     * 매입 발주 등록			: SM0201P01         O	O	O	O	O	O
     * 매입 입고				: SM0301P01         O	O	O	O	O	O
     * 구매비용 등록			: SM1001P01         O	O	O	O	O	O
     * 외주관리 등록			: SM1101P01         O	O	O	O	O	O
     * 과제 관리 등록			: WB2101P01         O	O	O	O	O	O
     * TASK 실적 등록			: WB2201P02         O	O	O	O	O	O
     * WBS 계획 문제			: WB2401P01         O	O	O	O	O	O
     * WBS 실적 문제			: WB2401P11         O	O	O	O	O	O
	 ********************************************************************************/
    private Map<String, String> fetchAllowedDataMap (Map<String, String> paramMap) {

        if (paramMap == null) return Collections.emptyMap();
        

        // 1) typ 정규화 (없으면 빈맵 반환)
        String typ = nz(paramMap, "fileTrgtTyp");
        if (typ == null) return Collections.emptyMap();
        typ = typ.toUpperCase();
        
        
        // 즉시 반환 그룹 1: coCd만 관리 → coCd 외 나머지 공란
        if (anyEqualsIgnoreCase(typ,
                "BM0501P01","TB_BM99P01","TB_CM09M01","TB_CM14M01","PM1002M01","CM1601M01")) {
            Map<String, String> out = outMap();
            fillOut(out,
                    nz(paramMap,"coCd"), null, null,
                    null, null, null, null);
            return out;
        }


        // 즉시 반환 그룹 2: 거래처 관리
        if ("TB_BM02M01".equalsIgnoreCase(typ)) {
            Map<String, String> out = outMap();
            fillOut(out,
                    nz(paramMap,"coCd"), nz(paramMap,"clntCd"), null,
                    null, null, null, null);
            return out;
        }


        // 즉시 반환 그룹 3: 거래처/프로젝트코드 관련
        if (anyEqualsIgnoreCase(typ, "BM1601P02","CR0202P01","CR0501P01","CR0801P01","IM0101P01")) {
            Map<String, String> out = outMap();
            fillOut(out,
                    nz(paramMap,"coCd"), nz(paramMap,"clntCd"), nz(paramMap,"prjctCd"),
                    null, null, null, null);
            return out;
        }
        

        // 이외의 경우: DB 조회
        /******************************************************************************************
         * 1. 각 업무별 테이블에 있는 값을 기준으로설정하기
         *******************************************************************************************/
        Map<String, String> m = cm08Mapper.selectMByTarget(paramMap);
        m.put("FILE_TRGT_TYP",typ); //String typ = nz(paramMap, "fileTrgtTyp");

        // 타입 결정 우선순위: salesCd > ordrsNo > clntCd > coCd
        String salesCd = nz(m,"salesCd"); if (salesCd == null) salesCd = nz(paramMap,"salesCd");
        String ordrsNo = nz(m,"ordrsNo"); if (ordrsNo == null) ordrsNo = nz(paramMap,"ordrsNo");
        String clntCd  = nz(m,"clntCd");  if (clntCd  == null) clntCd  = nz(paramMap,"clntCd");
        String coCd    = nz(m,"coCd");    if (coCd    == null) coCd    = nz(paramMap,"coCd");
        
        String type;
        if (salesCd != null)       type = "SALES_CD";
        else if (ordrsNo != null)  type = "ORDRS_NO";
        else if (clntCd  != null)  type = "CLNT_CD";
        else if (coCd    != null)  type = "CO_CD";
        else return Collections.emptyMap();
        	

        Map<String, String> out = outMap();
        Map<String, String> rm;

        /******************************************************************************************
         * 2. 추출가능한 자료를 기준으로 파일 저장 테이블 설정하기
         *******************************************************************************************/
        if ("SALES_CD".equals(type)) {
            m.put("TYPE", "SALES_CD");
            m.put("salesCd", salesCd);
            rm = cm08Mapper.selectMByRetriveValue(m); // 전부 허용
            if (rm == null) rm = Collections.<String,String>emptyMap();
            fillOut(out,
                    nz(rm,"coCd"), nz(rm,"clntCd"), nz(rm,"prjctCd"),
                    nz(rm,"ordrsNo"), nz(rm,"itemDiv"), nz(rm,"prdtCd"), nz(rm,"salesCd"));
            return out;

        } else if ("ORDRS_NO".equals(type)) {
            m.put("TYPE", "ORDRS_NO");
            m.put("ordrsNo", ordrsNo);
            rm = cm08Mapper.selectMByRetriveValue(m); // ordrsNo, clntPjt, clntCd, coCd
            if (rm == null) rm = Collections.<String,String>emptyMap();
            fillOut(out,
                    nz(rm,"coCd"), nz(rm,"clntCd"), nz(rm,"prjctCd"),
                    nz(rm,"ordrsNo"), null, null, null); // itemDiv/prdtCd/salesCd는 공란
            return out;

        } else if ("CLNT_CD".equals(type)) {
            fillOut(out,
            		coCd, clntCd, null,
                    null, null, null, null);
            return out;

        }
//        } else if ("CO_CD".equals(type)) {
            // coCd만
            fillOut(out,
                    coCd, null, null,
                    null, null, null, null);
            return out;

    }
	
}