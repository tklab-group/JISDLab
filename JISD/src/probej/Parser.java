/** */
package probej;

import debug.Location;
import debug.value.ValueInfo;
import debug.value.ValueInfoFactory;
import util.Name;

import java.time.LocalDateTime;
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

  Optional<ValueInfo> parseValue(String varName, String valueStr) {
    String[] values = valueStr.split(",", 0);
    if (values.length != 7 && values.length != 8) {
      return Optional.empty();
    }
    /*if values.length == 7, methodName is empty*/
    int offset = values.length - 8;
    int year = LocalDateTime.now().getYear();
    int month = Integer.parseInt(values[2 + offset]);
    int dayOfMonth = Integer.parseInt(values[3 + offset]);
    int hour = Integer.parseInt(values[4 + offset]);
    int minute = Integer.parseInt(values[5 + offset]);
    int second = Integer.parseInt(values[6 + offset]);
    int nanoOfSecond = Integer.parseInt(values[7 + offset]) * 1000000;
    var createdAt = LocalDateTime.of(year, month, dayOfMonth, hour, minute, second, nanoOfSecond);
    var value = ValueInfoFactory.create(varName, 0, null, values[0], createdAt);
    return Optional.ofNullable(value);
  }
}
