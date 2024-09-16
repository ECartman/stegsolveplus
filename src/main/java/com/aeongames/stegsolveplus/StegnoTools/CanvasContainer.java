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
import javax.imageio.ImageIO;

/**
 * a Class that holds the Image from a file,url,Stream or a provided original
 * image this class will hold the image and will not modify, but will not
 * provide a reference to it. because otherwise the image is easily editable and
 * we desire to hold unedited as much as possible for our forensic analysis if
 * you ask this class for the original image the best we do is provide a copy of
 * the original image. any morph or transformation we do is done into a new
 * image where we copy the data of the original image (Dependent on which
 * transformation or math is as to do) this class also has functions for those
 * transformations or analysis.
 *
 *
 * interesting
 * reads:http://www.eyemaginary.com/Compositing/EG06-Presentation.pdf
 * http://www.eyemaginary.com/Portfolio/index.html
 * https://cadik.posvete.cz/color_to_gray_evaluation/cadik08perceptualEvaluation-slides.pdf
 * <a href="https://stackoverflow.com/questions/6524196/java-get-pixel-array-from-image">StackOverflow
 * question on similar scene we used</a>
 *
 * @author Eduardo Vindas
 */
public class CanvasContainer {

    // <editor-fold defaultstate="collapsed" desc="static Vars">
    /**
     * the max value for a single color Channel 0xFF or (255) the max value of
     * an unsigned Byte.
     */
    static final int MAXUBYTE = 0xFF;
    /**
     * a mask to gather the RGB composed value. (no ALPHA channel) from a
     * Integer
     */
    static final int RGBMASK = 0x00FFFFFF;

    /**
     * Constants to read from the ARGB data from Byte or Short array.
     */
    static final int ALPHA = 0,
            RED = 1,
            GREEN = 2,
            BLUE = 3;
    // </editor-fold>

    /**
     * The Base Image for all combinations or calculations. this image is prone
     * to be changed. due the nature of {@link BufferedImage} thus to avoid
     * change this image is Final and Is NEVER to leave this class. if a caller
     * needs the "original" provide a copy.
     */
    private final BufferedImage originalImage;
    /**
     * this object will hold a reference to an array that contains the buffer of
     * the {@link originalImage} we do this on this manner because BufferImage
     * has synchronization to change the state and this is redundant for our
     * needs this is not needed. and thus to void slowness we will hold a
     * reference to the underline array. now why a Object instead of they array.
     * because the Array can be a Byte or Integer array. and we cannot assume
     * either. we could use 2 references for each but there is no real need and
     * to write into they would take more time. for our pro
     */
    private Object ImageDataReference = null;

    // <editor-fold defaultstate="collapsed" desc="Constructors">
    /**
     * Package Private constructor. creates a new instance of CanvasContainer
     * loading the Image from the Source.
     *
     * @param Source the Path where the file is stored. cannot be null
     * @throws IOException if the file fails to load.
     */
    CanvasContainer(Path Source) throws IOException {
        Objects.requireNonNull(Source, "the path is null");
        originalImage = ImageIO.read(Source.toFile());
        check(originalImage);
    }

    /**
     * Package Private constructor. creates a new instance of CanvasContainer
     * loading the Image from the Source.
     *
     * @param Source the URL where the file is stored. cannot be null
     */
    CanvasContainer(URL Source) throws IOException {
        Objects.requireNonNull(Source, "the path is null");
        originalImage = ImageIO.read(Source);
        check(originalImage);
    }

    /**
     * Package Private constructor. creates a new instance of CanvasContainer
     * loading the Image from the Source.
     *
     * @param ImageStream the Stream from which to read the image.
     */
    CanvasContainer(InputStream ImageStream) throws IOException {
        Objects.requireNonNull(ImageStream, "the path is null");
        originalImage = ImageIO.read(ImageStream);
        check(originalImage);
    }

    /**
     * Package Private constructor. creates a new instance of CanvasContainer
     * and takes a source image. given the way we want to control we will not
     * use the provided image. rather we will make a clone if the image and use
     * as our internal image.
     *
     * @param SourceToClone a {@link BufferedImage} to be copied to the internal
     * image to use for analysis.
     */
    CanvasContainer(final BufferedImage SourceToClone) {
        Objects.requireNonNull(SourceToClone, "the Source Image is null");
        originalImage = getCloneofImage(SourceToClone);
    }

