/*
 * Copyright (C) 2008 Esmertec AG.
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.russellpwirtz.library;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

public class HttpUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtil.class);

    public static final int HTTP_POST_METHOD = 1;
    public static final int HTTP_GET_METHOD = 2;

    private static final String HDR_VALUE_ACCEPT_LANGUAGE;

    static {
        HDR_VALUE_ACCEPT_LANGUAGE = getHttpAcceptLanguage();
    }

    // Definition for necessary HTTP headers.
    private static final String HDR_KEY_ACCEPT = "Accept";
    private static final String HDR_KEY_ACCEPT_LANGUAGE = "Accept-Language";

    private static final String HDR_VALUE_ACCEPT =
        "*/*, application/vnd.wap.mms-message, application/vnd.wap.sic";

    private HttpUtil() {}

    /**
     * A helper method to send or retrieve data through HTTP protocol.
     *
     * @param token The token to identify the sending progress.
     * @param url The URL used in a GET request. Null when the method is
     *         HTTP_POST_METHOD.
     * @param pdu The data to be POST. Null when the method is HTTP_GET_METHOD.
     * @param method HTTP_POST_METHOD or HTTP_GET_METHOD.
     * @return A byte array which contains the response data.
     *         If an HTTP error code is returned, an IOException will be thrown.
     * @throws IOException if any error occurred on network interface or
     *         an HTTP error code(&gt;=400) returned from the server.
     */
    public static byte[] httpConnection(Context context, long token,
            String url, byte[] pdu, int method, boolean isProxySet,
            String proxyHost, int proxyPort) throws IOException {
        if (url == null) {
            throw new IllegalArgumentException("URL must not be null.");
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("httpConnection: params list");
            LOGGER.debug("\ttoken\t\t= " + token);
            LOGGER.debug("\turl\t\t= " + url);
            LOGGER.debug("\tmethod\t\t= "
                    + ((method == HTTP_POST_METHOD) ? "POST"
                            : ((method == HTTP_GET_METHOD) ? "GET" : "UNKNOWN")));
            LOGGER.debug("\tisProxySet\t= " + isProxySet);
            LOGGER.debug("\tproxyHost\t= " + proxyHost);
            LOGGER.debug("\tproxyPort\t= " + proxyPort);
            // TODO Print out binary data more readable.
            //Log.v(TAG, "\tpdu\t\t= " + Arrays.toString(pdu));
        }

        HttpClient client = null;

        try {
            // Make sure to use a proxy which supports CONNECT.
            URI hostUrl = new URI(url);
            HttpHost target = new HttpHost(
                    hostUrl.getHost(), hostUrl.getPort(),
                    HttpHost.DEFAULT_SCHEME_NAME);

            client = createHttpClient();
            HttpRequest req = null;
            switch(method) {
                case HTTP_POST_METHOD:
                    ProgressCallbackEntity entity = new ProgressCallbackEntity(
                                                        context, token, pdu);
                    // Set request content type.
                    entity.setContentType("application/vnd.wap.mms-message");

                    HttpPost post = new HttpPost(url);
                    post.setEntity(entity);
                    req = post;
                    break;
                case HTTP_GET_METHOD:
                    req = new HttpGet(url);
                    break;
                default:
                    LOGGER.error("Unknown HTTP method: " + method
                            + ". Must be one of POST[" + HTTP_POST_METHOD
                            + "] or GET[" + HTTP_GET_METHOD + "].");
                    return null;
            }

            // Set route parameters for the request.
            HttpParams params = client.getParams();
            if (isProxySet) {
                ConnRouteParams.setDefaultProxy(
                        params, new HttpHost(proxyHost, proxyPort));
            }
            req.setParams(params);

            // Set necessary HTTP headers for MMS transmission.
//            req.addHeader(HDR_KEY_ACCEPT, HDR_VALUE_ACCEPT);
//            {
//                String xWapProfileTagName = MmsConfig.getUaProfTagName();
//                String xWapProfileUrl = MmsConfig.getUaProfUrl();
//
//                if (xWapProfileUrl != null) {
//                    req.addHeader(xWapProfileTagName, xWapProfileUrl);
//                }
//            }

            // Extra http parameters. Split by '|' to get a list of value pairs.
            // Separate each pair by the first occurrence of ':' to obtain a name and
            // value. Replace the occurrence of the string returned by
            // MmsConfig.getHttpParamsLine1Key() with the users telephone number inside
            // the value.
//            String extraHttpParams = MmsConfig.getHttpParams();
//            if (extraHttpParams != null) {
//                String line1Number = ((TelephonyManager)context
//                        .getSystemService(Context.TELEPHONY_SERVICE))
//                        .getLine1Number();
//                String line1Key = MmsConfig.getHttpParamsLine1Key();
//                String paramList[] = extraHttpParams.split("\\|");
//
//                for (String paramPair : paramList) {
//                    String splitPair[] = paramPair.split(":", 2);
//
//                    if (splitPair.length == 2) {
//                        String name = splitPair[0].trim();
//                        String value = splitPair[1].trim();
//
//                        if (line1Key != null) {
//                            value = value.replace(line1Key, line1Number);
//                        }
//                        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(value)) {
//                            req.addHeader(name, value);
//                        }
//                    }
//                }
//            }
            req.addHeader(HDR_KEY_ACCEPT_LANGUAGE, HDR_VALUE_ACCEPT_LANGUAGE);

            HttpResponse response = client.execute(target, req);
            StatusLine status = response.getStatusLine();
            if (status.getStatusCode() != 200) { // HTTP 200 is success.
                throw new IOException("HTTP error: " + status.getReasonPhrase());
            }

            HttpEntity entity = response.getEntity();
            byte[] body = null;
            if (entity != null) {
                try {
                    if (entity.getContentLength() > 0) {
                        body = new byte[(int) entity.getContentLength()];
                        DataInputStream dis = new DataInputStream(entity.getContent());
                        try {
                            dis.readFully(body);
                        } finally {
                            try {
                                dis.close();
                            } catch (IOException e) {
                                LOGGER.error("Error closing input stream: " + e.getMessage());
                            }
                        }
                    }
                } finally {
                    entity.consumeContent();
                }
            }
            return body;
        } catch (URISyntaxException e) {
            handleHttpConnectionException(e);
        } catch (IllegalStateException e) {
            handleHttpConnectionException(e);
        } catch (IllegalArgumentException e) {
            handleHttpConnectionException(e);
        } catch (SocketException e) {
            handleHttpConnectionException(e);
        } catch (Exception e) {
            handleHttpConnectionException(e);
        }
        finally {
//            if (client != null) {
//                client.close();
//            }
        }
        return null;
    }

    private static void handleHttpConnectionException(Exception exception)
            throws IOException {
        // Inner exception should be logged to make life easier.
        LOGGER.error(exception.getMessage());
        throw new IOException(exception.getMessage());
    }

    private static int mHttpSocketTimeout = 60*1000;            // default to 1 min
    private static HttpClient createHttpClient() {
        String userAgent = "Android-Http/0.1";
        HttpClient client = new DefaultHttpClient();
        HttpParams params = client.getParams();
        HttpProtocolParams.setContentCharset(params, "UTF-8");

        // set the socket timeout
        int soTimeout = mHttpSocketTimeout;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[HttpUtil] createHttpClient w/ socket timeout " + soTimeout + " ms, "
                    + ", UA=" + userAgent);
        }

        HttpConnectionParams.setSoTimeout(params, soTimeout);
        return client;
    }

    /**
     * Return the Accept-Language header.  Use the current locale plus
     * US if we are in a different locale than US.
     */
    private static String getHttpAcceptLanguage() {
        Locale locale = Locale.getDefault();
        StringBuilder builder = new StringBuilder();

        addLocaleToHttpAcceptLanguage(builder, locale);
        if (!locale.equals(Locale.US)) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            addLocaleToHttpAcceptLanguage(builder, Locale.US);
        }
        return builder.toString();
    }

    private static void addLocaleToHttpAcceptLanguage(
            StringBuilder builder, Locale locale) {
        String language = locale.getLanguage();

        if (language != null) {
            builder.append(language);

            String country = locale.getCountry();

            if (country != null) {
                builder.append("-");
                builder.append(country);
            }
        }
    }
}
