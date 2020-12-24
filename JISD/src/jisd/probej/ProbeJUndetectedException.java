package jisd.probej;

/**
 * Thrown to indicate a connection with ProbeJ went wrong.
 *
 * @author sugiyama
 */
public class ProbeJUndetectedException extends RuntimeException {
  public ProbeJUndetectedException(String msg) {
    super(msg);
  }
}
