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

var accessToken = localStorage.getItem("access_token");
var authorizationToken = '';
var jwt = '';
if (accessToken) {
   var authorizationToken = "Bearer " + accessToken;
   var jwt = parseJwt(authorizationToken);
} else {
   // 토큰이 없는 경우 처리
   console.log("Access token not found");
   // redirectToLogin();
}
var menuIdx = getCookie("menuIdx");

if (typeof ax5 !== 'undefined' && typeof ax5.ui !== 'undefined') {
	if (typeof ax5.ui.mask === 'function') {
		var mask = new ax5.ui.mask();
		var secondMask = new ax5.ui.mask();
		var thirdMask = new ax5.ui.mask();
	}
	if (typeof ax5.ui.modal === 'function') {
		var modal = new ax5.ui.modal();			//메인모달
		var secondModal = new ax5.ui.modal();	//popup모달
		var thirdModal = new ax5.ui.modal();	//추가1
		var openFourthModal = new ax5.ui.modal();	//추가2
		var blindModal = new ax5.ui.modal();
	}
	if (typeof ax5.ui.toast === 'function') {
		var toast = new ax5.ui.toast();
		toast.setConfig({
		    width: 500,
		    displayTime: 5000,
		    icon: '<i class="fa fa-bell"></i>',
		    containerPosition: "top-left",
		    closeIcon: '<i class="fa fa-times"></i>'
		});

		toast.init();
	}
	if (typeof ax5.ui.grid === 'function') {
		// 그리드 총건수 표기 커스텀
//		ax5.ui.grid.tmpl.page_status = function(){return '<span>총 {{totalElements}}건</span>';};
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

}
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
        ubiprefix = "https://gbs.gunyangitt.co.kr:8443/ubi4";
        break;
    case "dev" :
        ubiprefix = "http://localhost:8090/ubi4";
        break;
    case "local" :
        ubiprefix = "https://localhost:8443/ubi4";
        break;
    default :
        ubiprefix = "https://gbs.gunyangitt.co.kr:8443/ubi4";
	}
}

