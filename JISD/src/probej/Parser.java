/**
 * 
 */
package probej;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import debug.value.PrimitiveInfo;
import debug.value.ValueInfo;
import util.SrcUtil;

/**
 * @author sugiyama
 *
 */
class Parser {
  
  Optional<Location> parseLocation(String locStr) {
    String[] locs = locStr.split(",", 0);
    if (locs.length != 4) {
      return Optional.empty();
    }
    String className  = SrcUtil.toClassNameFromSourcePath(locs[0]);
    String methodName = locs[1];
    int lineNumber    = Integer.parseInt(locs[2]);
    String varName    = locs[3];
    var loc = new Location(className, methodName, lineNumber, varName);
    return Optional.ofNullable(loc);
  }
  
  Optional<ValueInfo> parseValue(String valueStr) {
    String[] values = valueStr.split(",", 0);
    if (values.length != 8) {
      return Optional.empty();
    }
    var value = new PrimitiveInfo(values[0]);
    return Optional.ofNullable(value); 
  }
}
