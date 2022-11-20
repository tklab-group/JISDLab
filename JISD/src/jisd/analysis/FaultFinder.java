package jisd.analysis;

import jisd.debug.Debugger;
import jisd.debug.DebuggerInfo;
import jisd.debug.Location;
import jisd.debug.Utility;
import jisd.util.Print;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static jisd.debug.Utility.*;

public class FaultFinder {
  @Getter @Setter
  public static String jisdCmdPath = "";
  @Getter @Setter
  public static boolean usingProbeByVariableCount = false;
  @Getter @Setter
  public static boolean usingProbeByStackTrace = false;
  @Getter @Setter
  public static boolean usingProbeByProgramSlice = false;
  @Getter @Setter
  public static boolean usingProbeAtStart = false;
  @Getter @Setter int topN = 10;
  @Getter @Setter double contextConstValue=0.3;
  @Getter @Setter String projectDir;
  @Getter @Setter String projectName = "";
  @Getter @Setter String projectId = "";
  @Getter @Setter String flCmdName = "fl";
  @Getter @Setter Granularity contextGranularity = Granularity.CLASS;
  @Getter @Setter String flRankingPath = "";
  @Getter @Setter(AccessLevel.PACKAGE) List<FlResult> flResults=new ArrayList<>();
  @Setter(AccessLevel.PACKAGE) List<String> flResultLines=new ArrayList<>();
  @Getter @Setter(AccessLevel.PACKAGE) Integer generation = 0;
  @Getter @Setter(AccessLevel.PACKAGE) HashMap<String, List<FlResult>> flResultsMap = new HashMap<>();
  @Getter List<String> srcDirs = new ArrayList<>();

  public enum Granularity {
    LINE,
    METHOD,
    CLASS
  }

  public FaultFinder(String projectDir) {
    this.projectDir = projectDir;
  }
  public FaultFinder(String projectName, String projectId) {
    this.projectName = projectName;
    this.projectId = projectId;
  }

  public void setSrcDirs(String... paths) {
    srcDirs = Arrays.stream(paths).collect(Collectors.toList());
  }

  public FlResult get(int rank) {
    if (!checkFlRankValidation(rank)) {
      return null;
    }
    return flResults.get(rank-1);
  }

  public void run() {
    if (flResultLines.isEmpty()) {
      String flResultFilePathStr;
      if (!flRankingPath.isEmpty()) {
        flResultFilePathStr = flRankingPath;
      } else {
        Optional<String[]> resultOpt;
        if (!projectName.isEmpty() && !projectId.isEmpty()) {
          resultOpt = exec(jisdCmdPath + " "+flCmdName+" " + projectName + " " + projectId);
        } else {
          resultOpt = exec(jisdCmdPath + " "+flCmdName+" " + projectDir);
        }
        if (!resultOpt.isPresent()) {
          return;
        }
        var result = resultOpt.get();
        if (result[1].length() > 0) {
          Print.out(result[1]);
        }
        flResultFilePathStr = result[0];
      }
      setFlResultsFromCsv(flResultFilePathStr);
      updateGeneration();
    }
    showFlResults();
    return;
  }

