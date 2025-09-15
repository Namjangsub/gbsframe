package com.dksys.biz.user.dw.dw01.model;


import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuditIngestReqOld {
    private OffsetDateTime evtTimeUtc;    // "2025-09-04T01:16:37Z"
    private OffsetDateTime evtTimeLocal;  // "2025-09-04T10:16:37+09:00"
    private String action; 					// CREATE/CHANGE/DELETE/RENAME/WRITE
    private User user;
    private Client client;
    private FileInfo file;
    private WinEvent winEvent;

    @JsonAlias({"RawMessage"})
    private String rawMessage;

    @JsonAlias({"RawXml"})
    private String rawXml;
    
    @Data public static class Client { private String machine; private String ip; }
    
    @Data public static class User { private String domain; private String name; private String sid; }
    @Data public static class FileInfo {
        private String fullPath; // D:/Shares/고객사/SALES/DOC/ABC.dwg
        private String share;    // \\FS01\고객사
        private String relPath;  // SALES/DOC/ABC.dwg
        private Long   size;     // bytes
        private String sha256;   // hex
        // (옵션) PowerShell에서 파싱 가능하다면 넣어주면 API가 우선 사용
        private String salesCd;  // ex) "25079-06HARDT"
        private String docNo;    // ex) "BR2" 등// ★ 추가: RENAME 대응
        
        // RENAME 대응
        @JsonAlias({"OldPath","old_path"})
        private String oldPath;

        @JsonAlias({"NewPath","new_path"})
        private String newPath;
    }
    @Data public static class WinEvent { 
    	private Integer id; 
    	private Long recordId; 
    	private String accesses;
    	@JsonAlias({"ProcessName","process_name"})
        private String  processName;
    	private String  accessText;
    	private String  accessMaskHex;
    	private Integer  accessMask;
    	private String  accessFlags;
    	private String  subjectUser;
    	private String  subjectDomain;
    	private String  source;
    } // 4660/4663/5145 등
}