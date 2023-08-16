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
  	
  	var solarHolidays = [ "0101", "0301", "0505", "0606", "0717", "0815", "1003", "1009", "1225" ]; //양력휴일
  	var lunaHolidays = [ "0101", "0102", "0408", "0814", "0815", "0816" ]; //음력휴일, 설전날도 넣어야함.
  	
  	//대체공휴일 입력구간. 임시휴일이나 대체공휴일이 있을 경우 배열에 넣으면됨. yyyymmdd 입력
  	var alternativeHolidays = [ "20150929", "20160210", "20170130",
  	"20180926", "20180507", "20190506", "20200127", "20220912",
  	"20230124", "20240212", "20240506", "20251008", "20270209",
  	"20290924", "20290507", "20300205", "20300506", "20320921",
  	"20330202", "20340221", "20350918", "20360130" ];
  	
  	var setLunaToSolar = []; //당년도의 음력을 양력으로

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
  		for (i = 0; i < lunaHolidays.length; i++) {
  			var solar = Resut(year + "" + lunaHolidays[i]);
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