package com.group.bdd.framework.api;

import com.group.bdd.framework.LogUtil;
import io.restassured.RestAssured;
import io.restassured.config.SSLConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.HashMap;

import static com.group.bdd.framework.Asserts.assertThat;
import static com.group.bdd.framework.ConfigLoader.config;
import static io.restassured.RestAssured.given;

public class RestAssuredAPI {

    private static final Logger LOG = LogManager.getLogger(RestAssuredAPI.class);

    ThreadLocal<RequestSpecification> request = new ThreadLocal<>();

    public static ThreadLocal<String> uri = new ThreadLocal<>();
    public static ThreadLocal<String> payload = new ThreadLocal<>();
    public static ThreadLocal<HashMap<String, String>> headersMap = new ThreadLocal<>();

    private String env = config().getString("test.environment");

    public void buildGETRequest(String url, HashMap<String, String> map) {
        uri.set(url);
        headersMap.set(map);
        LogUtil.log("Uri: '" + uri.get() + "'");
        LogUtil.logAttachment("Headers", headersMap.get().toString());
    }

    public void buildPOSTRequest(String url, HashMap<String, String> map, String body) {
        uri.set(url);
        headersMap.set(map);
        payload.set(body);
        LogUtil.log("Uri: '" + uri.get() + "'");
        LogUtil.logAttachment("Headers", headersMap.get().toString());
        LogUtil.logAttachment("Request Body:", body);
    }

    public void setKeyStoreAndTrusStoreforHTTPS() {
        KeyStore keyStore = null;
        KeyStore trustStore = null;
        SSLConfig config = null;

        String keyStorecertpath = System.getProperty("user.dir") + "\\src\\test\\resources\\certs\\keyStore.jks";
        String password = "changeit";
        String trustStorecertpath = System.getProperty("user.dir") + "\\src\\test\\resources\\certs\\trustStore.jks";

        try {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(
                    new FileInputStream(keyStorecertpath),
                    password.toCharArray());
            trustStore.load(
                    new FileInputStream(trustStorecertpath),
                    password.toCharArray());
            org.apache.http.conn.ssl.SSLSocketFactory clientAuthFactory = new org.apache.http.conn.ssl.SSLSocketFactory(keyStore, password, trustStore);
            clientAuthFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            config = new SSLConfig().with().sslSocketFactory(clientAuthFactory).and().allowAllHostnames();

            RestAssured.config = RestAssured.config().sslConfig(config);

        } catch (Exception ex) {
            System.out.println("Error while loading keystore  or trustStore>>>>>>>>>");
            ex.printStackTrace();
        }
    }

    public Response apiGET() {
        Response response = null;
        if (env.equals("TOBEUPDATED")) {
            setKeyStoreAndTrusStoreforHTTPS();
        }
        try {
            response = given().headers(headersMap.get()).
                    contentType(ContentType.JSON).
                    when().
                    get(uri.get());

            printLogBasedOnStatusCode(response);
        } catch (Exception ex) {
            assertThat("Unable to connect to service or error occurred inside api GET function. Error = " + ex.getMessage(), false);
        }
        return response;
    }

    public Response apiGETByContentTypeXML() {
        Response response = null;
        if (env.equals("TOBEUPDATED")) {
            setKeyStoreAndTrusStoreforHTTPS();
        }
        try {
            response = given().headers(headersMap.get()).
                    contentType(ContentType.XML).
                    when().
                    get(uri.get());
            printLogBasedOnStatusCode(response);

        } catch (Exception ex) {
            assertThat("Unable to connect to service or error occurred inside api GET function. Error = " + ex.getMessage(), false);
        }
        return response;
    }

    public Response apiPOSTMessage() {
        Response response = null;
        try {
            if (env.equals("TOBEUPDATED")) {
                setKeyStoreAndTrusStoreforHTTPS();
            }
            LogUtil.logAttachmentJson("Request Body :", payload.get());
            response = given().headers(headersMap.get()).contentType(ContentType.JSON).body(payload.get()).post(uri.get());
            printLogBasedOnStatusCode(response);
        } catch (Exception ex) {
            assertThat("Unable to connect to service or error occurred inside api GET function. Error = " + ex.getMessage(), false);
        }
        return response;
    }

