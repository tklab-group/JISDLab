/** */
package jisd.debug;

import jisd.probej.ProbeJ;

/**
 * Manages a target VM with ProbeJ.
 *
 * @author sugiyama
 */
public class ProbeJManager extends VMManager {
  ProbeJ probeJ;
  /** */
  public ProbeJManager(ProbeJ probeJ) {
    this.probeJ = probeJ;
  }

  @Override
  public void run() {
    probeJ.runVM();
    probeJ.run();
    DebuggerInfo.print("Debugger started.");
  }

  @Override
  void shutdown() {
    probeJ.exit();
    DebuggerInfo.print("Debugger exited.");
  }

  @Override
  void prepareStart(PointManager bpm) {
    // TODO Auto-generated method stub

  }

  public ProbeJ getProbeJ() {
    return probeJ;
  }
}
