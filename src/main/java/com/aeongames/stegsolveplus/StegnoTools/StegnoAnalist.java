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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * this class is a Holder for the information and access to the stegnography
 * tools that can be applied to an image.
 * 
 * TODO: load image from URL should be easy to add. but require changes on the UI to support it. 
 *
 * @author Ed
 */
public class StegnoAnalist {

    
     public static final String ValidImagesFiles[] = ImageIO.getReaderFormatNames();
    /**
     * the source file to read and or check data from.
     */
    private Path File        =null;
    private URL ImageAddress =null;
    
    /**
     * The Loaded image. initially this will be null
     */
    private BufferedImage originalImage;

    private Map<Integer, BufferedImage> TransformedImages;

    public StegnoAnalist(Path File) {
        this.File = File;
    }

    public StegnoAnalist(File file) {
        this.File = file.toPath();
    }
    
     public StegnoAnalist(URL Address) {
        this.ImageAddress = Address;
    }

    public void RunTrasFormations() throws IOException {
        if (originalImage == null) {
            LoadImage();
        }
    }

    private void LoadImage() throws IOException {
        if(File!=null){
            originalImage = ImageIO.read(File.toFile());
        }else{
            originalImage = ImageIO.read(ImageAddress);
        }
       
    }

}
