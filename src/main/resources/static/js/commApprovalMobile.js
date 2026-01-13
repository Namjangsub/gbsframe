
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

//보완요청 버튼
function approvalMemoComment() {
	$('#appConfirmAnchor').hide();
	if (commApproval.approvalMemoComment()) {
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

			$('#main_area').css('padding', '0px');
//			console.log('---html make');
			var htmlId = htmlParam.htmlArea;
			var htmlTable = `
			<div class="" style="display: block; width: 100%; height: 100%; margin-bottom: 30px; border:0px solid #eee; padding-bottom:5px;">
				<!--
		        <h3 class="location">
		          <span class="page_tit" style="text-align: left;">결재</span>
		        </h3>
		        -->
				<!--결재 테이블 -->
		        <div clss="contents" id="applist" style="whdth:100%; height: 100%; padding: 0px">
			    	<!-- 결재라인 table -->
			    	<table id="appLine" class="table_input">
			    		<colgroup>
			    			<col width="12%">
			    			<col width="15%">
							<col width="15%">
			    			<col width="24%">
			    			<col width="12%">
			    			<col width="22%">
			    		</colgroup>
			    		<tr id="appH">
			    			<th class="appTh">순번</th>
			    			<th class="appTh">결재자</th>
							<th class="appTh">투입공수</th>
			    			<th class="appTh">결재의견</th>
			    			<th class="appTh">상태</th>
			    			<th class="appTh">결재일자</th>
			    		</tr>
			    	</table>
			    	<div class="mobile_mid_btn" style="text-align: center; float:center; padding-top:10px;" id="appBtnDiv">
					<button	type="button" style="height: 40px;  width: 90px;" id="appConfirmAnchor" onclick="approvalConfirm();" >${actionType}</button>
					<button type="button" style="height: 40px;  width: 90px;" id="appConfirmAnchor"  onclick="approvalMemoComment();">보완요청</button>
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
				var sameTimeResultChk = this.param.sameTimeResultChk;	// 발주요청서 결재시 해당 건 동시등록여부 체크
				postAjaxSync("/user/wb/wb20/selectGetApprovalList", this.param, null
					, function(data){
						var list = data.resultList;
						var todoCfOpnHid = "";
		 				if( data.resultList.length > 0 ) {

		 					var htmlTr = "";
			 				$("#appLine tr").eq(0).next().remove();
					        $.each(list, function (idx, data) {
			 					var html = trTempl;
								html = html.replace(/@@deptId@@/g, data.todoId); // 각 행의 부서 코드
								html = html.replace(/@@todoKey@@/g, data.todoKey);
								//html = html.replace(/@@item1@@/gi, (idx+1));		//순번
								html = html.replace(/@@item1@@/gi, data.sanctnSn);		//순번
								html = html.replace(/@@item2@@/gi, data.todoNm);		//결재자명
								var todoCfOpn = data.todoCfOpn;
								if( typeof(todoCfOpn)== "undefined" || todoCfOpn=="" ) todoCfOpn = "";
								html = html.replace(/@@item4@@/gi, todoCfOpn);		//결재의견
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
						 				// if( data.todoDiv2CodeId=='TODODIV2090' && data.teamManager == 'TEAM01' ) {
						 				// 	confrmActDngEval = actDngEval;
						 				// }
									}
								}
								if(applyBtn == false) {
									html = html.replace(/@@bold@@/gi, "");
								} else {
									html = html.replace(/@@bold@@/gi, "");
								}
								// ==============================================================================================
								// 1. 문제, 발주요청서(결과따로/결과일괄) 결재시 팀장 공수입력 처리
								// 2. 문제(결과)에 대한 결재(TODODIV2090)
								// 3. 발주요청서 결과 따로 등록 건은 TODODIV2030 이렇게 들어오기 때문에 결과팝업창에서 처리가능
								// 4. 발주요청서결과 일괄등록 건은 TODODIV2020 이 구분자만 존재하므로 this.param.sameTimeResultChk 에서 동시 여부 판단
								// 5. this.param.sameTimeResultChk == 'Y' 이면 동시 등록
								// 6. this.param.sameTimeResultChk == 'N' 이면 따로 등록
								let editable = false; // 투입공수 입력여부 플래그
								// 본인결재건이면서 팀장일때
								if (applyBtn && data.deptTeamManager === 'TEAM01' && jwt.userId === data.todoId && ['GUN30', 'GUN40', 'TRN50', 'GUN60'].includes(jwt.deptId.slice(0, 5))) {
									if  ((data.todoDiv2CodeId === 'TODODIV2020' && sameTimeResultChk === 'Y') 
											|| (data.todoDiv2CodeId === 'TODODIV2090' || data.todoDiv2CodeId === 'TODODIV2030')){
											editable = true;
									}
								}
								// 투입공수 입력가능
								if (editable) {
									html = html.replace(
										/@@item3@@/gi,
										`<input type="text" name="actMh" class="form-control" value="${gPasIntChk((data.actMh)) || ''}" style='text-align:center; padding-right:5px; height:40px;' comma onkeyup="onlyNumber(this)" required msg="투입공수">
										 <input type="hidden" name="actTeamManager" value="${data.deptTeamManager}">
										 <input type="hidden" name="requiredMh" value="YES">`
									);
								} else {
									// 읽기전용
									html = html.replace(
										/@@item3@@/gi,
										`<input type="text" name="actMh" readonly value="${gPasIntChk((data.actMh)) || ''}" style='text-align:center; padding-right:5px; height:40px;' comma>`
									);
								}
								
								// ==============================================================================================
								
								//console.log('--applyBtn--' + applyBtn);
								html = html.replace(/@@item5@@/gi, data.sanctnSttusNm);		//상태명
								html = html.replace(/@@item6@@/gi, data.todoCfDt);		//확인(결재)일자
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

			const $tr = $("#appLine tr").find("font").closest("tr");
			//본인 결재의견
			const todoCfOpn = ($tr.find('textarea[name="todoCfOpn"]').val() ?? '').trim();
			// 2) 상태값: textarea가 들어있는 td의 "바로 다음 td"
			const status = $tr.find('textarea[name="todoCfOpn"]').closest('td').next('td').text().trim();   // "미승인" 또는 "승인"

			if(status === '승인') {
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
    		<tr data-dept-id="@@deptId@@" data-todoKey="@@todoKey@@" style="border-bottom:1px solid #dbdbdb;">
    			<td class="appTd">@@item1@@</td>
    			<td class="appTd">@@bold@@@@item2@@</font></td>
				<td class="appTd">@@item3@@</td>
    			<td class="appTd" ><textarea class='form-control' rows='2'  name='todoCfOpn' value="@@item4@@" readonly="readonly"></textarea></td>
    			<td class="appTd">@@item5@@</td>
    			<td class="appTd">@@item6@@</td>
    		</tr>
			`;
		return html;
	}

	//승인 ajax
	this.confirmApproval = function(param) {

		if (((this.param.todoDiv2CodeId === "TODODIV2020") || (this.param.todoDiv2CodeId === "TODODIV2030") || (this.param.todoDiv2CodeId === "TODODIV2090")) && this.param.teamManager == '평가' ) {
			if ((this.param.todoDiv2CodeId === "TODODIV2020") || (this.param.todoDiv2CodeId === "TODODIV2030")) {
				if (!['COBTP01', 'COBTP04', 'COBTP06', 'COBTP08', 'COBTP09'].includes($("#partCd").val())) {
					if (!inputValidation($('.popup_area [required]'))) {
						return false;
					}
					if (!$("input[name='FDMTSOLUT']:checked").val() && this.param.teamManager == '평가') {
						customAlert("근본원인을 선택해주세요.");
						return false;
					}
				}
			} else {
				if (!inputValidation($('.popup_area [required]'))) {
					return false;
				}
				if (!$("input[name='FDMTSOLUT']:checked").val()) {
					customAlert("근본원인을 선택해주세요.");
					return false;
				}
			}
		}

		// 평가대상인지 확인하고 평가대상은 결재방지
		let chkFlag = true;

		if (this.param.teamManager == '평가') {
			if (this.param.todoDiv2CodeId === "TODODIV2020" || this.param.todoDiv2CodeId === "TODODIV2030") {
				chkFlag = updateQualityResultComment('결과수정');
			} else if (this.param.todoDiv2CodeId === "TODODIV2090") {
				chkFlag = updateIssueComment('결과수정');
			}

			if (!chkFlag) {
				return false;
			}
		}

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

				// dept-id 기준으로 대상 tr 선택
				var $tr = $('tr[data-dept-id="'+ jwt.userId+'"]');
				
				// 각 항목 추출
				var actMh = gPasIntChk($tr.find('input[name="actMh"]').val());
				var requiredMh = $tr.find('input[name="requiredMh"]').val();
				var actTeamManager = $tr.find('input[name="actTeamManager"]').val();
				// var todoCfOpn = $tr.find('textarea[name="todoCfOpn"]').val();
				var todoCfOpn = ($tr.find('textarea[name="todoCfOpn"]').val() ?? '').trim();


				//부서코드 영업, 기술연구소, 구매, 생산팀의 팀장이면 결과 등록시 해당팀의 소요공수 입력 필수임
				// $('#requiredMh').val() == 'YES'   담당팀 투입공수 필수입력 대상임 
				if (requiredMh == 'YES') {
					// 투입공수 150이하 입력만 가능하게 제약
					if (actMh > 150) {
						customAlert('투입공수는 150 이하로 입력해주세요.');
						return false;
					}
				}

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
						, "etcField1"	: actMh			//팀장 작업공수 입력용으로 활용 2025.07.22
						, "actTeamManager"		:  actTeamManager
						, "issNo" 		: $('#issNo').val()
						, "reqNo" 		: $('#reqNo').val()
						, "actDngEval"	: $('#actDngEvalTodo').val()
						, "deptId"		: jwt.deptId.slice(0,5)
						, "sameTimeResultChk" : this.param.sameTimeResultChk || ''
				}

				Object.assign(paramMap, this.param);
				postAjaxSync("/user/wb/wb20/insertApprovalLine", paramMap, null, function(data){
					if(data.resultCode == 200){
						confirmYn = true;
						let todoYn = data.result.todoYn;
						if( todoYn == "Y" || todoCfOpn != '') {		//모든 결재요청이 완료되면 카톡 전송
							paramMap.bigo = "";		//보완요청일경우만 자료가 있음.
							
							sendTodoFinal(paramMap);
						}

					} else {
						customAlert("승인중 오류가 발생 되었습니다.");
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

	//보완요청 ajax
	this.approvalMemoComment = function(param) {
		// dept-id 기준으로 대상 tr 선택
		var $tr = $('tr[data-dept-id="'+ jwt.userId+'"]').first();	
		var todoCfOpn = ($tr.find('textarea[name="todoCfOpn"]').val() ?? '').trim();
			// 조건에 맞는 첫 번째 tr의 data-todokey 값
		const todoKey = $tr.attr('data-todokey');
		if (todoCfOpn == '' ) {
			customAlert('결재의견에 보완요청사항을 입력하고 보완요청 바랍니다.');
			return false;
		}
		var paramMap = {
				  userId 		: jwt.userId
				, todoKey 		: todoKey
				, todoCfOpn 	: todoCfOpn
				, pgmId 		: "WB2001P01"
		}
		postAjaxSync("/user/wb/wb20/insertApprovalMemoComment", paramMap, null, function(data){
			var list = data.result;
			if (list != undefined) {
//			    $.each(list, function (key, val) {  
//			        if ($('#mForm #' + key)[0]) { 	                        	   
//			            if (key == "todoDiv2CodeNm"){
//			                $('.tit').text(val +' 보기');                             
//			            }
//			            $('#mForm #' + key).val(val);
//			         }
//			    });  
			}
			if(data.resultCode == 200){
				list.creatPgm = "WB2001P01";
				list.pgmId = "WB2001P01";
				list.bigo = "보완요청";
				sendTodoFinal(list);
			} else {
				customAlert("보완요청 처리중 오류가 있습니다.  전산실 확인 바랍니다.");
			}
		});

		return true;
	}


	var kakaoErr = [];

	//최종결재시 결재완료 최초작성자(1번)에게 전송
	function sendTodoFinal(param) {
        //message load
        if (kakaoErr.length == 0) {
	        var paramObj = {
	            "userId" : jwt.userId
	            , "codeKind" : "KAKAOMSG"
	        };
	        postAjaxSync("/user/sm/sm02/selectCurrToday", paramObj, null, function(data) {
	            if(data.resultList.length > 0 ) {
	                kakaoErr = data.resultList;
	            }
	        });
        }

		//TODO 결재완료 알림톡
		let paramSend = {
				"tmplatDiv"		  : "TMPLATDIV05"				//발송템플릿관리번호
				, "todoDiv1CodeId": param.todoDiv1CodeId		//공통코드 해당업무 - 결재
				, "todoDiv2CodeId": param.todoDiv2CodeId		//공통코드 해당업무 - 결재 - 발주서
				, "todoDiv1CodeNm": "결재"						//공통코드 해당업무 - 결재 - 발주서
				, "todoDiv2CodeNm": param.todoDiv2CodeNm  		//공통코드 해당업무 - 결재 - 발주서 -- 템플릿 승인 후에는 삭제요망
				, "sanctnDiv2"	  : "결재"						//템플릿 결재구분2    #{sanctnDiv2} 처리 바랍니다.
				, "todoNo"	   	  : param.todoNo
				, "clntCd"		  : "1"							//발신회사는 로그인사용자 회사
				, "creatPgm"	  : param.pgmId
				, "sanctnSn"	  : "1"							//결재순번 1번을 결재상신자로 본다
				, "todoKey"		  : param.todoKey				//결재 고유 등록번호
				, "todoTitl"	  : param.todoTitl
				, "bigo"		  : (param?.bigo ?? '').trim()	//list.bigo = "보완요청";  결제 버튼 보완요청 처리시에만 전달됨
		}
		commKaKaoSendTodo(paramSend);
	}

	//to-do결재완료 메세지 알림톡
	function commKaKaoSendTodo(param) {
		//알림톡 API헤더 정보
		let talkParam = {
			authToken 	: "e/eDfZOFCsrBqadaECQ0+g==",
			serverName 	: "gyitt2400",
			paymentType : "P",
		};
		let talkBody = {
			service :"2310086157",		//서비스번호(1000000000 - 예시  real : 2310086157
			template: "10009",			//템플릿관리화면 템플릿ID	- 10004 번 결재완료시 템플릿으로 교체요망
	// 		buttons : [{				//알림톡 타이틀, 버튼 설정 필수
	// 		        name		: "웹링크버튼",
	// 		        type		: "WL",
	// 		        url_pc		: "http://pc.com/111",
	// 		        url_mobile	: "http://moible.com/222"
	// 		      }]
		};
		let sendCnt = 0;

		postAjaxSync("/user/bm/bm18/selectMaxMessageIdTodoCompleted", param, null, function(data) {

			if (data.resultList.length == 1 ) {
				if( data.resultList[0].maxMessageId == "" ) {
					alert("알림톡 발송을 위한 messageId 생성에 실패 했습니다.");
					return false;
				}
				let sendObj = data.resultList[0]; 	//결재완료 통보대상자 정보

				let mobile = sendObj.telNo; 		//수신인 휴대전화번호
				if(mobile == "" || mobile == null) {
					return false;
				}

				//message 치환
				let message = sendObj.messageDesc;	//발송될 내용 template
				if(message == "" || message == null) {
					return false;
				}
	// 			let title = sendObj.name + " " + param.todoDiv2CodeNm   //sendObj.todoTitl;
				let title = "[" + param.todoDiv2CodeNm + " " + param.todoNo + "]"   //sendObj.todoTitl;
				if (title.length > 50) {
					title = title.substring(0, 49); // 50자까지만 자르기
				}
				title += " " + param.sanctnDiv2;
				param.rcvId = sendObj.todoId;			//todo 수신id
				param.rcvNm = sendObj.name;				//todo 수신자명
				// param.nameTo = jwt.userNm;				//from 발신자명
				param.nameTo = sendObj.name;			//todo 수신자명
				param.sendDt = sendObj.sendDt;
				param.mobile = sendObj.telNo;			//수신번호
	// 			param.bussinessCnts = sendObj.todoTitl + " " + param.cnts;
				param.bussinessCnts = `${(sendObj?.todoTitl ?? '').toString().trim()} ${(param?.cnts ?? '').toString().trim()}`.trim();
				param.cnts = title;

				const bigo = (param?.bigo ?? '').trim();	//보완요청건인지 체크하기 위함, "보완요청" --> 정상처리시 공백
				if (sendObj.todoCfOpn){
					if (bigo) {
						param.confirmCnts = `[${jwt.userNm} 보완요청 결재보류 코멘트: ${sendObj.todoCfOpn}]\n보류 확인`;
						param.cnts = title + " 보류 보완 요청";
					} else {
						param.confirmCnts = `[${jwt.userNm} 완료 코멘트: ${sendObj.todoCfOpn}]\n결재 확인`;
						param.cnts = title + " 확인 요청";
					}
				} else {
					param.confirmCnts = `[${jwt.userNm} ${param.sanctnDiv2} 완료 확인`;
					param.cnts = title + " 요청";
				}
				var todayDate = new Date().format('yyyy-MM-dd HH:mm');
				param.endDt = todayDate;
	// 			let splitStr = Array.from(message.matchAll('\\{(.*?)\\}'), match => `${match[0]}`);
	// 			//loop
	// 			$.each(splitStr, function (key, val) {
	// 				let compKey = val.substring(1, val.length-1);
	// 				if( compKey != "" && compKey != "undefined" ) {
	// 					if( typeof(param[compKey]) != "undefined" ) {
	// 						let val2 = '#'+val;
	// 						message = message.replaceAll(val2, param[compKey]);
	// 					}
	// 				}
	// 			});
				/*  내용 : #{cnts}
					업무 : #{bussinessCnts}
					요청일시 : #{sendDt}
					요청자 : #{rcvNm}
					#{confirmCnts} 바랍니다.
					(주)건양아이티티/051-312-2400
					
					내용 : [발주/출장요청 REQ2503169] 결재 요청 건
					업무 : 지상거래처,AK,출도(정상), 정상[발주/출장요청REQ2503169]
					요청일시 : 2025-10-31 14:46
					요청자 : 최지상
					[남장섭 보완요청 코멘트(결재보류):현재 대응상황 점검 확인후 별도 보고 바람.] 확인 바랍니다.
					(주)건양아이티티/051-312-2400
					*/
				// `{key}` 패턴을 찾아 `param` 객체에서 대응하는 값을 치환
				Array.from(message.matchAll(/\{(.*?)\}/g), match => match[1]).forEach(compKey => {
					if (compKey && param[compKey] !== undefined) {
						message = message.replaceAll(`#{${compKey}}`, param[compKey]);
					}
				});
				talkBody.messageId = sendObj.maxMessageId;			//고객사에서 관리하는 메시지No - 40byte (unique 값);
				talkBody.mobile = mobile;				//휴대전화번호
				talkBody.title = param.cnts;					//알림톡 타이틀 -
				talkBody.message = message;				//알림톡 메시지 (1000 byte)
	// 			talkBody =  {"service": "2310086157","title":"[최지상 발주/출장요청REQ2503169] 자료보완 요청","message": "내용 : 최지상 지상거래처,AK,출도(정상), 정상[발주/출장요청REQ2503169] 요청 건\n업무 : 지상거래처,AK,출도(정상), 정상[발주/출장요청REQ2503169]\n\n요청일시 : 2025-10-31 14:46\n요청자 : 남장섭\n\n[최지상 보완요청 코멘트(결재보류):현재 대응상황 점검 확인후 별도 보고 바람.] 확인 바랍니다.\n\n(주)건양아이티티/051-312-2400","messageId":"KKO25053097","mobile": "01063393764","template": "10009","buttons":[{"name":"웹링크버튼","type":"WL","url_pc":"http://pc.com/111","url_mobile":"http://moible.com/222"}]}
				/*  최종 전대 메시지 JSON 형태
					{
						"service": "2310086157",
						"template": "10009",
						"messageId": "KKO25053072",
						"mobile": "01063393764",
						"title": "자료보완 요청",
						"message": "내용 : 최지상 지상거래처,AK,출도(정상), 정상[발주/출장요청REQ2503169] 요청 건\n업무 : 지상거래처,AK,출도(정상), 정상[발주/출장요청REQ2503169]\n\n요청일시 : 2025-10-31 14:46\n요청자 : 남장섭\n\n[최지상 보완요청 코멘트(결재보류):현재 대응상황 점검 확인후 별도 보고 바람.] 확인 바랍니다.\n\n(주)건양아이티티/051-312-2400",
					}
				*/
				
				let talkJson = JSON.stringify(talkBody);
				sendCnt = kakaoSendReal(talkJson, talkParam, param);
			}
			
		});	//user search ajax end
				
		//대상 loop end
		if( sendCnt > 0 ) {
	// 		alert("알림톡 정상 발송되었습니다.");
		}
}
}