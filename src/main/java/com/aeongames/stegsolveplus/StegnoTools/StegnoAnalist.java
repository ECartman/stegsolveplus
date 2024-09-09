/*
 *
 * Copyright © 2024 Eduardo Vindas. All rights reserved.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.aeongames.stegsolveplus.StegnoTools;

import com.aeongames.edi.utils.data.Pair;
import com.aeongames.edi.utils.error.LoggingHelper;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * this class is a Holder for the information and access to the stegnography
 * tools that can be applied to an image.
 *
 * TODO: load image from URL should be easy to add. but require changes on the
 * UI to support it.
 *
 * @author Eduardo Vindas
 */
public class StegnoAnalist {

    public static final String ValidImagesFiles[] = ImageIO.getReaderFormatNames();
    /**
     * the source file to read and or check data from.
     */
    private Path File = null;
    private URL ImageAddress = null;
    private CanvasContainer ImageCache;
    private static final Logger loger = LoggingHelper.getLogger(StegnoAnalist.class.getName());

    public StegnoAnalist(Path File) {
        this.File = File;
    }

    public StegnoAnalist(File file) {
        this.File = file.toPath();
    }

    public StegnoAnalist(URL Address) {
        this.ImageAddress = Address;
    }

    public Path getFilePath() {
        return File;
    }

    public String getAnalisisSource() {
        if (File != null) {
            return File.toString();
        } else {
            return ImageAddress.toString();
        }
    }

    public List<Pair<String, BufferedImage>> RunTrasFormations(boolean forced) throws IOException {
        loger.log(Level.INFO, "start RunTrasFormations");
        LoadImage(forced);
        if (ImageCache == null && ImageCache.ImageIsValid()) {
            //notify error.
            //fail to load the image we might throw a error instead? 
            return null;
        }
        loger.log(Level.INFO, "Image has been loaded");
        var list = new ArrayList<Pair<String, BufferedImage>>(20);
        //TODO:notify image is on memory
        loger.log(Level.INFO, "Getting Grey map");
        list.add(new Pair<>("Grey Map", TranformSymetricPixels(Color.BLACK)));
        loger.log(Level.INFO, "Getting Grey Scale Version");
        list.add(new Pair<>("Grey Scale", getGrayScaleCopy()));
        loger.log(Level.INFO, "Getting HSV inverted image");
        getHSVInversions(list);  //-->TODO: make this one faster. 
        list.add(new Pair<>("inverted Hue", HueInversionHSV()));
        loger.log(Level.INFO, "Getting inverted image");
        list.add(new Pair<>("inverted Xor", inversionRGB()));
        loger.log(Level.INFO, "Blue channel");
        list.add(new Pair<>("only Blue Pixels", getBlueImage()));
        loger.log(Level.INFO, "Blue bits");
        getImagePerBitOnBlueChannel(list);
        loger.log(Level.INFO, "Green channel");
        list.add(new Pair<>("only Green Pixels", getImageGreenimage()));
        loger.log(Level.INFO, "Green bits");
        getImagePerBitOnGreenChannel(list);
        loger.log(Level.INFO, "Red channel");
        list.add(new Pair<>("only Red Pixels", getImageRedimage()));
        loger.log(Level.INFO, "Red bits");
        getImagePerBitOnRedChannel(list);
        loger.log(Level.INFO, "Alpha image");
        list.add(new Pair<>("only ALPHA Pixels", getImageAlphaimage()));
        loger.log(Level.INFO, "Alpha bits");
        getImagePerBitOnAlphaChannel(list);
        loger.log(Level.INFO, "done");
        return list;
    }

    /**
     * Create a Buffer Image that contains pixels with the FillColor where the
     * specific pixel data r=g=b. for example. if a pixel color is all 0 (black)
     * or White (all 1) or any color that for all color the data is the same for
     * example. "1f1f1f" and so on. Highlights just the pixels for which r=g=b
     * (this is also known as "gray bits" because all R G B are the same value)
     *
     * @see
     * {@link https://web.stanford.edu/class/cs101/image-6-grayscale-adva.html}
     *
     */
    private BufferedImage TranformSymetricPixels(Color FillColor) {
        return ImageCache.getSymetricPixels(FillColor);
    }

    private BufferedImage getGrayScaleCopy() {
        return ImageCache.getGrayScale();
    }

