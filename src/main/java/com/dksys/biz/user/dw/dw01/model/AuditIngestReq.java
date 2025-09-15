package com.dksys.biz.user.dw.dw01.model;

import java.time.OffsetDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuditIngestReq {

    // 샘플 JSON: "time_utc", "time_local" 
    @JsonAlias({"time_local"})
    private OffsetDateTime evtTimeLocal;  // e.g. "2025-09-10T16:57:37.7001705+09:00"
    
    @JsonAlias({"time_utc"})
    private OffsetDateTime evtTimeUtc;    // e.g. "2025-09-10T07:57:37.7011705Z"

    // 샘플 JSON 섹션들
    private Ids ids;
    private EventInfo event;
    private Watcher watcher;

    @JsonProperty("fs_event")
    private FsEvent fsEvent;
    
    private ActorProcess actorProcess;

    // 샘플 JSON의 file (Windows 파일 메타)
    private FileInfo file;

    private Security security;
    private Host host;
    
    @JsonProperty("fileObj")
    private FileObj fileObj;

    @JsonProperty("rawMessage")
    private String rawMessage;
    // ----- Nested types -----

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Ids {
        private String eventId;             		 // # 이벤트 인스턴스 ID (세션 내 증가)
        private String subscriptionId;             	 
        private String runspaceId;                   // "DefaultRunspace.InstanceId"
        private String processId;             		 // # 이벤트 인스턴스 ID (세션 내 증가)
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EventInfo {
        private String sourceIdentifier;             // "FSW.Renamed"
        private String senderType;                   // "System.IO.FileSystemWatcher"
        private OffsetDateTime timeGenerated;        // "2025-09-10T16:57:37.5519214+09:00"
        private Object messageData;                  // null 가능
        private List<String> sourceArgs;             // ["System.IO.FileSystemWatcher","System.IO.RenamedEventArgs"]
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Watcher {
        private boolean includeSubdirectories;       // true
        private String filter;                       // "*.dwg"
        private String notifyFilter;                 // "FileName, DirectoryName, LastWrite"
        private String path;                         // "D:\\DWG_AUDIT\\"
        private boolean enableRaisingEvents;         // true
        private Integer internalBufferSize;          // 8192
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FsEvent {
        private String name;                         // "23127-01NVFDT-5000-001.dwg"
        private String oldName;                      // "23127-01NVFDT-5000-001R1.dwg"
        private String directory;                    // "D:\\DWG_AUDIT"
        private String type;                         // "Renamed"
        private String extension;                    // ".dwg"
        private String oldFullPath;                  // "D:\\DWG_AUDIT\\23127-01NVFDT-5000-001R1.dwg",
        private String fullPath;                     // "D:\\DWG_AUDIT\\23127-01NVFDT-5000-001.dwg"
    }


    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ActorProcess {	
        private OffsetDateTime eventTime; 		 	// "2025-09-12T13:54:15.8213753+09:00"
        private String subjectUser;                 // "USER"
        private String subjectDom;                  // "16-GPT",
        private String processId;                   // 8040
        private String processIdRaw;                // "0x1f68"
        private String processName;                 // C:\\Program Files\\WindowsApps\\Microsoft.WindowsNotepad_11.2507.26.0_x64__8wekyb3d8bbwe\\Notepad\\Notepad.exe"
        private String objectName;                  // "D:\\DWG_AUDIT\\23127-01NVFDT-5000-001R2.dwg"
        private String accessList;                  // "%%4417\n\t\t\t\t",
        private String accessMask;                  // "0x2"
        private String eventId;                     // "4663"
        private String eventNo;                     // "1916255"
        private String renderedMessage;             // ""
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FileInfo {
        // 샘플 JSON의 file 섹션
        private String attributes;                   // "Archive"
        private OffsetDateTime lastAccessTime;       // "2025-09-10T16:56:25.4706207+09:00"
        private OffsetDateTime creationTime;         // "2025-09-10T16:31:11.3523593+09:00"
        private OffsetDateTime lastWriteTime;        // "2025-09-10T16:56:25.4706207+09:00"

        @JsonAlias("length")
        private Long size;                           // 21 (bytes)

        @JsonProperty("isReadOnly")
        private Boolean readOnly;                    // false

        private Boolean exists;                      // true
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Security {
        private List<String> access;                 // ["BUILTIN\\Administrators:FullControl", ...]
        private String owner;                        // "16-GPT\\USER"
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Host {
        private Integer processId;                   // 58488
        private String processName;                   // 58488
        private String domain;                       // "16-GPT"
        private String user;                         // "USER"
        private String machine;                      // "16-GPT"
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FileObj {
        private String fullPath;                     // "D:\\DWG_AUDIT\\23127-..."
        private String share;                        // "\\\\16-GPT\\D$"
        private String relPath;                      // "DWG_AUDIT\\23127-..."
        @JsonAlias("size")
        private Long size;                           // 41 (bytes)
        private String sha256;                       
        private Boolean hashPending; //
    }
}
