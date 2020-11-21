package _static;

public class Local extends Static {
  String methodName;

  public Local(String srcDir, String className, String methodName, String name) {
    super(srcDir, className, name);
    this.methodName = methodName;
  }

  public String methodName() {
    return methodName;
  }
}
