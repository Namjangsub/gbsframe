if(ax5.ui.grid){
	// 그리드 총건수 표기 커스텀
//	ax5.ui.grid.tmpl.page_status = function(){return '<span>총 {{totalElements}}건</span>';};
	ax5.ui.grid.tmpl.page_status = function(){
	    return '<span>{{{progress}}} {{fromRowIndex}} - {{toRowIndex}} of {{dataRowCount}} {{#dataRealRowCount}}  현재페이지 {{.}}{{/dataRealRowCount}} {{#totalElements}}  전체갯수 {{.}}{{/totalElements}}</span>';
	  };

	// 그리드 formatter money 커스텀
	ax5.ui.grid.formatter["money"] = function () {
		if (typeof this.value !== "undefined") {
			if(typeof this.value == "number"){
				this.value = String(parseFloat(this.value.toFixed(3)));
			}
		    let val = this.value;
		    let regExpPattern = new RegExp('([0-9])([0-9][0-9][0-9][,.])');
		    let arrNumber = val.split('.');
		    arrNumber[0] += '.';

		    do {
		        arrNumber[0] = arrNumber[0].replace(regExpPattern, '$1,$2');
		    } while (regExpPattern.test(arrNumber[0]));

		    return (arrNumber.length > 1) ? arrNumber[0] + arrNumber[1] : arrNumber[0].split('.')[0];
		}else{
			return "";
		}
    };
}

var setCookie = function(name, value, exp) {
	var date = new Date();
	date.setTime(date.getTime() + exp * 24 * 60 * 60 * 1000);
	document.cookie = name + '=' + value + ';expires=' + date.toUTCString() + ';path=/';
};

var getCookie = function(name) {
	var value = document.cookie.match('(^|;) ?' + name + '=([^;]*)(;|$)');
	return value? value[2] : null;
};

var deleteCookie = function (name) {
	var temp = getCookie(name);
	if(temp){
		setCookie(name, temp, 0);
	}
}

var DOMAIN_URL = "";
function isMobile() {
    return /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
}

if(isMobile()){
	DOMAIN_URL = "";
	//DOMAIN_URL = "http://10.90.4.142";
}

var authorizationToken = getCookie("jwtToken");
var jwt = parseJwt(authorizationToken);
var menuIdx = getCookie("menuIdx");

var mask = new ax5.ui.mask();
var modal = new ax5.ui.modal();
var secondModal = new ax5.ui.modal();
var thirdModal = new ax5.ui.modal();
var commonModal = {};

// 모달 스택
function ModalStack() {
	this.modalArr = [];
}
ModalStack.prototype.push = function(modalObj){
	this.modalArr.push(modalObj);
}
ModalStack.prototype.pop = function(){
	return this.modalArr.pop() || null;
}
ModalStack.prototype.last = function(){
	return this.modalArr[this.modalArr.length-1];
}
ModalStack.prototype.size = function(){
	return this.modalArr.length;
}
ModalStack.prototype.close = function(){
	this.pop().target.close();
}
var modalStack = new ModalStack();

var ubiprefix = "";
if(jwt){
	switch (jwt.serverType){
    case "prod" :
        ubiprefix = "http://gbs.gunyangitt.co.kr:8090/ubi4";
        break;
    case "dev" :
        ubiprefix = "http://localhost:8090/ubi4";
        break;
    case "local" :
        ubiprefix = "http://localhost:8090/ubi4";
        break;
    default :
        ubiprefix = "http://gbs.gunyangitt.co.kr:8090/ubi4";
	}
}

var openModal = function(url, width, height, title, paramObj, callback) {
	modal.open({
		header: {
			title: title,
	        btns: {
	        	close: {
	                label: '<i class="fa fa-times-circle" aria-hidden="true"></i>',
	                onClick: function () {
	                	modalStack.close();
	                }
	            }
	        }
	    },
        width: width,
        height: height,
        closeToEsc: false,
        onStateChanged: function () {
            if (this.state === "open") {
                mask.open();
                var modalObj = {
                	"target": this.self,
                	"paramObj": paramObj,
                	"callback": callback
                }
                modalStack.push(modalObj);
            }
            else if (this.state === "close") {
                mask.close();
            }
        }
    }, function () {
    	var targetEl = this.$["body-frame"];
    	$.get(url, function(data) {
    		targetEl.append(data);
      	});
    });
};

var openSecondModal = function(url, width, height, title, paramObj, callback) {
	secondModal.open({
		header: {
			title: title,
			btns: {
	        	close: {
	                label: '<i class="fa fa-times-circle" aria-hidden="true"></i>',
	                onClick: function () {
	                	modalStack.close();
	                }
	            }
	        }
	    },
        width: width,
        height: height,
        closeToEsc: false,
        onStateChanged: function () {
        	if (this.state === "open") {
        		var modalObj = {
                	"target": this.self,
                	"paramObj": paramObj,
                	"callback": callback
                }
                modalStack.push(modalObj);
        	}
        }
    }, function () {
    	var targetEl = this.$["body-frame"];
    	$.get(url, function(data) {
    		targetEl.append(data);
      	});
    });
};

var openThirdModal = function(url, width, height, title, paramObj, callback) {
	thirdModal.open({
		header: {
			title: title,
			btns: {
	        	close: {
	                label: '<i class="fa fa-times-circle" aria-hidden="true"></i>',
	                onClick: function () {
	                	modalStack.close();
	                }
	            }
	        }
	    },
        width: width,
        height: height,
        closeToEsc: false,
        onStateChanged: function () {
        	if (this.state === "open") {
        		var modalObj = {
                	"target": this.self,
                	"paramObj": paramObj,
                	"callback": callback
                }
                modalStack.push(modalObj);
        	}
        }
    }, function () {
    	var targetEl = this.$["body-frame"];
    	$.get(url, function(data) {
    		targetEl.append(data);
      	});
    });
};

function parseJwt(token) {

	console.log('parseJwt !!!');
	if(token == null) {
		if(location.href.search("/static/index.html") != -1  || location.href.search("/static/mobile/index.html") != -1 )  {
			return;
		}else{
			if(isMobile()){
				location.href = "/static/mobile/index.html";
			}else{
				location.href = "/static/index.html";
			}
		}
	}
    var base64Url = token.split('.')[1];
    var base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    var jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));

    return JSON.parse(jsonPayload);
};


function selectGridValidation(obj) {
	if(obj.getList("selected").length > 1) {
		alert("한건 만 선택해주세요.");
		return true;
	}
	if(obj.getList("selected").length == 0) {
		alert("선택된 데이터가 없습니다.");
		return true;
	}
}

function selectGridValidationM(obj) {
	if(obj.getList("selected").length == 0) {
		alert("선택된 데이터가 없습니다.");
		return true;
	}
}

function checkGridRow(grid, type){
	var isValid = true;
	if(grid.getList("selected").length == 0){
		alert("선택된 데이터가 없습니다.");
		isValid = false;
	}

	if(type == "single"){
		if(grid.getList("selected").length > 1){
			alert("한건 만 선택해주세요.");
			isValid = false;
		}
	}
	return isValid;
}

var tokenErrorMsg = ["unauthorized", "invalid_token"];

