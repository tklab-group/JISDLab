package jisd.vis;

public class ElasticsearchExporter extends JsonExporter {

  public ElasticsearchExporter(String host, int port, String name) {
    this(host, port, name, "00:00");
  }

  public ElasticsearchExporter(String host, int port, String name, String timeLocale) {
    super(host, port, name, timeLocale, host+":"+port+"/_bulk?pretty");
  }
}
