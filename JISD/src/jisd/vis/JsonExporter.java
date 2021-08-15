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

public class JsonExporter implements IExporter{
  String host;
  int port;
  String name;
  private final StringBuilder jsonCache = new StringBuilder();
  Thread exporterThread;
  volatile boolean isStop = false;
  private int sleepTime = 0;
  @Getter
  @Setter
  private volatile boolean isVerbose = false;
  @Getter @Setter
  private volatile boolean isSuppressError = false;
  private Optional<LocalDateTime> previousUpdateTimeOpt = Optional.empty();
  String timeLocale;
  String exportUrl;

  public JsonExporter(String host, int port, String name, String timeLocale, String exportUrl) {
    this.host = host;
    this.port = port;
    this.name = name;
    this.timeLocale = timeLocale;
    this.exportUrl = exportUrl;
  }

  public String createJson(ValueInfo valueInfo) {
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
    } else {
      // the first time
      isSleep = true;
    }
    previousUpdateTimeOpt = Optional.of(currentTime);
    if (isSleep) {
      return sleepTime;
    } else {
      return 0;
    }
  }

  /**
   * post json cache data to Elasticsearch
   */
  public void postJson() {
    synchronized (jsonCache) {
      postJson(jsonCache.toString(), exportUrl);
      jsonCache.delete(0,jsonCache.length());
    }
  }

  /**
   * post json data
   * @param json json data
   * @return response
   */
  public String postJson(String json, String urlStr) {
    if (json.isEmpty()) {
      return "";
    }
    HttpURLConnection uc;
    try {
      URL url = new URL(urlStr);
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
      if (isVerbose) {
        Print.out("post json");
      }
      return body;
    } catch (IOException e) {
      e.printStackTrace();
      return "client - IOException : " + e.getMessage();
    }
  }
}