function postAjax(url, data, contentType, callback) {
	console.log(`postAjax url = ${url} `);
	if(contentType == null) {
		contentType = "application/json; charset=utf-8";
		data = JSON.stringify(data);
	} else if(contentType == "form") {
		contentType = "x-www-form-urlencoded; charset=utf-8";
	} else {
		contentType = contentType;
	}
	$.ajax({
	    type: "POST",
	    url: url,
	    contentType: contentType,
	    data: data,
	    beforeSend: function (request) {
            request.setRequestHeader("Authorization", authorizationToken);
        },
	    success: function(data){
	    	callback(data);
	    },
        error: function (data) {
        	if(tokenErrorMsg.indexOf(data.responseJSON.error) > -1) {
//        		alert("토큰이 만료되었습니다.");
				if(isMobile()){
					location.href = "/static/mobile/index.html";
				}else{
					location.href = "/static/index.html";
				}
        	}
        }
	});
}


function postAjaxSync(url, data, contentType, callback) {

	console.log(`postAjaxSync url = ${url} `);
	if(contentType == null) {
		contentType = "application/json; charset=utf-8";
		data = JSON.stringify(data);
	} else if(contentType == "form") {
		contentType = "x-www-form-urlencoded; charset=utf-8";
	}
	$.ajax({
	    type: "POST",
	    url: url,
	    contentType: contentType,
	    data: data,
	    async: false,
	    beforeSend: function (request) {
            request.setRequestHeader("Authorization", authorizationToken);
        },
	    success: function(data){
	    	callback(data);
	    },
        error: function (data) {
        	if(tokenErrorMsg.indexOf(data.responseJSON.error) > -1) {
//        		alert("토큰이 만료되었습니다.");
				if(isMobile()){
					location.href = "/static/mobile/index.html";
				}else{
					location.href = "/static/index.html";
				}
        	}
        }
	});
}

function deleteAjax(url, data, contentType, callback) {
	if(contentType == null) {
		contentType = "application/json; charset=utf-8";
	}
	$.ajax({
	    type: "DELETE",
	    url: url,
	    contentType: contentType,
	    data: JSON.stringify(data),
	    beforeSend: function (request) {
            request.setRequestHeader("Authorization", authorizationToken);
        },
	    success: function(data){
	    	callback(data);
	    },
        error: function (data) {
        	if(tokenErrorMsg.indexOf(data.responseJSON.error) > -1){
//        		alert("토큰이 만료되었습니다.");

				if(isMobile()){
					location.href = "/static/mobile/index.html";
				}else{
					location.href = "/static/index.html";
				}
        	}
        }
	});
}

function putAjax(url, data, contentType, callback) {
	if(contentType == null) {
		contentType = "application/json; charset=utf-8";
	}
	$.ajax({
	    type: "PUT",
	    url: url,
	    contentType: contentType,
	    data: JSON.stringify(data),
	    beforeSend: function (request) {
            request.setRequestHeader("Authorization", authorizationToken);
        },
	    success: function(data){
	    	callback(data);
	    },
        error: function (data) {
        	if(tokenErrorMsg.indexOf(data.responseJSON.error) > -1){
//        		alert("토큰이 만료되었습니다.");
        		if(isMobile()){
					location.href = "/static/mobile/index.html";
				}else{
					location.href = "/static/index.html";
				}
        	}
        }
	});
}

function filePostAjax(url, data, callback) {
	$.ajax({
//		enctype: 'multipart/form-data',
	    type: "POST",
	    url: url,
	    processData: false,
		contentType: false,
	    data: data,
	    beforeSend: function (request) {
            request.setRequestHeader("Authorization", authorizationToken);
        },
	    success: function(data){
	    	callback(data);
	    },
        error: function (data) {
        	if(tokenErrorMsg.indexOf(data.responseJSON.error) > -1){
//        		alert("토큰이 만료되었습니다.");

        		if(isMobile()){
					location.href = "/static/mobile/index.html";
				}else{
					location.href = "/static/index.html";
				}
        	}
        }
	});
}

function filePutAjax(url, data, callback) {
	$.ajax({
//		enctype: 'multipart/form-data',
	    type: "PUT",
	    url: url,
	    processData: false,
		contentType: false,
	    data: data,
	    beforeSend: function (request) {
            request.setRequestHeader("Authorization", authorizationToken);
        },
	    success: function(data){
	    	callback(data);
	    },
        error: function (data) {
        	if(tokenErrorMsg.indexOf(data.responseJSON.error) > -1){
//        		alert("토큰이 만료되었습니다.");
        		if(isMobile()){
					location.href = "/static/mobile/index.html";
				}else{
					location.href = "/static/index.html";
				}
        	}
        }
	});
}


function ubiExportAjax(fileName, arg, callback) {
	var url = ubiprefix + "/ubiexport.jsp";
	url += "?file="+fileName;
	url += "&arg="+encodeURIComponent(arg);
	// Report 서버에 요청 보내기
	$.ajax({
	    url: url,
	    method: "GET",
	    dataType: "html",
	    success: function(result) {
	    	callback(JSON.parse(result));
//	    	callback(result);
	    },
	    error: function(result) {
	    	callback(JSON.parse(result));
	    }
	});
}


function downloadPDFFromBase64(base64Data, fileName) {
	var byteCharacters = atob(base64Data); // Base64 디코딩
	var byteNumbers = new Array(byteCharacters.length);
	for (var i = 0; i < byteCharacters.length; i++) {
		byteNumbers[i] = byteCharacters.charCodeAt(i);
	}
	var byteArray = new Uint8Array(byteNumbers);
	var file = new Blob([byteArray], { type: 'application/pdf' });

//		// 파일을 form 객체에 추가
//	    var formData = new FormData();
//	    formData.append('file', file, fileName);

	//받은 파일을 다운로드 하는작업임
//	var url = URL.createObjectURL(file);
//	var anchor = $('<a>');
//	anchor.attr('href', url);
//	anchor.attr('download', fileName);
//
//	$('body').append(anchor);
//	anchor[0].click();
//	anchor.remove();
//
//	URL.revokeObjectURL(url);

	return file;
}

function inputValidation(inputList) {
	var isValid = true;
	$.each(inputList, function(idx, elem){
		if($.trim(elem.value) == ""){
			isValid = false;
			var alertMsg = $(elem).attr("msg") || "필수값";
			alert(alertMsg + "(을/를) 입력해주세요.");
			$(elem).focus();
			return false;
		}
	});
	return isValid;
}

// 양수, 음수, 소수점 포함 원단위 포맷 적용
function onlyNumber(elem){
	var regExp = /^(-?)([0-9]*)(\.?[0-9]*)([^0-9]*)/g;
	if(elem.value.trim()){
		$(elem).val(addCommaStr(deleteCommaStr($(elem).val().replace(regExp, "$1$2$3"))));
	}else{
		$(elem).val(0);
	}
}

// 양수, 음수 포함 원단위 포맷 적용
function onlyInteger(elem){
	var regExp = /^(-?)([0-9]*)([^0-9]*)/g;
	if(elem.value.trim()){
		$(elem).val(addCommaStr(deleteCommaStr($(elem).val().replace(regExp, "$1$2"))));
	}else{
		$(elem).val(0);
	}
}

//양수 원단위 포맷 적용
function onlyPositive(elem){
	var regExp = /^([0-9]*)([^0-9]*)/g;
	if(elem.value.trim()){
		$(elem).val(addCommaStr(deleteCommaStr($(elem).val().replace(regExp, "$1"))));
	}else{
		$(elem).val(0);
	}
}

