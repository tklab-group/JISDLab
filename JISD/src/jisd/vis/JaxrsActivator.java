package jisd.vis;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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