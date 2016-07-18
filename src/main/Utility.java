package main;

/**
 * Created by Anders on 08.07.16.
 */
public class Utility {
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String bytesToHex(byte myByte) {
        char hexChars[] = new char[2];

        int v = myByte & 0xFF;
        hexChars[0] = hexArray[v >>> 4];
        hexChars[1] = hexArray[v & 0x0F];

        return new String(hexChars);
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));

        }
        return data;
    }

    /**
     * Helps to know what bit is at some position in some number
     *
     * @param number      in which we search a bit
     * @param bitPosition at which we want to know a bit
     * @return bit that is on bitPosition at number
     * @throws IllegalArgumentException if bitPosition is negative
     */
    public static boolean bitAt(int number, int bitPosition) throws IllegalArgumentException {
        if (bitPosition >= 0) {
            return ((number >> bitPosition) & 1) == 1 ? true : false;
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Creates an integer from subset of integer-to-boolean-array boolean array treating it as binary number
     *
     * @param n    the number from which we'll generate new number
     * @param from bit position from which we'll start to calculate
     * @param till bit position on which we'll end to calculate (included)
     * @return
     */
    public static int intFromIntegerSubset(int n, int from, int till) throws IllegalArgumentException {
        int result = 0;
        int i = 0;

        if (from <= till) {
            while (from + i <= till) {
                result += (bitAt(n, from + i) ? 1 << i : 0);
                i++;
            }
            return result;
        } else {
            throw new IllegalArgumentException();
        }
    }
}
