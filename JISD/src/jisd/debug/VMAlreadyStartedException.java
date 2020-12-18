package jisd.debug;

public class VMAlreadyStartedException extends RuntimeException {
  public VMAlreadyStartedException(String msg) {
    super(msg);
  }
}
