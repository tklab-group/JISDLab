package jisd.debug;

/**
 * Thrown to indicate a target VM is already started.
 *
 * @author sugiyama
 */
public class VMAlreadyStartedException extends RuntimeException {
  public VMAlreadyStartedException(String msg) {
    super(msg);
  }
}
