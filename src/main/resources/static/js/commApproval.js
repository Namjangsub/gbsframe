
//결재승인 버튼
function approvalConfirm() { 
	commApproval.confirmApproval();	
}

//결재 메인
function Approval(htmlParam, param, popParam) {
	this.htmlParam = htmlParam;		//결재창 출력영역
	this.param = param;				//ADD 한 param
	this.popParam = popParam;		//부모창에서 공통팝업으로 전달한 param
	Object.assign(this.param, this.popParam);
	//this.boldFont = "font-weight:bold; color:blue;";
	this.boldFont = "<font style='font-weight:bold; color:blue;'>";
	
	this.todoId = null;
	this.applyBtn = false;
	var approvalParam = {}
	
	//html make
	this.makeHtml = function() {
		
		var trTempl = this.htmlTr();
		var boldFont = this.boldFont;
		var todoId = this.todoId;
		var applyBtn = this.applyBtn;
		//sanctnSn reset
		if( $("#appLine tr").length > 0 ) {
			delete this.param.sanctnSn;
			delete this.param.todoKey;
		}
		
		if( htmlParam.htmlArea ) {
//			console.log('---html make');
			var htmlId = htmlParam.htmlArea;
			var htmlTable = `
			<div class="" style="display: block; width: 700px; height: 100%; margin-bottom: 30px; border:0px solid #eee; padding-bottom:5px;">
				<!--
		        <h3 class="location">
		          <span class="page_tit" style="text-align: left;">결재</span>
		        </h3>
		        -->
				<!--결재 테이블 -->
		        <div clss="contents" id="applist" style="height: 100%; padding: 5px">
			    	<!-- 결재라인 table -->
			    	<table id="appLine" style="border: 1px solid #dbdbdb; border-collapse: collapse" >
			    		<colgroup>
			    			<col width="8%">
			    			<col width="10%">
			    			<col width="*%">
			    			<col width="10%">
			    			<col width="15%">			    			
			    		</colgroup>
			    		<tr id="appH" stye="text-align:center; border-bottom:1px solid #dbdbdb; height:25px;">
			    			<th class="appTh">순번</th>
			    			<th class="appTh">결재자</th>
			    			<th class="appTh">결재의견</th>			    			
			    			<th class="appTh">상태</th>
			    			<th class="appTh">결재일자</th>		    			
			    		</tr>
			    	</table>
			    	<div class="popup_bottom_btn" id="appBtnDiv">
						<button id="appConfirmAnchor"  onclick="approvalConfirm();">결재승인</button>
					</div>
		        </div>			        
				<!--결재 테이블 end-->		        
		    </div>		 			
			`;
			
			$("#"+htmlId).html('');
			$("#"+htmlId).append(htmlTable);
			
			//팀장 이슈 조치결과 결재일경우 위험성 평가 기능 추가 하기위함   남장섭 240618
			var actDngEval = `
						<tr style="text-align: right;">
			                <th class="hit" colspan=2>위험도 평가</th>
			                <td colspan=1>
			                    <select id="actDngEval" name="actDngEval"msg="위험도 평가" required>
			                        <option value="">선택</option>
									<option value="ACTDNG01">상</option>
									<option value="ACTDNG02">중</option>
									<option value="ACTDNG03">하</option>
								</select>
			                </td>
							<td colspan=2></td>
			            </tr>			
						`;
							
			//결재라인 read
			postAjaxSync("/user/wb/wb20/selectGetApprovalList", this.param, null
				, function(data){
					var list = data.resultList;
					var todoCfOpnHid = "";
	 				if( data.resultList.length > 0 ) {
	 					
	 					var htmlTr = "";
	 					var confrmActDngEval = "";
		 				$("#appLine tr").eq(0).next().remove();
				        $.each(list, function (idx, data) {
		 					var html = trTempl;				        	
							//html = html.replace(/@@item1@@/gi, (idx+1));		//순번
							html = html.replace(/@@item1@@/gi, data.sanctnSn);		//순번
							html = html.replace(/@@item2@@/gi, data.todoNm);		//결재자명
							var todoCfOpn = data.todoCfOpn;
							if( typeof(todoCfOpn)== "undefined" || todoCfOpn=="" ) todoCfOpn = "";
							html = html.replace(/@@item3@@/gi, todoCfOpn);		//결재의견
							//본인해당시 볼드처리 - button 활성화
							if( data.todoId != "undefined" && (data.todoId == jwt.userId ) ) {
								html = html.replace(/@@bold@@/gi, boldFont);
								todoId = data.todoId;
								//applyBtn SHOW - 순번이 1이거나 이전 결재 상태가 Y일 경우							
								if( data.sanctnSttus != "undefined" && data.sanctnSttus == "N"  ) {
									if( data.sanctnSn == "1" || data.preSttus=="Y") {
										applyBtn = true;										
									} 
								//결재가 1건일 경우 
								} else if( data.sanctnSn == "1" && typeof(data.preSttus)=="undefined" ) {
									applyBtn = true;									
								}
								//다음순번이 미결재일 경우 결재의견 가능하게 변경
								if( data.nextSttus=="N") applyBtn = true;
								//만족시 버튼 show
								if( applyBtn == true ) {
									approvalParam.todoKey = data.todoKey;
									approvalParam.sanctnSn = data.sanctnSn;
									approvalParam.coCd = data.coCd;
									approvalParam.todoDiv1CodeId = data.todoDiv1CodeId;
									approvalParam.todoDiv2CodeId = data.todoDiv2CodeId;
									approvalParam.todoNo = data.todoNo;
									html = html.replace(/readonly/gi, "");		//결재의견 input
									//팀장 이슈 조치결과 결재일경우 위험성 평가 기능 추가 하기위함   남장섭 240618
					 				if( data.todoDiv2CodeId=='TODODIV2090' && data.teamManager == 'TEAM01' ) {
					 					confrmActDngEval = actDngEval;
					 				}
								} 
							}
							if(applyBtn == false) {
								html = html.replace(/@@bold@@/gi, "");
							} else {
								html = html.replace(/@@bold@@/gi, "");								
							}
							//console.log('--applyBtn--' + applyBtn);
							html = html.replace(/@@item4@@/gi, data.sanctnSttusNm);		//상태명
							html = html.replace(/@@item5@@/gi, data.todoCfDt);		//확인(결재)일자
							htmlTr += html;
						});			
					} 
					$("#appLine").append(htmlTr);
					
					//팀장 이슈 조치결과 결재일경우 위험성 평가 기능 추가 하기위함   남장섭 240618
					$("#appLine").append(confrmActDngEval);
					
			});		//end ajax
			
			this.todoId = todoId;
			this.applyBtn = applyBtn;
			//btn control
			this.applyBtnCtrl();		
			if( this.applyBtn ) {
				Object.assign(this.param, approvalParam);				
			}			
		} else {
			alert( '결재란 영역을 찾을수 없습니다. \r\nex) <div id="approval_area"></div>');
		}
	}
	
	//btn show & auth
	this.applyBtnCtrl = function() {
		if( this.applyBtn ) {
			$("#appBtnDiv").show();
			$("#appConfirmAnchor").attr("onclick", "approvalConfirm()");
			//본인 결재의견
			let todoCfOpn = $("#appLine tr").find("font").closest("tr").find("input[name=todoCfOpn]").val();
			if( todoCfOpn != "" ) {
				$("#appConfirmAnchor").text("의견수정");
			}
		} else {
			//hide
			$("#appBtnDiv").hide();
			$("#appConfirmAnchor").removeAttr("onclick");		
		}		
		return;
	}
	
	//loop contents html
	this.htmlTr = function() {
		var html = ` 
    		<tr style="border-bottom:1px solid #dbdbdb;">
    			<td class="appTd">@@item1@@</td>
    			<td class="appTd">@@bold@@@@item2@@</font></td>
    			<td class="appTd" style='text-align:left; padding-left:5px; height:25px;'><input type='text' name='todoCfOpn' value="@@item3@@" class="form-control" readonly="readonly"></td>
    			<td class="appTd">@@item4@@</td>
    			<td class="appTd">@@item5@@</td>    			
    		</tr>		
			`;
		return html;
	}
	
	//승인 ajax
	this.confirmApproval = function(param) {

		if (!inputValidation($('.popup_area [required]'))) {
			return false;
		}
		var confirmYn = false;	
		//승인 save
		if( this.applyBtn ) {
			//본인 결재의견
			var todoCfOpn = $("#appLine tr").find("font").closest("tr").find("input[name=todoCfOpn]").val();
			//입력값 set			
			var paramMap = {		
					  "todoId" 		: jwt.userId
					, "todoCfOpn" 	: todoCfOpn
			}
			let anchorText = $("#appConfirmAnchor").text();
			let confirmText = (anchorText == "의견수정") ? "수정" : "승인"			
			Object.assign(paramMap, this.param);
			postAjaxSync("/user/wb/wb20/insertApprovalLine", paramMap, null, function(data){
				if(data.resultCode == 200){
					postAjaxSync("/user/qm/qm01/updateReqStChk", paramMap, null, function(data){
						if(data.resultCode == 200){
						}
						confirmYn = true;
					});
					
					//팀장 이슈 조치결과 결재일경우 위험성 평가 기능 추가 하기위함   남장섭 240618
					if ($('#actDngEval').length) {
						let paramObj = {
								  issNo 		: $('#issNo').val()
								, actDngEval	: $('#actDngEval').val()
						}
						postAjaxSync("/user/wb/wb24/updateWbsIssueResultEvaluate", paramObj, null, function(data){
							if(data.resultCode != 200){
								alert("위험도 평가 반영에 실해하였습니다.");	
							}
						});
			        } 
					
					//최종결재 완료시 알림톡
					postAjaxSync("/user/wb/wb20/selectTodoFinalYn", paramMap, null, function(data){
						let todoYn = data.result[0].todoYn;
						if( todoYn == "Y" ) {
							sendTodoFinal(paramMap);																
						}
					});					
				} else {
					alert("승인중 오류가 발생 되었습니다.");	
				}
			});	
			
			
			if( confirmYn ) {
				this.makeHtml();
				this.applyBtn = false;
				this.applyBtnCtrl();
			}
			return true;
		} else {
			return false;
		}    
	}
}



