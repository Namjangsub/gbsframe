package com.dksys.biz.user.cr.cr10.service.impl;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.admin.cm.cm15.service.CM15Svc;
import com.dksys.biz.user.cr.cr10.mapper.CR10Mapper;
import com.dksys.biz.user.cr.cr10.service.CR10Svc;
import com.dksys.biz.user.qm.qm01.mapper.QM01Mapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class CR10SvcImpl implements CR10Svc {

  @Autowired
  CR10Mapper cr10Mapper;

  @Autowired
  QM01Mapper QM01Mapper;
  
  @Autowired
  CM15Svc cm15Svc;

  @Autowired
  CM08Svc cm08Svc;

  @Override
  public int selectLgistReqPageCount(Map<String, String> paramMap) {
    return cr10Mapper.selectLgistReqPageCount(paramMap);
  }

  @Override
  public List<Map<String, String>> selectLgistReqPageList(Map<String, String> paramMap) {
    return cr10Mapper.selectLgistReqPageList(paramMap);
  }

  @Override
  public List<Map<String, String>> selectLgistReqList(Map<String, String> paramMap) {
    return cr10Mapper.selectLgistReqList(paramMap);
  }

  @Override
  public List<Map<String, String>> selectSelesCdList(Map<String, String> paramMap) {
    return cr10Mapper.selectSelesCdList(paramMap);
  }

  @Override
  public List<Map<String, String>> selectSelesCdViewList(Map<String, String> paramMap) {
    return cr10Mapper.selectSelesCdViewList(paramMap);
  }

  @Override
  public Map<String, String> selectLgistMastInfo(Map<String, String> paramMap) {
    return cr10Mapper.selectLgistMastInfo(paramMap);
  }
  @Override
  public Map<String, String> selectDefaultLgistLocation(Map<String, String> paramMap) {
    return cr10Mapper.selectDefaultLgistLocation(paramMap);
  }

  @Override
  public Map<String, String> selectDefaultTrnsDiv(Map<String, String> paramMap) {
    return cr10Mapper.selectDefaultTrnsDiv(paramMap);
  }
  
  @Override
  public List<Map<String, String>> selectLgistHistoryList(Map<String, String> paramMap) {
    return cr10Mapper.selectLgistHistoryList(paramMap);
  }
  
  @Override
  public List<Map<String, String>> selectLgistItemList(Map<String, String> paramMap) {
	  List<Map<String, String>> list = cr10Mapper.selectLgistItemList(paramMap);

	  String lgistNo = paramMap.get("lgistNo");
	  for (Map<String, String> row : list) {
	      String seq = row.get("lgistNoSeq");
	      String hasImg = row.get("hasImg");
	      if ("Y".equalsIgnoreCase(hasImg)) {
	          row.put("imgUrl", "/user/cr/cr10/itemListImage?lgistNo=" + lgistNo + "&lgistNoSeq=" + seq);
	      } else {
	          row.put("imgUrl", "");
	      }
	  }
	  return list;
  }


  @Override
  public Map<String, Object> checkLgistItemImage(Map<String, String> paramMap) {
  	  return cr10Mapper.checkLgistItemImage(paramMap);
  }
	
  @Override
  public String selectLgistAppCount(Map<String, String> paramMap) {
	  return cr10Mapper.selectLgistAppCount(paramMap);
  }

  @Override
  public List<Map<String, String>> selectLgistAppList(Map<String, String> paramMap) {
    return cr10Mapper.selectLgistAppList(paramMap);
  }

  @Override
  public Map<String, String> insertLgistMast(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {

	  	Map rtnMap = new HashMap();
	  	
	    Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
	    Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();

		//---------------------------------------------------------------
		//첨? ?일 처리 권한체크 ?작 -->?일 ?로?? ?? 권한 ?으?Exception 처리 ??
	  	//   ?수?:  jobType, userId, comonCd
		//---------------------------------------------------------------
		List<Map<String, String>> uploadFileList = gsonDtl.fromJson(paramMap.get("uploadFileArr"), dtlMap);
		if (uploadFileList.size() > 0) {
				//?근 권한 ?으?Exception 발생
				paramMap.put("jobType", "fileUp");
				cm15Svc.selectFileAuthCheck(paramMap);
		}
		//---------------------------------------------------------------
		//첨? ?일 권한체크  ??
		//---------------------------------------------------------------
		String  lgistNo = String.valueOf(cr10Mapper.selectLgistNoNext(paramMap));
		paramMap.put("lgistNo", lgistNo);
		int result = cr10Mapper.insertLgistMast(paramMap);
		rtnMap.put("result", String.valueOf(result));
		rtnMap.put("lgistNo", lgistNo);

	    List<Map<String, String>> salesCdList = gsonDtl.fromJson(paramMap.get("salesCdArr"), dtlMap);
	    for (Map<String, String> dtl : salesCdList) {
	    	dtl.put("lgistNo", lgistNo);
	    	dtl.put("userId", paramMap.get("userId"));
	    	dtl.put("pgmId", paramMap.get("pgmId"));
	    	

			paramMap.put("salesCd", dtl.get("salesCd"));
			
	    	String dtaChk = dtl.get("dtaChk").toString();
	    	if ("I".equals(dtaChk)) {
	    		cr10Mapper.insertLgistDetail(dtl);
	    	} else if ("U".equals(dtaChk)) {
	    		cr10Mapper.updateLgistDetail(dtl);
	    	} else if ("D".equals(dtaChk)) {
	    		cr10Mapper.deleteLgistDetail(dtl);
	    	}
	    }

		if (paramMap.get("tripRptS") != null && paramMap.get("tripRptS").length() > 0 && !"\"\"".equals(paramMap.get("tripRptS"))) {
			List<Map<String, String>> tripRptS = gsonDtl.fromJson(paramMap.get("tripRptS"), dtlMap);
			List<MultipartFile> files = mRequest.getFiles("lgistFiles");
			String[] seqArr = mRequest.getParameterValues("lgistFilesSeq");
			Map<String, MultipartFile> fileMap = new HashMap<>();
			if (files != null && seqArr != null) {
				int n = Math.min(files.size(), seqArr.length);
				for (int i = 0; i < n; i++) {
					String seq = (seqArr[i] == null) ? "" : seqArr[i].trim();
					MultipartFile mf = files.get(i);
					if (seq.isEmpty() || mf == null || mf.isEmpty()) continue;
					fileMap.put(seq, mf);
				}
			}

			for (Map<String, String> dtl : tripRptS) {
				dtl.put("lgistNo", lgistNo);
				dtl.put("userId", paramMap.get("userId"));
				dtl.put("pgmId", paramMap.get("pgmId"));

				String seq = dtl.get("lgistNoSeq");
				if (seq == null || seq.trim().isEmpty()) {
					seq = dtl.get("workRptNoSeq");
				}
				MultipartFile mf = (seq == null) ? null : fileMap.get(seq);

				if (mf != null && !mf.isEmpty()) {
					byte[] blob = mf.getBytes();
					String orgName = safeFileName(mf.getOriginalFilename(), 50);
					String mime = (mf.getContentType() == null) ? "" : mf.getContentType();
					dtl.put("lgistItemImgNm", orgName);
					dtl.put("lgistItemImgMime", mime);
					@SuppressWarnings("unchecked")
					Map raw = (Map) dtl;
					raw.put("lgistItemImg", blob);
				} else {
					dtl.put("lgistItemImgNm", "");
					dtl.put("lgistItemImgMime", "");
					@SuppressWarnings("unchecked")
					Map raw = (Map) dtl;
					raw.put("lgistItemImg", null);
				}

				String updCheck = dtl.get("updCheck");
				if ("D".equals(updCheck)) {
					cr10Mapper.deleteLgistItemImageDetail(dtl);
				} else {
					cr10Mapper.mergeLgistItemImageDetail(dtl);
				}
			}
		}

		if (paramMap.get("partListS") != null && paramMap.get("partListS").length() > 0 && !"\"\"".equals(paramMap.get("partListS"))) {
			List<Map<String, String>> partListS = gsonDtl.fromJson(paramMap.get("partListS"), dtlMap);
			List<MultipartFile> filesp = mRequest.getFiles("partFiles");
			String[] seqArrp = mRequest.getParameterValues("partFilesSeq");
			Map<String, MultipartFile> fileMapp = new HashMap<>();
			if (filesp != null && seqArrp != null) {
				int n = Math.min(filesp.size(), seqArrp.length);
				for (int i = 0; i < n; i++) {
					String seq = (seqArrp[i] == null) ? "" : seqArrp[i].trim();
					MultipartFile mf = filesp.get(i);
					if (seq.isEmpty() || mf == null || mf.isEmpty()) continue;
					fileMapp.put(seq, mf);
				}
			}

			for (Map<String, String> dtl : partListS) {
				dtl.put("lgistNo", lgistNo);
				dtl.put("userId", paramMap.get("userId"));
				dtl.put("pgmId", paramMap.get("pgmId"));

				String seq = dtl.get("lgistPartNoSeq");
				if (seq == null || seq.trim().isEmpty()) {
					seq = dtl.get("workRptNoSeq");
				}
				MultipartFile mf = (seq == null) ? null : fileMapp.get(seq);

				if (mf != null && !mf.isEmpty()) {
					byte[] blob = mf.getBytes();
					String orgName = safeFileName(mf.getOriginalFilename(), 50);
					String mime = (mf.getContentType() == null) ? "" : mf.getContentType();
					dtl.put("lgistPartImgNm", orgName);
					dtl.put("lgistPartImgMime", mime);
					@SuppressWarnings("unchecked")
					Map raw = (Map) dtl;
					raw.put("lgistPartImg", blob);
				} else {
					dtl.put("lgistPartImgNm", "");
					dtl.put("lgistPartImgMime", "");
					@SuppressWarnings("unchecked")
					Map raw = (Map) dtl;
					raw.put("lgistPartImg", null);
				}

				String updCheck = dtl.get("updCheck");
				if ("D".equals(updCheck)) {
					cr10Mapper.deleteLgistPartImageDetail(dtl);
				} else {
					cr10Mapper.mergeLgistPartImageDetail(dtl);
				}
			}
		}

		//---------------------------------------------------------------
		//첨? ?일 처리 ?작  (처음 ?록?에???일 ???게 ?음)
		//---------------------------------------------------------------
		if (uploadFileList.size() > 0) {
		    paramMap.put("fileTrgtTyp", paramMap.get("pgmId"));
//		    paramMap.put("fileTrgtKey", lgistNo);
		    cm08Svc.uploadFile(paramMap, mRequest);
		}
		//---------------------------------------------------------------
		//첨? ?일 처리  ??
		//---------------------------------------------------------------

		//---------------------------------------------------------------
		//결재 처리 ?작
		//---------------------------------------------------------------
//	    List<Map<String, String>> reqList = cr10Mapper.selectTodoAppReqList(paramMap);
//	    List<Map<String, String>> toDoAppList = gsonDtl.fromJson(reqList.toString(), dtlMap);
//	    /*
//	    DecimalFormat df = new DecimalFormat("00000");
//	    int numKey = Integer.valueOf(paramMap.get("fileTrgtKey"));
//	    */
//	    String todoDiv2CodeId = paramMap.get("lgistNo");
//	    int sanctnSn = cr10Mapper.selectTodoAppSanctnSn(paramMap);
//	    for (Map<String, String> dtl : toDoAppList) {
//	    	dtl.put("sanctnSn", String.valueOf(sanctnSn));
//	    	dtl.put("userId", paramMap.get("userId"));
//	    	dtl.put("pgmId", paramMap.get("pgmId"));
//	    	dtl.put("pgPath", paramMap.get("pgPath"));
//	    	dtl.put("pgParam", paramMap.get("pgParam"));
//	    	dtl.put("todoDiv2CodeId", todoDiv2CodeId);
//	    	cr10Mapper.insertTodoAppList(dtl);
//	    	sanctnSn++;
//	    }
//
//	    paramMap.put("todoDiv1CodeId", paramMap.get("appDiv"));
//	    paramMap.put("todoDiv2CodeId", todoDiv2CodeId);
//	    cr10Mapper.updateLgistMastTodoApp(paramMap);
		//---------------------------------------------------------------
		//결재 처리  ??
		//---------------------------------------------------------------

		//??결재 처리
		Gson gson = new Gson();		
		String pgParam = "{\"fileTrgtKey\":\""+ String.valueOf(paramMap.get("fileTrgtKey")) +"\"}";
		String todoTitle1 = lgistNo + " 물류진행?청 공유"; 
		
		Type stringList2 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowSharngListArr"), stringList2);
		if (sharngArr != null && sharngArr.size() > 0 ) {
			int i = 0;
	        for (Map<String, String> sharngMap : sharngArr) {
	            try {	 
	            	    sharngMap.put("reqNo", paramMap.get("lgistNo"));
	            	    sharngMap.put("fileTrgtKey", String.valueOf(paramMap.get("fileTrgtKey")));
	            	    sharngMap.put("pgmId", paramMap.get("pgmId"));
	            	    sharngMap.put("userId", paramMap.get("userId"));
	            	    sharngMap.put("sanCtnSn",Integer.toString(i+1));
	            	    sharngMap.put("pgParam", pgParam);
	            	    sharngMap.put("todoTitle", todoTitle1);
	                	QM01Mapper.insertWbsSharngList(sharngMap);       		
	            	i++;
	            } catch (Exception e) {
	                System.out.println("error2"+e.getMessage());
	            }
	        }
		}

		
		String todoTitle2 = lgistNo + " 물류진행?청 결재"; 
		
		//결재
		Type stringList3 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> approvalArr = gson.fromJson(paramMap.get("rowApprovalListArr"), stringList3);
		if (approvalArr != null && approvalArr.size() > 0 ) {
			int i = 0;
	        for (Map<String, String> approvalMap : approvalArr) {
	            try {	 
		            	approvalMap.put("reqNo", paramMap.get("lgistNo"));
		            	approvalMap.put("fileTrgtKey",String.valueOf(paramMap.get("fileTrgtKey")));
		            	approvalMap.put("pgmId", paramMap.get("pgmId"));
		            	approvalMap.put("userId", paramMap.get("userId"));
		            	approvalMap.put("sanCtnSn",Integer.toString(i+1));
		            	approvalMap.put("pgParam", pgParam);
		            	approvalMap.put("todoTitle", todoTitle2);
	                	QM01Mapper.insertWbsApprovalList(approvalMap);       		
	                	i++;
	            } catch (Exception e) {
	                System.out.println("error2"+e.getMessage());
	            }
	        }
		}
		//??결재 ??
	    return rtnMap;
  }
  private void assertApprovalEditable(Map<String, String> paramMap) throws Exception {
    int lockCnt = cr10Mapper.selectApprovalLockCntByFileTrgtKey(paramMap);
    if (lockCnt > 0) {
      throw new Exception("  Ǵ Ϸ    ϴ.");
    }
  }
  @Override
  public int updateLgistMast(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
	assertApprovalEditable(paramMap);
//	Gson gson = new Gson();
	Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
	Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>(){}.getType();

	//---------------------------------------------------------------
	//첨? ?일 처리 권한체크 ?작 -->?일 ?로?? ?? 권한 ?으?Exception 처리 ??
  	//   ?수?:  jobType, userId, comonCd
	//---------------------------------------------------------------
    HashMap<String, String> param = new HashMap<>();
    param.put("userId", paramMap.get("userId"));
	param.put("coCd", paramMap.get("coCd"));
    param.put("comonCd", paramMap.get("comonCd"));  //?로?엔?에 ?어???일 ????치 ?보

	List<Map<String, String>> uploadFileList = gsonDtl.fromJson(paramMap.get("uploadFileArr"), dtlMap);
	if (uploadFileList.size() > 0) {
			//?근 권한 ?으?Exception 발생 (jobType, userId, comonCd 3??수??요)
			param.put("jobType", "fileUp");
			cm15Svc.selectFileAuthCheck(param);
	}
	String[] deleteFileArr = gsonDtl.fromJson(paramMap.get("deleteFileArr"), String[].class);
	List<String> deleteFileList = Arrays.asList(deleteFileArr);
    for(String fileKey : deleteFileList) {  // ?????일 ?나???? ?요(?체 목록?서 ?? ?택???요??
		    Map<String, String> fileInfo = cm08Svc.selectFileInfo(fileKey);
			//?근 권한 ?으?Exception 발생
		    param.put("comonCd", fileInfo.get("comonCd"));  //?????일??보???????치 ?보
		    param.put("jobType", "fileDelete");
			cm15Svc.selectFileAuthCheck(param);
	}
	//---------------------------------------------------------------
	//첨? ?일 권한체크  ??
	//---------------------------------------------------------------

	int result = cr10Mapper.deleteLgistDetailAll(paramMap);
		result = cr10Mapper.updateLgistMast(paramMap);


    List<Map<String, String>> salesCdList = gsonDtl.fromJson(paramMap.get("salesCdArr"), dtlMap);
    for (Map<String, String> dtl : salesCdList) {
    	dtl.put("userId", paramMap.get("userId"));
    	dtl.put("pgmId", paramMap.get("pgmId"));
    	

		paramMap.put("salesCd", dtl.get("salesCd"));
		
    	//      반복문에?는 ??dtl)??"userId"? "pgmId"?추?
    	String dtaChk = dtl.get("dtaChk").toString();
    	/* "dtaChk" 값을 ?인?여
    	 * "I"??경우 cr10Mapper.insertLgistDetail(dtl)???출?여 ?로?트 ???보??입?고,
    	 * "U"??경우 cr10Mapper.updateLgistDetail(dtl)???출?여 ?로?트 ???보??데?트?고,
    	 * "D"??경우 * cr10Mapper.deleteLgistDetail(dtl)???출?여 ?로?트 ???보???.		 */
    	if ("I".equals(dtaChk)) {
    		//dtl.put("prjctSeq", paramMap.get("prjctSeq"));
    		cr10Mapper.insertLgistDetail(dtl);
    	} else if ("U".equals(dtaChk)) {
    		cr10Mapper.updateLgistDetail(dtl);
    	} else if ("D".equals(dtaChk)) {
    		cr10Mapper.deleteLgistDetail(dtl);
    	}
    }


    List<Map<String, String>> tripRptS = gsonDtl.fromJson(paramMap.get("tripRptS"), dtlMap);

	// 1) ?로???일/seq ?신
	List<MultipartFile> files = mRequest.getFiles("lgistFiles");
	String[] seqArr = mRequest.getParameterValues("lgistFilesSeq");

	// 2) seq -> MultipartFile 매핑(Map)
	Map<String, MultipartFile> fileMap = new HashMap<>();
	if (files != null && seqArr != null) {
	    int n = Math.min(files.size(), seqArr.length);
	    for (int i = 0; i < n; i++) {
	        String seq = (seqArr[i] == null) ? "" : seqArr[i].trim();
	        MultipartFile mf = files.get(i);
	        if (seq.isEmpty() || mf == null || mf.isEmpty()) continue;
	        fileMap.put(seq, mf); // key = LGIST_NO_SEQ
	    }
	}
	
    for (Map<String, String> dtl : tripRptS) {
    	dtl.put("userId", paramMap.get("userId"));
    	dtl.put("pgmId", paramMap.get("pgmId"));


        // dtl?서 매핑??seq) 추출: ?론?에???? ?명??맞추?요
        // 보통 lgistNoSeq ?는 workRptNoSeq ??나??어?니??
        String seq = dtl.get("lgistNoSeq");
        if (seq == null || (seq == null || seq.trim().isEmpty())) {
            seq = dtl.get("workRptNoSeq"); // fallback
        }

        MultipartFile mf = (seq == null) ? null : fileMap.get(seq);

        if (mf != null && !mf.isEmpty()) {
            // BLOB ??용 byte[]
            byte[] blob = mf.getBytes();

            // ?일?컬럼 50???한 고려)
            String orgName = safeFileName(mf.getOriginalFilename(), 50);
            String mime = (mf.getContentType() == null) ? "" : mf.getContentType();

            // MyBatis ?라미터??길 ??팅
            // mapper XML?서 #{lgistItemImg, jdbcType=BLOB} ?받도?
            dtl.put("lgistItemImgNm", orgName);
            dtl.put("lgistItemImgMime", mime);

            // Map<String,String>?는 byte[]?put?????습?다.
            // ?결: (A) Map<String,Object>?바꾸거나, (B) 별도 ?라미터 객체??야 ?니??
            // ?래??최소 ?정?로 가?한 방식: Map??Object?캐스?해??byte[]??는 ?회
            @SuppressWarnings("unchecked")
            Map raw = (Map) dtl;
            raw.put("lgistItemImg", blob);
        } else {
            // ?일???으???지 컬럼? ?? ?거??null 처리
            // update??경우 기존 BLOB ???려?XML?서 <if test="lgistItemImg != null"> 처리 ?요
            dtl.put("lgistItemImgNm", "");
            dtl.put("lgistItemImgMime", "");
            @SuppressWarnings("unchecked")
            Map raw = (Map) dtl;
            raw.put("lgistItemImg", null);
        }

    	String updCheck = dtl.get("updCheck").toString();
    	/* "updCheck" 값을 ?인?여 */
    	if ("D".equals(updCheck)) {
    		cr10Mapper.deleteLgistItemImageDetail(dtl);
    	} else {
    		cr10Mapper.mergeLgistItemImageDetail(dtl);
    	}
    }
    
    List<Map<String, String>> partListS = null;
    if(paramMap.get("partListS") != null && paramMap.get("partListS").length() > 0 && !"\"\"".equals(paramMap.get("partListS")) ) {
	    partListS = gsonDtl.fromJson(paramMap.get("partListS"), dtlMap);
		List<MultipartFile> filesp = mRequest.getFiles("partFiles");
		String[] seqArrp = mRequest.getParameterValues("partFilesSeq");
		Map<String, MultipartFile> fileMapp = new HashMap<>();
		if (filesp != null && seqArrp != null) {
		    int n = Math.min(filesp.size(), seqArrp.length);
		    for (int i = 0; i < n; i++) {
		        String seq = (seqArrp[i] == null) ? "" : seqArrp[i].trim();
		        MultipartFile mf = filesp.get(i);
		        if (seq.isEmpty() || mf == null || mf.isEmpty()) continue;
		        fileMapp.put(seq, mf); 
		    }
		}
		for (Map<String, String> dtl : partListS) {
	    	dtl.put("userId", paramMap.get("userId"));
	    	dtl.put("pgmId", paramMap.get("pgmId"));
	        String seq = dtl.get("lgistPartNoSeq");
	        if (seq == null || (seq == null || seq.trim().isEmpty())) {
	            seq = dtl.get("workRptNoSeq"); 
	        }
	        MultipartFile mf = (seq == null) ? null : fileMapp.get(seq);
	        if (mf != null && !mf.isEmpty()) {
	            byte[] blob = mf.getBytes();
	            String orgName = safeFileName(mf.getOriginalFilename(), 50);
	            String mime = (mf.getContentType() == null) ? "" : mf.getContentType();
	            dtl.put("lgistPartImgNm", orgName);
	            dtl.put("lgistPartImgMime", mime);
	            @SuppressWarnings("unchecked")
	            Map raw = (Map) dtl;
	            raw.put("lgistPartImg", blob);
	        } else {
	            dtl.put("lgistPartImgNm", "");
	            dtl.put("lgistPartImgMime", "");
	            @SuppressWarnings("unchecked")
	            Map raw = (Map) dtl;
	            raw.put("lgistPartImg", null);
	        }
	    	String updCheck = dtl.get("updCheck").toString();
	    	if ("D".equals(updCheck)) {
	    		cr10Mapper.deleteLgistPartImageDetail(dtl);
	    	} else {
	    		cr10Mapper.mergeLgistPartImageDetail(dtl);
	    	}
	    }
    }

    
    
	//---------------------------------------------------------------
	//첨? ?일 처리 ?작
	//---------------------------------------------------------------
    if (uploadFileList.size() > 0) {
	    paramMap.put("fileTrgtTyp", paramMap.get("pgmId"));
//	    paramMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
	    cm08Svc.uploadFile(paramMap, mRequest);
    }

    for(String fileKey : deleteFileList) {
    	cm08Svc.deleteFile(fileKey);
    }
	//---------------------------------------------------------------
	//첨? ?일 처리  ??
	//---------------------------------------------------------------
    //---------------------------------------------------------------
    //결재 처리 ?작 -?정?에??처리 ???역???다?결재 처리?다.
    //---------------------------------------------------------------
//    int todoAppCount = cr10Mapper.selectTodoAppCount(paramMap);
//    if(todoAppCount == 0) {
//	    List<Map<String, String>> reqList = cr10Mapper.selectTodoAppReqList(paramMap);
//	    List<Map<String, String>> toDoAppList = gsonDtl.fromJson(reqList.toString(), dtlMap);
//	    String todoDiv2CodeId = paramMap.get("lgistNo");
//	    int sanctnSn = cr10Mapper.selectTodoAppSanctnSn(paramMap);
//	    for (Map<String, String> dtl : toDoAppList) {
//	    	dtl.put("sanctnSn", String.valueOf(sanctnSn));
//	    	dtl.put("userId", paramMap.get("userId"));
//	    	dtl.put("pgmId", paramMap.get("pgmId"));
//	    	dtl.put("pgPath", paramMap.get("pgPath"));
//	    	dtl.put("pgParam", paramMap.get("pgParam"));
//	    	dtl.put("todoDiv2CodeId", todoDiv2CodeId);
//	    	cr10Mapper.insertTodoAppList(dtl);
//	    	sanctnSn++;
//	    }
//
//	    paramMap.put("todoDiv1CodeId", paramMap.get("appDiv"));
//	    paramMap.put("todoDiv2CodeId", todoDiv2CodeId);
//	    cr10Mapper.updateLgistMastTodoApp(paramMap);
//    }
    //---------------------------------------------------------------
    //결재 처리  ??
    //---------------------------------------------------------------

	//??결재 처리
    
    //?단 기존 리스????
	QM01Mapper.deleteApprovalList(paramMap); 
	
	Gson gson = new Gson();		
	String pgParam = "{\"fileTrgtKey\":\""+ String.valueOf(paramMap.get("fileTrgtKey")) +"\"}";
	String todoTitle1 = paramMap.get("lgistNo") + " 물류진행?청 공유"; 
	
	Type stringList2 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
	List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowSharngListArr"), stringList2);
	if (sharngArr != null && sharngArr.size() > 0 ) {
		int i = 0;
        for (Map<String, String> sharngMap : sharngArr) {
            try {	 
            	    sharngMap.put("reqNo", paramMap.get("lgistNo"));
            	    sharngMap.put("fileTrgtKey", String.valueOf(paramMap.get("fileTrgtKey")));
            	    sharngMap.put("pgmId", paramMap.get("pgmId"));
            	    sharngMap.put("userId", paramMap.get("userId"));
            	    sharngMap.put("sanCtnSn",Integer.toString(i+1));
            	    sharngMap.put("pgParam", pgParam);
            	    sharngMap.put("todoTitle", todoTitle1);
                	QM01Mapper.insertWbsSharngList(sharngMap);       		
            	i++;
            } catch (Exception e) {
                System.out.println("error2"+e.getMessage());
            }
        }
	}

	
	String todoTitle2 = paramMap.get("lgistNo") + " 물류진행?청 결재"; 
	
	//결재
	Type stringList3 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
	List<Map<String, String>> approvalArr = gson.fromJson(paramMap.get("rowApprovalListArr"), stringList3);
	if (approvalArr != null && approvalArr.size() > 0 ) {
		int i = 0;
        for (Map<String, String> approvalMap : approvalArr) {
            try {	 
	            	approvalMap.put("reqNo", paramMap.get("lgistNo"));
	            	approvalMap.put("fileTrgtKey", String.valueOf(paramMap.get("fileTrgtKey")));
	            	approvalMap.put("pgmId", paramMap.get("pgmId"));
	            	approvalMap.put("userId", paramMap.get("userId"));
	            	approvalMap.put("sanCtnSn",Integer.toString(i+1));
	            	approvalMap.put("pgParam", pgParam);
	            	approvalMap.put("todoTitle", todoTitle2);
                	QM01Mapper.insertWbsApprovalList(approvalMap);       		
                	i++;
            } catch (Exception e) {
                System.out.println("error2"+e.getMessage());
            }
        }
	}
	//??결재 ??
	
    return result;
  }
  
  private static String safeFileName(String name, int maxLen) {
	    if (name == null) return "";
	    name = name.replace("\\", "/");
	    if (name.contains("/")) name = name.substring(name.lastIndexOf('/') + 1);
	    return (name.length() > maxLen) ? name.substring(name.length() - maxLen) : name;
	}

  
  
  @Override
  public int updateLgistlistImage(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws IOException {
	Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
	Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>(){}.getType();


    List<Map<String, String>> tripRptS = gsonDtl.fromJson(paramMap.get("tripRptS"), dtlMap);

	// 1) ?로???일/seq ?신
	List<MultipartFile> files = mRequest.getFiles("lgistFiles");
	String[] seqArr = mRequest.getParameterValues("lgistFilesSeq");

	// 2) seq -> MultipartFile 매핑(Map)
	Map<String, MultipartFile> fileMap = new HashMap<>();
	if (files != null && seqArr != null) {
	    int n = Math.min(files.size(), seqArr.length);
	    for (int i = 0; i < n; i++) {
	        String seq = (seqArr[i] == null) ? "" : seqArr[i].trim();
	        MultipartFile mf = files.get(i);
	        if (seq.isEmpty() || mf == null || mf.isEmpty()) continue;
	        fileMap.put(seq, mf); // key = LGIST_NO_SEQ
	    }
	}
	


    // ?료가 ?나???으?PASS
    if ((tripRptS == null || tripRptS.isEmpty()) && fileMap.isEmpty()) {
        return 9999; // 처리????음, 9999 ?론?엔?에??처리?야??
    }
    
	
    int result = 0;
    for (Map<String, String> dtl : tripRptS) {
    	dtl.put("userId", paramMap.get("userId"));
    	dtl.put("pgmId", paramMap.get("pgmId"));


        // dtl?서 매핑??seq) 추출: ?론?에???? ?명??맞추?요
        // 보통 lgistNoSeq ?는 workRptNoSeq ??나??어?니??
        String seq = dtl.get("lgistNoSeq");
        if (seq == null || (seq == null || seq.trim().isEmpty())) {
            seq = dtl.get("workRptNoSeq"); // fallback
        }

        MultipartFile mf = (seq == null) ? null : fileMap.get(seq);

        if (mf != null && !mf.isEmpty()) {
            // BLOB ??용 byte[]
            byte[] blob = mf.getBytes();

            // ?일?컬럼 50???한 고려)
            String orgName = safeFileName(mf.getOriginalFilename(), 50);
            String mime = (mf.getContentType() == null) ? "" : mf.getContentType();

            // MyBatis ?라미터??길 ??팅
            // mapper XML?서 #{lgistItemImg, jdbcType=BLOB} ?받도?
            dtl.put("lgistItemImgNm", orgName);
            dtl.put("lgistItemImgMime", mime);

            // Map<String,String>?는 byte[]?put?????습?다.
            // ?결: (A) Map<String,Object>?바꾸거나, (B) 별도 ?라미터 객체??야 ?니??
            // ?래??최소 ?정?로 가?한 방식: Map??Object?캐스?해??byte[]??는 ?회
            @SuppressWarnings("unchecked")
            Map raw = (Map) dtl;
            raw.put("lgistItemImg", blob);
        } else {
            // ?일???으???지 컬럼? ?? ?거??null 처리
            // update??경우 기존 BLOB ???려?XML?서 <if test="lgistItemImg != null"> 처리 ?요
            dtl.put("lgistItemImgNm", "");
            dtl.put("lgistItemImgMime", "");
            @SuppressWarnings("unchecked")
            Map raw = (Map) dtl;
            raw.put("lgistItemImg", null);
        }
        
    	String updCheck = dtl.get("updCheck").toString();
    	/* "updCheck" 값을 ?인?여 */
    	if ("D".equals(updCheck)) {
    		result = cr10Mapper.deleteLgistItemImageDetail(dtl);
    	} else {
    		result = cr10Mapper.mergeLgistItemImageDetail(dtl);
    	}
    }

	
    return result;
  }
  
  
  @Override
  public int deleteLgistMast(Map<String, String> paramMap) throws Exception {
      assertApprovalEditable(paramMap);
	    //---------------------------------------------------------------
		//첨? ?일 권한체크  ?작 -->?? 권한 ?으?Exception, 관???일 ?체 체크
	  	//   ?수?:  jobType, userId, comonCd
		//---------------------------------------------------------------
	    List<Map<String, String>> deleteFileList = cm08Svc.selectFileListAll(paramMap);
	    HashMap<String, String> param = new HashMap<>();
	    param.put("jobType", "fileDelete");
		param.put("coCd", paramMap.get("coCd"));
	    param.put("userId", paramMap.get("userId"));
	    if (deleteFileList.size() > 0) {
		    for (Map<String, String> dtl : deleteFileList) {
					//?근 권한 ?으?Exception 발생
		            param.put("comonCd",  dtl.get("comonCd"));

					cm15Svc.selectFileAuthCheck(param);
			}
	    }
		//---------------------------------------------------------------
		//첨? ?일 권한체크 ??
		//---------------------------------------------------------------

	  int result = cr10Mapper.deleteLgistDetailAll(paramMap);
	  result += cr10Mapper.deleteLgistMast(paramMap);
	  result += cr10Mapper.deleteTodoDetail(paramMap);

		//---------------------------------------------------------------
		//첨? ?일 처리 ?작  (처음 ?록?에???일 ???게 ?음)
		//---------------------------------------------------------------
		if (deleteFileList.size() > 0) {
		    for (Map<String, String> deleteDtl : deleteFileList) {
		    	String fileKey = deleteDtl.get("fileKey").toString();
			    cm08Svc.deleteFile( fileKey );
		    }
		}
		//---------------------------------------------------------------
		//첨? ?일 처리  ??
		//---------------------------------------------------------------
	    return result;
  }

  @Override
  public int updateTodoAppConfirm(Map<String, String> paramMap) throws Exception {
	  return cr10Mapper.updateTodoAppConfirm(paramMap);
  }

  @Override
  public List<Map<String, String>> selectLgistPartList(Map<String, String> paramMap) {
	  List<Map<String, String>> list = cr10Mapper.selectLgistPartList(paramMap);
	  String lgistNo = paramMap.get("lgistNo");
	  for (Map<String, String> row : list) {
	      String seq = row.get("lgistPartNoSeq");
	      String hasImg = row.get("hasImg");
	      if ("Y".equalsIgnoreCase(hasImg)) {
	          row.put("imgUrl", "/user/cr/cr10/partListImage?lgistNo=" + lgistNo + "&lgistPartNoSeq=" + seq);
	      } else {
	          row.put("imgUrl", "");
	      }
	  }
	  return list;
  }

  @Override
  public int updatePartListImage(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws IOException {
	Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
	Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>(){}.getType();

    List<Map<String, String>> partListS = gsonDtl.fromJson(paramMap.get("partListS"), dtlMap);

	List<MultipartFile> files = mRequest.getFiles("partFiles");
	String[] seqArr = mRequest.getParameterValues("partFilesSeq");

	Map<String, MultipartFile> fileMap = new HashMap<>();
	if (files != null && seqArr != null) {
	    int n = Math.min(files.size(), seqArr.length);
	    for (int i = 0; i < n; i++) {
	        String seq = (seqArr[i] == null) ? "" : seqArr[i].trim();
	        MultipartFile mf = files.get(i);
	        if (seq.isEmpty() || mf == null || mf.isEmpty()) continue;
	        fileMap.put(seq, mf); 
	    }
	}

    if ((partListS == null || partListS.isEmpty()) && fileMap.isEmpty()) {
        return 9999; 
    }
    
    int result = 0;
    for (Map<String, String> dtl : partListS) {
    	dtl.put("userId", paramMap.get("userId"));
    	dtl.put("pgmId", paramMap.get("pgmId"));

        String seq = dtl.get("lgistPartNoSeq");
        if (seq == null || (seq == null || seq.trim().isEmpty())) {
            seq = dtl.get("workRptNoSeq"); // fallback
        }

        MultipartFile mf = (seq == null) ? null : fileMap.get(seq);

        if (mf != null && !mf.isEmpty()) {
            byte[] blob = mf.getBytes();
            String orgName = safeFileName(mf.getOriginalFilename(), 50);
            String mime = (mf.getContentType() == null) ? "" : mf.getContentType();

            dtl.put("lgistPartImgNm", orgName);
            dtl.put("lgistPartImgMime", mime);

            @SuppressWarnings("unchecked")
            Map raw = (Map) dtl;
            raw.put("lgistPartImg", blob);
        } else {
            dtl.put("lgistPartImgNm", "");
            dtl.put("lgistPartImgMime", "");
            @SuppressWarnings("unchecked")
            Map raw = (Map) dtl;
            raw.put("lgistPartImg", null);
        }
        
    	String updCheck = dtl.get("updCheck").toString();
    	if ("D".equals(updCheck)) {
    		result = cr10Mapper.deleteLgistPartImageDetail(dtl);
    	} else {
    		result = cr10Mapper.mergeLgistPartImageDetail(dtl);
    	}
    }
	
    return result;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public int updateLgistCompl(List<Map<String, String>> paramList) throws Exception {
    int result = 0;
    for (Map<String, String> param : paramList) {
        assertApprovalEditable(param);
        result += cr10Mapper.updateLgistCompl(param);
    }
    return result;
  }

}



