package io.github.notapresent.usersampler.common.http;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RequestTest {

  private final String url = "http://fake.url";
  private Request request;

  @Test
  public void itShouldSetDefaultTimeout() {
    request = new Request(url);
    assertEquals(5.0, request.getTimeout(), 0.1);
  }

  @Test
  public void itShouldUseGetAsDefaultMethod() {
    request = new Request(url);
    assertEquals(Method.GET, request.getMethod());
  }

  @Test
  public void itShouldUseDEFAULTRedirectPolicyByDefault() {
    request = new Request(url);
    assertEquals(request.getRedirectHandlingPolicy(),
        Request.RedirectPolicy.DEFAULT);
  }


}
