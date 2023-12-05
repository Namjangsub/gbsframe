var fileTreeGridView
var treeComonCd;
var treeModule = (function () {
    var fileArr=[];
    var deleteFileArr = [];
    var fileTreeParamObj;
    var currPgmAuthChk = true; // true:저장권한, false:저장권한 없음
    var fileTempCocd = '';
    //최초 진입시점---
    //   1. 파일트리명 = 각 화면의 트리구조를 보여줄 div테그 ID ex) <div id="deptTree" ></div>
    //   2. 그리드명  = 각 화면의 ax5-grid Html Tag ID  ex) <div id="my-grid" data-ax5grid="file-grid" data-ax5grid-config="{}" style="height: 100%; width: 100%"></div>
    //   3. 파일트리 최상위코드값  =  FIRETREE : 파일트리 최상위 값
    //   4. params 첨부자료가 저장된 타입과 일련번호를 전달한다.
    //      4-1. fileTrgtTyp   = 파일저장타입(프로그램명 또는 테이블명으로 프로젝트내에서 유니크한 값 지정) ex)TB_BM0101M01
    //      4-2. fileTrgtKey   = 파일저장일련번호 (프로그램명 또는 테이블명에서 관리되는 유니크한 일련번호 ex) 1, 2, 3, 4
    //	treeModule.initAll('deptTreeOrdars', 'file-grid', 'FILETREE', fileParam);
    //
    //                'deptTree', 'file-grid', 'FILETREE', paramTreeObj);
    function initAll(selector, gridSelector, codeId, params, _coCd) {
    	//coCd가 파라메터로넘어오면 넘어온 값을 우선 처리 아니면 하면의 coCd로 처리함
    	if (_coCd == '' || _coCd == undefined) {
    		fileTempCocd = $('#coCd').val();
    	} else {
    		fileTempCocd = _coCd;
    	}
    	
        if ($("#fileList_area #fileAttachTxt").length > 0) {
            // console.log("#fileAttachTxt 요소가 존재합니다.");
        } else {
            // console.log("#fileAttachTxt 요소가 존재하지 않습니다.");
            // #fileList_area 요소의 자식으로 새로운 코드를 삽입합니다.
            fileListArea_html_creation();
            
            createFileInput(gridSelector);
//            treeComonCd = codeId; //무시
            treeComonCd = 'FILETREE';             //최상위 트리 강제로 할당 (파라메터값 무시)
            params["comonCd"] = treeComonCd;      //comonCd는 xML 쿼리에서 트리ID 보관 필드명 
            params["userId"] = jwt.userId;        //캐시에 보관된 사용자 ID
            
            fileTreeParamObj = params;  //
            fileArr=[];
            deleteFileArr = [];
            
            initDeptTree(selector);
            
            currPgmAuthChk = authChk(); // true:저장권한, false:저장권한 없음
            
            fileTreeGridView.init(gridSelector);

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
                            (this.item.fileDown == 'Y')  ? downloadFile(fileKey) : alert('파일 다운로드 권한이 없습니다.');
                        }
                    },
                },
                columns: [

                    {key: "fileTrgtKey", label: "파일타겟키", hidden: true},
					{key : "nodeId",  label : "nodeId",	 width : 120, align: "center", hidden: true},
					{key : "fileUp",  label : "up",	 width : 60, align: "center", hidden: true},
					{key : "fileDown",  label : "down",	 width : 60, align: "center", hidden: true},
					{key : "fileUpdate",  label : "update",	 width : 60, align: "center", hidden: true},
					{key : "fileDelete",  label : "delete",	 width : 60, align: "center", hidden: true},
					{key: "fileKey", label: "파일키", hidden: true},
                    {key: "fileName", label: "파일명", width: 260, align: "left"},
                    {key: "fileType", label: "파일타입", width: 60, align: "center"},
                    {key: "fileSize", label: "파일크기", width: 100, align: "right", formatter: "money"},
                    {key: "creatDttm", label: "저장일자", width: 130, align: "center",},
                    {key: "creatNm", label: "등록자", width: 60, align: "center"},
                    {key: "clntCd", label: "거래처", width: 50, align: "center", hidden: true},
                    {key: "clntNm", label: "거래처명", width: 110, align: "center", hidden: true},
                    {key: "prdtCd", label: "제품코드", width: 50, align: "center", hidden: true},
                    {key: "prdtNm", label: "제품명", width: 100, align: "center", hidden: true},
                    {key: "itemCd", label: "아이템", width: 50, align: "center", hidden: true},
                    {key: "salesCd", label: "Sales코드", width: 110, align: "center"},
                    // {key: "prjctCd", label: "프로젝트", width: 50, align: "center", hidden: true},
                    // {key: "prjctNm", label: "프로젝트명", width: 110, align: "center"},
					{key : "lpath",  label : "저장위치",	 width : 180, align: "center", hidden: false},
                    {key: "fileDelete", label: "삭제", width: 60, align: "center",
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
                $("#fileAttachTxt").hide();
                $("#fileAttachCnts").show();
            };
            
            targetObj.setData({
                list: list,
                page: {
                    totalElements: list.length
                }
            });
        }
    }

    function initDeptTree(selector) {
        $('#' + selector).jstree("destroy");
        $('#' + selector).jstree(
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
    	  				'leaf' : {"icon" : 'glyphicon glyphicon-file'},
    	  				'unit' : {'icon' : 'glyphicon glyphicon-folder-close'},
    	  				'unit-open' : {'icon' : 'glyphicon glyphicon-folder-open'}
    				}
    			}
            }).on("loaded.jstree", function () {
            // 루트 노드 로드 완료 시
            //전체 노드 펼침
//            $('#' + selector).jstree("open_all");
            // 최상위 노드 펼침
            $('#' + selector).jstree(true).open_node($('#' + selector + ' li[aria-level="1"]').eq(0).attr('id'));

            var topLevelNode = $('#' + selector + ' li[aria-level="1"]').eq(0).attr('id');
			$('#' + selector).jstree(true).deselect_all();            
            $('#' + selector).jstree(true).select_node(topLevelNode);
            
			var tree = $('#' + selector).jstree(true);
			const topNode = 'FILETREE';
			const rootNode = tree.get_node(topNode);
			  
			//자식노드중에 isLeaf가 1인 자식인 하나도 없으면 트리목록 삭제 처리
			searchAndDelete(tree, topNode, topNode);
			
        })
            .on("refresh.jstree", function () {
                // 리프레시 완료 시
                var selectedId = $('#' + selector).jstree(true).get_selected()[0];
                $('#' + selector).jstree(true).deselect_all();
                $('#' + selector).jstree(true).select_node(selectedId);
            })
            .on("select_node.jstree", function (e, data) {
        		// 노드 선택 시 발생 이벤트

        		let targetTree = $('#' + selector).jstree('get_selected',true)[0];
        		if (targetTree == undefined) {
        			$("#file_tag").html("파일저장Tree에는 문서를 저장할 수 없습니다.");
        			treeComonCd = "FILETREE";
        		} else {
	                selectedNodeId = data.node.id;
	                selectedNodeText = data.node.text;
	                selectedNodeEtc = data.node.original.codeEtc;
	                var buttonFile = $("#button_file");
	                if (buttonFile.length) {
		    			if (selectedNodeEtc == 'N') {
		    				buttonFile.hide();
		    			} else {			
		    				buttonFile.show();
		    			}
	                }
        			let txt = selectedNodeText + ( (selectedNodeEtc =="N") ? "에는 저장할 수 없습니다." : "" );
        			$("#file_tag").html(txt);
        			treeComonCd =  selectedNodeId;
        		}
        		
        		if (!$('#' + selector).is(":visible")) {
        			$('#' + selector).show();
        		}
                getAllFilesForNodes(treeComonCd);

        		// 노드 선택 시
    		}).on('hover_node.jstree',function(e, data){
    			// 마우스 위치할때
    			if (data.node.original.codeEtc == 'N') {
    				$("#"+data.node.id).attr("title","자료를 저장할 수 없는 폴더입니다.");
    			} else {
    				$("#"+data.node.id).attr("title","");
    			}
			}).on('dblclick.jstree', function (e, data) {
			//선택된 노드의 값을 가져와서 처리한다.
			var selectedNode = $('#' + selector).jstree('get_selected');
			if (selectedNode.length > 0) {
				var selectedNodeData = $('#' + selector).jstree('get_node', selectedNode[0]).original;
				  
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

    
    function createFileInput(gridSelector) {
        // 새로운 input 요소 생성
        var fileInput = document.createElement('input');

        // input 속성 설정
        fileInput.type = 'file';
        fileInput.id = 'file';
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

    function downloadFile(fileKey) {
    	var tempObj = {
				"fileKey" : fileKey,
				"userId" : jwt.userId
			}
		postAjax("/admin/cm/cm08/fileDownInfoUser", tempObj, null, function(data){
			if(data.resultCode == 200){
				var fileInfo = data.fileInfo;
// 				var filePath = encodeURI(fileInfo.filePath + fileInfo.fileKey + "_" + fileInfo.fileName , "UTF-8");
				location.href = "/admin/cm/cm08/fileDownloadAuth?fileKey="+fileKey+"&userId="+jwt.userId;
			} else {
				alert("다운로드 권한이 없습니다.");	
			}
		});
    }

	//+,- 버튼에 따라 파일 트리와 그리드 크기를 200px 증가 또는 감소 처리하고
    //  최소크기는 200px, 최대크기는 1200px까지만 처리한다.
	function button_zoomUp( action ) {
		//parseInt(문자열, 진법);
		var heightValue = parseInt($(".ax5_grid[data-ax5grid='file-grid']").css("height"), 10);
		minmaxValue = (action == '+') ? heightValue + 100 : heightValue - 100;
		minmaxValue = (minmaxValue < 100) ? 100 : (minmaxValue > 1200) ? 1200 : minmaxValue;
		if (heightValue!=minmaxValue) {
			$("#treeview").css("height", minmaxValue);
			$('[data-ax5grid="file-grid"]').css("height", minmaxValue);
			$('[data-ax5grid="file-grid"] [data-ax5grid-container="root"] ').css("height", minmaxValue);
			fileTreeGridView.reqSetData(fileTreeGridView.target.list)
		}
	}
	
	
	// #fileList_area 요소의 자식으로 새로운 코드를 삽입합니다.
	function fileListArea_html_creation () {
		var fileListArea = $('#fileList_area');
		  fileListArea.append(`
		    <div id="fileAttachTxt" style="display:block; cursor: pointer; text-align: left; font-weight:bold; height: 30px;"><i class="fa fa-file-import"></i> 파일첨부　　</div>
		    <div id="fileAttachCnts" style="display:none">
		    <div class="col-xs-2 pd0">
		      <div class="contents mg0 pd0" style="width: 100%; min-width: 200px">
		        <h3 class="location">
		          <span class="page_tit" style="text-align: left;"> 파일트리</span>
		        </h3>
		        <div id="treeview" style="height: 300px; padding: 5px">
		          <div id="deptTreeOrdars"></div>
		        </div>
		      </div>
		    </div>
		    <div class="col-xs-10 pd0 pdl5">
		      <div class="contents  mg0 pd0" style="width: 100%; min-width: 200px">
		        <h3 class="location">
		          <a class="file_tag pdl20 pdr10" id="file_tag" style="font-weight: bold; color: blue;"></a>
		          <span class="page_tit" id="file_tit" style="text-align: right;"> 문서현황 </span>
		        </h3>
		        <button type="button" id="button_file" style="height: 20px; line-height: 10px;" onclick="file.click();" authchk> 첨부파일</button>
		        <div class="add_btn_small pdl10">
					<a onclick="treeModule.button_zoomUp('+');" style="height: 25px;"><i class="glyphicon glyphicon-zoom-in"></i>+</a>
					<a onclick="treeModule.button_zoomUp('-');" style="height: 25px;"><i class="glyphicon glyphicon-zoom-out"></i>-</a>
				</div>
		        <div class="ax5_grid" data-ax5grid="file-grid" data-ax5grid-config="{}" style="height: 300px; width: 100%"></div>
		      </div>
		    </div>
			<div class="col-xs-2" style="height: 70px;"></div>
			</div>	    
		  `);
		  
		  $("#fileAttachTxt").click(function() {
              $("#fileAttachTxt").hide();
              $("#fileAttachCnts").show();
              getAllFilesForNodes(treeComonCd);
          });
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
        downloadFile:downloadFile,
        button_zoomUp:button_zoomUp,
    };


})();



