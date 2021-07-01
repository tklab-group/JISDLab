package jisd.vis;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import io.prometheus.client.Summary;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Path("counter")
public class DebugResource {
  AtomicInteger counter = new AtomicInteger(0);

  private static final Counter incCallCounter =
    Counter.build().name("inc_call_count").help("counter increment count").register();
  private static final Counter methodCallCounter =
    Counter.build().name("method_call_count").labelNames("method", "url").help("counter method call count").register();
  private static final Summary incRequestsSummary =
    Summary.build().name("inc_requests_summary").help("increment request summary").register();
  private static final Summary currentRequestsSummary =
    Summary.build().name("current_val_requests_summary").labelNames("url").help("current val request summary").register();
  private static final Gauge currentDebugResultGauge =

    Gauge.build().name("current_debug_result").labelNames("name").help("current debug result").register();
  private static final Histogram currentDebugResultHist =
    Histogram.build().name("current_debug_result_histogram").labelNames("name").help("current debug result").register();

  public void updateDebugResultDouble(String name, Double value) {
    currentDebugResultGauge.labels(name).set(value);
  }

  @GET
  @Path("inc")
  @Produces(MediaType.TEXT_PLAIN)
  public int inc() {
    return incRequestsSummary.time(() -> {
      try {
        TimeUnit.MILLISECONDS.sleep(ThreadLocalRandom.current().nextLong(1000L));

        return counter.incrementAndGet();
      } finally {
        incCallCounter.inc();
        methodCallCounter.labels("inc", "/counter/inc").inc();
      }
    });
  }

  @GET
  @Path("current")
  @Produces(MediaType.TEXT_PLAIN)
  public int current() {
    return currentRequestsSummary.labels("/counter/current").time(() -> {
      try {
        TimeUnit.MILLISECONDS.sleep(ThreadLocalRandom.current().nextLong(1000L));

        return counter.get();
      } finally {
        methodCallCounter.labels("current", "/counter/current").inc();
      }
    });
  }
}