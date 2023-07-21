package com.dksys.biz.admin.cm.cm08.service.impl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
        List<MultipartFile> fileList = mRequest.getFiles("files");
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
    
}