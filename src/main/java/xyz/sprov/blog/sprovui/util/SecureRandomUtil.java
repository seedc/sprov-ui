package xyz.sprov.blog.sprovui.util;

import java.security.SecureRandom;

public class SecureRandomUtil {

    private static final char[] LOWER_SEQUENCE = new char[26];
    private static final char[] UPPER_SEQUENCE = new char[26];
    private static final char[] NUMBER_SEQUENCE  = new char[10];

    private static final char[] LETTER_SEQUENCE = new char[52];
    private static final char[] LOW_NUM_SEQUENCE = new char[36];
    private static final char[] UP_NUM_SEQUENCE = new char[36];

    private static final char[] LETTER_NUM_SEQUENCE = new char[62];

    private static final SecureRandom random = new SecureRandom();

    static {
        for (int i = 0; i < 26; ++i) {
            LOWER_SEQUENCE[i] = (char) (i + 'a');
            UPPER_SEQUENCE[i] = (char) (i + 'A');
            LETTER_SEQUENCE[i] = LOWER_SEQUENCE[i];
            LETTER_SEQUENCE[i + 26] = UPPER_SEQUENCE[i];
            LOW_NUM_SEQUENCE[i] = LOWER_SEQUENCE[i];
            UP_NUM_SEQUENCE[i] = UPPER_SEQUENCE[i];

            LETTER_NUM_SEQUENCE[i] = LOWER_SEQUENCE[i];
            LETTER_NUM_SEQUENCE[i + 26] = UPPER_SEQUENCE[i];
            if (i < 10) {
                NUMBER_SEQUENCE[i] = (char) (i + '0');
                LOW_NUM_SEQUENCE[i + 26] = NUMBER_SEQUENCE[i];
                UP_NUM_SEQUENCE[i + 26] = NUMBER_SEQUENCE[i];
                LETTER_NUM_SEQUENCE[i + 52] = NUMBER_SEQUENCE[i];
            }
        }
        shuffle(LOWER_SEQUENCE);
        shuffle(UPPER_SEQUENCE);
        shuffle(NUMBER_SEQUENCE);

        shuffle(LETTER_SEQUENCE);
        shuffle(LOW_NUM_SEQUENCE);
        shuffle(UP_NUM_SEQUENCE);

        shuffle(LETTER_NUM_SEQUENCE);
    }

    private static void shuffle(char[] arr) {
        for (int i = arr.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            char temp = arr[index];
            arr[index] = arr[i];
            arr[i] = temp;
        }
    }

    private static String randomSequence(char[] sequence, int len) {
        StringBuilder builder = new StringBuilder(len);
        int length = sequence.length;
        for (int i = 0; i < len; ++i) {
            builder.append(sequence[random.nextInt(length)]);
        }
        return builder.toString();
    }

    public static String randomLetter(int len) {
        return randomSequence(LETTER_SEQUENCE, len);
    }

    public static String randomLowNum(int len) {
        return randomSequence(LOW_NUM_SEQUENCE, len);
    }

    public static String randomLetterNum(int len) {
        return randomSequence(LETTER_NUM_SEQUENCE, len);
    }

}
