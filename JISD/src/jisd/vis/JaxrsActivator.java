package jisd.vis;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("")
public class JaxrsActivator extends Application {
  //Set<Object> singletons = new HashSet<>();

  public JaxrsActivator() {
    //resources.forEach(res -> singletons.add(res));
    //singletons.add(new MetricsResource());
  }

  /*
  @Override
  public Set<Object> getSingletons() {
    return singletons;
  }
  */
}