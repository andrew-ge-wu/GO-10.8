package example.captcha;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.ImageFilter;

import com.jhlabs.image.WaterFilter;
import com.octo.captcha.component.image.backgroundgenerator.BackgroundGenerator;
import com.octo.captcha.component.image.backgroundgenerator.GradientBackgroundGenerator;
import com.octo.captcha.component.image.color.SingleColorGenerator;
import com.octo.captcha.component.image.deformation.ImageDeformation;
import com.octo.captcha.component.image.deformation.ImageDeformationByFilters;
import com.octo.captcha.component.image.fontgenerator.FontGenerator;
import com.octo.captcha.component.image.fontgenerator.RandomFontGenerator;
import com.octo.captcha.component.image.textpaster.DecoratedRandomTextPaster;
import com.octo.captcha.component.image.textpaster.TextPaster;
import com.octo.captcha.component.image.textpaster.textdecorator.BaffleTextDecorator;
import com.octo.captcha.component.image.textpaster.textdecorator.TextDecorator;
import com.octo.captcha.component.image.wordtoimage.DeformedComposedWordToImage;
import com.octo.captcha.component.image.wordtoimage.WordToImage;
import com.octo.captcha.component.word.wordgenerator.RandomWordGenerator;
import com.octo.captcha.component.word.wordgenerator.WordGenerator;

/**
 * Default Greenfield Times image captcha engine.
 * <p>
 * A CaptchaEngine is the main interface from an application point of view to
 * return new captchas (in this particular case image captchas).
 * </p>
 * <p>
 * The Greenfield Times image captcha engine generates images of the following
 * captcha characteristics:
 * </p>
 * <ul>
 * <li>100x60 pixel image with white background.
 * <li>Contains 6 random (a-z) lower cased characters (19x19 pixel, Arial plain
 * black fonts).
 * <li>Characters are obfuscated and printed on a waved random text path.
 * </ul>
 */
public class DefaultCaptchaFactory extends CookieImageCaptchaFactory
{
    private final WordToImage wordToImage;

    private final WordGenerator wordGenerator;

    public DefaultCaptchaFactory() {

        WaterFilter water = new WaterFilter();

        water.setAmplitude(1d);
        water.setAntialias(true);
        water.setPhase(15d);
        water.setWavelength(80d);

        ImageDeformation backDef =
            new ImageDeformationByFilters(new ImageFilter[]{});
        ImageDeformation textDef =
            new ImageDeformationByFilters(new ImageFilter[]{});
        ImageDeformation postDef =
            new ImageDeformationByFilters(new ImageFilter[]{water});

        /*
         * Set Captcha Word Length Limitation
         */
        Integer minAcceptedWordLength = new Integer(6);
        Integer maxAcceptedWordLength = new Integer(6);

        /*
         * Set Captcha Image Size: Height and Width
         */
        Integer imageWidth = new Integer(100);
        Integer imageHeight = new Integer(60);

        /*
         * Set Captcha Font Size
         */
        Integer minFontSize = new Integer(19);
        Integer maxFontSize = new Integer(19);

        /*
         * The digits to generate captcha of
         */
        this.wordGenerator =
            (new RandomWordGenerator("abcdefghijklmnopqrstuvwxyz"));

        /*
         * Background generator
         */
        BackgroundGenerator backgroundGenerator =
            new GradientBackgroundGenerator(imageWidth, imageHeight,
                                            Color.white, Color.white);

        Font[] fonts = new Font[] { new Font("Arial", Font.PLAIN, 19) };

        /*
         * Fonts
         */
        FontGenerator fontGenerator = new RandomFontGenerator(minFontSize,
                                                              maxFontSize,
                                                              fonts);

        /*
         * Color
         */
        SingleColorGenerator scg = new SingleColorGenerator(Color.black);

        /*
         * Decorators
         */
        BaffleTextDecorator baffleDecorator =
            new BaffleTextDecorator(new Integer(1), Color.white);

        TextDecorator[] textDecorators = new TextDecorator[1];
        textDecorators[0] = baffleDecorator;

        TextPaster textPaster =
            new DecoratedRandomTextPaster(minAcceptedWordLength,
                                          maxAcceptedWordLength,
                                          scg,
                                          textDecorators);

        /*
         * Image generation
         */
        this.wordToImage =
            new DeformedComposedWordToImage(fontGenerator, backgroundGenerator,
                                            textPaster, backDef, textDef, postDef);
    }

    @Override
    public WordGenerator getWordGenerator() {
        return wordGenerator;
    }

    @Override
    public WordToImage getWordToImage() {
        return wordToImage;
    }
}