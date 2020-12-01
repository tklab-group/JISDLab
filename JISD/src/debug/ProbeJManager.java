/** */
package debug;

import probej.ProbeJ;

/** @author sugiyama */
class ProbeJManager extends VMManager {
  ProbeJ probeJ;
  /** */
  public ProbeJManager(ProbeJ probeJ) {
    this.probeJ = probeJ;
    probeJ.runVM();
    Debugger.sleep(1000);
    DebuggerInfo.print("VM with ProbeJ started.");
  }

  @Override
  public void run() {
    probeJ.run();
  }

  @Override
  void shutdown() {
    probeJ.exit();
  }

  @Override
  void prepareStart(PointManager bpm) {
    // TODO Auto-generated method stub

  }

  public ProbeJ getProbeJ() {
    return probeJ;
  }
}
