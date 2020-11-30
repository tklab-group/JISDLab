package _static;

public class LocalInfo extends StaticInfo {
  String methodName;

  public LocalInfo(StaticFile staticFile, String className, String methodName, String name) {
    super(staticFile, className, name);
    this.methodName = methodName;
  }

  public String methodName() {
    return methodName;
  }
}
