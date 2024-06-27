
//결재승인 버튼
///commApprovalMobile.js 모듈에서 일괄 처리하게 변경
//function approvalConfirm() { 
//	commApprovalMobile.confirmApproval();
//}
function approvalConfirm() {
	$('#appConfirmAnchor').hide();
	if (commApproval.confirmApproval()) {
		window.history.back();
		return true;
	}
	$('#appConfirmAnchor').show();
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

		let actionType = (this.param.todoDiv1CodeNm == '결재') ? '결재승인' : '공유확인';
		
		if( !htmlParam.htmlArea ) {		
			alert( '결재란 영역을 찾을수 없습니다. \r\nex) <div id="approval_area"></div>');
			return false;
		}
		
//			console.log('---html make');
			var htmlId = htmlParam.htmlArea;
			var htmlTable = `
			<div class="" style="display: block; width: 372px; height: 100%; margin-bottom: 30px; border:0px solid #eee; padding-bottom:5px;">
				<!--
		        <h3 class="location">
		          <span class="page_tit" style="text-align: left;">결재</span>
		        </h3>
		        -->
				<!--결재 테이블 -->
		        <div clss="contents" id="applist" style="whdth:100%; height: 100%; padding: 5px">
			    	<!-- 결재라인 table -->
			    	<table id="appLine" class="table_input">
			    		<colgroup>
			    			<col width="12%">
			    			<col width="20%">
			    			<col width="24%">
			    			<col width="16%">
			    			<col width="22%">
			    		</colgroup>
			    		<tr id="appH">
			    			<th class="appTh">순번</th>
			    			<th class="appTh">결재자</th>
			    			<th class="appTh">결재의견</th>			    			
			    			<th class="appTh">상태</th>
			    			<th class="appTh">결재일자</th>		    			
			    		</tr>
			    	</table>	
			    	<div class="mobile_mid_btn" style="text-align: center; float:center; padding-top:10px;" id="appBtnDiv">
					<button	type="button" style="height: 40px;  width: 90px;" id="appConfirmAnchor" onclick="approvalConfirm();" >${actionType}</button>
					<button type="button" class="close_btn" style="height: 40px;  width: 90px;" id="appConfirmAnchor" onclick="window.history.back();" >닫기</button>
					</div>
		        </div>			        
				<!--결재 테이블 end-->		        
		    </div>		 			
			`;
			
			$("#"+htmlId).html('');
			$("#"+htmlId).append(htmlTable);
			//결재일경우에만 결재진행상태 표시함

			if (this.param.todoDiv1CodeNm == '공유') {
				$('#appLine').remove();
				applyBtn = true;
			} else {  //결재인경우
				//팀장 이슈 조치결과 결재일경우 위험성 평가 기능 추가 하기위함   남장섭 240618
				var actDngEval = `
							<tr style="text-align: right;">
				                <th class="hit" colspan=2>위험도 평가</th>
				                <td colspan=1>
				                    <select id="actDngEvalTodo" name="actDngEvalTodo"msg="위험도 평가" required>
				                        <option value="">선택</option>
										<option value="ACTDNG01">상</option>
										<option value="ACTDNG02">중</option>
										<option value="ACTDNG03">하</option>
									</select>
				                </td>
								<td colspan=2></td>
				            </tr>			
							`;
				var confrmActDngEval = '';
				//결재라인 read
				postAjaxSync("/user/wb/wb20/selectGetApprovalList", this.param, null
					, function(data){
						var list = data.resultList;
						var todoCfOpnHid = "";
		 				if( data.resultList.length > 0 ) {
		 					
		 					var htmlTr = "";
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
									//결재문서가 수주목표가 결재가 아니면 순서 상관없이 결재처리 가능함. 20240625 남장섭
									if (data.todoDiv2CodeId != "TODODIV2100") {
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
			}
			this.todoId = todoId;
			this.applyBtn = applyBtn;
			//btn control
			this.applyBtnCtrl();		
			if( this.applyBtn ) {
				Object.assign(this.param, approvalParam);				
			}	
	}
	
	//btn show & auth
	this.applyBtnCtrl = function() {
		if( this.applyBtn ) {
			$("#appBtnDiv").show();
			$("#appConfirmAnchor").attr("onclick", "approvalConfirm()");
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
    			<td class="appTd" ><textarea class='form-control' rows='2'  name='todoCfOpn' value="@@item3@@" readonly="readonly"></textarea></td>
    			<td class="appTd">@@item4@@</td>
    			<td class="appTd">@@item5@@</td>    			
    		</tr>		
			`;
		return html;
	}
	
	//승인 ajax
	this.confirmApproval = function(param) {
		//승인 save
		if( !this.applyBtn ) {
			  return false
		}
			if( this.param.todoDiv1CodeNm == "공유" ) {
				if( this.param.sanctnSttus == "N" ) {
					if( confirm("확인 처리 하시겠습니까?") ) {
					    if (this.param.todoKey != undefined) {
						 	 var today = new Date();
						     var strDate = today.getFullYear()+"-"+('0'+(today.getMonth()+1)).slice(-2)+"-"+('0'+today.getDate()).slice(-2);
						     
						     var formData = new FormData();
						     formData.append("todoKey", this.param.todoKey);
						     formData.append("creatId", jwt.userId);
						     formData.append("creatPgm", this.param.pgmId);
						     //formData.append("todoCfDt", strDate);
						     filePutAjax("/user/wb/wb20/toDoCfDtUpdate", formData, function(data){
						         if(data.resultCode == 200){
									this.applyBtn = false;
						         }
						     }); 
					    }    
					}
				} 
				return true;
			} else if( this.param.todoDiv1CodeNm == "결재" ) {
			
				if (!inputValidation($('.popup_area [required]'))) {
					return false;
				}
				var confirmYn = false;	
				//본인 결재의견
				var todoCfOpn = $("#appLine tr").find("font").closest("tr").find("textarea[name=todoCfOpn]").val();
				//입력값 set			
				var paramMap = {		
						"todoId" : jwt.userId
						, "todoCfOpn" : todoCfOpn
						, "issNo" 		: $('#issNo').val()
						, "actDngEval" : $('#actDngEvalTodo').val()
				}
	
				Object.assign(paramMap, this.param);
				postAjaxSync("/user/wb/wb20/insertApprovalLine", paramMap, null, function(data){
					if(data.resultCode == 200){
						confirmYn = true;
						let todoYn = data.result.todoYn;
						if( todoYn == "Y" ) {
							sendTodoFinal(paramMap);																
						}
						
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
			} //결재 END
	}
	

	var kakaoErr = [];

	//최종결재시 결재완료 최초작성자(1번)에게 전송
	function sendTodoFinal(param) {
		//message load
		var paramObj = {
						"userId" : jwt.userId
						, "codeKind" : "KAKAOMSG"
						};
		postAjaxSync("/user/sm/sm02/selectCurrToday", paramObj, null
					, function(data) {   
						if(data.resultList.length > 0 ) {
							kakaoErr = data.resultList;
						}
		});     //user search ajax end	
		
		//TODO 결재완료 알림톡
		let paramSend = {
				"tmplatDiv": "TMPLATDIV03"		//발송템플릿관리번호
				, "todoDiv1CodeId": param.todoDiv1CodeId					//공통코드 해당업무 - 결재
				, "todoDiv2CodeId": param.todoDiv2CodeId					//공통코드 해당업무 - 결재 - 발주서
				, "todoDiv1CodeNm": "결재"						//공통코드 해당업무 - 결재 - 발주서
				, "todoDiv2CodeNm": param.todoTitl + "["+param.todoDiv2CodeNm + param.todoNo +"]"  //공통코드 해당업무 - 결재 - 발주서 -- 템플릿 승인 후에는 삭제요망
				, "sanctnDiv2": "결재"							//템플릿 결재구분2    #{sanctnDiv2} 처리 바랍니다.
				, "todoNo": param.todoNo
				, "clntCd": "1"					//발신회사는 로그인사용자 회사			
				, "creatPgm": param.pgmId
				, "sanctnSn": "1"			//결재순번 1번을 결재상신자로 본다
		}
		commKaKaoSendTodo(paramSend);	
	}

	//to-do결재요청 알림톡
	function commKaKaoSendTodo(param) {
//	 	console.log('---카카오 발주서 결재모두완료시 알림톡발송시작 --' + param);
		//알림톡수신번호 채번
		var maxMessageId = null;			//message id(unique key)
		var talkMessage = null 				//발송될 내용 template
		var mobile = null					//수신인 휴대전화번호
		var title = null					//todo title
		var sendList = [];
		let talkParam = {};
		let talkBody = {};		
		let sendCnt = 0;
		postAjaxSync("/user/bm/bm18/selectMaxMessageIdTodo", param, null
					, function(data) {   
						if(data.resultList.length > 0 ) {
							if( data.resultList[0].maxMessageId != "" ) {
								sendList = data.resultList; 
							} else {
								alert("알림톡 발송을 위한 messageId 생성에 실패 했습니다.");
								return;
							} 

						}
		});     	
		//user search ajax end
		
		//대상 loop
		$.each(sendList, function (key, sendObj) {
			maxMessageId = sendObj.maxMessageId;							
			talkMessage = sendObj.messageDesc;
			mobile =  sendObj.telNo;
			//로그용
			param.rcvId = sendObj.todoId;			//todo 수신id
			param.rcvNm = sendObj.name;				//todo 수신자명
			param.nameTo = sendObj.name;				//todo 수신자명
			title =   sendObj.name + " " + param.todoDiv2CodeNm   //sendObj.todoTitl;
			param.title = title;								//요청내용제목
			param.sendDt = sendObj.sendDt;
			if (sendObj.todoCfOpn){
				param.todoDiv2CodeNm += "\n["+ sendObj.name + " 코멘트:" + sendObj.todoCfOpn + "]";
			}
			var todayDate = new Date().format('yyyy-MM-dd HH:mm');
			param.endDt = todayDate;
			
			if(mobile == "" || mobile == null) {
				alert('수신전화번호가 없습니다.\r\n['+clntNm+'] 거래처관리의 사업부 첫번째 항목을 확인해 주십시오.');
				return;
			}
			if(talkMessage == "" || talkMessage == null) {
				alert('발송할 내용이 없습니다.\r\n알림톡템플릿관리를 확인해 주십시오.');
				return;
			}	
			//message 치환
			let message = talkMessage;
			let splitStr = Array.from(message.matchAll('\\{(.*?)\\}'), match => `${match[0]}`);	
			//loop
			$.each(splitStr, function (key, val) {		
				let compKey = val.substring(1, val.length-1);
				if( compKey != "" && compKey != "undefined" ) {
					if( typeof(param[compKey]) != "undefined" ) {
						let val2 = '#'+val;
						message = message.replaceAll(val2, param[compKey]);
					}
				}
			});

			talkParam.authToken = "e/eDfZOFCsrBqadaECQ0+g==";
			talkParam.serverName = "gyitt2400";		
			talkParam.paymentType = "P";
			talkBody.service = "2310086157";				//서비스번호(1000000000 - 예시  real : 2310086157
			talkBody.messageId = maxMessageId;			//고객사에서 관리하는 메시지No - 40byte (unique 값);	
			if (title.length > 50) {
				title = title.substring(0, 49); // 50자까지만 자르기
			}
			talkBody.title = title;					//알림톡 타이틀 -
			talkBody.message = message;				//알림톡 메시지 (1000 byte) 
			talkBody.mobile = mobile;				//휴대전화번호
			talkBody.template = "10006";			//템플릿관리화면 템플릿ID	- 10004 번 결재완료시 템플릿으로 교체요망
			let talkJson = JSON.stringify(talkBody);		
			sendCnt = kakaoSendReal(talkJson, talkParam, param);		
		});
		//대상 loop end
		if( sendCnt > 0 ) {
//	 		alert("알림톡 정상 발송되었습니다.");		
		}
	}
}