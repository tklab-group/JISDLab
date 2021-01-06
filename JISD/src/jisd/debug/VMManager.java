package jisd.debug;

/**
 * Manage a target VM.
 *
 * @author sugiyama
 */
public abstract class VMManager implements Runnable {

  /** Run the debugger. */
  @Override
  public abstract void run();

  /** Shut down the debugger. */
  abstract void shutdown();

  abstract void prepareStart(PointManager bpm);
}
