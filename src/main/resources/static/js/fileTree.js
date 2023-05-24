var fileTreeGridView
var treeModule = (function () {
    var fileArr = [];
    var fileArrOri=[];
    var uploadedFiles= {};
    var deleteFileArr = [];
    var paramObj;
    var codeIdVal;
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
                    {key: "fileName", label: "파일명", width: 360, align: "center"},
                    {key: "fileType", label: "파일타입", width: 60, align: "center"},
                    {key: "fileSize", label: "파일크기", width: 100, align: "right", formatter: "money"},
                    {key: "creatDttm", label: "생성일자", width: 460, align: "center",},

                    // {key: "prjctCd", label: "프로젝트", width: 50, align: "center", hidden: true},
                    // {key: "prjctNm", label: "프로젝트명", width: 110, align: "center"},
                    {
                        key: "fileDelete", label: "삭제", width: 80, align: "center",
                        formatter: function () {
                            return '<button style="height: 18px; padding:0px;" type="button" data-delete-row="'
                                + this.dindex
                                + '">삭제</button>';
                        }
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
            targetObj.setData({
                list: list,
                page: {
                    totalElements: list.length
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

                var childrenNodes = getAllChildrenNodes(data.instance, clickedId);

                selectedNodeId = data.node.id;


                var allNodes = [clickedId].concat(childrenNodes);
                getAllFilesForNodes(allNodes, function (allFiles) {
                    if (!uploadedFiles[selectedNodeId]) {
                        uploadedFiles[selectedNodeId] = allFiles;
                    }
                    updateFileTreeGridView();
                });




                if (!$('#' + selector).is(":visible")) {
                    $('#' + selector).show();
                }
                const buttonFile = document.getElementById("button_file");

                // 합쳐진 코드 부분 시작
                // const buttonFile = document.getElementById("button_file");
                // document.getElementById('zip_down').addEventListener('click', function() {
                //     downloadAllFilesInGrid();
                // });

                var deleteButtons = document.querySelectorAll('button[data-delete-row]'); // Replace with your delete button selector

                // Check if the selected node is a leaf node
                if ($('#' + selector).jstree(true).is_leaf(data.node)) {
                    buttonFile.disabled = false; // 활성화
                    buttonFile.style.backgroundColor = ""; // 기본 배경색으로 변경 (옵션)



                } else {
                    buttonFile.disabled = true; // 비활성화
                    buttonFile.style.backgroundColor = "gray"; // 회색 배경색으로 변경 (옵션)


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
        fileArrOri=[];
        function getFileListForNode(index) {
            if (index >= nodeIds.length) {
                callback(fileArrOri);
                return;
            }

            var nodeId = nodeIds[index];
            paramObj.comonCd = nodeId;
            postAjax("/admin/cm/cm08/selectTreeFileModule", paramObj, null, function (data) {
                fileArrOri = fileArrOri.concat(data.fileList);
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
            var newFile = {
                'fileKey': 0,
                'tempId': new Date().getTime() + idx,
                'fileName': obj.name,
                'fileType': testArr[testArr.length - 1],
                'fileSize': obj.size,
                'nodeId': selectedNodeId,
                'coCd': coCd,
                'file': obj
            };

            if (!uploadedFiles[selectedNodeId]) {
                uploadedFiles[selectedNodeId] = [];
            }
            uploadedFiles[selectedNodeId].push(newFile);
        });

        updateFileTreeGridView();
    }

    function updateFileTreeGridView() {

        var nodeUploadedFiles = uploadedFiles[selectedNodeId] || [];
        fileArr = fileArr.concat(nodeUploadedFiles);
        console.log(JSON.stringify(fileArr));
        fileArr = fileArr.filter(function (item) {
            return item &&
                typeof item === 'object' &&
                'fileKey' in item
        });
        console.log("해당위치"+JSON.stringify(fileArr))
        fileTreeGridView.reqSetData(nodeUploadedFiles);
    }


    function getFileArr() {

        return fileArr; // fileArr를 반환하는 함수
    }

    function getFileArrOri() {

        return fileArrOri; // fileArr를 반환하는 함수
    }


    function getDeleteFileArr() {
        return deleteFileArr; // fileArr를 반환하는 함수
    }


    function deleteFile(rowIndex) {
        if(fileArrOri[rowIndex]){

            deleteFileArr.push(fileArrOri[rowIndex].fileKey);
            fileArrOri.splice(rowIndex, 1);

        }else if(fileArr[rowIndex]) {

            removeUnsavedFile(fileArr[rowIndex].tempId);
        }

        fileTreeGridView.target.removeRow(rowIndex);




    }

    function removeUnsavedFile(tempId) {

        // selectedNodeId에 대한 파일 목록에서 해당 tempId를 가진 파일을 찾아 제거
        if (uploadedFiles[selectedNodeId]) {
            uploadedFiles[selectedNodeId] = uploadedFiles[selectedNodeId].filter(function (uploadedFile) {
                if (uploadedFile) {
                    return uploadedFile.tempId !== tempId;
                } else {
                    return false;
                }
            });
        }
        // fileArr에서 해당 tempId를 가진 파일을 제거
        fileArr = fileArr.filter(function (item) {

            if (item) {
                return item.tempId !== tempId;
            } else {
                // uploadedFile이 정의되지 않았다면, false를 반환
                return false;
            }




        });


        // 화면에 보이는 목록도 업데이트
        updateFileTreeGridView();
    }

    function downloadFile(fileKey) {

        postAjax("/admin/cm/cm08/fileDownInfo", {"fileKey": fileKey}, null, function (data) {
            var fileInfo = data.fileInfo;
            var filePath = encodeURI(fileInfo.filePath + fileInfo.fileKey + "_" + fileInfo.fileName, "UTF-8");
            location.href = "/admin/cm/cm08/fileDownload?filePath=" + filePath;
        });
    }
    function initFileArrays() {
        fileArr = [];
        uploadedFiles = {};
        deleteFileArr = [];

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
        getFileArr:getFileArr,
        getFileArrOri:getFileArrOri,
        getDeleteFileArr:getDeleteFileArr,
        deleteFile:deleteFile,
        downloadFile:downloadFile,
        initFileArrays:initFileArrays
    };


})();



