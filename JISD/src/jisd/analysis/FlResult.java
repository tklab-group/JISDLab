package jisd.analysis;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FlResult {
  @Getter String className;
  @Getter int line = 0;
  @Getter @Setter double score;
  @Getter @Setter String methodName = "";
  @Getter
  List<String> parameters;
  @Getter @Setter(AccessLevel.PACKAGE) int rank;

  public FlResult(String className, String methodName, int lineNumber, double score) {
    this.className = className;
    var methodNameRaw = methodName.split("\\(");
    this.methodName = methodNameRaw[0];
    this.parameters = Arrays.stream(methodNameRaw[1].replace(")", "").split(",")).collect(Collectors.toList());
    if (this.parameters.size()==1 && this.parameters.get(0).isEmpty()) {
      this.parameters = new ArrayList<>();
    }
    this.line = lineNumber;
    this.score = score;
  }

  public FlResult(String className, String methodName, List<String> parameters, int lineNumber, double score) {
    this.className = className;
    this.methodName = methodName;
    this.parameters = parameters;
    this.line = lineNumber;
    this.score = score;
  }

  public String getMethodNameWithParameters() {
    var parasOpt = parameters.stream().reduce((a, b)->a+","+b);
    var paras = (parasOpt.isPresent()) ? parasOpt.get() : "";
    var str = methodName + "("+ paras +")";
    return str;
  }

  public String getMethodFullName() {
    var parasOpt = parameters.stream().reduce((a, b)->a+","+b);
    var paras = (parasOpt.isPresent()) ? parasOpt.get() : "";
    var str = className + "." + methodName + "("+ paras+")";
    return str;
  }
}
