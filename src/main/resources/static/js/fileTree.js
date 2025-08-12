var treeModule = (function () {var fileTreeGridView
var treeComonCd;
var approvalWorkingGrid; //팝업화면에서 결재정보 저장용
	var fileArr=[];
	var deleteFileArr = [];
	var fileTreeParamObj;
	var currPgmAuthChk = true; // true:저장권한, false:저장권한 없음
	var fileTempCocd = '';
	var fileTree_fileList_area = '';
	//최초 진입시점---
	//   1. 파일트리명 = 각 화면의 트리구조를 보여줄 div테그 ID ex) <div id="deptTree" ></div>
	//   2. 그리드명  = 각 화면의 ax5-grid Html Tag ID  ex) <div id="my-grid" data-ax5grid="file-grid" data-ax5grid-config="{}" style="height: 100%; width: 100%"></div>
	//   3. 파일트리 최상위코드값  =  FIRETREE : 파일트리 최상위 값
	//   4. params 첨부자료가 저장된 타입과 일련번호를 전달한다.
	//      4-1. fileTrgtTyp   = 파일저장타입(프로그램명 또는 테이블명으로 프로젝트내에서 유니크한 값 지정) ex)TB_BM0101M01
	//      4-2. fileTrgtKey   = 파일저장일련번호 (프로그램명 또는 테이블명에서 관리되는 유니크한 일련번호 ex) 1, 2, 3, 4
	//	treeModule.initAll('deptTreeOrdars', 'file-grid', 'FILETREE', fileParam, coCd, popSelector);
	//
	//                'deptTree', 'file-grid', 'FILETREE', paramTreeObj);
	function initAll(selector, gridSelector, codeId, params, _fileList_area, _coCd) {
		//coCd가 파라메터로넘어오면 넘어온 값을 우선 처리 아니면 하면의 coCd로 처리함
		if (_coCd == '' || _coCd == undefined) {
			fileTempCocd = $('#coCd').val();
		} else {
			fileTempCocd = _coCd;
		}

		gridSelector = gridSelector.replace('-',''); 
		//첨부파일영역 설정값이 없으면 기본은 : fileList_area 로 명영함.
		if (_fileList_area == '' || _fileList_area == undefined) {
			_fileList_area = "#fileList_area";
		} else {
			_fileList_area = "#" + _fileList_area;
		}
		fileTree_fileList_area = _fileList_area;
//    	let popSelector = $('.popup_area.of_a:last').attr('id');		//마지막 .popup_area.of_a의  class ID 추출
		let popSelector = '.popup_area:last';		//마지막 .popup_area.of_a의  class ID 추출
		//일정관리에서 이슈등록은 fileList_area영역이 일정관리와 이슈화면영역과 겹침 발생
		// $('#wbsPopupIss').length --> 이슈화면이 활성화 되어 있는지 확인하여
		// 이슈가 활성화 되어있으면 이전 팝업의 파일첨부영역 삭제처리하여 오류 발생 제거함
		if ($('#wbsPopupIss').length || $('#wbsPopupRslt').length) {
			$("#wbsPopup #fileList_area").remove();
		}
		if ($(popSelector + ' ' + _fileList_area + " #fileAttachTxt").length > 0) {
			// console.log("#fileAttachTxt 요소가 존재합니다.");
		} else {
			// console.log("#fileAttachTxt 요소가 존재하지 않습니다.");
			// #fileList_area 요소의 자식으로 새로운 코드를 삽입합니다.
			fileListArea_html_creation(popSelector, _fileList_area, gridSelector, selector);

			createFileInput(gridSelector, selector);
//            treeComonCd = codeId; //무시
			treeComonCd = 'FILETREE';             //최상위 트리 강제로 할당 (파라메터값 무시)
			params["comonCd"] = treeComonCd;      //comonCd는 xML 쿼리에서 트리ID 보관 필드명
			params["userId"] = jwt.userId;        //캐시에 보관된 사용자 ID

			fileTreeParamObj = params;  //
			fileArr=[];
			deleteFileArr = [];

			initDeptTree(popSelector + ' #' + selector, popSelector);

			currPgmAuthChk = authChk(); // true:저장권한, false:저장권한 없음

			fileTreeGridView.init(gridSelector, _fileList_area);

			//--------------------------------------------------------------------
			//To-Do List에서 팝업창을 뛰우면 정보확인하고 결재버튼 클릭시 결재 처리를 위한 버튼 추가  시작
			// 결재, 승인 버튼활성화를 위해서는 modalStack.last().paramObj.gridObj 에 결재 승인을 위한 파라메터 값이 있어야 함.
			//--------------------------------------------------------------------
//        	console.log(modalStack.last().paramObj.gridObj);
			if (modalStack.last() != undefined) {
				approvalWorkingGrid = modalStack.last().paramObj.gridObj; //결재 승인을 위한 파라메터 전역변수에 저장함
				if (approvalWorkingGrid != undefined) { //To-Do List에서 넘어온 작업임
					modalStack.last().paramObj.gridObj = "";	//AJAX 직렬화 하지 않은 배열은 실행오류 발생됨으로 배열변수 Clear 처리함

					//결재전이면 결재승인버튼 추가
					//결재안된 상태이고 User와 결재자가 동일하면 결재버튼 활성화
					if (approvalWorkingGrid.sanctnSttus == "N" && approvalWorkingGrid.todoId == jwt.userId) {
						const actionType = (approvalWorkingGrid.todoDiv1CodeNm == '결재') ? '결재승인' : '공유확인';
						const todoKey = approvalWorkingGrid.todoKey;	//TO-Do 고유번호
						const callCmd = `<button class="callApprovalWorking" onclick="treeModule.callApprovalWorking('${todoKey}', '${approvalWorkingGrid.creatPgm}')">${actionType}</button>`;
						$('#popForm a:has(i.i_search_w)').removeAttr('onclick');  //popForm ID안에 있는 <a>태그중 자식으로 i태그 i_search_w 클래스가 있으면 onclick 제거--> 결재창과 중복 방지를 위함
						$('#popForm a:has(i.i_search_w)').remove();  //I 태그도 삭제
						$('.popup_bottom_btn').last().append(callCmd);
						
						if (approvalWorkingGrid.teamManager) {
							//발주요청처리결과화면이면
							if (params.fileTrgtTyp == 'QM0101P03' || params.fileTrgtTyp == 'QM0101P01') {
								$('#measRst').attr('readonly', false).css({'background-color': '#ffffff', 'color': '#00000'});
								$('#resltRst').attr('readonly', false).css({'background-color': '#ffffff', 'color': '#00000'});
								$('#FDMTSOLUT-radioButtonContainer').css({'background-color':'#ffffff','pointer-events':'auto'}).find('input[type=radio], input[type=checkbox]').off('click').prop('readonly', false).removeAttr('readonly');
								$('#fdmtSolutCnt').attr('readonly', false).css({'background-color': '#ffffff', 'color': '#00000'});
								$('#COBGB-checkboxContainer').css({'background-color':'#ffffff','pointer-events':'auto'}).find('input[type=radio], input[type=checkbox]').off('click');
							} else if (params.fileTrgtTyp == 'WB2401P01' || params.fileTrgtTyp == 'WB2401P11') { //계획문제, 결과문제
								$('#measRst').css('pointer-events', 'auto').prop('readonly', false).css('background-color', '#ffffff');
								$('#actCnts').css('pointer-events', 'auto').prop('readonly', false).css('background-color', '#ffffff');
								$('#FDMTSOLUT-radioButtonContainer').css('pointer-events', 'auto').prop('readonly', false).css('background-color', '#ffffff');
								$('#fdmtSolutCnt').css('pointer-events', 'auto').prop('readonly', false).css('background-color', '#ffffff');
								$('.issAct').removeClass('no-click');
							}

						} else {  //평가자가 아닌경우 필수 입력 필드 제외 처리
							$('#FDMTSOLUT-radioButtonContainer').removeAttr('required').closest('td').prev('th').removeClass('hit');
							$('#fdmtSolutCnt').removeAttr('required').closest('td').prev('th').removeClass('hit');
							$('#actMh').removeAttr('required').closest('td').prev('th').removeClass('hit');
						}
					}
				} else {	//To-Do List가 아닌경우 각 화면에서 결재대상이면 처리하기 위함
					if (params.todoNo != undefined && params.fileTrgtKey != '0' && params.fileTrgtKey != undefined) {
						//-- 각 화면에서 결재자정보에 User 가 있으면 결재, 승인버튼 활성화 하기 위함
						//selectCurrentUserApprovalDataList
						let paramObj = {
								"todoNo" 			: params.todoNo,
								"todoFileTrgtKey" 	: params.fileTrgtKey,
								"salesCd" 			: params.salesCd,
								"etcField2" 		: params.etcField2,
								"userId" 			: jwt.userId,
								"useYn" 			: 'Y',
						}
						
						postAjax("/user/wb/wb20/selectCurrentUserApprovalDataList", paramObj, null, function(data){
							if (data.resultList.length > 0) {
								approvalWorkingGrid = data.resultList[0]; //결재 승인을 위한 파라메터 전역변수에 저장함

								//결재전이면 결재승인버튼 추가
								const actionType = (approvalWorkingGrid.todoDiv1CodeNm == '결재') ? '결재승인' : '공유확인';
								const todoKey = approvalWorkingGrid.todoKey;	//TO-Do 고유번호
								const callCmd = `<button class="callApprovalWorking" onclick="treeModule.callApprovalWorking('${todoKey}', '${params.fileTrgtTyp}')">${actionType}</button>`;

								$('#popForm a:has(i.i_search_w)').removeAttr('onclick');  //popForm ID안에 있는 <a>태그중 자식으로 i태그 i_search_w 클래스가 있으면 onclick 제거--> 결재창과 중복 방지를 위함
								$('#popForm a:has(i.i_search_w)').remove();  //I 태그도 삭제
								$('.popup_bottom_btn').last().append(callCmd);	//마지막 popup_bottom_btn class에서 버튼 추가
								
								if (approvalWorkingGrid.teamManager == '평가') {
									//발주요청처리결과화면이면 수정 가능하게 속성 변경 처리
									if (params.fileTrgtTyp == 'QM0101P03' || params.fileTrgtTyp == 'QM0101P01') {
										const currFormId = (params.fileTrgtTyp == 'QM0101P03') ? '#popForm2' : '#popFormQ01';
										const issCheck = !['COBTP01', 'COBTP04', 'COBTP06', 'COBTP08', 'COBTP09'].includes($(currFormId + " #partCd").val());	// 정상건인지 문제건인지 체크
										if (issCheck && data.resultList[0].teamManagerCheck == 'Y') {
											$(currFormId + ' #FDMTSOLUT-radioButtonContainer').closest('td').prev('th').addClass('hit');
											$(currFormId + ' #fdmtSolutCnt').closest('td').prev('th').addClass('hit');
											$(currFormId + ' #fdmtSolutCnt').attr('required', 'true');
										}
										$(currFormId + ' #measRst').attr('readonly', false).css({'background-color': '#ffffff', 'color': '#00000'});
										$(currFormId + ' #resltRst').attr('readonly', false).css({'background-color': '#ffffff', 'color': '#00000'});
										$(currFormId + ' #FDMTSOLUT-radioButtonContainer').css({'background-color':'#ffffff','pointer-events':'auto'}).find('input[type=radio], input[type=checkbox]').off('click');
										$(currFormId + ' #fdmtSolutCnt').attr('readonly', false).css({'background-color': '#ffffff', 'color': '#00000'});
										$(currFormId + ' #COBGB-checkboxContainer').css({'background-color':'#ffffff','pointer-events':'auto'}).find('input[type=radio], input[type=checkbox]').off('click');
									} else if (params.fileTrgtTyp == 'WB2401P01' || params.fileTrgtTyp == 'WB2401P11') {
										$('#measRst').css('pointer-events', 'auto').prop('readonly', false).css('background-color', '#ffffff');
										$('#actCnts').css('pointer-events', 'auto').prop('readonly', false).css('background-color', '#ffffff');
										$('#FDMTSOLUT-radioButtonContainer').css('pointer-events', 'auto').prop('readonly', false).css('background-color', '#ffffff');
										$('#fdmtSolutCnt').css('pointer-events', 'auto').prop('readonly', false).css('background-color', '#ffffff');
										$('.issAct').removeClass('no-click');
									}
								} else {  //평가자가 아닌경우 필수 입력 필드 제외 처리
									$('#FDMTSOLUT-radioButtonContainer').removeAttr('required').closest('td').prev('th').removeClass('hit');
									$('#fdmtSolutCnt').removeAttr('required').closest('td').prev('th').removeClass('hit');
									$('#actMh').removeAttr('required').closest('td').prev('th').removeClass('hit');
								}
							}
						});
					}
				}
			}
			//--------------------------------------------------------------------
			//To-Do List에서 팝업창을 뛰우면 정보확인하고 결재버튼 클릭시 결재 처리를 위한 버튼 추가  끝
			//--------------------------------------------------------------------
		}
	}


	fileTreeGridView = {
		target: new ax5.ui.grid(),

		init: function (gridSelector) {

			this.target.setConfig({
				target: $("[data-ax5grid=\"" + gridSelector + "\"]"),
				showLineNumber: true,
				showRowSelector: false,
				multipleSelect: false,
				header: {
					selector: false
				},
				body: {
					onClick: function () {
						this.self.select(this.dindex);
					},
					onDBLClick: function () {
						var fileKey = this.item.fileKey;
						if (fileKey) {
							if (this.item.fileDown == 'Y') {
								if (this.column.key == "fileName") {
									//첨부파일 이미지뷰
									let tempType = this.item.fileType.toLowerCase();
									const viewType = ['jpg', 'jpeg', 'png', 'gif', 'pdf', 'heic']
									if (viewType.includes(tempType)) {
										const imageList = this.self.list.filter(item => {
											const fileType = item.fileType.toLowerCase();
											return viewType.includes(tempType);
										});
										imageViewPopup(fileKey, this.item.fileName, imageList);
									} else {
										downLoadFile(fileKey, this.item.fileName);
									}
								} else {
									downLoadFile(fileKey, this.item.fileName);
								}
							} else {
								alert('파일 다운로드 권한이 없습니다.');
							}
						}
					},
				},
				columns: [

					{key: "fileTrgtKey", 	label: "파일타겟키", 		hidden: true},
					{key : "nodeId",  		label : "nodeId",	 	width : 120, align: "center", hidden: true},
					{key : "fileUp",  		label : "up",	 		width : 60, align: "center", hidden: true},
					{key : "fileDown",  	label : "down",	 		width : 60, align: "center", hidden: true},
					{key : "fileUpdate",  	label : "update",	 	width : 60, align: "center", hidden: true},
					{key : "fileDelete",  	label : "delete",	 	width : 60, align: "center", hidden: true},
					{key: "fileKey", 		label: "파일키", 			hidden: true},
					{key: "fileName", 		label: "파일명", 			width: 260, align: "left"},
					{key: "fileType", 		label: "종류", 			width: 60, align: "center"},
					{key: "fileSize", 		label: "파일크기", 		width: 90, align: "right", formatter: "money"},
					{key: "creatDttm", 		label: "저장일자", 		width: 130, align: "center",},
					{key: "creatNm", 		label: "등록자", 			width: 60, align: "center"},
					{key: "clntCd", 		label: "거래처", 			width: 50, align: "center", hidden: true},
					{key: "clntNm", 		label: "거래처명", 		width: 110, align: "center", hidden: true},
					{key: "prdtCd", 		label: "제품코드", 		width: 50, align: "center", hidden: true},
					{key: "prdtNm", 		label: "제품명", 			width: 100, align: "center", hidden: true},
					{key: "itemCd", 		label: "아이템", 			width: 50, align: "center", hidden: true},
					{key: "salesCd", 		label: "Sales코드", 		width: 110, align: "center"},
					// {key: "prjctCd", label: "프로젝트", width: 50, align: "center", hidden: true},
					// {key: "prjctNm", label: "프로젝트명", width: 110, align: "center"},
					{key : "lpath",  		label : "저장위치",	 	width : 160, align: "left", hidden: false},
					{key: "fileDelete", 	label: "삭제", 			width: 60, align: "center",
						formatter:function() {return (this.item.fileDelete == "Y" || this.item.fileKey == 0 ) && currPgmAuthChk ? '<button style="height: 18px; padding:0px;" type="button" onclick="treeModule.deleteFile('+this.dindex+')" authchk>삭제</button>' : '불가'}
					}],
				page: {
					display: false
				}
			});
			this.target.$target.on("click", "[data-delete-row]", function (event) {
				const rowIndex = parseInt(event.target.getAttribute("data-delete-row"), 10);
				deleteFile(rowIndex);
			});

			return this;
		},
		reqSetData: function (list) {
			var targetObj = this.target;
			//첨부 자료가 있으면 파일 영역 표시함
			if (list.length > 0) {
				$(fileTree_fileList_area + " #fileAttachTxt").hide();
				$(fileTree_fileList_area + " #fileAttachCnts").show();
			};

			targetObj.setData({
				list: list,
				page: {
					totalElements: list.length
				}
			});
		}
	}

	function initDeptTree(selector, popSelector) {	//selector = '.popup_area:last #deptTree'
		$(selector).jstree('destroy');
		$(selector).jstree(
			{
				plugins: ['types'],
				core : {
					check_callback: true,
					data : selectDeptTree(),

					check_callback: function (operation, node, node_parent, node_position, more) {
						if (operation === 'delete_node') {
							if (node.original.isLeaf == 0) {
								return true; // 삭제가능
							} else {
								return false; // 살제불가
							}
						}
						// 삭제가능
						return true;
					},
					types : {
						'leaf' : {'icon' : 'glyphicon glyphicon-file'},
						'unit' : {'icon' : 'glyphicon glyphicon-folder-close'},
						'unit-open' : {'icon' : 'glyphicon glyphicon-folder-open'}
					}
				}
			}).on("loaded.jstree", function () {
			// 루트 노드 로드 완료 시
			//전체 노드 펼침
//            $(selector).jstree("open_all");
			// 최상위 노드 펼침
			$(selector).jstree(true).open_node($(selector + ' li[aria-level="1"]').eq(0).attr('id'));

			var topLevelNode = $(selector + ' li[aria-level="1"]').eq(0).attr('id');
			$(selector).jstree(true).deselect_all();
			$(selector).jstree(true).select_node(topLevelNode);

			var tree = $(selector).jstree(true);
			const topNode = 'FILETREE';
			const rootNode = tree.get_node(topNode);

			//자식노드중에 isLeaf가 1인 자식인 하나도 없으면 트리목록 삭제 처리
			searchAndDelete(tree, topNode, topNode);

		})
			.on("refresh.jstree", function () {
				// 리프레시 완료 시
				var selectedId = $(selector).jstree(true).get_selected()[0];
				$(selector).jstree(true).deselect_all();
				$(selector).jstree(true).select_node(selectedId);
			})
			.on("select_node.jstree", function (e, data) {
				// 노드 선택 시 발생 이벤트

				let targetTree = $(selector).jstree('get_selected',true)[0];
				if (targetTree == undefined) {
					$(popSelector + " #file_tag").html("파일저장Tree에는 문서를 저장할 수 없습니다.");
					treeComonCd = "FILETREE";
				} else {
					selectedNodeId = data.node.id;
					selectedNodeText = data.node.text;
					selectedNodeEtc = data.node.original.codeEtc;
					var buttonFile = $(popSelector + " #button_file");
					if (buttonFile.length) {
						if (selectedNodeEtc == 'N') {
							buttonFile.hide();
						} else {
							buttonFile.show();
						}
					}
					let txt = selectedNodeText + ( (selectedNodeEtc =="N") ? "에는 저장할 수 없습니다." : "" );
					$(popSelector + " #file_tag").html(txt);
					treeComonCd =  selectedNodeId;
				}

				if (!$(selector).is(":visible")) {
					$(selector).show();
				}
				getAllFilesForNodes(treeComonCd);

				// 노드 선택 시
			}).on('hover_node.jstree',function(e, data){
				// 마우스 위치할때
				if (data.node.original.codeEtc == 'N') {
					$(selector + " #"+data.node.id).attr("title","자료를 저장할 수 없는 폴더입니다.");
				} else {
					$(selector + " #"+data.node.id).attr("title","");
				}
			}).on('dblclick.jstree', function (e, data) {
			//선택된 노드의 값을 가져와서 처리한다.
			var selectedNode = $(selector).jstree('get_selected');
			if (selectedNode.length > 0) {
				var selectedNodeData = $(selector).jstree('get_node', selectedNode[0]).original;

				// 선택된 노드의 속성 값을 가져오기
				if (selectedNodeData.codeEtc == 'N') {
					alert('"' + selectedNodeData.text + '" 폴더는 자료를 저장할 수 없는 폴더입니다.');
				} else {
					file.click();
				}
			}

			}).on('open_node.jstree', function (e, data) {
				// 노드 열릴 때
				data.instance.set_type(data.node, 'unit-open');
			}).on('close_node.jstree', function (e, data) {
			// 노드 닫힐 때
			data.instance.set_type(data.node, 'unit');
		})
	}


	function createFileInput(gridSelector, selector) {
		// 새로운 input 요소 생성
		var fileInput = document.createElement('input');

		// input 속성 설정
		fileInput.type = 'file';
		fileInput.id = selector +'file';
		fileInput.style.display = 'none';
		fileInput.multiple = 'multiple';

		// 이벤트 리스너 추가
		fileInput.addEventListener('change', function () {
			addFileToTree(this);
		});


		// 생성된 input 요소를 grid 요소 바로 다음에 추가
		var gridElement = document.querySelector('[data-ax5grid="' + gridSelector + '"]');
		if (gridElement) {
			gridElement.parentNode.insertBefore(fileInput, gridElement.nextSibling);
		} else {
			console.error('Grid element not found with provided selector:', gridSelector);
		}
	}

	//트리 선택시 서버에 등록된 첨부 화일을 가져와서 그리드에 출력하기 위한 작업
	function getAllFilesForNodes(nodeId) {
		if (!fileTreeParamObj.fileTrgtTyp || !fileTreeParamObj.fileTrgtKey) {
			return false;
		}
		fileArr=[];
		tempObj = fileTreeParamObj;
		tempObj["pageNo"] = 1;
		tempObj["recordCnt"] = 99999999;
		if (nodeId) {
			tempObj["comonCd"] = nodeId;
		}
//		tempObj["userId"] = jwt.userId; //initAll에서 처리
		postAjax("/admin/cm/cm08/selectTreeFileList", tempObj, null, function (data) {
			fileArr = data.fileList;
			fileTreeGridView.reqSetData(fileArr);
		});
	}

	function selectDeptTree() {
		var deptTree = null;
		fileTreeParamObj["coCd"] = fileTempCocd;
		fileTreeParamObj["userId"] = jwt.userId;
		fileTreeParamObj["useYn"] = 'Y';

		postAjaxSync("/admin/cm/cm05/selectDocTreeListAuth", fileTreeParamObj, null, function(data){
			deptTree = data.docTreeList;
		});
		return deptTree;
	}

	function addFileToTree(elem) {
		var tempFiles = elem.files;
		$.each(tempFiles, function (idx, obj) {
			var testArr = obj.name.split(".");
			fileArr.push({
				'fileKey': 0,
				'fileName': obj.name,
				'fileType': testArr[testArr.length - 1],
				'fileSize': obj.size,
				'nodeId': selectedNodeId,
				'lpath': selectedNodeText,
				'coCd': fileTempCocd,
				'file': obj
			});
		});

		fileTreeGridView.reqSetData(fileArr);
		$(elem).val("");
	}


	function getFileArr() {

		return fileArr; // fileArr를 반환하는 함수
	}

	function getDeleteFileArr() {
		return deleteFileArr; // fileArr를 반환하는 함수
	}

	function getFileNodeId() {
		return selectedNodeId; // file tree nodeId를 반환하는 함수
	}

	function deleteFile(rowIndex) {
		//서버에 등록된 화일인 경우에는 삭제권한이 있어야 삭제 가능함
		if ((fileArr[rowIndex].fileKey != 0) && (fileArr[rowIndex].fileDelete != 'Y')) {
			alert('파일 삭제 권한이 없습니다.')
			return false;
		};

		if(fileArr[rowIndex].fileKey){
			// 기 등록되어 있는 파일 삭제시
			deleteFileArr.push(fileArr[rowIndex].fileKey);
		}
		//전체 화일 배열에서 제거처리
		fileArr.splice(rowIndex, 1);

		//화면 그리드에서 삭제
		fileTreeGridView.target.removeRow(rowIndex);

	}

//    function downLoadFileAll() {
//    	let downLoadList = fileTreeGridView.target.list;
//    	downLoadList.forEach((elem) => {
//    		if (downLoadFile(elem.fileKey) == 'ERROR') return false;
//    	});
//    }
//
//	function downLoadFile(fileKey) {
//		return new Promise((resolve) => {
//			var tempObj = {
//					"fileKey" : fileKey,
//					"userId" : jwt.userId
//				}
//			postAjaxSync("/admin/cm/cm08/fileDownInfoUser", tempObj, null, function(data){
//				if(data.resultCode == 200){
//					var fileInfo = data.fileInfo;
//	// 				var filePath = encodeURI(fileInfo.filePath + fileInfo.fileKey + "_" + fileInfo.fileName , "UTF-8");
//					location.href = "/admin/cm/cm08/fileDownloadAuth?fileKey="+fileKey+"&userId="+jwt.userId;
//					resolve('SUCCESS');
//				} else {
//					alert("다운로드 권한이 없습니다.");
//					resolve('ERROR');
//				}
//			});
//		});
//	}

	async function delay(ms) {
		return new Promise(resolve => setTimeout(resolve, ms));
	}
	
	//async 함수로 정의하고
	//for...of 루프를 사용하여 downLoadFile을 순차적으로 호출
	// 전체 파일 다운로드 함수
	async function downLoadFileAll() {
		let downLoadList = fileTreeGridView.target.list;
		for (const elem of downLoadList) {
//        	console.log(elem.fileName);
			const result = await downLoadFile(elem.fileKey, elem.fileName);
			if (result === 'ERROR') {
				alert(`파일 다운로드 중 오류가 발생했습니다. 파일 이름: ${elem.fileName}. 관리자에게 문의하세요.`);
			}
			// 0.1초 딜레이 추가
			await delay(100);
		}
	}
	
	
	// 파일 다운로드 함수 (fetch + Blob 방식 사용)
	async function downLoadFile(fileKey, fileName) {
		try {
//			const response = await fetch(`/admin/cm/cm08/fileDownloadAuth?fileKey=${fileKey}&userId=${jwt.userId}`);
			const response = await fetch(`/admin/cm/cm08/fileDownloadAuth2?fileKey=${fileKey}&userId=${jwt.userId}`, {
				  method: "GET",
				  headers: {"Authorization": authorizationToken}
			});
			if (!response.ok) throw new Error('다운로드 실패');
			const blob = await response.blob();

			saveBlobAsFile(blob, fileName);
			return 'SUCCESS';
			
		} catch (error) {
			console.error("파일 다운로드 중 오류:", error);
			return 'ERROR';
		}
	}

	// 파일 저장 함수
	function saveBlobAsFile(blob, fileName) {
		const url = URL.createObjectURL(blob);
		const a = document.createElement('a');
		a.href = url;
		a.download = fileName;
		document.body.appendChild(a);
		a.click();
		URL.revokeObjectURL(url);
		a.remove();
	}

	
	
	
	

	//+,- 버튼에 따라 파일 트리와 그리드 크기를 200px 증가 또는 감소 처리하고
	//  최소크기는 200px, 최대크기는 1200px까지만 처리한다.
	function button_zoomUp( action , popSelector, fileGrid ) {
		//parseInt(문자열, 진법);
		var heightValue = parseInt($(popSelector + " .ax5_grid[data-ax5grid='" + fileGrid + "']").css("height"), 10);
		minmaxValue = (action == '+') ? heightValue + 100 : heightValue - 100;
		minmaxValue = (minmaxValue < 100) ? 100 : (minmaxValue > 1200) ? 1200 : minmaxValue;
		if (heightValue!=minmaxValue) {
			$(popSelector + " #treeview").css("height", minmaxValue);
			$(popSelector +' [data-ax5grid="' + fileGrid + '"]').css("height", minmaxValue);
			$(popSelector + ' [data-ax5grid="' + fileGrid + '"] [data-ax5grid-container="root"] ').css("height", minmaxValue);
			fileTreeGridView.reqSetData(fileTreeGridView.target.list)
		}
	}


	// #fileList_area 요소의 자식으로 새로운 코드를 삽입합니다.
	function fileListArea_html_creation (popSelector, fileSelector, fileGrid, selector) {
		var fileListArea = "";
		if (popSelector) {
			fileListArea = $(popSelector +' '+ fileSelector);
		} else {
			fileListArea = $(fileSelector);
		}
		fileListArea.append(`
			<div id="fileAttachTxt" style="display:block; cursor: pointer; text-align: left; font-weight:bold; height: 30px;"><i class="fa fa-file-import"></i> 파일첨부　　</div>
			<div id="fileAttachCnts" style="display:none">
			<div class="col-xs-2 pd0">
			<div class="contents mg0 pd0" style="width: 100%; min-width: 200px">
				<h3 class="location">
				<span class="page_tit" style="text-align: left;"> 파일트리</span>
				</h3>
				<div id="treeview" style="height: 300px; padding: 5px">
				<div id="${selector}"></div>
				</div>
			</div>
			</div>
			<div class="col-xs-10 pd0 pdl5">
			<div class="contents  mg0 pd0" style="width: 100%; min-width: 200px">
				<h3 class="location">
				<a class="file_tag pdl20 pdr10" id="file_tag" style="font-weight: bold; color: blue;"></a>
				<span class="page_tit" id="file_tit" style="text-align: right;"> 문서현황 </span>
				</h3>
				<button type="button" id="button_file" style="height: 20px; line-height: 10px;" onclick="${selector}file.click();" authchk> 첨부파일</button>
				<div class="add_btn_small pdl10">
					<a onclick="treeModule.downLoadFileAll('${popSelector}', '${fileGrid}');" style="width:90px; height: 25px;"><i class="fas fa-download"></i>전체다운로드</a>
					<a onclick="treeModule.button_zoomUp('+', '${popSelector}', '${fileGrid}');" style="height: 25px;"><i class="fas fa-search-plus"></i>+</a>
					<a onclick="treeModule.button_zoomUp('-', '${popSelector}', '${fileGrid}');" style="height: 25px;"><i class="fas fa-search-minus"></i>-</a>
				</div>
				<div class="ax5_grid" data-ax5grid="${fileGrid}" data-ax5grid-config="{}" style="height: 300px; width: 100%"></div>
			</div>
			</div>
			<div class="col-xs-2" style="height: 70px;"></div>
			</div>
		`);

		$(popSelector + " #fileAttachTxt").click(function() {
			$(popSelector + " #fileAttachTxt").hide();
			$(popSelector + " #fileAttachCnts").show();
			getAllFilesForNodes(treeComonCd);
		});
	}


	function callApprovalWorking(todoKey, callPgm = ''){
		paramObj = {
				"todoKey" : todoKey,
				"userId"  : jwt.userId,
		}
		let row = {};
		postAjaxSync("/user/wb/wb20/selectCurrentUserApprovalDataListFromTodoKey", paramObj, null, function(data){
			row = data.resultList[0];
		});
		if (row.todoId != jwt.userId) {
			alert("결재/확인은 담당자만 처리가능합니다. ");
			return false;
		}
		if (row.todoCfDt != undefined) {
			if(!confirm("결재/확인 처리 완료건입니다.  의견 수정하시겟습니까?")) {
				return false;
			}
		}

		// 1. 문제발주요청서, 결과등록, 문제조치로 들어온 결제 이면서 팀장일 때 유효성 검사
 		// 2. 발주에서 결과 처리할때 발행자 기준으로 해당 팀장 여부 확인
		// 3. 정상발주 제외일때만 유효성 체크
		// 4. 문제조치는 팀장 결재시 유효성 체크
		if (((row.todoDiv2CodeId === "TODODIV2020") || (row.todoDiv2CodeId === "TODODIV2030") || (row.todoDiv2CodeId === "TODODIV2090")) && approvalWorkingGrid.teamManager == '평가' ) {
			if ((row.todoDiv2CodeId === "TODODIV2020") || (row.todoDiv2CodeId === "TODODIV2030")) {
				if (!['COBTP01', 'COBTP04', 'COBTP06', 'COBTP08', 'COBTP09'].includes($("#partCd").val())) {
					if (!inputValidation($('.popup_area [required]'))) {
						return false;
					}
					if (!$("input[name='FDMTSOLUT']:checked").val() && approvalWorkingGrid.sameTimeResult == 'Y') {
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
		
		//코칭수정 버튼이 활성화 상태이면 결과 Update 처리 선행
		//필요시 프로그램 에 따라 분기 처리
		if ($("#commentBtn").is(":enabled")) { //코칭 수정이면 코칭만 저장함
			let chkFlag = false;
			if (callPgm =='QM0101P01') {
				chkFlag = ModalApp.updateQualityResultComment('결과수정');
			} else if (callPgm == 'QM0101P03') {
				chkFlag = updateQualityResultComment('결과수정');
			} else  if (callPgm == 'WB2401P01' || callPgm == 'WB2401P11') {
				chkFlag = updateIssueComment('결과수정');
			}
			if (!chkFlag) {return false;}
		}

		if( row.todoDiv1CodeNm == "공유" ) {
			if( row.sanctnSttus == "N" ) {
				if( confirm("확인 처리 하시겠습니까?") ) {
					if (row.todoKey != undefined) {
						var today = new Date();
						var strDate = today.getFullYear()+"-"+('0'+(today.getMonth()+1)).slice(-2)+"-"+('0'+today.getDate()).slice(-2);

						var formData = new FormData();
						formData.append("todoKey", row.todoKey);
						formData.append("creatId", jwt.userId);
						formData.append("creatPgm", "WB2001M01");
						//formData.append("todoCfDt", strDate);
						filePutAjax("/user/wb/wb20/toDoCfDtUpdate", formData, function(data){
							if(data.resultCode == 200){
								$('.callApprovalWorking').last().remove();	//마지막 callApprovalWorking class에서 버튼 제거
//								alert("공유 확인되었습니다.");
								if (typeof gridView !== 'undefined') {
									gridView.initView().setData(0);
								}
									modalStack.close();
							}
						});
					}
				}
			} else {
				alert("이미 확인처리 하였습니다.");
				return;
			}
		} else if( row.todoDiv1CodeNm == "결재" ) {
			var paramObj = {
					"coCd":row.coCd
					, "salesCd":row.salesCd
					, "issNo":row.todoNo
					, "todoDiv1CodeId":row.todoDiv1CodeId
					, "todoDiv1CodeNm":row.todoDiv1CodeNm
					, "todoDiv2CodeId":row.todoDiv2CodeId
					, "todoDiv2CodeNm":row.todoDiv2CodeNm
					, "todoFileTrgtKey":row.todoFileTrgtKey
					, "todoTitl":row.todoTitl
					, "sanctnSn":row.sanctnSn
					, "pgmId": "WB2001M01"
					, "userId": jwt.userId
					, "histNo" : row.etcField2
			};
			if (row.todoDiv2CodeId === "TODODIV2020") {
				paramObj.sameTimeResultChk = sameTimeResultChk;
			}
			openThirdModal("/static/html/user/wb/wb20/WB2001P01.html", 730, 300, "", paramObj, function(data){
			// openFourthModal("/static/html/user/wb/wb20/WB2001P01.html", 730, 300, "", paramObj, function(data){
				if (data == "승인완료") {
					$('.callApprovalWorking').last().remove();		//마지막 callApprovalWorking class에서 버튼 제거
					if (typeof gridView !== 'undefined') {
						gridView.initView().setData(0);
					}
					modalStack.close();
					if ($('#areaApproval').length && jwt.userId == 'EMJ8105') updateApprovalHold();
				}
			});
		}
	}

	return {
		initDeptTree: initDeptTree,
		getAllFilesForNodes: getAllFilesForNodes,
		selectDeptTree: selectDeptTree,
		initAll: initAll,
		addFileToTree: addFileToTree,
		getFileArr:getFileArr,
		getDeleteFileArr:getDeleteFileArr,
		getFileNodeId:getFileNodeId,
		deleteFile:deleteFile,
		downLoadFileAll:downLoadFileAll,
		downLoadFile:downLoadFile,
		button_zoomUp:button_zoomUp,
		callApprovalWorking:callApprovalWorking,
	};


})();