    public BufferedImage getUnEditedCopy() {
        return ImageCache.getCloneImage();
    }

    private BufferedImage TranformGreyScaleSlow() {
        var transform = ImageCache.createBIemptyCopy(BufferedImage.TYPE_BYTE_GRAY);
        ImageCache.MathOnPixels(transform, RGB -> {
            var rr = Math.pow(RGB[CanvasContainer.RED] / 255.0f, 2.2f);
            var gg = Math.pow(RGB[CanvasContainer.GREEN] / 255.0f, 2.2f);
            var bb = Math.pow(RGB[CanvasContainer.BLUE] / 255.0f, 2.2f);
            // Calculate luminance:
            var lum = 0.2126f * rr + 0.7152f * gg + 0.0722f * bb;

            // Gamma compand and rescale to byte range:
            int grayLevel = (int) (255.0 * Math.pow(lum, 1.0 / 2.2));
            return (grayLevel << 16) + (grayLevel << 8) + grayLevel;
        });
        return transform;
    }

    /**
     * Inverts the color of the image using HSV Rotation. <br>
     * <a href="https://www.niwa.nu/2013/05/math-behind-colorspace-conversions-rgb-hsl/">Source
     * 1</a>
     * <a href="https://geraldbakker.nl/psnumbers/hsb-explained.html">Source
     * 2</a>
     *
     * @return a instance of BufferImage with the inverted HUE colors.
     */
    private void getHSVInversions(List<Pair<String, BufferedImage>> storage) {
        //inverted hue 
        var transform = ImageCache.createBIemptyCopy();
        //iverted hue and Brightness 
        var transform2 = ImageCache.createBIemptyCopy();
        //inverted saturation
        var transform3 = ImageCache.createBIemptyCopy();
        //inverted brightness only.
        var transform4 = ImageCache.createBIemptyCopy();

        ImageCache.MathOnPixels((RGB, PixelPoint) -> {
            float[] HSV = new float[3];
            Color.RGBtoHSB(
                    RGB[CanvasContainer.RED],
                    RGB[CanvasContainer.GREEN],
                    RGB[CanvasContainer.BLUE],
                    HSV);
            var invertedHue = (HSV[0] + 0.5f) % 1f;
            var invertedbright = 1f - HSV[2];
            var InvertedColor = Color.HSBtoRGB(invertedHue, HSV[1], HSV[2]);
            transform.setRGB(PixelPoint.x, PixelPoint.y, InvertedColor);
            InvertedColor = Color.HSBtoRGB(invertedHue, HSV[1], invertedbright);
            transform2.setRGB(PixelPoint.x, PixelPoint.y, InvertedColor);
            InvertedColor = Color.HSBtoRGB(HSV[1], 1f - HSV[1], HSV[2]);
            transform3.setRGB(PixelPoint.x, PixelPoint.y, InvertedColor);
            InvertedColor = Color.HSBtoRGB(HSV[1], HSV[1], invertedbright);
            transform4.setRGB(PixelPoint.x, PixelPoint.y, InvertedColor);
        });
        storage.add(new Pair<>("Inverted Hue", transform));
        storage.add(new Pair<>("Inverted Hue and Brightness", transform2));
        storage.add(new Pair<>("Inverted Saturation", transform3));
        storage.add(new Pair<>("Inverted Brightness", transform4));
    }

    /**
     * Inverts the color of the image using HSV Rotation. <br>
     * <a href="https://www.niwa.nu/2013/05/math-behind-colorspace-conversions-rgb-hsl/">Source
     * 1</a>
     * <a href="https://geraldbakker.nl/psnumbers/hsb-explained.html">Source
     * 2</a>
     *
     * @return a instance of BufferImage with the inverted HUE colors.
     */
    private BufferedImage HueInversionHSV() {
        //inverted hue 
        var transform = ImageCache.createBIemptyCopy();

        ImageCache.MathOnPixels(transform, RGB -> {
            float[] HSV = new float[3];
            Color.RGBtoHSB(
                    RGB[CanvasContainer.RED],
                    RGB[CanvasContainer.GREEN],
                    RGB[CanvasContainer.BLUE],
                    HSV);
            var InvertedColor = Color.HSBtoRGB((HSV[0] + 0.5f) % 1f, HSV[1], HSV[2]);
            return InvertedColor;
        });
        return transform;
    }

