package example.membership;

import java.security.SecureRandom;

public class PasswordService {

    private static final int DEFAULT_PASSWORD_LENGTH = 8;

    private static final String DEFAULT_ALLOWED_PASSWORD_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";

    private final int length;

    private final String allowedChars;

    public PasswordService() {
        this(DEFAULT_PASSWORD_LENGTH, DEFAULT_ALLOWED_PASSWORD_CHARS);
    }
    
    public PasswordService(int length, String allowedChars) {
        this.length = length;
        this.allowedChars = allowedChars;
    }
    
    /**
     * Generates a random String that can be used as a password.
     * 
     * @return a random String of length 8 using only alphanumeric characters.
     */
    public String generatePassword() {
        final SecureRandom random = new SecureRandom();
        final StringBuilder salt = new StringBuilder();
        for (int i = 0; i < length; i++) {
            salt.append(allowedChars.charAt(random
                    .nextInt(allowedChars.length())));
        }
        return salt.toString();
    }
}
