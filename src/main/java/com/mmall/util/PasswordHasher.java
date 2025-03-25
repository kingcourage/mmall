package com.mmall.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;

/**
 * Secure password hashing utility using PBKDF2 with HMAC-SHA256
 */
public class PasswordHasher {
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Generates a secure hash from a password with a random salt
     *
     * @param password The password to hash
     * @return A string containing the salt and hash, Base64 encoded
     */
    public static String hashPassword(String password) {
        byte[] salt = generateSalt();
        byte[] hash = hashPassword(password.toCharArray(), salt);
        
        // Format: base64(salt) + ":" + base64(hash)
        return Base64.getEncoder().encodeToString(salt) + ":" + 
               Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Verifies a password against a stored hash
     *
     * @param password The password to verify
     * @param storedHash The stored hash to verify against
     * @return true if the password matches, false otherwise
     */
    public static boolean verifyPassword(String password, String storedHash) {
        try {
            String[] parts = storedHash.split(":");
            if (parts.length != 2) return false;

            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] hash = Base64.getDecoder().decode(parts[1]);

            byte[] testHash = hashPassword(password.toCharArray(), salt);
            return Arrays.equals(hash, testHash);
        } catch (IllegalArgumentException e) {
            return false; // Handle invalid Base64 encoding
        }
    }

    /**
     * Checks if a stored hash is in legacy MD5 format
     *
     * @param storedHash The hash to check
     * @return true if the hash is in legacy format
     */
    public static boolean isLegacyHash(String storedHash) {
        return !storedHash.contains(":") && storedHash.length() == 32;
    }

    private static byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        SECURE_RANDOM.nextBytes(salt);
        return salt;
    }

    private static byte[] hashPassword(char[] password, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error while hashing password", e);
        }
    }
}