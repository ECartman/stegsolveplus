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
import java.util.Arrays;
import java.util.Objects;
import java.io.IOException;
import java.util.function.Function;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferByte;
import java.util.function.BiConsumer;
import com.aeongames.edi.utils.visual.ImageUtils;

/**
 * we need to decouple this class. it handles transformations and the image
 * itself we have migrated to WrappedImage to contain the image. and for know
 * this function handle. "developing" facets of the image into other images.
 * this Class works as a that holds the Image from a file,url,Stream or a
 * provided original image this class will hold the image and will not modify,
 * but will not provide a reference to it. because otherwise the image is easily
 * editable and we desire to hold unedited as much as possible for our forensic
 * analysis if you ask this class for the original image the best we do is
 * provide a copy of the original image. any morph or transformation we do is
 * done into a new image where we copy the data of the original image (Dependent
 * on which transformation or math is as to do) this class also has functions
 * for those transformations or analysis.
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
     * The Base Image for all combinations or calculations. this image is prone
     * to be changed. due the nature of {@link BufferedImage} thus to avoid
     * change this image is Final and Is NEVER to leave this class. if a caller
     * needs the "original" provide a copy.
     */
    private final WrappedImage originalImageHolder;

    // <editor-fold defaultstate="collapsed" desc="Constructors">
    /**
     * Package Private constructor. creates a new instance of CanvasContainer
     * loading the Image from the Source.
     *
     * @param Source the Path where the file is stored. cannot be null
     * @throws IOException if the file fails to load.
     */
    CanvasContainer(WrappedImage image) throws IOException {
        originalImageHolder = Objects.requireNonNull(image, "the WrappedImage is null");
        check(originalImageHolder);
    }

    /**
     * checks for a Image null reference. if is throws a Wrapped
     * {@link NullPointerException} in a {@link IOException} this is done this
     * way as this is a error while READING the file and thus we can safely
     * consider a I/O error while the underline error is the ref is null. (this
     * is almost because the Param to BufferedImage constructor is null or (and
     * what we look for) there was no Reader to parse the file.
     *
     * @param image the reference to check if is null
     * @throws IOException if the reference is null.
     */
    private void check(WrappedImage image) throws IOException {
        if (!image.isImageLoaded()) {
            StringBuilder b = new StringBuilder("We cannot Read the Image,");
            b.append("From source: <");
            if (image.getFileSource() != null) {
                b.append(image.getFileSource().toString());
            } else {
                b.append(image.getURLSource().getPath());
            }
            b.append("> ");
            b.append("This error can be caused by either there is no supported Image reader for the URL/file or the URL/file is Not a image.");
            throw new IOException(new NullPointerException(b.toString()));
        }
    }

    // </editor-fold>
    
    /**
     * *
     * returns a white BufferedImage with the same Dimensions as the OG support
     * a limited set of image types:
     * <p>
     * Supported options:</p>
     * <ul>
     * <li>{@link java.awt.image.BufferedImage#TYPE_3BYTE_BGR} –
     * 3 bytes per pixel, BGR order</li>
     * <li>{@link java.awt.image.BufferedImage#TYPE_4BYTE_ABGR} –
     * 4 bytes per pixel, alpha + BGR order</li>
     * <li>{@link java.awt.image.BufferedImage#TYPE_4BYTE_ABGR_PRE} –
     * premultiplied alpha variant of {@code TYPE_4BYTE_ABGR}</li>
     * <li>{@link java.awt.image.BufferedImage#TYPE_INT_RGB} – 32‑bit integer
     * pixels, RGB color components</li>
     * <li>{@link java.awt.image.BufferedImage#TYPE_INT_ARGB} – 32‑bit integer
     * pixels, ARGB (with alpha)</li>
     * <li>{@link java.awt.image.BufferedImage#TYPE_INT_ARGB_PRE} –
     * premultiplied alpha variant of {@code TYPE_INT_ARGB}</li>
     * <li>{@link java.awt.image.BufferedImage#TYPE_INT_BGR} – 32‑bit integer
     * pixels, BGR color components</li>
     * <li>{@link java.awt.image.BufferedImage#TYPE_BYTE_GRAY} – 8‑bit grayscale
     * image</li>
     * </ul>
     *
     * @param TypeRequred the type of image should be one of the supported types
     * otherwise the function will throw a exception.
     * @return a empty image of the desired type.
     *
     * @see BufferedImage#TYPE_3BYTE_BGR
     * @see BufferedImage#TYPE_4BYTE_ABGR
     * @see BufferedImage#TYPE_4BYTE_ABGR_PRE
     * @see BufferedImage#TYPE_INT_RGB
     * @see BufferedImage#TYPE_INT_ARGB
     * @see BufferedImage#TYPE_INT_ARGB_PRE
     * @see BufferedImage#TYPE_INT_BGR
     * @see BufferedImage#TYPE_BYTE_GRAY
     */
    private BufferedImage getSupportedEmptyCanvas(int TypeRequred) {
        switch (TypeRequred) {
            case BufferedImage.TYPE_3BYTE_BGR:
            case BufferedImage.TYPE_4BYTE_ABGR:
            case BufferedImage.TYPE_4BYTE_ABGR_PRE:
            case BufferedImage.TYPE_INT_RGB:
            case BufferedImage.TYPE_INT_ARGB:
            case BufferedImage.TYPE_INT_ARGB_PRE:
            case BufferedImage.TYPE_INT_BGR:
            case BufferedImage.TYPE_BYTE_GRAY:
                return ImageUtils.createBIemptyCopy(originalImageHolder.getDimensions(), TypeRequred);
            default:
                throw new UnsupportedOperationException(String.format("%s: %d", "the specific Type of image is not Supported", TypeRequred));
        }
    }

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
                order = new int[]{ImageColorUtilities.ALPHA,ImageColorUtilities.RED, ImageColorUtilities.GREEN, ImageColorUtilities.BLUE};
            case BufferedImage.TYPE_INT_BGR, BufferedImage.TYPE_3BYTE_BGR, BufferedImage.TYPE_4BYTE_ABGR, BufferedImage.TYPE_4BYTE_ABGR_PRE ->
                order = new int[]{ImageColorUtilities.ALPHA, ImageColorUtilities.BLUE, ImageColorUtilities.GREEN, ImageColorUtilities.RED};//basically is inverted or contrary Endianess
        }
        //TODO: support other types. 
        return order;
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

    private static int getRawIndexForImageIndex(int RawIndexPerImgIndex, int Index) {
        return RawIndexPerImgIndex * Index;
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
     * null null null null null null null null null null null null null null
     * null null null null null null null null null null null null null null null     {@link CanvasContainer#ALPHA},
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
     * provided function. TODO:: this class is consuming a lot of memory. due
     * big images transformations. we need to fix that...
     */
    public BufferedImage MathOnPixels(int TypeRequred, Function<Short[], Short[]> MathFunction) {
        //note if provided a unsupported type we could use whatever we want... or throw a error.
        BufferedImage ResultImage = getSupportedEmptyCanvas(TypeRequred);
        var Destinationdatabuffer = ResultImage.getRaster().getDataBuffer();
        //todo: move the for and add this inside the byte buffer case 
        var Translation = getColorOrder(TypeRequred);
        var hasAlpha = ResultImage.getAlphaRaster() != null;
        for (int i = 0; i < originalImageHolder.getTotalPixels(); i++) {
            var CalculatedPixel = MathFunction.apply(originalImageHolder.getRGB(i));
            switch (Destinationdatabuffer) {
                case DataBufferByte bytesData -> {
                    var byteData = bytesData.getData();//alternative we can use bytesData.getSize() instead of the lenght and call .getElem to get the value. but note this taxes on byte to int convertion
                    if (TypeRequred == BufferedImage.TYPE_BYTE_GRAY) {
                        if (CalculatedPixel.length == 1) {
                            byteData[i] = CalculatedPixel[0].byteValue();
                        } else {
                            byteData[i] = CalculatedPixel[ImageColorUtilities.RED].byteValue();
                        }
                    } else {
                        int BytesPerPixel = hasAlpha ? 4 : 3;
                        var ConvertedIndex = getRawIndexForImageIndex(BytesPerPixel, i);
                        final int jumpPerPixel = BytesPerPixel - 1;
                        byteData[ConvertedIndex] = CalculatedPixel[Translation[ImageColorUtilities.ALPHA]].byteValue();
                        byteData[ConvertedIndex + jumpPerPixel - 2] = CalculatedPixel[Translation[ImageColorUtilities.RED]].byteValue();
                        byteData[ConvertedIndex + jumpPerPixel - 1] = CalculatedPixel[Translation[ImageColorUtilities.GREEN]].byteValue();
                        byteData[ConvertedIndex + jumpPerPixel] = CalculatedPixel[Translation[ImageColorUtilities.BLUE]].byteValue();
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
                                = (hasAlpha ? CalculatedPixel[Translation[ImageColorUtilities.ALPHA]] << 24 : 0)
                                | CalculatedPixel[Translation[ImageColorUtilities.RED]] << 16
                                | CalculatedPixel[Translation[ImageColorUtilities.GREEN]] << 8
                                | CalculatedPixel[Translation[ImageColorUtilities.BLUE]];
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
                                CalculatedPixel[ImageColorUtilities.ALPHA] << 24
                                | CalculatedPixel[ImageColorUtilities.RED] << 16
                                | CalculatedPixel[ImageColorUtilities.GREEN] << 8
                                | CalculatedPixel[ImageColorUtilities.BLUE]
                        );
                    }
                }

            }
        }
        ResultImage.flush();
        return ResultImage;
    }

    public void MathOnPixelsbyIndex(BiConsumer<Short[], Integer> MathConsumer) {
        for (int i = 0; i < originalImageHolder.getTotalPixels(); i++) {
            MathConsumer.accept(originalImageHolder.getRGB(i), i);
        }
    }

    /**
     * this function execute the provided "math" functionality into the pixel
     * data for the image.and returns a image with the resulting data from the
     * function.for each pixel.
     *
     * @param requiredtype the type of image is desired as results. thus
     * function supports: <pre>
     * {@link BufferedImage#TYPE_INT_RGB}
     * {@link BufferedImage#TYPE_INT_ARGB}
     * {@link BufferedImage#TYPE_INT_ARGB_PRE}
     * {@link BufferedImage#TYPE_INT_BGR}
     * </pre>
     *
     * @param MathFunction a {@link Function} that accepts and returns an array
     * of shorts values. the input array is an array of {@link Short} type
     * values of size {@code 4} in the order this class works with. see: null
     * null null null null null null null null null null null null null null
     * null null null null null null null null null null null null null null
     * null null     {@link CanvasContainer#ALPHA},
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
    public BufferedImage MathOnPixelInt(int requiredtype, Function<Integer, Integer> MathFunction) {
        //note if provided a unsupported type we could use whatever we want... or throw a error.
        BufferedImage ResultImage = getSupportedEmptyCanvas(requiredtype);
        var Destinationdatabuffer = (DataBufferInt) ResultImage.getRaster().getDataBuffer();
        for (int i = 0; i < originalImageHolder.getTotalPixels(); i++) {
            var rgb = originalImageHolder.getRGB(i);
            int rgbint = rgb[ImageColorUtilities.ALPHA] << 24 | rgb[ImageColorUtilities.RED] << 16 | rgb[ImageColorUtilities.GREEN] << 8
                    | rgb[ImageColorUtilities.BLUE];
            var CalculatedPixel = MathFunction.apply(rgbint);
            if (requiredtype == BufferedImage.TYPE_INT_BGR) {
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

    BufferedImage getBlueForIndex(int Index, Color FillColor) {
        return getColorForIndex(Index, ImageColorUtilities.BLUE, FillColor);
    }

    BufferedImage getGreenForIndex(int Index, Color FillColor) {
        return getColorForIndex(Index, ImageColorUtilities.GREEN, FillColor);
    }

    BufferedImage getRedForIndex(int Index, Color FillColor) {
        return getColorForIndex(Index, ImageColorUtilities.RED, FillColor);
    }

    BufferedImage getAlphaForIndex(int Index, Color FillColor) {
        return getColorForIndex(Index, ImageColorUtilities.ALPHA, FillColor);
    }

    /**
     * check if the Index is one of the valid ones.
     *
     * @param Index the index to read
     * @param Channel the channel to gather.
     */
    private static void checkValidChannel(int Index, int Channel) {
        if (Channel < 0 || Channel > ImageColorUtilities.BLUE) {
            throw new ArrayIndexOutOfBoundsException("Invalid Channel");
        }
        if (Index < 0 || Index >= 8) {
            throw new ArrayIndexOutOfBoundsException("the index(bit) Specified is not present on the image");
        }
    }

    /**
     * checks if the provided Channel is Alpha. and if so check if the channel
     * is available on the image.
     * if this conditions are meet returns null. 
     * if the conditions are not meet it returns a image filled with the fill color
     * as can be assumed the image is fully opaque. 
    * @param Channel the channel to check if is alpha
     * @param FillColor the color to fill the image with. 
     * @return 
     */
    private BufferedImage getBaseImage(int Channel) {
        if (Channel == ImageColorUtilities.ALPHA && !originalImageHolder.hasAlpha()) {
            var image = ImageUtils.createBIemptyCopy(originalImageHolder.getDimensions(), BufferedImage.TYPE_BYTE_BINARY);
            var databuff = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
            Arrays.fill(databuff, (byte) 0);
            return image;
        }
        return null;
    }

    //TODO:: this implementation is now corrected but now it might consume too much memory
    //Fix the data consumption.
    BufferedImage getColorForIndex(int Index, int Channel, Color FillColor) {
        checkValidChannel(Index, Channel);
        var image = getBaseImage(Channel);
        if (Objects.nonNull(image)) {
            return image;
        }
        image = ImageUtils.createBINoAlphaemptyCopy(originalImageHolder.getDimensions());
        //here if needs be we could fill the new image with white pixels. or something... 
        var Destinationdatabuffer = image.getRaster().getDataBuffer();//rgb is int. thus. 
        originalImageHolder.feedBufferWithColorForIndex(Destinationdatabuffer, Channel, Index, image.getType(), FillColor);
        return image;
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
        var image = ImageUtils.createBIemptyCopy(originalImageHolder.getDimensions(), BufferedImage.TYPE_BYTE_GRAY);
        var Destinationdatabuffer = (DataBufferByte) image.getRaster().getDataBuffer();
        /*fill the image with empty "canvas color" */
        Arrays.fill(Destinationdatabuffer.getData(), (byte) 0xFF);
        originalImageHolder.feedBufferWithImageSymetric(Destinationdatabuffer, image.getType(), (image.getAlphaRaster() != null), Fill);
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
        return getImageForChannel(false, ImageColorUtilities.BLUE);
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
        return getImageForChannel(false, ImageColorUtilities.ALPHA);
    }

    /**
     * returns a image copy of the original image that contains only the data
     * for the green channel.
     *
     * @return a BufferedImage with only the Blue Channel. the image is RGB. but
     * only with the green data.
     */
    public BufferedImage getGreenImage() {
        return getImageForChannel(false, ImageColorUtilities.GREEN);
    }

    /**
     * returns a image copy of the original image that contains only the data
     * for the Red channel.
     *
     * @return a BufferedImage with only the Blue Channel. the image is RGB. but
     * only with the Red data.
     */
    public BufferedImage getRedImage() {
        return getImageForChannel(false, ImageColorUtilities.RED);
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
        checkValidChannel(1, Channel);
        var image = getBaseImage(Channel);
         if (Objects.nonNull(image)) {
            return image;
        }
        image = ImageUtils.createBIemptyCopy(originalImageHolder.getDimensions(), (GrayImage ? BufferedImage.TYPE_BYTE_GRAY : BufferedImage.TYPE_3BYTE_BGR));
        var Destinationdatabuffer = (DataBufferByte) image.getRaster().getDataBuffer();
        originalImageHolder.feedBufferWithImageChannel(image.getType(),Destinationdatabuffer,Channel);
        return image;
    }
    // </editor-fold>
}
