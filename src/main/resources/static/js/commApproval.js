
// PM51 순차결재 대상 결재구분. 값이 있으면 "선행되어야 하는 신청부서 결재구분"을 의미한다.
// (관리부서 결재는 신청부서 결재가 모두 완료되어야 진행 가능 - 서버 validatePm51SequentialApproval과 동일 기준)
var PM51_SEQ_GENERAL_OF = {
	'TODODIV2190': '', 'TODODIV2200': '',					// 신청부서(출장신청서/출장복명서)
	'TODODIV2191': 'TODODIV2190', 'TODODIV2201': 'TODODIV2200'	// 관리부서(출장신청서/출장복명서)
};

// PM51(출장신청서 TODODIV2190/2191, 출장복명서 TODODIV2200/2201) 순차결재 진입 안내.
// 본인 차례가 아니면 blocked=true(결재버튼 차단)와 함께 순차 순서상 가장 앞선 미결자를 지목한다.
// 관리부서(2191/2201) 결재자는 신청부서(2190/2200) 결재가 먼저 완료되어야 하므로 신청부서 미결자를 우선 안내한다.
// 대상 문서가 아니거나 본인 차례이면 빈 결과를 반환하므로 타 모듈에는 영향이 없다.
function pm51SequentialNotice(list, colSpan) {
	var result = { blocked: false, message: '', html: '' };
	if (!list || list.length === 0) return result;

	var myRow = null;
	$.each(list, function(idx, row) {
		if (row.todoId == jwt.userId) { myRow = row; return false; }
	});
	if (!myRow || !PM51_SEQ_GENERAL_OF.hasOwnProperty(myRow.todoDiv2CodeId)) return result;
	if (myRow.sanctnSttus == 'Y') return result;	//본인 승인 완료 건(결재의견 수정)은 제한하지 않는다.

	var generalDiv = PM51_SEQ_GENERAL_OF[myRow.todoDiv2CodeId];	//관리부서 결재구분이면 선행 신청부서 결재구분
	var isMngApprover = !!generalDiv;

	//순차 순서(신청부서 -> 관리부서)대로 본인 차례 앞의 미결자를 모두 모은다.
	var pendingList = [];
	if (isMngApprover && myRow.todoNo) {
		$.each(pm51PendingRowsAsc(pm51FetchApprovalLine(myRow.todoNo, generalDiv), 0), function(idx, row) {
			pendingList.push({ lineNm: '신청부서', row: row });
		});
	}
	// 결재함 진입 파라미터(todoKey/sanctnSn)에 따라 list에 본인 행 1건만 담겨 오는 경우가 있으므로,
	// 같은 결재선의 앞 순번 판정은 문서번호+결재구분으로 전체를 다시 조회해서 수행한다.
	var ownRows = pm51FetchApprovalLine(myRow.todoNo, myRow.todoDiv2CodeId);
	if (ownRows.length === 0) ownRows = list;
	$.each(pm51PendingRowsAsc(ownRows, Number(myRow.sanctnSn || 0)), function(idx, row) {
		pendingList.push({ lineNm: isMngApprover ? '관리부서' : '신청부서', row: row });
	});
	if (pendingList.length === 0) return result;

	//순차상 가장 앞선 미결자를 지목하고, 미결자가 여러 명이면 순서대로 함께 안내한다.
	var firstItem = pendingList[0];
	var firstNm = pm51NameWithJik(firstItem.row);
	var firstDesc = (isMngApprover && firstItem.lineNm === '신청부서') ? '신청부서 결재자' : '이전결재자';
	var particle = pm51SubjectParticle(firstNm);

	result.blocked = true;
	// 본인이 바로 다음 차례(앞의 미결자가 1명)이면 해당 결재자를 지목해서 알리고,
	// 부사장처럼 앞에 여러 명이 남아 있으면 특정인을 지목하지 않고 진행 불가 사유만 알린다.
	// 미결자 전체 목록은 결재선 아래 안내문구(html)에 계속 표시된다.
	var closingMsg = '이전 결재가 완료되어야 결재를 진행할 수 있습니다.';
	result.message = (pendingList.length === 1)
		? (firstDesc + ' ' + firstNm + particle + ' 승인하지 않은 상태입니다.\r\n' + closingMsg)
		: closingMsg;

	var htmlList = '';
	if (pendingList.length > 1) {
		$.each(pendingList, function(idx, item) {
			htmlList += '<br>' + (idx + 1) + '. ' + item.lineNm + ' ' + item.row.sanctnSn + '순번 ' + pm51NameWithJik(item.row);
		});
		htmlList = '<br><b>[미결 결재자]</b>' + htmlList;
	}
	result.html = '<tr><td colspan="' + colSpan + '" style="text-align:left; padding:8px; color:#d9534f; background:#fff8f8; border-top:1px solid #dbdbdb;">'
		+ firstDesc + ' <b>' + firstNm + '</b>(순번 ' + firstItem.row.sanctnSn + ')' + particle + ' 승인하지 않은 상태입니다.<br>'
		+ '이전 결재가 완료되어야 결재를 진행할 수 있습니다.'
		+ htmlList
		+ '</td></tr>';
	return result;
}

