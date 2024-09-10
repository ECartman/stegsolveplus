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

    /**
     * the max value for a single color Channel 0xFF or (255) the max value of
     * an unsigned Byte.
     */
    static final int MAXUBYTE = 0xFF;
    /**
     * a hex mask to gather the Alpha level.
     */
    static final int ALPHA_MASK = 0xFF000000;
    /**
     * a mask to gather the RGB composed value.
     */
    static final int RGBMASK = 0x00FFFFFF;

    /**
     * Constants to read from the ARGB data
     */
    static final int ALPHA = 0,
            RED = 1,
            GREEN = 2,
            BLUE = 3;
    /**
     * The Base Image for all combinations or calculations. this image is prone
     * to be changed. due the nature of {@link BufferedImage} thus to avoid
     * change this image is Final and Is NEVER to leave this class. if a caller
     * needs the "original" provide a copy.
     */
    private final BufferedImage originalImage;

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
     * returns true if the image is valid.
     *
     * @return whenever the image is valid or not.
     */
    public boolean ImageIsValid() {
        return Objects.nonNull(originalImage);
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
     * <br> null null null     {@link BufferedImage#TYPE_BYTE_GRAY}
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
    private static int[] getOrder(int type) {
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

    private static int getTranslationIndexfor(int type, int channel) {
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
        }
    }

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
    private static Point RelativiseLinearIndexToXY(final int Width, final int index) {
        return new Point(index % Width, index / Width);
    }

    private static int getIndexForPosition(final int Width, int x, int y) {
        return y * Width + x;
    }

    private static int getIndexForPosition(final int Width, Point p) {
        return p.y * Width + p.x;
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

    /**
     * gather the information from the image at the desired {@code Index} from
     * the {@code bytesData} for the desired Color Channel {@code channel}
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
     * <pre>
     * @param Index the index from which look the color data at the {@code DataBufferByte}
     * @return the byte value of the desired index. for the specified channel. a
     * value between 0 and 266 (you need to call
     * {@link CanvasContainer#convertToUnsigned(byte)}
     */
    private static byte getColorPixelByte(int type, final boolean hasAlpha, final DataBufferByte bytesData, int channel, int Index) {
        var order = getOrder(type);//this will crash if not found. that is desireable as we want to fix that problem. see the To do's on getOrder
        var byteData = bytesData.getData();//alternative we can use bytesData.getSize() instead of the lenght and call .getElem to get the value. but note this taxes on byte to int convertion
        final int BytesPerPixel = hasAlpha ? 4 : 3;
        final int jumpPerPixel = BytesPerPixel - 1;
        //on this case we are not looping the data rather we know the index we want. 
        //HOWEVER the index provided is the index for the pixel. not the raw data. 
        //and thus we need to do some math to conver the index from a image position
        //into the raw bytes array
        var ConvertedIndex = getRawIndexForImageIndex(BytesPerPixel, Index);
        if (channel == order[0]) {
            return hasAlpha ? byteData[ConvertedIndex] : (byte) MAXUBYTE;//full alpha (opaque) if has not alpha
        } else if (channel == order[1]) {
            return byteData[ConvertedIndex + jumpPerPixel - 2];
        } else if (channel == order[2]) {
            return byteData[ConvertedIndex + jumpPerPixel - 1];
        } else if (channel == order[3]) {
            return byteData[ConvertedIndex + jumpPerPixel];
        } else {
            return 0;
        }
    }

    private static void cloneChannelBytes(int SourceType, int DestinationType, boolean hasAlpha, boolean destHasAlpha, DataBufferByte bytesData, DataBufferByte Destinationdatabuffer, int Channel) {
        final int srcBytesPerPixel = hasAlpha ? 4 : 3;
        final int destBytesPerPixel = destHasAlpha ? 4 : 3;
        final int jumpPerPixel = srcBytesPerPixel - 1;
        var byteData = bytesData.getData();
        var DestbyteData = Destinationdatabuffer.getData();
        var srcTranslatedChannel = getTranslationIndexfor(SourceType, Channel);
        var destTranslatedChannel = getTranslationIndexfor(DestinationType, Channel);
        if (hasAlpha && destHasAlpha && Channel == ALPHA) {
            //maybe but slower
            //destTranslatedChannel=getTranslationIndexfor(BLUE, Channel);
            destTranslatedChannel++; //make alpha visible if there is alpha channel
        }
        for (int Sourceindex = 0, destindex = 0; Sourceindex + jumpPerPixel < byteData.length; Sourceindex += srcBytesPerPixel, destindex += destBytesPerPixel) {
            DestbyteData[destindex + destTranslatedChannel] = byteData[Sourceindex + srcTranslatedChannel];
        }
    }

    private static void DrawSymetricBytes(int Type, int DestType, boolean srcHasAlpha, boolean DestHasAlpha, DataBufferByte SrcBuffer, DataBufferByte destBuffer, Color Fill) {
        CanvasContainer.DrawSymetricBytes(Type, DestType, srcHasAlpha, DestHasAlpha, SrcBuffer, destBuffer, getRGBArray(Fill));
    }

    private static void DrawSymetricBytes(int Type, int DestType, boolean srcHasAlpha, boolean DestHasAlpha, DataBufferByte SrcBuffer, DataBufferByte destBuffer, byte[] RGBfill) {
        final int FirstBytesPerPixel = srcHasAlpha ? 4 : 3;
        final int SecondBytesPerPixel = DestHasAlpha ? 4 : 3;
        final int jumpPerPixel = FirstBytesPerPixel - 1;
        var SourceData = SrcBuffer.getData();
        var destData = destBuffer.getData();
        var SecondTranslationOrder = getOrder(DestType);
        for (int srcIndex = 0, SecondIndex = 0; srcIndex + jumpPerPixel < SourceData.length; srcIndex += FirstBytesPerPixel, SecondIndex += SecondBytesPerPixel) {
            var same = SourceData[srcIndex + jumpPerPixel] == SourceData[srcIndex + jumpPerPixel - 1]
                    && SourceData[srcIndex + jumpPerPixel - 1] == SourceData[srcIndex + jumpPerPixel - 2];
            if (same) {
                //put this pixel as color 
                destData[SecondIndex + SecondBytesPerPixel - 3] = RGBfill[SecondTranslationOrder[RED] - 1];//we -1 as this does not have alpha index thus 'red'is 0 instead of 1 
                destData[SecondIndex + SecondBytesPerPixel - 2] = RGBfill[SecondTranslationOrder[GREEN] - 1];
                destData[SecondIndex + SecondBytesPerPixel - 1] = RGBfill[SecondTranslationOrder[BLUE] - 1];
            }
        }
    }

    private static void DrawSymetricInt(int type, int DestType, boolean DestHasAlpha, DataBufferInt SrcBuffer, DataBufferByte destBuffer, Color Fill) {
        CanvasContainer.DrawSymetricInt(type, DestType, DestHasAlpha, SrcBuffer, destBuffer, getRGBArray(Fill));
    }

    private static void DrawSymetricInt(int type, int DestType, boolean DestHasAlpha, DataBufferInt SrcBuffer, DataBufferByte destBuffer, byte[] RGBfill) {
        final int SecondBytesPerPixel = DestHasAlpha ? 4 : 3;
        var SourceData = SrcBuffer.getData();
        var destData = destBuffer.getData();
        var SecondTranslationOrder = getOrder(DestType);
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

    private void cloneChannelInt(int SourceType, int DestinationType, boolean hasAlpha, boolean destHasAlpha, DataBufferInt IntegersData, DataBufferByte Destinationdatabuffer, int Channel) {
        final int destBytesPerPixel = destHasAlpha ? 4 : 3;
        var SrcData = IntegersData.getData();
        var DestbyteData = Destinationdatabuffer.getData();
        var srcTranslatedChannel = getTranslationIndexfor(SourceType, Channel);
        var destTranslatedChannel = getTranslationIndexfor(DestinationType, Channel);
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

    private static int getColorPixelInt(int type, final boolean hasAlpha, final DataBufferInt intData, int channel, int Index) {
        var order = getOrder(type);//this will crash if not found. that is desireable as we want to fix that problem. see the To do's on getOrder
        var pixels = intData.getData();//alternative we can use intData.getSize() instead of the lenght and call .getElem to get the value. but note this taxes on byte to int convertion
        if (channel == order[0]) {
            return hasAlpha ? ((pixels[Index] & ALPHA_MASK) >>> 24) : (byte) MAXUBYTE;//full alpha (opaque) if has not alpha
        } else if (channel == order[1]) {
            return ((pixels[Index] >>> 16) & MAXUBYTE);
        } else if (channel == order[2]) {
            return ((pixels[Index] >>> 8) & MAXUBYTE);
        } else if (channel == order[3]) {
            return (pixels[Index] & MAXUBYTE);
        } else {
            return 0;
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
    private static void processIntData(int type, final boolean hasAlpha, final DataBufferInt intData, byte[][] Colormaps) {
        var order = getOrder(type);//this will crash if not found. that is desireable as we want to fix that problem. see the To do's on getOrder
        var pixels = intData.getData();//alternative we can use intData.getSize() instead of the lenght and call .getElem to get the value. but note this taxes on byte to int convertion
        for (int pixel = 0; pixel < pixels.length; pixel++) {
            //Colormaps[order[0]][pixel] = hasAlpha ? (byte) (( intData.getElem(pixel) & ALPHA_MASK) >>> 24) : (byte) MAXSINGLEVALUE;//full alpha (opaque) if has not alpha
            Colormaps[order[0]][pixel] = hasAlpha ? (byte) ((pixels[pixel] & ALPHA_MASK) >>> 24) : (byte) MAXUBYTE;//full alpha (opaque) if has not alpha
            Colormaps[order[1]][pixel] = (byte) ((pixels[pixel] >>> 16) & MAXUBYTE);
            Colormaps[order[2]][pixel] = (byte) ((pixels[pixel] >>> 8) & MAXUBYTE);
            Colormaps[order[3]][pixel] = (byte) (pixels[pixel] & MAXUBYTE);
        }
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
        var image = createBIemptyCopy(BufferedImage.TYPE_3BYTE_BGR);//note We could just return a binary image
        var Destinationdatabuffer = (DataBufferByte) image.getRaster().getDataBuffer();
        var databuffer = originalImage.getRaster().getDataBuffer();
        /*fill the image with empty "canvas color" */
        Arrays.fill(Destinationdatabuffer.getData(), (byte) 0xFF);
        switch (databuffer) {
            case DataBufferByte bytesData -> {
                var hasAlphaChannel = originalImage.getAlphaRaster() != null;
                DrawSymetricBytes(originalImage.getType(), image.getType(), hasAlphaChannel, image.getAlphaRaster() != null, bytesData, Destinationdatabuffer, Fill);
            }
            case DataBufferInt IntegerData ->
                DrawSymetricInt(originalImage.getType(), image.getType(), image.getAlphaRaster() != null, IntegerData, Destinationdatabuffer, Fill);
            default -> {
                var rgbfill = getRGBArray(Fill);
                var Destdata = Destinationdatabuffer.getData();
                for (int i = 0; i < getTotalPixels(); i++) {//Should we do the loop once we know the type of buffer and avoid 1 computation?
                    var pos = RelativiseLinearIndexToXY(originalImage.getWidth(), i);
                    var data = originalImage.getRaster().getDataElements(pos.x, pos.y, null);
                    var green = originalImage.getColorModel().getGreen(data);
                    var same
                            = originalImage.getColorModel().getRed(data) == green
                            && green == originalImage.getColorModel().getBlue(data);
                    if (same) {
                        var baseindex = getRawIndexForImageIndex(3, i);
                        Destdata[baseindex] = rgbfill[2];
                        Destdata[baseindex + 1] = rgbfill[1];
                        Destdata[baseindex + 2] = rgbfill[0];
                    }
                }
            }
        }
        image.flush();
        return image;
    }

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
     * values of size {@code 4} in the order this class works with. (see: null
     * null null null null null null     {@link CanvasContainer#ALPHA},
     * {@link CanvasContainer#RED},
     * {@link CanvasContainer#GREEN},
     * {@link CanvasContainer#BLUE}. the resulting array is also Expected that
     * return a array in the same Order and size. with the resulting data with a
     * Single Exception: {@link BufferedImage#TYPE_BYTE_GRAY} that will accept
     * an single value array.
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
        var Translation = getOrder(TypeRequred);
        var hasAlpha = ResultImage.getAlphaRaster() != null;
        for (int i = 0; i < getTotalPixels(); i++) {
            var CalculatedPixel = MathFunction.apply(ToWrapperArray(getRGB(i)));
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
                    if (CalculatedPixel.length == 1) {
                        resultvalue
                                = (hasAlpha ? CalculatedPixel[0] << 24 : 0)
                                | CalculatedPixel[0] << 16
                                | CalculatedPixel[0] << 8
                                | CalculatedPixel[0];
                    } else {
                        resultvalue
                                = (hasAlpha ? CalculatedPixel[ALPHA] << 24 : 0)
                                | CalculatedPixel[RED] << 16
                                | CalculatedPixel[GREEN] << 8
                                | CalculatedPixel[BLUE];
                    }
                    //IntegerData.getData()[i]= resultvalue;
                    IntegerData.setElem(i, resultvalue);
                }
                default -> {
                    var point = RelativiseLinearIndexToXY(ResultImage.getWidth(), i);
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

    public void MathOnPixels(BiConsumer<Short[], Point> MathConsumer) {
        for (int i = 0; i < getTotalPixels(); i++) {
            MathConsumer.accept(ToWrapperArray(getRGB(i)), RelativiseLinearIndexToXY(originalImage.getWidth(), i));
        }
    }
    
    public void MathOnPixelsbyIndex(BiConsumer<Short[], Integer> MathConsumer) {
        for (int i = 0; i < getTotalPixels(); i++) {
            MathConsumer.accept(ToWrapperArray(getRGB(i)),i);
        }
    }

    //TODO change to NOT use RGB!!
    /**
     * @deprecated this function requires upgrade to use read pixels faster.
     * @param Destination
     * @param MathFunction
     */
    public void MathOnPixelInt(BufferedImage Destination, Function<Integer, Integer> MathFunction) {
        for (int x = 0; x < originalImage.getWidth(); x++) {
            for (int y = 0; y < originalImage.getHeight(); y++) {
                var CalculatedPixel = MathFunction.apply(originalImage.getRGB(x, y));
                Destination.setRGB(x, y, CalculatedPixel);
            }
        }
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
    public short[] getRGB(int LinearPosition) {
        if (LinearPosition < 0 && LinearPosition > getTotalPixels()) {
            throw new ArrayIndexOutOfBoundsException("the index Specified is not present on the image");
        }
        var result = new short[4];
        result[BLUE] = (short) getBlue(LinearPosition);
        result[GREEN] = (short) getGreen(LinearPosition);
        result[RED] = (short) getRed(LinearPosition);
        result[ALPHA] = (short) getAlpha(LinearPosition);
        return result;
    }

    public short[] getRGB(int x, int y) {
        var position = getIndexForPosition(originalImage.getWidth(), x, y);
        return getRGB(position);
    }

    private Short[] ToWrapperArray(short[] b) {
        var result = new Short[b.length];
        Arrays.parallelSetAll(result, i -> b[i]);
        return result;
    }

    protected int getColor(int Channel, int x, int y) {
        if (Channel < ALPHA && Channel > BLUE) {
            return -1;//invalid
        }
        if (originalImage.getAlphaRaster() == null) {
            return MAXUBYTE;//the image is fully opaque. 
        }
        var databuffer = originalImage.getRaster().getDataBuffer();
        var i = getIndexForPosition(originalImage.getWidth(), x, y);
        int readvalue;
        switch (databuffer) {
            case DataBufferByte bytesData ->
                readvalue = convertToUnsigned(getColorPixelByte(originalImage.getType(), true, bytesData, Channel, i));
            case DataBufferInt IntegerData ->
                readvalue = getColorPixelInt(originalImage.getType(), true, IntegerData, Channel, i);
            default -> {
                readvalue = getColorDefaultMethod(Channel, x, y);
            }
        }
        return readvalue;
    }

    protected int getColorDefaultMethod(int Channel, int x, int y) {
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
                readvalue = originalImage.getColorModel().getBlue(data);
        }
        return readvalue;
    }

    protected int getColor(int Channel, int LinearPosition) {
        if (Channel < ALPHA && Channel > BLUE) {
            return -1;//invalid
        }
        if (Channel == ALPHA && originalImage.getAlphaRaster() == null) {
            return MAXUBYTE;//the image is fully opaque. 
        }
        var databuffer = originalImage.getRaster().getDataBuffer();
        int readvalue;
        switch (databuffer) {
            case DataBufferByte bytesData ->
                readvalue = convertToUnsigned(getColorPixelByte(originalImage.getType(), originalImage.getAlphaRaster() != null, bytesData, Channel, LinearPosition));
            case DataBufferInt IntegerData ->
                readvalue = getColorPixelInt(originalImage.getType(), originalImage.getAlphaRaster() != null, IntegerData, Channel, LinearPosition);
            default -> {
                var pos = RelativiseLinearIndexToXY(originalImage.getWidth(), LinearPosition);
                readvalue = getColorDefaultMethod(Channel, pos.x, pos.y);
            }
        }
        return readvalue;
    }

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

    /**
     * gets a Edited Copy of the image that only contain Grayscale from the
     * original image.
     *
     * @return a Grayscale image.
     */
    BufferedImage getGrayScale() {
        var transform = createBIemptyCopy(BufferedImage.TYPE_BYTE_GRAY);
        var g = transform.getGraphics();
        g.drawImage(originalImage, 0, 0, null);
        g.dispose();
        return transform;
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
            throw new ArrayIndexOutOfBoundsException("the index(bit) Specified is not present on the image");
        }
        var image = createBINoAlphaemptyCopy();//and RGB image
        var hasAlphaChannel = originalImage.getAlphaRaster() != null;
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
        var databuffer = originalImage.getRaster().getDataBuffer();
        for (int i = 0; i < getTotalPixels(); i++) {//Should we do the loop once we know the type of buffer and avoid 1 computation?
            int readvalue = 0b0;
            switch (databuffer) {
                case DataBufferByte bytesData ->
                    readvalue = convertToUnsigned(getColorPixelByte(originalImage.getType(), hasAlphaChannel, bytesData, Channel, i));
                case DataBufferInt IntegerData ->
                    readvalue = getColorPixelInt(originalImage.getType(), hasAlphaChannel, IntegerData, Channel, i);
                default -> {
                    var pos = RelativiseLinearIndexToXY(originalImage.getWidth(), i);
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
        var hasAlphaChannel = originalImage.getAlphaRaster() != null;
        if (Channel == ALPHA && !hasAlphaChannel) {
            //if this image has no alpha channel then it means if it were to add one it will be fully opaque
            var image = createBIemptyCopy(BufferedImage.TYPE_INT_RGB);//note We could just return a binary image
            var databuff = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
            Arrays.fill(databuff, 0);
            image.flush();
            return image;
        }
        var image = createBIemptyCopy(BufferedImage.TYPE_3BYTE_BGR);//note We could just return a binary image
        var Destinationdatabuffer = (DataBufferByte) image.getRaster().getDataBuffer();//rgb is int. thus. 
        var databuffer = originalImage.getRaster().getDataBuffer();
        //NOTE: Alpha channel will become visible on the "blue" or Red channel
        //(depending on the order of the bytes and colors)
        //if we need to recreate the data. we will neeed a diferent function. 
        switch (databuffer) {
            case DataBufferByte bytesData ->
                cloneChannelBytes(originalImage.getType(), image.getType(), hasAlphaChannel, image.getAlphaRaster() != null, bytesData, Destinationdatabuffer, Channel);
            case DataBufferInt IntegerData ->
                cloneChannelInt(originalImage.getType(), image.getType(), hasAlphaChannel, image.getAlphaRaster() != null, IntegerData, Destinationdatabuffer, Channel);
            default -> {
                //this is slow. but given that we dont know how to handle otheise nothing we can do
                var Destdata = Destinationdatabuffer.getData();
                for (int i = 0; i < getTotalPixels(); i++) {
                    var pos = RelativiseLinearIndexToXY(originalImage.getWidth(), i);
                    var data = originalImage.getRaster().getDataElements(pos.x, pos.y, null);
                    var baseindex = getRawIndexForImageIndex(3, i);
                    switch (Channel) {
                        case ALPHA -> {
                            Destdata[baseindex + 2] = (byte) originalImage.getColorModel().getAlpha(data);
                        }
                        case RED -> {
                            Destdata[baseindex + 2] = (byte) originalImage.getColorModel().getRed(data);
                        }
                        case GREEN ->
                            Destdata[baseindex + 1] = (byte) originalImage.getColorModel().getGreen(data);
                        case BLUE -> {
                            Destdata[baseindex] = (byte) originalImage.getColorModel().getBlue(data);
                        }
                    }
                    //var p = RelativiseLinearIndexToXY(image.getWidth(), i);
                    //image.setRGB(p.x, p.y, singleChannelColor);
                }
            }
        }
        return image;
    }

}
