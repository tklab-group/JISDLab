package jisd.analysis;

import jisd.debug.*;
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

import static jisd.debug.Utility.exec;
import static jisd.debug.Utility.sleep;

public class FaultFinder {
  @Getter @Setter
  public static String jisdCmdPath = "";
  @Getter @Setter int topN = 10;
  @Getter @Setter String projectDir;
  @Getter @Setter String projectName;
  @Getter @Setter String projectId;
  @Getter @Setter(AccessLevel.PACKAGE) List<FlResult> flResults=new ArrayList<>();
  @Setter(AccessLevel.PACKAGE) List<String> flResultLines=new ArrayList<>();
  @Getter @Setter(AccessLevel.PACKAGE) Integer generation = 0;
  @Getter @Setter(AccessLevel.PACKAGE) HashMap<String, List<FlResult>> flResultsMap = new HashMap<>();
  @Getter List<String> srcDirs = new ArrayList<>();

  public FaultFinder(String projectDir) {
    this.projectDir = projectDir;
  }

  public void setSrcDirs(String... paths) {
    srcDirs = Arrays.stream(paths).collect(Collectors.toList());
  }

  public List<FlResult> run() {
    if (flResultLines.isEmpty()) {
      Optional<String[]> resultOpt;
      if (projectName != "" && projectId != "") {
        resultOpt = exec(jisdCmdPath + " fl " + projectName + " " + projectId);
      } else {
        resultOpt = exec(jisdCmdPath + " " + projectDir);
      }
      if (!resultOpt.isPresent()) {
        return flResults;
      }
      var result = resultOpt.get();
      if (result[1].length() > 0) {
        Print.out(result[1]);
      }
      var flResultFilePathStr = result[0];
      setFlResultsFromCsv(flResultFilePathStr);
      updateGeneration();
    }

    showFlResults();
    return flResults;
  }

  public List<FlResult> susp(int rank) {
    if (!checkFlRankValidation(rank)) {
      return null;
    }
    var removedClassName = flResults.get(rank-1).className;
    for (int i = 0; i < flResults.size(); i++) {
      var res = flResults.get(i);
      if (res.className.equals(removedClassName)) {
         res.score += 0.5;
      }
    }
    reRanking();
    updateGeneration();
    showFlResults();
    return flResults;
  }

  public List<FlResult> remove(int rank) {
    if (!checkFlRankValidation(rank)) {
      return null;
    }
    var removedClassName = flResults.get(rank-1).className;
    flResults = flResults.stream()
      .filter(res->!res.className.equals(removedClassName))
      .collect(Collectors.toList());
    setRank();
    updateGeneration();
    showFlResults();
    return flResults;
  }

  void reRanking() {
    flResults.sort(Comparator.comparing(flResult -> -flResult.score));
    setRank();
  }

  void probe() {
    var flResultsTopN = flResults.stream().limit(topN).collect(Collectors.toList());
    String cmd;
    Thread targetVmThread;
    if (projectName != "" && projectId != "") {
      cmd = jisdCmdPath + " debug " + projectName + " " + projectId;
    } else {
      cmd = jisdCmdPath + " debug " + projectDir;
    }
    DebuggerInfo.setVerbose(false);
    targetVmThread = new Thread(()->{exec(cmd);});
    targetVmThread.start();
    sleep(Debugger.defaultSleepTime);
    var dbg = new Debugger(25432);
    var points = flResultsTopN.stream()
      .map(res->dbg.watch(res.className, res.line))

      .collect(Collectors.toList());
    var dbgThread = new Thread(()->{dbg.run();});
    dbgThread.start();
    try {
      dbgThread.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    DebuggerInfo.setVerbose(true);
    points.forEach(pOpt->{
      var p = pOpt.get();
      Print.out(p.getClassName()+":"+p.getLineNumber());
      p.getResults().forEach((name, r)->{
        Print.out(name+": "+r.lv());
      });
    });
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
    flResults =  flResultLines.stream()
      .map(flResultLine->flResultLine.split(","))
      .map(flResultStr->new FlResult(flResultStr[0], Integer.parseInt(flResultStr[1]), Double.parseDouble(flResultStr[2])))
      .collect(Collectors.toList());
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
      Print.out(flResult.rank + ". " + flResult.className+":"+flResult.line+" (score="+flResult.score+")");
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
  }

  public void clear() {
    flResults.clear();
    flResultsMap.clear();
    generation = 0;
  }
}
