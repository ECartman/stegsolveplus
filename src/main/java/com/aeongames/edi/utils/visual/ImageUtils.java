/*
 * Copyright 2008-2011,2024-2025 Eduardo Vindas
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
 /*
*Created on Oct 9, 2010
 */
package com.aeongames.edi.utils.visual;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;
import java.util.Objects;
import java.util.Optional;
import javax.swing.ImageIcon;

/**
 *
 * @author Eduardo Vindas / cartman
 */
public class ImageUtils {

    // <editor-fold defaultstate="collapsed" desc="create empty image"> 
    /**
     * creates a new BufferedImage that support ARGB wit the same dimensions as
     * the original image. but <strong>NO CONTENT</strong>
     *
     * @return a new instance of BufferedImage that support ARGB (rgb+alpha)
     */
    public static BufferedImage createBIemptyCopy(Dimension dim) {
        return createBIemptyCopy(dim,BufferedImage.TYPE_INT_ARGB);
    }

    /**
     * creates a new BufferedImage that support RGB (rgb NOT ALPHA) wit the same
     * dimensions as the original image.
     *
     * @return a new instance of BufferedImage that support RGB but no Alpha
     * Channel
     */
    public static BufferedImage createBINoAlphaemptyCopy(Dimension dim) {
        return createBIemptyCopy(dim,BufferedImage.TYPE_INT_RGB);
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
    public static BufferedImage createBIemptyCopy(Dimension dim, int type) {
        return new BufferedImage((int) dim.getWidth(), (int) dim.getHeight(), type);
    }
    // </editor-fold>

    /**
     * we want to show a image but we want to keep the ratio, so lets made a
     * image using this approach what it is done is we take the smallest
     * parameter as we want a image that is able to be display within the
     * parameter size we assume the image will be contained within a "box" also
     * we will calculate the position there we will place the image
     *
     * @param width the width of the image container or the context where will
     * be paced
     * @param height the height of the image container or the context where will
     * be paced
     * @param to_resize image that we require to calculate the the ratio for
     * @return a array of integers with the following values 0= width to set
     * 1=height to set 2=the width where the image required to be place 3=the
     * height within the image will be set.
     */
    public static int[] keep_ratio_for_size(int width, int height, Image to_resize) {
        double scale = determineImageScale(to_resize.getWidth(null), to_resize.getHeight(null), width, height);
        var WidthToUse = (int) (to_resize.getWidth(null) * scale);
        var HeightToUse = (int) (to_resize.getHeight(null) * scale);
        WidthToUse = (WidthToUse < 1) ? 1 : WidthToUse;
        HeightToUse = (HeightToUse < 1) ? 1 : HeightToUse;
        var PositionForW = width / 2 - WidthToUse / 2;
        var PositionForH = height / 2 - HeightToUse / 2;
        return new int[]{WidthToUse, HeightToUse, PositionForW, PositionForH};
    }

    /**
     * determine scale of the image returns the smallest of the scale values
     */
    private static double determineImageScale(int sourceWidth, int sourceHeight, int targetWidth, int targetHeight) {
        double scalex = (double) targetWidth / sourceWidth;
        double scaley = (double) targetHeight / sourceHeight;
        return Math.min(scalex, scaley);
    }

    /**
     * converts a Image into a Buffered image. if the image is already a buffer
     * image returns the parameters (does not create a copy!)
     *
     * @param image the image to transform to buffered image
     * @return the same image if is instance of BufferedImage or a new
     * BufferedImage with the same content as the parameter or null if the image
     * is null.
     */
    public static BufferedImage toBufferedImage(Image image) {
        if (Objects.isNull(image)) {
            return null;
        } else if (image instanceof BufferedImage buff) {
            return buff;
        }
        // This code ensures that all the pixels in the image are loaded
        if (image.getWidth(null) < 0) {
            MediaTracker tracker = new MediaTracker(new Canvas());
            tracker.addImage(image, 0);
            try {
                tracker.waitForID(0);
            } catch (InterruptedException e) {
            }
        }
        // Determine if has Alpha on image or has Alpha on pixels
        boolean hasAlpha = hasAlpha(image);
        BufferedImage bimage = null;
        int transparency = (hasAlpha)
                ? Transparency.BITMASK
                : Transparency.OPAQUE;
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            // Create the buffered image
            GraphicsDevice gs = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gs.getDefaultConfiguration();
            bimage = gc.createCompatibleImage(
                    image.getWidth(null),
                    image.getHeight(null),
                    transparency);
        } catch (HeadlessException e) {
            // The system does not have a screen
        }

        if (bimage == null) {
            // Create a buffered image using the default color model
            var type = (hasAlpha) ? BufferedImage.TYPE_INT_ARGB
                    : BufferedImage.TYPE_INT_RGB;
            bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
        }
        // Copy image to buffered image
        Graphics g = bimage.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return bimage;
    }

    /**
     * converts a Image into a Buffered image. if the image is already a buffer
     * image returns the parameters (does not create a copy!)
     *
     * @param image the image to transform to buffered image
     * @param releaseOg if true the original image is to be released (if not
     * null nor a buffered image)
     * @return the same image if is instance of BufferedImage or a new
     * BufferedImage with the same content as the parameter null if the image is
     * null.
     */
    public static BufferedImage toBufferedImage(Image image, boolean releaseOg) {
        if (Objects.isNull(image)) {
            return null;
        } else if (image instanceof BufferedImage buff) {
            return buff;
        }
        BufferedImage bimage = toBufferedImage(image);
        if (releaseOg) {
            image.flush();
        }
        return bimage;
    }

