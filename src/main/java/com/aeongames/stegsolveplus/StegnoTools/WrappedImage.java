/* 
 *  Copyright © 2025 Eduardo Vindas Cordoba. All rights reserved.
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

import java.net.URL;
import java.util.Objects;
import java.awt.Dimension;
import java.nio.file.Path;
import java.io.IOException;
import java.nio.file.Files;
import javax.imageio.ImageIO;
import java.net.URLConnection;
import java.util.logging.Level;
import com.drew.metadata.Metadata;
import java.net.HttpURLConnection;
import java.io.BufferedInputStream;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferByte;
import java.nio.file.StandardOpenOption;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.aeongames.edi.utils.visual.ImageUtils;
import com.aeongames.edi.utils.error.LoggingHelper;
import java.awt.Color;
import java.awt.image.DataBuffer;
import java.util.function.Consumer;

/**
 * this is a ImageWrapper for Buffer Image. Due its nature {@link BufferedImage}
 * is mutable. and writable we don't want to allow this. thus this class
 * constructor load the image from several sources. and store a copy of the
 * image and its metadata and when requested it can share and load some aspects
 * of the image as value or copies. but it does not disclose the loaded image.
 * if constructed from another Buffered image it makes a copy used on the
 * instance.
 *
 * To Consider: we might want to decouple the Image loading part of the code. so
 * it can allow extensibility via dependency injection.
 *
 * @author Eduardo Vindas
 */
public final class WrappedImage {

    /**
     * timeout to read a file from URL
     */
    private static final int URLTIMEOUT = 1000;//1 sec timout.
    /**
     * this is the image we will use as "original" this class will not provide
     * it, this image will only be set once. but not necessarily at
     * construction.
     */
    private BufferedImage originalImage;
    /**
     * Image File Metadata.
     */
    private Metadata Imagemetadata;
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

    /**
     * the File Path of the image to source
     */
    private final Path PathSource;
    /**
     * the URL of the image to source.
     */
    private final URL UrlSource;

    // <editor-fold defaultstate="collapsed" desc="Constructors">
    /**
     * Package Private constructor. creates a new instance of Wrapped Image
     * loading the Image from the Source.
     *
     * @param Source the Path where the file is stored. cannot be null
     * @throws IOException if the file fails to load.
     */
    WrappedImage(Path Source) {
        PathSource = Objects.requireNonNull(Source, "the path is null");
        UrlSource = null;
        Imagemetadata = null;
    }

    /**
     * Package Private constructor. creates a new instance of Wrapped Image
     * loading the Image from the Source.
     *
     * @param Source the URL where the file is stored. cannot be null
     */
    WrappedImage(URL Source) {
        UrlSource = Objects.requireNonNull(Source, "the URL is null");
        PathSource = null;
        Imagemetadata = null;
    }

    /**
     * Package Private constructor. creates a new instance of Wrapped Image and
     * takes a source image. given the way we want to control we will not use
     * the provided image. rather we will make a clone if the image and use as
     * our internal image. to caller: if the provided image is not longer needed
     * please flush it
     *
     * @param SourceToClone a {@link BufferedImage} to be copied to the internal
     * image to use for analysis. caller should dispose or handle that image as
     * deem required.
     */
    WrappedImage(final BufferedImage SourceToClone) {
        originalImage = getCloneofImage(Objects.requireNonNull(SourceToClone, "the Source Image is null"));
        UrlSource = null;
        PathSource = null;
        Imagemetadata = null;
    }

