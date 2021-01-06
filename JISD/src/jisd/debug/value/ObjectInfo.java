/** */
package jisd.debug.value;

import com.sun.jdi.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Information of an object value.
 *
 * @author sugiyama
 */
public class ObjectInfo extends ValueInfo {

  /** */
  public ObjectInfo(String name, int stratum, LocalDateTime createdAt, Value jValue) {
    super(name, stratum, createdAt, jValue);
    if (jValue != null) {
      rt = ((ObjectReference) jValue).referenceType();
    }
  }

  public ValueInfo invokeMethod(ThreadReference thread, String name, Object... args) {
    try {
      var values = new ArrayList<Value>();
      Arrays.stream(args)
          .forEach(
              arg -> {
                Value argValue;
                if (arg instanceof String) {
                  argValue = thread.virtualMachine().mirrorOf((String) arg);
                } else if (arg instanceof Integer) {
                  argValue = thread.virtualMachine().mirrorOf((Integer) arg);
                } else {
                  return;
                }
                values.add(argValue);
              });
      var returnValue =
          ((ObjectReference) jValue)
              .invokeMethod(
                  thread,
                  rt.methodsByName(name).get(0),
                  values,
                  ObjectReference.INVOKE_SINGLE_THREADED);
      ValueInfo value =
          ValueInfoFactory.create("return of " + name, 0, returnValue, "", LocalDateTime.now());
      return value;
    } catch (InvalidTypeException e) {
      e.printStackTrace();
    } catch (ClassNotLoadedException e) {
      e.printStackTrace();
    } catch (IncompatibleThreadStateException e) {
      e.printStackTrace();
    } catch (InvocationException e) {
      e.printStackTrace();
    }
    return null;
  }
}
