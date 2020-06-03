package com.helena.essorfrench;

import android.content.Context;

public class CharacterUtils {

    public static boolean isChinese(String strName) {
        char[] ch = strName.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (isChinese(c)) {
                return true;
            }
        }
        return false;
    }

    //get right word from meaning, word is not exact when searching without accent
    //we can get right word from first line of meaning
    public static String getRightWordFromChineseMeaning(Context context, String meaning){
        int index = meaning.indexOf(context.getResources().getString(R.string.real_word_token));
        if(index != -1){
            String subString = meaning.substring(0, index);
            String[] strArray = subString.split(System.lineSeparator());
            return strArray[0];
        }
        return null;
    }

    private static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
            return true;
        }
        return false;
    }


    public static String findWordForRightHanded(String str, int offset) {
        //Log.d("MeaningActivity", "is Chinese (offset) = " + isChinese(str.charAt(offset)));
        //Log.d("MeaningActivity", "char = " + str.charAt(offset));
        //Log.d("MeaningActivity", "char(offset) = " + str.substring(offset, offset+10));
        if (str.length() == offset) {
            offset--; // without this code, you will get exception when touching end of the text
        }

        if (str.charAt(offset) == ' ') {
            offset--;
        }

        int startIndex = offset;
        int endIndex = offset;

        try {
            while (str.charAt(startIndex) != ' '
                    && !isChinese(str.charAt(startIndex))
                    && Character.isLetter(str.charAt(startIndex))
                    && str.charAt(startIndex) != '\n') {
                startIndex--;
            }
            startIndex++;
        } catch (StringIndexOutOfBoundsException e) {
            startIndex = 0;
        }
        //Log.d("MeaningActivity", "(startIndex) = " + startIndex);

        char begin = str.charAt(startIndex);
        if (begin == ',' || begin == '.' ||
                begin == '!' || begin == '?' ||
                begin == ':' || begin == ';' ||
                begin == '(' || begin == ')') {
            startIndex++;
        }

        //Log.d("MeaningActivity", "(startIndex) = " + startIndex);

        try {
            while (str.charAt(endIndex) != ' '
                    && !isChinese(str.charAt(endIndex))
                    && (Character.isLetter(str.charAt(endIndex)) || str.charAt(endIndex) == '\'')
                    && str.charAt(endIndex) != '\n') {
                endIndex++;
            }
        } catch (StringIndexOutOfBoundsException e) {
            endIndex = str.length();
        }

        //Log.d("MeaningActivity", "(endIndex) = " + endIndex);

        // without this code, you will get 'here!' instead of 'here'
        // if you use only english, just check whether this is alphabet
        char last = str.charAt(endIndex - 1);
        if (last == ',' || last == '.' ||
                last == '!' || last == '?' ||
                last == ':' || last == ';' ||
                last == '(' || last == ')') {
            endIndex--;
        }

        return str.substring(startIndex, endIndex).trim().toLowerCase();
    }

    private static boolean isChinesePunctuation(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_FORMS
                || ub == Character.UnicodeBlock.VERTICAL_FORMS) {
            return true;
        } else {
            return false;
        }
    }
}