    public Response apiPOSTMessageWithContentTypeXML() {
        Response response = null;
        try {
            if (env.equals("TOBEUPDATED")) {
                setKeyStoreAndTrusStoreforHTTPS();
            }
            LogUtil.logAttachmentJson("Request Body :", payload.get());
            response = given().headers(headersMap.get()).
                    contentType(ContentType.XML).
                    body(payload.get()).
                    post(uri.get());
            printLogBasedOnStatusCode(response);
        } catch (Exception ex) {
            assertThat("Unable to connect to service or error occurred inside api GET function. Error = " + ex.getMessage(), false);
        }
        return response;
    }

    public Response apiPUTMessage() {
        Response response = null;
        try {
            LogUtil.logAttachmentJson("Request Body :", payload.get());
            response = given().headers(headersMap.get()).contentType(ContentType.JSON).body(payload.get()).when().put(uri.get());
            printLogBasedOnStatusCode(response);
        } catch (Exception ex) {
            assertThat("Unable to connect to service or error occurred inside api GET function. Error = " + ex.getMessage(), false);
        }
        return response;
    }

    public Response apiPUTMessagewithoutBody() {
        Response response = null;
        try {
            LogUtil.logAttachmentJson("Request Body :", payload.get());
            response = given().headers(headersMap.get()).contentType(ContentType.JSON).when().put(uri.get());
            printLogBasedOnStatusCode(response);
        } catch (Exception ex) {
            assertThat("Unable to connect to service or error occurred inside api GET function. Error = " + ex.getMessage(), false);
        }
        return response;
    }

    public void printLogBasedOnStatusCode(Response response) {
        LogUtil.log("Response Status: " + response.getStatusCode());
        LogUtil.log("Response Line: " + response.getStatusLine());
        LogUtil.log("Response Time(milliseconds): " + response.getTime());
        if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
            if (response.getContentType().contains("json")) {
                LogUtil.logAttachmentJson("Actual Response :", response.getBody().prettyPrint());
            } else if (response.getContentType().contains("xml")) {
                LogUtil.logAttachmentXML("Actual Response :", response.getBody().prettyPrint());
            } else {
                LogUtil.logAttachment("Actual Response :", response.getBody().prettyPrint());
            }
        } else {
            LogUtil.logAttachment("Actual Response :", response.getBody().asString());
        }
    }

    public Response apiPATCHMessage() {
        Response response = null;
        try {
            LogUtil.logAttachmentJson("Request Body :", payload.get());
            response = given().headers(headersMap.get()).contentType(ContentType.JSON).body(payload.get()).when().patch(uri.get());
            printLogBasedOnStatusCode(response);
        } catch (Exception ex) {
            assertThat("Unable to connect to service or error occurred inside api GET function. Error = " + ex.getMessage(), false);
        }
        return response;
    }

    public Response apiPATCHMessagewithoutBody() {
        Response response = null;
        try {
            LogUtil.logAttachmentJson("Request Body :", payload.get());
            response = given().headers(headersMap.get()).contentType(ContentType.JSON).when().patch(uri.get());
            printLogBasedOnStatusCode(response);
        } catch (Exception ex) {
            assertThat("Unable to connect to service or error occurred inside api GET function. Error = " + ex.getMessage(), false);
        }
        return response;
    }

    public Response apiDELETE() {
        Response response = null;
        if (env.equals("TOBEUPDATED")) {
            setKeyStoreAndTrusStoreforHTTPS();
        }
        try {
            response = given().headers(headersMap.get()).
                    contentType(ContentType.JSON).
                    when().
                    delete(uri.get());

            printLogBasedOnStatusCode(response);
        } catch (Exception ex) {
            assertThat("Unable to connect to service or error occurred inside api GET function. Error = " + ex.getMessage(), false);
        }
        return response;
    }

}
