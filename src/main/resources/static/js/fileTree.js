
var treeModule = (function () {
    var fileArr = [];
    var paramObj;
    var codeIdVal;
    var fileTreeGridView = {
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
                        var fileKeyAttributeName = "fileKey";
                        var fileKey = this.item[fileKeyAttributeName];
                        if (fileKey) {
                            downloadFile(fileKey);
                        }
                    },
                },
                columns: [

                    {key: "fileTrgtKey", label: "파일타겟키", hidden: true},
                    {key: "fileKey", label: "파일키", hidden: true},
                    {key: "fileName", label: "파일명", width: 260, align: "left"},
                    {key: "fileType", label: "파일타입", width: 60, align: "center"},
                    {key: "fileSize", label: "파일크기", width: 100, align: "right", formatter: "money"},
                    {key: "creatDttm", label: "생성일자", width: 130, align: "center",},
                    {key: "creatId", label: "생성자", width: 100, align: "center"},
                    {key: "clntCd", label: "거래처", width: 50, align: "center", hidden: true},
                    {key: "clntNm", label: "거래처명", width: 110, align: "center"},
                    {key: "prdtCd", label: "제품코드", width: 50, align: "center", hidden: true},
                    {key: "prdtNm", label: "제품명", width: 100, align: "center"},
                    {key: "itemCd", label: "아이템", width: 50, align: "center"},
                    {key: "salesCd", label: "Sales코드", width: 110, align: "center"},
                    {key: "prjctCd", label: "프로젝트", width: 50, align: "center", hidden: true},
                    {key: "prjctNm", label: "프로젝트명", width: 110, align: "center"},
                    {
                        key: "fileDelete", label: "삭제", width: 80, align: "center",
                        formatter: function () {
                            return '<button style="height: 18px; padding:0px;" type="button" onclick="deleteFile('
                                + this.dindex
                                + ');">삭제</button>';
                        }
                    }],
                page: {
                    display: false
                }
            });
            return this;
        },
        reqSetData: function (list) {
            var targetObj = this.target;
            targetObj.setData({
                list: list,
                page: {
                    totalElements: fileArr.length
                }
            });
        }
    }

    function init(codeId, params) {
        codeIdVal = codeId;
        paramObj = params;

    }
    function initDeptTree(selector) {
        $('#' + selector).jstree("destroy");
        $('#' + selector).jstree(
            {
                plugins: ['types'],
                core: {
                    data: selectDeptTree()
                },
                types: {
                    'unit': {
                        'icon': 'glyphicon glyphicon-folder-close'
                    },
                    'unit-open': {
                        'icon': 'glyphicon glyphicon-folder-open'
                    }
                }
            }).on("loaded.jstree", function () {
            // 루트 노드 로드 완료 시
            //전체 노드 펼침

            // $("#'+selector+'").jstree("open_all");
            // 최상위 노드 펼침
            $('#' + selector).jstree(true).open_node($('#' + selector + ' li[aria-level="1"]').eq(0).attr('id'));
            $('#' + selector).jstree(true).open_node($('#' + selector + ' li[aria-level="2"]').eq(0).attr('id'));

            var topLevelNode = $('#' + selector + ' li[aria-level="1"]').eq(0).attr('id');
            $('#' + selector).jstree(true).select_node(topLevelNode);
        })
            .on("refresh.jstree", function () {
                // 리프레시 완료 시
                var selectedId = $('#' + selector).jstree(true).get_selected()[0];
                $('#' + selector).jstree(true).deselect_all();
                $('#' + selector).jstree(true).select_node(selectedId);
            })
            .on("select_node.jstree", function (e, data) {
                var clickedId = data.node.id;

                paramObj.comonCd = clickedId;

                // 선택한 노드의 모든 하위 노드를 가져옵니다.
                var childrenNodes = getAllChildrenNodes(data.instance, clickedId);


                selectedNodeId = data.node.id;
                isEditMode = paramObj.actionType === 'U'
                if (isEditMode) {
                    var allNodes = [clickedId].concat(childrenNodes);
                    // 수정 화면인 경우, 데이터베이스에서 파일 목록을 가져옵니다.
                    getAllFilesForNodes(allNodes, function (allFiles) {
                        fileTreeGridView.reqSetData(allFiles);
                    });
                } else {
                    //표시하는 부분
                    var relatedFiles = fileArr.filter(function (file) {
                        return file.nodeId === selectedNodeId;
                    });
                    fileTreeGridView.reqSetData(relatedFiles);

                }


                if (!$('#' + selector).is(":visible")) {
                    $('#' + selector).show();
                }
                const buttonFile = document.getElementById("button_file");

                // 합쳐진 코드 부분 시작
                // const buttonFile = document.getElementById("button_file");
                // document.getElementById('zip_down').addEventListener('click', function() {
                //     downloadAllFilesInGrid();
                // });


                if (data.node.id.length < 7 || data.node.id == "FILETREE") {

                    buttonFile.disabled = true; // 비활성화
                    buttonFile.style.backgroundColor = "gray"; // 회색 배경색으로 변경 (옵션)
                } else {
                    buttonFile.disabled = false; // 활성화
                    buttonFile.style.backgroundColor = ""; // 기본 배경색으로 변경 (옵션)

                }


                $("#file_tit").html($('#' + selector).jstree('get_selected', true)[0].text);

                // 합쳐진 코드 부분 끝
            })
            .on('open_node.jstree', function (e, data) {
                // 노드 열릴 때
                data.instance.set_type(data.node, 'unit-open');
            }).on('close_node.jstree', function (e, data) {
            // 노드 닫힐 때
            data.instance.set_type(data.node, 'unit');
        })
    }
    function initAll(selector, gridSelector, codeId, params) {
        createFileInput(gridSelector);
        init(codeId, params);
        initDeptTree(selector);
        fileTreeGridView.init(gridSelector);
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

    function getAllChildrenNodes(instance, nodeId) {
        var childrenNodes = [];
        var nodeChildren = instance.get_node(nodeId).children;

        for (var i = 0; i < nodeChildren.length; i++) {
            childrenNodes.push(nodeChildren[i]);
            childrenNodes = childrenNodes.concat(getAllChildrenNodes(instance, nodeChildren[i]));
        }
        return childrenNodes;
    }

    function getAllFilesForNodes(nodeIds, callback) {
        var allFiles = [];

        function getFileListForNode(index) {
            if (index >= nodeIds.length) {
                callback(allFiles);
                return;
            }

            var nodeId = nodeIds[index];
            paramObj.comonCd = nodeId;
            postAjax("/admin/cm/cm08/selectTreeFileList", paramObj, null, function (data) {
                allFiles = allFiles.concat(data.fileList);
                getFileListForNode(index + 1);
            });
        }

        getFileListForNode(0);
    }

    function selectDeptTree() {
        var deptTree = null;
        var paramObj = {};
        postAjaxSync("/admin/cm/cm05/selectDocTreeList", paramObj, null, function (data) {
            deptTree = data.docTreeList.filter(function (node) {
                return isRelatedToFitr02(node, data.docTreeList);
            });
        });
        return deptTree;
    }

    function isRelatedToFitr02(node, nodeList) {
        if (node.deptId === 'FILETREE' || node.deptId === codeIdVal || node.parent === codeIdVal) {
            return true;
        }
        var parentNode = nodeList.find(function (parentNode) {
            return parentNode.deptId === node.parentDeptId;
        });
        if (!parentNode) {
            return false;
        }
        return isRelatedToFitr02(parentNode, nodeList);
    }

    function addFileToTree(elem) {
        var tempFiles = elem.files;
        $.each(tempFiles, function (idx, obj) {
            var testArr = obj.name.split(".");
            fileArr.unshift({
                'fileKey': 0,
                'fileName': obj.name,
                'fileType': testArr[testArr.length - 1],
                'fileSize': obj.size,
                'nodeId': selectedNodeId,
                'coCd': coCd,
                'file': obj

            });
        });

        var relatedFiles = fileArr.filter(function (file) {
            return file.nodeId === selectedNodeId;
        });
        fileTreeGridView.reqSetData(relatedFiles);

    }

    function deleteFile(rowIndex) {
        fileTreeGridView.target.removeRow(rowIndex);

        if (fileArr[rowIndex].fileKey) {

            deleteFileArr.push(fileArr[rowIndex].fileKey);
        }

        fileArr.splice(rowIndex, 1);
        fileTreeGridView.setData();
    }

    function downloadFile(fileKey) {

        postAjax("/admin/cm/cm08/fileDownInfo", {"fileKey": fileKey}, null, function (data) {
            var fileInfo = data.fileInfo;
            var filePath = encodeURI(fileInfo.filePath + fileInfo.fileKey + "_" + fileInfo.fileName, "UTF-8");
            location.href = "/admin/cm/cm08/fileDownload?filePath=" + filePath;
        });
    }

    function getFileArr() {
        return fileArr; // fileArr를 반환하는 함수
    }
    return {
        initDeptTree: initDeptTree,
        getAllChildrenNodes: getAllChildrenNodes,
        getAllFilesForNodes: getAllFilesForNodes,
        selectDeptTree: selectDeptTree,
        isRelatedToFitr02: isRelatedToFitr02,
        init: init,
        initAll: initAll,
        addFileToTree: addFileToTree,
        deleteFile: deleteFile,
        downloadFile: downloadFile,
        getFileArr:getFileArr

    };


})();




