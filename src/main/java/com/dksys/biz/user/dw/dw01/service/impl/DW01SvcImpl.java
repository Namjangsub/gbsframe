package com.dksys.biz.user.dw.dw01.service.impl;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dksys.biz.user.dw.dw01.mapper.DW01Mapper;
import com.dksys.biz.user.dw.dw01.model.AuditIngestReq;
import com.dksys.biz.user.dw.dw01.service.DW01Svc;
import com.dksys.biz.user.dw.dw01.util.DwgNameParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import reactor.util.annotation.Nullable;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class DW01SvcImpl implements DW01Svc {

    private final DW01Mapper dw01Mapper;

    private final ObjectMapper objectMapper;
    
    @Override
	public void insertHistory(AuditIngestReq req) {
        String auditLog;

        try {
            auditLog = objectMapper.writeValueAsString(req);    // JSON 직렬화
        } catch (JsonProcessingException e) {
//            throw new IllegalStateException("AuditIngestReq JSON 직렬화 실패", e);
            auditLog =  "AuditIngestReq JSON 직렬화 실패";
        }

        // 2) 파일 경로: 기본 fullPath, RENAME인 경우 newPath 우선 지정
        String fullPath = Optional.ofNullable(req.getFsEvent())
                .map(f -> f.getFullPath())
                .orElse(null);
        String recordId = Optional.ofNullable(req.getActorProcess())
                .map(a -> a.getEventNo())
                .orElse(null);
        Map<String, Object> logParam = new HashMap<>();
        logParam.put("action",   Optional.ofNullable(req.getFsEvent())
                .map(f -> f.getType())
                .orElse(null));
        logParam.put("salesCd",  null);
        logParam.put("recordId",   recordId);
        logParam.put("fullPath",   fullPath);
        
        logParam.put("auditLog", auditLog);
        dw01Mapper.insertHistory(logParam);   // 감사기록
    }
    
    
    @Override
    @Transactional
    public void ingestAudit(AuditIngestReq req) {
        // 0) 입력 방어 및 필수값 체크
        if (req == null || req.getFile() == null) {
            throw new IllegalArgumentException("file is required");
        }

        // 1) fullPath 결정: 기본은 file.fullPath, RENAME이면 newPath 허용
        String reqActionRaw = Optional.ofNullable(req.getFsEvent())
                .map(f -> f.getType())
                .map(Object::toString)
                .orElse("WRITE")
                .trim();
        String reqAction = reqActionRaw.toUpperCase(Locale.ROOT);

        String fullPath = Optional.ofNullable(req.getFsEvent())
                .map(f -> f.getFullPath())
                .orElse(null);
        String oldPath  = Optional.ofNullable(req.getFsEvent())
                .map(f -> f.getOldFullPath())
                .orElse(null);

        String normFullPath = normalizePath(fullPath);  // D:\DWG_AUDIT\23127-01NVFDT-3000-001R1.dwg
        String normOldPath = normalizePath(oldPath);    // D:\DWG_AUDIT\23127-01NVFDT-3000-001.dwg
        String normChkPath  =  ("RENAMED".equals(reqAction)) ? normOldPath : normFullPath; // RENAMED는 oldPath, 나머지는 fullPath
        boolean noPath = isEmpty(fullPath);
        
        // 파일명/확장자 후보 (경로 없어도 최대한 추정) - fullPath 우선, oldPath 차선
        String candidateForName = !isEmpty(fullPath) ? fullPath : oldPath;

        // 파일명/확장자 추출 시도 - 실패 시 UNKNOWN/N/A 처리
        String fileNameExtMin;
        try {
            Path pForName = Paths.get(candidateForName);
            fileNameExtMin = pForName.getFileName() != null ? pForName.getFileName().toString() : "UNKNOWN";
        } catch (Exception ignore) {
            fileNameExtMin = isEmpty(candidateForName) ? "UNKNOWN" : candidateForName;
        }
        String extMin      = Optional.ofNullable(FilenameUtils.getExtension(fileNameExtMin)).orElse("");
        String fileNameMin = isEmpty(extMin) ? fileNameExtMin : FilenameUtils.getBaseName(fileNameExtMin);
        String salesCdFromName = DwgNameParser.parseFromPath(fileNameExtMin)
                .map(r -> r.salesCd)
                .orElse(null);
        
        // 중복 선언 금지: checksum은 여기 한 번만 선언
        String checksum = Optional.ofNullable(req.getFileObj())
                .map(f -> f.getSha256())
                .orElse(null);
        String ip = Optional.ofNullable(req.getHost())
                .map(h -> h.getMachine())
                .orElse(null);
        String evtId = Optional.ofNullable(req.getActorProcess())
                .map(a -> a.getEventId())
                .orElse(null);
    
        // 앱 이름 - 공백/빈값이면 PS-AuditCollector로 대체 지정 
        String appName = Optional.ofNullable(req.getActorProcess())
                .map(a -> a.getProcessName())
                .filter(s -> s != null && !s.trim().isEmpty())  // JDK 8에서는 trim() + isEmpty()
                .orElse("PS-AuditCollector");
        String sourceEvtType =  Optional.ofNullable(req.getFsEvent())
                .map(f -> f.getType())
                .map(Object::toString)
                .orElse(null);
        String accessMask   = Optional.ofNullable(req.getActorProcess())
                .map(a -> a.getAccessMask())
                .map(Object::toString)
                .orElse(null);
        String recordId =  Optional.ofNullable(req.getActorProcess())
                .map(a -> a.getEventNo())
                .orElse(null);
        recordId = (recordId != null) ? recordId :  "0";
        
        String rawMessage     = String.valueOf(req.getRawMessage());
                

        
        // 2) 경로가 끝내 없으면 AUDIT만 남기고 종료 (문서/버전 생성 금지) → return
        if (noPath) {
            Map<String, Object> insA = new HashMap<>();
            insA.put("docId", null);
            insA.put("verId", null);
            insA.put("action", ( "DELETED".equals(reqAction) ? "DELETED" :
                                "RENAMED".equals(reqAction) ? "RENAMED" :
                                "CREATED".equals(reqAction) ? "CREATED" : "TOUCH") + "/NOPATH");
            insA.put("salesCd", (salesCdFromName != null) ? salesCdFromName : "SALESCD_NOT");
            insA.put("docNo", recordId);

            insA.put("fileName", "UNKNOWN".equals(fileNameExtMin) ? "N/A" : fileNameMin);
            insA.put("ext",      extMin);
            insA.put("fileSize", req.getFile().getSize());
            insA.put("checksum", checksum);
            insA.put("clientIp", ip);
            insA.put("userId",   deriveUserId(req));
            insA.put("userName", deriveUserName(req));
            insA.put("appName",  appName);
            insA.put("rawMessage", rawMessage);
//             insA.put("rawXml",     req.getRawXml());
            if (!isEmpty(oldPath)) insA.put("oldPath", oldPath);
            if (!isEmpty(fullPath)) insA.put("newPath", fullPath);

            insA.put("recordId", recordId);
            insA.put("sourceEvtType", sourceEvtType);
            insA.put("sourceEvtId", evtId);

            dw01Mapper.insertAudit(insA);
            return;
        }
        
     	// 3) 경로가 있는 정상 흐름
        Path p = Paths.get(fullPath);					 // 예: D:\DWG_AUDIT\23127-01NVFDT-3000-001.dwg
        String fileNameExt = p.getFileName().toString(); // 예: 23127-01NVFDT-1300-105-6.dwg
        String ext = FilenameUtils.getExtension(fileNameExt).toLowerCase(Locale.ROOT);	// dwg
        String fileName = FilenameUtils.getBaseName(fileNameExt);						// 23127-01NVFDT-3000-001

        // 2) salesCd / docNo 추론 (JSON → relPath → 상위폴더)
        String salesCd = null;   // 파일명에서만 설정
        // 격리 플래그
        boolean quarantined = false;

        // 2-1) 파일명 파싱(PART/UNIT/REV) - 확장자 포함 이름으로 호출
        String partNo = null, unitNo = null, revNo = null;
        Optional<DwgNameParser.Result> parsed = DwgNameParser.parseFromPath(fileNameExt);
        if (parsed.isPresent()) {
            DwgNameParser.Result r = parsed.get();
            salesCd = r.salesCd;  // salesCd는 파일명에서만! 조건 없이 대입
            partNo = r.partNo;
            unitNo = r.unitNo;
            revNo  = r.revNo; // null 가능
        }

        // 2-2) salesCd가 없으면 경로에서 추출 시도 (상위 폴더명) - salesCd는 파일명 우선 적용
        if (isEmpty(salesCd)) { 
            // fileNameExt의 앞에서 13byte 짤라서 저장 
            if (fileNameExt.length() >= 13) {
                salesCd = fileNameExt.substring(0, 13);
            } else {
                salesCd = fileNameExt; // 13byte 미만이면 전체
            }
            // 그래도 없으면 SALESCD_NOT 지정 - 반드시 있어야 하므로
            // 파일명/경로 모두에서 추출 실패 시:
            if (isEmpty(salesCd)) {salesCd = "SALESCD_NOT"; quarantined = true; }
        }

        // 3) DOC 헤더 조회/생성
        // 1) 경로로 버전 우선 조회
        Map<String, Object> docParam = new HashMap<>();
        docParam.put("path", normChkPath);
        Map<String, Object> verByPath = dw01Mapper.findVersionByPath(docParam);
//        if (verByPath == null && "RENAMED".equals(reqAction) && !isEmpty(oldPath)) {
//            Map<String, Object> oldDocParam = new HashMap<>();
//            normFullPath = normalizePath(oldPath);
//            oldDocParam.put("path", normFullPath);
//            verByPath = dw01Mapper.findVersionByPath(oldDocParam);
//        }

        Long docId = null, verId = null;
        Integer lastNo = null;
        String lastChecksum = null, docSt = null;
        // verByPath가 null이면 신규문서/버전 분기
        // verByPath가 null이 아니면 docId/verId/lastNo/lastChecksum/docSt 획득
        if (verByPath != null) {
            verId        = ((Number) verByPath.get("VER_ID")).longValue();
            lastNo       = ((Number) verByPath.get("VER_NO")).intValue();
            docId        = ((Number) verByPath.get("DOC_ID")).longValue();
            lastChecksum = (String) verByPath.get("CHECKSUM_SHA256");
            docSt        = (String) verByPath.get("DOC_ST");
        }
        
        String userIdDerived   = deriveUserId(req);
        String userNameDerived = deriveUserName(req);
        

        // 5) 액션 최종 판정 (요청액션 우선, 체크섬 보조)
        String resolvedAction;
        switch (reqAction) {
        case "DELETED": resolvedAction = "DELETED"; break;
        case "RENAMED": resolvedAction = "RENAMED"; break;
        case "RESTORE":
        case "CREATED": resolvedAction = "CREATED"; break;
        case "CHANGED": resolvedAction = (checksum != null && !Objects.equals(lastChecksum, checksum)) ? "MODIFY" : "TOUCH"; break;
        case "WRITED":
        default:
            resolvedAction = (lastChecksum == null) ? "CREATED" : (checksum == null ? "TOUCH" : (Objects.equals(lastChecksum, checksum) ? "TOUCH" : "MODIFY"));
        }
/***********************************************************************************************************************************************
        액션			  FILE_NAME			EXT			NEW_PATH				OLD_PATH			CHECKSUM		비고
**********************************************************************************************************************************************
        CREATE				base			ext			normFullPath			NULL				sha256			VER 생성
        MODIFY				base			ext			normFullPath			NULL				sha256			VER 생성
        TOUCH				base			ext			normFullPath			NULL				(가능하면) sha		VER 증가 없음
        RENAME				base			ext			normalize(fullPath)		normalize(oldPath)	(선택)			VER 증가 없음, VERSION 경로 동기화
        DELETE				base			ext			normFullPath (삭제 전 경로)	NULL				NULL			VER 증가 없음
        UNTRACKED/NOPATH	base or "N/A"	ext or ""	(가능하면)					(가능하면)				NULL			DOC/VER 생성 금지
***********************************************************************************************************************************************/

    // 3) ★★★ DELETE 처리 전용 분기 ★★★
    if ("DELETED".equals(resolvedAction)) {
        // 3-1) 경로 매칭 성공: 기존 문서/버전에 삭제 이력만 남김
        // 경로 매칭 성공/실패에 따라 AUDIT만 남기고 return
        if (verByPath != null) {
            // (선택) UPDATED_BY 스탬핑만 수행 (버전증가/헤더 변화 없음)
//         	Integer currentVerNoForRename = (lastNo != null) ? lastNo : 1; // 최소 1로 방어
//             // 버전 증가 없이 헤더만 최신화
//             Map<String, Object> mapperParam = new HashMap<>();
//             mapperParam.put("docId",    docId);
//             mapperParam.put("fileName", normFullPath);
//             mapperParam.put("ext",      ext);
//             mapperParam.put("verNo",    currentVerNoForRename);
//             mapperParam.put("userId",   userIdDerived);
//             dw01Mapper.updateDocHead(mapperParam);
            
            // 문서 상태를 DELETED 로 표시 (예: DOC_ST='D' 또는 DELETED_YN='Y')
            Map<String, Object> delHead = new HashMap<>();
            delHead.put("docId",  docId);
            delHead.put("userId", userIdDerived);
            dw01Mapper.markDocDeleted(delHead); 
            // SQL 예:
            // UPDATE TB_DRAWING_DOC
            //    SET DOC_ST='D', UPDATED_BY=#{userId}, UPDATED_AT=SYSDATE
            //  WHERE DOC_ID=#{docId}
        }

        // AUDIT 적재 (verId/docId 포함) → return
        Map<String,Object> insA = new HashMap<>();
        insA.put("docId",    (docId != null ? docId :  0));
        insA.put("verId",    (verId != null ? verId :  0));
        insA.put("action",   (verByPath != null) ? "DELETED" : "DELETED/UNTRACKED");
        insA.put("salesCd",  salesCd);
        insA.put("docNo",    recordId);
        insA.put("partNo",   partNo);
        insA.put("unitNo",   unitNo);
        insA.put("revNo",    revNo);
        insA.put("fileName", normFullPath);
        insA.put("ext",      ext);
        insA.put("fileSize", req.getFile().getSize()); // 보통 null
        insA.put("checksum", checksum);                 // 대부분 null
        insA.put("clientIp", ip);
        insA.put("userId",   userIdDerived);
        insA.put("userName", userNameDerived);
        insA.put("appName",  appName);
        insA.put("rawMessage", rawMessage);
//         insA.put("rawXml",     req.getRawXml());
        if (!isEmpty(oldPath)) insA.put("oldPath", oldPath);
        if (!isEmpty(fullPath)) insA.put("newPath", fullPath);
        // ★ 스키마 컬럼에 맞춰 SOURCE_EVT_ID 사용
        insA.put("recordId", recordId);
        insA.put("sourceEvtType", sourceEvtType);
        insA.put("sourceEvtId", evtId);

        dw01Mapper.insertAudit(insA);
        
        return;
    }  

    // 4) 경로 매칭 실패: 신규 문서/버전 생성
    if (verByPath == null) {
        // 신규 문서 등록 (헤더+버젼) → AUDIT 적재 (verId/docId 포함) → return
        Map<String, Object> ins = new HashMap<>();
        ins.put("salesCd",  salesCd);
        ins.put("docNo",    recordId);
        ins.put("verNo",    0);
        ins.put("fileName", normFullPath);
        ins.put("ext",      ext);
        ins.put("pathBase", p.getParent() != null ? p.getParent().toString() : "");
        ins.put("userId",   userIdDerived);
        ins.put("partNo",   partNo);
        ins.put("unitNo",   unitNo);
        ins.put("revNo",    revNo);
        dw01Mapper.insertDoc(ins); // selectKey
        docId = ins.get("docId") != null ? ((Number) ins.get("docId")).longValue() : null;
        
        // 신규 버전 0 등록 (VER_NO=0) → 헤더 최신화는 없음 → AUDIT 적재 (O) → return
        Map<String, Object> insV = new HashMap<>();
        insV.put("docId",     (docId != null ? docId :  0));
        insV.put("verNo",     0);
        insV.put("fileSize",  req.getFile().getSize());
        insV.put("checksum",  checksum);
        insV.put("storedPath", normFullPath);
        insV.put("comment",    quarantined ? (resolvedAction + "/QUARANTINE") : resolvedAction);
        insV.put("userId",     userIdDerived);
        dw01Mapper.insertVersion(insV);
        verId = insV.get("verId") != null ? ((Number) insV.get("verId")).longValue() : null; // (O)
    } 

    // 4) ★★★ RENAME 처리 전용 분기 ★★★
    if ("RENAMED".equals(resolvedAction)) {
        
        // fullPath와 oldPath가 DB에 존재하는지 확인
        // fullPath가 존재하면 삭제 처리 (파일+버젼) → 신규 등록 처리 (헤더/버젼) → AUDIT 등록 → return
        // oldPath가 존재하지 않으면 신규 등록 처리 (헤더/버젼) → AUDIT 등록 → return
        // 둘 다 존재하지 않으면 AUDIT만 등록하고 return
        // fullPath 존재 여부 확인
        // fullPath가 존재하면 삭제 처리 (파일+버젼)
        // oldPath가 존재하지 않으면 신규 등록 처리 (헤더/버젼)
        Map<String, Object> newPathParam = new HashMap<>();
        newPathParam.put("fileName", normFullPath);
        Map<String, Object> newPathRetrive = dw01Mapper.pathExists(newPathParam);
        boolean fullPathExists = newPathRetrive != null && newPathRetrive.get("DOC_ID") != null && !"".equals(newPathRetrive.get("DOC_ID"));

        // fullPath가 존재하면 삭제 처리 (파일+버젼)
        if (fullPathExists) {
            // fullPath가 존재하면 삭제 처리
            Map<String, Object> delParam = new HashMap<>();
            delParam.put("fileName", normFullPath);
            dw01Mapper.deletePath(delParam); // DELETE 파일

            // 버젼관리 삭제
            dw01Mapper.deletePathVersion(delParam); // DELETE 버젼관리
        }

        // oldPath가 존재하지 않으면 신규 등록 처리 (헤더/버젼)
        Map<String, Object> oldPathParam = new HashMap<>();
        oldPathParam.put("fileName", normOldPath);
        Map<String, Object> oldPathRetrive = dw01Mapper.pathExists(oldPathParam);
        boolean oldPathExists = oldPathRetrive != null && oldPathRetrive.get("DOC_ID") != null && !"".equals(oldPathRetrive.get("DOC_ID"));
        // 이전 파일이 없으면 초기화 등록 처리 (헤더/버젼) → AUDIT 등록 → return
        if (!oldPathExists) {
        	verByPath = null; // 신규 등록 처리 분기로 이동

            // 신규 등록 처리 (헤더/버젼)
            Map<String, Object> ins = new HashMap<>();
            ins.put("salesCd",  salesCd);
            ins.put("docNo",    recordId);
            ins.put("verNo",    1);
            ins.put("fileName", normFullPath);
            ins.put("ext",      ext);
            ins.put("pathBase", p.getParent() != null ? p.getParent().toString() : "");
            ins.put("userId",   userIdDerived);
            ins.put("partNo",   partNo);
            ins.put("unitNo",   unitNo);
            ins.put("revNo",    revNo);
            dw01Mapper.insertDoc(ins); // selectKey
            docId = ins.get("docId") != null ? ((Number) ins.get("docId")).longValue() : null;
            
            // 신규 버전 1 등록 (VER_NO=1) → 헤더 최신화는 없음
            Map<String, Object> insV = new HashMap<>();
            insV.put("docId",     (docId != null ? docId :  0));
            insV.put("verNo",     1);
            insV.put("fileSize",  req.getFile().getSize());
            insV.put("checksum",  checksum);
            insV.put("storedPath", normFullPath);
            insV.put("comment",    quarantined ? (resolvedAction + "/QUARANTINE") : resolvedAction);
            insV.put("userId",     userIdDerived);
            dw01Mapper.insertVersion(insV);
            verId = insV.get("verId") != null ? ((Number) insV.get("verId")).longValue() : null; // (O)

        }

        // RENAME/UNTRACKED로 AUDIT만 남기고 return
        // fullPath가 존재하지 않으면 헤더/버젼 갱신 없이 AUDIT만 남기고 return
        if ((verByPath != null) && (!isEmpty(fullPath))) {
	    	// VERSION 경로 동기화
            Map<String, Object> updV = new HashMap<>();
            updV.put("verId",      verId);
            updV.put("storedPath", normFullPath);
            dw01Mapper.updateVersionPath(updV);
            

        	Integer currentVerNoForRename = (lastNo != null) ? lastNo : 1; // 최소 1로 방어
            // 버전 증가 없이 헤더만 최신화
            Map<String, Object> mapperParam = new HashMap<>();
            mapperParam.put("docId",    docId);
            mapperParam.put("fileName", normFullPath);
            mapperParam.put("ext",      ext);
            mapperParam.put("verNo",    currentVerNoForRename);
            mapperParam.put("userId",   userIdDerived);
            mapperParam.put("partNo",   partNo);
            mapperParam.put("unitNo",   unitNo);
            mapperParam.put("revNo",    revNo);
            if (!isEmpty(fullPath)) {
                Path np = Paths.get(fullPath);
                String newBase = np.getParent() != null ? np.getParent().toString() : "";
                mapperParam.put("pathBase", newBase);
            }
            dw01Mapper.updateDocHead(mapperParam);
        } 
        
        // AUDIT 적재 (verId/docId 포함) → return
        Map<String, Object> insA = new HashMap<>();
        insA.put("docId",    (docId != null ? docId :  0));
        insA.put("verId",    (verId != null ? verId :  0));
        insA.put("action",   verByPath != null ? "RENAMED" : "RENAMED/UNTRACKED");
        insA.put("salesCd",  salesCd);
        insA.put("docNo",    recordId);
        insA.put("partNo",   partNo);
        insA.put("unitNo",   unitNo);
        insA.put("revNo",    revNo);
        insA.put("fileName", normFullPath);
        insA.put("ext",      ext);
        insA.put("fileSize", req.getFile().getSize());
        insA.put("checksum", checksum);
        insA.put("clientIp", ip);
        insA.put("userId",   userIdDerived);
        insA.put("userName", userNameDerived);
        insA.put("appName",  appName);
        // 원본
        insA.put("rawMessage", rawMessage);
//        insA.put("rawXml",     req.getRawXml());
        // RENAME (선택 컬럼)
        if (!isEmpty(oldPath)) insA.put("oldPath", oldPath);
        if (!isEmpty(fullPath)) insA.put("newPath", fullPath);
        insA.put("recordId", recordId);
        insA.put("sourceEvtId", evtId);
        insA.put("sourceEvtType", sourceEvtType);

        dw01Mapper.insertAudit(insA);

        return;
    }
    

    // 6) 버전 INSERT / 헤더 갱신 (TOUCH 제외)
    if (!"TOUCH".equals(resolvedAction)) {
        // bumpActiveVer → insertVersion → updateDocHead 순
        // (동시성 주의) bumpActiveVer에서 SELECT FOR UPDATE로 잠금
        
        Map<String, Object> bumpParam = new HashMap<>();
        bumpParam.put("docId", docId);
        bumpParam.put("userId", userIdDerived);
        dw01Mapper.bumpActiveVer(bumpParam); // UPDATE ACTIVE_VER = NVL(ACTIVE_VER,0)+1
        

        Integer verNo = dw01Mapper.getActiveVerForUpdate(docId); // SELECT ... FOR UPDATE
        if (verNo == null || verNo < 1) verNo = 1;

        // 신규 버전 등록 (VER_NO=1) → 헤더 최신화 → AUDIT 적재 (O) → return        
        Map<String, Object> insV = new HashMap<>();
        insV.put("docId",     docId);
        insV.put("verNo",     verNo);
        insV.put("fileSize",  req.getFile().getSize());
        insV.put("checksum",  checksum);
        insV.put("storedPath", normFullPath);
        insV.put("comment",    quarantined ? (resolvedAction + "/QUARANTINE") : resolvedAction);
        insV.put("userId",     userIdDerived);
        dw01Mapper.insertVersion(insV);
        verId = insV.get("verId") != null ? ((Number) insV.get("verId")).longValue() : null; // (O)


        // 헤더 최신화 (파일명/확장자/경로/버전/수정자) → AUDIT 적재 (O) → return 
        Map<String, Object> mapperParam = new HashMap<>();
        mapperParam.put("docId",   docId);
        mapperParam.put("fileName", normFullPath);
        mapperParam.put("ext",      ext);
        mapperParam.put("verNo",    verNo);
        mapperParam.put("userId",  userIdDerived);
        mapperParam.put("partNo",   partNo);
        mapperParam.put("unitNo",   unitNo);
        mapperParam.put("revNo",    revNo);
        dw01Mapper.updateDocHead(mapperParam);
    }
        
    // 7) 감사 기록 INSERT (verId/docId 포함)
    Map<String, Object> insA = new HashMap<>();
    insA.put("docId",    (docId != null ? docId :  0));
    insA.put("verId",    verId);
    insA.put("action",   resolvedAction);
    insA.put("salesCd",  salesCd);
    insA.put("docNo",    recordId);
    insA.put("partNo",   partNo);
    insA.put("unitNo",   unitNo);
    insA.put("revNo",    revNo);
    insA.put("fileName", normFullPath);
    insA.put("ext",      ext);
    insA.put("fileSize", req.getFile().getSize());
    insA.put("checksum", checksum);
    insA.put("clientIp", ip);
    insA.put("userId",   userIdDerived);
    insA.put("userName", userNameDerived);
    insA.put("appName",  appName);
    // 원본
    insA.put("rawMessage", rawMessage);
//  insA.put("rawXml",     req.getRawXml());
    // RENAME (선택 컬럼)
    if (!isEmpty(oldPath)) insA.put("oldPath", oldPath);
    if (!isEmpty(fullPath)) insA.put("newPath", fullPath);
    insA.put("recordId", recordId);
    insA.put("sourceEvtId", evtId);
    insA.put("sourceEvtType", sourceEvtType);

    dw01Mapper.insertAudit(insA);
}

    // null 또는 공백만 있으면 true (양쪽 공백 제거 후 판단)
    private static boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    // p가 null이면 null, levelsUp만큼 부모로 올라가서 이름 추출 (루트면 null)
    @Nullable
    private static String nameAt(Path p, int levelsUp) {
        if (p == null) return null;
        p = p.normalize(); // 후행 구분자/.. 정리
        for (int i = 0; i < levelsUp && p != null; i++) {
            p = p.getParent(); // 루트면 null
        }
        if (p == null) return null;
        Path fn = p.getFileName(); // 루트면 null
        return (fn != null) ? fn.toString() : null;
    }
    
    // user가 있으면 "domain,name" (둘 중 하나만 있으면 있는 것만)
    // user가 없으면 client.ip → 없으면 client.machine
    private static String deriveUserId(AuditIngestReq req) {
        if (req == null) return null;

        // 1) user가 있으면 "domain,name" (둘 중 하나만 있으면 있는 것만)
        if (req.getHost() != null) {
            String domain = Optional.ofNullable(req.getHost().getDomain()).map(String::trim).orElse(null);
            String name   = Optional.ofNullable(req.getHost().getUser()).map(String::trim).orElse(null);
            if (!isEmpty(domain) || !isEmpty(name)) {
                if (!isEmpty(domain) && !isEmpty(name)) return domain + "," + name; // 예: "16-GPT,USER"
                return !isEmpty(name) ? name : domain;
            }
        }

        // 2) user가 없으면 client.ip → 없으면 client.machine
        if (req.getHost() != null) {
            String ip = Optional.ofNullable(req.getHost().getMachine()).map(String::trim).orElse(null);
            if (!isEmpty(ip)) return ip;
            String machine = Optional.ofNullable(req.getHost().getMachine()).map(String::trim).orElse(null);
            if (!isEmpty(machine)) return machine;
        }
        return null;
    }

    // user.name이 있으면 그대로, 없으면 userId 대체
    private static String deriveUserName(AuditIngestReq req) {
        // user.name이 있으면 그대로, 없으면 userId 대체
        String name = Optional.ofNullable(req)
                .map(AuditIngestReq::getHost)
                .map(h -> h.getUser())
                .orElse(null);
        if (!isEmpty(name)) return name.trim();
        return deriveUserId(req);
    }

    // 경로 정규화: 구분자 통일, 대소문자 통일, 후행 구분자 제거    
    private static String normalizePath(String path) {
        if (isEmpty(path)) return path;
        String s = path.replace('/', '\\').trim();
        // Windows 케이스-인센서티브 대응: 소문자화 (SMB/NTFS 혼용 시 유용)
        s = s.toUpperCase(Locale.ROOT);
        // 후행 구분자 제거
        if (s.endsWith("\\"))
            s = s.substring(0, s.length()-1);
        return s;
    }
}
