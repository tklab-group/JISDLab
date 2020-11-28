package debug;

/**
 * Target JavaVM manager
 *
 * @author sugiyama
 */
abstract class VMManager implements Runnable {

  /** Run the debugger. */
  @Override
  public abstract void run();

  /** Shut down the debugger. */
  abstract void shutdown();

  abstract void prepareStart(PointManager bpm);
}
