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
    clearDebugResults();
    p = Optional.empty();
    setRequested(false);
  }

  @Override
  void requestSetPoint(VMManager vmMgr, PointManager pm) {
    if (!(vmMgr instanceof ProbeJManager)) {
      /* do nothing */
      return;
    }
    p = Optional.of(((ProbeJManager) vmMgr).getProbeJ());
    requestSetPoint(p);
  }

  void requestSetPoint(Optional<ProbeJ> p) {
    if (p.isEmpty()) {
      return;
    }
    if (varNames.isEmpty()) {
      // Todo: *
      return;
    }
    varNames.forEach(
        (varName) -> {
          p.get().requestSetProbePoint(new Location(className, methodName, lineNumber, varName));
        });
    setRequested(true);
  }

  @Override
  public void add(String varName) {
    if (p.isEmpty()) {
      return;
    }
    if (varNames.contains(varName)) {
      DebuggerInfo.print("This name is already registered.");
      return;
    }
    p.get().requestSetProbePoint(new Location(className, methodName, lineNumber, varName));
    addVarName(varName);
  }

  @Override
  public void remove(String varName) {
    if (p.isEmpty()) {
      return;
    }
    p.get().requestRemoveProbePoint(new Location(className, methodName, lineNumber, varName));
    removeVarName(varName);
  }

  @Override
  public void enable() {
    isEnable = true;
    varNames.forEach(
        varName -> {
          requestSetPoint(p);
        });
  }

  @Override
  public void disable() {
    isEnable = false;
    varNames.forEach(
        varName -> {
          remove(varName);
        });
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
          var results =
              p.get().getResults(new Location(className, methodName, lineNumber, varName));
          results.forEach(
              (key, values) -> {
                addValues(key.varName, values);
              });
        });
  }

  void addValues(String varName, ArrayList<ValueInfo> values) {
    synchronized (this) {
      Optional<DebugResult> res = Optional.ofNullable(drs.get(varName));
      if (res.isPresent()) {
        res.get().addValues(values);
        return;
      }
      Location loc = new Location(className, methodName, lineNumber, varName);
      DebugResult dr = new DebugResult(loc);
      if (maxRecords.containsKey(varName)) {
        dr.setMaxRecordNoOfValue(maxRecords.get(varName));
      }
      if (maxExpands.containsKey(varName)) {
        dr.setMaxRecordNoOfValue(maxExpands.get(varName));
      }
      dr.addValues(values);
      addDebugResult(varName, dr);
    }
  }

  void addValue(String varName, ValueInfo value) {
    synchronized (this) {
      Optional<DebugResult> res = Optional.ofNullable(drs.get(varName));
      if (res.isPresent()) {
        res.get().addValue(value);
        return;
      }
      Location loc = new Location(className, methodName, lineNumber, varName);
      DebugResult dr = new DebugResult(loc);
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
