package io.github.notapresent;

import com.google.appengine.api.urlfetch.*;

import java.io.IOException;
import java.net.URL;


public class HTTPSession {
    public static final int MAX_REDIRECTS = 5;
    private URLFetchService service;
    private HTTPSessionCookieManager cookieManager;

    public HTTPSession(URLFetchService service, HTTPSessionCookieManager cookieManager) {
        this.service = service;
        this.cookieManager = cookieManager;
    }

    public HTTPResponse fetch(URL url) throws IOException {
        HTTPSessionRequest req = new HTTPSessionRequest(url, HTTPMethod.GET,
                FetchOptions.Builder.withDefaults());
        return fetch(req);
    }

    public HTTPResponse fetch(HTTPSessionRequest req) throws IOException {
        int numHops = 0;
        boolean followRedirects = req.getFollowRedirects();
        HTTPResponse resp = null;

        while (++numHops < MAX_REDIRECTS) {
            resp = doRequest(req);

            if (!followRedirects || !HTTPUtil.isRedirect(resp.getResponseCode())) {
                break;
            }
            req = HTTPSessionRequest.makeRedirect(req, resp);
        }
        return resp;
    }

    private HTTPResponse doRequest(HTTPSessionRequest req) throws IOException {
        HTTPHeader cookieHeader = cookieManager.load(req.getURL());

        if (cookieHeader != null) {
            req.addHeader(cookieHeader);
        }

        HTTPResponse resp = service.fetch(req);

        cookieManager.save(req.getURL(), resp.getHeadersUncombined());

        return resp;
    }
}