//0-9(십진수)만 허용
function onlyDecimal(elem){
	$(elem).val($(elem).val().replace(/[^0-9]/g,""));
}

// 한글 제거
function exceptKorean(elem){
	$(elem).val($(elem).val().replace(/[ㄱ-ㅎ|ㅏ-ㅣ|가-힣]/g,""));
}

// 계좌번호 (숫자, 하이픈만 허용)
function onlyBkac(elem){
	$(elem).val($(elem).val().replace(/^[-]|[^0-9-]/gi,""));
}

// 전화번호 포맷 변경
function telNumberFormatter(elem){
	onlyDecimal(elem);
	$(elem).val($(elem).val().replace(/(^02.{0}|^01.{1}|[0-9]{3})([0-9]+)([0-9]{4})/g,"$1-$2-$3"));
}

// 사업자 등록번호 포맷 변경
function crnFormatter(elem){
	onlyDecimal(elem);
	if($(elem).val().length <= 10){
		$(elem).val($(elem).val().replace(/(\d{3})(\d{2})(\d{5})/g, "$1-$2-$3"));
	}else{
	// 개인사업자일경우 주민번호
		$(elem).val($(elem).val().substr(0, 6) + "-" + $(elem).val().substr(6, 7));
	}
}

// 콤마 제거
function deleteComma(elem) {
	$(elem).val($(elem).val().replace(/,/g, ""));
}

// 원단위 콤마 추가 스트링변수용
function addCommaStr(value) {
	if(typeof value == "number"){
		value = String(parseFloat(value.toFixed(3)));
	}
	let val = value;
    let regExpPattern = new RegExp('([0-9])([0-9][0-9][0-9][,.])');
    let arrNumber = val.split('.');
    arrNumber[0] += '.';

    do {
        arrNumber[0] = arrNumber[0].replace(regExpPattern, '$1,$2');
    } while (regExpPattern.test(arrNumber[0]));

    // 입력시 소수점 밑 세자리수 제한을 위하여 substring(0, 3) 필요.
    return (arrNumber.length > 1) ? arrNumber[0] + arrNumber[1].substring(0, 3) : arrNumber[0].split('.')[0];
}

// 콤마 제거 스트링변수용
function deleteCommaStr(value) {
    return value.toString().replace(/,/g, "");
}

// 하이픈 제거
function deleteHyphen(elem){
	$(elem).val($(elem).val().replace(/-/g, ""));
}

// 하이픈 제거 스트링변수용
function deleteHyphenStr(value){
	return value.replace(/-/g, "");
}

var authArr;
// 권한에 따른 메뉴 보여주기
function setMenuAuth() {
	var formData = {
		"authInfo" : jwt.authInfo
	}
	postAjaxSync("/selectMenuAuth", formData, null, function(data) {
		authArr = data.accessList;
		checkMenuAuth(data.accessList);
	});
}

function checkMenuAuth(accessList) {
		var html = "";
		var imgIdx = 1;
		$.each(accessList, function(idx, item){
			if(item.upMenuId != "MENU100" && item.menuType == "FOLDER" && item.useYn == 'Y') {
				html += '<li>';
				html += '  <img src="/static/img/svg/menu_0'+imgIdx+'.svg">';
				html += '	<a>'+item.menuNm+'</a> <!-- 서브메뉴 -->';
				html += '	<div class="sub_menu">';
				html += '		<dl id="'+item.menuId+'"></dl>';
				html += '	</div>';
				html += '</li>';
				imgIdx++;
			}
		});
		$('.menu').html(html);

		$.each(accessList, function(idx, item){
			if(item.menuType == "HTML" && item.useYn == 'Y') {
				html = '<dl><dd><a href="'+item.menuUrl+'" onclick="setCookie(\'menuSaveYn\', \''+item.saveYn+'\', 1); insertPgmHistory(\''+item.menuUrl+'\');">'+item.menuNm+'</a></dd></dl>';
				//html = '<dl><dd><a href="'+item.menuUrl+'" onclick="insertPgmHistory(\''+item.menuUrl+'\');">'+item.menuNm+'</a></dd></dl>';
				$("#"+item.upMenuId).append(html);
			}
		});
	}

//로그아웃
function logoutClick(){
		deleteCookie("jwtToken");
		deleteCookie("menuIdx");
		location.href = "/";
}

//공통코드 검색 함수
function setCommonSelect(selectArr){
	$.each(selectArr, function(idx, elem){
		var param = {
			"codeKind" : $(elem).data('kind'),
			"codeRprc" : $(elem).data('rprc'),
			"codeEtc"  : $(elem).data('etc'),
			"codeDesc" : $(elem).data('desc')
		};
		postAjaxSync("/admin/cm/cm05/selectChildCodeList", param , null,  function(data){
			var optionHtml = '';
			var codeList = data.childCodeList;
			$.each(codeList, function (index, item){
				optionHtml += '<option value="'+item.codeId+'" data-rprc="'+item.codeRprc+'" data-etc="'+item.codeEtc+'" data-desc="'+item.codeDesc+'" data-dz-code="'+item.dzCode+'">';
				optionHtml += item.codeNm;
				optionHtml += '</option>';
			});
			$(elem).append(optionHtml);
		})
	})
}

function setCommonDiv(inputCd){
	$.each(inputCd, function(idx, elem){
		var param = {
			"codeKind" : $(elem).data('kind')
		};
		postAjaxSync("/admin/cm/cm05/selectChildCodeList", param , null,  function(data){
			var inputHtml = '';
			var codeList = data.childCodeList;
			var i = "CP";
			var j = 0;
			   $.each(codeList, function (index, item){
					j = j+1;
					inputHtml += '<td style="border:0px;font-size:13px;">';
					inputHtml += '<input type="radio" id="'+item.codeKind+j+'" name="'+item.codeKind+i+'" value="'+item.codeId+'" style="padding:0px;margin:0;width:auto;">';
					inputHtml += ' ' +item.codeNm;
					inputHtml += '</td>';
				});
				$(elem).append(inputHtml);

		})
	})
}

function setCommonTd(inputCd,inputCd2,inputCd3){
	$.each(inputCd, function(idx, elem){
		var param = {
			"codeId" : inputCd2,
			"codeKind" : inputCd3
		};
		postAjaxSync("/admin/cm/cm05/selectChildCodeList", param , null,  function(data){
			var inputHtml = '';
			var codeList = data.childCodeList;
			var i = "CP";
			var j = 0;
			   $.each(codeList, function (index, item){
					j = j+1;
					inputHtml += '<input type="checkbox" id="'+item.codeKind+j+'M" name="'+item.codeKind+i+'" value="'+item.codeId+'" style="padding:0px;margin:0;width:auto;">';
					inputHtml += ' ' +item.codeNm+' ';

				});
				$(elem).append(inputHtml);

		})
	})
}


