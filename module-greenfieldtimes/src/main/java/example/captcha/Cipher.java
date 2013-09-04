package example.captcha;

/**
 * Interface to for symmetric cryptography.
 */
public interface Cipher {

    /**
     * Encrypt a String.
     * 
     * @param contents
     *            to encrypt
     * @return a String encrypted contents
     * @throws CipherException
     *             if failed to encrypted contents
     */
    public String encrypt(String contents) throws CipherException;

    /**
     * Decrypt a String.
     * 
     * @param contents
     *            to decrypt
     * @return a String encrypted contents
     * @throws CipherException
     *             if failed to decrypted contents
     */
    public String decrypt(String contents) throws CipherException;

}
