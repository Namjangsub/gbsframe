//공용 도움말 호출 팝업
function manualPopup(pgmId) {

	if(!pgmId)pgmId = $('#pgmId').val();
	
	var paramObj = {
					"userId": jwt.userId,
					"pgmId" : pgmId
					};
	
	openModal("/static/html/admin/bm/bm99/BM9901P01.html", 1400, 500, "", paramObj);
	
}