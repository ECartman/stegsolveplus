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

import com.aeongames.edi.utils.data.Pair;
import com.aeongames.edi.utils.error.LoggingHelper;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.SwingWorker;

/**
 * this class is a background worker that conducts and waits for the underline
 * Color transformations are done. some of the underline transformations needs
 * to be enhanced as some of them are a bit slow. also there are a few that work
 * similar (for example. getting the blue pixels are pulled and showed. but at
 * this same point the process could also pull the independent bits & so on.
 *
 * generally speaking this is fine for images that are less than 2k resolution
 * but for big images takes several seconds to minutes..
 *
 *
 * TODO: load image from URL should be easy to add. but require changes on the
 * UI to support it.
 *
 * @author Eduardo Vindas
 */
public class StegnoAnalysis extends SwingWorker<List<Pair<String, BufferedImage>>, Pair<String, BufferedImage>> {

    public static final String STATE_STAGE = "STATE_STAGE";
    public static final String STAGE_ERROR = "STAGE_ERROR";
    public static final String ValidImagesFiles[] = ImageIO.getReaderFormatNames();

    public enum TransformAnalysis {
        OriginalImage("Original"),
        GreyMap("Grey Map"),
        GreyScale("Grey Scale"),
        BluePixels("Blue Pixels"),
        GreenPixels("Green Pixels"),
        RedPixels("Red Pixels"),
        AlphaPixels("Alpha Pixels"),
        FirstForthImage("Image of the bit 1 and 2"),
        SecondForthImage("Image of the bit 3 and 4"),
        ThirdForthImage("Image of the bit 5 and 6"),
        ForthForthImage("Image of the bit 7 and 8"),
        XorInversion("inverted Bits Image"),
        InvertHue("Inverted Hue"),
        InvertHueBright("Inverted Hue and Brightness"),
        InvertSaturation("Inverted Saturation"),
        InvertBright("Inverted Brightness");
        public final String Name;

        private TransformAnalysis(String name) {
            this.Name = name;
        }

    }

    /**
     * the source file to read and or check data from.
     */
    private final Path File;
    private final URL ImageAddress;
    private CanvasContainer ImageCache;
    private static final Logger loger = LoggingHelper.getLogger(StegnoAnalysis.class.getName());
    private final BiConsumer<Boolean, List<Pair<String, BufferedImage>>> callBack;

    public StegnoAnalysis(BiConsumer<Boolean, List<Pair<String, BufferedImage>>> callback, Path File) {
        this.File = File;
        callBack = callback;
        ImageAddress = null;
    }

    public StegnoAnalysis(BiConsumer<Boolean, List<Pair<String, BufferedImage>>> callback, File file) {
        this(callback, file.toPath());
    }

    public StegnoAnalysis(BiConsumer<Boolean, List<Pair<String, BufferedImage>>> callback, URL Address) {
        this.ImageAddress = Address;
        callBack = callback;
        File = null;
    }

    public Path getFilePath() {
        return File;
    }

    public String getAnalysisSource() {
        if (File != null) {
            return File.toString();
        } else {
            return ImageAddress.toString();
        }
    }

    public String getSourceName() {
        if (File != null) {
            return File.getFileName().toString().strip();
        } else {
            return ImageAddress.getPath().toString();
        }
    }

    /**
     * Create a Buffer Image that contains pixels with the FillColor where the
     * specific pixel data r=g=b. for example. if a pixel color is all 0 (black)
     * or White (all 1) or any color that for all color the data is the same for
     * example. "1f1f1f" and so on. Highlights just the pixels for which r=g=b
     * (this is also known as "gray bits" because all R G B are the same value)
     *
     * @see
     * {@link https://web.stanford.edu/class/cs101/image-6-grayscale-adva.html}
     *
     */
    private BufferedImage TranformSymetricPixels(Color FillColor) {
        return ImageCache.getSymetricPixels(FillColor);
    }

    private BufferedImage getGrayScaleCopy() {
        return ImageCache.getGrayScale();
    }

    protected BufferedImage getUnEditedCopy() {
        return ImageCache.getCloneImage();
    }