// 지정한 문서번호/결재구분의 결재선 전체를 조회한다. (결재함 진입 파라미터에 따라 화면에 넘어온 목록이
// 본인 행만 담고 있을 수 있으므로, 순차 판정은 항상 이 조회 결과를 기준으로 한다.)
function pm51FetchApprovalLine(todoNo, todoDiv2CodeId) {
	if (!todoNo || !todoDiv2CodeId) return [];
	var rows = [];
	postAjaxSync("/user/wb/wb20/selectGetApprovalList", { todoNo: todoNo, todoDiv2CodeId: todoDiv2CodeId }, null, function(data) {
		rows = data.resultList || [];
	});
	return rows;
}

// 미결(sanctnSttus!=='Y') 결재행을 순번 오름차순으로 반환. maxSn을 주면 그 순번보다 앞선 행만 대상으로 한다.
function pm51PendingRowsAsc(rows, maxSn) {
	var pending = [];
	$.each(rows || [], function(idx, row) {
		if (row.sanctnSttus == 'Y') return;
		var sn = Number(row.sanctnSn || 0);
		if (!sn) return;
		if (maxSn && sn >= maxSn) return;
		pending.push(row);
	});
	pending.sort(function(a, b) { return Number(a.sanctnSn || 0) - Number(b.sanctnSn || 0); });
	return pending;
}

// 결재자 표기: 이름 뒤에 직급을 붙인다. (예: 홍길동팀장) 직급이 없으면 이름만 사용한다.
function pm51NameWithJik(row) {
	var nm = $.trim((row && (row.todoNm || row.todoId)) || '');
	var jik = String((row && row.jik) || '').replace(/\s/g, '');	//직급 가운데 공백까지 제거
	return jik ? (nm + jik) : nm;
}

// 주격조사 선택: 마지막 글자에 받침이 있으면 '이', 없으면 '가'. (예: 홍길동팀장이 / 김영희대리가)
function pm51SubjectParticle(text) {
	var last = (text || '').slice(-1);
	var code = last.charCodeAt(0);
	if (code >= 0xAC00 && code <= 0xD7A3) {
		return ((code - 0xAC00) % 28) > 0 ? '이' : '가';
	}
	return '이';
}

//결재승인 버튼
function approvalConfirm() {
	commApproval.confirmApproval();
}

