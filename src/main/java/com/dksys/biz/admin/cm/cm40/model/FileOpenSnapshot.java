package com.dksys.biz.admin.cm.cm40.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FileOpenSnapshot {

    @JsonProperty("Success")
    private Boolean success;

    @JsonProperty("CollectedAtUtc")
    private String collectedAtUtc;

    @JsonProperty("Count")
    private Integer count;

    @JsonProperty("Files")
    private List<FileOpenFile> files;

    @JsonProperty("Error")
    private String error;
}