function setCommonItem(inputCd){
	$.each(inputCd, function(idx, elem){
		var param = {
			"codeKind" : $(elem).data('kind')
		};
		postAjaxSync("/admin/cm/cm05/selectPtchildCodeList", param , null,  function(data){
			var inputHtml = '';
			var codeList = data.PtchildCodeList;
			var i = "CP";
			var j = 0;
			$.each(codeList, function (index, item){
				j = j+1;
				inputHtml += '<tr>';
				if(item.midNm2 == ' '){
				    inputHtml += ' ';
				}else{
				    inputHtml += '<th rowspan="'+item.subCnt+'">';
				}
				inputHtml += item.midNm2;
				inputHtml += '</th>';
				inputHtml += '<td style="text-align:left;height:20px;">';
				inputHtml += ' ';
				inputHtml += '<input type="checkbox" id="'+item.midCd+j+'" name="'+item.midCd+i+'" value="'+item.subCd+'">';
				inputHtml += ' ';
				inputHtml += item.subNm;
				inputHtml += '</td>';
				inputHtml += '</tr>';
			});
			if(i < 16){
			  for (i; i < 15; i++) {
				inputHtml += '<tr style="height:20px;">';
				inputHtml += '<th> </th>';
				inputHtml += '<td> </th>';
				inputHtml += '</tr>';
			  }
			}
			$(elem).append(inputHtml);
		})
	})
}


function mainDefaultLoad(menuNm, subMenuNm) {
	// left
	$("#head_area").load("/static/html/header.html", function(){
		$("#head_area #title").html(subMenuNm);
	});
	$("#head_area").after('<div class="menu_off"><a class="off_btn"></a></div>');
	$('.off_btn').click(function () {
	    $('#head_area').toggleClass('off');
	    $('#top_area').toggleClass('on');
	    $('#main_area').toggleClass('on');
    });
	// top
	$("#top_area").load("/static/html/top.html", function(){
		$('#topMenu').text(menuNm);
		$('#topSubMenu').text(subMenuNm);
		$("#topUserNm").text(jwt.userNm);
		setMenuAuth();
	});

	//메뉴 off 시 메인그리드 리플레쉬 그리드 이름 gridView 사용시 적용
	$(".menu_off").on("click",function(){
		if($(".menu_off").css("left") == "0px") {
			try {
				gridView.initView().setData(0);
			} catch (err){
			}
		}
	});
}

function dateToStr(str) {
	var format = new Date(str);
    var year = format.getFullYear();
    var month = format.getMonth() + 1;
    if(month<10) month = '0' + month;
    var date = format.getDate();
    if(date<10) date = '0' + date;
    var hour = format.getHours();
    if(hour<10) hour = '0' + hour;
    var min = format.getMinutes();
    if(min<10) min = '0' + min;
    var sec = format.getSeconds();
    if(sec<10) sec = '0' + sec;
    return year + "-" + month + "-" + date;
}

function lastWeek() {
	  var d = new Date()
	  var dayOfMonth = d.getDate()
	  d.setDate(dayOfMonth - 7)
	  return dateToStr(d)
}

function before30day() {
	  var d = new Date()
	  var dayOfMonth = d.getDate()
	  d.setDate(dayOfMonth - 30)
	  return dateToStr(d)
}
function after30day() {
	  var d = new Date()
	  var dayOfMonth = d.getDate()
	  d.setDate(dayOfMonth + 30)
	  return dateToStr(d)
}

function formatDate(date) {
	var myMonth = date.getMonth() + 1;
	var myWeekDay = date.getDate();

	var addZero = function(num) {
		if (num < 10) {
			num = "0" + num;
		}
		return num;
	}
	var md = addZero(myMonth) + "-" + addZero(myWeekDay);
	return md;
}

function getMonth(type) {
	var now = new Date();
	var nowYear = now.getYear();
	var returnDate;
	if(type == "S") {
		returnDate = new Date(now.getYear(), now.getMonth(), 1);
	} else {
		returnDate = new Date(now.getYear(), now.getMonth() + 1, 0);
	}
	nowYear += (nowYear < 2000) ? 1900 : 0;
	return nowYear + "-" + formatDate(returnDate);
}

function dateValidation() {
	if($(".input_calendar")[0].value > $(".input_calendar")[1].value) {
		alert("날짜를 확인해주세요");
		$(".input_calendar")[0].value = "";
		return;
	} else {
		$(".datepicker").remove();
	}
}

 //new Date(json.createDt).format("yyyy-MM-dd");
Date.prototype.format = function(f) {
    if (!this.valueOf()) return " ";

    var weekName = ["일요일", "월요일", "화요일", "수요일", "목요일", "금요일", "토요일"];
    var d = this;

    return f.replace(/(yyyy|yy|MM|dd|E|hh|mm|ss|a\/p)/gi, function($1) {
        switch ($1) {
            case "yyyy": return d.getFullYear();
            case "yy": return (d.getFullYear() % 1000).zf(2);
            case "MM": return (d.getMonth("S") + 1).zf(2);
            case "dd": return d.getDate().zf(2);
            case "E": return weekName[d.getDay()];
            case "HH": return d.getHours().zf(2);
            case "hh": return ((h = d.getHours() % 12) ? h : 12).zf(2);
            case "mm": return d.getMinutes().zf(2);
            case "ss": return d.getSeconds().zf(2);
            case "a/p": return d.getHours() < 12 ? "오전" : "오후";
            default: return $1;
        }
    });
};
String.prototype.string = function(len){var s = '', i = 0; while (i++ < len) { s += this; } return s;};
String.prototype.zf = function(len){return "0".string(len - this.length) + this;};
Number.prototype.zf = function(len){return this.toString().zf(len);};


// 양수만 입력
function naturalNumber(elem){
	$(elem).val($(elem).val().replace(/[^0-9]/g,""));
}


// 날짜 입력 (hypen 없이 8숫자입력)
function dateMask(elem){

	naturalNumber(elem);

	var date = elem.value;

    if (date == "" || date == null || date.length < 5) {
      elem.value = date;
      return;
    }

    var DataFormat = "";
    var RegPhonNum = "";

    // 날짜 포맷(yyyy-mm-dd) 만들기
    if (date.length <= 6) {
      DataFormat = "$1-$2"; // 포맷을 바꾸려면 이곳을 변경
      RegPhonNum = /([0-9]{4})([0-9]+)/;
    } else if (date.length <= 8) {
      DataFormat = "$1-$2-$3"; // 포맷을 바꾸려면 이곳을 변경
      RegPhonNum = /([0-9]{4})([0-9]{2})([0-9]+)/;
    }

    date = date.replace(RegPhonNum, DataFormat);

    elem.value = date;

    // 모두 입력됐을 경우 날짜 유효성 확인
    if (date.length == 10) {

      var isVaild = true;

      if (isNaN(Date.parse(date))) {
        // 유효 날짜 확인 여부
        isVaild = false;
      } else {

        // 년, 월, 일 0 이상 여부 확인
        var date_sp = date.split("-");
        date_sp.forEach(function(sp) {
          if (parseInt(sp) == 0) {
            isVaild = false;
          }
        });

        // 마지막 일 확인
        var last = new Date(new Date(date).getFullYear(), new Date(date).getMonth()+1, 0);
        // 일이 달의 마지막날을 초과했을 경우 다음달로 자동 전환되는 현상이 있음 (예-2월 30일 -> 3월 1일)
        if (parseInt(date_sp[1]) != last.getMonth()+1) {
					var date_sp2 = date_sp.slice(0);
					date_sp2[2] = '01';
					var date2 = date_sp2.join("-");
					last = new Date(new Date(date2).getFullYear(), new Date(date2).getMonth()+1, 0);
				}
        if (last.getDate() < parseInt(date_sp[2])) {
          isVaild = false;
        }
      }

      if (!isVaild) {
        alert("잘못된 날짜입니다. \n다시 입력하세요.");
        elem.value = "";
        elem.focus();
        return;
      }
    }
}

