/** */
package probej;

import debug.value.PrimitiveInfo;
import debug.value.ValueInfo;
import util.Name;

import java.util.Optional;

/** @author sugiyama */
class Parser {

  Optional<Location> parseLocation(String locStr) {
    String[] locs = locStr.split(",", 0);
    if (locs.length != 3 && locs.length != 4) {
      return Optional.empty();
    }
    /*if locs.length == 3, methodName is empty*/
    int offset = locs.length - 4;
    String className = Name.toClassNameFromSourcePath(locs[0]);
    String methodName = "";
    if (locs.length == 4) {
      methodName = locs[1];
    }
    int lineNumber = Integer.parseInt(locs[2 + offset]);
    String varName = locs[3 + offset];
    var loc = new Location(className, methodName, lineNumber, varName);
    return Optional.ofNullable(loc);
  }

  Optional<ValueInfo> parseValue(String valueStr) {
    String[] values = valueStr.split(",", 0);
    if (values.length != 7 && values.length != 8) {
      return Optional.empty();
    }
    var value = new PrimitiveInfo(values[0]);
    return Optional.ofNullable(value);
  }
}
