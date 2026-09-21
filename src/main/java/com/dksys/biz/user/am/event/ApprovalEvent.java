package com.dksys.biz.user.am.event;

import java.util.Map;

public class ApprovalEvent {

    private final String eventType; // SUBMIT, APPROVE, REJECT, CANCEL, COMPLETE
    private final String docId;
    private final String docNo;
    private final String docTitle;
    private final String draUserId;
    private final String draUserNm;
    private final String nextApproverId;
    private final String nextApproverNm;
    private final Map<String, Object> extraInfo;

    public ApprovalEvent(String eventType, String docId, String docNo, String docTitle,
                         String draUserId, String draUserNm, String nextApproverId,
                         String nextApproverNm, Map<String, Object> extraInfo) {
        this.eventType = eventType;
        this.docId = docId;
        this.docNo = docNo;
        this.docTitle = docTitle;
        this.draUserId = draUserId;
        this.draUserNm = draUserNm;
        this.nextApproverId = nextApproverId;
        this.nextApproverNm = nextApproverNm;
        this.extraInfo = extraInfo;
    }

    public String getEventType() { return eventType; }
    public String getDocId() { return docId; }
    public String getDocNo() { return docNo; }
    public String getDocTitle() { return docTitle; }
    public String getDraUserId() { return draUserId; }
    public String getDraUserNm() { return draUserNm; }
    public String getNextApproverId() { return nextApproverId; }
    public String getNextApproverNm() { return nextApproverNm; }
    public Map<String, Object> getExtraInfo() { return extraInfo; }
}
