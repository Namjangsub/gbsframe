

/* Create Tables */

CREATE TABLE AppendFileInformation
(
	FILE_KEY numeric NOT NULL,
	FILE_TRGT_TYP varchar(20),
	FILE_TRGT_KEY varchar(20),
	FILE_PATH varchar(255),
	FILE_NAME varchar(255),
	FILE_SIZE numeric,
	FILE_TYPE varchar(255),
	USE_YN varchar(1),
	CREAT_ID varchar(20) NOT NULL,
	CREAT_PGM varchar(30) NOT NULL,
	CREAT_DTTM timestamp DEFAULT SYSDATE NOT NULL,
	UDT_ID varchar(20),
	UDT_PGM varchar(30),
	UDT_DTTM timestamp,
	PRIMARY KEY (FILE_KEY)
);


CREATE TABLE BOM
(
	prdt_cd varchar(20) NOT NULL UNIQUE,
	matl_cd varchar(20) NOT NULL,
	matr_nm varchar(50),
	matr_drw_no varchar(20),
	matr_top_cd varchar(20),
	rpdt_matr_cd varchar(20),
	-- M	소재비
	-- A	구매품
	-- AT	후처리
	-- W	가공비
	-- ELEC	전기자재
	-- TURN	턴키
	-- ETC 재고품
	matr_mat_grp varchar(20),
	matr_makr_nm varchar(50),
	matr_mat_nm varchar(50),
	matr_nmr_nm varchar(50),
	matr_siz_nm varchar(30),
	min_ordrg_qty decimal(13),
	matr_unit_cd varchar(20),
	matr_rqm_qty decimal(13),
	matr_wt_qty numeric(15,3),
	PCHS_OUTSD_CD varchar(20),
	2D_DSGN_DAY decimal(5),
	3d_dsgn_day decimal(5),
	2d_dsgn_rqm_day decimal(5),
	3d_dsgn_rqm_day decimal(5),
	last_PRDCTN_rqm_day decimal(5),
	avrg_PRDCTN_rqm_day decimal(5),
	pchs_clnt_cd1 varchar(13),
	pchs_clnt_pct1 decimal(3),
	pchs_clnt_cd2 varchar(13),
	pchs_clnt_pct2 decimal(3),
	pchs_clnt_cd3 varchar(13),
	pchs_clnt_pct3 decimal(3),
	DLVR_RQM_day decimal(5),
	last_pchs_upr numeric(15,2),
	avrg_pchs_upr decimal(15,2),
	last_trst_dt datetime,
	-- 설계대상항목구분
	DSGN_trgt_YN varchar(1),
	22222 varchar(20),
	22222 varchar(20),
	PRIMARY KEY (prdt_cd, matl_cd)
);


CREATE TABLE BOM1
(
	ODR_SEQ numeric(10) NOT NULL UNIQUE,
	222 varchar(20) NOT NULL UNIQUE,
	222 varchar(20) NOT NULL,
	matr_nm varchar(50),
	22222 varchar(20),
	matr_top_cd varchar(20),
	rpdt_matr_cd varchar(20),
	-- M	소재비
	-- A	구매품
	-- AT	후처리
	-- W	가공비
	-- ELEC	전기자재
	-- TURN	턴키
	-- ETC 재고품
	matr_mat_grp varchar(20),
	matr_makr_nm varchar(50),
	matr_mat_nm varchar(50),
	matr_nmr_nm varchar(50),
	matr_siz_nm varchar(30),
	min_ordrg_qty decimal(13),
	matr_unit_cd varchar(20),
	matr_rqm_qty decimal(13),
	matr_wt_qty numeric(15,3),
	PCHS_OUTSD_CD varchar(20),
	2D_DSGN_DAY decimal(5),
	3d_dsgn_day decimal(5),
	2d_dsgn_rqm_day decimal(5),
	3d_dsgn_rqm_day decimal(5),
	last_PRDCTN_rqm_day decimal(5),
	avrg_PRDCTN_rqm_day decimal(5),
	pchs_clnt_cd1 varchar(13),
	pchs_clnt_pct1 decimal(3),
	pchs_clnt_cd2 varchar(13),
	pchs_clnt_pct2 decimal(3),
	pchs_clnt_cd3 varchar(13),
	pchs_clnt_pct3 decimal(3),
	DLVR_RQM_day decimal(5),
	last_pchs_upr numeric(15,2),
	avrg_pchs_upr decimal(15,2),
	last_trst_dt datetime,
	2222 varchar(1),
	22222 varchar(1),
	2222 varchar(20),
	22222 varchar(20),
	PRIMARY KEY (ODR_SEQ, 222, 222)
);


