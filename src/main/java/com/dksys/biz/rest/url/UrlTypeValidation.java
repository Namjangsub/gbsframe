package com.dksys.biz.rest.url;


import org.springframework.stereotype.Component;

import java.net.URL;
import java.net.URLConnection;

@Component
public class UrlTypeValidation {
    public boolean valid(String url) {
        try {
            URL connectionUrl = new URL(url);
            URLConnection conn = connectionUrl.openConnection();
            conn.connect();
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}