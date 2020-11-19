/**
 * 
 */
package debug;

import probej.ProbeJ;

/**
 * @author sugiyama
 *
 */
class ProbeJManager extends VMManager {
  ProbeJ probeJ;
  /**
   * 
   */
  public ProbeJManager(ProbeJ probeJ) {
    this.probeJ = probeJ;
  }

  @Override
  public void run() {
    probeJ.run();
  }

  @Override
  void shutdown() {
    // TODO Auto-generated method stub
    probeJ.exit();
  }

  @Override
  void prepareStart(BreakPointManager bpm) {
    // TODO Auto-generated method stub
    
  }
  
  public ProbeJ getProbeJ() {
    return probeJ;  
  }
}
