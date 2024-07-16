package std.ffi;

import java.util.Arrays;

public class array_help {
    public static String[] null_str_array() {
        return null;
    }

    public static String byteArrayToStr(byte[] arr) {
        return Arrays.toString(arr);
    }
}
