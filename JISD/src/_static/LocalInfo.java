package _static;

public class LocalInfo extends StaticInfo {
  String methodName;

  public LocalInfo(String className, String methodName, String name) {
    super(className, name);
    this.methodName = methodName;
  }

  public String methodName() {
    return methodName;
  }
}
