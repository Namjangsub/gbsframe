/********************************************
 * calculateHoliday(startDate, duration)
 *   startDate : 기준일자 '2023-09-01' 
 *   duration  : 워킹데이  10
 *   
 *   결과 
 *       YMD:"2023-09-14"
 *       year:2023
 *       month:9
 *       date:14
 *       day:"목"         --요일
 *       workingDays:10  --근무일수
 *       holiDays:4      --휴일일수
 *     
  *******************************************    
 * calculateHolidayCnt(startDate, endDate)
 *   startDate : 시작일자 '2023-09-26' 
 *   endDate  :  종료일자 '2023-10-04'
 *      
 *   결과 
 *       workingDays: 3  --근무일수
 *       holiDays: 6      --휴일일수    
 ********************************************
 */

function calculateHoliday(startDate, duration=1) {
	const endDate = "2050-12-31"				//변환가능 최대 날자
	const chkDuration = pasIntChk(duration);	//숫자만 추출
	const addSubVal = chkDuration > 0 ? 1 : -1;	//증가할지 감소할지
	const countDay = Math.abs(chkDuration);		//카운팅 날자수

	try {
		const start = new Date(startDate);
 		const end = new Date(endDate);
		const hol = new Date();
		 
		let workingDays = 0;
		let holiDaysCnt = 0;
		let lunaChkYear = 0;

		do {
			const day = start.getDay(); // 0:일요일, 6:토요일
			const chkYear = start.getFullYear();
			const chkMonth = start.getMonth() + 1;
			const chkDay = start.getDate();
	         
			if (lunaChkYear != chkYear) {  //년도가 바뀌면 음력설전날을 추가 하는 배열 생성
				yearHolidays = yearHolidayTable(chkYear);  //해당년도 휴일테이블 만들기
				lunaChkYear = chkYear;
			}
			/**************************
			 * 휴일여부체크 Start
			 **************************
			 */	     	
			let holDaySw = 0;  //1이면 휴일 아니면 평일로 산정하기 위한 스위치 역활
		    
			if (day == 0 || day == 6) { // 0:일요일, 6:토요일
				holDaySw = 1;
				} else {
				for (const holiday of yearHolidays) { //전체 휴일 체크
		        	
		            hol.setTime(Date.parse(holiday.slice(0, 4) + "-" + holiday.slice(4, 6) + "-" + holiday.slice(6, 8)));
		            if (start.toISOString().slice(0, 10) < hol.toISOString().slice(0, 10))  break;
		            if (start.toISOString().slice(0, 10) === hol.toISOString().slice(0, 10)) {
		            	holDaySw = 1;
		                break;
		            }
		        }
		     }
			/**************************
			 * 휴일여부체크 end
			 *************************
			 */
			
		     if (holDaySw) {
		    	 holiDaysCnt++;
		     } else {
		    	 workingDays++;
		     }
			
			start.setDate(start.getDate() + addSubVal);
		     
		} while (workingDays < countDay);

//		console.log('근무일:' + workingDays + '   휴일 :' + holiDaysCnt);
		
		start.setDate(start.getDate() + (addSubVal * -1));  //하루 차이 방지하기위함
		
		return new myDate(start, workingDays, holiDaysCnt);
		
	} catch (error) {
		console.error(error);
	}
}