CREATE TABLE MaterialCode
(
	222 varchar(20) NOT NULL UNIQUE,
	222 varchar(50),
	rpdt_matr_cd varchar(20),
	22222 varchar(20),
	22222 varchar(20),
	-- M	소재비
	-- A	구매품
	-- AT	후처리
	-- W	가공비
	-- ELEC	전기자재
	-- TURN	턴키
	-- ETC 재고품
	matr_mat_grp varchar(20),
	matr_nm varchar(50),
	matr_makr_nm varchar(50),
	matr_mat_nm varchar(50),
	matr_nmr_nm varchar(50),
	matr_siz_nm varchar(30),
	matr_unit_cd varchar(20),
	matr_wt_qty numeric(15,3),
	pchs_clnt_cd1 varchar(13),
	pchs_clnt_pct1 decimal(3),
	pchs_clnt_cd2 varchar(13),
	pchs_clnt_pct2 decimal(3),
	pchs_clnt_cd3 varchar(13),
	pchs_clnt_pct3 decimal(3),
	DLVR_RQM_day decimal(5),
	22222 decimal(13),
	min_ordrg_qty decimal(13),
	last_pchs_upr numeric(15,2),
	avrg_pchs_upr decimal(15,2),
	22222 varchar(100),
	last_trst_dt datetime,
	2222 varchar(1),
	2222 varchar(1),
	2222 varchar(20),
	2222 varchar(20),
	22222 varchar(20),
	PRIMARY KEY (222)
);


CREATE TABLE TB_BM01M01
(
	-- 제품코드임
	PRDT_CD varchar(20) NOT NULL UNIQUE,
	PRDT_NM varchar(200),
	-- 제고유형구분(철근, 형강, 건자재)
	PRDT_DIV varchar(20),
	PRDT_SPEC varchar(50),
	-- 철근지름:13~~29
	-- 기타
	PRDT_SIZE varchar(30),
	PRDT_CLNT decimal(10),
	-- 감싸기, 초음파융착기,열풍융착기등
	PRDT_GRP varchar(20),
	PRDT_ITEM varchar(20),
	-- 처리단위(EA,KG,SH,매,본,장)
	PRDT_UNIT varchar(20),
	PRDT_WT numeric(12,3),
	PRDT_UPR numeric(15,2),
	-- YYYYMMDD
	PRDT_DT varchar(8),
	USE_YN varchar(1) DEFAULT 'Y',
	-- 여분필드(고정)
	ETC_FIELD1 char(10),
	-- 숫자여분필드
	ETC_FIELD2 numeric,
	-- 여분필드(가변)
	ETC_FIELD3 varchar(500),
	CREAT_ID varchar(20) NOT NULL,
	CREAT_PGM varchar(30) NOT NULL,
	CREAT_DTTM timestamp DEFAULT SYSDATE NOT NULL,
	UDT_ID varchar(20),
	UDT_PGM varchar(30),
	UDT_DTTM timestamp,
	PRIMARY KEY (PRDT_CD)
);


CREATE TABLE TB_BM06D01
(
	PRJCT_SEQ numeric(10) NOT NULL,
	PRJCT_ITEM varchar(20) NOT NULL,
	PRJCT_ITEM_RMK varchar(2000),
	PRJCT_ITEM_SORT numeric,
	PRIMARY KEY (PRJCT_SEQ, PRJCT_ITEM)
);


CREATE TABLE TB_BM06M01
(
	PRJCT_SEQ numeric(10) NOT NULL UNIQUE,
	CO_CD varchar(10),
	YYYYMM numeric(6),
	INPEXP_CD varchar(20),
	-- 신규,개조
	NEW_PRDT_CD varchar(20),
	-- PRJCTCD
	PRJCT_CD varchar(20) NOT NULL UNIQUE,
	PRJCT_NM varchar(50),
	EQP_NM varchar(200),
	CAR_NM varchar(50),
	-- MAKR
	MAKR_CD varchar(20),
	EQP_QTY numeric(15,3),
	CLNT_CD numeric(10),
	CLNT_MNG_NM varchar(50),
	-- 입찰/수의
	ODR_CD varchar(20),
	-- CURR
	SET_CUR varchar(20),
	ODR_RMK varchar(2000),
	EPCT_AMT numeric(15,2),
	ORDRS_PLAN_AMT numeric(15,2),
	WINBD_CD varchar(20),
	WINBD_RMK varchar(200),
	ORDRS_PLAN_DT date,
	ORDRS_AMT numeric(15,2),
	ORDRS_DT date,
	DUDT_PLAN_DT date,
	SALES_MNG varchar(20),
	PRJCT_RMK varchar(2000),
	-- 여분필드(고정)
	ETC_FIELD1 char(10),
	-- 숫자여분필드
	ETC_FIELD2 numeric,
	-- 여분필드(가변)
	ETC_FIELD3 varchar(500),
	CREAT_ID varchar(20) NOT NULL,
	CREAT_PGM varchar(30) NOT NULL,
	CREAT_DTTM timestamp DEFAULT SYSDATE NOT NULL,
	UDT_ID varchar(20),
	UDT_PGM varchar(30),
	UDT_DTTM timestamp,
	1ST_TST_DT date,
	2ND_TST_DT date,
	MSPRDT_DT date,
	YEAR_PRDCTN_QTY decimal(10),
	PRIMARY KEY (PRJCT_SEQ)
);