    /**
     * creates and returns a <strong>copy</strong> of the imaged resized to the
     * desired dimensions and using the provided Hint.(for resizing)
     *
     * @param source the source image to create a resized version.
     * @param width the desired width
     * @param height the desired height
     * @param OptionalHint A Optional value that refers to the hint for resize.
     * @return a resized Instance of the image.
     */
    public static ImageIcon ScaleImageIcon(ImageIcon source, int width, int height, Integer OptionalHint) {
        Image image = source.getImage();
        var opthint = Optional.ofNullable(OptionalHint);
        int hint;
        if (opthint.isPresent()) {
            hint = opthint.get();
        } else {
            hint = java.awt.Image.SCALE_SMOOTH;
        }
        Image newimg = image.getScaledInstance(width, height, hint); // scale it the smooth way
        return new ImageIcon(newimg);
    }

    private static void upscale(final BufferedImage img, BufferedImage result, boolean KeepAspect) {
        Graphics2D g2 = result.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int[] size = {result.getWidth(), result.getHeight(), 0, 0};
        if (KeepAspect) {
            size = ImageUtils.keep_ratio_for_size(result.getWidth(), result.getHeight(), img);
        }
        g2.drawImage(img, size[2], size[3], size[0], size[1], null);
        g2.dispose();
    }

    private static void downscale(final BufferedImage img, BufferedImage result, boolean KeepAspect) {
        int srcW = img.getWidth();
        int srcH = img.getHeight();
        int targetW = result.getWidth();
        int targetH = result.getHeight();
        int scaledW = targetW;
        int scaledH = targetH;
        int xOffset = 0, yOffset = 0;

        if (KeepAspect) {
            double scalex = (double) targetW / srcW;
            double scaley = (double) targetH / srcH;
            double scale = Math.min(scalex, scaley);
            scaledW = (int) Math.round(srcW * scale);
            scaledH = (int) Math.round(srcH * scale);
            xOffset = (targetW - scaledW) / 2;
            yOffset = (targetH - scaledH) / 2;
        }

        BufferedImage currentImg = img;
        int currentW = srcW;
        int currentH = srcH;
        // Multi-step downscaling for best quality
        while (currentW > scaledW || currentH > scaledH) {
            int nextW = currentW > scaledW ? Math.max(scaledW, currentW / 2) : currentW;
            int nextH = currentH > scaledH ? Math.max(scaledH, currentH / 2) : currentH;
            BufferedImage tmpimg = new BufferedImage(nextW, nextH, currentImg.getType());
            Graphics2D g2 = tmpimg.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(currentImg, 0, 0, nextW, nextH, null);
            g2.dispose();
            if (currentImg != img) {
                currentImg.flush();
            }
            currentImg = tmpimg;
            currentW = nextW;
            currentH = nextH;
        }
        // Draw the final scaled image into the result buffer, centered if needed
        Graphics2D g2 = result.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(currentImg, xOffset, yOffset, scaledW, scaledH, null);
        g2.dispose();

        if (currentImg != img && currentImg != result) {
            currentImg.flush();
        }
    }

    /**
     * creates and returns a copy of the image resized to the desired dimensions
     *
     * @param img the source image to create a resized version.
     * @param targetWidth the desired width
     * @param targetHeight the desired height
     * @return a resized Copy of the image.
     */
    public static BufferedImage getScaledInstance(BufferedImage img, int targetWidth, int targetHeight, boolean KeepAspect) {
        int w = img.getWidth();
        int h = img.getHeight();
        //if the image is SAME size just return a fast copy:
        if (w == targetWidth && h == targetHeight) {
            var result = new BufferedImage(targetWidth, targetHeight, img.getType());
            var g = result.createGraphics();
            g.drawImage(img, 0, 0, null);
            g.dispose();
            return result;
        } else if (targetWidth >= w && targetHeight >= h) {
            var result = new BufferedImage(targetWidth, targetHeight, img.getType());
            upscale(img, result, KeepAspect);
            return result;
        }
        // If downscaling, use multi-step for best quality
        BufferedImage currentImg = img;
        downscale(img, currentImg, KeepAspect);
        return currentImg;
    }

    /**
     * creates and returns a copy of the imaged resized to the desired
     * dimensions and using the provided Hint.(for resizing)
     *
     * @param source the source image to create a resized version.
     * @param width the desired width
     * @param height the desired height
     * @return a resized Instance of the image.
     */
    public static ImageIcon ScaleImageIcon(ImageIcon source, int width, int height) {
        return ScaleImageIcon(source, width, height, null);
    }

    /**
     * creates and returns a copy of the imaged resized to the desired
     * dimensions and using the provided Hint.(for resizing)
     *
     * @param source the source image to create a resized version.
     * @param width the desired width
     * @param height the desired height
     * @param Hint A value that refers to the hint for resize.
     * @return a resized Instance of the image.
     */
    public static ImageIcon ScaleImageIcon(ImageIcon source, int width, int height, int Hint) {
        return ScaleImageIcon(source, width, height, Integer.valueOf(Hint));
    }

    /**
     * check if the provided image has or support Alpha Channel if the Image is
     * a Buffered Image it gathers the value from the Color Model
     *
     * otherwise we attempt to get the Color model from a Pixel via (pixel
     * grabber).
     *
     * @param image the image to check
     * @return
     */
    public static boolean hasAlpha(Image image) {
        if (image instanceof BufferedImage buff) {
            return buff.getColorModel().hasAlpha();
        }
        PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
        try {
            pg.grabPixels(1000);
        } catch (InterruptedException e) {
        }
        ColorModel cm = pg.getColorModel();
        if (cm != null) {
            return cm.hasAlpha();
        }
        return false;
    }
}
