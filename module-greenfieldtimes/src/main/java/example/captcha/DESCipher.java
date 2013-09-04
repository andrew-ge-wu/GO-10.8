package example.captcha;

import java.security.InvalidKeyException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.polopoly.util.Base64;

/**
 * This class provides the use of DES algorithm.
 */
public class DESCipher implements example.captcha.Cipher {

    private Cipher encrypter;

    private Cipher decrypter;

    /**
     * Create a DES cipher. The key string (Base64 encoded) must be of 56 bit
     * length . Can be generated using the static method
     * {@link #generateSecretKey()}.
     * 
     * @param secretKeyString
     *            the 56 bit Base64 encoded secret key
     * @throws InvalidKeyException
     *             If the key is invalid (invalid encoding, wrong length,
     *             uninitialized, etc).
     */
    public DESCipher(String secretKeyString) throws CipherException {
        byte[] secretKeyBytes = Base64.decode(secretKeyString);
        SecretKey secretKey = new SecretKeySpec(secretKeyBytes, "DES");
        try {
            encrypter = Cipher.getInstance("DES");
            encrypter.init(Cipher.ENCRYPT_MODE, secretKey);
            decrypter = Cipher.getInstance("DES");
            decrypter.init(Cipher.DECRYPT_MODE, secretKey);
        } catch (Exception e) {
            throw new CipherException("Failed to initiate DES cipher", e);
        }
    }

    /**
     * Obtain the Secret key as a String. It is useful you can store the String
     * in a content for all kinds of encryption and decryption.
     * 
     * @return the SecretKey as String
     */
    public static String generateSecretKey() throws CipherException {

        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("DES");
            SecretKey secretKey = keyGen.generateKey();
            byte[] secretKeyBytes = secretKey.getEncoded();
            return Base64.encodeBytes(secretKeyBytes);
        } catch (Exception e) {
            throw new CipherException("Unable to generate secret key", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see example.captcha.Cipher#encrypt(java.lang.String)
     */
    public String encrypt(String contents) throws CipherException {

        try {
            // Encode the string into bytes
            byte[] contentBytes = contents.getBytes("UTF8");

            // Encrypt
            byte[] encryptedBytes = encrypter.doFinal(contentBytes);

            String base64 = Base64.encodeBytes(encryptedBytes);
            
            return base64;
            
        } catch (Exception e) {
            throw new CipherException("Failed to encrypt content", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see example.captcha.Cipher#decrypt(java.lang.String)
     */
    public String decrypt(String contents) throws CipherException {

        try {
            String base64 = padIfNecessary(contents);
            
            // Decode base64 to get bytes
            byte[] contentBytes = Base64.decode(base64);

            // Decrypt
            byte[] decryptedBytes = decrypter.doFinal(contentBytes);

            // Convert bytes to string
            return new String(decryptedBytes, "UTF8");
        } catch (Exception e) {
            throw new CipherException("Failed to decrypt content", e);
        }
    }
    
    /**
     * Add = to end of stripped base64 to make valid base64.
     */
    private String padIfNecessary(String base64String)
    {
        // Tomcat (some versions) can't deal with '=' in cookies (and will have
        // removed then), so we restore if needed.
        if (base64String == null || base64String.length() % 4 == 0) {
            return base64String;
        }
        
        // Need to add padding
        char[] padded = new char[base64String.length() + 4 - base64String.length() % 4];
        char[] original = base64String.toCharArray();
        System.arraycopy(original, 0, padded, 0, original.length);
        for (int i = base64String.length(); i < padded.length; i++) {
            padded[i] = '=';
        }

        return new String(padded);
    }
}