package com.dksys.biz.auth.mapper;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ClientCertMapper {

	Map<String, String> selectByCertSerial(Map<String, String> clientCertParm);
    
    int updateByCertSerial(Map<String, String> clientCertParm);

}