function insertPgmHistory(url) {
	var formData = {
		"id" : jwt.userId,
		"name" : jwt.userNm,
		"pgmId" : url.substr(url.lastIndexOf("/")+1,9)
	}
	postAjax("/admin/cm/cm06/insertPgmHistory", formData, null, function(data){

	});
}

// function callReport(fileName, arg, width, height, multicount, ismultireport){
// 	var url = ubiprefix + "/ubihtml.jsp";
// 	url += "?file="+fileName;
// 	url += "&arg="+encodeURIComponent(arg);
// 	url += "&multicount=" + multicount;
// 	url += "&ismultireport=" + ismultireport;
// 	if (width ==""){
// 		width = 900;
// 	}
// 	if (height ==""){
// 		height = 900;
// 	}
// 	popCenter(url, "report", width, height, "yes");
// }

function callReport(fileName, arg, width, height){
	var url = ubiprefix + "/ubihtml.jsp";
	url += "?file="+fileName;
	url += "&arg="+encodeURIComponent(arg);
	if (width ==""){
		width = 900;
	}
	if (height ==""){
		height = 900;
	}
	popCenter(url, "report", width, height, "yes");
}

function popCenter(url, name, width, height, scroll) {
	var str = "height=" + height + ",innerHeight=" + height;
	str += ",width=" + width + ",innerWidth=" + width;
	str += ",status=no,scrollbars=" + scroll;

	if (window.screen)
	{
		var ah = screen.availHeight - 30;
		var aw = screen.availWidth - 10;

		var xc = (aw - width) / 2;
		var yc = (ah - height) / 2;

		str += ",left=" + xc + ",screenX=" + xc;
		str += ",top=" + yc + ",screenY=" + yc;
	}

	return window.open(url, name, str);
}

// 주소창 파라미터 받기
$.urlParam = function(name){
    var results = new RegExp('[\?&]' + name + '=([^&#]*)').exec(window.location.href);
    if(results){
     return results[1] || 0;
    } else {
     return null;
    }
}

function authChk(menuUrl){
	if(!menuUrl){
		var url = window.location.href;
		menuUrl = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("."));
	}
		var arr = JSON.parse(getCookie("authArr"));
        var saveYn = "N";
//        for(var i = 0; i < arr.length; i++){
//            if(arr[i].m == menuUrl){
//                saveYn = arr[i].s;
//                break;
//            }
//        }
//        $.each($("[authchk]"), function(idx, elem){
//            if(saveYn == "Y"){
//                $(elem).show();
//            } else {
////                $(elem).hide();
//                $(elem).remove();
//            }
//        });

        //array함수로 기능 대체하고 버튼을 삭제함(버튼을 사용하는 프로그램은 오류 발생 가능)
        // 버튼 숨김으로 하면 소스 편집하여 강제처리가능으로 위험
        const foundMenu = arr.find(item => item.m === menuUrl);
        saveYn = foundMenu.s;
        if(saveYn != "Y"){
        	const authElements = $("[authchk]");
        	authElements.remove();
        	return false;
        }

//	// select 회사코드 disable (감사용 임시코드)
//	$('select[data-kind="CO"]').prop("disabled", true);
    	return true;
}


// 버튼 컨펌
function confirmBefore(btnElem){
	return confirm("\'"+$(btnElem).text()+"\'하시겠습니까?");
}

// 거래처 코드 제거
function resetClntCd(){
	$('#clntCd_S').val("");
}

// 매출 거래처 코드 제거
function resetSellClntCd(){
	$('#sellClntCd_S').val("");
}

// 매입 거래처 코드 제거
function resetPchsClntCd(){
	$('#pchsClntCd_S').val("");
}

// 연관거래처 코드 제거
function resetlinkGrpClntCd(){
	$('#linkGrpClntCd_S').val("");
}

// 프로젝트 코드 제거
function resetPrjctCd(){
	$('#prjctCd_S').val("");
}

// 현장 코드 제거
function resetSiteCd(){
	$('#siteCd_S').val("");
}



// 함수 재귀호출을 이용한 저장가능한 자식노드가 없으면 트리 삭제 처리
// jstree 자식노드중에 isLeaf인 1인 자식인 없는 트리 삭제처리
// 파라메터 : 트리 인스턴스, nodeId, 최상위트리ID
function searchAndDelete(jstreeInstance, nodeId, topNode) {
	const node = jstreeInstance.get_node(nodeId)
    var children = node.children;
	if (!children) {
		jstreeInstance.delete_node(nodeId);
	} else {
	    if (children.length == 0) {
	    	if (node.original.isLeaf == 0) {
	    		jstreeInstance.delete_node(node.id);
	        	if (node.parent != topNode) {
	        		searchAndDelete(jstreeInstance, node.parent, topNode);
	    		}
	    	}
	    } else {
			// 자식노드로 재검색
			for (var i = children.length - 1; i >= 0; i--) {
				var childId = children[i];
				searchAndDelete(jstreeInstance, childId, topNode);
			}
	    }
	}
}

/**
 * input, textarea에 아래의 요소를 load 이벤트를 등록한다.
 * @param _form
 * @returns
 * oninput {
 	data-positive : 양수
 	data-number : 양수, 음수, 소수점
 	data-integer : 양수, 음수
 	comma or money : 양수 comma
   }
   onkeyup {
   	data-maxlength : byte크기만큼 입력막기
   }
 * date, date="toDay"
 * */
