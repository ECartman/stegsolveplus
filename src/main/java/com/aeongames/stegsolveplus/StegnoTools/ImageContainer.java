/* 
 *  Copyright © 2024 Eduardo Vindas Cordoba. All rights reserved.
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
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
     * a map that will contain the RGB data for the image when loaded. note that
     * we store the color data as binary not as decimal thus 2's compliment is
     * part of the value not a sign (aka this is a unsigned value on a singed
     * store this is done this way for storage efficiency.
     */
    private List<Byte[]> RGBA_Data;

    //TODO: User of remove
    private Map<Integer, BufferedImage> TransformedImages;

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
    private void LoadRGBData() {
        if (Objects.isNull(originalImage)) {
            return;
        }
        var totalpixels = getTotalPixels();
        RGBA_Data = new ArrayList<>(4);
        Byte A[] = new Byte[totalpixels],
                R[] = new Byte[totalpixels],
                G[] = new Byte[totalpixels],
                B[] = new Byte[totalpixels];
        for (int Line = 0; Line < originalImage.getWidth(); Line++) {
            for (int Column = 0; Column < originalImage.getHeight(); Column++) {
                var bytes = getEachColorValueFrom(originalImage.getRGB(Line, Column));
                A[Column * originalImage.getWidth() + Line] = bytes[ALPHA];
                R[Column * originalImage.getWidth() + Line] = bytes[RED];
                G[Column * originalImage.getWidth() + Line] = bytes[GREEN];
                B[Column * originalImage.getWidth() + Line] = bytes[BLUE];
            }
        }
        RGBA_Data.add(ALPHA, A);
        RGBA_Data.add(RED, R);
        RGBA_Data.add(GREEN, G);
        RGBA_Data.add(BLUE, B);
    }

    /**
     * Transform the Integer value that represent a ARBG pixel or color data and
     * convert it into an Byte array that contains this information in the
     * following order:
     * <br>index 0 alpha channel data
     * <br>index 1 red channel data
     * <br>index 2 green channel data
     * <br>index 3 blue channel data
     *
     * @param ARGB
     * @return a byte array with the BINARY value that represent the color
     * (please note this is not a Numeric value. is a unsigned value or binary)
     */
    private byte[] getEachColorValueFrom(int ARGB) {
        byte b = (byte) ((ARGB) & MAXSINGLEVALUE);
        byte g = (byte) ((ARGB >>> 8) & MAXSINGLEVALUE);
        byte r = (byte) ((ARGB >>> 16) & MAXSINGLEVALUE);
        byte a = (byte) ((ARGB >>> 24) & MAXSINGLEVALUE);
        return new byte[]{a, r, g, b};
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
     *
     * @return a new instance of BufferedImage that support ARGB (rgb+alpha)
     */
    public BufferedImage createBIemptyCopy() {
        return createBIemptyCopy(BufferedImage.TYPE_INT_ARGB);
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

    public boolean[] getSymetricPixels(BufferedImage image, Color Fill) {
        boolean[] pixelmap = new boolean[getTotalPixels()];
        for (int i = 0; i < pixelmap.length; i++) {
            pixelmap[i] = Objects.equals(RGBA_Data.get(RED)[i], RGBA_Data.get(GREEN)[i])
                    && Objects.equals(RGBA_Data.get(BLUE)[i], RGBA_Data.get(GREEN)[i]);
            var x = i / originalImage.getWidth();
            var y = i % originalImage.getWidth();
            image.setRGB(x, y,
                    pixelmap[i] ? Fill.getRGB()
                            : Color.WHITE.getRGB());
        }
        return pixelmap;
    }

    public void MathOnPixels(BufferedImage image, Function<Byte[], Integer> MathFunction) {
        for (int i = 0; i < getTotalPixels(); i++) {
            var CalculatedPixel = MathFunction.apply(ToWrapperArray(getRGB(i)));
            var x = i / originalImage.getWidth();
            var y = i % originalImage.getWidth();
            image.setRGB(x, y, CalculatedPixel);
        }
    }

    public void MathOnPixelInt(BufferedImage Destination, Function<Integer, Integer> MathFunction) {
        for (int Line = 0; Line < originalImage.getWidth(); Line++) {
            for (int Column = 0; Column < originalImage.getHeight(); Column++) {
                var CalculatedPixel = MathFunction.apply(originalImage.getRGB(Line, Column));
                Destination.setRGB(Column, Line, CalculatedPixel);
            }
        }
    }

    public byte[] getRGB(int x, int y) {
        byte b = RGBA_Data.get(BLUE)[x * originalImage.getWidth() + y];
        byte g = RGBA_Data.get(GREEN)[x * originalImage.getWidth() + y];
        byte r = RGBA_Data.get(RED)[x * originalImage.getWidth() + y];
        byte a = RGBA_Data.get(ALPHA)[x * originalImage.getWidth() + y];
        return new byte[]{a, r, g, b};
    }

    public byte[] getRGB(int LinearPosition) {
        if (LinearPosition < 0 && LinearPosition > getTotalPixels()) {
            throw new ArrayIndexOutOfBoundsException("the index Specified is not present on the image");
        }
        byte b = RGBA_Data.get(BLUE)[LinearPosition];
        byte g = RGBA_Data.get(GREEN)[LinearPosition];
        byte r = RGBA_Data.get(RED)[LinearPosition];
        byte a = RGBA_Data.get(ALPHA)[LinearPosition];
        return new byte[]{a, r, g, b};
    }

    private Byte[] ToWrapperArray(byte[] b) {
        var result = new Byte[b.length];
        Arrays.parallelSetAll(result, i -> b[i]);
        return result;
    }

    public byte getAlpha(int x, int y) {
        return RGBA_Data.get(ALPHA)[x * originalImage.getWidth() + y];
    }

    public byte getRed(int x, int y) {
        return RGBA_Data.get(RED)[x * originalImage.getWidth() + y];
    }

    public byte getGreen(int x, int y) {
        return RGBA_Data.get(GREEN)[x * originalImage.getWidth() + y];
    }

    public byte getBlue(int x, int y) {
        return RGBA_Data.get(BLUE)[x * originalImage.getWidth() + y];
    }

    BufferedImage getOriginal() {
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
        var image = createBIemptyCopy();//note We could just return a binary image
        for (int i = 0; i < getTotalPixels(); i++) {
            var CalculatedPixel = ((RGBA_Data.get(Channel)[i] >>> Index) & 1) == 0 ? RGBMASK : FillColor.getRGB();
            var x = i / originalImage.getWidth();
            var y = i % originalImage.getWidth();
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
        var image = createBIemptyCopy();//note We could just return a binary image
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
            var x = i / originalImage.getWidth();
            var y = i % originalImage.getWidth();
            image.setRGB(x, y, CalculatedPixel);
        }
        return image;
    }

}
