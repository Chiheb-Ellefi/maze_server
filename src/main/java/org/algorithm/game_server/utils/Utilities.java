package org.algorithm.game_server.utils;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

public class Utilities {
    private static final String DICTIONARY = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static String createSessionID(String firstClient, String secondClient) {
        long timestamp = System.currentTimeMillis();
        String input = firstClient + secondClient + timestamp;

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
        byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));

        BigInteger number = new BigInteger(1, hashBytes);

        return toBase62(number);
    }

    private static String toBase62(BigInteger number) {
        if (number.equals(BigInteger.ZERO)) {
            return String.valueOf(DICTIONARY.charAt(0));
        }
        StringBuilder hash = new StringBuilder();
        BigInteger base = BigInteger.valueOf(62);
        while (number.compareTo(BigInteger.ZERO) > 0) {
            BigInteger[] divRem = number.divideAndRemainder(base);
            int remainder = divRem[1].intValueExact();
            hash.insert(0, DICTIONARY.charAt(remainder));
            number = divRem[0];
        }
        return hash.toString();
    }
}