    // </editor-fold>
    /**
     * create a copy of the Provided BufferedImage the caller is responsible of
     * handling the OG image as need be.
     *
     * @param original the source Buffer Image to copy.
     * @return a Copy of the BufferedImage.
     */
    private static BufferedImage getCloneofImage(BufferedImage original) {
        //https://stackoverflow.com/questions/3514158/how-do-you-clone-a-bufferedimage
        //for other few methods that could be used. 
        var clone = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());
        var g = clone.createGraphics();
        g.drawImage(original, 0, 0, null);
        g.dispose();
        return clone;
    }

    /**
     * determines if the Image is loaded or not.
     *
     * @return
     */
    public boolean isImageLoaded() {
        return Objects.nonNull(originalImage);
    }

    public BufferedImage getImageCopy() {
        Objects.requireNonNull(originalImage, "the image is not yet loaded");
        return getCloneofImage(originalImage);
    }

    public BufferedImage getScaledImageCopy(int width, int height) {
        Objects.requireNonNull(originalImage, "the image is not yet loaded");
        return ImageUtils.getScaledInstance(originalImage, width, height, true);
    }

    /**
     * gets a Copy of the image converted into gray scale.
     *
     * @return a Gray scale image.
     */
    public BufferedImage getGrayScaleCopy() {
        var dim = getDimensions();
        var transform = new BufferedImage((int) dim.getWidth(), (int) dim.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        var g = transform.createGraphics();
        g.drawImage(originalImage, 0, 0, null);
        g.dispose();
        return transform;
    }

    /**
     * if the image is not already provided and is not loaded loads it and setup
     * the Single Image instance. as well as its metadata
     */
    public void LoadImage() throws IOException {
        LoadImage(null);
    }

    public void LoadImage(Consumer<String> ReporterFuntion) throws IOException {
        if (ReporterFuntion == null) {
            ReporterFuntion = (t) -> {
            };
        }
        if (Objects.nonNull(originalImage)) {
            //image was provided or is alredy set. 
        } else if (Objects.nonNull(PathSource)) {
            handleFileSource(ReporterFuntion);
        } else if (Objects.nonNull(UrlSource)) {
            handleURLSource(ReporterFuntion);
        }
        ReporterFuntion.accept("setting Color Buffers");
        setupDataBuffer();
        ReporterFuntion.accept("Finishing Processing the Image.");
    }

    /**
     * handles the BufferStream of the image and read the Image Metadata as well
     * as create the BufferImage with the color data.
     *
     * @param bis the Buffer stream of the image to be read
     * @param filesize the size of the data to read from the stream
     * @throws IOException if there is a Error reading the image.
     */
    private void ReadImage(BufferedInputStream bis, int filesize, Consumer<String> ReporterFuntion) throws IOException {
        //set the buffer mark to go back in the buffer. 
        bis.mark((int) filesize);
        try {
            ReporterFuntion.accept("Reading Image metadata");
            Imagemetadata = ImageMetadataReader.readMetadata(bis, filesize);
        } catch (ImageProcessingException MetaErr) {
            LoggingHelper.getLogger("ReadOnlyBuffImage").log(Level.SEVERE, "Unable to read Image Metadata", MetaErr);
        }
        ReporterFuntion.accept("Resetting The Stream");
        bis.reset(); // Reset to the beginning for ImageIO
        ReporterFuntion.accept("Reading the Image Raster and Image pixel data");
        originalImage = ImageIO.read(bis);
    }

    /**
     * sets and get the Raster DataBuffer to be handled on the Get Color
     * functions.
     */
    private void setupDataBuffer() {
        if (ImageDataReference != null || !FastReadSupported(originalImage.getType())) {
            return;
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
    }

    private static boolean FastReadSupported(int type) {
        switch (type) {
            case BufferedImage.TYPE_INT_RGB, BufferedImage.TYPE_INT_ARGB, BufferedImage.TYPE_INT_ARGB_PRE, BufferedImage.TYPE_INT_BGR, BufferedImage.TYPE_3BYTE_BGR, BufferedImage.TYPE_4BYTE_ABGR, BufferedImage.TYPE_4BYTE_ABGR_PRE -> {
                return true;
            }
        }
        return false;
    }

    // <editor-fold defaultstate="collapsed" desc="Source Handlers"> 
    private void handleFileSource(Consumer<String> ReporterFuntion) throws IOException {
        //we asume the file exist and is valid. 
        int filesize = 8192;
        try {
            filesize = (int) Files.size(PathSource);
            ReporterFuntion.accept(String.format("The File to be loaded is of size: %d", filesize));
        } catch (IOException ex) {
            ReporterFuntion.accept("Failed to Read the Filesize Attribute from the file.");
            LoggingHelper.getLogger("ReadOnlyBuffImage").log(Level.SEVERE, "Unable to determine File Size", ex);
        }
        /*
            we cant process both the metadata and the image in a single phase (i.e., a single 
            sequential pass through the stream) using standard Java APIs or the `metadata-extractor`
            library. Both `ImageIO.read()` and `ImageMetadataReader.readMetadata()` need to parse the image
            file independently, and both expect to start reading from the beginning of the stream.
            
            to work arround this limitation. what we opted to do is to buffer then whole file. this way we only read the file
            from the Storage once but we process its data several times. 
         */
        try (var bis = new BufferedInputStream(Files.newInputStream(PathSource, StandardOpenOption.READ), filesize)) {
            ReadImage(bis, filesize, ReporterFuntion);
        } catch (IOException ex) {
            ReporterFuntion.accept("Fail to Read The file.");
            LoggingHelper.getLogger("ReadOnlyBuffImage").log(Level.SEVERE, "IO Exception loading the Image", ex);
            throw ex;
        }
    }

    private void handleURLSource(Consumer<String> ReporterFuntion) throws IOException {
        URLConnection connection;
        try {
            ReporterFuntion.accept("Setting The Connection");
            connection = UrlSource.openConnection();
            connection.setConnectTimeout(URLTIMEOUT);
            connection.setReadTimeout(URLTIMEOUT);
            ReporterFuntion.accept("Stablishing The Connection");
            connection.connect();
            // Make sure response code is in the 200 range.
            if (connection instanceof HttpURLConnection httpconn) {
                if (httpconn.getResponseCode() / 100 != 2) {
                    ReporterFuntion.accept(String.format("unable to Connect error: %d", httpconn.getResponseCode()));
                    LoggingHelper.getLogger("ReadOnlyBuffImage").log(Level.SEVERE, "Connection did not open with code 200");
                    return;
                }
            }
            //if we unable to determine the type of connection we open... oh well
            //proceed with assumption it will work...
            ReporterFuntion.accept("Lookingup Data Lenght.");
            int datasize = connection.getContentLength();
            ReporterFuntion.accept(String.format("data Size Reported as: %d", datasize));
            try (var bis = new BufferedInputStream(connection.getInputStream(), datasize)) {
                ReadImage(bis, datasize, ReporterFuntion);
            }
        } catch (IOException ex) {
            ReporterFuntion.accept("Connection Failed.");
            LoggingHelper.getLogger("ReadOnlyBuffImage").log(Level.SEVERE, "Unable to open a Connection or read the data", ex);
            throw ex;
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="image properties"> 
    /**
     * returns the Dimension of the Original Image. or Dimension as 0 if the
     * image is not set.
     *
     * @return the Dimension of the Original Image. or Dimension as 0 if the
     * image is not set.
     */
    public Dimension getDimensions() {
        if (Objects.isNull(originalImage)) {
            return new Dimension();
        }
        return new Dimension(originalImage.getWidth(), originalImage.getHeight());
    }

    /**
     * returns if the image supports alpha channel.
     *
     * @return true if this image support alpha. false otherwise.
     */
    public boolean hasAlpha() {
        if (Objects.isNull(originalImage)) {
            return false;
        }
        //originalImage.getColorModel().hasAlpha()
        return originalImage.getAlphaRaster() != null;
    }

    /**
     * returns the type of the Underline BufferImage type.
     *
     * @return the type of the Underline BufferImage type.
     * @throws NullPointerException if the image is not setup.
     */
    public int getType() {
        return originalImage.getType();
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

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Color Functions">
    /**
     * gathers the Color information for the specified position
     *
     * @param Channel the color channel that most be one of the following:      <pre>
     * {@link ImageColorUtilities#ALPHA}
     * {@link ImageColorUtilities#RED}
     * {@link ImageColorUtilities#GREEN}
     * {@link #BLUE}
     * </pre>
     *
     * @param x the X axis.
     * @param y the Y axis.
     * @return the color information as a integer value that is between 0 and
     * 0xFF(255);
     */
    public int getColor(int Channel, int x, int y) {
        if (Channel == ImageColorUtilities.ALPHA && !hasAlpha()) {
            return ImageColorUtilities.MAXUBYTE;
        }
        var index = ImageColorUtilities.getIndexForPosition(originalImage.getWidth(), x, y);
        int readvalue;
        switch (ImageDataReference) {
            case byte[] bytesData ->
                readvalue = Byte.toUnsignedInt(ImageColorUtilities.getColorPixelByte(originalImage.getType(), hasAlpha(), bytesData, Channel, index));
            case int[] IntegerData ->
                readvalue = ImageColorUtilities.getColorPixelInt(originalImage.getType(), hasAlpha(), IntegerData, Channel, index);
            case null ->
                readvalue = getColorDefaultMethod(Channel, x, y);
            default ->
                readvalue = getColorDefaultMethod(Channel, x, y);
        }
        return readvalue;
    }

    /**
     * gathers the Color information for the specified position the color
     * information as a integer value that is between 0 and 0xFF(255) as a
     * integer. it should be safe to cast to short. or to use as Unsigned Byte
     * (but remember that Java has no Unsigned. thus you need to account for the
     * Compliment of 2 being a number).
     *
     * @param Channel the color channel that most be one of the following:      <pre>
     * {@link ImageColorUtilities#ALPHA}
     * {@link ImageColorUtilities#RED}
     * {@link ImageColorUtilities#GREEN}
     * {@link ImageColorUtilities#BLUE}
     * </pre>
     *
     * @param LinearPosition the index position from the first pixel to the last
     * pixel of the image.
     * @return the color information as a integer value that is between 0 and
     * 0xFF(255) as a integer.
     */
    public int getColor(int Channel, int LinearPosition) {
        if (Channel == ImageColorUtilities.ALPHA && !hasAlpha()) {
            return ImageColorUtilities.MAXUBYTE;//the image is fully opaque. 
        }
        int readvalue;
        switch (ImageDataReference) {
            case byte[] bytesData ->
                readvalue = ImageColorUtilities.convertToUnsigned(ImageColorUtilities.getColorPixelByte(originalImage.getType(), hasAlpha(), bytesData, Channel, LinearPosition));
            case int[] IntegerData ->
                readvalue = ImageColorUtilities.getColorPixelInt(originalImage.getType(), hasAlpha(), IntegerData, Channel, LinearPosition);
            case null -> {
                var pos = ImageColorUtilities.getPointForIndex(originalImage.getWidth(), LinearPosition);
                readvalue = getColorDefaultMethod(Channel, pos.x, pos.y);
            }
            default -> {
                var pos = ImageColorUtilities.getPointForIndex(originalImage.getWidth(), LinearPosition);
                readvalue = getColorDefaultMethod(Channel, pos.x, pos.y);
            }
        }
        return readvalue;
    }

    private int getColorDefaultMethod(int Channel, int x, int y) {
        int readvalue;
        var data = originalImage.getRaster().getDataElements(x, y, null);
        switch (Channel) {
            case ImageColorUtilities.ALPHA ->
                readvalue = originalImage.getColorModel().getAlpha(data);
            case ImageColorUtilities.RED ->
                readvalue = originalImage.getColorModel().getRed(data);
            case ImageColorUtilities.GREEN ->
                readvalue = originalImage.getColorModel().getGreen(data);
            case ImageColorUtilities.BLUE ->
                readvalue = originalImage.getColorModel().getBlue(data);
            default ->
                throw new IndexOutOfBoundsException(String.format("Invalid Channel %d", Channel));
        }
        return readvalue;
    }

    /**
     * returns the ARGB data for the desired position. if the
     * {@code LinearPosition} is not known. you can call {@link ImageColorUtilities#getRGB(int, int)
     * }
     * with the X,Y coordinates
     *
     * @param LinearPosition the linear position on the Image where to gather
     * the color info.
     * @return an array that contains the data for ARGB data (in the order
     * define by this class (aRGB))
     * @see ImageColorUtilities#ALPHA
     * @see ImageColorUtilities#RED
     * @see ImageColorUtilities#GREEN
     * @see ImageColorUtilities#BLUE
     */
    public Short[] getRGB(int LinearPosition) {
        if (LinearPosition < 0 && LinearPosition > getTotalPixels()) {
            throw new ArrayIndexOutOfBoundsException("the index Specified is not present on the image");
        }
        var result = new Short[4];
        result[ImageColorUtilities.BLUE] = (short) getBlue(LinearPosition);
        result[ImageColorUtilities.GREEN] = (short) getGreen(LinearPosition);
        result[ImageColorUtilities.RED] = (short) getRed(LinearPosition);
        result[ImageColorUtilities.ALPHA] = (short) getAlpha(LinearPosition);
        return result;
    }

    /**
     *
     * returns the ARGB data for the desired position.
     *
     * @param x the X coordinate of the image
     * @param y the Y coordinate of the image
     * @return an array that contains the data for ARGB data (in the order
     * define by this class (aRGB))
     * @see ImageColorUtilities#ALPHA
     * @see ImageColorUtilities#RED
     * @see ImageColorUtilities#GREEN
     * @see ImageColorUtilities#BLUE
     */
    public Short[] getRGB(int x, int y) {
        var position = ImageColorUtilities.getIndexForPosition((int) getDimensions().getWidth(), x, y);
        return getRGB(position);
    }

    /**
     * return the alpha pixel "intensity" if any. if there is none it returns a
     * full opaque
     *
     * @param LinearPosition the linear position on the Image where to gather
     * the color info.
     * @return the intensity of the alpha channel. or 0xFF if none (assuming
     * full opacity.)
     */
    public int getAlpha(int LinearPosition) {
        if (!hasAlpha()) {
            return ImageColorUtilities.MAXUBYTE;//the image is fully opaque. 
        }
        return getColor(ImageColorUtilities.ALPHA, LinearPosition);
    }

    public int getAlpha(int x, int y) {
        if (!hasAlpha()) {
            return ImageColorUtilities.MAXUBYTE;//the image is fully opaque. 
        }
        return getColor(ImageColorUtilities.ALPHA, x, y);
    }

    public int getRed(int LinearPosition) {
        return getColor(ImageColorUtilities.RED, LinearPosition);
    }

    public int getRed(int x, int y) {
        return getColor(ImageColorUtilities.RED, x, y);
    }

    public int getGreen(int LinearPosition) {
        return getColor(ImageColorUtilities.GREEN, LinearPosition);
    }

    public int getGreen(int x, int y) {
        return getColor(ImageColorUtilities.GREEN, x, y);
    }

    public int getBlue(int LinearPosition) {
        return getColor(ImageColorUtilities.BLUE, LinearPosition);
    }

    public int getBlue(int x, int y) {
        return getColor(ImageColorUtilities.BLUE, x, y);
    }

    // </editor-fold>
    /**
     * return null if no Path was used to create this image. otherwise returns
     * the reference of the File Path used (or to be used) to load the image
     *
     * @return Null or a Reference to the Path to load the Image data.
     */
    public Path getFileSource() {
        return PathSource;
    }

    /**
     * return null if no URL was used to create this image. otherwise returns
     * the reference of the {@code URL} used (or to be used) to load the image
     *
     * @return Null or a Reference to the {@code URL} to load the Image data.
     */
    public URL getURLSource() {
        return UrlSource;
    }

    public String getSourceString() {
        if (Objects.nonNull(PathSource)) {
            return PathSource.toAbsolutePath().toString();
        } else {
            return UrlSource.toString();
        }
    }

    void feedBufferWithImageChannel(int type, DataBufferByte Destinationdatabuffer, int Channel) {
        //NOTE: Alpha channel will become visible on the "blue" or Red channel (if the image is not grey)
        switch (ImageDataReference) {
            case byte[] bytesData ->
                cloneChannelBytes(originalImage.getType(), type, hasAlpha(), false, bytesData, Destinationdatabuffer, Channel);
            case int[] IntegerData ->
                cloneChannelInt(originalImage.getType(), type, hasAlpha(), false, IntegerData, Destinationdatabuffer, Channel);
            case null -> {
                cloneChannelDefault(Channel, originalImage, getTotalPixels(), type, Destinationdatabuffer.getData());
            }
            default -> {
                cloneChannelDefault(Channel, originalImage, getTotalPixels(), type, Destinationdatabuffer.getData());
            }
        }
    }

    void feedBufferWithImageSymetric(DataBufferByte Destinationdatabuffer, int type, boolean DestAlpha, Color Fill) {
        switch (ImageDataReference) {
            case byte[] bytesData -> {
                ImageColorUtilities.DrawSymetricBytes(originalImage.getType(), type, hasAlpha(), DestAlpha, bytesData, Destinationdatabuffer, Fill);
            }
            case int[] IntegerData ->
                ImageColorUtilities.DrawSymetricInt(originalImage.getType(), type, DestAlpha, IntegerData, Destinationdatabuffer, Fill);
            case null -> {
                var rgbfill = ImageColorUtilities.getRGBArray(Fill);
                var Destdata = Destinationdatabuffer.getData();
                for (int i = 0; i < getTotalPixels(); i++) {//Should we do the loop once we know the type of buffer and avoid 1 computation?
                    var pos = ImageColorUtilities.getPointForIndex(originalImage.getWidth(), i);
                    var data = originalImage.getRaster().getDataElements(pos.x, pos.y, null);
                    var green = originalImage.getColorModel().getGreen(data);
                    var same = originalImage.getColorModel().getRed(data) == green
                            && green == originalImage.getColorModel().getBlue(data);
                    if (same) {
                        var baseindex = i;//getRawIndexForImageIndex(3, i);
                        Destdata[baseindex] = rgbfill[2];
                        //Destdata[baseindex + 1] = rgbfill[1];
                        //Destdata[baseindex + 2] = rgbfill[0];
                    }
                }
            }
            default -> {
                var rgbfill = ImageColorUtilities.getRGBArray(Fill);
                var Destdata = Destinationdatabuffer.getData();
                for (int i = 0; i < getTotalPixels(); i++) {//Should we do the loop once we know the type of buffer and avoid 1 computation?
                    var pos = ImageColorUtilities.getPointForIndex(originalImage.getWidth(), i);
                    var data = originalImage.getRaster().getDataElements(pos.x, pos.y, null);
                    var green = originalImage.getColorModel().getGreen(data);
                    var same
                            = originalImage.getColorModel().getRed(data) == green
                            && green == originalImage.getColorModel().getBlue(data);
                    if (same) {
                        var baseindex = i;//getRawIndexForImageIndex(3, i);
                        Destdata[baseindex] = rgbfill[2];
                        //Destdata[baseindex + 1] = rgbfill[1];
                        //Destdata[baseindex + 2] = rgbfill[0];
                    }
                }
            }

        }
    }

    void feedBufferWithColorForIndex(DataBuffer Destinationdatabuffer,int Channel,int Index, int type, Color Fill){
        for (int i = 0; i < getTotalPixels(); i++) {//Should we do the loop once we know the type of buffer and avoid 1 computation?
            int readvalue = 0b0;
            switch (ImageDataReference) {
                case byte[] bytesData ->
                    readvalue = ImageColorUtilities.convertToUnsigned(ImageColorUtilities.getColorPixelByte(getType(), hasAlpha(), bytesData, Channel, i));
                case int[] IntegerData ->
                    readvalue = ImageColorUtilities.getColorPixelInt(getType(), hasAlpha(), IntegerData, Channel, i);
                case null -> {
                    var pos = ImageColorUtilities.getPointForIndex(originalImage.getWidth(), i);
                    var data = originalImage.getRaster().getDataElements(pos.x, pos.y, null);
                    switch (Channel) {
                        case ImageColorUtilities.ALPHA ->
                            readvalue = originalImage.getColorModel().getAlpha(data);
                        case ImageColorUtilities.RED ->
                            readvalue = originalImage.getColorModel().getRed(data);
                        case ImageColorUtilities.GREEN ->
                            readvalue = originalImage.getColorModel().getGreen(data);
                        case ImageColorUtilities.BLUE ->
                            readvalue = originalImage.getColorModel().getBlue(data);
                    }
                }
                default -> {
                    var pos = ImageColorUtilities.getPointForIndex(originalImage.getWidth(), i);
                    var data = originalImage.getRaster().getDataElements(pos.x, pos.y, null);
                    switch (Channel) {
                        case ImageColorUtilities.ALPHA ->
                            readvalue = originalImage.getColorModel().getAlpha(data);
                        case ImageColorUtilities.RED ->
                            readvalue = originalImage.getColorModel().getRed(data);
                        case ImageColorUtilities.GREEN ->
                            readvalue = originalImage.getColorModel().getGreen(data);
                        case ImageColorUtilities.BLUE ->
                            readvalue = originalImage.getColorModel().getBlue(data);
                    }
                }

            }
            //To consider. maybe dont set any color if not found. allow whatever is default on the provided image. 
            var CalculatedPixel = ((readvalue >>> Index) & 0b1) == 0b0 ? ImageColorUtilities.RGBMASK : Fill.getRGB();
            Destinationdatabuffer.setElem(i, CalculatedPixel);
        }
    }
    
    // <editor-fold defaultstate="collapsed" desc="Clone Channels"> 
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
        var srcTranslatedChannel = ImageColorUtilities.getColorTranslation(SourceType, Channel);
        //destination Navigation+
        int destBytesPerPixel;
        if (DestinationType == BufferedImage.TYPE_BYTE_GRAY) {
            destBytesPerPixel = 1;
        } else {
            destBytesPerPixel = destHasAlpha ? 4 : 3;
        }
        var destTranslatedChannel = ImageColorUtilities.getColorTranslation(DestinationType, Channel);
        if (hasAlpha && destHasAlpha && Channel == ImageColorUtilities.ALPHA) {
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
        var destTranslatedChannel = ImageColorUtilities.getColorTranslation(DestinationType, Channel);

        var srcTranslatedChannel = ImageColorUtilities.getColorTranslation(SourceType, Channel);

        var shift = 0;
        if (hasAlpha && !destHasAlpha && Channel == ImageColorUtilities.ALPHA) {
            // leave the shift as 0 make the alpha visible on the Blue or Red channel. 
        } else {
            shift = 8 * (3 - srcTranslatedChannel);
        }
        for (int Sourceindex = 0, destindex = 0; Sourceindex < SrcData.length; Sourceindex++, destindex += destBytesPerPixel) {
            DestbyteData[destindex + destTranslatedChannel] = (byte) ((SrcData[Sourceindex] >>> shift) & ImageColorUtilities.MAXUBYTE);
        }
    }

    private static void cloneChannelDefault(int Channel, BufferedImage srcimg, int totalpixels, int DestType, byte[] Destdata) {
        for (int i = 0; i < totalpixels; i++) {
            var pos = ImageColorUtilities.getPointForIndex(srcimg.getWidth(), i);
            var data = srcimg.getRaster().getDataElements(pos.x, pos.y, null);
            var baseindex = DestType != BufferedImage.TYPE_BYTE_GRAY ? ImageColorUtilities.getRawIndexForImageIndex(3, i) : i;
            switch (Channel) {
                case ImageColorUtilities.ALPHA -> {//check if hasAlphaChannel maybe? 
                    Destdata[baseindex + (DestType == BufferedImage.TYPE_BYTE_GRAY ? 0 : 2)]
                            = (byte) srcimg.getColorModel().getAlpha(data);
                }
                case ImageColorUtilities.RED -> {
                    Destdata[baseindex + (DestType == BufferedImage.TYPE_BYTE_GRAY ? 0 : 2)]
                            = (byte) srcimg.getColorModel().getRed(data);
                }
                case ImageColorUtilities.GREEN ->
                    Destdata[baseindex + (DestType == BufferedImage.TYPE_BYTE_GRAY ? 0 : 1)]
                            = (byte) srcimg.getColorModel().getGreen(data);
                case ImageColorUtilities.BLUE -> {
                    Destdata[baseindex]
                            = (byte) srcimg.getColorModel().getBlue(data);
                }
            }
        }
    }

    //</editor-fold>

}
