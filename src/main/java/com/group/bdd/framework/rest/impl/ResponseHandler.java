package com.group.bdd.framework.rest.impl;

import com.group.bdd.framework.rest.HttpResponse;

import java.io.IOException;

public interface ResponseHandler {
    HttpResponse handle(org.apache.http.HttpResponse var1) throws IOException;
}
