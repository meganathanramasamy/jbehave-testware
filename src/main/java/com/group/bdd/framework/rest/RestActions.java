package com.group.bdd.framework.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;

public class RestActions {
    public static final String BASE_URI_PARAM = "rest.base.uri";

    private static final Logger LOG = LogManager.getLogger(RestActions.class);

    public RestActions() {
    }

    public static HttpToolBuilder newBuilder(URI baseHost,String KeystorePath,String KeyStorePass) {
        return HttpToolBuilder.newBuilder(baseHost,KeystorePath,KeyStorePass);
    }

    public static RequestBuilder onUri(String uri,String KeystorePath,String KeyStorePass) {
        return newBuilder(URI.create(uri),KeystorePath,KeyStorePass).build().request();
    }
}