//보완요청 버튼
function approvalMemoComment() {
	commApproval.approvalMemoComment();
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
			    			<col width="8%">
			    			<col width="*%">
			    			<col width="10%">
			    			<col width="15%">
			    		</colgroup>
			    		<tr id="appH" stye="text-align:center; border-bottom:1px solid #dbdbdb; height:25px;">
			    			<th class="appTh">순번</th>
			    			<th class="appTh">결재자</th>
			    			<th class="appTh">투입공수</th>
			    			<th class="appTh">결재의견</th>
			    			<th class="appTh">상태</th>
			    			<th class="appTh">결재일자</th>
			    		</tr>
			    	</table>
			    	<div class="popup_bottom_btn" id="appBtnDiv">
						<button id="appConfirmAnchor"  onclick="approvalConfirm();">결재승인</button>
						<button id="appConfirmAnchor"  onclick="approvalMemoComment();">보완요청</button>
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
							html = html.replace(/@@todoKey@@/g, data.todoKey); // 각 행의 부서 코드
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
								//PM51(출장신청서 TODODIV2190/2191, 출장복명서 TODODIV2200/2201)은 순차결재 문서이므로
								//차례가 아닌 결재자에게는 결재버튼을 노출하지 않는다(서버 validatePm51SequentialApproval과 동일 기준).
								//단, 본인이 이미 승인한 건은 결재의견 수정을 위해 버튼을 유지한다.
								var pm51SeqDivs = ["TODODIV2190", "TODODIV2191", "TODODIV2200", "TODODIV2201"];
								var isPm51Seq = ($.inArray(data.todoDiv2CodeId, pm51SeqDivs) > -1);
								if (isPm51Seq) {
									if (data.sanctnSttus == "Y") applyBtn = true;	//의견수정
								} else {
									//결재문서가 수주목표가 결재가 아니면 순서 상관없이 결재처리 가능함. 20240625 남장섭
									if (data.todoDiv2CodeId != "TODODIV2100") {
										applyBtn = true;
									}
									//다음순번이 미결재일 경우 결재의견 가능하게 변경
									if( data.nextSttus=="N") applyBtn = true;
								}
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
//					 				if( data.todoDiv2CodeId=='TODODIV2090' && data.teamManager == 'TEAM01' ) {
//					 					confrmActDngEval = actDngEval;
//					 				}
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
							// 7. 정상건은 투입공수 입력패스 (정상출도, A/S유상, Spare유상, 고객E/O, 설치시운전 정상)
							let editable = false; // 투입공수 입력여부 플래그
							// 본인결재건이면서 팀장일때
							if (applyBtn && (data.deptTeamManager === 'TEAM01' || data.teamManager === '평가' || data.teamManager === 'Y') && jwt.userId === data.todoId && ['GUN30', 'GUN40', 'TRN50', 'GUN60', 'GUN70'].includes(jwt.deptId.slice(0, 5))) {
								if  ((data.todoDiv2CodeId === 'TODODIV2020' && (sameTimeResultChk === 'Y' || data.sameTimeResult == 'Y') && !['COBTP01','COBTP04','COBTP06','COBTP08','COBTP09'].includes(data.partCd)) 
									|| (data.todoDiv2CodeId === 'TODODIV2090') || (data.todoDiv2CodeId === 'TODODIV2030' && !['COBTP01','COBTP04','COBTP06','COBTP08','COBTP09'].includes(data.partCd))){
									editable = true;
								}
							}
							// 투입공수 입력가능
							if (editable) {
								html = html.replace(
									/@@item3@@/gi,
									`<input type="text" name="actMh" class="form-control" value="${gPasFloatChk((data.actMh)) || ''}" style='text-align:center; padding-right:5px; height:40px;' comma onkeyup="onlyNumber(this)" required msg="투입공수">
									 <input type="hidden" name="actTeamManager" value="${data.deptTeamManager}">
									 <input type="hidden" name="requiredMh" value="YES">`
								);
							} else {
								// 읽기전용
								html = html.replace(
									/@@item3@@/gi,
									`<input type="text" name="actMh" readonly value="${gPasFloatChk((data.actMh)) || ''}" style='text-align:center; padding-right:5px; height:40px;' comma>`
								);
							}
							
							// ==============================================================================================
							html = html.replace(/@@item5@@/gi, data.sanctnSttusNm);		//상태명
							html = html.replace(/@@item6@@/gi, data.todoCfDt);		//확인(결재)일자
							htmlTr += html;
						});
					}
					$("#appLine").append(htmlTr);

					//팀장 이슈 조치결과 결재일경우 위험성 평가 기능 추가 하기위함   남장섭 240618
					$("#appLine").append(confrmActDngEval);

					//PM51 순차결재: 본인 차례가 아니면 결재버튼을 차단하고, 진입 시 어느 결재자에서 멈춰 있는지 알린다.
					var pm51Notice = pm51SequentialNotice(list, 6);
					if (pm51Notice.blocked) {
						applyBtn = false;
						$("#appLine").append(pm51Notice.html);
						if (pm51Notice.message) customAlert(pm51Notice.message);
					}

			});		//end ajax

			this.todoId = todoId;
			this.applyBtn = applyBtn;
			//btn control
			this.applyBtnCtrl();
			if( this.applyBtn ) {
				Object.assign(this.param, approvalParam);
			}
		} else {
			customAlert( '결재란 영역을 찾을수 없습니다. \r\nex) <div id="approval_area"></div>');
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
    			<td class="appTd" style='text-align:left; padding-left:5px; height:25px;'><textarea type='text' name='todoCfOpn' class="form-control" readonly="readonly">@@item4@@</textarea></td>
    			<td class="appTd">@@item5@@</td>
    			<td class="appTd">@@item6@@</td>
    		</tr>
			`;
		return html;
	}

	//승인 ajax
	this.confirmApproval = function(param) {

        $("#mcPartCd").closest('td').prev('th').removeClass('hit');
        $("#mcPartCd").removeAttr('required', 'true');
        $("#impactCd").closest('td').prev('th').removeClass('hit');
        $("#impactCd").removeAttr('required', 'true');
        $("#importantCd").closest('td').prev('th').removeClass('hit');
        $("#importantCd").removeAttr('required', 'true');

		// dept-id 기준으로 대상 tr 선택
		var $tr = $('tr[data-dept-id="'+ jwt.userId+'"]');
		
		// 각 항목 추출
		var actMh = gPasFloatChk($tr.find('input[name="actMh"]').val());
		var requiredMh = $tr.find('input[name="requiredMh"]').val();
		var actTeamManager = $tr.find('input[name="actTeamManager"]').val();
//		var todoCfOpn = $tr.find('textarea[name="todoCfOpn"]').val();
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
		//승인 save
		if( this.applyBtn ) {
			//입력값 set
			var paramMap = {
					  "todoId" 		: jwt.userId
					, "todoCfOpn" 	: todoCfOpn
					, "actMh"		: actMh
					, "etcField1"	: actMh			//팀장 작업공수 입력용으로 활용 2025.07.22
					, "actTeamManager"		:  actTeamManager
					, "issNo" 		: $('#issNo').val()
					, "reqNo" 		: $('#reqNo').val()
					, "actDngEval"	: $('#actDngEvalTodo').val()
					, "deptId"		: jwt.deptId.slice(0,5)
					, "sameTimeResultChk" : this.param.sameTimeResultChk || ''
			}
			let anchorText = $("#appConfirmAnchor").text();
			let confirmText = (anchorText == "의견수정") ? "수정" : "승인"

			Object.assign(paramMap, this.param);

			postAjaxSync("/user/wb/wb20/insertApprovalLine", paramMap, null, function(data){
				if(data.resultCode == 200){
					confirmYn = true;
					let todoYn = data.result.todoYn;
					if( todoYn == "Y" || todoCfOpn != '') {		//모든 결재요청이 완료되면 카톡 전송
						paramMap.bigo = "";		//보완요청일경우만 자료가 있음.
						// PFU 공유 등록 결과는 insertApprovalLine 응답으로 함께 처리
						if (data.result.pfuShareTargetYn == "Y" && data.result.pfuShareResultCode == "200") {
							sendTodoPfuShare(paramMap);

						}
						sendTodoFinal(paramMap);
					} else if (typeof notifyPm51NextApprover === 'function'
							&& typeof PM51_SEQUENTIAL_DIV_MAP !== 'undefined'
							&& PM51_SEQUENTIAL_DIV_MAP.hasOwnProperty(paramMap.todoDiv2CodeId)) {
						// PM51 순차결재: 중간 결재자가 결재의견 없이 승인하면 sendTodoFinal()이 호출되지 않아
						// 다음 차례 결재자에게 결재요청 알림톡이 발송되지 않던 문제 보완.
						var pm51HasNext = notifyPm51NextApprover(paramMap.todoNo, paramMap.todoDiv2CodeId, paramMap.pgmId);
						if (!pm51HasNext) {
							// 신청부서(개인) 결재가 모두 완료됨 -> 관리부서 결재 1번 순번에게 시작 알림
							notifyPm51NextApprover(paramMap.todoNo, PM51_SEQUENTIAL_DIV_MAP[paramMap.todoDiv2CodeId], paramMap.pgmId);
						}
					}
					//결재처리 완료 - 상단 결재미완료/공유미확인 건수 갱신 (isForce=true로 캐시 무시 및 타 탭 전파)
					if (typeof myTodoStatusRtv === 'function') {
						myTodoStatusRtv(true);
					}
				} else {
					customAlert(data.resultMessage || "승인중 오류가 발생 되었습니다.");
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
}