    /**
     * https://stackoverflow.com/questions/687261/converting-rgb-to-grayscale-intensity
     * https://en.wikipedia.org/wiki/Grayscale
     * http://www.w3.org/Graphics/Color/sRGB
     * https://github.com/WinMerge/freeimage/blob/master/Source/Utilities.h#L478
     * https://stackoverflow.com/questions/9131678/convert-a-rgb-image-to-grayscale-image-reducing-the-memory-in-java
     *
     * @return
     */
    private BufferedImage TranformGreyScaleSlow(boolean NoGamaCorrection) {
        return ImageCache.MathOnPixels(BufferedImage.TYPE_BYTE_GRAY, RGB -> {
            if (NoGamaCorrection) {
                // Calculate luminance:
                var lum = 0.2126f * RGB[CanvasContainer.RED]
                        + 0.7152f * RGB[CanvasContainer.GREEN]
                        + 0.0722f * RGB[CanvasContainer.BLUE];
                return new Short[]{(short) (lum + 0.5F)};
            }
            // Normalize and gamma correct:Rec709 (HDTV)
            var rr = Math.pow(RGB[CanvasContainer.RED] / 255.0f, 2.2f);
            var gg = Math.pow(RGB[CanvasContainer.GREEN] / 255.0f, 2.2f);
            var bb = Math.pow(RGB[CanvasContainer.BLUE] / 255.0f, 2.2f);
            // Calculate luminance:
            var lum = 0.2126f * rr + 0.7152f * gg + 0.0722f * bb;

            // Gamma compand and rescale to byte range:
            short grayLevel = (short) (255.0 * Math.pow(lum, 1.0 / 2.2));
            return new Short[]{grayLevel};
        });
    }

    private BufferedImage Forthofbyte(int part) {
        var base = 0b11 << part * 2;
        var move = 6 - 2 * part;
        //i think MathOnPixels is a bit slow due loops. we could work on a better one
        //that works on less loops the current one. is slow due several iterations 
        //on changes to code that is itended to run once not on a loop
        return ImageCache.MathOnPixels(BufferedImage.TYPE_4BYTE_ABGR, ARGB -> {
            var results = new Short[4];
            results[CanvasContainer.ALPHA] = CanvasContainer.MAXUBYTE;
            results[CanvasContainer.RED] = (short) ((ARGB[CanvasContainer.RED] & base) << move);
            results[CanvasContainer.GREEN] = (short) ((ARGB[CanvasContainer.GREEN] & base) << move);
            results[CanvasContainer.BLUE] = (short) ((ARGB[CanvasContainer.BLUE] & base) << move);
            return results;
        });
    }

    /**
     * Inverts the color of the image using HSV Rotation. <br>
     * <a href="https://www.niwa.nu/2013/05/math-behind-colorspace-conversions-rgb-hsl/">Source
     * 1</a>
     * <a href="https://geraldbakker.nl/psnumbers/hsb-explained.html">Source
     * 2</a>
     *
     * @return a instance of BufferImage with the inverted HUE colors.
     */
    private void getHSVInversions(List<Pair<String, BufferedImage>> storage) {
        //inverted hue 
        var transform = ImageCache.createBIemptyCopy();
        //iverted hue and Brightness 
        var transform2 = ImageCache.createBIemptyCopy();
        //inverted saturation
        var transform3 = ImageCache.createBIemptyCopy();
        //inverted brightness only.
        var transform4 = ImageCache.createBIemptyCopy();

        ImageCache.MathOnPixelsbyIndex((RGB, Index) -> {
            float[] HSV = new float[3];
            Color.RGBtoHSB(
                    RGB[CanvasContainer.RED],
                    RGB[CanvasContainer.GREEN],
                    RGB[CanvasContainer.BLUE],
                    HSV);
            var invertedHue = (HSV[0] + 0.5f) % 1f;
            var invertedbright = 1f - HSV[2];
            var InvertedColor = Color.HSBtoRGB(invertedHue, HSV[1], HSV[2]);
            transform.getRaster().getDataBuffer().setElem(Index, InvertedColor);
            InvertedColor = Color.HSBtoRGB(invertedHue, HSV[1], invertedbright);
            transform2.getRaster().getDataBuffer().setElem(Index, InvertedColor);
            InvertedColor = Color.HSBtoRGB(HSV[1], 1f - HSV[1], HSV[2]);
            transform3.getRaster().getDataBuffer().setElem(Index, InvertedColor);
            InvertedColor = Color.HSBtoRGB(HSV[1], HSV[1], invertedbright);
            transform4.getRaster().getDataBuffer().setElem(Index, InvertedColor);
        });
        var e = new Pair<>(TransformAnalysis.InvertHue.Name, transform);
        storage.add(e);
        publish(e);
        e = new Pair<>(TransformAnalysis.InvertHueBright.Name, transform2);
        storage.add(e);
        publish(e);
        e = new Pair<>(TransformAnalysis.InvertSaturation.Name, transform3);
        storage.add(e);
        publish(e);
        e = new Pair<>(TransformAnalysis.InvertBright.Name, transform4);
        storage.add(e);
        publish(e);
    }

