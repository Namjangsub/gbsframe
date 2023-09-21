//공용 도움말 호출 팝업
function manualPopup(userId,pgmId) {

	if(!userId)userId = $('#userId').val();
	if(!pgmId)pgmId = $('#pgmId').val();
	
	var paramObj = {
					"userId": userId,
					"pgmId" : pgmId
					};
	
	openModal("/static/html/admin/bm/bm99/BM9901P01.html", 1000, 500, "", paramObj);
	
}