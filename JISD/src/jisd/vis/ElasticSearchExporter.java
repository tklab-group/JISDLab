package jisd.vis;

import jisd.debug.Utility;
import jisd.debug.value.ValueInfo;
import jisd.util.Print;
import jisd.util.Number;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

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
  public void update(ValueInfo valueInfo) {
    var jsonQuery = createJson(valueInfo);
    synchronized (jsonCache) {
      jsonCache.append(jsonQuery);
    }
    Utility.sleep(sleepTime);
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
      Print.out("post json");
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
