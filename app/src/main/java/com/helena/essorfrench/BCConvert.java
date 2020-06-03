package com.helena.essorfrench;

/**
 *
 * convert strings from halfwidth to fullwidth or vice versa
 *
 */
public class BCConvert {

    //visible halfwidth char in ASCII table begins with  '!'
    static final char DBC_CHAR_START = 33;
    //visible halfwidth char in ASCII table ends with  '~'
    static final char DBC_CHAR_END = 126;
    //visible fullwidth char in ASCII table begins with  '！'
    static final char SBC_CHAR_START = 65281;
    //visible fullwidth char in ASCII table begins with  '～'
    static final char SBC_CHAR_END = 65374;
    //offset between halfwidth and fullwidth except space symbol
    static final int CONVERT_STEP = 65248;
    //space symbol for fullwidth
    static final char SBC_SPACE = 12288;
    //space symbol for halfwidth
    static final char DBC_SPACE = ' ';
    //peroid symbol for fullwidth
    static final char SBC_PERIED = '。';
    //peroid symbol for halfwidth
    static final char DBC_PERIED = '.';

    /**
     *
     * halfwidth to fullwidth
     * only handle space, chars between DBC_CHAR_START and DBC_CHAR_END
     *
     */
    public static String bj2qj(String src) {
        if (src == null) {
            return null;
        }

        StringBuilder buf = new StringBuilder(src.length());
        char[] ca = src.toCharArray();
        for (int i = 0; i < ca.length; i++) {
            if (ca[i] == DBC_SPACE) {
                buf.append(SBC_SPACE);
            } else if ((ca[i] >= DBC_CHAR_START) && (ca[i] <= DBC_CHAR_END)) {
                buf.append((char) (ca[i] + CONVERT_STEP));
            } else {
                buf.append(ca[i]);
            }
        }
        return buf.toString();
    }

    /**
     *
     * fullwidth to halfwidth
     * only handle space, chars between SBC_CHAR_START and SBC_CHAR_END
     *
     */
    public static String qj2bj(String src) {
        if (src == null) {
            return null;
        }
        StringBuilder buf = new StringBuilder(src.length());
        char[] ca = src.toCharArray();
        for (int i = 0; i < src.length(); i++) {
            if (ca[i] >= SBC_CHAR_START && ca[i] <= SBC_CHAR_END) {
                buf.append((char) (ca[i] - CONVERT_STEP));
            } else if (ca[i] == SBC_SPACE) {
                buf.append(DBC_SPACE);
            } else if(ca[i] == SBC_PERIED){
                buf.append(DBC_PERIED);
            } else {
                buf.append(ca[i]);
            }
        }
        return buf.toString();
    }
}