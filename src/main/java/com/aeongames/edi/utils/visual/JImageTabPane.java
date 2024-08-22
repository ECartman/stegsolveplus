/*
 *
 * Copyright © 2008-2024 Eduardo Vindas. All rights reserved.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.aeongames.edi.utils.visual;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.io.IOException;
import java.util.Objects;
import javax.imageio.ImageIO;

/**
 *
 * A component that lets the user switch between a group of components by
 * clicking on a tab with a given title and/or icon. this version extends on
 * <a href="https://docs.oracle.com/javase/tutorial/uiswing/components/tabbedpane.html">How
 * to Use Tabbed Panes</a>
 * <br>
 * this version extends from a normal {@link JTabbedPane} as this version
 * supports Rendering a image when there are not tabs on the pane.
 *
 * @author Eduardo V
 * @see JTabbedPane
 */
public class JImageTabPane extends javax.swing.JTabbedPane {

    //<editor-fold defaultstate="collapsed" desc="Variables">
    /**
     * the policy to use to resize and or print the image the default is Scale
     * Small Only
     */
    private ImageScaleComponents ScalePolicy = ImageScaleComponents.SCALE_SMALL_ONLY;
    /**
     * the default image location we use on our Image panel when none is
     * provided.
     */
    private static final String DEF_LOGO = ImagePanel.DEF_LOGO;
    /**
     * the image to be show or process.
     */
    private Image img;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Constructors">
    /**
     * creates a new instance of the JImageTabPane using the default logo. also
     * set the TabLayout policy as
     * {@link javax.swing.JTabbedPane#SCROLL_TAB_LAYOUT}
     */
    public JImageTabPane() {
        super();
        img = loadDefault();
        setSize(new Dimension(img.getWidth(null), img.getHeight(null)));
        this.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
    }

    /**
     * creates a new instance of the JImageTabPane tab pane using the selected
     * image passed by parameter. of the Image is null we will use the default.
     * set the TabLayout policy as
     * {@link javax.swing.JTabbedPane#SCROLL_TAB_LAYOUT}
     *
     * @param todisplay the {@link java.awt.Image} to display, on the pane
     */
    public JImageTabPane(Image todisplay) {
        super();
        img = todisplay != null ? todisplay : loadDefault();
        setSize(new Dimension(img.getWidth(null), img.getHeight(null)));
        this.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
    }
    //</editor-fold>

    /**
     * Loads the default image to use on this pane.
     * @return 
     */
    private Image loadDefault() {
        Image loaded = null;
        try {
            loaded = ImageIO.read(this.getClass().getResource(DEF_LOGO));
        } catch (IOException ex) {
            try {
                loaded = java.awt.Toolkit.getDefaultToolkit().getImage(this.getClass().getResource(DEF_LOGO));
            } catch (Exception sub) {
                //we should print error if debug build. here. 
            }
        }
        return loaded;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        var tmp = new RenderingHints(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        tmp.put(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
//      tmp.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHints(tmp);
        if (this.getTabCount() == 0) {
            switch (ScalePolicy) {
                case SCALE_ALWAYS:
                    paintRespectRatio(g);
                    break;
                case SCALE_USE_ALL_SPACE:
                    paintDefault(g);
                    break;
                default:
                case SCALE_SMALL_ONLY:
                    paintToSize(g);
                    break;
            }
        } else {
            setBackground(getBackground());
        }
    }

    /**
     * sets the policy for resize the background image
     * it has to be one of the {@link ImageScaleComponents} values
     *
     * @param policy the policy to use.
     * @throws NullPointerException if the parameter is null.
     * @see ImageScaleComponents
     */
    public void setBackgroundPolicy(ImageScaleComponents policy) {
        Objects.requireNonNull(policy, "Invalid Policy");
        ScalePolicy = policy;
    }

    /**
     * returns the current policy
     * @return the scale policy
     */
    public ImageScaleComponents getBacgroundScalePolicy() {
        return ScalePolicy;
    }

    /**
     * changes the image to be painted.
     *
     * @param todisplay
     */
    protected void changeImage(Image todisplay) {
        if (todisplay != null) {
            this.img = todisplay;
        }
    }

    /**
     * paints the image from size 1x1 to the image actual size that of course we
     * will respect the image ratio so the image will be show as it should with
     * not forced size also will center the image
     */
    private void paintToSize(Graphics g) {
        if (img.getWidth(null) > -1 && img.getWidth(null) < this.getWidth() && img.getHeight(null) < this.getHeight()) {
            int Width = (this.getWidth() / 2) - img.getWidth(null) / 2;
            int Height = (this.getHeight() / 2) - img.getHeight(null) / 2;
            g.drawImage(img, Width, Height, img.getWidth(null), img.getHeight(null), null);
        } else {
            paintRespectRatio(g);
        }
    }

    /**
     * paints the image from size 1x1 to whatever is possible without error (yet
     * unknown to me) use with caution also id the image is expanded to much
     * might eventually look... no good... it respect the image ratio BTW
     */
    private void paintRespectRatio(Graphics g) {
        //ok now we want to keep the image ratio so lets try the new aproach
        int[] size = ImageUtils.keep_ratio_for_size(this.getWidth(), this.getHeight(), img);
        g.drawImage(img, size[2], size[3], size[0], size[1], null);
    }

    /**
     * sets the image on the panel but stretch to the PANEL size so this will
     * not respect the ratio will fill the hold panel.
     */
    private void paintDefault(Graphics g) {
        g.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), null);
    }
}
