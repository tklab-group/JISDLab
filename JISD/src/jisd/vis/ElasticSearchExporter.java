package jisd.vis;

import jisd.debug.Utility;
import jisd.debug.value.ValueInfo;
import jisd.util.Number;
import jisd.util.Print;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public class ElasticSearchExporter implements IExporter {
  String host;
  int port;
  String name;
  private final StringBuilder jsonCache = new StringBuilder();
  Thread exporterThread;
  volatile boolean isStop = false;
  long id = 0;
  private String timeLocale;
  private int sleepTime = 0;
  @Getter @Setter
  private volatile boolean isVerbose = false;
  @Getter @Setter
  private volatile boolean isSuppressError = false;
  private Optional<LocalDateTime> previousUpdateTimeOpt = Optional.empty();

  public ElasticSearchExporter(String host, int port, String name) {
    this(host, port, name, "09:00");
  }

  public ElasticSearchExporter(String host, int port, String name, String timeLocale) {
    this.host = host;
    this.port = port;
    this.name = name;
    this.timeLocale = timeLocale;
  }

  public void run() {
    run(0);
  }

  public void run(int sleepTime) {
    isStop = false;
    this.sleepTime = sleepTime;
    exporterThread = new Thread(()->{
      while (!isStop) {
        // 1秒毎に送信
        Utility.sleep(1000);
        postJson();
      }
      postJson();
    });
    exporterThread.start();
  }

  public void stop() {
    isStop = true;
  }

  @Override
  public int update(ValueInfo valueInfo) {
    var jsonQuery = createJson(valueInfo);
    synchronized (jsonCache) {
      jsonCache.append(jsonQuery);
    }
    var currentTime = valueInfo.getCreatedAt();
    var isSleep = false;
    if (previousUpdateTimeOpt.isPresent()) {
      var previousUpdateTime = previousUpdateTimeOpt.get();
      if (! previousUpdateTime.equals(currentTime)) {
        // different observed point
        long timeDiff = previousUpdateTime.until(currentTime, ChronoUnit.MILLIS);
        if (timeDiff < 10) {
          // 前回時刻から10ms以上経過していないとき
          if (!isSuppressError) {
            Print.err("Some values may not be displayed in the visualization tool because the time interval is too short(< 10ms).");
          }
        }
        isSleep = true;
      }
    }
    previousUpdateTimeOpt = Optional.of(currentTime);
    if (isSleep) {
      return sleepTime;
    } else {
      return 0;
    }
  }

  String createJson(ValueInfo valueInfo) {
    var varName = valueInfo.getName();
    var value = valueInfo.getValue();
    var valueStr = value;
    if (!Number.isNumeric(value)) {
      value = "0";
    }
    var timestamp = valueInfo.getCreatedAt().toString();
    StringBuilder sb = new StringBuilder();
    sb.append("{\"create\":{\"_index\":\"").append(name).append("\"}}\n")
      .append("{")
      .append("\"@timestamp\":").append("\"").append(timestamp+"+"+timeLocale).append("\",")
      .append("\"name\":").append("\"").append(varName).append("\",")
      .append("\"value\":").append(value).append(",")
      .append("\"value_string\":").append("\"").append(valueStr).append("\"")
      .append("}\n");
    return sb.toString();
  }

  public void postJson() {
    synchronized (jsonCache) {
      postJson(jsonCache.toString());
      if (isVerbose) {
        Print.out("post json");
      }
      jsonCache.delete(0,jsonCache.length());
    }
  }

  String postJson(String json) {
    if (json.isEmpty()) {
      return "";
    }
    HttpURLConnection uc;
    try {
      var separator = File.separator.charAt(0);
      String query = host+":"+port+separator+"_bulk?pretty";
      URL url = new URL(query);
      uc = (HttpURLConnection) url.openConnection();
      uc.setRequestMethod("POST");
      uc.setUseCaches(false);
      uc.setDoOutput(true);
      uc.setRequestProperty("Content-Type", "application/json; charset=utf-8");
      OutputStreamWriter out = new OutputStreamWriter(
        new BufferedOutputStream(uc.getOutputStream()));
      out.write(json);
      out.close();

      BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
      String line = in.readLine();
      String body = "";
      while (line != null) {
        body = body + line;
        line = in.readLine();
      }
      uc.disconnect();
      return body;
    } catch (IOException e) {
      e.printStackTrace();
      return "client - IOException : " + e.getMessage();
    }
  }
}