function initLoadForm(_form){
	$.each($(_form+" textarea"), function (idx, elem) {
		if($(elem).is('[data-maxlength]')){
			var data_maxlength = $(elem).attr('data-maxlength');
			if(!isEmpty(data_maxlength)){
				if(!isEmpty($(elem).val())){
					$(elem).val(dataMaxLength($(elem).val(),data_maxlength));
				}
				$(elem).on("keyup", function() {
					$(elem).val(dataMaxLength($(elem).val(),data_maxlength));
				});
			}
		}
	});

	$.each($(_form+"  input"), function (idx, elem) {
		//date
		if($(elem).is('[date]')){
			if(!isEmpty($(elem).val())){
				var val_date = $(elem).val();
				if(val_date.length == 8){
					val_date = val_date.replace(/(\d{4})(\d{2})(\d{2})/, '$1-$2-$3');
				}
				var set_date = new Date(val_date).format("yyyy-MM-dd");//날짜검사.
				$(elem).val(set_date);
			}
			else {
				var set_date = $(elem).attr('date');
				if(isEmpty(set_date)){
					$(elem).datepicker({
						format : "yyyy-mm-dd",
						language : "ko",
						autoclose : true
					});
				}else {
					$(elem).datepicker({
						format : "yyyy-mm-dd",
						language : "ko",
						autoclose : true
					}).datepicker("setDate", set_date);
				}
			}
		}

		//data-maxlength
		if($(elem).is('[data-maxlength]')){
			var data_maxlength = $(elem).attr('data-maxlength');
			if(!isEmpty(data_maxlength)){
				if(!isEmpty($(elem).val())){
					$(elem).val(dataMaxLength($(elem).val(),data_maxlength));
				}
				$(elem).on("keyup", function() {
					$(elem).val(dataMaxLength($(elem).val(),data_maxlength));
				});
			}
		}

		//comma
		if($(elem).is('[comma]')){
			$(elem).css("text-align","right");
			if(!isEmpty($(elem).val())){
				$(elem).val($(elem).val().replace(/\B(?=(\d{3})+(?!\d))/g, ","));
			}
			$(elem).on("input", function() {
				$(elem).val($(elem).val().replace(/[^0-9]/g, ""));
			});
			$(elem).on("focus", function() {
				$(elem).val($(elem).val().replace(/[^0-9]/g, ""));
			});
			$(elem).on("blur", function() {
				$(elem).val($(elem).val().replace(/\B(?=(\d{3})+(?!\d))/g, ","));
			});
		}
		//money
		else
		if($(elem).is('[money]')){
			$(elem).css("text-align","right");
			if(!isEmpty($(elem).val())){
				$(elem).val($(elem).val().replace(/[^0-9]/g, "").replace(/\B(?=(\d{3})+(?!\d))/g, ","));
			}
			$(elem).on("input", function() {
				$(elem).val($(elem).val().replace(/[^0-9]/g, "").replace(/\B(?=(\d{3})+(?!\d))/g, ","));
			});
			$(elem).on("focus", function() {
				$(elem).val($(elem).val().replace(/[^0-9]/g, "").replace(/\B(?=(\d{3})+(?!\d))/g, ","));
			});
			$(elem).on("blur", function() {
				$(elem).val($(elem).val().replace(/[^0-9]/g, "").replace(/\B(?=(\d{3})+(?!\d))/g, ","));
			});
		}
		//data-positive
		else
		if($(elem).is('[data-positive]')){
			$(elem).css("text-align","right");
			if(!isEmpty($(elem).val())){
				$(elem).val(Number($(elem).val().replace(/[^0-9]/g, "")));
			}
			$(elem).on("input", function() {
				$(elem).val($(elem).val().replace(/[^0-9]/g, ""));
			});
			$(elem).on("focus", function() {
				if(!isEmpty($(elem).val())){
					$(elem).val(Number($(elem).val().replace(/[^0-9]/g, "")));
				}
			});
			$(elem).on("blur", function() {
				if(!isEmpty($(elem).val())){
					$(elem).val(Number($(elem).val().replace(/[^0-9]/g, "")));
				}
			});
		}
		//data-number
		else
		if($(elem).is('[data-number]')){
			$(elem).css("text-align","right");
			if(!isEmpty($(elem).val())){
				$(elem).val(Number($(elem).val().replace(/[^-\.0-9]/g, "")));
			}
			$(elem).on("input", function() {
				$(elem).val($(elem).val().replace(/[^-\.0-9]/g, "").replace(/^(-?)([0-9]*)(\.?)([^0-9]*)([0-9]*)([^0-9]*)/,"$1$2$3$5"));
			});
			$(elem).on("focus", function() {
				if(!isEmpty($(elem).val())){
					$(elem).val(Number($(elem).val().replace(/[^-\.0-9]/g, "")));
				}
			});
			$(elem).on("blur", function() {
				if(!isEmpty($(elem).val())){
					$(elem).val(Number($(elem).val().replace(/[^-\.0-9]/g, "")));
				}
			});
		}
		//data-integer
		else
		if($(elem).is('[data-integer]')){
			$(elem).css("text-align","right");
			if(!isEmpty($(elem).val())){
				$(elem).val(Number($(elem).val().replace(/[^0-9-]/g, "")));
			}
			$(elem).on("input", function() {
				$(elem).val($(elem).val().replace(/[^0-9-]/g, "").replace(/^(-?)([0-9]*)([^0-9]*)/g,"$1$2"));
			});
			$(elem).on("focus", function() {
				if(!isEmpty($(elem).val())){
					$(elem).val(Number($(elem).val().replace(/[^0-9-]/g, "")));
				}
			});
			$(elem).on("blur", function() {
				if(!isEmpty($(elem).val())){
					$(elem).val(Number($(elem).val().replace(/[^0-9-]/g, "")));
				}
			});
		}
	});
}

//byte 크기만큼 입력
function dataMaxLength(value , maxLength){
	if(isEmpty(value)) return "";
	var byte_length = getByteLength(value);
	if(byte_length > maxLength){
		var last_cut_value = value.substring(0,value.length-1);
		return dataMaxLength(last_cut_value , maxLength);
	}
	return value;
}

//row data
function getRowData(_list, rowIdx) {
	if(rowIdx < 0 || _list.length == 0) return null;
	return _list[rowIdx];
}

//getValue
function getValue(_list, rowIdx, column) {
	var data = getRowData(_list, rowIdx);
	if(data == null) return "";
	return data[column];
}

//Grid Data Null value __modified__ 활용
function initGridDataNullValue (_list , _columns, adColumnObj) {
	var adColumn = [];
	if(!isEmpty(adColumnObj) && Array.isArray(adColumnObj)) {
		adColumn = adColumnObj;
	}
	else if(!isEmpty(adColumnObj)){
		adColumn = adColumnObj.split(",");
    }
	$.each(_list, function(idx, obj){
		$.each(_columns, function(cidx, col){
			if(isEmpty(obj[col.key])){
				obj[col.key] = "";
			}
			for(var i=0; i<adColumn.length; i++){
				if(col.key == adColumn[i]){
					if(isEmpty(obj["__old__"+col.key])){
						obj["__old__"+col.key] = obj[col.key];
					}
				}
			}
		});
		if(isEmpty(obj["__modified__"])){
			obj["__modified__"] = false;
		}
	});
	return _list;
}

//값을 비교하여 이전데이타와 같으면 true return;
function isSameValue(_list, rowIdx, adColumnObj){
	var result = false;
	var adColumn = [];
	if(!isEmpty(adColumnObj) && Array.isArray(adColumnObj)) {
		adColumn = adColumnObj;
	}
	else if(!isEmpty(adColumnObj)){
		adColumn = adColumnObj.split(",");
    }
	var data = getRowData(_list, rowIdx);
	for(var i=0; i<adColumn.length; i++) {
		var col_id = adColumn[i];
		var old_col_id = "__old__"+col_id;
		if(data[col_id] != data[old_col_id]){
			return false;
		}
		else {
			result = true;
		}
	}
	return result;
}

//해당글자가 한글인지
function is_hangul_char(ch) {
	var c = ch.charCodeAt(0);
	if( 0x1100<=c && c<=0x11FF ) return true;
	if( 0x3130<=c && c<=0x318F ) return true;
	if( 0xAC00<=c && c<=0xD7A3 ) return true;
	return false;
}

//한글입력수
function get_hangul_length(text_val){
	if(isEmpty(text_val)) return 0;
    const text_len = text_val.length;

    let totalByte=0;
    for(let i=0; i<text_len; i++){
    	const each_char = text_val.charAt(i);
        const uni_char = escape(each_char);
        if(uni_char.length>4){
            totalByte += 1;
        }
    }
    return totalByte ;
}

