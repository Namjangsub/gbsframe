SET SESSION FOREIGN_KEY_CHECKS=0;

/* Drop Indexes */

DROP INDEX bom_ix1 ON BOM;



/* Drop Tables */

DROP TABLE IF EXISTS BOM;


 
 
/* Create Tables */

-- BOM테이블
CREATE TABLE BOM
(
	prdt_cd varchar(20) NOT NULL,
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
	matr_mat_grp varchar(20) COMMENT 'M	소재비
A	구매품
AT	후처리
W	가공비
ELEC	전기자재
TURN	턴키
ETC 재고품',
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
	DSGN_trgt_YN varchar(1) COMMENT '설계대상항목구분',
	PRIMARY KEY (prdt_cd, matl_cd),
	UNIQUE (prdt_cd),
	CONSTRAINT BOM_UX1 UNIQUE (prdt_cd, matl_cd)
) COMMENT = 'BOM테이블';



/* Create Indexes */

-- 자재별 제품
CREATE INDEX bom_ix1 ON BOM (matl_cd ASC, prdt_cd ASC);



