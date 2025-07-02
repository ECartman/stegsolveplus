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

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

/**
 *
 * @author Eduardo Vindas
 */
public class ImageColorUtilities {

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
     * Channel And positions
     */
    public static final int ALPHA = 0, RED = 1, GREEN = 2, BLUE = 3;

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
        return Byte.toUnsignedInt(value);
    }

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
    public static int[] getColorOrder(int type) {
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
     * converts a color object into an Byte array ordered in 0=R 1=Green 2=Blue
     *
     * @param col the color to convert
     * @return a byte array with the colors represented from 0 to 0xFF
     */
    public static byte[] getRGBArray(Color col) {
        byte[] RGBBYTES = new byte[3];
        RGBBYTES[0] = (byte) col.getRed();
        RGBBYTES[1] = (byte) col.getGreen();
        RGBBYTES[2] = (byte) col.getBlue();
        return RGBBYTES;
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
    static byte getColorPixelByte(int type, final boolean hasAlpha, final byte[] byteData, int channel, int Index) {
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
     * Calculates the index for the given coordinates on a image. the returned
     * value is the index of a particular pixel on the image graph
     *
     * @param Width the Width of the image.
     * @param x the x axis to locate a particular pixel
     * @param y the y axis to locate a particular pixel
     * @return the index of the pixel on a linear order.
     */
    static int getIndexForPosition(int Width, int x, int y) {
        return y * Width + x;
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
    static Point getPointForIndex(final int Width, final int index) {
        return new Point(index % Width, index / Width);
    }

    /**
     * Inflate the index to its position on a linear Array. this is required for
     * example when we have a byte array that contains all the image data but we
     * need to see a index on a canvas. so for example if a color has 3 colors
     * per index we need to multiply 3 by the index to get the index of the
     * value we seeking
     *
     * @param bytesperPosition the amount of bytes there exists on a specific
     * position
     * @param Index the index to seek
     * @return the position on a linear array
     */
    static int getRawIndexForImageIndex(int bytesperPosition, int Index) {
        return bytesperPosition * Index;
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
    static int getColorPixelInt(int type, final boolean hasAlpha, final int[] intData, int channel, int Index) {
        var Translate = getColorOrder(type);//this will crash if not found. that is desireable as we want to fix that problem. see the To do's on getOrder
        if (channel == Translate[ALPHA]) {
            return hasAlpha ? ((intData[Index] >>> 24) & MAXUBYTE) : MAXUBYTE;//full alpha (opaque) if has not alpha
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

    /**
     * draws a "grey map" from the provided source. to the Destination Buffer.
     *
     * @param SourceData an array that contains the image data to read
     * @param destBuffer the destination buffer of the search of Grey pixels
     * @param Fill the data to fill the resulting value.
     */
    static void DrawSymetricGreyInt(final int[] SourceData, DataBufferByte destBuffer, byte Fill) {
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
    static void cloneChannelBytes(int SourceType, int DestinationType, boolean hasAlpha, boolean destHasAlpha, byte[] SrcBuffer, DataBufferByte Destinationdatabuffer, int Channel) {
        //source navigation.
        int srcBytesPerPixel = hasAlpha ? 4 : 3;
        int jumpPerPixel = srcBytesPerPixel - 1;
        var destData = Destinationdatabuffer.getData();
        var srcTranslatedChannel = getColorTranslation(SourceType, Channel);
        //destination Navigation+
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
    static void cloneChannelInt(int SourceType, int DestinationType, boolean hasAlpha, boolean destHasAlpha, int[] SrcData, DataBufferByte Destinationdatabuffer, int Channel) {
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
    static void DrawSymetricBytes(int Type, int DestType, boolean srcHasAlpha, boolean DestHasAlpha, byte[] SrcBuffer, DataBufferByte destBuffer, Color Fill) {
        DrawSymetricBytes(Type, DestType, srcHasAlpha, DestHasAlpha, SrcBuffer, destBuffer, getRGBArray(Fill));
    }

    static void DrawSymetricGreyBytes(boolean srcHasAlpha, byte[] SourceData, DataBufferByte destBuffer, byte Fill) {
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
    static void DrawSymetricBytes(int Type, int DestType, boolean srcHasAlpha, boolean DestHasAlpha, byte[] SourceData, DataBufferByte destBuffer, byte[] RGBfill) {
        if (DestType == BufferedImage.TYPE_BYTE_GRAY) {
            var fillIntesity = Math.min(Math.min(RGBfill[0], RGBfill[1]), RGBfill[2]);
            DrawSymetricGreyBytes(srcHasAlpha, SourceData, destBuffer, (byte) fillIntesity);
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
    static void DrawSymetricInt(int type, int DestType, boolean DestHasAlpha, int[] SrcBuffer, DataBufferByte destBuffer, Color Fill) {
        DrawSymetricInt(type, DestType, DestHasAlpha, SrcBuffer, destBuffer, getRGBArray(Fill));
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
    static void DrawSymetricInt(int type, int DestType, boolean DestHasAlpha, int[] SourceData, DataBufferByte destBuffer, byte[] RGBfill) {
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

    /**
     * Copies a Specific channel from the source Image
     *
     * @param Channel the color channel to clone
     * @param srcimg the source image to copy the data from
     * @param totalpixels the total pixels of the image
     * @param DestType the type of resulting image
     * @param Destdata the destination image data.
     */
    static void cloneChannelDefault(int Channel, BufferedImage srcimg, int totalpixels, int DestType, byte[] Destdata) {
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

    /**
     * Translates from one Ordering for example RGB into BGR to be used when
     * moving the pixels around on the canvas of a resulting image.
     *
     * @param type the type of image to translate.
     * @param channel the channel desire to get the translation
     * @return the channel to use for the given channel at the specified type.
     */
    static int getColorTranslation(int type, int channel) {
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

    static BufferedImage getFullOpaqueImage(Color FillColor) {
        var image = new BufferedImage(5, 5, BufferedImage.TYPE_BYTE_BINARY);
        var databuff = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        Arrays.fill(databuff, FillColor.getRGB());
        return image;
    }
    
    
    
}
