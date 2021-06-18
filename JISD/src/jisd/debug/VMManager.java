package jisd.debug;

import lombok.Getter;

/**
 * Manage a target VM.
 *
 * @author sugiyama
 */
public abstract class VMManager implements Runnable {

  @Getter private final Debugger debugger;
  VMManager(Debugger debugger) {
    this.debugger = debugger;
  }

  /** Run the debugger. */
  @Override
  public abstract void run();

  /** Shut down the debugger. */
  abstract void shutdown();

  abstract void prepareStart(PointManager bpm);
}
