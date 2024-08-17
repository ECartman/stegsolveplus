/* 
 *  Copyright © 2024 Eduardo Vindas. All rights reserved.
 *  
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 * 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aeongames.stegsolveplus.StegnoTools;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javax.imageio.ImageIO;

/**
 *
 * @author Eduardo
 */
public class ImageContainer {

    /**
     * the max value for a single color Channel
     */
    static final int MAXSINGLEVALUE = 0xFF;
    /**
     * a hex mask to gather the Alpha level.
     */
    static final int ALPHA_MASK = 0xFF000000;
    /**
     * a mask to gather the Red Channel data.
     */
    static final int RED_MASK = 0x00FF0000;
    /**
     * a mask to gather the Green Channel data.
     */
    static final int GREEN_MASK = 0x0000FF00;
    /**
     * a mask to gather the blue Channel data.
     */
    static final int BLUE_MASK = 0x000000FF;
    /**
     * a mask to gather the RGB composed value.
     */
    static final int RGBMASK = RED_MASK | GREEN_MASK | BLUE_MASK;

    /**
     * Constants to read from the RGBA data
     */
    static final int ALPHA = 0,
            RED = 1,
            GREEN = 2,
            BLUE = 3;
    /**
     * The Loaded image. initially this will be null
     */
    private final BufferedImage originalImage;
    /**
     * a map that will contain the RGB data for the image when loaded.
     * initially this was made so colors could be handled separately and no 
     * calculation were needed to separate or extract a channel. 
     * 
     * TODO: however it seems that most of the calculations are OK using 
     * all channels. and thus this might not be as good on performance. 
     * need to profile it. 
     * 
     */
    private List<Short[]> RGBA_Data;

    /**
     * Package Private constructor. creates a new instance of ImageContainer
     * loading the Image from the Source.
     *
     * @param Source the Path where the file is stored. cannot be null
     * @throws IOException if the file fails to load.
     */
    ImageContainer(Path Source) throws IOException {
        Objects.requireNonNull(Source, "the path is null");
        originalImage = ImageIO.read(Source.toFile());
    }
    
    public boolean ImageIsValid(){
        return !Objects.isNull(originalImage);
    }

    /**
     * Package Private constructor. creates a new instance of ImageContainer
     * loading the Image from the Source.
     *
     * @param Source the URL where the file is stored. cannot be null
     */
    ImageContainer(URL Source) throws IOException {
        Objects.requireNonNull(Source, "the path is null");
        originalImage = ImageIO.read(Source);
    }

    /**
     * Package Private constructor. creates a new instance of ImageContainer
     * loading the Image from the Source.
     *
     * @param ImageStream the Stream from which to read the image.
     */
    ImageContainer(InputStream ImageStream) throws IOException {
        Objects.requireNonNull(ImageStream, "the path is null");
        originalImage = ImageIO.read(ImageStream);
    }

    /**
     * load the ARGB data from the image per pixels and stores into the cache
     * (RGBA_Data)
     */
    public void LoadRGBData() {
        if (Objects.isNull(originalImage)) {
            return;
        }
        var totalpixels = getTotalPixels();
        RGBA_Data = new ArrayList<>(4);
        Short A[] = new Short[totalpixels],
                R[] = new Short[totalpixels],
                G[] = new Short[totalpixels],
                B[] = new Short[totalpixels];
        for (int x = 0,arrayindex=0; x < originalImage.getWidth(); x++) {
            for (int y = 0; y < originalImage.getHeight(); y++,arrayindex++) {
                var color = originalImage.getRGB(x, y);
                A[arrayindex] = (short)((color & ALPHA_MASK)>>> 24);
                R[arrayindex] = (short)((color & RED_MASK)  >>> 16);
                G[arrayindex] = (short)((color & GREEN_MASK)>>> 8);
                B[arrayindex] = (short)((color) & BLUE_MASK);
            }
        }
        RGBA_Data.add(ALPHA, A);
        RGBA_Data.add(RED, R);
        RGBA_Data.add(GREEN, G);
        RGBA_Data.add(BLUE, B);
    }