    /**
     * Inverts the RGB color of the image.
     *
     * @return a instance of BufferImage with the inverted color data
     */
    private BufferedImage inversionRGB() {
        var transform = ImageCache.createBIemptyCopy();
        ImageCache.MathOnPixelInt(transform, IntRGB -> {
            return IntRGB ^ CanvasContainer.RGBMASK;
        });
        return transform;
    }

    /**
     * creates a new BufferedImage that is only White and a Fill Color.
     *
     * @param FillColor The fill Color to use to fill the pixels that contains
     * data on the desired bit index to inspect. (if null the fill will be
     * black)
     * @param Index the color index of a ARGB(32 bit integer) value to inspect
     * @return a new BufferedImage that is only White and a Fill Color. or null
     * if the Index is invalid.
     * @throws IndexOutOfBoundsException if the index is invalid.
     */
    private BufferedImage transformFromIndexBit(int Index, Color FillColor) {
        if (Index >= 32 || Index < 0) {
            //invalid index color data exist on a 32bit integer
            throw new IndexOutOfBoundsException("the index cannot be less than 0 or larger than 31");
            //return null;
        }
        FillColor = Objects.requireNonNullElse(FillColor, Color.BLACK);
        //which channel?
        if (Index < 8) {//blue channel
            return ImageCache.getBlueForIndex(Index, FillColor);
        }
        if (Index < 16) {//green 
            return ImageCache.getGreenForIndex(Index & 8, FillColor);
        }
        if (Index < 24) {//red
            return ImageCache.getRedForIndex(Index & 8, FillColor);
        }
        return ImageCache.getAlphaForIndex(Index & 8, FillColor);
    }

    private BufferedImage[] getImagePerBitOnBlueChannel(List<Pair<String, BufferedImage>> storage) {
        var BitsImages = new BufferedImage[8];
        for (int index = 0; index < BitsImages.length; index++) {
            BitsImages[index] = ImageCache.getBlueForIndex(index, Color.BLUE);
            storage.add(new Pair<>(String.format("Blue bit Index %d", index), BitsImages[index]));
        }
        return BitsImages;
    }

    private BufferedImage getBlueImage() {
        return ImageCache.getBlueImage();
    }

    private BufferedImage[] getImagePerBitOnGreenChannel(List<Pair<String, BufferedImage>> storage) {
        var BitsImages = new BufferedImage[8];
        for (int index = 0; index < BitsImages.length; index++) {
            BitsImages[index] = ImageCache.getGreenForIndex(index, Color.GREEN);
            storage.add(new Pair<>(String.format("Green bit Index %d", index), BitsImages[index]));
        }
        return BitsImages;
    }

    private BufferedImage getImageGreenimage() {
        return ImageCache.getGreenImage();
    }

    private BufferedImage[] getImagePerBitOnRedChannel(List<Pair<String, BufferedImage>> storage) {
        var BitsImages = new BufferedImage[8];
        for (int index = 0; index < BitsImages.length; index++) {
            BitsImages[index] = ImageCache.getRedForIndex(index, Color.RED);
            storage.add(new Pair<>(String.format("Red bit Index %d", index), BitsImages[index]));
        }
        return BitsImages;
    }

    private BufferedImage getImageRedimage() {
        return ImageCache.getRedImage();
    }

    private BufferedImage[] getImagePerBitOnAlphaChannel(List<Pair<String, BufferedImage>> storage) {
        var BitsImages = new BufferedImage[8];
        for (int index = 0; index < BitsImages.length; index++) {
            BitsImages[index] = ImageCache.getAlphaForIndex(index, Color.BLACK);
            storage.add(new Pair<>(String.format("Alpha bit Index %d", index), BitsImages[index]));
        }
        return BitsImages;
    }

    private BufferedImage getImageAlphaimage() {
        return ImageCache.getAlphaImage();
    }

    private void LoadImage(boolean forced) throws IOException {
        //TODO: we might want to ensure only the supported files were provided
        //and or if not. add code to handle formats that do not match the requirements from 
        //ImageIO. 
        if (ImageCache != null && !forced) {
            return;
        }
        if (File != null) {
            ImageCache = new CanvasContainer(File);
        } else {
            ImageCache = new CanvasContainer(ImageAddress);
        }
    }

}
