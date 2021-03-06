package io.github.notapresent.usersampler.common.http;

import com.google.common.base.Charsets;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Response {

  final Map<String, String> headers;
  private final int status;
  private final byte[] content;
  private String finalUrl;
  private Request request;

  public Response(int status, byte[] content, String finalUrl) {
    this(status,
        new TreeMap<>(String::compareToIgnoreCase),
        content,
        finalUrl
    );
  }

  public Response(int status, Map<String, String> headers, byte[] content, String finalUrl) {
    this.status = status;
    this.headers = new TreeMap<>(String::compareToIgnoreCase);
    this.headers.putAll(headers);
    this.content = content;
    this.finalUrl = finalUrl;
  }

  public Request getRequest() {
    return request;
  }

  public void setRequest(Request request) {
    this.request = request;
  }

  public int getStatus() {
    return status;
  }

  public byte[] getContentBytes() {
    return content;
  }

  public String getFinalUrl() {
    return finalUrl;
  }

  public void setFinalUrl(String url) {
    finalUrl = url;
  }


  public Map<String, String> getHeaders() {
    return headers;
  }

  public String getContentString() {
    return new String(getContentBytes(), Charsets.UTF_8);
  }

  public String getContentString(Charset charSet) {
    return new String(getContentBytes(), charSet);
  }

  private void setHeader(String name, String value) {
    headers.put(name, value);
  }

  public void setHeader(String name, List<String> values) {
    setHeader(name, String.join(",", values));
  }

  public boolean isRedirect() {
    return Util.isRedirect(this.status);
  }
}
