package com.dksys.biz.main.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
	private String username;
	private String password;
    private String id; // 클라이언트에서 보낸 id도 수용
    private String grant_type; // grant_type: password 도 수용
}