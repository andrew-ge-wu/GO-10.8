package example.captcha;

import java.awt.image.BufferedImage;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.Random;

import com.octo.captcha.CaptchaException;
import com.octo.captcha.component.image.wordtoimage.WordToImage;
import com.octo.captcha.component.word.wordgenerator.WordGenerator;

public abstract class CookieImageCaptchaFactory implements ClusteredImageCaptchaFactory {

    private final Random random = new SecureRandom();

    private Cipher cipher;

    /*
     * (non-Javadoc)
     * @see example.captcha.ClusteredImageCaptchaFactory#getImageCaptcha()
     */
    public CookieImageCaptcha getImageCaptcha() throws CaptchaException {

        if (getWordGenerator() == null) {
            throw new CaptchaException("WordGenerator has not been initialized");
        }
        
        if (getWordToImage() == null) {
            throw new CaptchaException("WordToImage has not been initialized");
        }
        
        if (cipher == null) {
            throw new CaptchaException("Cipher has not been initialized");
        }
        
        Integer wordLength = getRandomLength();
        String word = getWordGenerator().getWord(wordLength,
                Locale.getDefault());

        BufferedImage image = null;
        try {
            image = getWordToImage().getImage(word);
        } catch (Throwable e) {
            throw new CaptchaException(e);
        }

        CookieImageCaptcha captcha = new CookieImageCaptcha(image, word, cipher);
        return captcha;
    }

    public abstract WordToImage getWordToImage();

    public abstract WordGenerator getWordGenerator();
    
    public void setCipher(Cipher cipher) {
        this.cipher = cipher;
    }

    protected Integer getRandomLength() {

        int range = getWordToImage().getMaxAcceptedWordLength()
                - getWordToImage().getMinAcceptedWordLength();
        int randomRange = range != 0 ? random.nextInt(range + 1) : 0;
        return new Integer(randomRange
                + getWordToImage().getMinAcceptedWordLength());
    }

}