  public void susp(int rank) {
    if (!checkFlRankValidation(rank)) {
      return;
    }
    Set<String> methodList = new HashSet<>();
    Map<Integer, Integer> lineNumberCount = new HashMap<>();

    if (usingProbeByVariableCount) {
      lineNumberCount = probeByVariableCount(rank);
    }
    if (usingProbeByStackTrace) {
      methodList = probeByStackTrace(rank);
    }
    if (usingProbeByProgramSlice) {
      methodList = probeByProgramSlice(rank);
    }
    var removedItem = flResults.get(rank-1);
    var newFlResults = new ArrayList<FlResult>();
    for (int i = 0; i < flResults.size(); i++) {
      var res = flResults.get(i);
      FlResult newRes;
      var isClassNameSame = res.className.equals(removedItem.className);
      var isMethodNameSame = res.getMethodNameWithParameters().equals(removedItem.getMethodNameWithParameters());
      var isLineNumberSame = res.line == removedItem.line;
      if (isClassNameSame && isMethodNameSame && isLineNumberSame) {
        continue;
      }
      if (contextGranularity == Granularity.LINE) {
        newRes = new FlResult(res.className, res.methodName, res.parameters, res.line, res.score);
      }
      else if (contextGranularity == Granularity.METHOD) {
        if (isClassNameSame && isMethodNameSame) {
          newRes = new FlResult(res.className, res.methodName, res.parameters, res.line, res.score+contextConstValue);
        } else {
          newRes = new FlResult(res.className, res.methodName, res.parameters, res.line, res.score);
        }
      } else {
        if (isClassNameSame) {
          newRes = new FlResult(res.className, res.methodName, res.parameters, res.line, res.score+contextConstValue);
        } else {
          newRes = new FlResult(res.className, res.methodName, res.parameters, res.line, res.score);
        }
      }
      newFlResults.add(newRes);
    }
    flResults = newFlResults;
    if (usingProbeByVariableCount) {
      updateScoreByVariableCount(removedItem, lineNumberCount);
    }
    if (usingProbeByStackTrace) {
      updateScoreByStackTrace(methodList);
    }
    if (usingProbeByProgramSlice) {
      updateScoreByProgramSlice(methodList);
    }
    reRanking();
    updateGeneration();
    showFlResults();
    return;
  }

  public void list(int rank) {
    if (!checkFlRankValidation(rank)) {
      return;
    }
    printSrc(flResults.get(rank-1), srcDirs);
  }

  public void list(FlResult result) {
    printSrc(result, srcDirs);
  }

  public void remove(int rank) {
    if (!checkFlRankValidation(rank)) {
      return;
    }
    var removedItem = flResults.get(rank-1);
    flResults = flResults.stream()
      .filter(res->{
        var isClassNameSame = res.className.equals(removedItem.className);
        var isMethodNameSame = res.getMethodNameWithParameters().equals(removedItem.getMethodNameWithParameters());
        var isLineNumberSame = res.line == removedItem.line;
        if (contextGranularity == Granularity.LINE) {
          return !(isClassNameSame && isMethodNameSame && isLineNumberSame);
        } else if (contextGranularity == Granularity.METHOD) {
          return !(isClassNameSame && isMethodNameSame);
        } else {
          return !isClassNameSame;
        }
      })
      .collect(Collectors.toList());
    setRank();
    updateGeneration();
    showFlResults();
    return;
  }

  void reRanking() {
    flResults.sort(Comparator.comparing(flResult -> -flResult.score));
    setRank();
  }

  private void updateScoreByVariableCount(FlResult removedflResult, Map<Integer, Integer> lineNumberCount) {
    var targetMethodName = removedflResult.getMethodFullName();
    //lineNumberCount.forEach((k,v)->Print.out(k+":"+v));
    for (var flResult: flResults) {
      if (targetMethodName.equals(flResult.getMethodFullName())) {
        Integer count = lineNumberCount.get(flResult.line);
        if (count != null) {
          flResult.setScore(flResult.score+contextConstValue*count/2.0);
        }
      }
    }
  }

  private void updateScoreByProgramSlice(Set<String> classList) {
    for (var cls: classList) {
      for (var flResult: flResults) {
        if (cls.equals(flResult.getClassName())) {
          flResult.setScore(flResult.score+contextConstValue);
        }
      }
    }
  }

  void updateScoreByStackTrace(Set<String> methodList) {
    for (var method: methodList) {
      for (var flResult: flResults) {
        if (method.equals(flResult.getMethodFullName())) {
          flResult.setScore(flResult.score+contextConstValue);
        }
      }
    }
  }

