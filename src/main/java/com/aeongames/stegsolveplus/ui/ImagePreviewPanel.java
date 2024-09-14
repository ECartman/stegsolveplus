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
package com.aeongames.stegsolveplus.ui;

import com.aeongames.edi.utils.text.LabelText;
import com.aeongames.edi.utils.visual.Panels.ImagePanel;
import java.awt.CardLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;

/**
 *
 * @author cartman
 */
public class ImagePreviewPanel extends javax.swing.JPanel {

    public static final String ThumbClickEvent = "ThumbClicked";
    private static final String THUMBNAIL = "ImageView";
    private final String PreviewTitle;
    private BufferedImage ImageToPreview;
    private final CardLayout Layout;

    /**
     * Creates new form ImagePreviewPanel
     *
     * @param Title the title to display
     */
    public ImagePreviewPanel(String Title) {
        PreviewTitle = Objects.requireNonNull(Title, "The title for the thumb is required and cannot be null");
        ImageToPreview = null;
        initComponents();
        if (getLayout() instanceof CardLayout ly) {
            Layout = ly;
        } else {
            Layout = null;
        }
    }

    public ImagePreviewPanel(String Title, BufferedImage Image) {
        final var img = Objects.requireNonNull(Image, "The provided image is null");
        PreviewTitle = Objects.requireNonNull(Title, "The title for the thumb is required and cannot be null");
        var totalpix = img.getWidth() * img.getHeight();
        if (totalpix >= 4000000) {
            addAncestorListener(new javax.swing.event.AncestorListener() {
                @Override
                public void ancestorAdded(AncestorEvent event) {
                }

                @Override
                public void ancestorRemoved(AncestorEvent event) {
                }

                @Override
                public void ancestorMoved(AncestorEvent event) {
                    SetImage(ImageToPreview);
                    //we no longer need to listen to events. remove this listener
                    removeAncestorListener(this);
                }
            });
        } else {
            ImageToPreview = img;
        }
        initComponents();
        if (getLayout() instanceof CardLayout ly) {
            Layout = ly;
        } else {
            Layout = null;
        }
        if (ImageToPreview != null) {
            Layout.show(this, THUMBNAIL);
        } else {
            ImageToPreview = img;
        }
    }

