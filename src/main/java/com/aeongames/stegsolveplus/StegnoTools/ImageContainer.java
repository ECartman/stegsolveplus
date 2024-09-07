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
 */
package com.aeongames.stegsolveplus.StegnoTools;

import com.aeongames.edi.utils.error.LoggingHelper;
import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.logging.Level;
import javax.imageio.ImageIO;

/**
 *
 * @author Eduardo
 */
public class ImageContainer {

    /**
     * the max value for a single color Channel we use int rather than Byte cuz
     * java would convert it back to integer and back and thus wasting
     * computation. so for sake of space is not worth
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
     * a map that will contain the RGB data for the image when loaded. initially
     * this was made so colors could be handled separately and no calculation
     * were needed to separate or extract a channel.
     *
     * TODO: however it seems that most of the calculations are OK using all
     * channels. and thus this might not be as good on performance. need to
     * profile it.
     *
     */
    byte[][] RGBA_Data;

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

    public boolean ImageIsValid() {
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
            LoggingHelper.getLogger(ImageContainer.class.getName()).log(Level.WARNING, "the Image is null");
            return;
        }
        //this is faster but depends on the type of image to read the data accurately.
        //and of the type or order we desire.        
        var databuffer = originalImage.getRaster().getDataBuffer();
        if (databuffer.getNumBanks() > 1) {
            LoggingHelper.getLogger(ImageContainer.class.getName()).log(Level.INFO, "the Image contains Multiple ({0}) banks odd", databuffer.getNumBanks());
        }
        var totalpixels = getTotalPixels();
        byte[][] Colormaps = new byte[4][totalpixels];
        //we can either instanceof OR: 
        //databuffer.getDataType() == DataBuffer.TYPE_BYTE or make a guess depending 
        //on the type of Bufferimage. or several checks. but seems exesive 
        //and just cast... i think for the moment instance of does both things for us.
        var hasAlphaChannel = originalImage.getAlphaRaster() != null;
        if (databuffer instanceof DataBufferByte bytesData) {
            processByteData(originalImage.getType(), hasAlphaChannel, bytesData, Colormaps);
        } else if (databuffer instanceof DataBufferInt IntegerData) {
            processIntData(originalImage.getType(), hasAlphaChannel, IntegerData, Colormaps);
        } else {
            for (int y = 0, arrayindex = 0; y < originalImage.getHeight(); y++) {
                for (int x = 0; x < originalImage.getWidth(); x++, arrayindex++) {
                    var color = originalImage.getRGB(x, y);
                    //originalImage.getRaster().getDataElements(x, y, null);
                    Colormaps[ALPHA][arrayindex] = (byte) ((color & ALPHA_MASK) >>> 24);
                    Colormaps[RED][arrayindex] = (byte) ((color & RED_MASK) >>> 16);
                    Colormaps[GREEN][arrayindex] = (byte) ((color & GREEN_MASK) >>> 8);
                    Colormaps[BLUE][arrayindex] = (byte) ((color) & BLUE_MASK);
                }
            }
        }
        RGBA_Data = Colormaps;
    }

    /**
     * this functions returns the ordered indexes for the ARGB channels
     * depending on the type of image. example:
     * <p>
     * {@link BufferedImage#TYPE_INT_RGB} just returns an array with the order
     * {@code {ALPHA, RED, GREEN, BLUE}}
     * <br>
     * on the other hand for example if use:
     * <br> {@link BufferedImage#TYPE_4BYTE_ABGR} returns an array with the
     * order {@code {ALPHA, BLUE, GREEN, RED}}
     * </p>
     * <br>
     * <br>
     * NOT supported: TODO: add dictionaries for this translations.
     * <br> null null null null     {@link BufferedImage#TYPE_BYTE_GRAY}
     * {@link BufferedImage#TYPE_BYTE_BINARY}
     * {@link BufferedImage#TYPE_BYTE_INDEXED}
     * {@link BufferedImage#TYPE_USHORT_GRAY}
     * {@link BufferedImage#TYPE_USHORT_565_RGB}
     * {@link BufferedImage#TYPE_USHORT_555_RGB}
     * {@link BufferedImage#TYPE_CUSTOM}
     *
     * @param type
     * @return if a Not supported is used will return {@code null} otherwise a
     * array with the type translation for the channel order into ARGB
     *
     * @see BufferedImage#TYPE_INT_RGB
     * @see BufferedImage#TYPE_INT_ARGB
     * @see BufferedImage#TYPE_INT_ARGB_PRE
     * @see BufferedImage#TYPE_INT_BGR
     * @see BufferedImage#TYPE_3BYTE_BGR
     * @see BufferedImage#TYPE_4BYTE_ABGR
     * @see BufferedImage#TYPE_4BYTE_ABGR_PRE
     */
    private int[] getOrder(int type) {
        int[] order = null;// indexes A, R, G, B
        switch (type) {
            case BufferedImage.TYPE_INT_RGB, BufferedImage.TYPE_INT_ARGB, BufferedImage.TYPE_INT_ARGB_PRE ->
                order = new int[]{ALPHA, RED, GREEN, BLUE};
            case BufferedImage.TYPE_INT_BGR, BufferedImage.TYPE_3BYTE_BGR, BufferedImage.TYPE_4BYTE_ABGR, BufferedImage.TYPE_4BYTE_ABGR_PRE ->
                order = new int[]{ALPHA, BLUE, GREEN, RED};//basically is inverted or contrary Endianess
        }
        //TODO: support other types. 
        return order;
    }

    /**
     * process the DataBufferByte that contains the pseudo raw data from the
     * image into a Buffer image. and moves into the color maps provided
     *
     * @param type the type of image it was loaded. this is important to
     * Understand the order of the bytes (if they are little or big endian and
     * or know if it is RGB or BGR order.
     * <br> also look at
     * <a href="https://stackoverflow.com/questions/6524196/java-get-pixel-array-from-image">StackOverflow
     * question on similar scene we used</a>
     * @param hasAlpha whenever or not the image has alpha byte. (ARGB, ABGR)
     * @param bytesData the DataBuffer that contains the Image data (bytes)
     * @param Colormaps the Destination of the read and process data.
     */
    private void processByteData(int type, final boolean hasAlpha, final DataBufferByte bytesData, byte[][] Colormaps) {
        var order = getOrder(type);//this will crash if not found. that is desireable as we want to fix that problem. see the To do's on getOrder
        var byteData = bytesData.getData();//alternative we can use bytesData.getSize() instead of the lenght and call .getElem to get the value. but note this taxes on byte to int convertion
        final int BytesPerPixel = hasAlpha ? 4 : 3;
        final int jumpPerPixel = BytesPerPixel - 1;
        for (int dataIndex = 0, DestIndex = 0; dataIndex + jumpPerPixel < byteData.length; dataIndex += BytesPerPixel) {
            /* 
            Colormaps[order[0]][DestIndex] = hasAlpha ?(byte) bytesData.getElem(dataIndex) : (byte) MAXSINGLEVALUE;//full alpha (opaque) if has not alpha
            Colormaps[order[1]][DestIndex] =(byte) bytesData.getElem(dataIndex + jumpPerPixel - 2);
            Colormaps[order[2]][DestIndex] = (byte) bytesData.getElem(dataIndex + jumpPerPixel - 1);
            Colormaps[order[3]][DestIndex] = (byte) bytesData.getElem(dataIndex + jumpPerPixel);
             */
            Colormaps[order[0]][DestIndex] = hasAlpha ? byteData[dataIndex] : (byte) MAXSINGLEVALUE;//full alpha (opaque) if has not alpha
            Colormaps[order[1]][DestIndex] = byteData[dataIndex + jumpPerPixel - 2];
            Colormaps[order[2]][DestIndex] = byteData[dataIndex + jumpPerPixel - 1];
            Colormaps[order[3]][DestIndex] = byteData[dataIndex + jumpPerPixel];
            DestIndex++;
        }
    }

    /**
     * process the DataBufferInt that contains the pseudo raw data from the
     * image into a Buffer image. and moves into the color maps provided
     *
     * @param type the type of image it was loaded. this is important to
     * Understand the order of the bytes (if they are little or big endian and
     * or know if it is RGB or BGR order.
     * <br> also look at
     * <a href="https://stackoverflow.com/questions/59007942/how-to-optimize-this-bufferedimage-loop/59055178#59055178">StackOverflow
     * question on similar scene we used</a>
     * @param hasAlpha whenever or not the image has alpha byte. (ARGB, ABGR)
     * @param intData the DataBufferInt that contains the Image data (Integers)
     * NOTE: this is really similar than using get rbg.
     * @param Colormaps the Destination of the read and process data.
     */
    private void processIntData(int type, final boolean hasAlpha, final DataBufferInt intData, byte[][] Colormaps) {
        var order = getOrder(type);//this will crash if not found. that is desireable as we want to fix that problem. see the To do's on getOrder
        var pixels = intData.getData();//alternative we can use intData.getSize() instead of the lenght and call .getElem to get the value. but note this taxes on byte to int convertion
        for (int pixel = 0; pixel < pixels.length; pixel++) {
            //Colormaps[order[0]][pixel] = hasAlpha ? (byte) (( intData.getElem(pixel) & ALPHA_MASK) >>> 24) : (byte) MAXSINGLEVALUE;//full alpha (opaque) if has not alpha
            Colormaps[order[0]][pixel] = hasAlpha ? (byte) ((pixels[pixel] & ALPHA_MASK) >>> 24) : (byte) MAXSINGLEVALUE;//full alpha (opaque) if has not alpha
            Colormaps[order[1]][pixel] = (byte) ((pixels[pixel] & RED_MASK) >>> 16);
            Colormaps[order[2]][pixel] = (byte) ((pixels[pixel] & GREEN_MASK) >>> 8);
            Colormaps[order[3]][pixel] = (byte) (pixels[pixel] & BLUE_MASK);
        }
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
    public static int convertToUnsigned(byte value) {
        //return ((value) & MAXSINGLEVALUE);//this is the same basically
        return Byte.toUnsignedInt(value);
    }

    /**
     * creates a new BufferedImage that support ARGB (rgb+alpha) wit the same
     * dimensions as the original image.
     *
     * @return a new instance of BufferedImage that support ARGB (rgb+alpha)
     */
    public BufferedImage createBIemptyCopy() {
        return createBIemptyCopy(BufferedImage.TYPE_INT_ARGB);
    }

    /**
     * creates a new BufferedImage that support RGB (rgb NOT ALPHA) wit the same
     * dimensions as the original image.
     *
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

    public int getTotalPixels() {
        return originalImage.getWidth() * originalImage.getHeight();
    }

    /**
     * Calculates the {@link Point} (the X and Y) position of the provided index
     * given the provided Image Width. the calculation is done as follows:<br>
     * <pre>
     * {@code
     *  Y_Axis = <the index> / Width
     *   X_Axis = <the index> % Width
     * }
     * </pre> the {@code Y_Axis} is how many times the Width has been looped
     * thus how many times the value of {@code index} has leaped the
     * {@code Width}
     * <br>
     * the {@code X_Axis} is the reminder pass a {@code Width} and thus
     * providing the index or Column within the current Row
     * <br>
     *
     * @param Width the source Width to calculate the point on a Plane
     * @param index the current index where the point is located at.
     * @return
     */
    private Point RelativiseLinearIndexToXY(int Width, int index) {
        return new Point(index % Width, index / Width);
    }

    public void getSymetricPixels(BufferedImage image, Color Fill) {
        var totalpixels = getTotalPixels();
        for (int i = 0; i < totalpixels; i++) {
            var symetric = Objects.equals(RGBA_Data[RED][i], RGBA_Data[GREEN][i])
                    && Objects.equals(RGBA_Data[BLUE][i], RGBA_Data[GREEN][i]);
            var p = RelativiseLinearIndexToXY(image.getWidth(),i);
            image.setRGB(p.x, p.y,symetric 
                    ? Fill.getRGB()
                    : Color.WHITE.getRGB());
        }
    }

    public void MathOnPixels(BufferedImage image, Function<Short[], Integer> MathFunction) {
        for (int i = 0; i < getTotalPixels(); i++) {
            var CalculatedPixel = MathFunction.apply(ToWrapperArray(getRGB(i)));
            var point = RelativiseLinearIndexToXY(image.getWidth(), i);
            image.setRGB(point.x, point.y, CalculatedPixel);
        }
    }

    public void MathOnPixels(BiConsumer<Short[], Point> MathConsumer) {
        for (int i = 0; i < getTotalPixels(); i++) {
            MathConsumer.accept(ToWrapperArray(getRGB(i)), RelativiseLinearIndexToXY(originalImage.getWidth(), i));
        }
    }

    //TODO change to NOT use RGB
    public void MathOnPixelInt(BufferedImage Destination, Function<Integer, Integer> MathFunction) {
        for (int x = 0; x < originalImage.getWidth(); x++) {
            for (int y = 0; y < originalImage.getHeight(); y++) {
                var CalculatedPixel = MathFunction.apply(originalImage.getRGB(x, y));
                Destination.setRGB(x, y, CalculatedPixel);
            }
        }
    }

    public short[] getRGB(int x, int y) {
        short b = RGBA_Data[BLUE][y * originalImage.getWidth() + x];
        short g = RGBA_Data[GREEN][y * originalImage.getWidth() + x];
        short r = RGBA_Data[RED][y * originalImage.getWidth() + x];
        short a = RGBA_Data[ALPHA][y * originalImage.getWidth() + x];
        return new short[]{a, r, g, b};
    }

    public short[] getRGB(int LinearPosition) {
        if (LinearPosition < 0 && LinearPosition > getTotalPixels()) {
            throw new ArrayIndexOutOfBoundsException("the index Specified is not present on the image");
        }
        byte b = RGBA_Data[BLUE][LinearPosition];
        byte g = RGBA_Data[GREEN][LinearPosition];
        byte r = RGBA_Data[RED][LinearPosition];
        byte a = RGBA_Data[ALPHA][LinearPosition];
        return new short[]{a, r, g, b};
    }

    private Short[] ToWrapperArray(short[] b) {
        var result = new Short[b.length];
        Arrays.parallelSetAll(result, i -> b[i]);
        return result;
    }

    public byte getAlpha(int x, int y) {
        return RGBA_Data[ALPHA][y * originalImage.getWidth() + x];
    }

    public byte getRed(int x, int y) {
        return RGBA_Data[RED][y * originalImage.getWidth() + x];
    }

    public byte getGreen(int x, int y) {
        return RGBA_Data[GREEN][y * originalImage.getWidth() + x];
    }

    public byte getBlue(int x, int y) {
        return RGBA_Data[BLUE][y * originalImage.getWidth() + x];
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
            var CalculatedPixel = ((RGBA_Data[Channel][i] >>> Index) & 1) == 0 ? RGBMASK : FillColor.getRGB();
            var p = RelativiseLinearIndexToXY(originalImage.getWidth(), i);
            image.setRGB(p.x,p.y, CalculatedPixel);
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
        //if we need to recreate the data. we will neeed a diferent function. 
        for (int i = 0; i < getTotalPixels(); i++) {
            int CalculatedPixel = convertToUnsigned(RGBA_Data[Channel][i]) << position;
            var p = RelativiseLinearIndexToXY(image.getWidth(),i);
            image.setRGB(p.x, p.y, CalculatedPixel);
        }
        return image;
    }

}