  public Map<Integer, Integer> probeByVariableCount(int rank) {
    if (!checkFlRankValidation(rank)) {
      return null;
    }
    var varList = probeVariable(rank);
    var flResult = flResults.get(rank-1);
    Debugger dbg;
    DebuggerInfo.setVerbose(false);
    if (projectName != "" && projectId != "") {
      dbg = new Debugger(25432, projectName, projectId);
    } else {
      // for debug
      String cmd = jisdCmdPath + " debug " + projectDir;
      var targetVmThread = new Thread(()->{exec(cmd);});
      targetVmThread.start();
      sleep(3000);
      dbg = new Debugger(25432);
    }
    dbg.setSrcDir(srcDirs.toArray(new String[0]));
    var flClassName = flResult.className;
    var flMethodName = flResult.methodName;
    dbg.stopAt(flClassName, flResult.line);
    List<Integer> lines = new ArrayList<>();
    dbg.run(0);
    dbg.sleepUntilBreak();
    try {
      while (true) {
        var location = dbg.loc();
        var className = location.getClassName();
        var methodName = location.getMethodName();
        if (!flClassName.equals(className) || !flMethodName.equals(methodName)) {
          dbg.exit();
          break;
        }
        lines.add(location.getLineNumber());
        dbg.stepOver();
      }
    } catch (Exception e) {
      dbg.exit();
    }
    Print.out(lines.size());
    DebuggerInfo.setVerbose(true);
    // count line number
    Map<Integer, Integer> lineNumberCount = new HashMap<>();
    varList.stream().filter(v->{
      for (int line: lines) {
        if (line == v.line) {
          return true;
        }
      }
      return false;
    }).forEach(v->{
      var lineNumberOpt = lineNumberCount.get(v.line);
      if (lineNumberOpt != null) {
        lineNumberCount.put(v.line, lineNumberOpt+1);
      } else {
        lineNumberCount.put(v.line, 1);
      }
    });
    return lineNumberCount;
  }

  public Set<String> probeByStackTrace(int rank) {
    if (!checkFlRankValidation(rank)) {
      return null;
    }
    var flResult = flResults.get(rank-1);
    Debugger dbg;
    DebuggerInfo.setVerbose(false);
    if (projectName != "" && projectId != "") {
      dbg = new Debugger(25432, projectName, projectId);
    } else {
      // for debug
      String cmd = jisdCmdPath + " debug " + projectDir;
      var targetVmThread = new Thread(()->{exec(cmd);});
      targetVmThread.start();
      sleep(3000);
      dbg = new Debugger(25432);
    }
    var pOpt = dbg.watch(flResult.className, flResult.line);
    dbg.runAll();
    DebuggerInfo.setVerbose(true);
    Set<String> methodList = new HashSet<>();
    var p = pOpt.get();
    //Print.out(p.getResults().get("this.numDigits").getValues().size());
    //p.getResults().forEach((k,v)->Print.out(v.lv()));
    p.getResults().forEach((k,v)->v.getValues().stream()
      .map(val->val.getStackTraceList().stream()
        .map(s->methodList.add(s.getMethodFullName()))
        .collect(Collectors.toList()))
      .collect(Collectors.toList()));
    return methodList;
  }
  public Set<String> probeByProgramSlice(int rank) {
    var varList = probeVariable(rank);

    Set<String> classList = varList.stream().map(v->v.type).collect(Collectors.toSet());
    return classList;
  }

  public List<Variable> probeVariable(int rank) {
    if (!checkFlRankValidation(rank)) {
      return new ArrayList<>();
    }
    var flResult = flResults.get(rank-1);
    Launcher launcher = new Launcher();

    // path can be a folder or a file
    // addInputResource can be called several times
    launcher.addInputResource(createPlainUri(flResult.className, srcDirs));
    // the compliance level should be set to the java version targeted by the input resources, e.g. Java 17
    launcher.getEnvironment().setComplianceLevel(8);
    String[] srcDirsArray = new String[srcDirs.size()];
    launcher.getEnvironment().setSourceClasspath(srcDirs.toArray(srcDirsArray));

    launcher.buildModel();

    CtModel model = launcher.getModel();
    CtType t = model.getAllTypes().stream().collect(Collectors.toList()).get(0);
    var methodName = flResult.methodName;
    var methods = (List<CtExecutable>) t.getMethodsByName(methodName);
    if (methods.size() == 0) {
      methods = (List<CtExecutable>) t.getTypeMembers().stream().filter(e-> {
        if (e instanceof CtConstructor) {
          return true;
        }
        return false;
      }).filter(e->((CtClass) ((CtConstructor)e).getParent()).getSimpleName().equals(methodName)).collect(Collectors.toList());
    }
    var method = methods.stream().filter(m->{
      boolean isFind = true;
      List<CtParameter> paras = m.getParameters();
      var flParas = flResult.parameters;
      if (paras.size() != flParas.size()) return false;
      for (int i = 0; i < paras.size(); i++) {
        var p = paras.get(i);
        if (!p.getType().toString().equals(flParas.get(i))) {
          isFind = false;
        }
      }
      return isFind;
    }).findFirst();
    if (method.isEmpty()) {
      return new ArrayList<>();
    }
    var m = method.get();
    CtBlock c = m.getBody();
    var e = c.getDirectChildren();
    List<Variable> varList = new ArrayList<>();
    probeElement(e, varList);
    return varList;
  }