//전체 byte수 오라클3byte 기준
function getByteLength(text_val){
	if(isEmpty(text_val)) return 0;
    const text_len = text_val.length;
    let totalByte=0;
    for(let i=0; i<text_len; i++){
    	const each_char = text_val.charAt(i);
        const uni_char = escape(each_char);
        if(uni_char.length>4){
        	// 한글 : 3Byte
            totalByte += 3;
        }else{
        	// 영문,숫자,특수문자 : 1Byte
            totalByte += 1;
        }
    }
    return totalByte ;
}

//값체크
function isEmpty(str){
	if(typeof str == "undefined" || str == null || (str+"") == "")
		return true;
	else
		return false ;
}

/**
 * 화면 load 시 사용
 * @param _form
 * @param _data
 * @param _callback
 * @returns
 * 예1) loadFormData("#popForm", data.result);
 * 예2) loadFormData("#popForm", data.result, function(data){ $('#trnsDiv').trigger('change'); });
 */
function loadFormData(_form, _data, _callback) {
	$.each(_data, function(key, value) {
		if(!isEmpty(value)) {
			$(_form+" [name="+key+"]").val(value);
		}
	});
	initLoadForm(_form);
	if(!isEmpty(_callback)){ _callback(_data); }
}

/**
 * 화면 에서 검색 param return
 * @param _form
 * @param _pageNo
 * @param _recordCnt
 * @returns
 * 예1) var formData = getSearchParam("#popForm");
 * 예2) var paramObj = getSearchParam(".table_input", 1, 99999999);
 */
function getSearchParam(_form, _pageNo, _recordCnt){
	let resultParam = {};
	$.each($(_form+" input"), function (idx, elem) {
		var _id = $(elem).attr('id');
		if(!isEmpty(_id)){
			var _idrxIndex = _id.indexOf("_");
			if(_idrxIndex != -1) {
				var set_id = _id.substring(0,_idrxIndex);
				resultParam[set_id] = $(elem).val();
			}
			else {
				resultParam[_id] = $(elem).val();
			}
		}
	});
	$.each($(_form+" select"), function (idx, elem) {
		var _id = $(elem).attr('id');
		if(!isEmpty(_id)){
			var _idrxIndex = _id.indexOf("_");
			if(_idrxIndex != -1) {
				var set_id = _id.substring(0,_idrxIndex);
				resultParam[set_id] = $(elem).val();
			}
			else {
				resultParam[_id] = $(elem).val();
			}
		}
	});

	if(!isEmpty(_pageNo)){
		resultParam["pageNo"] = _pageNo;
	}
	if(!isEmpty(_recordCnt)){
		resultParam["recordCnt"] = _recordCnt;
	}
	return resultParam;
}
//리스트 검색하여 일치한 Index return
function findIndexArray(_list, column, value, _startIndex, _endIndex) {
	var start_index = "0";
	var end_index = _list.length;
	if(!isEmpty(_startIndex)) start_index = ""+_startIndex;
	if(!isEmpty(_endIndex)) end_index = ""+_endIndex;

	var resultArray = [];
	if( _list.length > 0 ) {
        $.each($.extend({}, _list), function (idx, elem) {
        	if(Number(idx) >= Number(start_index)
        			&& Number(idx) <= Number(end_index)){
				if(elem[column] == value) {
					resultArray.push(idx);
				}
        	}
        });
	}
	return resultArray;
}

//내림차순
function ascSort(rArray){
	rArray.sort(function(a, b) {
		  return a - b;
	});
}

//오름차순
function descSort(rArray){
	rArray.sort(function(a, b) {
		  return b - a;
	});
}

//그리드 중복체크 중복된값 set ""
function gridOverLapCheckSetNull(gridObj, _column) {
	var _list = gridObj.getList();
	for(var idx=_list.length-2; idx >= 0; idx--){
		_list = gridObj.getList();
		var elem = _list[idx];
		var value = elem[_column];
		//var check_idx = _list.length - idx -2;
		if(!isEmpty(value)) {
			var idxArray = findIndexArray(_list, _column, value);
			if(idxArray.length > 1){
				var setIndex = idxArray[1];
				gridObj.setValue(setIndex, _column, "");
			}
		}
	}
}

//null체크 replace
function xreplace(obj,str1,str2) {
	if(isEmpty(obj)) return "";
	return obj.replace(str1,str2);
}

//null체크 trim
function trim(str) {
	if(isEmpty(str)) return "";
	return str.replace(/^\s+|\s+$/g,"");
}

//콤보데이타에서 text로 code return
function getOptionsCode(options,text_value) {
	for(var i=0; i<options.length; i++){
		var elem = options[i];
		if(elem.text == text_value){
			return elem.value;
		}
	}
	return "";
}

//콤보데이타에서code로 text return
function getOptionsText(options,code_value) {
	for(var i=0; i<options.length; i++){
		var elem = options[i];
		if(elem.value == code_value){
			return elem.text;
		}
	}
	return "";
}

//보이는 컬럼리스트
function getViewColumnKey(gridObj, notPushColumns){
	var viewColumnsKey = [];
	var notPushColumnsKey = [];
	if(!isEmpty(notPushColumns)) notPushColumnsKey = notPushColumns;
	var gridColumns = gridObj.colGroup;
	$.each(gridColumns, function (idx, elem) {
		var isNotPushColumns = true;
		for(var i=0; i<notPushColumnsKey.length; i++) {
			if(notPushColumnsKey[i] == elem.key){
				isNotPushColumns = false;
				break;
			}
		}
		if(isNotPushColumns){
			viewColumnsKey.push(elem.key);
		}
	});
	return viewColumnsKey;
}

//edit 컬럼리스트
function getEditorColumnKey(gridObj, notPushColumns){
	var editorColumnsKey = [];
	var notPushColumnsKey = [];
	if(!isEmpty(notPushColumns)) notPushColumnsKey = notPushColumns;
	var gridColumns = gridObj.colGroup;
	$.each(gridColumns, function (idx, elem) {
		var isNotPushColumns = true;
		for(var i=0; i<notPushColumnsKey.length; i++) {
			if(notPushColumnsKey[i] == elem.key){
				isNotPushColumns = false;
				break;
			}
		}
		if(!isEmpty(elem.editor) && isNotPushColumns){
			editorColumnsKey.push(elem.key);
		}
	});
	return editorColumnsKey;
}

//해당 TreeGrid 의 __children__ 제거 , 이중배열제거, 저장가능 데이타로 변경한다.
function deleteChildren(_list){
	$.each(_list, function (idx, elem) {
		delete elem.__children__;
	});
	return _list;
}

//현재데이타로 계층관계의 Index를 return 한다.
//사용예) var lowIndexArray = findLowerArray(bom_Arr, "id_value"); //pid,id 계층관계 컬럼으로 사용시 생략
//사용예) var lowIndexArray = findLowerArray(bom_Arr, "lowerCd","upperCd","lowerCd"); //upperCd,lowerCd 를 계층관계 컬럼으로 사용시
findLowerArray = function(_list, _lowerCd, _upperCol, _lowerCol) {
	var upperCol = "pid";
	var lowerCol = "id";
	if(!isEmpty(_upperCol)) upperCol = _upperCol;
	if(!isEmpty(_lowerCol)) lowerCol = _lowerCol;
	var resultArray = [];
	selecLowerIndex(_list ,resultArray , _lowerCd, upperCol, lowerCol);
	return resultArray;
}

