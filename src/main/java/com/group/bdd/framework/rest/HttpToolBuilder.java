package com.group.bdd.framework.rest;

import com.group.bdd.framework.rest.impl.HttpTool;
import com.group.bdd.framework.rest.impl.HttpToolImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;

public final class HttpToolBuilder {
    private int defaultTimeout = 60;
    private URI restHost;
    private String KeyStorePath;
    private String KeyStorePassword;

    private static final Logger LOG = LogManager.getLogger(HttpToolBuilder.class);

    HttpToolBuilder(URI restHost, String KeyStorePath, String KeyStorePassword) {
        this.restHost = restHost;
        this.KeyStorePassword = KeyStorePassword;
        this.KeyStorePath = KeyStorePath;
    }

    public static HttpToolBuilder newBuilder(URI restHost, String KeyStorePath, String KeyStorePassword) {
        return new HttpToolBuilder(restHost, KeyStorePath, KeyStorePassword);
    }

    public HttpToolBuilder timeout(int sec) {
        this.defaultTimeout = sec;
        return this;
    }

    public HttpTool build() {
        return new HttpToolImpl(this.restHost, this.KeyStorePath, this.KeyStorePassword, this.defaultTimeout);
    }
}