  boolean probeElement(List<CtElement> es, List<Variable> varList) {
    boolean flag = es.isEmpty();
    for (var e : es) {
      if (e instanceof CtVariableAccess) {
        varList.add(new Variable(((CtVariableAccess)e).getType().toString(), e.toString(), e.getPosition().getLine()));
        //Print.out(((CtVariableAccess)e).getType()+": "+e+": "+e.getPosition().getLine());
      } else if (e instanceof CtVariable) {
        varList.add(new Variable(((CtVariable)e).getType().toString(), ((CtVariable) e).getSimpleName(), e.getPosition().getLine()));
        //Print.out(((CtVariable)e).getType()+": "+((CtVariable) e).getSimpleName()+": "+e.getPosition().getLine());
      }
      probeElement(e.getDirectChildren(), varList);
    }
    return flag;
  }

  void updateGeneration() {
    generation++;
    flResultsMap.put(generation.toString(), flResults);
  }

  public String uri(int rank) {
    if (!checkFlRankValidation(rank)) {
      return "";
    }
    var res = flResults.get(rank-1);
    var loc = new Location(res.className, "", res.line, "");
    return Utility.uri(loc, srcDirs);
  }

  public void setFlResultsFromCsv(String filePath) {
    Path flResultFilePath = Paths.get(filePath);
    try {
      flResultLines = Files.readAllLines(flResultFilePath);
    } catch (IOException e) {
      e.printStackTrace();
    }
    setFlResultsFromCsv(flResultLines);
  }

  void setFlResultsFromCsv(List<String> flResultLines) {
    flResults = flResultLines.stream().skip(1)
      .map(flResultLine -> {
        String[] classNameRaw = flResultLine.split("#");
        String className = classNameRaw[0].replace("$", ".");
        String[] methodNameRaw = classNameRaw[1].split(":");
        String methodName = methodNameRaw[0];
        String[] lineNumberRaw = methodNameRaw[1].split(";");
        int lineNumber = Integer.parseInt(lineNumberRaw[0]);
        double score = Double.parseDouble(lineNumberRaw[1]);
        return new FlResult(className, methodName, lineNumber, score);
      }).collect(Collectors.toList());
    setRank();
  }

  boolean checkFlRankValidation(int rank) {
    if (rank > flResults.size()) {
      Print.err("no such ranked element.");
      return false;
    }
    return true;
  }

  public void showFlResults() {
    Print.out("[Suspicious Line Ranking] (Current generation: "+generation+")");
    if (flResults.size() == 0) {
      Print.out("No element.");
      return;
    }
    flResults.stream().limit(topN).forEach(flResult-> {
      Print.out(flResult.rank + ". " + flResult.getClassName()+"#"+flResult.getMethodNameWithParameters() + ":" + flResult.line + " (score=" + flResult.score + ")");
    });
  }

  void setRank() {
    for (int rank=1; rank <= flResults.size(); rank++) {
      flResults.get(rank-1).setRank(rank);
    }
  }

  public void reset(Integer generation) {
    if (!flResultsMap.containsKey(generation.toString())) {
      Print.err("generation not found.");
      return;
    }
    flResults = flResultsMap.get(generation.toString());
    this.generation = generation;
    showFlResults();
  }

  public void clear() {
    flResults.clear();
    flResultsMap.clear();
    generation = 0;
  }
}
