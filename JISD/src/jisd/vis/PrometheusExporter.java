package jisd.vis;

import io.prometheus.client.exporter.MetricsServlet;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletContainerInitializerInfo;
import io.undertow.servlet.api.ServletInfo;
import io.undertow.servlet.util.DefaultClassIntrospector;
import jisd.debug.value.ValueInfo;
import org.jboss.resteasy.plugins.servlet.ResteasyServletInitializer;

import javax.servlet.ServletException;
import java.util.*;

import jisd.util.Number;

public class PrometheusExporter implements IExporter {
  String host;
  int port;
  String path;
  String name;
  Optional<Undertow> server = Optional.empty();
  Optional<DeploymentManager> manager = Optional.empty();
  DebugResource resource = new DebugResource();

  public PrometheusExporter(String host, int port, String path, String name) {
    this.host = host;
    this.port = port;
    this.path = path;
    this.name = name;
  }

  public PrometheusExporter(String host, int port) {
    this(host, port, "/metrics");
  }

  public PrometheusExporter(String host, int port, String path) {
    this(host, port, path, "jisdvis");
  }

  public void run() {
    String contextPath = "";

    Set<Class<?>> jaxrsClasses = new HashSet<>(Arrays.asList(JaxrsActivator.class));

    DeploymentInfo deployment;
    try {
      deployment = Servlets
        .deployment()
        .setClassLoader(PrometheusExporter.class.getClassLoader())
        .setDeploymentName("prometheus-client")
        .setContextPath(contextPath)
        .addServlet(createPrometheusServlet())  // サーブレットを追加
        .addServletContainerInitializer(
          new ServletContainerInitializerInfo(ResteasyServletInitializer.class,
            DefaultClassIntrospector.INSTANCE.createInstanceFactory(ResteasyServletInitializer.class),
            jaxrsClasses)
        );
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
      return;
    }

    manager = Optional.of(Servlets.defaultContainer().addDeployment(deployment));
    manager.get().deploy();
    HttpHandler serverHandler = null;
    try {
      serverHandler = manager.get().start();
    } catch (ServletException e) {
      e.printStackTrace();
      return;
    }

    // build server
    server = Optional.of(Undertow
      .builder()
      .addHttpListener(port, host)
      .setHandler(serverHandler)
      .build()
    );
    server.get().start();
  }

  // MetricsServletを定義
  ServletInfo createPrometheusServlet() {
    return new ServletInfo(name, MetricsServlet.class).addMappings(path);
  }

  public void stop() {
    if (server.isPresent()) {
      server.get().stop();
    }
    if (manager.isPresent()) {
      try {
        manager.get().stop();
      } catch (ServletException e) {
        e.printStackTrace();
        return;
      }
    }
  }

  @Override
  public void update(ValueInfo valueInfo) {
    String varName = valueInfo.getName();
    String value = valueInfo.getValue();
    try {
      if (Number.isNumeric(value)) {
        double valueDouble = Double.parseDouble(value);
        resource.updateDebugResultDouble(varName, valueDouble);
      }
    } catch (NumberFormatException e) {
      e.printStackTrace();
    }
  }
}