    public final void SetImage(BufferedImage img) {
        ImageToPreview = img;
        var totalpix = ImageToPreview.getWidth() * ImageToPreview.getHeight();
        var acceptable = this.getWidth() * this.getHeight() * 1.50;
        if (totalpix >= acceptable) {
            new Thread(() -> {
                double scalex = (double) (this.getWidth() * 1.50) / ImageToPreview.getWidth();
                double scaley = (double) (this.getHeight() * 1.50) / ImageToPreview.getHeight();
                BufferedImage tmp = scale(ImageToPreview, Math.min(scalex, scaley), AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
                try {
                    SwingUtilities.invokeAndWait(() -> {
                        ImagePreviewPanel.setImage(tmp);
                        Layout.show(this, THUMBNAIL);
                        repaint();
                    });
                } catch (InterruptedException | InvocationTargetException ex) {
                    Logger.getLogger(ImagePreviewPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }, "Thumbnail Resize").start();
            //if the image is  50%  or more than the size of this panel 
            //for sake of performance lets Scale the image down to our acceptable region
            //this also helps as the image will have its Raster untouch to be render.

        } else {
            ImagePreviewPanel.setImage(ImageToPreview);
            Layout.show(this, THUMBNAIL);
            repaint();
        }
    }

    private static BufferedImage scale(final BufferedImage before, final double scale, final int type) {
        int w = before.getWidth();
        int h = before.getHeight();
        int w2 = (int) (w * scale);
        int h2 = (int) (h * scale);
        BufferedImage after = new BufferedImage(w2, h2, before.getType());
        AffineTransform scaleInstance = AffineTransform.getScaleInstance(scale, scale);
        AffineTransformOp scaleOp = new AffineTransformOp(scaleInstance, type);
        scaleOp.filter(before, after);
        return after;
    }

    public BufferedImage getImage() {
        return ImageToPreview;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        LoadPanel = new com.aeongames.edi.utils.visual.Panels.TranslucentPanel();
        txtloading = new javax.swing.JLabel();
        jProgressBar1 = new javax.swing.JProgressBar();
        jLabel1 = new javax.swing.JLabel();
        ImagThumbpanel = new com.aeongames.edi.utils.visual.Panels.TranslucentPanel();
        jPanel1 = new javax.swing.JPanel();
        txtTitle = new javax.swing.JLabel();
        ImagePreviewPanel = ImageToPreview == null
        ? new ImagePanel()
        : new ImagePanel(ImageToPreview);

        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        setMinimumSize(new java.awt.Dimension(300, 270));
        setOpaque(false);
        setPreferredSize(new java.awt.Dimension(300, 300));
        setLayout(new java.awt.CardLayout());

        txtloading.setFont(new java.awt.Font(txtloading.getFont().getName(), 0, 15));
        txtloading.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        txtloading.setText(LabelText.getTrimmedtoComponentsize(PreviewTitle,txtloading,200));
        txtloading.setToolTipText("");
        txtloading.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                txtloadingComponentResized(evt);
            }
        });

        jProgressBar1.setIndeterminate(true);

        jLabel1.setFont(new java.awt.Font(txtloading.getFont().getName(), 0, 15));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Loading");

        javax.swing.GroupLayout LoadPanelLayout = new javax.swing.GroupLayout(LoadPanel);
        LoadPanel.setLayout(LoadPanelLayout);
        LoadPanelLayout.setHorizontalGroup(
            LoadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(LoadPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(LoadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
                    .addComponent(txtloading, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        LoadPanelLayout.setVerticalGroup(
            LoadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(LoadPanelLayout.createSequentialGroup()
                .addContainerGap(53, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtloading, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(95, Short.MAX_VALUE))
        );

        add(LoadPanel, "Loading");

        ImagThumbpanel.setBackground(new java.awt.Color(255, 255, 255));
        ImagThumbpanel.setArcHeight(20);
        ImagThumbpanel.setArcWidth(20);

        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        txtTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        txtTitle.setText(PreviewTitle);
        txtTitle.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(txtTitle, javax.swing.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(txtTitle, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 31, Short.MAX_VALUE)
        );

        ImagePreviewPanel.setOpaque(false);
        ImagePreviewPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                ImagePreviewPanelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                ImagePreviewPanelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                ImagePreviewPanelMouseExited(evt);
            }
        });
        ImagePreviewPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                ImagePreviewPanelComponentResized(evt);
            }
        });
        ImagePreviewPanel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        javax.swing.GroupLayout ImagePreviewPanelLayout = new javax.swing.GroupLayout(ImagePreviewPanel);
        ImagePreviewPanel.setLayout(ImagePreviewPanelLayout);
        ImagePreviewPanelLayout.setHorizontalGroup(
            ImagePreviewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        ImagePreviewPanelLayout.setVerticalGroup(
            ImagePreviewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 235, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout ImagThumbpanelLayout = new javax.swing.GroupLayout(ImagThumbpanel);
        ImagThumbpanel.setLayout(ImagThumbpanelLayout);
        ImagThumbpanelLayout.setHorizontalGroup(
            ImagThumbpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(ImagePreviewPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        ImagThumbpanelLayout.setVerticalGroup(
            ImagThumbpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ImagThumbpanelLayout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(ImagePreviewPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        add(ImagThumbpanel, "ImageView");
    }// </editor-fold>//GEN-END:initComponents

    private void txtloadingComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_txtloadingComponentResized
        txtloading.setText(LabelText.getTrimmedtoComponentsize(PreviewTitle, txtloading, 200));
    }//GEN-LAST:event_txtloadingComponentResized

    private void ImagePreviewPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_ImagePreviewPanelMouseClicked
        if (evt.getClickCount() >= 2) {
            evt.consume();
            firePropertyChange(ThumbClickEvent, PreviewTitle, ImageToPreview);
        }
    }//GEN-LAST:event_ImagePreviewPanelMouseClicked

    private void ImagePreviewPanelComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_ImagePreviewPanelComponentResized
        //TO CONSIDER if the Resize is Major. Recalculate or create a new copy of the thumbnail for the ImagePanel
    }//GEN-LAST:event_ImagePreviewPanelComponentResized

    private void ImagePreviewPanelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_ImagePreviewPanelMouseEntered
    }//GEN-LAST:event_ImagePreviewPanelMouseEntered

    private void ImagePreviewPanelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_ImagePreviewPanelMouseExited
    }//GEN-LAST:event_ImagePreviewPanelMouseExited


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.aeongames.edi.utils.visual.Panels.TranslucentPanel ImagThumbpanel;
    private com.aeongames.edi.utils.visual.Panels.ImagePanel ImagePreviewPanel;
    private com.aeongames.edi.utils.visual.Panels.TranslucentPanel LoadPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JLabel txtTitle;
    private javax.swing.JLabel txtloading;
    // End of variables declaration//GEN-END:variables
}
