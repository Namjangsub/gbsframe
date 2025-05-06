  var today = new Date();

  function setToday(year, month){
  	today = new Date(year, month-1, 1);
  }

  //today 에 Date객체를 넣어줌 //ex)5일
	function prevCalendar() {
		gridView.initView();
		today = new Date(today.getFullYear(), today.getMonth() - 1, today.getDate()); // month 를 조정해 해당 월을 변경함
		buildCalendar(); // 달력을 새로 그려준다.
		gridView.setData();
	}

	function nextCalendar() {
		gridView.initView();
		today = new Date(today.getFullYear(), today.getMonth() + 1, today.getDate());
		buildCalendar();
		gridView.setData();
	}

  function buildCalendar() {// 현재 달fur
  	var week = [ '일', '월', '화', '수', '목', '금', '토', '일'];
  	var nMonth = new Date(today.getFullYear(), today.getMonth(), 1); // 이번 달의 첫째 날
  	var lastDate = new Date(today.getFullYear(), today.getMonth() + 1, 0); // 이번 달의 마지막 날
  	var tblCalendar = $("#calendar"); // 테이블 달력을 만들 테이블
  	var tblCalendarYM = $("#calendarYM"); // yyyy년 m월 출력할 곳
  	tblCalendarYM.html(today.getFullYear() + "년 " + (today.getMonth() + 1) + "월");// yyyy년 m월 출력

  	var dayWeek = week[nMonth.getDay()]; //이번달 첫째날의 요일
  	var year = today.getFullYear();
  	var lastYear = 0;

  	// year와 lastyear가 동일할경우 setLunaToSolar를 계산하지 않음;
  	var isSame = false;
  	if (year == lastYear)
  		isSame = true;

  	var solarHolidays = [ "0101", "0301", "0505", "0606", "0815", "1003", "1009", "1225" ]; //양력휴일
  	var lunarHoliTempdays = [ "0101", "0102", "0408", "0814", "0815", "0816" ]; //음력휴일, 설전날도 넣어야함.

  	//대체공휴일 입력구간. 임시휴일이나 대체공휴일이 있을 경우 배열에 넣으면됨. yyyymmdd 입력
	//20241001 임시 공휴일-->당사는 20241004일로 변경 근무 진행함
  	var alternativeHolidays = [   "20140910", "20150929", "20160210", "20170130", "20171006",
		"20180507", "20180926", "20190506", "20200127", "20210816",
		"20211004", "20211011", "20220912", "20221010", "20230124",
		"20230529", "20231002", "20240212", "20240410", "20240501",
		"20240506", "20240729", "20240730", "20240731", "20241004",
		"20250127", "20250303",
		"20250501", "20250506", "20251008", "20260302", "20260525",
		"20260817", "20261005", "20270209", "20270816", "20271004",
		"20271011", "20271227", "20281005", "20290507", "20290521",
		"20290924", "20300205", "20300506", "20310303", "20320517",
		"20320816", "20320921", "20321004", "20321011", "20321227",
		"20330202", "20331010", "20331226", "20340221", "20350507",
		"20350918", "20360130", "20360303", "20360506", "20361006",
		"20361007", "20370217", "20370302", "20370817", "20371005",
		"20380816", "20380915", "20381004", "20381011", "20381227",
		"20390126", "20390502", "20391004", "20391005", "20391010",
		"20391226", "20400214", "20400507", "20410506", "20420303",
		"20420930", "20430302", "20430518", "20430817", "20431005",
		"20440201", "20440506", "20441010", "20441226", "20450927",
		"20460507", "20460514", "20460917", "20470128", "20470506",
		"20471007", "20480302", "20480817", "20481005", "20490510",
		"20490816", "20490913", "20491004", "20491011", "20491227"];

  	var setLunaToSolar = []; //당년도의 음력을 양력으로

  //휴일관리 테이블에서 불러오기 --> 휴일관리는 TB_CM12M01에서 등록 관리로 변경함.
//	var paramObj = {
//		"calDivCd": "A",	//calDivCd : A(All), S(solarHolidays), L(lunaHoliTempdays), E(eventHolidays)
//	};
//	postAjaxSync("/admin/cm/cm12/selectSolarLunarEventHolidaysList", paramObj, null, function(data){
//		let temp = data.resultSolar;	//양력휴일
//		solarHolidays = temp.map(item => item.calYmd.trim());
//		
//		temp = data.resultLumar;	//음력휴일, 설전날은 계산헤서 넣음.
//		lunarHoliTempdays = temp.map(item => item.calYmd.trim());
//		
//		temp = data.resultEnent;	//대체공휴일 입력구간. 임시휴일
//		alternativeHolidays = temp.map(item => item.calYmd.trim());
//	});
  	
  	
  	//테이블에 기존값이 존재할 경우 해당 값을 다 날림.
//	if ($('#calendar').length > 0) {
//	    $('#calendar thead').empty(); // Delete all content inside <thead>
//	    $('#calendar tbody').empty(); // Delete all content inside <tbody>
//	}

//    if (tblCalendar.find("thead").length === 0) {
//        tblCalendar.append("<thead></thead>");
//    }

//    var header = tblCalendar.find("thead")[0];
//  	var hrow = null;
//  	hrow = header.insertRow();
//  	var headerCell = document.createElement("th");
//  	headerCell.innerHTML = "소속(팀)";
//  	headerCell.rowSpan = 2;
//  	hrow.appendChild(headerCell);
//
//  	headerCell = document.createElement("th");
//  	headerCell.innerHTML = "성명";
//  	headerCell.rowSpan = 2;
//  	hrow.appendChild(headerCell);

  	var workDay = 0;
  	var cnt = nMonth.getDay();

  	//setLunaToSolar
  	///////////////////////////////////////////////////
  	if (!isSame) { //당년도의 음력휴일 양력으로 변환
  		for (i = 0; i < lunarHoliTempdays.length; i++) {
  			var solar = Resut(year + "" + lunarHoliTempdays[i]);
  			if (i == 0) {
  				//var cDate = new Date();
  				var cMonth = solar.substring(0, 2);
  				var cDay = solar.substring(2, 4);
  				var cDate = new Date(parseInt(cMonth) + "/"
  				+ parseInt(cDay) + "/" + year);
  				cDate.setDate(cDate.getDate() - 1); // 하루전

  				/* cDate.setMonth(parseInt(cMonth-1)); // 월 설정
  				cDate.setDate(parseInt(cDay)); //일 설정
  				//cDate.setDate(-1); //하루전날

  				//var sdate = cDate.setDate(cDate.getDate()-1); */
  				var sm = (cDate.getMonth() + 1);
  				if (sm < 10)
  					sm = "0" + sm;
  				var sd = (cDate.getDate());
  				if (sd < 10)
  					sd = "0" + sd;
  				sDate = sm + "" + sd;
  				setLunaToSolar.push(sDate);
  			}
  			setLunaToSolar.push(solar);
  		}
  		lastYear = today.getFullYear();
  	}

  	var year1 = today.getFullYear();
  	var month1 = (today.getMonth() + 1);
  	//console.log(dayWeek);
  	if ((today.getMonth() + 1) < 10) {
  		month1 = "0" + (today.getMonth() + 1);
  	}

  	var list = [];

  	for (i = 0; i < solarHolidays.length; i++) { // 양력휴일 넣음
  		if (solarHolidays[i].substring(0, 2) == month1) {
  			list.push(solarHolidays[i].substring(2, 4)); //휴일이 있을경우 list에 넣는다.
  		}
  	}

  	for (i = 0; i < setLunaToSolar.length; i++) { // 음력휴일 넣음
  		if (setLunaToSolar[i].substring(0, 2) == month1) {
  			list.push(setLunaToSolar[i].substring(2, 4)); //휴일이 있을경우 list에 넣는다.
  		}
  	}

  	for (i = 0; i < alternativeHolidays.length; i++) { //해당년도의 임시, 대체공휴일 추가
  		if (alternativeHolidays[i].substring(0, 4) == year) {//공휴년도가 해당년도와 일치할경우
  			if (alternativeHolidays[i].substring(4, 6) == month1) { //공휴년도가 해당월과 일치할경우
  				list.push(alternativeHolidays[i].substring(6, 8)); //일치하는 휴일이 있을경우 list에 넣는다.
  			}
  		}
  	}
    var tempWeek = "";
  	for (i = 0; i < lastDate.getDate(); i++) {
  		var pass = false;
//  		headerCell = document.createElement("th");
  		if (list.length > 0) { //휴일이 있을경우
  			for (j = 0; j < list.length; j++) {
  				if (list[j] == (i + 1)) {
//  					headerCell.className = "red"; //공휴일은 빨간날로
  					pass = true;
  					break; //공휴일을 그렸으면 토,일 검사를 하지 않음.
  				}
  			}
  		}
  		if (!pass) {
  			if (cnt % 7 == 6) {
//  				headerCell.className = "blue"; // 토요일은 파란색으로
  			}
  			if (cnt % 7 == 0) {
//  				headerCell.className = "red"; // 일요일은 빨간색으로
  			}
  		}
		tempWeek = week[(nMonth.getDay() + i) % 7];
//  		headerCell.innerHTML = tempWeek;
//  		hrow.appendChild(headerCell);

  		gridView.target.addColumn({label: tempWeek,
  			columns:[{key: "d"+(i+1), label: i+1, width: 40, align: "center"}]});

  		cnt++;
  	}

	gridView.target.addColumn({key: "tot", label: "합계", width: 80, align: "center"});
	gridView.target.addColumn({key: "rmk", label: "비고", width: 120, align: "center"});


//  	headerCell = document.createElement("th");
//  	headerCell.innerHTML = "비고";
//  	headerCell.className = "cBlue";
//  	headerCell.rowSpan = 2;
//  	hrow.appendChild(headerCell);
//
//  	headerCell = document.createElement("th");
//  	headerCell.innerHTML = "기타";
//  	headerCell.className = "cBlue";
//  	headerCell.rowSpan = 2;
//  	hrow.appendChild(headerCell);
//
//  	hrow = calendar.insertRow();

//  	cnt = nMonth.getDay();
//  	for (i = 1; i <= lastDate.getDate(); i++) { //1일부터 그달의 마지막일까지 그려준다.
//  		var pass = false;
//  		var isHoliday = false;
//  		headerCell = document.createElement("th");
//
//  		if (list.length > 0) { //휴일이 있을경우
//  			for (j = 0; j < list.length; j++) {
//  				if (list[j] == i) {
//  					headerCell.className = "red";
//  					pass = true;
//  					isHoliday = true;
//  					break;
//  				}
//  			}
//  		}
//  		if (!pass) {
//  			if (cnt % 7 == 6) {
//  				headerCell.className = "blue";
//  				isHoliday = true;
//  			}
//  			if (cnt % 7 == 0) {
//  				headerCell.className = "red";
//  				isHoliday = true;
//  			}
//  		}
//  		if (!isHoliday) {
//  			workDay++;
//  		}
//  		headerCell.innerHTML = i;
//  		hrow.appendChild(headerCell);
//  		cnt++;
//  	}
  }