    /**
     * converts the provided byte from its signed value into a unsigned value
     * unfortunately to do so it has to use a variable that requires way more
     * memory.
     *
     * @param value the byte to convert into the unsigned representation of the
     * byte
     * @return a integer that represent the unsigned value of the provided byte
     */
    public static int convertToNumeric(byte value) {
        //return ((value) & MAXSINGLEVALUE);
        return Byte.toUnsignedInt(value);
    }

    /**
     * creates a new BufferedImage that support ARGB (rgb+alpha)
     *wit the same dimensions as the original image. 
     * @return a new instance of BufferedImage that support ARGB (rgb+alpha)
     */
    public BufferedImage createBIemptyCopy() {
        return createBIemptyCopy(BufferedImage.TYPE_INT_ARGB);
    }
    
    /**
     * creates a new BufferedImage that support RGB (rgb NOT ALPHA)
     *wit the same dimensions as the original image. 
     * @return a new instance of BufferedImage that support ARGB (rgb+alpha)
     */
    public BufferedImage createBINoAlphaemptyCopy() {
        return createBIemptyCopy(BufferedImage.TYPE_INT_RGB);
    }

    /**
     * creates a new BufferedImage for the provided type (i.e: RGB,
     * TYPE_BYTE_GRAY (gray scale))
     *
     * @param type the type to use.
     * @return a new instance of BufferedImage
     * @see ColorSpace
     * @see #TYPE_INT_RGB
     * @see #TYPE_INT_ARGB
     * @see #TYPE_INT_ARGB_PRE
     * @see #TYPE_INT_BGR
     * @see #TYPE_3BYTE_BGR
     * @see #TYPE_4BYTE_ABGR
     * @see #TYPE_4BYTE_ABGR_PRE
     * @see #TYPE_BYTE_GRAY
     * @see #TYPE_USHORT_GRAY
     * @see #TYPE_BYTE_BINARY
     * @see #TYPE_BYTE_INDEXED
     * @see #TYPE_USHORT_565_RGB
     * @see #TYPE_USHORT_555_RGB
     */
    public BufferedImage createBIemptyCopy(int type) {
        return new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), type);
    }

    /**
     * Returns the width of the {@code BufferedImage}.
     *
     * @return the width of this {@code BufferedImage}
     */
    public int getWidth() {
        return originalImage.getWidth();
    }

    /**
     * Returns the height of the {@code BufferedImage}.
     *
     * @return the height of this {@code BufferedImage}
     */
    public int getHeight() {
        return originalImage.getHeight();
    }

     public int getTotalPixels() {
        return originalImage.getWidth() * originalImage.getHeight();
    }

    public void getSymetricPixels(BufferedImage image, Color Fill) {
        //boolean[] pixelmap = new boolean[];
        var totalpixels=getTotalPixels();
        for (int i = 0; i < totalpixels; i++) {
            var symetric = Objects.equals(RGBA_Data.get(RED)[i], RGBA_Data.get(GREEN)[i])
                    && Objects.equals(RGBA_Data.get(BLUE)[i], RGBA_Data.get(GREEN)[i]);
            var x = i / image.getHeight();
            var y = i % image.getHeight();
            image.setRGB(x, y,
                    symetric ? Fill.getRGB()
                            : Color.WHITE.getRGB());
        }
    }

    public void MathOnPixels(BufferedImage image, Function<Short[], Integer> MathFunction) {
        for (int i = 0; i < getTotalPixels(); i++) {
            var CalculatedPixel = MathFunction.apply(ToWrapperArray(getRGB(i)));
            var x = i / image.getHeight();
            var y = i % image.getHeight();
            image.setRGB(x, y, CalculatedPixel);
        }
    }
    
    public void MathOnPixels(BiConsumer<Short[],Point>MathConsumer){
        for (int i = 0; i < getTotalPixels(); i++) {
            var x = i / originalImage.getHeight();
            var y = i % originalImage.getHeight();
            MathConsumer.accept(ToWrapperArray(getRGB(i)),new Point(x, y));
        }
    }

    public void MathOnPixelInt(BufferedImage Destination, Function<Integer, Integer> MathFunction) {
        for (int x = 0; x < originalImage.getWidth(); x++) {
            for (int y = 0; y < originalImage.getHeight(); y++) {
                var CalculatedPixel = MathFunction.apply(originalImage.getRGB(x, y));
                Destination.setRGB(x,y, CalculatedPixel);
            }
        }
    }

    public short[] getRGB(int x, int y) {
        short b = RGBA_Data.get(BLUE)[x * originalImage.getWidth() + y];
        short g = RGBA_Data.get(GREEN)[x * originalImage.getWidth() + y];
        short r = RGBA_Data.get(RED)[x * originalImage.getWidth() + y];
        short a = RGBA_Data.get(ALPHA)[x * originalImage.getWidth() + y];
        return new short[]{a, r, g, b};
    }

    public short[] getRGB(int LinearPosition) {
        if (LinearPosition < 0 && LinearPosition > getTotalPixels()) {
            throw new ArrayIndexOutOfBoundsException("the index Specified is not present on the image");
        }
        short b = RGBA_Data.get(BLUE)[LinearPosition];
        short g = RGBA_Data.get(GREEN)[LinearPosition];
        short r = RGBA_Data.get(RED)[LinearPosition];
        short a = RGBA_Data.get(ALPHA)[LinearPosition];
        return new short[]{a, r, g, b};
    }

    private Short[] ToWrapperArray(short[] b) {
        var result = new Short[b.length];
        Arrays.parallelSetAll(result, i -> b[i]);
        return result;
    }

    public short getAlpha(int x, int y) {
        return RGBA_Data.get(ALPHA)[x * originalImage.getWidth() + y];
    }

    public short getRed(int x, int y) {
        return RGBA_Data.get(RED)[x * originalImage.getWidth() + y];
    }

    public short getGreen(int x, int y) {
        return RGBA_Data.get(GREEN)[x * originalImage.getWidth() + y];
    }

    public short getBlue(int x, int y) {
        return RGBA_Data.get(BLUE)[x * originalImage.getWidth() + y];
    }

    BufferedImage getOriginal() {
        //TODO: we might want to return a Clone not the actual OG.
        return originalImage;
    }

    BufferedImage getBlueForIndex(int Index, Color FillColor) {
        return getColorForIndex(Index, BLUE, FillColor);
    }

    BufferedImage getAlphaForIndex(int Index, Color FillColor) {
        return getColorForIndex(Index, ALPHA, FillColor);
    }

    BufferedImage getGreenForIndex(int Index, Color FillColor) {
        return getColorForIndex(Index, GREEN, FillColor);
    }

    BufferedImage getRedForIndex(int Index, Color FillColor) {
        return getColorForIndex(Index, RED, FillColor);
    }

    BufferedImage getColorForIndex(int Index, int Channel, Color FillColor) {
        if (Channel < 0 || Channel > BLUE) {
            throw new ArrayIndexOutOfBoundsException("Invalid Channel");
        }
        if (Index < 0 || Index >= 8) {
            throw new ArrayIndexOutOfBoundsException("the index Specified is not present on the image");
        }
        var image = createBINoAlphaemptyCopy();//note We could just return a binary image
        for (int i = 0; i < getTotalPixels(); i++) {
            var CalculatedPixel = ((RGBA_Data.get(Channel)[i] >>> Index) & 1) == 0 ? RGBMASK : FillColor.getRGB();
            var x = i / image.getHeight();
            var y = i % image.getHeight();
            image.setRGB(x, y, CalculatedPixel);
        }
        return image;
    }

    BufferedImage getBlueImage() {
        return getImageForChannel(BLUE);
    }

    BufferedImage getAlphaImage() {
        return getImageForChannel(ALPHA);
    }

    BufferedImage getGreenImage() {
        return getImageForChannel(GREEN);
    }

    BufferedImage getRedImage() {
        return getImageForChannel(RED);
    }

    BufferedImage getImageForChannel(int Channel) {
        var image = createBINoAlphaemptyCopy();//note We could just return a binary image
        int position;
        switch (Channel) {
            case RED:
                position = 16;
                break;
            case GREEN:
                position = 8;
                break;
            case BLUE:                
            default:
                position = 0;
                break;
        }
        //NOTE: Alpha channel will become visible on the "blue" channel
        //if we need to recrete the data. we will neeed a diferent function. 
        for (int i = 0; i < getTotalPixels(); i++) {
            int CalculatedPixel = RGBA_Data.get(Channel)[i]<< position;
            var x = i / image.getHeight();
            var y = i % image.getHeight();
            image.setRGB(x, y, CalculatedPixel);
        }
        return image;
    }

}