function yearHolidayTable (_currYear) {
	/* 처리방법
	 * 
	 * 해당년도의 휴일 테이블을 만들어 사용 한다.
	 * 음력에 해당하는 양력일자를 구해서 휴일테이블에 추가한다.
	 * 휴일테이블을 해당년도것만 골라서 비교한다
	 * 
	 * Date함수의 요일값을 활용하여 휴일 체크한다.
	 * getDay() 함수의 값이  0:일요일, 6:토요일
	 */	
	const calendar = new KoreanLunarCalendar();	//음력변화을 위한 라이브러리

  	//1949~1959년까지 식목일 공휴일지정, 1961~2005년까지 공휴일지정
  	//2006년까지는 0717(체헌절) 공휴일, 0405(식목일)
  	//1949~1990년까지 1009(한글날) 1990년폐지됐다가 2014년부터 공휴일 시행 상기내용은 일자계산시 반영 되지 않음
	var solarHolidays = [ "0101", "0301", "0505", "0606", "0815", "1003", "1009", "1225" ]; //양력휴일
	var lunaHoliTempdays = [ "0101", "0102", "0408", "0814", "0815", "0816" ]; //음력휴일, 설전날도 넣어야함.
	var yearHolidays = [];  //전체 휴일 내역
	
	//대체공휴일 입력구간. 임시휴일이나 대체공휴일이 있을 경우 배열에 넣으면됨. yyyymmdd 입력 2014~2049년까지
	var eventHolidays = [   "20140910", "20150929", "20160210", "20170130", "20171006", 
							"20180507", "20180926", "20190506", "20200127", "20210816", 
							"20211004", "20211011", "20220912", "20221010", "20230124", 
							"20230529", "20231002", "20240212", "20240410", "20240506", 
							"20250303", "20250506", "20251008", "20260302", "20260525", 
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
	
	/**************************
	 * 휴일테이블만들기 Start
	 **************************
	 */
	calendar.setLunarDate(_currYear,1 ,1 , false) //음력설날 양력으로 변환 20230101 --> 0122
	//lunarCalendar: {year: 2023, month: 1, day: 1, intercalation: false}
	//solarCalendar: {year: 2023, month: 1, day: 22}
	
	//설전날자를 휴일에 추가
	let set설날 = new Date(calendar.solarCalendar.year + '-' + calendar.solarCalendar.month + '-' + calendar.solarCalendar.day);
	set설날.setDate(set설날.getDate() - 1); //음력 설전날 양력일자로 휴일
	eventHolidays.push(set설날.getFullYear() + (set설날.getMonth() + 1).toString().padStart(2, '0') + set설날.getDate().toString().padStart(2, '0'));
	
	//음력 휴일을 양력으로 변경
	for (const lunaday of lunaHoliTempdays) { //음력날자에 해당하는 양력휴일 추구
		calendar.setLunarDate(_currYear, parseInt(lunaday.slice(0, 2)), parseInt(lunaday.slice(2, 4)), false);
		//solarCalendar: {year: 2023, month: 1, day: 22}

		eventHolidays.push(calendar.solarCalendar.year + calendar.solarCalendar.month.toString().padStart(2, '0') + calendar.solarCalendar.day.toString().padStart(2, '0'));
 	}
	
	//휴테이블에 eventHolidays[] + solarHolidays[]의 앞에 해당년도 추가하고 머지해서 오름차순으로 정렬
	var combinedHolidays = eventHolidays.concat(solarHolidays.map(date => _currYear + date)).sort();
		
	yearHolidays = combinedHolidays.filter(function(date) {
		return date.startsWith(_currYear);
	});
	/**************************
	 * 휴일테이블만들기 End
	 **************************
	 */
	return yearHolidays;
}


function calculateHolidayCnt(startDate, endDate) {
//	const startDate = "2024-12-29"
//	const endDate = "2050-12-31"				//변환가능 최대 날자
	
	try {
		const start = new Date(startDate);
 		const end = new Date(endDate);
		const hol = new Date();
		let yearHolidays = [];
		
		let workingDays = 0;
		let holiDaysCnt = 0;
		let lunaChkYear = 0;

		while (start <= end) {
			const day = start.getDay(); // 0:일요일, 6:토요일
			const chkYear = start.getFullYear();
			const chkMonth = start.getMonth() + 1;
			const chkDay = start.getDate();
	         
			if (lunaChkYear != chkYear) {    //년도가 바뀌면 음력설전날을 추가 하는 배열 생성
				yearHolidays = yearHolidayTable(chkYear);  //해당년도 휴일테이블 만들기
				lunaChkYear = chkYear;
			}
			/**************************
			 * 휴일여부체크 Start
			 **************************
			 */	     	
			let holDaySw = 0;  //1이면 휴일 아니면 평일로 산정하기 위한 스위치 역활
		    
			if (day == 0 || day == 6) { // 0:일요일, 6:토요일
				holDaySw = 1;
				} else {
				for (const holiday of yearHolidays) { //전체 휴일 체크
		        	
		            hol.setTime(Date.parse(holiday.slice(0, 4) + "-" + holiday.slice(4, 6) + "-" + holiday.slice(6, 8)));
		            if (start.toISOString().slice(0, 10) < hol.toISOString().slice(0, 10))  break;
		            if (start.toISOString().slice(0, 10) === hol.toISOString().slice(0, 10)) {
		            	holDaySw = 1;
		                break;
		            }
		        }
		     }
			/**************************
			 * 휴일여부체크 end
			 *************************
			 */
			
		     if (holDaySw) {
		    	 holiDaysCnt++;
		     } else {
		    	 workingDays++;
		     }
			
			start.setDate(start.getDate() + 1);
		     
		} 

//		console.log('근무일:' + workingDays + '   휴일 :' + holiDaysCnt);
		
		return new myWorkDay(workingDays, holiDaysCnt);
		
	} catch (error) {
		console.error(error);
	}
}

function pasIntChk (value) {
	let num = value;
	if (typeof value === 'string'){ 
		num = value.replace(/[^-0-9,.]/g, '');
		num.replace(/,/g, '');
	}
	return isNaN(num) ? 1 : num;
}

function myDate(start, workingDays, holiDaysCnt)
{
  	const week = [ '일', '월', '화', '수', '목', '금', '토'];
	this.YMD = moment(start).format('YYYY-MM-DD');
    this.year =  start.getFullYear();
    this.month = start.getMonth() + 1;
    this.date = start.getDate();
    this.day = week[start.getDay()];
    this.workingDays = workingDays;
    this.holiDays = holiDaysCnt;
}


function myWorkDay(workingDays, holiDaysCnt)
{
    this.workingDays = workingDays;
    this.holiDays = holiDaysCnt;
}