//하위계층을 찾아 Index를 rArray 에 저장
selecLowerIndex = function(_list, rArray, value, _upperCol, _lowerCol) {
	if( _list.length > 0 ) {
        $.each($.extend({}, _list), function (idx, elem) {
        	var isIdx = true;
        	for(var i=0; i<rArray.length; i++) {
        		if(idx == rArray[i]) {
        			isIdx = false;
        			break;
        		}
        	}
        	var upperCd = elem[_upperCol];
        	var lowerCd = elem[_lowerCol];
			if(elem[_upperCol] == value) {
				if(isIdx) rArray.push(idx);
				selecLowerIndex(_list, rArray, lowerCd, _upperCol, _lowerCol);//재귀메소드
			}
        });
	}
}

//엑셀 데이타를 그리드 데이타로 return; /user/sm/sm01/uploadExcelFile 사용시 Excel 데이타를 그리드 데이타로 변환.
function getGridExcelData(columnsKey, dataList){
	var resultData = [];
		$.each($.extend({}, dataList), function (idx, elem) {
			if(idx != "0")
			{
	 			var data ={};
	 			for(var i=0; i<columnsKey.length; i++){
	 				var colKey = columnsKey[i];
	 				var value = elem["key"+(i+1)];
	 				data[colKey] = value;
	 			}
	 			resultData.push(data);
			}
	});
	return resultData;
}

//---------------------------------------------------------------------------------
//그리드 엑셀 export 작업용
//메인화면 스크립트 삽입 필요
//<script src="https://unpkg.com/exceljs/dist/exceljs.min.js"></script>
//format --> 엑셀문서는 자동으로  엑셀문서이름_yyyymmdd.xlsx로 생성됨
//exportJSONToExcel(json Data, 헤더 정의 자료, 엑셀문서이름);
//
//	function jsonToExcel (grid) {
// 		const targetJson = grid.target.getList();
// 		const targetHeader = grid.target.columns;
// 		exportJSONToExcel(targetJson, targetHeader, '프로젝트이슈');
// 	}
//---------------------------------------------------------------------------------
function exportJSONToExcel (_excelJsonData, _excelHeader, _excelFileName = 'excel', _hiddenField = false) {
	if (!_excelJsonData) {
		alert('엑셀로 변환할 자료가 없습니다.')
		return false;
	}
//	let _excelJsonData = _grid.target.getList();
//	let _excelHeader = _grid.target.columns;
	// 엑셀 워크북 생성
	let workbook = new ExcelJS.Workbook();
	let worksheet = workbook.addWorksheet(_excelFileName+"Sheet1");

	// 헤더 스타일 설정
	const headerFill = {
		type: "pattern",
		pattern: "solid",
		fgColor: { argb: "C0C0C0" } // 그레이 색상
	};

	const headerFont = {
		name: '나눔고딕',
		size: 10,
		bold: true,
		color: { argb: "000000" } // 검은색 글자
	};

	/*
	 * thick: 굵은 선
	 * medium: 중간 두께의 선
	 * thin:  얇은 선
	 * dotted: 점선
	 * dashDot: 대시-점선
	 * dashDotDot: 대시-점-점선
	 * slantDashDot: 사선 대시-점선
	 */
	const headerBorder = {
		top: { style: "thin" },
		bottom: { style: "thin" },
		left: { style: "thin" },
		right: { style: "thin" }
	};
	const headerAlignment = {
		horizontal: "center" // 가운데 정렬
	};
	const dataFont = {
		name: '나눔고딕',
		size: 10
	};
	// 헤더 셀 생성
	let outFieldArr = [];
	let outCellNo = 0;
	let lastRow = 0;
	$.each(_excelHeader, function(i, header) {
		if (!_excelHeader[i].hidden || _hiddenField) {
		    outCellNo += 1;
		    outFieldArr.push(_excelHeader[i].key);
		    let cell = worksheet.getCell(1, outCellNo);
		    cell.value = _excelHeader[i].label;
		    cell.fill = headerFill;
		    cell.font = headerFont;
		    cell.border = headerBorder;
		    cell.alignment = headerAlignment;
		    //셀폭은 기본 그리드 헤드 넓이를 기준으로 70% 크기로 셀폭을 조정하고 이후 각 컬럼 자료를 전환하면서 문자길이에 따라 조정합니다.
		    let cellWidth = typeof _excelHeader[i].width === 'number' ? _excelHeader[i].width / 7 : 10;
		    worksheet.getColumn(outCellNo).width =  Math.min(cellWidth, 10);
		}
	});

	// 데이터 삽입
	for (let row = 0; row < _excelJsonData.length; row++) {
		let key = Object.keys(_excelJsonData[row]);
		var data = _excelJsonData[row];

		$.each(outFieldArr, function(col, cellKey) {
			let cell = worksheet.getCell(row + 2, col + 1);
			let number = data[cellKey];
			let lcCellKey = cellKey.toLowerCase();
			//숫자 필드는 숫자형 자료로 전환합니다.
			if (
				lcCellKey.indexOf('qty') !== -1 ||
				lcCellKey.indexOf('amt') !== -1 ||
				lcCellKey.indexOf('seq') !== -1 ||
				lcCellKey.indexOf('upr') !== -1 ||
				lcCellKey.indexOf('rate') !== -1 ||
				lcCellKey.indexOf('cost') !== -1 ||
				lcCellKey.indexOf('size') !== -1 ||
				lcCellKey.indexOf('pct') !== -1
			) {
					if (/^\d+$/.test(data[cellKey])) {
						number = parseFloat(data[cellKey]);
					}

			}

			cell.value = number;
			if (typeof number === 'number') {
				cell.numFmt = '#,##0'; // 숫자 형식 지정
				cell.alignment = { horizontal: 'right' }; // 오른쪽 정렬
			}

		    cell.font = dataFont;
			cell.border = headerBorder;
			//셀폭은 기본 그리드 헤드 넓이를 기준으로 70% 크기의 셀폭을 최소 길이로 하고 컬럼 문자길이에 따라 조정합니다.
			var cellValue = number !== undefined ? number.toString() : '';
			const curWidth = worksheet.getColumn(col + 1).width;
			worksheet.getColumn(col + 1).width = Math.max(curWidth, cellValue.toString().length * 1.0);
		});

		lastRow = row + 2;
	}

	//Sum row 색성  ????필요할까?
//	lastRow += 1
//	let cell = worksheet.getCell(lastRow, 1);
//	cell.value = '합계'
//	cell.alignment = { horizontal: 'center' }; // 오른쪽 정렬
//	worksheet.mergeCells(lastRow , 1, lastRow, 4);
//
//	let cell = worksheet.getCell(lastRow, 7);
//	cell.value = { formula: 'SUM(A1:D1)' };

	// 파일 저장
	workbook.xlsx.writeBuffer().then(function(buffer) {
		let blob = new Blob([buffer], { type: "application/octet-stream" });
		let dt = moment(new Date()).format('YYYYMMDD');
		let excelFileName = _excelFileName + '_' + dt + '.xlsx';
		if (window.navigator.msSaveBlob) {
			// IE 10+
			window.navigator.msSaveBlob(blob, excelFileName);
		} else {
			let link = document.createElement("a");
			link.href = window.URL.createObjectURL(blob);
			link.download = excelFileName;
			link.click();
		}
	});

}