CREATE TABLE TB_CR01D01
(
	EST_SEQ numeric(10) NOT NULL UNIQUE,
	EST_PRDT_SEQ numeric(3) NOT NULL,
	-- 1:MACHINE PART, 2:SUPERVISOR PART
	EST_PART_CD varchar(20),
	EST_PRDT_CD varchar(20),
	EST_PRDT_NM varchar(100),
	EST_PRDT_SPEC varchar(50),
	EST_PRDT_UNIT varchar(20),
	EST_PRDT_QTY numeric(15,3),
	EST_PRDT_UPR numeric(15,2),
	EST_PRDT_AMT numeric(15,2),
	EST_PRDT_RMK varchar(500),
	EST_PRDT_WT numeric(15,3),
	EST_PRDT_NNR varchar(20),
	EST_NEGO_AMT numeric(15,2),
	-- 여분필드(고정)
	ETC_FIELD1 char(10),
	-- 숫자여분필드
	ETC_FIELD2 numeric,
	-- 여분필드(가변)
	ETC_FIELD3 varchar(500),
	CREAT_ID varchar(20) NOT NULL,
	CREAT_PGM varchar(30) NOT NULL,
	CREAT_DTTM timestamp DEFAULT SYSDATE NOT NULL,
	UDT_ID varchar(20),
	UDT_PGM varchar(30),
	UDT_DTTM timestamp,
	PRIMARY KEY (EST_SEQ, EST_PRDT_SEQ)
);


CREATE TABLE TB_CR01M01
(
	EST_SEQ numeric(10) NOT NULL UNIQUE,
	CO_CD varchar(10) NOT NULL,
	-- 견적서 번호(회사코드(3) + YYMMDD+SEQ(3)))
	EST_NO varchar(50),
	PRJCT_SEQ decimal(10) NOT NULL UNIQUE,
	EST_CNTS varchar(200),
	EST_RPRC varchar(200),
	SALES_EMP_ID varchar(20),
	EST_REQ_DT date,
	REQ_CLNT_CD numeric(10),
	REQ_TEL_NO varchar(10),
	REQ_FAX_NO varchar(20),
	REQ_MNG_NM varchar(50),
	REQ_DEPT_NM varchar(50),
	REQ_MNG_EMAIL varchar(50),
	SET_CUR varchar(20),
	EST_AMT numeric(15,2),
	EST_RMK varchar(2000),
	EST_RMK2 varchar(2000),
	EST_SND_DT timestamp,
	EST_SND_ID varchar(20),
	EST_SND_NM varchar(50),
	WINBD_YN varchar(1),
	WINBD_DT date,
	FAIL_RMK varchar(2000),
	EST_NEGO_AMT numeric(15,2),
	APRV_MNG_ID varchar(20),
	APRV_MNG_DTTM timestamp,
	APRV_1_ID varchar(20),
	APRV_1_DTTM timestamp,
	APRV_2_ID varchar(20),
	APRV_2_DTTM timestamp,
	APRV_3_ID varchar(20),
	APRV_3_DTTM timestamp,
	-- 여분필드(고정)
	ETC_FIELD1 char(10),
	-- 숫자여분필드
	ETC_FIELD2 numeric,
	-- 여분필드(가변)
	ETC_FIELD3 varchar(500),
	CREAT_ID varchar(20) NOT NULL,
	CREAT_PGM varchar(30) NOT NULL,
	CREAT_DTTM timestamp DEFAULT SYSDATE NOT NULL,
	UDT_ID varchar(20),
	UDT_PGM varchar(30),
	UDT_DTTM timestamp,
	PRIMARY KEY (EST_SEQ)
);


