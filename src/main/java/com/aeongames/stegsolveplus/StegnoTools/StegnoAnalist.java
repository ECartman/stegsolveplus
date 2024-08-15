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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;
import javax.imageio.ImageIO;

/**
 * this class is a Holder for the information and access to the stegnography
 * tools that can be applied to an image.
 *
 * TODO: load image from URL should be easy to add. but require changes on the
 * UI to support it.
 *
 * @author Ed
 */
public class StegnoAnalist {

    public static final String ValidImagesFiles[] = ImageIO.getReaderFormatNames();
    /**
     * the source file to read and or check data from.
     */
    private Path File = null;
    private URL ImageAddress = null;
    private ImageContainer ImageCache;

    public StegnoAnalist(Path File) {
        this.File = File;
    }

    public StegnoAnalist(File file) {
        this.File = file.toPath();
    }

    public StegnoAnalist(URL Address) {
        this.ImageAddress = Address;
    }

    public void RunTrasFormations(boolean forced) throws IOException {
        LoadImage(forced);
        if (ImageCache == null) {
            //fail to load the image we might throw a error instead? 
            return;
        }
        //TODO: update the original image is loaded 
        //image at this point is loaded. now lets create the transformations. 
    }

    /**
     * Create a Buffer Image that contains pixels with the FillColor where the
     * specific pixel data r=g=b. for example. if a pixel color is all 0 (black)
     * or White (all 1) or any color that for all color the data is the same for
     * example. "1f1f1f" and so on. Highlights just the pixels for which r=g=b
     *
     */
    private BufferedImage TranformSymetricPixels(Color FillColor) {
        var transform = ImageCache.createBIemptyCopy();
        ImageCache.getSymetricPixels(transform, FillColor);
        return transform;
    }

    private BufferedImage TransformGreyScale() {
        var transform = ImageCache.createBIemptyCopy(BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = transform.getGraphics();
        g.drawImage(ImageCache.getOriginal(), 0, 0, null);
        g.dispose();
        return transform;
    }

    private BufferedImage TranformGreyScaleSlow() {
        var transform = ImageCache.createBIemptyCopy(BufferedImage.TYPE_BYTE_GRAY);
        ImageCache.MathOnPixels(transform, RGB -> {
            var rr = Math.pow(ImageContainer.convertToNumeric(RGB[ImageContainer.RED]) / 255.0f, 2.2f);
            var gg = Math.pow(ImageContainer.convertToNumeric(RGB[ImageContainer.GREEN]) / 255.0f, 2.2f);
            var bb = Math.pow(ImageContainer.convertToNumeric(RGB[ImageContainer.BLUE]) / 255.0f, 2.2f);
            // Calculate luminance:
            var lum = 0.2126f * rr + 0.7152f * gg + 0.0722f * bb;

            // Gamma compand and rescale to byte range:
            int grayLevel = (int) (255.0 * Math.pow(lum, 1.0 / 2.2));
            return (grayLevel << 16) + (grayLevel << 8) + grayLevel;
        });
        return transform;
    }

    /**
     * Inverts the color of the image using HSV Rotation.
     *
     * @return a instance of BufferImage with the inverted color data
     */
    private BufferedImage inversionHSV() {
        var transform = ImageCache.createBIemptyCopy();
        ImageCache.MathOnPixels(transform, RGB -> {
            float[] HSV = new float[3];
            Color.RGBtoHSB(
                    ImageContainer.convertToNumeric(RGB[ImageContainer.RED]),
                    ImageContainer.convertToNumeric(RGB[ImageContainer.GREEN]),
                    ImageContainer.convertToNumeric(RGB[ImageContainer.BLUE]),
                    HSV);
            //once with HSV 
            var InvertedColor = Color.HSBtoRGB((HSV[0] + 180) % 360, HSV[1], HSV[2]);
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
            return IntRGB ^ ImageContainer.RGBMASK;
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
    
    private BufferedImage[] getImagePerBitOnBlueChannel(){
        var BitsImages=new BufferedImage[8];
        for(int index=0;index<BitsImages.length;index++){
            BitsImages[index]=ImageCache.getBlueForIndex(index, Color.BLUE);
        }
        return BitsImages;
    }
    
    private BufferedImage getBlueImage(){
        return ImageCache.getBlueImage();
    }
    
    private BufferedImage[] getImagePerBitOnGreenChannel(){
        var BitsImages=new BufferedImage[8];
        for(int index=0;index<BitsImages.length;index++){
            BitsImages[index]=ImageCache.getGreenForIndex(index, Color.GREEN);
        }
        return BitsImages;
    }
    
    private BufferedImage getImageGreenimage(){
        return ImageCache.getGreenImage();
    }
    
    private BufferedImage[] getImagePerBitOnRedChannel(){
        var BitsImages=new BufferedImage[8];
        for(int index=0;index<BitsImages.length;index++){
            BitsImages[index]=ImageCache.getRedForIndex(index, Color.RED);
        }
        return BitsImages;
    }
    
    private BufferedImage getImageRedimage(){
        return ImageCache.getRedImage();
    }
    
    private BufferedImage[] getImagePerBitOnAlphaChannel(){
        var BitsImages=new BufferedImage[8];
        for(int index=0;index<BitsImages.length;index++){
            BitsImages[index]=ImageCache.getAlphaForIndex(index, Color.BLACK);
        }
        return BitsImages;
    }
    
    private BufferedImage getImageAlphaimage(){
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
            ImageCache = new ImageContainer(File);
        } else {
            ImageCache = new ImageContainer(ImageAddress);
        }
    }

}
