package _static;

public class Local extends Static {
  String methodName;

  public Local(StaticFile staticFile, String className, String methodName, String name) {
    super(staticFile, className, name);
    this.methodName = methodName;
  }

  public String methodName() {
    return methodName;
  }
}
