package utils;

import java.security.SecureRandom;

public class MeetingLinkGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String HEX_CHARS = "0123456789abcdef";

    public static String generateJitsiLink() {
        StringBuilder hex = new StringBuilder(12);
        for (int i = 0; i < 12; i++) {
            hex.append(HEX_CHARS.charAt(RANDOM.nextInt(HEX_CHARS.length())));
        }
        return "https://meet.jit.si/" + hex;
    }
}
