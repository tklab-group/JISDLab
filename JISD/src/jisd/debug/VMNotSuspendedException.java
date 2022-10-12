package jisd.debug;

/**
 * Thrown to indicate a target VM is not suspended.
 *
 * @author sugiyama
 */
public class VMNotSuspendedException extends RuntimeException {
  public VMNotSuspendedException() {
    super("VM not suspended");
  }
  public VMNotSuspendedException(String msg) {
    super(msg);
  }
}
