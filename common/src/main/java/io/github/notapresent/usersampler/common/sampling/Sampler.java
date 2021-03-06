package io.github.notapresent.usersampler.common.sampling;

import com.google.inject.Inject;
import io.github.notapresent.usersampler.common.http.HttpError;
import io.github.notapresent.usersampler.common.http.Request;
import io.github.notapresent.usersampler.common.http.RequestFactory;
import io.github.notapresent.usersampler.common.http.RequestMultiplexer;
import io.github.notapresent.usersampler.common.http.Response;
import io.github.notapresent.usersampler.common.site.FatalSiteError;
import io.github.notapresent.usersampler.common.site.RetryableSiteError;
import io.github.notapresent.usersampler.common.site.SiteAdapter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


public class Sampler {

  public static final int MAX_BATCH_RETRIES = 2;
  private final Set<SiteAdapter> inProgress = new HashSet<>();
  private final Map<SiteAdapter, Sample> results = new HashMap<>();
  private final RequestMultiplexer muxer;
  private final RequestFactory requestFactory;


  @Inject
  public Sampler(RequestMultiplexer muxer, RequestFactory requestFactory) {
    this.muxer = muxer;
    this.requestFactory = requestFactory;
  }

  public Map<SiteAdapter, Sample> takeSamples(List<SiteAdapter> adapters) {
    for (SiteAdapter site : adapters) {
      site.reset();
      inProgress.add(site);
    }

    while (!inProgress.isEmpty()) {
      processBatch(makeBatch(inProgress));
    }

    return results;
  }

  private RequestBatch makeBatch(Collection<SiteAdapter> sites) {
    RequestBatch batch = new RequestBatch();

    for (SiteAdapter site : sites) {
      site.getRequests(requestFactory).forEach((req) -> batch.put(req, site));
    }

    return batch;
  }

  private void processBatch(RequestBatch batch) {
    int batchRetries = 0;

    while (batchRetries++ < MAX_BATCH_RETRIES && !batch.isEmpty()) {
      RequestBatch retryBatch = new RequestBatch();
      Map<Request, Future<Response>> responseFutures = muxer.multiSend(batch.requests());

      for (Request request : responseFutures.keySet()) {
        SiteAdapter site = batch.siteFor(request);

        Future<Response> responseFuture = responseFutures.get(request);

        if (processResponseFuture(site, responseFuture)) {
          retryBatch.put(request, site);
        }
      }

      batch = retryBatch;
    }

    for (SiteAdapter site : batch.sites()) {
      inProgress.remove(site);
      results.put(site, errorSample(site));
    }
  }

  private boolean processResponseFuture(SiteAdapter site, Future<Response> respFut) {
    try {
      Response response = respFut.get();
      site.registerResponse(response);

      if (site.isDone()) {
        inProgress.remove(site);
        results.put(site, okSample(site));
      }
      return false;
    } catch (HttpError | FatalSiteError e) {    // Failed request considered a fatal error
      inProgress.remove(site);
      results.put(site, errorSample(site));
      return false;
    } catch (RetryableSiteError e) {
      return true;
    } catch (InterruptedException | ExecutionException e) {   // Should never happen
      throw new RuntimeException(e);
    }
  }

  private Sample okSample(SiteAdapter site) {
    return new Sample(site.getResult(), SampleStatus.OK);
  }

  private Sample errorSample(SiteAdapter site) {
    return new Sample(new HashMap<>(), SampleStatus.ERROR);
  }
}
