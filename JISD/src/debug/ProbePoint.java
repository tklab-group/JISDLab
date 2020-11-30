/** */
package debug;

import debug.value.ValueInfo;
import probej.ProbeJ;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

/** @author sugiyama */
public class ProbePoint extends Point {

  Optional<ProbeJ> p;
  /**
   * Constructor
   *
   * @param className class name
   * @param lineNumber line number
   * @param varNames variable names
   * @param isBreak break or not at points
   */
  ProbePoint(String className, int lineNumber, ArrayList<String> varNames, boolean isBreak) {
    super(className, lineNumber, "", varNames, isBreak);
  }

  /**
   * Constructor
   *
   * @param className class name
   * @param methodName method name
   * @param varNames variable names
   * @param isBreak break or not at points
   */
  ProbePoint(String className, String methodName, ArrayList<String> varNames, boolean isBreak) {
    super(className, 0, methodName, varNames, isBreak);
  }

  @Override
  void reset() {
    /* Todo: Implementation */
  }

  @Override
  void requestSetPoint(VMManager vmMgr, PointManager pm) {
    if (!(vmMgr instanceof ProbeJManager)) {
      /* do nothing */
      return;
    }
    p = Optional.of(((ProbeJManager) vmMgr).getProbeJ());
    if (varNames.isEmpty()) {
      return;
    }
    varNames.forEach(
        (varName) -> {
          p.get().requestSetProbePoint(className, varName, lineNumber);
        });
    setRequested(true);
  }

  @Override
  public HashMap<String, DebugResult> getResults() {
    fetchResults();
    return super.getResults();
  }

  void fetchResults() {
    if (p.isEmpty()) {
      return;
    }
    /* TODO: when varNames is empty */

    varNames.forEach(
        (varName) -> {
          HashMap<Location, ArrayList<ValueInfo>> results =
              p.get().getResults(className, varName, lineNumber);
          results.forEach(
              (key, values) -> {
                values.forEach(
                    value -> {
                      addValue(varName, value);
                    });
              });
        });
  }

  void addValue(String varName, ValueInfo value) {
    synchronized (this) {
      Optional<DebugResult> res = Optional.ofNullable(drs.get(varName));
      if (res.isPresent()) {
        res.get().addValue(value);
        return;
      }
      DebugResult dr = new DebugResult(className, lineNumber, varName);
      if (maxRecords.containsKey(varName)) {
        dr.setMaxRecordNoOfValue(maxRecords.get(varName));
      }
      if (maxExpands.containsKey(varName)) {
        dr.setMaxRecordNoOfValue(maxExpands.get(varName));
      }
      dr.addValue(value);
      addDebugResult(varName, dr);
    }
  }
}