    /**
     * Inverts the color of the image using HSV Rotation. <br>
     * <a href="https://www.niwa.nu/2013/05/math-behind-colorspace-conversions-rgb-hsl/">Source
     * 1</a>
     * <a href="https://geraldbakker.nl/psnumbers/hsb-explained.html">Source
     * 2</a>
     *
     * @return a instance of BufferImage with the inverted HUE colors.
     */
    private BufferedImage HueInversionHSV() {
        //inverted hue 
        return ImageCache.MathOnPixels(BufferedImage.TYPE_INT_ARGB, RGB -> {
            float[] HSV = new float[3];
            Color.RGBtoHSB(
                    RGB[CanvasContainer.RED],
                    RGB[CanvasContainer.GREEN],
                    RGB[CanvasContainer.BLUE],
                    HSV);
            var InvertedColor = Color.HSBtoRGB((HSV[0] + 0.5f) % 1f, HSV[1], HSV[2]);
            var results = new Short[4];
            results[CanvasContainer.ALPHA] = (short) ((InvertedColor >> 24) & CanvasContainer.MAXUBYTE);
            results[CanvasContainer.RED] = (short) ((InvertedColor >> 16) & CanvasContainer.MAXUBYTE);
            results[CanvasContainer.GREEN] = (short) ((InvertedColor >> 8) & CanvasContainer.MAXUBYTE);
            results[CanvasContainer.BLUE] = (short) (InvertedColor & CanvasContainer.MAXUBYTE);
            return results;
        });
    }

