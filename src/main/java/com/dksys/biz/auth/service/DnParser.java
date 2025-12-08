package com.dksys.biz.auth.service;

import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import java.util.HashMap;
import java.util.Map;

public class DnParser {

    public static Map<String, String> parse(String subjectDN) {
        Map<String, String> out = new HashMap<>();

        try {
            LdapName dn = new LdapName(subjectDN);

            String cn = null;
            String o  = null;

            for (Rdn rdn : dn.getRdns()) {
                String type = rdn.getType();               // "CN", "OU", "O" ...
                String value = String.valueOf(rdn.getValue());

                if ("CN".equalsIgnoreCase(type)) cn = value;
                if ("O".equalsIgnoreCase(type))  o  = value;
            }

            out.put("orgName", o);

            // CN = js_nam_desktop → userId=js_nam, deviceType=desktop
            if (cn != null) {
                int lastUnderscore = cn.lastIndexOf('_');
                if (lastUnderscore > 0 && lastUnderscore < cn.length() - 1) {
                    out.put("userId", cn.substring(0, lastUnderscore));
                    out.put("deviceType", cn.substring(lastUnderscore + 1));
                } else {
                    // 언더스코어 규칙이 아니면 CN 전체를 userId로 처리
                    out.put("userId", cn);
                    out.put("deviceType", null);
                }
            }

            return out;

        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid subjectDN: " + subjectDN, e);
        }
    }
}
