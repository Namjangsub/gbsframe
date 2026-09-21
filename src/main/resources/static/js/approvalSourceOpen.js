// 결재 원천화면(원본 문서)을 팝업모달로 여는 공용 함수. 결재함(WB2001M01)과 AM 전자결재 조회(AM1201P01)에서 공용.

function openApprovalSourceDoc(item, reloadCallback) {
	if (!item) return;
	reloadCallback = reloadCallback || function(){};

	// 현재 열려 있는 모달 레벨을 감지해 "다음 레벨" 모달로 자동으로 연다(global.js openAutoModal).
	// 결재함 목록(모달 없음)->1차, AM문서조회 1차->2차, AM문서조회 3차(첨부뷰어)->4차 등 진입 경로와 무관하게 상위 레벨로 뜬다.
	// openAutoModal 이 없는 예외 상황(로드 순서 등)에서는 기존 openModal 로 폴백한다.
	var _openModal = (typeof openAutoModal === 'function') ? openAutoModal
	              : ((typeof openModal === 'function') ? openModal : function(){});

	let addParam = {"viewMode": 1, "todoView": "INQ", "todoKey":item.todoKey, "gridObj" :item};
	let paramObj = {};
	if (item.todoDiv2CodeId != "TODODIV9999") {
		paramObj = JSON.parse(item.pgParam);
		Object.assign(paramObj, addParam);
	}

	if (item.todoDiv2CodeId =="TODODIV1030" || item.todoDiv2CodeId =="TODODIV2060" ||
		item.todoDiv2CodeId =="TODODIV1090" || item.todoDiv2CodeId =="TODODIV2090" ) {
		_openModal("/static/html" + item.pgPath, 1200, 870, "", paramObj, reloadCallback);
	} else if (item.todoDiv2CodeId =="TODODIV1060"){
		_openModal("/static/html" + item.pgPath, 900, 880, "프로젝트이슈 To-Do", paramObj, reloadCallback);
	} else if (item.todoDiv2CodeId =="TODODIV2100"){
		_openModal("/static/html" + item.pgPath, $('body').width(), $('body').height()-40, "수주복표원가  To-Do", paramObj, reloadCallback);
	} else if (item.todoDiv2CodeId =="TODODIV1120" || item.todoDiv2CodeId =="TODODIV2120"){
		_openModal("/static/html" + item.pgPath, 1700, $('body').height()-40, "PFU 결재 화면", paramObj, reloadCallback);
	} else if (item.todoDiv2CodeId == "TODODIV2170") {
			_openModal("/static/html" + item.pgPath, 1600, 850, "", paramObj, reloadCallback)
	} else if (item.todoDiv2CodeId == "TODODIV1201" || item.todoDiv2CodeId == "TODODIV2201" || item.todoDiv2CodeId == "TODODIV1200" || item.todoDiv2CodeId == "TODODIV2200"|| item.todoDiv2CodeId == "TODODIV2190" || item.todoDiv2CodeId == "TODODIV2191" || item.todoDiv2CodeId == "TODODIV1190" || item.todoDiv2CodeId == "TODODIV1191") {
			_openModal("/static/html" + item.pgPath, 1700, 860, "", paramObj, reloadCallback)
	} else if (item.todoDiv2CodeId == "TODODIV2410" || item.todoDiv2CodeId == "TODODIV2420"
			|| item.todoDiv2CodeId == "TODODIV1410" || item.todoDiv2CodeId == "TODODIV1420") {
		paramObj.reqNo = item.todoNo;
		paramObj.coCd = item.coCd;
		paramObj.actionType = 'A';
		paramObj.openStage = (item.todoDiv2CodeId == 'TODODIV2420' || item.todoDiv2CodeId == 'TODODIV1420') ? 'RESULT' : 'REQ';
		paramObj.todoDiv2CodeId = item.todoDiv2CodeId;
		paramObj.todoKey = item.todoKey;
		paramObj.gridObj = item;
		_openModal("/static/html" + item.pgPath, 1100, 800, "", paramObj, reloadCallback);
	} else if (item.todoDiv2CodeId == "TODODIV9999") {
		if (typeof wbsIssModal === 'function') wbsIssModal(item);
	} else {
		_openModal("/static/html" + item.pgPath, 1600, 850, "", paramObj, reloadCallback);
	}
}