    /**
     * Inverts the RGB color of the image.
     *
     * @return a instance of BufferImage with the inverted color data
     */
    private BufferedImage inversionRGB() {
        return ImageCache.MathOnPixels(BufferedImage.TYPE_4BYTE_ABGR, ARGB -> {
            var results = new Short[4];
            results[CanvasContainer.ALPHA] = CanvasContainer.MAXUBYTE;
            results[CanvasContainer.RED] = (short) (ARGB[CanvasContainer.RED] ^ CanvasContainer.RGBMASK);
            results[CanvasContainer.GREEN] = (short) (ARGB[CanvasContainer.GREEN] ^ CanvasContainer.RGBMASK);
            results[CanvasContainer.BLUE] = (short) (ARGB[CanvasContainer.BLUE] ^ CanvasContainer.RGBMASK);
            return results;
        });
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

    private List<Pair<String, BufferedImage>> getImagePerBitOnBlueChannel(List<Pair<String, BufferedImage>> storage) {
        storage = storage == null ? new ArrayList<>(8) : storage;
        for (int index = 0; index < 8; index++) {
            storage.add(new Pair<>(String.format("Blue Bit at the %d bit", index + 1), ImageCache.getBlueForIndex(index, Color.BLUE)));
        }
        return storage;
    }

    private List<Pair<String, BufferedImage>> getImagePerBitOnGreenChannel(List<Pair<String, BufferedImage>> storage) {
        storage = storage == null ? new ArrayList<>(8) : storage;
        for (int index = 0; index < 8; index++) {
            storage.add(new Pair<>(String.format("Green Bit at the %d bit", index), ImageCache.getGreenForIndex(index, Color.GREEN)));
        }
        return storage;
    }

    private List<Pair<String, BufferedImage>> getImagePerBitOnRedChannel(List<Pair<String, BufferedImage>> storage) {
        storage = storage == null ? new ArrayList<>(8) : storage;
        for (int index = 0; index < 8; index++) {
            storage.add(new Pair<>(String.format("Red Bit at the %d bit", index), ImageCache.getRedForIndex(index, Color.RED)));
        }
        return storage;
    }

    private List<Pair<String, BufferedImage>> getImagePerBitOnAlphaChannel(List<Pair<String, BufferedImage>> storage) {
        storage = storage == null ? new ArrayList<>(8) : storage;
        if (ImageCache.HasAlphaChannel()) {
            for (int index = 0; index < 8; index++) {
                storage.add(new Pair<>(String.format("Alpha Bit at the %d bit", index), ImageCache.getAlphaForIndex(index, Color.BLACK)));
            }
        }
        return storage;
    }

    @Override
    protected List<Pair<String, BufferedImage>> doInBackground() throws Exception {
        var Stage = "BootStrapping the Analysis";
        loger.log(Level.INFO, Stage);
        firePropertyChange(STATE_STAGE, null, Stage);
        Stage = bootstrapImage(Stage);
        var prev = Stage;
        Stage = "Image is loaded. Starting Transformation Analysis.";
        firePropertyChange(STATE_STAGE, prev, Stage);
        loger.log(Level.INFO, Stage);
        return RunTrasFormations(Stage);
    }

    private String bootstrapImage(String Stage) throws IOException {
        try {
            if (File != null) {
                var prev = Stage;
                Stage = String.format("Loading the File %s", File.getFileName().toString());
                firePropertyChange(STATE_STAGE, prev, Stage);
                ImageCache = new CanvasContainer(File);
            } else {
                var prev = Stage;
                Stage = String.format("Loading the URL %s", ImageAddress.getPath().toString());
                firePropertyChange(STATE_STAGE, prev, Stage);
                ImageCache = new CanvasContainer(ImageAddress);
            }
        } catch (IOException ex) {
            var prev = Stage;
            Stage = String.format("Unable to Load the Image for Analysis due a error: %s", ex.getMessage());
            firePropertyChange(STAGE_ERROR, prev, Stage);
            loger.getLogger(StegnoAnalysis.class.getName()).log(Level.SEVERE, "Error Loading the underline Image from the provided Source", ex);
            throw ex;//rethow so the Future class traps the error at the setException
        }
        return Stage;
    }

    protected List<Pair<String, BufferedImage>> RunTrasFormations(String Stage) {
        var list = new ArrayList<Pair<String, BufferedImage>>(20);
        var stack = new Stack<RecursiveTask<Pair<String, BufferedImage>>>();
        var stackListResult = new Stack<RecursiveTask<List<Pair<String, BufferedImage>>>>();
        /**
         * *****************************************************
         */
        loger.log(Level.INFO, "Schelduling Tasks");
        bookandStartListTask(stackListResult, new RecursiveTask<List<Pair<String, BufferedImage>>>() {
            @Override
            protected List<Pair<String, BufferedImage>> compute() {
                loger.log(Level.INFO, "Start getHSVInversions Task");
                var list = new ArrayList<Pair<String, BufferedImage>>(4);
                getHSVInversions(list);
                loger.log(Level.INFO, "Task: getHSVInversions, Done");
                return list;
            }
        });

        bookandStartListTask(stackListResult, new RecursiveTask<List<Pair<String, BufferedImage>>>() {
            @Override
            protected List<Pair<String, BufferedImage>> compute() {
                loger.log(Level.INFO, "Start getImagePerBitOnBlueChannel Task");
                var list = getImagePerBitOnBlueChannel(null);
                for (var e : list) {
                    publish(e);
                }
                loger.log(Level.INFO, "Task: getImagePerBitOnBlueChannel, Done");
                return list;
            }
        });
        bookandStartListTask(stackListResult, new RecursiveTask<List<Pair<String, BufferedImage>>>() {
            @Override
            protected List<Pair<String, BufferedImage>> compute() {
                loger.log(Level.INFO, "Start getImagePerBitOnGreenChannel Task");
                var list = getImagePerBitOnGreenChannel(null);
                for (var e : list) {
                    publish(e);
                }
                loger.log(Level.INFO, "Task: getImagePerBitOnGreenChannel, Done");
                return list;
            }
        });
        bookandStartListTask(stackListResult, new RecursiveTask<List<Pair<String, BufferedImage>>>() {
            @Override
            protected List<Pair<String, BufferedImage>> compute() {
                loger.log(Level.INFO, "Start getImagePerBitOnRedChannel Task");
                var list = getImagePerBitOnRedChannel(null);
                for (var e : list) {
                    publish(e);
                }
                loger.log(Level.INFO, "Task: getImagePerBitOnRedChannel, Done");
                return list;
            }
        });
        bookandStartListTask(stackListResult, new RecursiveTask<List<Pair<String, BufferedImage>>>() {
            @Override
            protected List<Pair<String, BufferedImage>> compute() {
                loger.log(Level.INFO, "Start getImagePerBitOnAlphaChannel Task");
                var list = getImagePerBitOnAlphaChannel(null);
                for (var e : list) {
                    publish(e);
                }
                loger.log(Level.INFO, "Task: getImagePerBitOnAlphaChannel, Done");
                return list;
            }
        });
        bookandStartTask(stack, new RecursiveTask<Pair<String, BufferedImage>>() {
            @Override
            protected Pair<String, BufferedImage> compute() {
                var created = new Pair<>(TransformAnalysis.OriginalImage.Name, getUnEditedCopy());
                publish(created);
                loger.log(Level.INFO, "Task: Copy Original, Done");
                return created;
            }
        });
        bookandStartTask(stack, new RecursiveTask<Pair<String, BufferedImage>>() {
            @Override
            protected Pair<String, BufferedImage> compute() {
                var created = new Pair<>(TransformAnalysis.GreyMap.Name, TranformSymetricPixels(Color.BLACK));
                publish(created);
                loger.log(Level.INFO, "Task: GreyMask, Done");
                return created;
            }
        });
        bookandStartTask(stack, new RecursiveTask<Pair<String, BufferedImage>>() {
            @Override
            protected Pair<String, BufferedImage> compute() {
                var created = new Pair<>(TransformAnalysis.GreyScale.Name, getGrayScaleCopy());
                publish(created);
                loger.log(Level.INFO, "Task: GreyScale, Done");
                return created;
            }
        });
        bookandStartTask(stack, new RecursiveTask<Pair<String, BufferedImage>>() {
            @Override
            protected Pair<String, BufferedImage> compute() {
                loger.log(Level.INFO, "Start FirstForthImage Task");
                var created = new Pair<>(TransformAnalysis.FirstForthImage.Name, Forthofbyte(0));
                publish(created);
                loger.log(Level.INFO, "Task: FirstForthImage, Done");
                return created;
            }
        });
        bookandStartTask(stack, new RecursiveTask<Pair<String, BufferedImage>>() {
            @Override
            protected Pair<String, BufferedImage> compute() {
                loger.log(Level.INFO, "Start SecondForthImage Task");
                var created = new Pair<>(TransformAnalysis.SecondForthImage.Name, Forthofbyte(1));
                publish(created);
                loger.log(Level.INFO, "Task: SecondForthImage, Done");
                return created;
            }
        });
        bookandStartTask(stack, new RecursiveTask<Pair<String, BufferedImage>>() {
            @Override
            protected Pair<String, BufferedImage> compute() {
                loger.log(Level.INFO, "Start ThirdForthImage Task");
                var created = new Pair<>(TransformAnalysis.ThirdForthImage.Name, Forthofbyte(2));
                loger.log(Level.INFO, "Task: ThirdForthImage, Done");
                publish(created);
                return created;
            }
        });
        bookandStartTask(stack, new RecursiveTask<Pair<String, BufferedImage>>() {
            @Override
            protected Pair<String, BufferedImage> compute() {
                loger.log(Level.INFO, "Start ForthForthImage Task");
                var created = new Pair<>(TransformAnalysis.ForthForthImage.Name, Forthofbyte(3));
                publish(created);
                loger.log(Level.INFO, "Task: ForthForthImage, Done");
                return created;
            }
        });
        bookandStartTask(stack, new RecursiveTask<Pair<String, BufferedImage>>() {
            @Override
            protected Pair<String, BufferedImage> compute() {
                loger.log(Level.INFO, "Start XorInversion Task");
                var created = new Pair<>(TransformAnalysis.XorInversion.Name, inversionRGB());
                publish(created);
                loger.log(Level.INFO, "Task: XorInversion, Done");
                return created;
            }
        });
        bookandStartTask(stack, new RecursiveTask<Pair<String, BufferedImage>>() {
            @Override
            protected Pair<String, BufferedImage> compute() {
                loger.log(Level.INFO, "Start BluePixels Task");
                var created = new Pair<>(TransformAnalysis.BluePixels.Name, ImageCache.getBlueImage());
                publish(created);
                loger.log(Level.INFO, "Task: BluePixels, Done");
                return created;
            }
        });
        bookandStartTask(stack, new RecursiveTask<Pair<String, BufferedImage>>() {
            @Override
            protected Pair<String, BufferedImage> compute() {
                loger.log(Level.INFO, "Start GreenPixels Task");
                var created = new Pair<>(TransformAnalysis.GreenPixels.Name, ImageCache.getGreenImage());
                publish(created);
                loger.log(Level.INFO, "Task: GreenPixels, Done");
                return created;
            }
        });
        bookandStartTask(stack, new RecursiveTask<Pair<String, BufferedImage>>() {
            @Override
            protected Pair<String, BufferedImage> compute() {
                loger.log(Level.INFO, "Start RedPixels Task");
                var created = new Pair<>(TransformAnalysis.RedPixels.Name, ImageCache.getRedImage());
                publish(created);
                loger.log(Level.INFO, "Task: RedPixels, Done");
                return created;
            }
        });
        bookandStartTask(stack, new RecursiveTask<Pair<String, BufferedImage>>() {
            @Override
            protected Pair<String, BufferedImage> compute() {
                loger.log(Level.INFO, "Start AlphaPixels Task");
                var created = new Pair<>(TransformAnalysis.AlphaPixels.Name, ImageCache.getAlphaImage());
                publish(created);
                loger.log(Level.INFO, "Task: AlphaPixels, Done");
                return created;
            }
        });
        //list.add(new Pair<>("Grey Scale REC709 (gamma Corrected)", TranformGreyScaleSlow()));
        //list.add(new Pair<>("Grey Scale REC709 fast", TranformGreyScaleSlow(true)));
        loger.log(Level.INFO, "Joining Tasks");
        while (!stack.isEmpty()) {
            list.add(stack.pop().join());
        }
        while (!stackListResult.isEmpty()) {
            list.addAll(stackListResult.pop().join());
        }
        loger.log(Level.INFO, "done");
        System.gc();
        return list;
    }

    private void bookandStartTask(Stack<RecursiveTask<Pair<String, BufferedImage>>> stack, RecursiveTask<Pair<String, BufferedImage>> recursiveTask) {
        var Pool = ForkJoinPool.commonPool();
        stack.push(recursiveTask);
        //recursiveTask.fork();
        Pool.submit(recursiveTask);
    }

    private void bookandStartListTask(Stack<RecursiveTask<List<Pair<String, BufferedImage>>>> stack, RecursiveTask<List<Pair<String, BufferedImage>>> recursiveTask) {
        var Pool = ForkJoinPool.commonPool();
        stack.push(recursiveTask);
        //recursiveTask.fork();
        Pool.submit(recursiveTask);
    }

    @Override
    protected void process(List<Pair<String, BufferedImage>> chunks) {
        callBack.accept(false, chunks);
    }

    @Override
    protected void done() {
        try {
            loger.log(Level.INFO, "StegnoAnalysis Done, Calling back");
            callBack.accept(true, get());
        } catch (InterruptedException ex) {
            loger.log(Level.SEVERE, "Task was Cancelled. or interrupted.", ex);
        } catch (ExecutionException ex) {
            //TODO add a callback if error.
            loger.log(Level.SEVERE, "an error happend during execution", ex.getCause());
        }
    }

    public static List<String> getAnalysisTransformationNames() {
        var list = new ArrayList<String>(TransformAnalysis.values().length);
        for (var ordered : TransformAnalysis.values()) {
            list.add(ordered.Name);
        }
        return list;
    }

}
