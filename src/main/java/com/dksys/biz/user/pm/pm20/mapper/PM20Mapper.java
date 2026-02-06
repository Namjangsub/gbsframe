package com.dksys.biz.user.pm.pm20.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PM20Mapper {
	List<Map<String, String>> selectList_pm20(Map<String, String> paramMap);

	List<Map<String, String>> selectAgendaList(Map<String, String> paramMap);

	List<Map<String, String>> select_agenda_no_by_date(Map<String, String> paramMap);

	List<Map<String, String>> select_pm20_d02_List(Map<String, String> paramMap);

	String selectMaxFileTrgtKeySeq(Map<String, String> paramMap);
	
	int insert_update_agenda_title(Map<String, String> param);

	int pm20_main_update(Map<String, String> param);

	int pm20_d3_insert_update(Map<String, String> param);

	int pm20_d03_update_date(Map<String, String> param);

	int pm20_d02_update_date(Map<String, String> param);
	
	int pm20_update_file_trgt_key(Map<String, String> param);

	int pm20_update_status(Map<String, String> param);

	int pm20_d03_delete_by_agenda(Map<String, String> param);

	int pm20_d01_delete(Map<String, String> param);

	int pm20_d03_delete_by_date(Map<String, String> param);

	int pm20_d02_delete_by_date(Map<String, String> param);

	int pm20_shift_agenda_no_d01(Map<String, String> param);

	int pm20_shift_agenda_no_d03(Map<String, String> param);

	int pm20_move_agenda_no_d01(Map<String, String> param);

	int pm20_move_agenda_no_d03(Map<String, String> param);

	int pm20_d02_update(Map<String, Object> param);

	int pm20_d02_delete(Map<String, Object> param);

	int pm20_d02_delete_selected(Map<String, Object> param);

	int pm20_swap_agenda_no_d01(Map<String, String> param);

	int pm20_swap_agenda_no_d03(Map<String, String> param);

	int pm20_swap_agenda_no_d02(Map<String, String> param);

	int pm20_shift_agenda_no_d02(Map<String, String> param);

	int pm20_move_agenda_no_d02(Map<String, String> param);

	int pm20_swap_file_trgt_key(Map<String, String> param);

	int pm20_swap_file_trgt_key_step2(Map<String, String> param);

	int pm20_swap_file_trgt_key_step3(Map<String, String> param);

	int pm20_shift_file_trgt_key(Map<String, String> param);

	int pm20_m01_delete_main(Map<String, String> paramMap);

}
