package ru.retype.utils;

import ru.retype.script.Scripting;

import java.util.Random;

public class NameUtils {

    private static final Random random = new Random();
    private static char[] dictionary = "1234567890qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM".toCharArray();

    public static String generateString(int id) {
        String s = "iIl";
        return convertToBase(id, s);
    }

    public static String convertToBase(int i, String str) {
        StringBuilder sb = new StringBuilder();

        do {
            sb.append(str.charAt(i % str.length()));
            i /= str.length();
        } while (i != 0);

        return sb.toString();
    }

    public static String getRandomString(int len){
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < len; i++){
            stringBuilder.append(dictionary[random.nextInt(dictionary.length)]);
        }
        return stringBuilder.toString();
    }

}
