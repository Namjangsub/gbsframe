package com.dksys.biz.rest.url;

import java.util.Random;

import org.springframework.stereotype.Component;

@Component
public class Base62Util {

	private static final char[] BASE62 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
	private static final int BASE = 62;
	
    public static String encoding(int id) {
        final StringBuilder sb = new StringBuilder();
        
        do {
            int i = id % BASE;
            sb.append(BASE62[i]);
            id /= BASE;
        } while (id > 0);
        
        return sb.toString();
    }

    public static int decoding(String shortUrl) {
        int result = 0;
        int power = 1;
        
        for (int i = 0; i < shortUrl.length(); i++) {
            int digit = new String(BASE62).indexOf(shortUrl.charAt(i));
            result += digit * power;
            power *= BASE;
        }
        
        return result;
    }

    // 인증번호 및 임시 비밀번호 생성
    public String createCode() {
        Random random = new Random();
        StringBuffer key = new StringBuffer();

        for (int i = 0; i < 5; i++) {
            int index = random.nextInt(4);

            switch (index) {
                case 0: key.append((char) ((int) random.nextInt(26) + 97)); break;
                case 1: key.append((char) ((int) random.nextInt(26) + 65)); break;
                default: key.append(random.nextInt(9));
            }
        }
        return key.toString();
    }
}