CREATE TABLE TB_CR02D01
(
	ODR_SEQ numeric(10) NOT NULL UNIQUE,
	ODR_DTL_SEQ numeric(10) NOT NULL,
	PRDT_CD varchar(20) NOT NULL,
	SALES_CODE varchar(50),
	ORD_DTL_RMK varchar(2000),
	ODR_DTL_TYP varchar(20),
	ODR_DTL_QTY decimal(15,3),
	ODR_DTL_UPR numeric(15,2),
	ODR_DTL_AMT numeric(15,2),
	ODR_DTL_VAT numeric(15,2),
	ODR_DTL_DC numeric(15,2),
	PRIMARY KEY (ODR_SEQ, ODR_DTL_SEQ)
);


CREATE TABLE TB_CR02M01
(
	ODR_SEQ numeric(10) NOT NULL UNIQUE,
	CO_CD varchar(10),
	EST_SEQ numeric(10),
	PRJCT_SEQ numeric(10),
	SALES_EMP_ID varchar(20),
	CREAT_DT date,
	RECPT_DTTM date,
	-- ODRTYP-신규,개조,기타
	ODR_TYP_CD varchar(20),
	LCNT_CD numeric(10),
	-- 공통코드(PMNTMTD)-현금,어음
	CSTM_PMNT_CD varchar(20),
	CSTM_NM varchar(50),
	CSTM_TEL varchar(20),
	CSTM_EMAIL varchar(50),
	CSTM_RMK varchar(2000),
	-- CURR
	ODR_CURR_CD varchar(20),
	PRDT_CD varchar(20) NOT NULL,
	ODR_HD_TYP varchar(20),
	ODR_HD_QTY numeric(15,3),
	ODR_HD_UPR decimal(15,2),
	ODR_HD_AMT numeric(15,2),
	ODR_HD_VAT numeric(15,2),
	ODR_HD_DC numeric(15,2),
	CTRT_DT date,
	DUDT_DT date,
	DCSN_DT date,
	PFU_DT date,
	DSGN_END_DT date,
	PCHS_END_DT date,
	ASBL_END_DT date,
	ELCTY_END_DT date,
	TEST_END_DT date,
	CNDT_END_DT date,
	ISPCTN_END_DT date,
	PACK_END_DT date,
	OUT_END_DT date,
	SHIPNG_END_DT date,
	ARV_END_DT date,
	ISTLTN_STRT_DT date,
	ISTLTN_END_DT date,
	ASBL_SMPL_DT date,
	TO_PRDT_DT date,
	PRDCTN_MNG_NM varchar(50),
	SALES_MNG_NM varchar(50),
	MSPRDT_DT date,
	PACK_MTD_CD varchar(20),
	TRANS_INFO_CD varchar(20),
	DELY_CNDT_CD varchar(20),
	DES_NM varchar(50),
	EQP_CRTFC_CD varchar(20),
	VAT_CD varchar(20),
	APRV_MNG_ID varchar(20),
	APRV_MNG_DTTM timestamp,
	APRV_1_ID varchar(20),
	APRV_1_DTTM timestamp,
	APRV_2_ID varchar(20),
	APRV_2_DTTM timestamp,
	APRV_3_ID varchar(20),
	APRV_3_DTTM timestamp,
	ODR_CTRT_YN varchar(20),
	ODR_CTRT_DT date,
	ODR_CTRT_AMT numeric(15,2),
	ODR_PPAY_YN varchar(20),
	ODR_PPAY_DT ,
	ODR_PPAY_AMT decimal(15,2),
	ODR_WARN_YN varchar(20),
	ODR_WARN_DT date,
	ODR_WARN_AMT numeric(15,2),
	PRIMARY KEY (ODR_SEQ)
);


CREATE TABLE TB_CR03M01
(
	ODR_SEQ numeric(10) NOT NULL,
	ODR_DTL_SEQ numeric(10) NOT NULL,
	CO_CD varchar(10),
	CTRT_DT date,
	DUDT_DT date,
	DCSN_DT date,
	PFU_DT date,
	DSGN_END_DT date,
	PCHS_END_DT date,
	ASBL_END_DT date,
	ELCTY_END_DT date,
	TEST_END_DT date,
	CNDT_END_DT date,
	ISPCTN_END_DT date,
	PACK_END_DT date,
	OUT_END_DT date,
	SHIPNG_END_DT date,
	ARV_END_DT date,
	ISTLTN_STRT_DT date,
	ISTLTN_END_DT date,
	ASBL_SMPL_DT date,
	TO_PRDT_DT date,
	PRDCTN_MNG_NM varchar(50),
	SALES_MNG_NM varchar(50),
	PRIMARY KEY (ODR_SEQ, ODR_DTL_SEQ)
);