    /**
     * checks for a Image null reference. if is throws a Wrapped
     * {@link NullPointerException} in a {@link IOException} this is done this
     * way as this is a error while READING the file and thus we can safely
     * consider a I/O error while the underline error is the ref is null. (this
     * is almost because the Param to BufferedImage constructor is null or (and
     * what we look for) there was no Reader to parse the file.
     *
     * @param originalImage the reference to check if is null
     * @throws IOException if the reference is null.
     */
    private void check(BufferedImage originalImage) throws IOException {
        if (null == originalImage) {
            throw new IOException(new NullPointerException("We cannot Read the Image, This error can be caused by either there is no supported Image reader for the file or the file is Not a image."));
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="create empty image"> 
    /**
     * creates a new BufferedImage that support ARGB wit the same dimensions as
     * the original image. but <strong>NO CONTENT</strong>
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
     * @return a new instance of BufferedImage that support RGB but no Alpha
     * Channel
     */
    public BufferedImage createBINoAlphaemptyCopy() {
        return createBIemptyCopy(BufferedImage.TYPE_INT_RGB);
    }

    /**
     * creates a new BufferedImage for the provided type (i.e: RGB,
     * TYPE_BYTE_GRAY (gray scale)) with the same dimensions than the original
     * but with no content.
     *
     * @param type the type to use.
     * @return a new instance of BufferedImage with the same dimensions as the
     * original
     * @throws NullPointerException if the original image is null (fail to load)
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
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="imageInfo">
    /**
     * get the total number of Pixels (combination of colors (A)RBG) on this
     * image.
     *
     * @return the total of pixels on the Original image.
     */
    public int getTotalPixels() {
        return originalImage.getWidth() * originalImage.getHeight();
    }

    /**
     * check if the image has a Alpha channel on the original image.
     *
     * @return
     */
    public boolean HasAlphaChannel() {
        return originalImage.getAlphaRaster() != null;
    }

    private synchronized Object setupDataBuffer() {
        if (ImageDataReference != null) {
            return ImageDataReference;
        }
        var databuffer = originalImage.getRaster().getDataBuffer();
        switch (databuffer) {
            case DataBufferByte bytesData ->
                ImageDataReference = bytesData.getData();
            case DataBufferInt IntegerData ->
                ImageDataReference = IntegerData.getData();
            default ->
                ImageDataReference = null;                    
        }
        return ImageDataReference;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="static info Function"> 
    /**
     * this functions returns the ordered indexes for the ARGB channels from
     * ARGB to The Destination order. depending on the type of image. example:
     * <pre>
     * {@link BufferedImage#TYPE_INT_RGB} just returns an array with the order
     * {@code
     *
     * ALPHA ->0  (no change)
     * RED   ->1  (no change)
     * GREEN ->2  (no change)
     * BLUE  ->3  (no change)
     * }
     * in contrast, {@link BufferedImage#TYPE_4BYTE_ABGR} returns an array
     * with the order:
     * {@code
     *
     * ALPHA ->0  (no change)
     * BLUE  ->1  (index for RED returns BLUE)
     * GREEN ->2  (no change)
     * RED   ->3  (index for BLUE returns RED)
     * }
     * </pre> NOT supported:
     * <pre>
     * {@link BufferedImage#TYPE_BYTE_GRAY}
     * {@link BufferedImage#TYPE_BYTE_BINARY}
     * {@link BufferedImage#TYPE_BYTE_INDEXED}
     * {@link BufferedImage#TYPE_USHORT_GRAY}
     * {@link BufferedImage#TYPE_USHORT_565_RGB}
     * {@link BufferedImage#TYPE_USHORT_555_RGB}
     * {@link BufferedImage#TYPE_CUSTOM}
     * </pre>
     *
     * @param type the Type of image being used.
     * @return {@code type} is not supported we return {@code null} otherwise a
     * array with the type translation for the channel in ARGB to the desired
     * one.
     *
     * @see BufferedImage#TYPE_INT_RGB
     * @see BufferedImage#TYPE_INT_ARGB
     * @see BufferedImage#TYPE_INT_ARGB_PRE
     * @see BufferedImage#TYPE_INT_BGR
     * @see BufferedImage#TYPE_3BYTE_BGR
     * @see BufferedImage#TYPE_4BYTE_ABGR
     * @see BufferedImage#TYPE_4BYTE_ABGR_PRE
     */
    private static int[] getColorOrder(int type) {
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

    private static int getColorTranslation(int type, int channel) {
        switch (type) {
            default -> {
                return channel;
            }
            case BufferedImage.TYPE_INT_RGB, BufferedImage.TYPE_INT_ARGB, BufferedImage.TYPE_INT_ARGB_PRE -> {
                return channel;//no translation is required
            }
            case BufferedImage.TYPE_INT_BGR, BufferedImage.TYPE_4BYTE_ABGR, BufferedImage.TYPE_4BYTE_ABGR_PRE -> {
                switch (channel) {
                    case ALPHA:
                        return 0;
                    default:
                    case BLUE:
                        return 1;
                    case GREEN:
                        return 2;
                    case RED:
                        return 3;
                }
            }
            case BufferedImage.TYPE_3BYTE_BGR -> {
                switch (channel) {
                    default:
                    case BLUE:
                        return 0;
                    case GREEN:
                        return 1;
                    case RED:
                        return 2;
                }
            }
            case BufferedImage.TYPE_BYTE_GRAY -> {
                return 0;
            }
        }
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
    private static Point getPointForIndex(final int Width, final int index) {
        return new Point(index % Width, index / Width);
    }

    /**
     * Calculates the index for the given coordinates on a image. the returned
     * value is the index of a particular pixel on the image graph
     *
     * @param Width the Width of the image.
     * @param x the x axis to locate a particular pixel
     * @param y the y axis to locate a particular pixel
     * @return the index of the pixel on a linear order.
     */
    private static int getIndexForPosition(final int Width, int x, int y) {
        return y * Width + x;
    }

    private static int getRawIndexForImageIndex(int RawIndexPerImgIndex, int Index) {
        return RawIndexPerImgIndex * Index;
    }

    private static byte[] getRGBArray(Color col) {
        byte[] RGBBYTES = new byte[3];
        RGBBYTES[0] = (byte) col.getRed();
        RGBBYTES[1] = (byte) col.getGreen();
        RGBBYTES[2] = (byte) col.getBlue();
        return RGBBYTES;
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
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Clone Channels"> 
    private static void cloneChannelDefault(int Channel, BufferedImage srcimg, int totalpixels, int DestType, byte[] Destdata) {
        for (int i = 0; i < totalpixels; i++) {
            var pos = getPointForIndex(srcimg.getWidth(), i);
            var data = srcimg.getRaster().getDataElements(pos.x, pos.y, null);
            var baseindex = getRawIndexForImageIndex(3, i);
            switch (Channel) {
                case ALPHA -> {//check if hasAlphaChannel maybe? 
                    Destdata[baseindex + DestType == BufferedImage.TYPE_BYTE_GRAY ? 0 : 2]
                            = (byte) srcimg.getColorModel().getAlpha(data);
                }
                case RED -> {
                    Destdata[baseindex + DestType == BufferedImage.TYPE_BYTE_GRAY ? 0 : 2]
                            = (byte) srcimg.getColorModel().getRed(data);
                }
                case GREEN ->
                    Destdata[baseindex + DestType == BufferedImage.TYPE_BYTE_GRAY ? 0 : 1]
                            = (byte) srcimg.getColorModel().getGreen(data);
                case BLUE -> {
                    Destdata[baseindex]
                            = (byte) srcimg.getColorModel().getBlue(data);
                }
            }
        }
    }

    /**
     * <strong>This Function Should not be called from a loop, if there are
     * multiple threads accessing the {@link DataBufferByte} as this object is
     * Sync and thus might run slow. performance will be impacted.
     * </strong>
     * this is due
     *
     * @param SourceType
     * @param DestinationType
     * @param hasAlpha
     * @param destHasAlpha
     * @param SrcBuffer
     * @param Destinationdatabuffer
     * @param Channel
     */
    private static void cloneChannelBytes(int SourceType, int DestinationType, boolean hasAlpha, boolean destHasAlpha, byte[] SrcBuffer, DataBufferByte Destinationdatabuffer, int Channel) {
        //source navigation.
        int srcBytesPerPixel = hasAlpha ? 4 : 3;
        int jumpPerPixel = srcBytesPerPixel - 1;
        var destData = Destinationdatabuffer.getData();
        var srcTranslatedChannel = getColorTranslation(SourceType, Channel);
        //destination Navigation
        int destBytesPerPixel;
        if (DestinationType == BufferedImage.TYPE_BYTE_GRAY) {
            destBytesPerPixel = 1;
        } else {
            destBytesPerPixel = destHasAlpha ? 4 : 3;
        }
        var destTranslatedChannel = getColorTranslation(DestinationType, Channel);
        if (hasAlpha && destHasAlpha && Channel == ALPHA) {
            destTranslatedChannel++; //make alpha visible if there is alpha channel if there is none. this calc is not required.
        }
        for (int Sourceindex = 0, destindex = 0; Sourceindex + jumpPerPixel < SrcBuffer.length; Sourceindex += srcBytesPerPixel, destindex += destBytesPerPixel) {
            destData[destindex + destTranslatedChannel] = SrcBuffer[Sourceindex + srcTranslatedChannel];
        }
    }

    /**
     * <strong>This Function Should not be called from a loop, if there are
     * multiple threads accessing the {@link DataBufferByte} as this object is
     * Sync and thus might run slow. performance will be impacted.
     * </strong>
     *
     * @param SourceType
     * @param DestinationType
     * @param hasAlpha
     * @param destHasAlpha
     * @param IntegersData
     * @param Destinationdatabuffer
     * @param Channel
     */
    private static void cloneChannelInt(int SourceType, int DestinationType, boolean hasAlpha, boolean destHasAlpha, int[] SrcData, DataBufferByte Destinationdatabuffer, int Channel) {
        int destBytesPerPixel;
        if (DestinationType == BufferedImage.TYPE_BYTE_GRAY) {
            destBytesPerPixel = 1;
        } else {
            destBytesPerPixel = destHasAlpha ? 4 : 3;
        }
        var DestbyteData = Destinationdatabuffer.getData();
        var destTranslatedChannel = getColorTranslation(DestinationType, Channel);

        var srcTranslatedChannel = getColorTranslation(SourceType, Channel);

        var shift = 0;
        if (hasAlpha && !destHasAlpha && Channel == ALPHA) {
            // leave the shift as 0 make the alpha visible on the Blue or Red channel. 
        } else {
            shift = 8 * (3 - srcTranslatedChannel);
        }
        for (int Sourceindex = 0, destindex = 0; Sourceindex < SrcData.length; Sourceindex++, destindex += destBytesPerPixel) {
            DestbyteData[destindex + destTranslatedChannel] = (byte) ((SrcData[Sourceindex] >>> shift) & MAXUBYTE);
        }
    }
    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="DrawnSymetric (grey-ish)pixels">
    /**
     * <strong>This Function Should not be called from a loop, if there are
     * multiple threads accessing the {@link DataBufferByte} as this object is
     * Sync and thus might run slow. performance will be impacted.
     * </strong>
     *
     * @param Type
     * @param DestType
     * @param srcHasAlpha
     * @param DestHasAlpha
     * @param SrcBuffer
     * @param destBuffer
     * @param Fill
     */
    private static void DrawSymetricBytes(int Type, int DestType, boolean srcHasAlpha, boolean DestHasAlpha, byte[] SrcBuffer, DataBufferByte destBuffer, Color Fill) {
        CanvasContainer.DrawSymetricBytes(Type, DestType, srcHasAlpha, DestHasAlpha, SrcBuffer, destBuffer, getRGBArray(Fill));
    }

    private static void DrawSymetricGreyBytes(boolean srcHasAlpha, byte[] SourceData, DataBufferByte destBuffer, byte Fill) {
        final int FirstBytesPerPixel = srcHasAlpha ? 4 : 3;
        final int jumpPerPixel = FirstBytesPerPixel - 1;
        var destData = destBuffer.getData();
        for (int srcIndex = 0, SecondPixel = 0; srcIndex + jumpPerPixel < SourceData.length; srcIndex += FirstBytesPerPixel, SecondPixel++) {
            var same = SourceData[srcIndex + jumpPerPixel] == SourceData[srcIndex + jumpPerPixel - 1]
                    && SourceData[srcIndex + jumpPerPixel - 1] == SourceData[srcIndex + jumpPerPixel - 2];
            if (same) {
                destData[SecondPixel] = Fill;
            }
        }
    }

    /**
     * <strong>This Function Should not be called from a loop, if there are
     * multiple threads accessing the {@link DataBufferByte} as this object is
     * Sync and thus might run slow. performance will be impacted.
     * </strong>
     *
     * @param Type
     * @param DestType
     * @param srcHasAlpha
     * @param DestHasAlpha
     * @param SourceData
     * @param destBuffer
     * @param RGBfill
     */
    private static void DrawSymetricBytes(int Type, int DestType, boolean srcHasAlpha, boolean DestHasAlpha, byte[] SourceData, DataBufferByte destBuffer, byte[] RGBfill) {
        if (DestType == BufferedImage.TYPE_BYTE_GRAY) {
            var fillIntesity = Math.min(Math.min(RGBfill[0], RGBfill[1]), RGBfill[2]);
            CanvasContainer.DrawSymetricGreyBytes(srcHasAlpha, SourceData, destBuffer, (byte) fillIntesity);
            return;
        }
        final int FirstBytesPerPixel = srcHasAlpha ? 4 : 3;
        final int SecondBytesPerPixel = DestHasAlpha ? 4 : 3;
        final int jumpPerPixel = FirstBytesPerPixel - 1;
        var destData = destBuffer.getData();
        var SecondTranslationOrder = getColorOrder(DestType);
        for (int srcIndex = 0, SecondIndex = 0; srcIndex + jumpPerPixel < SourceData.length; srcIndex += FirstBytesPerPixel, SecondIndex += SecondBytesPerPixel) {
            var same = SourceData[srcIndex + jumpPerPixel] == SourceData[srcIndex + jumpPerPixel - 1]
                    && SourceData[srcIndex + jumpPerPixel] == SourceData[srcIndex + jumpPerPixel - 2];
            if (same) {
                //put this pixel as color 
                destData[SecondIndex + SecondBytesPerPixel - 3] = RGBfill[SecondTranslationOrder[RED] - 1];//we -1 as this does not have alpha index thus 'red'is 0 instead of 1 
                destData[SecondIndex + SecondBytesPerPixel - 2] = RGBfill[SecondTranslationOrder[GREEN] - 1];
                destData[SecondIndex + SecondBytesPerPixel - 1] = RGBfill[SecondTranslationOrder[BLUE] - 1];
            }
        }
    }

    /**
     * <strong>This Function Should not be called from a loop, if there are
     * multiple threads accessing the {@link DataBufferByte} as this object is
     * Sync and thus might run slow. performance will be impacted.
     * </strong>
     *
     * @param type
     * @param DestType
     * @param DestHasAlpha
     * @param SrcBuffer
     * @param destBuffer
     * @param Fill
     */
    private static void DrawSymetricInt(int type, int DestType, boolean DestHasAlpha, int[] SrcBuffer, DataBufferByte destBuffer, Color Fill) {
        CanvasContainer.DrawSymetricInt(type, DestType, DestHasAlpha, SrcBuffer, destBuffer, getRGBArray(Fill));
    }

    /**
     * <strong>This Function Should not be called from a loop, if there are
     * multiple threads accessing the {@link DataBufferByte} as this object is
     * Sync and thus might run slow. performance will be impacted.
     * </strong>
     *
     * @param type
     * @param DestType
     * @param DestHasAlpha
     * @param SourceData
     * @param destBuffer
     * @param RGBfill
     */
    private static void DrawSymetricInt(int type, int DestType, boolean DestHasAlpha, int[] SourceData, DataBufferByte destBuffer, byte[] RGBfill) {
        if (DestType == BufferedImage.TYPE_BYTE_GRAY) {
            var fillIntesity = Math.min(Math.min(RGBfill[0], RGBfill[1]), RGBfill[2]);
            DrawSymetricGreyInt(SourceData, destBuffer, (byte) fillIntesity);
            return;
        }
        final int SecondBytesPerPixel = DestHasAlpha ? 4 : 3;
        var destData = destBuffer.getData();
        var SecondTranslationOrder = getColorOrder(DestType);
        for (int srcIndex = 0, SecondIndex = 0; srcIndex < SourceData.length; srcIndex++, SecondIndex += SecondBytesPerPixel) {
            var mid = ((SourceData[srcIndex] >>> 8) & MAXUBYTE);
            var same = ((SourceData[srcIndex] >>> 16) & MAXUBYTE) == mid
                    && (SourceData[srcIndex] & MAXUBYTE) == mid;
            if (same) {
                //put this pixel as color 
                destData[SecondIndex + SecondBytesPerPixel - 3] = RGBfill[SecondTranslationOrder[1] - 1];//we -1 as this does not have alpha index thus 'red'is 0 instead of 1 
                destData[SecondIndex + SecondBytesPerPixel - 2] = RGBfill[SecondTranslationOrder[2] - 1];
                destData[SecondIndex + SecondBytesPerPixel - 1] = RGBfill[SecondTranslationOrder[3] - 1];
            }
        }
    }

    private static void DrawSymetricGreyInt(int[]  SourceData, DataBufferByte destBuffer, byte Fill) {
        var destData = destBuffer.getData();
        for (int srcIndex = 0; srcIndex < SourceData.length; srcIndex++) {
            var mid = ((SourceData[srcIndex] >>> 8) & MAXUBYTE);
            var same = ((SourceData[srcIndex] >>> 16) & MAXUBYTE) == mid
                    && (SourceData[srcIndex] & MAXUBYTE) == mid;
            if (same) {
                destData[srcIndex] = Fill;
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="get Color for Pixel at index"> 
    /**
     * gather the information from the image at the desired {@code Index} from
     * the linear position on the image. this function translate the Index into
     * the index that the {@code bytesData} locates the pixel data for the
     * desired index and for the desired Color Channel {@code channel}
     *
     * @param type the type of image it was loaded. this is important to
     * Understand the order of the bytes (if they are little or big edian and or
     * know if it is RGB or BGR order.
     * <br> also look at
     * <a href="https://stackoverflow.com/questions/6524196/java-get-pixel-array-from-image">StackOverflow
     * question on similar scene we used</a>
     * @param hasAlpha whenever or not the image has alpha byte. (ARGB, ABGR)
     * @param bytesData the DataBuffer that contains the Image data (bytes)
     * @param channel the Color channel that is desired to be returned one of
     * the following:      <pre>
     * {@link CanvasContainer#ALPHA}
     * {@link CanvasContainer#RED}
     * {@link CanvasContainer#GREEN}
     * {@link CanvasContainer#BLUE}
     * </pre>
     *
     * @param Index the index from which we should locate the pixel on a linear
     * lookup
     * @return a value between 0 and 0xFF(255) (unsigned) with the intensity for
     * the particular channel on the provided index (you need to call
     * {@link CanvasContainer#convertToUnsigned(byte)} or
     * {@link Byte#toUnsignedInt(byte)}
     * @throws IndexOutOfBoundsException if the channel is not      <pre>
     * {@link CanvasContainer#ALPHA}
     * {@link CanvasContainer#RED}
     * {@link CanvasContainer#GREEN}
     * {@link CanvasContainer#BLUE}
     * </pre>
     */
    private static byte getColorPixelByte(int type, final boolean hasAlpha, final byte[] byteData, int channel, int Index) {
        var order = getColorOrder(type);//this will crash if not found. that is desireable as we want to fix that problem. see the To do's on getOrder
        final int BytesPerPixel = hasAlpha ? 4 : 3;
        var ConvertedIndex = getRawIndexForImageIndex(BytesPerPixel, Index);
        if (channel == order[ALPHA]) {
            return hasAlpha ? byteData[ConvertedIndex] : (byte) MAXUBYTE;//full alpha (opaque) if has not alpha
        } else if (channel == order[RED]) {
            return byteData[ConvertedIndex + BytesPerPixel - 3];
        } else if (channel == order[GREEN]) {
            return byteData[ConvertedIndex + BytesPerPixel - 2];
        } else if (channel == order[BLUE]) {
            return byteData[ConvertedIndex + BytesPerPixel - 1];
        } else {
            throw new IndexOutOfBoundsException(String.format("Invalid Channel %d", channel));
        }
    }

    /**
     * gather the information from the image at the desired {@code Index} from
     * the linear position on the image. this function translate the Index into
     * the index that the {@code bytesData} locates the pixel data for the
     * desired index and for the desired Color Channel {@code channel}
     *
     * @param type the type of image it was loaded. this is important to
     * Understand the order of the bytes (if they are little or big edian and or
     * know if it is RGB or BGR order.
     * <br> also look at
     * <a href="https://stackoverflow.com/questions/6524196/java-get-pixel-array-from-image">StackOverflow
     * question on similar scene we used</a>
     * @param hasAlpha whenever or not the image has alpha byte. (ARGB, ABGR)
     * @param bytesData the DataBuffer that contains the Image data (bytes)
     * @param channel the Color channel that is desired to be returned one of
     * the following:      <pre>
     * {@link CanvasContainer#ALPHA}
     * {@link CanvasContainer#RED}
     * {@link CanvasContainer#GREEN}
     * {@link CanvasContainer#BLUE}
     * </pre>
     *
     * @param Index the index from which we should locate the pixel on a linear
     * lookup
     * @return a value between 0 and 0xFF(255) with the intensity for the
     * particular channel on the provided index.
     * @throws IndexOutOfBoundsException if the channel is not      <pre>
     * {@link CanvasContainer#ALPHA}
     * {@link CanvasContainer#RED}
     * {@link CanvasContainer#GREEN}
     * {@link CanvasContainer#BLUE}
     * </pre>
     */
    private static int getColorPixelInt(int type, final boolean hasAlpha, final int[] intData, int channel, int Index) {
        var Translate = getColorOrder(type);//this will crash if not found. that is desireable as we want to fix that problem. see the To do's on getOrder
        if (channel == Translate[ALPHA]) {
            return hasAlpha ? ((intData[Index] >>> 24) & MAXUBYTE) : (byte) MAXUBYTE;//full alpha (opaque) if has not alpha
        } else if (channel == Translate[RED]) {
            return ((intData[Index] >>> 16) & MAXUBYTE);
        } else if (channel == Translate[GREEN]) {
            return ((intData[Index] >>> 8) & MAXUBYTE);
        } else if (channel == Translate[BLUE]) {
            return (intData[Index] & MAXUBYTE);
        } else {
            throw new IndexOutOfBoundsException(String.format("Invalid Channel %d", channel));
        }
    }
    // </editor-fold>

    /**
     * this function execute the provided "math" functionality into the pixel
     * data for the image. and returns a image with the resulting data from the
     * function. for each pixel.
     *
     * @param TypeRequred the type of image is desired as results. thus function
     * supports: <pre>
     * {@link BufferedImage#TYPE_3BYTE_BGR}
     * {@link BufferedImage#TYPE_4BYTE_ABGR}
     * {@link BufferedImage#TYPE_4BYTE_ABGR_PRE}
     * {@link BufferedImage#TYPE_INT_RGB}
     * {@link BufferedImage#TYPE_INT_ARGB}
     * {@link BufferedImage#TYPE_INT_ARGB_PRE}
     * {@link BufferedImage#TYPE_INT_BGR}
     * {@link BufferedImage#TYPE_BYTE_GRAY}
     * </pre>
     *
     * @param MathFunction a {@link Function} that accepts and returns an array
     * of shorts values. the input array is an array of {@link Short} type
     * values of size {@code 4} in the order this class works with. see: null
     * null null null null null null null null null null null null null     {@link CanvasContainer#ALPHA},
     * {@link CanvasContainer#RED},
     * {@link CanvasContainer#GREEN},
     * {@link CanvasContainer#BLUE}. the resulting array is also Expected that
     * return a array in the same Order (ARGB) and size. with the resulting data
     * with a Single Exception: {@link BufferedImage#TYPE_BYTE_GRAY} that will
     * accept an single value array.<br>
     * <strong>NOTE:</strong>no matter the type of image provided this function
     * expects and uses arrays with the ARGB order. no matter if the type is BGR
     * as this function handles the translations.
     * @return a image that contain the changes to the pixels done via the
     * provided function. TODO:: This is slow because the call stack ends up
     * into a Syncronized block at StateTrackableDelegate.SetUntrable. this
     * blocking statement blocks us a BIG deal. and for no reason. as MULTIPLE
     * calls have been done alredy to this function making thus function
     * pointless.
     */
    public BufferedImage MathOnPixels(int TypeRequred, Function<Short[], Short[]> MathFunction) {
        //note if provided a unsupported type we could use whatever we want... or throw a error.
        BufferedImage ResultImage = null;
        switch (TypeRequred) {
            default ->
                throw new UnsupportedOperationException(String.format("%s: %d", "the specific Type of image is not Supported", TypeRequred));
            case BufferedImage.TYPE_3BYTE_BGR, BufferedImage.TYPE_4BYTE_ABGR, BufferedImage.TYPE_4BYTE_ABGR_PRE, BufferedImage.TYPE_INT_RGB, BufferedImage.TYPE_INT_ARGB, BufferedImage.TYPE_INT_ARGB_PRE, BufferedImage.TYPE_INT_BGR, BufferedImage.TYPE_BYTE_GRAY ->
                ResultImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), TypeRequred);
        }
        var Destinationdatabuffer = ResultImage.getRaster().getDataBuffer();
        //todo: move the for and add this inside the byte buffer case 
        var Translation = getColorOrder(TypeRequred);
        var hasAlpha = ResultImage.getAlphaRaster() != null;
        for (int i = 0; i < getTotalPixels(); i++) {
            var CalculatedPixel = MathFunction.apply(getRGB(i));
            switch (Destinationdatabuffer) {
                case DataBufferByte bytesData -> {
                    var byteData = bytesData.getData();//alternative we can use bytesData.getSize() instead of the lenght and call .getElem to get the value. but note this taxes on byte to int convertion
                    if (TypeRequred == BufferedImage.TYPE_BYTE_GRAY) {
                        if (CalculatedPixel.length == 1) {
                            byteData[i] = CalculatedPixel[0].byteValue();
                        } else {
                            byteData[i] = CalculatedPixel[RED].byteValue();
                        }
                    } else {
                        int BytesPerPixel = hasAlpha ? 4 : 3;
                        var ConvertedIndex = getRawIndexForImageIndex(BytesPerPixel, i);
                        final int jumpPerPixel = BytesPerPixel - 1;
                        byteData[ConvertedIndex] = CalculatedPixel[Translation[ALPHA]].byteValue();
                        byteData[ConvertedIndex + jumpPerPixel - 2] = CalculatedPixel[Translation[RED]].byteValue();
                        byteData[ConvertedIndex + jumpPerPixel - 1] = CalculatedPixel[Translation[GREEN]].byteValue();
                        byteData[ConvertedIndex + jumpPerPixel] = CalculatedPixel[Translation[BLUE]].byteValue();
                    }
                }
                case DataBufferInt IntegerData -> {
                    int resultvalue;
                    //greyscape. for non grey type. just put the same value on the try channels.
                    if (CalculatedPixel.length == 1) {
                        resultvalue
                                = (hasAlpha ? 0xFF : 0)
                                | CalculatedPixel[0] << 16
                                | CalculatedPixel[0] << 8
                                | CalculatedPixel[0];
                    } else {
                        //ensure we put the data in the right order for the type
                        //using the translation
                        resultvalue
                                = (hasAlpha ? CalculatedPixel[Translation[ALPHA]] << 24 : 0)
                                | CalculatedPixel[Translation[RED]] << 16
                                | CalculatedPixel[Translation[GREEN]] << 8
                                | CalculatedPixel[Translation[BLUE]];
                    }
                    //IntegerData.getData()[i]= resultvalue;
                    IntegerData.setElem(i, resultvalue);
                }
                default -> {
                    var point = getPointForIndex(ResultImage.getWidth(), i);
                    //slow but on this function should not happend. we will add this code mostly for example on how to do if this where the case.
                    if (CalculatedPixel.length == 1) {
                        ResultImage.setRGB(point.x, point.y,
                                CalculatedPixel[0] << 24
                                | CalculatedPixel[0] << 16
                                | CalculatedPixel[0] << 8
                                | CalculatedPixel[0]
                        );
                    } else {
                        ResultImage.setRGB(point.x, point.y,
                                CalculatedPixel[ALPHA] << 24
                                | CalculatedPixel[RED] << 16
                                | CalculatedPixel[GREEN] << 8
                                | CalculatedPixel[BLUE]
                        );
                    }
                }

            }
        }
        ResultImage.flush();
        return ResultImage;
    }

    public void MathOnPixelsbyIndex(BiConsumer<Short[], Integer> MathConsumer) {
        for (int i = 0; i < getTotalPixels(); i++) {
            MathConsumer.accept(getRGB(i), i);
        }
    }

    /**
     * this function execute the provided "math" functionality into the pixel
     * data for the image. and returns a image with the resulting data from the
     * function. for each pixel.
     *
     * @param TypeRequred the type of image is desired as results. thus function
     * supports: <pre>
     * {@link BufferedImage#TYPE_INT_RGB}
     * {@link BufferedImage#TYPE_INT_ARGB}
     * {@link BufferedImage#TYPE_INT_ARGB_PRE}
     * {@link BufferedImage#TYPE_INT_BGR}
     * </pre>
     *
     * @param MathFunction a {@link Function} that accepts and returns an array
     * of shorts values. the input array is an array of {@link Short} type
     * values of size {@code 4} in the order this class works with. see: null
     * null null null null null null null null null null null null null null     {@link CanvasContainer#ALPHA},
     * {@link CanvasContainer#RED},
     * {@link CanvasContainer#GREEN},
     * {@link CanvasContainer#BLUE}. the resulting array is also Expected that
     * return a array in the same Order (ARGB) and size. with the resulting data
     * with a Single Exception: {@link BufferedImage#TYPE_BYTE_GRAY} that will
     * accept an single value array.<br>
     * <strong>NOTE:</strong>no matter the type of image provided this function
     * expects and uses arrays with the ARGB order. no matter if the type is BGR
     * as this function handles the translations.
     * @return a image that contain the changes to the pixels done via the
     * provided function.
     */
    public BufferedImage MathOnPixelInt(int TypeRequred, Function<Integer, Integer> MathFunction) {
        //note if provided a unsupported type we could use whatever we want... or throw a error.
        BufferedImage ResultImage = null;
        switch (TypeRequred) {
            default ->
                throw new UnsupportedOperationException(String.format("%s: %d", "the specific Type of image is not Supported", TypeRequred));
            case BufferedImage.TYPE_INT_RGB, BufferedImage.TYPE_INT_ARGB, BufferedImage.TYPE_INT_ARGB_PRE, BufferedImage.TYPE_INT_BGR ->
                ResultImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), TypeRequred);
        }
        var Destinationdatabuffer = (DataBufferInt) ResultImage.getRaster().getDataBuffer();
        for (int i = 0; i < getTotalPixels(); i++) {
            var rgb = getRGB(i);
            int rgbint = rgb[ALPHA] << 24 | rgb[RED] << 16 | rgb[GREEN] << 8
                    | rgb[BLUE];
            var CalculatedPixel = MathFunction.apply(rgbint);
            if (TypeRequred == BufferedImage.TYPE_INT_BGR) {
                var reversed = CalculatedPixel & 0xFF00FF00;//ALPHA AND GREEN ARE ON THE SAME PLACE
                reversed |= (CalculatedPixel >>> 16) & 0xFF;
                reversed |= (CalculatedPixel & 0xFF) << 16;
                CalculatedPixel = reversed;
            }
            Destinationdatabuffer.setElem(i, CalculatedPixel);
        }
        ResultImage.flush();
        return ResultImage;
    }

    /**
     * returns the ARGB data for the desired position. if the
     * {@code LinearPosition} is not known. you can call {@link CanvasContainer#getRGB(int, int)
     * }
     * with the X,Y coordinates
     *
     * @param LinearPosition the linear position on the Image where to gather
     * the color info.
     * @return and array that contains the data for ARGB data (in the order
     * define by this class (aRGB))
     * @see CanvasContainer#ALPHA
     * @see CanvasContainer#RED
     * @see CanvasContainer#GREEN
     * @see CanvasContainer#BLUE
     */
    public Short[] getRGB(int LinearPosition) {
        if (LinearPosition < 0 && LinearPosition > getTotalPixels()) {
            throw new ArrayIndexOutOfBoundsException("the index Specified is not present on the image");
        }
        var result = new Short[4];
        result[BLUE] = (short) getBlue(LinearPosition);
        result[GREEN] = (short) getGreen(LinearPosition);
        result[RED] = (short) getRed(LinearPosition);
        result[ALPHA] = (short) getAlpha(LinearPosition);
        return result;
    }

    public Short[] getRGB(int x, int y) {
        var position = getIndexForPosition(originalImage.getWidth(), x, y);
        return getRGB(position);
    }

    /**
     * gathers the Color information for the specified position
     *
     * @param Channel the color channel that most be one of the following:      <pre>
     * {@link CanvasContainer#ALPHA}
     * {@link CanvasContainer#RED}
     * {@link CanvasContainer#GREEN}
     * {@link CanvasContainer#BLUE}
     * </pre>
     *
     * @param x the X axis.
     * @param y the Y axis.
     * @return the color information as a integer value that is between 0 and
     * 0xFF(255);
     */
    private int getColor(int Channel, int x, int y) {
        if (Channel == ALPHA && originalImage.getAlphaRaster() == null) {
            return MAXUBYTE;//we will not check or compute. is a waste. if no alpha is fully opaque.
        }
        var dataArrayObject = ImageDataReference==null?setupDataBuffer():ImageDataReference;
        var i = getIndexForPosition(originalImage.getWidth(), x, y);
        int readvalue;
        switch (dataArrayObject) {
            case byte[] bytesData ->
                readvalue = Byte.toUnsignedInt(getColorPixelByte(originalImage.getType(), HasAlphaChannel(), bytesData, Channel, i));
            case int[] IntegerData ->
                readvalue = getColorPixelInt(originalImage.getType(), HasAlphaChannel(), IntegerData, Channel, i);
            default -> {
                readvalue = getColorDefaultMethod(Channel, x, y);
            }
        }
        return readvalue;
    }

    private int getColor(int Channel, int LinearPosition) {
        if (Channel == ALPHA && originalImage.getAlphaRaster() == null) {
            return MAXUBYTE;//the image is fully opaque. 
        }
        var dataArrayObject = ImageDataReference==null?setupDataBuffer():ImageDataReference;
        int readvalue;
        switch (dataArrayObject) {
            case byte[] bytesData ->
                readvalue = convertToUnsigned(getColorPixelByte(originalImage.getType(), HasAlphaChannel(), bytesData, Channel, LinearPosition));
            case int[] IntegerData ->
                readvalue = getColorPixelInt(originalImage.getType(), HasAlphaChannel(), IntegerData, Channel, LinearPosition);
            default -> {
                var pos = getPointForIndex(originalImage.getWidth(), LinearPosition);
                readvalue = getColorDefaultMethod(Channel, pos.x, pos.y);
            }

        }
        return readvalue;
    }

    private int getColorDefaultMethod(int Channel, int x, int y) {
        int readvalue;
        var data = originalImage.getRaster().getDataElements(x, y, null);
        switch (Channel) {
            case ALPHA ->
                readvalue = originalImage.getColorModel().getAlpha(data);
            case RED ->
                readvalue = originalImage.getColorModel().getRed(data);
            case GREEN ->
                readvalue = originalImage.getColorModel().getGreen(data);
            case BLUE ->
                readvalue = originalImage.getColorModel().getBlue(data);
            //not posible. due the first if.
            default ->
                throw new IndexOutOfBoundsException(String.format("Invalid Channel %d", Channel));
        }
        return readvalue;
    }

    // <editor-fold defaultstate="collapsed" desc="public get Color functions">
    public int getAlpha(int LinearPosition) {
        return getColor(ALPHA, LinearPosition);
    }

    public int getAlpha(int x, int y) {
        return getColor(ALPHA, x, y);
    }

    public int getRed(int LinearPosition) {
        return getColor(RED, LinearPosition);
    }

    public int getRed(int x, int y) {
        return getColor(RED, x, y);
    }

    public int getGreen(int LinearPosition) {
        return getColor(GREEN, LinearPosition);
    }

    public int getGreen(int x, int y) {
        return getColor(GREEN, x, y);
    }

    public int getBlue(int LinearPosition) {
        return getColor(BLUE, LinearPosition);
    }

    public int getBlue(int x, int y) {
        return getColor(BLUE, x, y);
    }
    // </editor-fold>

    /**
     * this method provides a copy or clone of the original image that was
     * loaded into it. we do NOT provide the original image as BufferedImage are
     * not immutable. and are easy to edit. this class is intended to ensure the
     * analytical functions to be executed on the original image from the source
     * and thus giving access to it might poison the image.
     *
     * @return a copy(new instance) of the original image.
     */
    BufferedImage getCloneImage() {
        return getCloneofImage(originalImage);
    }

    private static BufferedImage getCloneofImage(BufferedImage original) {
        //https://stackoverflow.com/questions/3514158/how-do-you-clone-a-bufferedimage
        //for other few methods that could be used. 
        var clone = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());
        var g = clone.getGraphics();
        g.drawImage(original, 0, 0, null);
        g.dispose();
        return clone;
    }

    BufferedImage getBlueForIndex(int Index, Color FillColor) {
        return getColorForIndex(Index, BLUE, FillColor);
    }

    BufferedImage getGreenForIndex(int Index, Color FillColor) {
        return getColorForIndex(Index, GREEN, FillColor);
    }

    BufferedImage getRedForIndex(int Index, Color FillColor) {
        return getColorForIndex(Index, RED, FillColor);
    }

    BufferedImage getAlphaForIndex(int Index, Color FillColor) {
        return getColorForIndex(Index, ALPHA, FillColor);
    }

    //TODO:: this is a slow implementation due Syncronized DAta load inside a loop. 
    //We need to either invert that (get the data prior the loop) or cache the data. 
    //the issue is also present in Math Pixels. 
    BufferedImage getColorForIndex(int Index, int Channel, Color FillColor) {
        if (Channel < 0 || Channel > BLUE) {
            throw new ArrayIndexOutOfBoundsException("Invalid Channel");
        }
        if (Index < 0 || Index >= 8) {
            throw new ArrayIndexOutOfBoundsException("the index(bit) Specified is not present on the image");
        }
        var image = createBINoAlphaemptyCopy();//and RGB image
        var hasAlphaChannel = HasAlphaChannel();
        if (Channel == ALPHA && !hasAlphaChannel) {
            //if this image has no alpha channel then it means if it were to add one it will be fully opaque
            //and thus for this specific case. it fills the data and thus a fully opaque image
            var databuff = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
            Arrays.fill(databuff, FillColor.getRGB());
            image.flush();
            return image;
        }
        //here if needs be we could fill the new image with white pixels. or something... 
        var Destinationdatabuffer = (DataBufferInt) image.getRaster().getDataBuffer();//rgb is int. thus. 
        var dataArrayObject = ImageDataReference==null?setupDataBuffer():ImageDataReference;
        for (int i = 0; i < getTotalPixels(); i++) {//Should we do the loop once we know the type of buffer and avoid 1 computation?
            int readvalue = 0b0;
            switch (dataArrayObject) {
                case byte[] bytesData ->
                    readvalue = convertToUnsigned(getColorPixelByte(originalImage.getType(), hasAlphaChannel, bytesData, Channel, i));
                case int[] IntegerData ->
                    readvalue = getColorPixelInt(originalImage.getType(), hasAlphaChannel, IntegerData, Channel, i);
                default -> {
                    var pos = getPointForIndex(originalImage.getWidth(), i);
                    var data = originalImage.getRaster().getDataElements(pos.x, pos.y, null);
                    switch (Channel) {
                        case ALPHA ->
                            readvalue = originalImage.getColorModel().getAlpha(data);
                        case RED ->
                            readvalue = originalImage.getColorModel().getRed(data);
                        case GREEN ->
                            readvalue = originalImage.getColorModel().getGreen(data);
                        case BLUE ->
                            readvalue = originalImage.getColorModel().getBlue(data);
                    }
                }

            }
            //To consider. maybe dont set any color if not found. allow whatever is default on the provided image. 
            var CalculatedPixel = ((readvalue >>> Index) & 0b1) == 0b0 ? RGBMASK : FillColor.getRGB();
            Destinationdatabuffer.setElem(i, CalculatedPixel);
        }
        image.flush();
        return image;
    }

    /**
     * gets a Copy of the image converted into gray scale.
     *
     * @return a Grayscale image.
     */
    public BufferedImage getGrayScale() {
        var transform = createBIemptyCopy(BufferedImage.TYPE_BYTE_GRAY);
        var g = transform.getGraphics();
        g.drawImage(originalImage, 0, 0, null);
        g.dispose();
        return transform;
    }

    /**
     * Create a Buffer Image that contains pixels with the FillColor where the
     * specific pixel data r=g=b. for example. if a pixel color is all 0 (black)
     * or White (all 1) or any color that for all color the data is the same for
     * example. "1f1f1f" and so on. Highlights just the pixels for which r=g=b
     * (this is also known as "gray bits" because all R G B are the same value)
     *
     * @param Fill the color to fill for those pixel that match the Symetry.
     * @return a BufferImage that contains the RGB data (we don't guarantee the
     * type of image the image could be {@link BufferedImage#TYPE_3BYTE_BGR} or
     * {@link BufferedImage#TYPE_INT_RGB} or any other.) that match that
     * criteria.
     * @see
     * {@link https://web.stanford.edu/class/cs101/image-6-grayscale-adva.html}
     *
     */
    public BufferedImage getSymetricPixels(Color Fill) {
        var image = createBIemptyCopy(BufferedImage.TYPE_BYTE_GRAY);//note We could just return a binary image
        var Destinationdatabuffer = (DataBufferByte) image.getRaster().getDataBuffer();
        var dataArrayObject = ImageDataReference==null?setupDataBuffer():ImageDataReference;
        /*fill the image with empty "canvas color" */
        Arrays.fill(Destinationdatabuffer.getData(), (byte) 0xFF);
        switch (dataArrayObject) {
            case byte[] bytesData -> {
                var hasAlphaChannel = HasAlphaChannel();
                DrawSymetricBytes(originalImage.getType(), image.getType(), hasAlphaChannel, image.getAlphaRaster() != null, bytesData, Destinationdatabuffer, Fill);
            }
            case int[] IntegerData ->
                DrawSymetricInt(originalImage.getType(), image.getType(), image.getAlphaRaster() != null, IntegerData, Destinationdatabuffer, Fill);
            default -> {
                var rgbfill = getRGBArray(Fill);
                var Destdata = Destinationdatabuffer.getData();
                for (int i = 0; i < getTotalPixels(); i++) {//Should we do the loop once we know the type of buffer and avoid 1 computation?
                    var pos = getPointForIndex(originalImage.getWidth(), i);
                    var data = originalImage.getRaster().getDataElements(pos.x, pos.y, null);
                    var green = originalImage.getColorModel().getGreen(data);
                    var same
                            = originalImage.getColorModel().getRed(data) == green
                            && green == originalImage.getColorModel().getBlue(data);
                    if (same) {
                        var baseindex = getRawIndexForImageIndex(3, i);
                        Destdata[baseindex] = rgbfill[2];
                        //Destdata[baseindex + 1] = rgbfill[1];
                        //Destdata[baseindex + 2] = rgbfill[0];
                    }
                }
            }

        }
        image.flush();
        return image;
    }

    // <editor-fold defaultstate="collapsed" desc="Image For channel">
    /**
     * returns a image copy of the original image that contains only the data
     * for the blue channel.
     *
     * @return a BufferedImage with only the Blue Channel. the image is RGB. but
     * only with the Blue data.
     */
    public BufferedImage getBlueImage() {
        return getImageForChannel(false, BLUE);
    }

    /**
     * returns a image copy of the original image that contains only the data
     * for the Alpha channel. (in a visible format.)
     *
     * @return a BufferedImage with only the Blue Channel. the image is RGB. but
     * only with the alpha data. shifted into a visible plane (either red or
     * blue channel)
     */
    public BufferedImage getAlphaImage() {
        return getImageForChannel(false, ALPHA);
    }

    /**
     * returns a image copy of the original image that contains only the data
     * for the green channel.
     *
     * @return a BufferedImage with only the Blue Channel. the image is RGB. but
     * only with the green data.
     */
    public BufferedImage getGreenImage() {
        return getImageForChannel(false, GREEN);
    }

    /**
     * returns a image copy of the original image that contains only the data
     * for the Red channel.
     *
     * @return a BufferedImage with only the Blue Channel. the image is RGB. but
     * only with the Red data.
     */
    public BufferedImage getRedImage() {
        return getImageForChannel(false, RED);
    }

    /**
     * Creates a new BufferedImage for the Specified Channel consistent of only
     * the data from that specific channel. the resulting image can be Grayscale
     * OR Color.
     *
     * @param GrayImage if the returning image is to be gray scale.
     * @param Channel the channel to create a Buffer image.
     * @return a BufferedImage with the information of a single channel.
     */
    BufferedImage getImageForChannel(boolean GrayImage, int Channel) {
        var hasAlphaChannel = HasAlphaChannel();
        if (Channel == ALPHA && !hasAlphaChannel) {
            var image = createBIemptyCopy(BufferedImage.TYPE_BYTE_GRAY);//note We could just return a binary image
            var databuff = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
            Arrays.fill(databuff, (byte) 0);
            image.flush();
            return image;
        }
        var image = GrayImage ? createBIemptyCopy(BufferedImage.TYPE_BYTE_GRAY) : createBIemptyCopy(BufferedImage.TYPE_3BYTE_BGR);
        var Destinationdatabuffer = (DataBufferByte) image.getRaster().getDataBuffer();
        final var dataArrayObject = ImageDataReference==null?setupDataBuffer():ImageDataReference;        
        //NOTE: Alpha channel will become visible on the "blue" or Red channel (if the image is not grey)
        switch (dataArrayObject) {
            case byte[] bytesData ->
                cloneChannelBytes(originalImage.getType(), image.getType(), hasAlphaChannel, false, bytesData, Destinationdatabuffer, Channel);
            case int[] IntegerData ->
                cloneChannelInt(originalImage.getType(), image.getType(), hasAlphaChannel, false, IntegerData, Destinationdatabuffer, Channel);
            default -> {
                var Destdata = Destinationdatabuffer.getData();
                cloneChannelDefault(Channel, originalImage, getTotalPixels(), image.getType(), Destdata);
            }
        }
        image.flush();
        return image;
    }
    // </editor-fold>
}