var openModal = function(url, width, height, title, paramObj, callback) {
	modal.open({
		header: {
			title: title,
	        btns: {
//	            minimize: {
//	                label: '<i class="fa fa-minus-circle" aria-hidden="true"></i>', onClick: function () {
//	                    modal.minimize();
//	                }
//	            },
//	            maximize: {
//	                label: '<i class="fa fa-plus-circle" aria-hidden="true"></i>', onClick: function () {
//	                	modal.css({width:$(window).width(), height: $(window).height()});
//	                	let ax5gridObj = $('[data-ax5grid]');
//	                	$('[data-ax5grid]').each(function() {
//	                		  var ax5gridAttributeValue = $(this).attr('data-ax5grid');
//	                		  console.log('Grid Name:', ax5gridAttributeValue);
//	                	});
//	                }
//	            },
	        	close: {
//	                label: '<i class="fa fa-times-circle" aria-hidden="true"></i>',
	                label: '<img src="/static/img/close.ico" style="width: 20px; height: 20px;" alt="Close Icon">',
	                onClick: function () {
	                	if (typeof setMouseTrackerVisible === 'function') {
	                		setMouseTrackerVisible(false);
	                	}
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
//	                label: '<i class="fa fa-times-circle" aria-hidden="true"></i>',
	                label: '<img src="/static/img/close.ico" style="width: 20px; height: 20px;" alt="Close Icon">',
	                onClick: function () {
	                	if (typeof setMouseTrackerVisible === 'function') {
	                		setMouseTrackerVisible(false);
	                	}
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
                secondMask.open();
        		var modalObj = {
                	"target": this.self,
                	"paramObj": paramObj,
                	"callback": callback
                }
                modalStack.push(modalObj);
        	} else if (this.state === "close") {
        		secondMask.close();
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
//	                label: '<i class="fa fa-times-circle" aria-hidden="true"></i>',
	                label: '<img src="/static/img/close.ico" style="width: 20px; height: 20px;" alt="Close Icon">',
	                onClick: function () {
	                	if (typeof setMouseTrackerVisible === 'function') {
	                		setMouseTrackerVisible(false);
	                	}
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
                thirdMask.open();
        		var modalObj = {
                	"target": this.self,
                	"paramObj": paramObj,
                	"callback": callback
                }
                modalStack.push(modalObj);
        	} else if (this.state === "close") {
        		thirdMask.close();
            }
        }
    }, function () {
    	var targetEl = this.$["body-frame"];
    	$.get(url, function(data) {
    		targetEl.append(data);
      	});
    });
};

var openFourthModal = function(url, width, height, title, paramObj, callback) {
	thirdModal.open({
		header: {
			title: title,
			btns: {
	        	close: {
//	                label: '<i class="fa fa-times-circle" aria-hidden="true"></i>',
	                label: '<img src="/static/img/close.ico" style="width: 20px; height: 20px;" alt="Close Icon">',
	                onClick: function () {
	                	if (typeof setMouseTrackerVisible === 'function') {
	                		setMouseTrackerVisible(false);
	                	}
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
                thirdMask.open();
        		var modalObj = {
                	"target": this.self,
                	"paramObj": paramObj,
                	"callback": callback
                }
                modalStack.push(modalObj);
        	} else if (this.state === "close") {
        		thirdMask.close();
            }
        }
    }, function () {
    	var targetEl = this.$["body-frame"];
    	$.get(url, function(data) {
    		targetEl.append(data);
      	});
    });
};

var openBlindModal = function(url, width, height, title, paramObj, callback) {
	blindModal.open({
		header: {
			title: title,
			btns: {close: {}
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

//	console.log('parseJwt !!!');
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



function setupAuthorization(headerValue) {
	$.ajaxSetup({
		contentType: "application/x-www-form-urlencoded; charset=utf-8",
		beforeSend: function (request)
        {
            request.setRequestHeader("Authorization", headerValue);
        }
	});
}


// refresh_token 요청용 함수
const clientId = "frontend-app";
const clientSecret = "gunyang";
const basicAuth = btoa(`${clientId}:${clientSecret}`);

function refreshAccessToken(callback) {
    const refreshData = {
        grant_type: "refresh_token",
		refresh_token: localStorage.getItem("refresh_token"),
		username  	: jwt.userId,
		id  		: jwt.userId,
    };

    $.ajax({
        type: "POST",
        url: "/oauth/token",
        data: $.param(refreshData),
        async: false,                               // ← 동기 호출
        xhrFields: { withCredentials: true }, // 반드시 있어야 쿠키 전송 refresh_token은 필수임
        headers: {
            "Authorization": "Basic " + basicAuth,
            "Content-Type": "application/x-www-form-urlencoded; charset=utf-8",
            "Accept": "application/json",
        },
        success: function(data) {
            if (data.access_token) {
                localStorage.setItem("access_token", data.access_token);
                authorizationToken = "Bearer " + data.access_token;
                jwt = parseJwt(authorizationToken);
                callback(true);
            } else {
                callback(false);
            }
        },
        error: function(xhr, textStatus, errorThrown) {
            console.error("❌ Refresh Token 실패");
            logoutClick();
            location.href = isMobile() ? "/static/mobile/index.html" : "/static/index.html";
//            if (xhr.status === 401) {
//                logoutClick();
//                location.href = isMobile() ? "/static/mobile/index.html" : "/static/index.html";
//            } else if (xhr.status === 400) {
//                try {
//                    const errorResponse = JSON.parse(xhr.responseText);
//                } catch (e) {
//                    console.error("📋 에러 응답 파싱 실패:", xhr.responseText);
//                }
//            } else {
//                console.warn("🔴 알 수 없는 오류 발생");
//            }
            callback(false);
        }
    });
}

var tokenErrorMsg = ["unauthorized", "invalid_token", "invalid_grant"];

function postAjax(url, data, contentType, callback, blockProc=true, retryCount = 0) {
	if (typeof $.blockUI === 'function' && blockProc) openProgress(true);
//	console.log(`postAjax url = ${url} `);
	if(contentType == null) {
		contentType = "application/json; charset=utf-8";
		data = JSON.stringify(data);
	} else if(contentType == "form") {
		contentType = "application/x-www-form-urlencoded; charset=utf-8";
	} else {
		contentType = contentType;
	}
	$.ajax({
	    type: "POST",
	    url: url,
	    contentType: contentType,
	    data: data,
	    headers: {"Authorization": authorizationToken},
	    success: function(data){
	    	callback(data);
	    },
	    error: function (xhr) {
            const error = xhr.responseJSON?.error;
            if (tokenErrorMsg.indexOf(error) > -1) {
                // refresh 요청
                refreshAccessToken(function(success) {
                    if (success) {
                    	postAjax(url, data, contentType, callback, blockProc, 1); // 또는 실패 시 callback 재시도 등
                    } else {
                        location.href = isMobile() ? "/static/mobile/index.html" : "/static/index.html";
                    }
                });

                return;
            }

            // 기타 오류가 있으면 로그 출력
            console.error("요청 실패:", xhr);
	    },
        complete: function() {
        	if (typeof $.blockUI === 'function' && blockProc) openProgress(false);
        }
	});
}


function postAjaxSync(url, data, contentType, callback, retryCount = 0) {

//	console.log(`postAjaxSync url = ${url} `);
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
	    error: function (xhr) {
            const error = xhr.responseJSON?.error;
            if (tokenErrorMsg.indexOf(error) > -1) {
                // refresh 요청
                refreshAccessToken(function(success) {
                    if (success) {
                    	postAjaxSync(url, data, contentType, callback, 1); // 또는 실패 시 callback 재시도 등
                    } else {
                        location.href = isMobile() ? "/static/mobile/index.html" : "/static/index.html";
                    }
                });

                return;
            }

            // 기타 오류가 있으면 로그 출력
            console.error("요청 실패:", xhr);
	    },
	});
}

function deleteAjax(url, data, contentType, callback, blockProc=true, retryCount = 0) {
	if (typeof $.blockUI === 'function' && blockProc) openProgress(true);
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
	    error: function (xhr) {
            const error = xhr.responseJSON?.error;
            if (tokenErrorMsg.indexOf(error) > -1) {
                // refresh 요청
                refreshAccessToken(function(success) {
                    if (success) {
                    	deleteAjax(url, data, contentType, callback, blockProc, 1); // 또는 실패 시 callback 재시도 등
                    } else {
                        location.href = isMobile() ? "/static/mobile/index.html" : "/static/index.html";
                    }
                });

                return;
            }

            // 기타 오류가 있으면 로그 출력
            console.error("요청 실패:", xhr);
	    },
        complete: function() {
        	if (typeof $.blockUI === 'function' && blockProc) openProgress(false);
        }
	});
}

function putAjax(url, data, contentType, callback, blockProc=true, retryCount = 0) {
	if (typeof $.blockUI === 'function' && blockProc) openProgress(true);
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
	    error: function (xhr) {
            const error = xhr.responseJSON?.error;
            if (tokenErrorMsg.indexOf(error) > -1) {
                // refresh 요청
                refreshAccessToken(function(success) {
                    if (success) {
                    	putAjax(url, data, contentType, callback, blockProc, 1); // 또는 실패 시 callback 재시도 등
                    } else {
                        location.href = isMobile() ? "/static/mobile/index.html" : "/static/index.html";
                    }
                });

                return;
            }

            // 기타 오류가 있으면 로그 출력
            console.error("요청 실패:", xhr);
	    },
        complete: function() {
        	if (typeof $.blockUI === 'function' && blockProc) openProgress(false);
        }
	});
}

function filePostAjax(url, data, callback, blockProc=true, retryCount = 0) {
	if (typeof $.blockUI === 'function' && blockProc) openProgress(true);
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
	    error: function (xhr) {
            const error = xhr.responseJSON?.error;
            if (tokenErrorMsg.indexOf(error) > -1) {
                // refresh 요청
                refreshAccessToken(function(success) {
                    if (success) {
                    	filePostAjax(url, data, callback, blockProc, 1); // 또는 실패 시 callback 재시도 등
                    } else {
                        location.href = isMobile() ? "/static/mobile/index.html" : "/static/index.html";
                    }
                });

                return;
            }

            // 기타 오류가 있으면 로그 출력
            console.error("요청 실패:", xhr);
	    },
        complete: function() {
        	if (typeof $.blockUI === 'function' && blockProc) openProgress(false);
        }
	});
}

function filePostAjaxButton(url, data, callback, blockProc=true, retryCount = 0) {
	if (typeof $.blockUI === 'function' && blockProc) openProgress(true);

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
	    error: function (xhr) {
            const error = xhr.responseJSON?.error;
            if (tokenErrorMsg.indexOf(error) > -1) {
                // refresh 요청
                refreshAccessToken(function(success) {
                    if (success) {
                    	filePostAjaxButton(url, data, callback, blockProc, 1); // 또는 실패 시 callback 재시도 등
                    } else {
                        location.href = isMobile() ? "/static/mobile/index.html" : "/static/index.html";
                    }
                });

                return;
            }

            // 기타 오류가 있으면 로그 출력
            console.error("요청 실패:", xhr);
	    },
        complete: function() {
        	if (typeof $.blockUI === 'function' && blockProc) openProgress(true);
        }
	});
}

function filePutAjax(url, data, callback, blockProc=true, retryCount = 0) {
	if (typeof $.blockUI === 'function' && blockProc) openProgress(true);
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
	    error: function (xhr) {
            const error = xhr.responseJSON?.error;
            if (tokenErrorMsg.indexOf(error) > -1) {
                // refresh 요청
                refreshAccessToken(function(success) {
                    if (success) {
                    	filePutAjax(url, data, callback, blockProc, 1); // 또는 실패 시 callback 재시도 등
                    } else {
                        location.href = isMobile() ? "/static/mobile/index.html" : "/static/index.html";
                    }
                });

                return;
            }

            // 기타 오류가 있으면 로그 출력
            console.error("요청 실패:", xhr);
	    },
        complete: function() {
        	if (typeof $.blockUI === 'function' && blockProc) openProgress(false);
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

function ubiExportAjaxSync(fileName, arg, callback) {
	var url = ubiprefix + "/ubiexport.jsp";
	url += "?file="+fileName;
	url += "&arg="+encodeURIComponent(arg);
	// Report 서버에 요청 보내기
	$.ajax({
		url: url,
		method: "GET",
		dataType: "html",
	    async: false,
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

function downloadImageFromBase64(base64Data, fileName, fileType , type) {
    var byteCharacters = atob(base64Data); // Base64 디코딩
    var byteNumbers = new Array(byteCharacters.length);

    for (var i = 0; i < byteCharacters.length; i++) {
        byteNumbers[i] = byteCharacters.charCodeAt(i);
    }
    var byteArray = new Uint8Array(byteNumbers);
    var file = "";
    if (type == "image") {
    	file = new Blob([byteArray], { type: 'image/' + fileType , lastModified: Date.now() }); // 이미지 파일로 설정, 필요한 경우 수정 가능
    } else {
        file = new File([blob], fileName,  { type: 'application/octet-stream' , lastModified: new Date()});
    }

	return file;
}



function base64ToFile(base64Data, fileName, fileType , type) {
    const byteCharacters = atob(base64Data);
    const byteArray = new Uint8Array(byteCharacters.length);

    for (let i = 0; i < byteCharacters.length; i++) {
        byteArray[i] = byteCharacters.charCodeAt(i);
    }

    const blob = new Blob([byteArray]);
    const file = new File([blob], fileName,  { type: 'application/octet-stream' , lastModified: new Date()});

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
//		$(elem).val(addCommaStr(deleteCommaStr($(elem).val().replace(regExp, "$1$2$3"))));
		var cleanedValue = elem.value.replace(regExp, "$1$2$3");
        cleanedValue = cleanedValue.replace(/,/g, '').replace(/^0+/, '');
        $(elem).val(addCommaStr(cleanedValue));
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
	if (elem && elem.value && typeof elem.value === "string") {
	    elem.value = elem.value.replace(/[^0-9]/g, "");
	}
//	$(elem).val($(elem).val().replace(/[^0-9]/g,""));
}

// 한글 제거
function exceptKorean(elem){
	if (elem && elem.value && typeof elem.value === "string") {
		elem.value = elem.value.replace(/[a-h|a-ㅣ|ga-heh]/g, "");
	}
//	$(elem).val($(elem).val().replace(/[ㄱ-ㅎ|ㅏ-ㅣ|가-힣]/g,""));
}

// 계좌번호 (숫자, 하이픈만 허용)
function onlyBkac(elem){
	if (elem && elem.value && typeof elem.value === "string") {
		$(elem).val($(elem).val().replace(/^[-]|[^0-9-]/gi,""));
	}
}

// 전화번호 포맷 변경
function telNumberFormatter(elem){
	if (elem && elem.value && typeof elem.value === "string") {
		onlyDecimal(elem);
		$(elem).val($(elem).val().replace(/(^02.{0}|^01.{1}|[0-9]{3})([0-9]+)([0-9]{4})/g,"$1-$2-$3"));
	}
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
	if (elem && typeof elem.value === "string") {
	    elem.value = elem.value.replace(/,/g, "");
	}
//	$(elem).val($(elem).val().replace(/,/g, ""));
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
	if (typeof value !== 'string') return value;

//    return value.toString().replace(/,/g, "");
	let num = value.replace(/[^-0-9,.]/g, '');
    return num.replace(/,/g, '');
}

// 하이픈 제거
function deleteHyphen(elem){
	$(elem).val($(elem).val().replace(/-/g, ""));
}

// 하이픈 제거 스트링변수용
function deleteHyphenStr(value){
	if (value == undefined ) return "";
	return value.replace(/-/g, "");
}

//var authArr;
// 권한에 따른 메뉴 보여주기
function setMenuAuth() {
	var formData = {
		"authInfo" : jwt.authInfo,
		"userId"   : jwt.userId
	}
	postAjax("/selectMenuAuth", formData, null, function(data) {
		if (Array.isArray(data?.accessList) && data.accessList.length > 0) {
//			authArr = data.accessList;
			checkMenuAuth(data.accessList);
		}
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
				html = '<dl><dd><a href="'+item.menuUrl+'" id="'+item.originId+'" onclick="setCookie(\'menuSaveYn\', \''+item.saveYn+'\', 1); insertPgmHistory(\''+item.menuUrl+'\');">'+item.menuNm+'</a></dd></dl>';
				$("#"+item.upMenuId).append(html);
			}
		});

		//+, - ==> 문자 바꿀때는 주의할 것 백단에서 Insert, Delete문 분기문자로 인식하고 있음.
		//if (jobType.equals("+")) 이면 insert 처리, else delete 처리
		//id=U99 는 즐겨찾기 메뉴 ID, 제외하고 메뉴앞에 + 표지를 추가함
		$("#U99 a").before('<span class="minus-icon">-</span>');
		
		//U99의 자손 a태그의 id를 배열로 추가
		let U99DescendantIds = $("#U99 a")
									    .filter(function() {
									        return $(this).attr("id");
									    })
									    .map(function() {
									        return $(this).attr("id");
									    })
									    .get();
		$("dl:not(#U99) > dl > dd > a").each(function() {
			const currentId = $(this).attr("id");
			// #U99의 자손중에 동일 ID가 있는지  확인
		    if (currentId && U99DescendantIds.includes(currentId)) {
		        $(this).before('<span class="minus-icon">-</span>');
		    } else{
		        $(this).before('<span class="plus-icon">+</span>');
		    }
		});
		

        // +, - 클릭 시 addon() 실행
        $(".plus-icon, .minus-icon").click(function(event) {
            event.stopPropagation(); // 부모 요소로 이벤트 전파 방지
            favoritesMenuControl(this);
        });
		
	}


function favoritesMenuControl(obj){
	const jobType = $(obj).text().trim();	//+,- 텍스트 추출
    const $nextA = $(obj).next('a');
    const menuText = $nextA.length ? $nextA.text().trim() : '';
    const menuId = $nextA.length ? $nextA.attr('id') : '';
    
    if (!menuId.startsWith("U")) return false; 
	
	var param = {
		"jobType" 	: jobType,
		"menuId" 	: menuId,
		"menuText" 	: menuText,
		"userId"	: jwt.userId
	};
	postAjaxSync("/admin/cm/cm01/favoritesMenuControl", param , null,  function(data){
		let resultCode = data.resultCode;
		if (resultCode == 200) {
			//즐겨찾기 추가 삭제 완료
			setMenuAuth();	//메뉴정보 갱신
		} else {
			alert(data.resultMessage);
		}
	})
	
}

//로그아웃
function logoutClick(){
	localStorage.removeItem("access_token");
	localStorage.removeItem("refresh_token");
	
	deleteCookie("menuIdx");
	deleteCookie("authArr");
	deleteCookie("menuSaveYn");
	
	$.ajax({
	    type: "GET",
	    url: "/customLogout",
	    xhrFields: { withCredentials: true }, 
	    success: function() {
	        location.href = "/";
	    }
	});
//	location.href = "/";
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
//				if ($(elem).data('kind') == 'CO') {	//회사코드인 경우 관리회사만 가능 함.
//				if ($(elem).attr('ID') == 'coCd_S') {	//회사코드인 경우 관리회사만 가능 함.
//					if (jwt.mngCoCd.includes(item.codeId)) {
////						console.log('관리회사입니다.');
//					} else {
////						console.log('조회불가 회사입니다.');
//						return ;
//					}
//				}
				optionHtml += '<option value="'+item.codeId+'" data-rprc="'+item.codeRprc+'" data-etc="'+item.codeEtc+'" data-desc="'+item.codeDesc+'" data-dz-code="'+item.dzCode+'">';
				optionHtml += item.codeNm;
				optionHtml += '</option>';
			});
			$(elem).append(optionHtml);
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
		"pgmId" : url.substr(url.lastIndexOf("/")+1).replace(".html", "")
	}
	postAjax("/admin/cm/cm06/insertPgmHistory", formData, null, function(data){

	});
}

function callReport(fileName, arg, width, height, reporttitle){
	var url = ubiprefix + "/ubihtml.jsp";
	url += "?file="+fileName;
	url += "&arg="+encodeURIComponent(arg);
	if( typeof(reporttitle) != "undefined" ) {
		url += "&reporttitle="+encodeURIComponent(reporttitle);
	}
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

//function authChk(menuUrl){
//	if(!menuUrl){
//		var url = window.location.href;
//		menuUrl = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("."));
//	}
//	
//		var arr = JSON.parse(getCookie("authArr"));
//        var saveYn = "N";
//
//        //array함수로 기능 대체하고 버튼을 삭제함(버튼을 사용하는 프로그램은 오류 발생 가능)
//        // 버튼 숨김으로 하면 소스 편집하여 강제처리가능으로 위험
//        if (arr) {
//	        const foundMenu = arr.find(item => item.m === menuUrl);
//	        if (foundMenu && foundMenu.s === 'Y') {
//	        	//정상적인 처리가능
//	        } else {
//	            $("[authchk]").remove();
//	            return false;
//	        }
//        } else {
//        	console.error('arr의 값이 비었습니다.');
//        }
////	// select 회사코드 disable (감사용 임시코드)
////	$('select[data-kind="CO"]').prop("disabled", true);
//    	return true;
//}


function authChk(menuUrl){
	if(!menuUrl){
		var url = window.location.href;
		menuUrl = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("."));
	}
	
		var arr = JSON.parse(getCookie("authArr"));
        //array함수로 기능 대체하고 버튼을 삭제함(버튼을 사용하는 프로그램은 오류 발생 가능)
        // 버튼 숨김으로 하면 소스 편집하여 강제처리가능으로 위험
        if (arr) {
	        const foundMenu = arr[0][menuUrl];
	        if (foundMenu === 'Y') {
	        	//정상적인 처리가능
	        } else if (foundMenu === 'N') {
	        	//조회만 가능
	            $("[authchk]").remove();
	            return false;
	        } else {
	        	//권한이 없음
				if(isMobile()){
					location.href = "/static/mobile/index.html";
				}else{
					location.href = "/static/index.html";
				}
	            return false;
	        }
        } else {
        	console.error('권한정보가 없습니다.');
			if(isMobile()){
				location.href = "/static/mobile/index.html";
			}else{
				location.href = "/static/index.html";
			}
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
 * readonly 작동안함.
 * oninput {
 	data-positive : 양수
 	data-number : 양수, 음수, 소수점
 	data-integer : 양수, 음수
 	data-money : 양수 comma
   }
   onkeyup {
    comma
   	data-maxlength : byte크기만큼 입력막기
   }
 * date, date="toDay"
 * */
function initLoadForm(_form){
	$.each($(_form+" textarea"), function (idx, elem) {
		if($(elem).is('[data-maxlength]') && $(elem).is('[readonly]') && $(elem).attr('readonly') == "readonly"){
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
		var not_readonly = true;
		if($(elem).is('[readonly]') && $(elem).attr('readonly') == "readonly"){
			not_readonly = false;
		}
		if($(elem).is('[date]') && not_readonly){
			if(!isEmpty($(elem).val())){
				var val_date = $(elem).val();
				if(val_date.length == 8){
					val_date = val_date.replace(/(\d{4})(\d{2})(\d{2})/, '$1-$2-$3');
				}
				var set_date = new Date(val_date).format("yyyy-MM-dd");//날짜검사.
				$(elem).val(set_date);
				$(elem).datepicker({ //날짜팝업
					format : "yyyy-mm-dd",
					language : "ko",
					autoclose : true
				});
			}
			else {
				var set_date = $(elem).attr('date');//today
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
				if(not_readonly) {
					$(elem).on("keyup", function() {
						$(elem).val(dataMaxLength($(elem).val(),data_maxlength));
					});
				}
			}
		}

		if($(elem).is('[data-money]')){
			$(elem).css("text-align","right");
			if(!isEmpty($(elem).val())){
				$(elem).val($(elem).val().replace(/[^0-9]/g, "").replace(/\B(?=(\d{3})+(?!\d))/g, ","));
			}
			if(not_readonly) {
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
		}
		//data-positive
		else
		if($(elem).is('[data-positive]')){
			$(elem).css("text-align","right");
			if(!isEmpty($(elem).val())){
				$(elem).val(Number($(elem).val().replace(/[^0-9]/g, "")));
			}
			if(not_readonly) {
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
		}
		//data-number
		else
		if($(elem).is('[data-number]') && $(elem).not('[comma]')){
			$(elem).css("text-align","right");
			if(!isEmpty($(elem).val())){
				$(elem).val(Number($(elem).val().replace(/[^-\.0-9]/g, "")));
			}
			if(not_readonly) {
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
		}
		//data-integer
		else
		if($(elem).is('[data-integer]')){
			$(elem).css("text-align","right");
			if(!isEmpty($(elem).val())){
				$(elem).val(Number($(elem).val().replace(/[^0-9-]/g, "")));
			}
			if(not_readonly) {
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
		}

		if($(elem).is('[comma]') && $(elem).not('[data-money]')){
			$(elem).css("text-align","right");
			if(!isEmpty($(elem).val())){
				$(elem).val($(elem).val().replace(/\B(?=(\d{3})+(?!\d))/g, ","));
			}
			if($(elem).is('[data-number]') && not_readonly){
				$(elem).on("blur", function() {
					$(elem).val(addCommaStr(deleteCommaStr($(elem).val())));
				});
			}
			else if(not_readonly) {
				$(elem).on("blur", function() {
					$(elem).val($(elem).val().replace(/[^0-9]/g, "").replace(/\B(?=(\d{3})+(?!\d))/g, ","));
				});
			}
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
	try {
		if(typeof str == "undefined" || str == null || (typeof str == "string" && (str+"") == "") ) {
			return true;
		}
		else {
			if(str) return false;
			else return true;
		}
	} catch (e) {
//		console.log("====isEmpty error>>",e);
		return true;
	}
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
	$.each($(_form+" textarea"), function (idx, elem) {
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

//FormData 데이타에서 comma 제거
function getFormData(_form){
	var formData = new FormData($(_form)[0]);
	for (let key of formData.keys()) {
		var _input = $(_form+' [name="'+key+'"]');
		if(_input.is('[comma]') || _input.is('[data-money]') || _input.is('[money]')){
			var value = formData.get(key);
			if(!isEmpty(value)) {
				var setValue = value.replace(/,/g, "");
				formData.set(key,setValue);
			}
		}
	}
	return formData;
}
//Json 로 data 생성
function getSaveData(_form){
	let data = {};
	var formData = new FormData($(_form)[0]);
	for (let key of formData.keys()) {
		data[key] = formData.get(key);
	}

	setDeleteCommaFormData(_form, data);
	return data;
}
//Json 데이타에서 comma 제거
function setDeleteCommaFormData(_form, _data){
	$.each($(_form+"  input"), function (idx, elem) {
		//date , data-money
		if($(elem).is('[comma]') || $(elem).is('[data-money]') || $(elem).is('[money]')){
			var _name = $(elem).attr('name');
			if(!isEmpty(_name)) {
				var value = _data[_name];
				if(!isEmpty(value)) {
					_data[_name] = value.replace(/,/g, "");
				}
			}
		}
	});
}
//readonly, disabled
function setDisabledInput(_form , _status){
	$.each($(_form+" input"), function (idx, elem) {
		$(elem).prop("readonly", _status);
	});
	$.each($(_form+" textarea"), function (idx, elem) {
		$(elem).prop("readonly", _status);
		$(elem).addClass("form-control");
	});
	$.each($(_form+" select"), function (idx, elem) {
		$(elem).prop("readonly", _status);
		$(elem).prop("disabled", _status);
	});
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

//array codeKind filter
function getFilterDataKind(_list, _value) {
	const resultData = _list.filter(function(element){
		return _value == element.codeKind;
	});
	return resultData;
}

/** 그리드 콤보에서 사용
 * 예) SM0101M01.html 참조
 *  var comboGridOptions = [];  //그리드 콤보 사용시 필수
 *  function fetchCurrencyData() {
 *  setComboGridOptions("ORDRGDIV10,ORDRGDIV20");
 *  var options1 = getGridOptions(comboGridOptions, "ORDRGDIV10");
 *  var options2 = getGridOptions(comboGridOptions, "ORDRGDIV20");
*/
function setComboGridOptions(codeKind){
	var paramObj = {};
	paramObj.codeKindList = codeKind;
	postAjaxSync("/admin/cm/cm05/selectComboCodeList", paramObj, null, function(data) {
		comboGridOptions = data.resultList;
	});
}

//화면에 있는 콤보를 한번의 트랜잭션으로 조회한다. comboKindString
function setComboKindOptions(selectArr) {
	try {
		var codeKind = "";
		$.each(selectArr, function(idx, elem) {
			var kindVal = $(elem).data('kind');
			if(isEmpty(codeKind) || codeKind.indexOf(kindVal) == -1) {
				if(isEmpty(codeKind)) codeKind += kindVal;
				else codeKind += ","+kindVal;
			}
		});
		var paramObj = {};
		if(!isEmpty(codeKind)) paramObj.codeKindList = codeKind;
		if(!isEmpty(codeKind)){
			postAjaxSync("/admin/cm/cm05/selectComboCodeList", paramObj, null, function(data) {
				setSelectOptions(selectArr, data.resultList);
			});
		}
	} catch (e) {
		return setCommonSelect(selectArr);
	}
}

/**
 * 메인화면에서 전체 Code를 한번의 트랜잭션으로 조회하며 코드조회목록을 재사용한다.
 * @param $("#main_area select[data-kind]")
 * @returns
 * 기존 setCommonSelect: select의 숫자만큼 트랜잭션 개선 => setComboOptions 전체 select 를 한번의 트랜잭션으로 처리
 * 재사용 조건: 코드조회목록을 재사용 하려면 아래의 변수 2개가 선언되어있어야 한다.
 * var comboDataList = []; //메인화면에 선언되어 있을경우 메인화면에 조회된 콤보데이타를 재사용한다.
 * var comboKindString = "CO,ITEMLIST,ORDRGDIV20,ORDRGDIV10,GOODSDIV";
 * 처리방식
 * 1. comboDataList가 메인화면에 선언되어있다면 재사용하여 사용된다.
 * 2. comboDataList가 전역변수에 없다면 setComboKindOptions(전체 Code를 한번의 트랜잭션으로 조회) 연결 =>에러시 기존 setCommonSelect 연결
 */
function setComboOptions(selectArr) {
	var isComboData = false;
	try {
		if(comboDataList){
			isComboData = true;
		}
	} catch (e) {}
	var isComboKind = false;
	try {
		if(comboKindString){
			isComboKind = true;
		}
	} catch (e) {}

	if(!isComboData) return setComboKindOptions(selectArr);

	var codeKind = "";
	var codeKindChange = false;
	if(isComboKind){//comboKindString 있다면
		codeKind = comboKindString;
		$.each(selectArr, function(idx, elem) {
			var kindVal = $(elem).data('kind');
			if(isEmpty(codeKind) || codeKind.indexOf(kindVal) == -1) {
				if(isEmpty(codeKind)) codeKind += kindVal;
				else codeKind += ","+kindVal;
			}
		});
		if(isEmpty(codeKind)) {
			codeKindChange = true;
			comboKindString = codeKind;
		}
	} else {
		$.each(selectArr, function(idx, elem) {
			var kindVal = $(elem).data('kind');
			if(isEmpty(codeKind) || codeKind.indexOf(kindVal) == -1) {
				if(isEmpty(codeKind)) codeKind += kindVal;
				else codeKind += ","+kindVal;
			}
		});
	}
	var paramObj = {};
	if(!isEmpty(codeKind)) paramObj.codeKindList = codeKind;

	if(isEmpty(comboDataList) || comboDataList.length == 0 || codeKindChange){
		postAjaxSync("/admin/cm/cm05/selectComboCodeList", paramObj, null, function(data) {
			comboDataList = data.resultList;
			setSelectOptions(selectArr, data.resultList);
		});//postAjaxSync
	}
	else {
		setSelectOptions(selectArr, comboDataList);
	}
}

//화면의 select 변경
function setSelectOptions(_selectArr, _comboList) {
	$.each(_selectArr, function(idx, elem) {
		var optionHtml = '';
		var codeKind = $(elem).data('kind');
		var codeList = getSelectData(_comboList, codeKind);
		$.each(codeList, function (index, item){
			optionHtml += '<option value="'+item.codeId+'">';
			optionHtml += item.codeNm;
			optionHtml += '</option>';
		});
		$(elem).append(optionHtml);
	});
}

//데이타 copy and codeKind로 filter gridCombo return
function getGridOptions(_comboList, _kind){
	let copyList = JSON.parse(JSON.stringify(_comboList));
	let resultList = getFilterDataKind(copyList,_kind);
	var resultData = [];
	$.each(resultList, function(idx, elem) {
		var options = {value:elem.codeId,  text:elem.codeNm};
		resultData.push(options);
	});
	return resultData;
}

//데이타 copy and codeKind로 filter
function getSelectData(_comboList, _kind){
	let copyList = JSON.parse(JSON.stringify(_comboList));
	let resultList = getFilterDataKind(copyList,_kind);
	return resultList;
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
			let lcCellKey = (typeof cellKey === 'string') ? cellKey.toLowerCase() : cellKey.toString();
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

//********************************************************
// 월마감 체크
//    파라메터 :
//       chkValue : 체크일자 (년월일)
//       chkType  : 'D'- 삭제
//       coCd     : 회사코드
//********************************************************
function monthCloseChk(chkValue, chkType, coCd = jwt.coCd){

	if(chkValue === '' || chkValue== undefined) return false;
	if(!chkType){
		chkType = "";
	}

	var url = window.location.href;
	var menuUrl = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("."));
	var rtnVal    = "N";
	var rtnHolVal = "N";
	var rtnDate   = "";
	var sysDate   = "";
	var workDay   = "";
	var actionType = "";
	var resultList = [];
//	var coCd = jwt.coCd;

	chkValue = chkValue.replace(/\-/g, '');

	if(chkType === 'D' || chkType === 'C' || chkType === 'U' ){
		actionType = chkType;
	}else{
		actionType = modalStack.last().paramObj.actionType;
	}

	var paramObj = {};
	paramObj.coCd = coCd;
	paramObj.menuUrl = menuUrl;
	paramObj.actionType = actionType;
	paramObj.chkValue = chkValue;

	//console.log(paramObj);
	postAjaxSync("/admin/cm/cm05/selectMonthCloseChk", paramObj, null, function(data) {

		rtnVal = data.rtnVal;
		rtnHolVal= data.rtnHolVal;
		rtnDate= data.rtnDate;
		sysDate = data.sysDate;
		workDay= data.workDay;
		resultList = data.resultList;
	});//postAjaxSync

	if(typeof rtnVal == "undefined" || rtnVal == null) return false;
	if(rtnHolVal === 'Y'){	//발주, 요인별 발주 및 출장요청, WBS이슈관리
		rtnDate = calculateHoliday(sysDate, workDay);

		rtnDate = rtnDate.YMD;
		rtnDate = rtnDate.replace(/\-/g, '');
		rtnDate = parseFloat(rtnDate);
		chkValue = parseFloat(chkValue);

		if(rtnDate - chkValue > 0){
//			$("#actionBtn").hide();
			if(actionType === 'C') alert('마감된 일자입니다. ' + rtnDate + '일 이전은 데이터를 등록할 수가 없습니다.');
			if(actionType === 'D') alert('마감된 일자입니다. ' + rtnDate + '일 이전은 삭제할 수가 없습니다.');
//			if(actionType === 'U') setDisabledInputDate(true);
			if(actionType === 'U') alert('마감된 일자입니다. ' + rtnDate + '일 이전은 수정할 수가 없습니다.');
			return false;
		}else{
//			$("#actionBtn").show();
//			if(actionType === 'U') setDisabledInputDate(false);
			authChk();
			return true;
		}

	}else{
		if (rtnVal === 'Y') {  //마감완료
//			$("#actionOrdrsBtn").hide();
//			$("#actionBtn").hide();
			if(actionType === 'C') alert('마감된 일자입니다. ' + rtnDate + '일 이전은 데이터를 등록할 수가 없습니다.');
			if(actionType === 'D') alert('마감된 일자입니다. ' + rtnDate + '일 이전은 삭제할 수가 없습니다.');
//			if(actionType === 'U') setDisabledInputDate(true);
			if(actionType === 'U') alert('마감된 일자입니다. ' + rtnDate + '일 이전은 수정할 수가 없습니다.');
			return false;
		}else{
//			$("#actionOrdrsBtn").show();
//			$("#actionBtn").show();
//			if(actionType === 'U') setDisabledInputDate(false);
			authChk();
			return true;
		}
	}
	return true;
}

//checked date disabled
function setDisabledInputDate(_status){
	$.each($('.popup_area input[date]'), function(idx, elem) {
		$(elem).prop("disabled", _status);
	});
}

//매입마감일자 체크(마감일자 이전 여부 true return)
function inCloseChk(chkValue){

	if(chkValue === '') return;

	var url = window.location.href;
	var menuUrl = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("."));
	var pchsCloseYn   = "";
	var coCd = jwt.coCd;

	chkValue = chkValue.replace(/\-/g, '');

	var paramObj = {};
	paramObj.coCd = coCd;
	paramObj.menuUrl = menuUrl;
	paramObj.chkValue = chkValue.replace(/\-/g, '');

	//console.log(paramObj);
	postAjaxSync("/admin/cm/cm05/selectMonthCloseChk", paramObj, null, function(data) {

		pchsCloseYn= data.pchsCloseYn;
	});//postAjaxSync

	if(typeof pchsCloseYn == "undefined" || pchsCloseYn == null) return;

	if (pchsCloseYn === 'Y') {
		return true;
	}else{
		return false;
	}
	return false;
}

/**
 * Open Progress
 * @param progress bar 호출할 함수
 */
openProgress = function(boolean){
	if(boolean){
		let path = window.location.pathname;
		let message = "<div><img src='/static/img/progress.gif'/> <font size='5' color='blue'> 실행중 기다려 주십시요.</font></div>";
		if (path.includes('/static/mobile')) {
			message = "<div style='white-space: nowrap;'><img src='/static/img/progress.gif'/> <font size='2' color='blue'> 실행중 기다려 주십시요.</font></div>";
		}
		$.blockUI.defaults.overlayCSS.opacity = 0.1;
		$.blockUI({
			message: message,
			css: {
				backgroundColor: 'rgba(0,0,0,0.0)',
				color: '#000000',
				border: '0px solid #a00'
			}
		});
	}else{
		$.unblockUI();
	}
};



function kakaoSendReal(talkJson, talkParam, param) {
	let talkDeJson = JSON.parse(talkJson);
	let sendCnt = 0;
	if (jwt.serverType =='prod' ||jwt.kakaoSend) {	//KAKAO-SEND 설정은 application.yml에 설정됨, 운영모드와 kakaoSend :true일때만  카카오톡 발송처리함. 아닌경우 이력만 남김
		//알림톡
		$.ajax({
		    type: "POST",
		    url: "https://talkapi.lgcns.com/request/kakao.json",
	        headers: {
	            "Authorization": "Basic " + basicAuth,
	            "Content-Type": "application/x-www-form-urlencoded; charset=utf-8",
	        },
		    contentType: "x-www-form-urlencoded; charset=utf-8",
		    data: talkJson,
	      beforeSend: function (xhr) {
	          xhr.setRequestHeader("authToken", talkParam.authToken);
	          xhr.setRequestHeader("serverName", talkParam.serverName);
	          xhr.setRequestHeader("paymentType", talkParam.paymentType);
	      },
		    async: false,
		    success: function(data){
	//	    	console.log('status:' + data.status);
		    	let err = data.status;
		    	if( err.indexOf("ERR") > -1 || err.indexOf("KKO")> -1 ) {
		    		let errorMsg = err;
		    		let find = kakaoErr.find(e => e.codeId === err);
		    		if( typeof(find) != "undefined" ) {
						if( typeof(find.codeNm) != "undefined" ) {
				    		errorMsg = find.codeNm;
						}
		    		}
			    	insertKakaoMessage(err, talkDeJson, param);
		    		alert("오류코드: ["+param.nameTo+" Hp."+ talkDeJson.mobile + "] "+data.status+"\r\n\r\n" + errorMsg+"로 메세지 전송 실패하였습니다.");
		    	} else if( data.status == "OK" ) {
		    		//alert("알림톡 정상 발송되었습니다.");
		    		sendCnt++;
			    	insertKakaoMessage(err, talkDeJson, param);
		    	}
		    },
	      error: function (data) {
	      	insertKakaoMessage(data.status, talkDeJson, param);
	//      	console.log('---ajax error---');
	      }
		});
	//	console.log('---success---' + sendCnt);
		return sendCnt;
	} else { //운영모드에서만 카카오톡 발송처리함.  이력만 남김
		sendCnt++;
    	insertKakaoMessage('OK', talkDeJson, param);
		return sendCnt;
	}
}

//알림톡전송로크
function insertKakaoMessage(dStatus, talkDeJson, param){
	var formData = {
			 "mssageId": talkDeJson.messageId
			, "rcvId": param.rcvId
			, "rcvNm": param.rcvNm
			, "clntCd": param.clntCd
			, "tmplatDiv": param.tmplatDiv
			, "sendgStatus": dStatus
			, "title": talkDeJson.title
			, "mssage": talkDeJson.message
			, "mobile": talkDeJson.mobile
			, "nameTo": param.nameTo
			, "creatId": jwt.userId
			, "creatPgm": param.creatPgm
	};

	postAjax("/user/bm/bm18/insertKakaoMessage", formData, null, function(data) {
		//alert(data.resultMessage);// 결과 메시지를 alert으로 출력
			if (data.resultCode == 200) {							//  요청이 성공(200)한 경우, gridViewPop.setData(0)를 호출하여 그리드 뷰를 업데이트하고,
//				console.log('--알림톡 로그 정상 저장--');
			} else {
//				console.log('--알림톡 로그 정상 저장 오류--');
			}
	});
}


//프로젝트코드 멀티셀렉트용
//multi select 항목 자동 생성
function multiPrjctSelectHtml (_arrList, elem, _type=0) {
	let optionHtml = '';
	$.each(_arrList, function (index, item){
		if (item?.code == undefined) {
			optionHtml += '<option value="">없음(전체)</option>'
		} else {
			optionHtml += '<option value="'+item.code+'">';
			optionHtml += item.name;
			optionHtml += '</option>\n';
		};
	});
	$(elem).empty()
	$(elem).append(optionHtml);
	if (_type==0)	$(elem).multiselect('rebuild');
}



//그리드 높이 화면사이즈에 맞게 조정
function gridHeightResize(_grid, _minusHeight) {
 	let virtualHeight = $('body').height();
    let tagHeight = virtualHeight - _minusHeight;
    _grid.target.setHeight(tagHeight);
}


function gPasIntChk (value) {
	const cvtValue = parseInt(deleteCommaStr(value));
	return isNaN(cvtValue) ? 0 : cvtValue;
}

function gPasFloatChk (value) {
	const cvtValue = parseFloat(deleteCommaStr(value));
	return isNaN(cvtValue) ? 0 : cvtValue;
}


//이미지뷰 팝업
function imageViewPopup(_fileKey, _filename, fileList = []) {

	var paramObj = {
			"fileKey" 	: _fileKey,
			"fileName" 	: _filename,
			"fileList"  : fileList
		};
	openThirdModal("/static/html/cmn/modal/attachImageView.html", $('body').width()-40, $('body').height()-50, _filename, paramObj, function(data) {
	});
}




//백엔드에서 해당 구매내역에 대한 거래처의 문제현황을 검색해오기
//메세지 뿌리기
// 파라메터 구성 
//     1. 업무구분 코드(type) : 'M':구매  ,  'S':영업,   'D': 설계,   'P':생산 
//     2. 거래처코드 (vendCd) : 구매처, 협력사 또는 고객사 
//     3. 구매인경우 자재목록(list) : 구매 아이템 내역을 포함하는  list 구매는 가공업체인 경우만 문제 관리 메세지 표시
//
// 영업인 경우 업체정보만 있음 
// 설계인경우에도 업체 정보만 있음
function toastMsg(type, vendCd, list={}) { //theme : default, primary, success, info, warning, danger
	let oemChecking = false;
	let param = { vendCd 		: vendCd,
				selectType 	: 'OEM'
				};
	if (type == 'M') oemChecking = list.some(item => item.oemDivTxt === '가공품'); //구매팀 제외조건 적용.  가공품인경우만 문제내역 표시
	if (type == 'O') {	//외주관리에서 Call 하는것
		oemChecking = true;
		param.selectType = 'OEM';
	}
//	if (type == 'S') oemChecking = 영업팀 고객사 제외 조건 작성;
//	if (type == 'D') oemChecking = 연구소 협력사 제외 조건 작성;
//	if (type == 'P') oemChecking = 생산팀 협력사 제외 조건 작성;
	if (!oemChecking) return false;

	postAjax("/user/wb/wb24/selectVendProblemList", param, null, function(data) {
		let problemList = data.vendProblem;
		if (problemList.length > 0) {	//최근문제 5개에 대한 내용만 추출하기
			const problemMsg = problemList.slice(0, 5).map(item => '[' + item.creatDttm + ':' + item.subCdNm + '] '+  item.issSj).join('\n');
			const message = problemList[0].vendNm + ' 최근 문제 5건 내역\n' + problemMsg;
			try {
				toast.push({msg: message, theme: 'danger'}
				, function () {
//		            console.log(this);
		        });
			} catch (error) {
//				console.error('toast push 중 오류 발생:', error);
			}
		}
	});
}


function toastConfirm(type, vendCd, list={}) { //theme : default, primary, success, info, warning, danger
	let oemChecking = list.some(item => item.oemDivTxt === '가공품');
	if (!oemChecking) return false;

	const param = {
					vendCd 		: vendCd,
					selectType 	: 'OEM'
				};
	postAjax("/user/wb/wb24/selectVendProblemList", param, null, function(data) {
		let problemList = data.vendProblem;
		if (problemList.length > 0) {	//최근문제 5개에 대한 내용만 추출하기
			const problemMsg = problemList.slice(0, 5).map(item => '[' + item.creatDttm + ':' + item.subCdNm + '] '+  item.issSj).join('\n');
			const message = problemList[0].vendNm + ' 최근 문제 5건 내역\n' + problemMsg;
			try {
				toast.confirm({msg: message, theme: 'danger'}
				, function () {
//		            console.log(this);
		        });
			} catch (error) {
//				console.error('toast push 중 오류 발생:', error);
			}
		}
	});
}


function openapi(prompt) {
	if ($('#'+prompt).is(':disabled') || $('#'+prompt).is('[readonly]') || $('#actionBtn').is(':hidden')) {
		$('#'+prompt).val($('#'+prompt).val().replace("\n자료 저장할 권한이 없습니다.","") + "\n자료 저장할 권한이 없습니다.");
		return false;
	}
	//chatGPT API Call	-->"aiType":"GPT"
	//ollama  API Call  --> "aiType":"OLLAMA"
	const paramObj = {
			 "aiType"		: "OLLAMA"
			,"originMsg"	: $('#'+prompt).val()
			,"prompt"		: "\n -여기까지 오타 확인하고 문장을 자연스럽게 수정해줘 "
	}
	postAjax("/user/bot/chatRtv", paramObj, null, function(data){
		try {
			if (data.chatgpt != undefined) {
				$('#'+prompt).val(data.chatgpt);
				txtareaHeightResize($('#'+prompt));
			} else {
				alert("AI실행 오류 발생!! 전산실 연락 바랍니다.\n"+ data.msg);
			}
		} catch {
			alert("AI실행 오류 발생!! 전산실 연락 바랍니다.\n"+ error.message);
			return false; 
		} finally {
			// 종료 처리;
		}
	});
	
}

function disableFormAll(formId) {
	const $form = $('#' + formId);

	$form.find('input, select, textarea, button, a').each(function () {
		const $el = $(this);
		const tag = $el.prop('tagName');
		const type = $el.prop('type');

		if ($el.hasClass('close_btn') || $el.hasClass('report_btn')) {
			//닫기버튼은 무조건 활성화
			return false;
		} else {
			// 공통 스타일: 회색 배경
			$el.css({ 'background-color': '#e6e6e6'});
		}

		// input, textarea
		if (tag === 'INPUT' || tag === 'TEXTAREA') {
			$el.attr('readonly', true).removeAttr('onclick').off('click');
			// ax5picker 제거 (input_calendar)
			if ($el.hasClass('input_calendar')) {
				const ax5picker = $el.data('ax5picker');
				if (ax5picker) ax5picker.remove();
				$el.removeClass('input_calendar').datepicker('destroy');
			}
		}

		// select (기본)
		if (tag === 'SELECT') {
			$el.prop('disabled', true);
		}

		// checkbox, radio
		if (type === 'checkbox' || type === 'radio') {
			$el.on('click', function (e) { e.preventDefault(); });
		}

		// button
		if (tag === 'BUTTON') {
			$el.prop('disabled', true);
		}

		// ax5select (ax5select 대상은 별도 처리 필요)
		if ($el.hasClass('ax5select')) {
			const instance = $el.data('ax5select');
			if (instance) {
				instance.disable();
			}
		}

		// ax5picker / datepicker
		if ($el.hasClass('input-calendar') || $el.hasClass('datepicker')) {
			$el.attr('readonly', true).removeAttr('onclick').off('click');
		}

		// autocomplete (jQuery UI 등 커스텀일 경우 readonly 처리)
		if ($el.hasClass('ui-autocomplete-input')) {
			$el.attr('readonly', true);
		}

		// a 태그: 클릭 차단
		if (tag === 'A') {
			$el.removeAttr('onclick')
			.removeAttr('href')
			.off('click')
			.on('click', function (e) { e.preventDefault(); })  // 안전망
			.css({ 'pointer-events': 'none', 'opacity': 0.5 });
			
			}
	});
}