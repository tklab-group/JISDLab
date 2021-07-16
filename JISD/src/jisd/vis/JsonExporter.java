package jisd.vis;

import jisd.debug.Utility;
import jisd.debug.value.ValueInfo;
import jisd.util.Print;
import jisd.util.Number;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import static java.lang.System.getProperty;

public class JsonExporter implements IExporter {
  String host;
  int port;
  String name;
  private final StringBuilder jsonCache = new StringBuilder();
  Thread exporterThread;
  volatile boolean isStop = false;
  long id = 0;

  public JsonExporter(String host, int port, String name) {
    this.host = host;
    this.port = port;
    this.name = name;
  }

  public void run() {
    isStop = false;
    exporterThread = new Thread(()->{
      while (!isStop) {
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
    Utility.sleep(10);
    synchronized (jsonCache) {
      jsonCache.append(jsonQuery);
    }
  }

  String createJson(ValueInfo valueInfo) {
    var varName = valueInfo.getName();
    var value = valueInfo.getValue();
    if (!Number.isNumeric(value)) {
      value = "0";
    }
    var timestamp = valueInfo.getCreatedAt().toString();
    StringBuilder sb = new StringBuilder();
    sb.append("{\"update\":{\"_index\":\"").append(name).append("\",\"_id\": \"").append(id++).append("\"}}\n")
      .append("{\"doc\":{")
      .append("\"@timestamp\":").append("\"").append(timestamp+"+09:00").append("\",")
      .append("\"name\":").append("\"").append(varName).append("\",")
      .append("\"value\":").append(value)
      .append("}}\n");
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
