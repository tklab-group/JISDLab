package jisd.util;

public final class Number {
    private Number() {}

    public static boolean isNumeric(String strNum) {
        return strNum.matches("-?\\d+(\\.\\d+)?");
    }
}
