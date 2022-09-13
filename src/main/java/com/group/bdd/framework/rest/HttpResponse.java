package com.group.bdd.framework.rest;


import com.group.bdd.framework.rest.Constants.HttpStatus;

import java.io.InputStream;
import java.util.Map;

public interface HttpResponse {
    Map<String, String> getHeaders();

    HttpStatus getResponseCode();

    long getResponseTimeToEntityMillis();

    long getResponseTimeMillis();

    String getBody();

    long getSize();

    Map<String, String> getCookies();

    String getContentType();

    String getStatusLine();

    InputStream getContent();
}
