
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
	this.boldFont = "font-weight:bold; color:blue;";
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
			console.log('---html make');
			var htmlId = htmlParam.htmlArea;
			var htmlTable = `
			<div class="" style="display: block; width: 420px; height: 100%; margin-bottom: 30px; border:1px solid #eee; overflow-y:scroll; padding-bottom:5px;">
		        <h3 class="location">
		          <span class="page_tit" style="text-align: left;">결재</span>
		        </h3>
				<!--결재 테이블 -->
		        <div clss="contents" id="applist" style="height: 100%; padding: 5px">
			    	<!-- 결재라인 table -->
			    	<table border="1" id="appLine">
			    		<colgroup>
			    			<col width="15%">
			    			<col width="35%">
			    			<col width="15%">
			    			<col width="35%">
			    		</colgroup>
			    		<tr id="appH" stye="text-align:center; border-bottom:1px solid #dbdbdb;">
			    			<th class="appTh">순번</th>
			    			<th class="appTh">결재자</th>
			    			<th class="appTh">상태</th>
			    			<th class="appTh">결재일자</th>		    			
			    		</tr>
			    	</table>
			    	<div class="add_btn_small pdl10" style="float:right; padding-top:10px;" id="appBtnDiv">
					<a style="height: 30px; line-height: 28px; width: 90px;" id="appConfirmAnchor" onclick="approvalConfirm();" >결재승인</a>
					</div>
		        </div>			        
				<!--결재 테이블 end-->		        
		    </div>		 			
			`;
			
			$("#"+htmlId).html('');
			$("#"+htmlId).append(htmlTable);
			
			//결재라인 read
			postAjaxSync("/user/wb/wb20/selectGetApprovalList", this.param, null
				, function(data){
					var list = data.resultList;
					//var m = data.resultList.length;
	 				if( data.resultList.length > 0 ) {
	 					
	 					var htmlTr = "";
				        $.each(list, function (idx, data) {
		 					var html = trTempl;				        	
							//html = html.replace(/@@item1@@/gi, (idx+1));		//순번
							html = html.replace(/@@item1@@/gi, data.sanctnSn);		//순번
							html = html.replace(/@@item2@@/gi, data.todoNm);		//결재자명
							//본인해당시 볼드처리 - button 활성화
							if( data.todoId != "undefined" && (data.todoId == jwt.userId ) ) {
								html = html.replace(/@@bold@@/gi, boldFont);
								todoId = data.todoId;
								//applyBtn SHOW - 순번이 1이거나 이전 결재 상태가 Y일 경우							
								if( data.sanctnSttus != "undefined" && data.sanctnSttus == "N"  ) {
									if( data.sanctnSn == "1" || data.preSttus=="Y") {
										applyBtn = true;
										approvalParam.todoKey = data.todoKey;
										approvalParam.sanctnSn = data.sanctnSn;
										approvalParam.coCd = data.coCd;
										approvalParam.todoDiv1CodeId = data.todoDiv1CodeId;
										approvalParam.todoDiv2CodeId = data.todoDiv2CodeId;										
									} 
								}
							}
							html = html.replace(/@@item3@@/gi, data.sanctnSttusNm);		//상태명
							html = html.replace(/@@item4@@/gi, data.todoCfDt);		//확인(결재)일자
							htmlTr += html;
						});			
					} 
	 				$("#appLine tr").eq(0).next().remove();
	 				console.log('--htmlTr--'+htmlTr);
					$("#appLine").append(htmlTr);
	 				
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
    		<tr>
    			<td>@@item1@@</td>
    			<td><font style="@@bold@@">@@item2@@</font></td>
    			<td>@@item3@@</td>
    			<td>@@item4@@</td>
    		</tr>		
			`;
		return html;
	}
	
	//승인 ajax
	this.confirmApproval = function(param) {
		
		var confirmYn = false;	
		//승인 save
		if( this.applyBtn ) {
			var paramMap = {		
					"todoId" : jwt.userId
			}
			Object.assign(paramMap, this.param);
			
			postAjaxSync("/user/wb/wb20/insertApprovalLine", paramMap, null, function(data){
				if(data.resultCode == 200){
					alert("승인 되었습니다.");
					confirmYn = true;
				} else {
					alert("승인중 오류가 발생 되었습니다.");	
				}
			});	
			if( confirmYn ) {
				this.makeHtml();
				this.applyBtn = false;
				this.applyBtnCtrl();
			}
		} else {
			return;
		}        
	}		
}