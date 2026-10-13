package com.dksys.biz.admin.cm.cm40.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FileOpenFile {

    @JsonProperty("FileId")
    private String fileId;

    @JsonProperty("SessionId")
    private String sessionId;

    @JsonProperty("Path")
    private String path;

    @JsonProperty("ShareRelativePath")
    private String shareRelativePath;

    @JsonProperty("ClientComputerName")
    private String clientComputerName;

    @JsonProperty("ClientUserName")
    private String clientUserName;

    @JsonProperty("ShareName")
    private String shareName;

    @JsonProperty("EntryType")
    private String entryType;

    @JsonProperty("IsDirectory")
    private Boolean isDirectory;
}
