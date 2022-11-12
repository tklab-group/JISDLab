package jisd.analysis;

import jisd.debug.Debugger;
import jisd.debug.DebuggerInfo;
import jisd.debug.Location;
import jisd.debug.Utility;
import jisd.util.Print;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

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
  @Getter @Setter int topN = 10;
  @Getter @Setter double contextConstValue=0.3;
  @Getter @Setter String projectDir;
  @Getter @Setter String projectName = "";
  @Getter @Setter String projectId = "";
  @Getter @Setter String flCmdName = "fl";
  @Getter @Setter Granularity contextGranularity = Granularity.CLASS;
  @Getter @Setter String flRankingPath;
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
    Set<String> methodList = probe(rank);
    var removedItem = flResults.get(rank-1);
    var newFlResults = new ArrayList<FlResult>();
    for (int i = 0; i < flResults.size(); i++) {
      var res = flResults.get(i);
      FlResult newRes;
      var isClassNameSame = res.className.equals(removedItem.className);
      var isMethodNameSame = res.methodName.equals(removedItem.methodName);
      var isLineNumberSame = res.line == removedItem.line;
      if (contextGranularity == Granularity.LINE || (isClassNameSame && isMethodNameSame && isLineNumberSame)) {
        continue;
      }
      if (contextGranularity == Granularity.METHOD) {
        if (isClassNameSame && isMethodNameSame) {
          newRes = new FlResult(res.className, res.methodName, res.line, res.score+contextConstValue);
        } else {
          newRes = new FlResult(res.className, res.methodName, res.line, res.score);
        }
      } else {
        if (isClassNameSame) {
          newRes = new FlResult(res.className, res.methodName, res.line, res.score+contextConstValue);
        } else {
          newRes = new FlResult(res.className, res.methodName, res.line, res.score);
        }
      }
      newFlResults.add(newRes);
    }
    flResults = newFlResults;
    updateScoreByStackTrace(methodList);
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
        var isMethodNameSame = res.methodName.equals(removedItem.methodName);
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

  void updateScoreByStackTrace(Set<String> methodList) {
    for (var method: methodList) {
      for (var flResult: flResults) {
        if (method.equals(flResult.className+"."+flResult.methodName)) {
          flResult.setScore(flResult.score+contextConstValue);
        }
      }
    }
  }

  public Set<String> probe(int rank) {
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
    var dbgThread = new Thread(()->{dbg.runAll();});
    dbgThread.start();
    try {
      dbgThread.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    DebuggerInfo.setVerbose(true);
    Set<String> methodList = new HashSet<>();
    var p = pOpt.get();
    var stackTraceList = p.getStackTraceList();
    stackTraceList.stream().map(s->methodList.add(s.getClassName()+"."+s.getMethodName())).collect(Collectors.toList());
    return methodList;
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
      Print.out(flResult.rank + ". " + flResult.className+"#"+flResult.methodName + ":" + flResult.line + " (score=" + flResult.score + ")");
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
