/*
 * Copyright © 2024-2025 Eduardo Vindas. All rights reserved.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.aeongames.stegsolveplus.ui;

import java.net.URL;
import java.util.List;
import java.awt.Image;
import java.util.Objects;
import java.util.HashMap;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;
import java.util.function.Consumer;
import java.awt.image.BufferedImage;
import com.aeongames.edi.utils.data.Pair;
import java.beans.PropertyChangeListener;
import com.aeongames.edi.utils.error.ErrorData;
import com.aeongames.edi.utils.visual.panels.ImagePanel;
import com.aeongames.stegsolveplus.ui.tabcomponents.Tab;
import com.aeongames.edi.utils.visual.ImageScaleComponents;
import com.aeongames.edi.utils.visual.panels.ErrorGlassPane;
import com.aeongames.stegsolveplus.ui.tabcomponents.TabClose;
import com.aeongames.stegsolveplus.StegnoTools.StegnoAnalyzer;

/**
 *
 * @author Eduardo Vindas
 */
public class InvestigationTab extends Tab {

    public final class ChangePropertys {
        public static final String BUSY = "BUSY";
        public static final String STATEINFO = "STATE_STRING";
    }
    private boolean isBusy = false;
    private final StegnoAnalyzer Analyst;
    private HashMap<String, ImagePreviewPanel> ThumbsReferences;
    private final PropertyChangeListener ThumbClickListener;
    private final InvestigationModel LinkingPojo;

    /**
     * Creates new form InvestigationTab
     * <strong> we Assume that the {@code FilePath} is NOT null and can be read
     * </strong>
     * thus the Caller need to make the check prior calling this constructor.
     * otherwise the Analysis might Fail
     *
     * @param FilePath the filePath to investigate we assume it is not null
     */
    public InvestigationTab(Path FilePath) {
        FilePath = Objects.requireNonNull(FilePath, "provided path is null");
        initComponents();
        LinkingPojo=new InvestigationModel();
        LinkingPojo.linkMetadataComponent(Metadatatxt);
        LinkingPojo.linkFiletxtFirst(txtFileText01);
        LinkingPojo.linkFiletxtSecond(txtFileText2);        
        //TODO:LinkingPojo.linkCharsetLabelfirst(txtcharset01);
        //TODO:LinkingPojo.linkCharsetLabelSecond(txtcharset02);
        LinkingPojo.setStatusBarTextPojo(pFooter.getStatusBarPojo());
        SetTitleInternal(FilePath);
        ThumbClickListener = generateThumbReader();
        Analyst = new StegnoAnalyzer(FilePath);
        Analyst.setLinkingPojo(LinkingPojo);
        prepareAnalysis();

    }

    public InvestigationTab(URL Link) {
        Link = Objects.requireNonNull(Link, "provided Link is null");
        initComponents();
        LinkingPojo=new InvestigationModel();
        LinkingPojo.linkMetadataComponent(Metadatatxt);
        LinkingPojo.setStatusBarTextPojo(pFooter.getStatusBarPojo());
        SetTitleInternal(Link);
        ThumbClickListener = generateThumbReader();
        Analyst = new StegnoAnalyzer(Link);
        prepareAnalysis();
    }

    private PropertyChangeListener generateThumbReader() {
        return (evt) -> {
            if (evt.getPropertyName().equals(ImagePreviewPanel.THUMB_CLICK_EVENT)) {
                if (evt.getSource() instanceof ImagePreviewPanel) {
                    var closeComponent = new TabClose(AnalysisTabs);
                    var imagep = new ImagePanel((Image) evt.getNewValue());
                    imagep.SetBackgroundPolicy(ImageScaleComponents.SCALE_ALWAYS);
                    imagep.SmoothWhenScale(false);
                    AnalysisTabs.addTab(evt.getOldValue().toString(), imagep);
                    AnalysisTabs.setTabComponentAt(AnalysisTabs.getTabCount() - 1, closeComponent);
                    AnalysisTabs.setSelectedIndex(AnalysisTabs.getTabCount() - 1);
                    //update the information on the component.
                    closeComponent.Update(AnalysisTabs.getTabCount() - 1);
                }
            }
        };
    }

    private Consumer<List<Pair<String, BufferedImage>>> getCallback() {
        return (List) -> {
            if (Analyst.isCancelled()) {
                //if the task was cancelled that means *This* UI. is no longer valid. bail
                return;
            }
            if (List == null && Analyst.isDone()) {
                //fail. TODO: add the means to read error from the process.
                ThumbGridPanel.setLayout(null);
                ThumbGridPanel.removeAll();
                var err = new ErrorGlassPane(new ErrorData("Error Loading file.", Analyst.exceptionNow().getMessage(), Analyst.exceptionNow()),
                        (t) -> {
                            this.Close(true);
                        });
                ThumbGridPanel.add(err);
                ThumbGridPanel.setLayout(new javax.swing.BoxLayout(ThumbGridPanel, javax.swing.BoxLayout.PAGE_AXIS));
                err.setVisible(true);
                ThumbGridPanel.invalidate();
                ThumbGridPanel.repaint();
                pFooter.setFooterText(String.format("analysis Finish with errors for: %s", Analyst.getSourceName()));
                //Notify the Parent our work is done. 
                setAvailable();
                return;
            } else if (List == null || (List.isEmpty() && !Analyst.isDone())) {
                return;//null or empty is notified. nothing to do. 
            }
            for (var pair : List) {
                var mapvalue = ThumbsReferences.get(pair.getLeft());
                if (mapvalue == null) {
                    //this is a thumb that does not require a specific order so
                    //can be added at the end of the UI list
                    mapvalue = new ImagePreviewPanel(pair.getLeft(), pair.getRight());
                    mapvalue.addPropertyChangeListener(ImagePreviewPanel.THUMB_CLICK_EVENT, ThumbClickListener);
                    ThumbsReferences.put(pair.getLeft(), mapvalue);
                    ThumbGridPanel.add(mapvalue);
                }
                //note after this point avoid using ThumbsReferences use the mapvalue
                if (mapvalue.getImage() == null) {
                    mapvalue.SetImage(pair.getRight());
                }
                //redundant
                //mapvalue.repaint();
            }
            List.clear();
            ThumbGridPanel.invalidate();
            ThumbGridPanel.repaint();
            InvestigationTab.this.repaint();
            if (Analyst.isDone()) {
                pFooter.setFooterText(String.format("analysis Finish for: %s", Analyst.getSourceName()));
                //Notify the Parent our work is done. 
                setAvailable();
            }
        };
    }

    private Consumer<BufferedImage> getImageLoadCallback() {
        return (image) -> {
            if (Analyst.isCancelled()) {
                //if the task was cancelled that means *This* UI. is no longer valid. bail
                return;
            }
            if (image == null) {
                AnalysisTabs.setEnabledAt(1, true);
                AnalysisTabs.setSelectedIndex(1);
                //fail. TODO: add the means to read error from the process.
                ThumbGridPanel.setLayout(null);
                ThumbGridPanel.removeAll();
                var err = new ErrorGlassPane(new ErrorData("Error Loading file.", Analyst.exceptionNow().getMessage(), Analyst.exceptionNow()),
                        (t) -> {
                            this.Close(true);
                        });
                ThumbGridPanel.add(err);
                ThumbGridPanel.setLayout(new javax.swing.BoxLayout(ThumbGridPanel, javax.swing.BoxLayout.PAGE_AXIS));
                err.setVisible(true);
                ThumbGridPanel.invalidate();
                ThumbGridPanel.repaint();
                pFooter.setFooterText(String.format("analysis Finish with errors for: %s", Analyst.getSourceName()));
                //Notify the Parent our work is done. 
                setAvailable();
                return;
            }
            Originalimg.SetImage(image, true);
        };
    }

    public boolean IsAnalizing(Path OtherFile) {
        if (OtherFile == null) {
            return false;
        }
        var path = Analyst.getFilePath();
        if (path != null) {
            path = path.toAbsolutePath();
            OtherFile = OtherFile.toAbsolutePath();
            boolean result = false;
            try {
                result = Files.isSameFile(path, OtherFile);
            } catch (IOException ex) {
            }
            return result;
        } else {
            return OtherFile.toAbsolutePath().toString().equals(Analyst.getAnalysisSource());
        }
    }

    public boolean IsAnalizing(URL OtherFile) {
        return OtherFile.toString().equals(Analyst.getAnalysisSource());
    }

    public Object getImageResource() {
        var path = Analyst.getFilePath();
        if (path != null) {
            return path;
        }
        return Analyst.getAnalysisSource();
    }

    private void prepareAnalysis() {
        List<String> names = StegnoAnalyzer.getAnalysisTransformationNames();
        ThumbsReferences = new HashMap<>(names.size());
        for (String name : names) {
            var preview = new ImagePreviewPanel(name);
            preview.addPropertyChangeListener(ImagePreviewPanel.THUMB_CLICK_EVENT, ThumbClickListener);
            ThumbsReferences.put(name, preview);
            ThumbGridPanel.add(preview);
        }
        AnalysisTabs.setEnabledAt(1, false);
    }

    public void startAnalysis() {
        if (!Analyst.isDone()) {
            pFooter.setFooterText(String.format("Analysing File: %s", Analyst.getSourceName()));
            setBusy();
            Analyst.RunTransformations(getCallback());
            AnalysisTabs.setEnabledAt(1, true);
        }
    }
    
    public void TextAnalize() {
       if (txtFileText01.getText().strip().isBlank()) {
            pFooter.setFooterText(String.format("Analysing text from File: %s", Analyst.getSourceName()));
            setBusy();
            Analyst.RunTextAnalisys((t) -> {
                setAvailable();
            });
            AnalysisTabs.setSelectedIndex(2);
        }
    }
    
    public void addBusyListener(PropertyChangeListener listener) {
        addPropertyChangeListener(ChangePropertys.BUSY, listener);
    }

    public void removeBusyListener(PropertyChangeListener listener) {
        removePropertyChangeListener(ChangePropertys.BUSY, listener);
    }

    /**
     * set the title for this tab to match the Path. if the filename is too long
     * the function truncates to 20 characters.
     *
     * @param FilePath the file to use to setup the title.
     */
    private void SetTitleInternal(Path FilePath) {
        var Filename = FilePath.getFileName().toString().strip();
        pFooter.setFooterText(String.format("Ready File: %s", Filename));
        var extension = Filename;
        if (Objects.nonNull(Filename) && Filename.length() > 20) {
            var StartExtensionIndex = Filename.lastIndexOf('.');
            if (StartExtensionIndex >= 0) {
                extension = Filename.substring(StartExtensionIndex + 1);
            }
            //ok. we want to do something like <filenameTruncated>...<.><extension>
            //and we wante to be <=20 characters. now long extension might be a problem.
            //but we will part from an aumption that images rarely will have a
            //extension longer than .XXXX 
            //the limit is 20 characters. but substract 3 for the elipsis. thus 17 
            int limit = 20 - 3 - (extension.length());
            //if limit is less or equal to 0 truncate the whole file name and bail
            if (limit <= 0) {
                Filename = Filename.substring(0, 20);
            } else {
                Filename = String.format("%s...%s", Filename.substring(0, limit), extension);
            }
        }
        _InternalSetTitle(Filename);
    }

    private void SetTitleInternal(URL Link) {
        //assume the file is alredy non null. we are too deep if it is not a verification was missing before
        var Filename = Link.getPath();//Link.getFile();
        var index = Filename.lastIndexOf('/');
        Filename = Filename.substring(index < 0 ? 0 : index).strip();
        pFooter.setFooterText(String.format("analizing Link: %s", Link.toString()));
        var extension = Filename;
        if (Filename != null && Filename.length() > 20) {
            var StartExtensionIndex = Filename.lastIndexOf('.');//get the file type
            if (StartExtensionIndex >= 0) {
                //ditch the <.>
                extension = Filename.substring(StartExtensionIndex + 1);
            }
            //ok. we want to do something like <filenameTruncated>...<.><extension>
            //and we wante to be <=20 characters. now long extension might be a problem.
            //but we will part from an aumption that images rarely will have a
            //extension longer than .XXXX 
            //the limit is 20 characters. but substract 3 for the elipsis. thus 17 
            int limit = 20 - 3 - (extension.length());
            //if limit is less or equal to 0 truncate the whole file name and bail
            if (limit <= 0) {
                Filename = Filename.substring(0, 20);
            } else {
                Filename = String.format("%s...%s", Filename.substring(0, limit), extension);
            }
        }
        _InternalSetTitle(Filename);
    }

    //TODO::do the actual cleanup. pop if wants to keep work open?
    @Override
    public boolean Close(boolean force) {
        if (!Analyst.isDone() && !Analyst.isCancelled()) {
            Analyst.stopAnalysis();
        }
        /*
        try {
            Analyst.get();
        } catch (InterruptedException | ExecutionException ex) {
            LoggingHelper.getLogger(InvestigationTab.class.getName())
                    .log(Level.INFO, "Exception on Results, This might be expected", ex);
        }*/
        if (!force) {
            System.gc();
        }
        setAvailable();
        return true;
    }

    @Override
    protected void setBusy() {
        SetCursorBusy();
        var oldstate = isBusy;
        isBusy = true;
        pFooter.SetProgressIndeterminate();
        firePropertyChange(ChangePropertys.BUSY, oldstate, isBusy);
    }

    @Override
    protected void setAvailable() {
        var oldstate = isBusy;
        isBusy = false;
        pFooter.SetProgress(0);
        firePropertyChange(ChangePropertys.BUSY, oldstate, isBusy);
        ClearCursor();
    }

    public boolean isBusy() {
        return isBusy;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        AnalysisTabs = new com.aeongames.edi.utils.visual.panels.JImageTabPane();
        Originalimg = new com.aeongames.stegsolveplus.ui.ImagePreviewPanel();
        TransformPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        ThumbGridPanel = new javax.swing.JPanel();
        ImgInfoPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        Metadatatxt = new com.aeongames.edi.utils.visual.TranslucentTextArea();
        jScrollPane3 = new javax.swing.JScrollPane();
        txtFileText01 = new com.aeongames.edi.utils.visual.TranslucentTextArea();
        jLabel1 = new javax.swing.JLabel();
        txtcharset01 = new javax.swing.JLabel();
        txtcharset02 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        txtFileText2 = new com.aeongames.edi.utils.visual.TranslucentTextArea();
        jLabel2 = new javax.swing.JLabel();
        pFooter = new com.aeongames.stegsolveplus.ui.Footer();

        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                formComponentShown(evt);
            }
        });

        AnalysisTabs.addTab("Original Image", new javax.swing.ImageIcon(getClass().getResource("/com/aeongames/stegsolveplus/ui/image.png")), Originalimg); // NOI18N

        TransformPanel.setOpaque(false);

        jScrollPane1.setOpaque(false);
        jScrollPane1.getVerticalScrollBar().setUnitIncrement(25);

        ThumbGridPanel.setLayout(new java.awt.GridLayout(0, 3));
        jScrollPane1.setViewportView(ThumbGridPanel);

        javax.swing.GroupLayout TransformPanelLayout = new javax.swing.GroupLayout(TransformPanel);
        TransformPanel.setLayout(TransformPanelLayout);
        TransformPanelLayout.setHorizontalGroup(
            TransformPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 781, Short.MAX_VALUE)
        );
        TransformPanelLayout.setVerticalGroup(
            TransformPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 446, Short.MAX_VALUE)
        );

        AnalysisTabs.addTab("Transformations", new javax.swing.ImageIcon(getClass().getResource("/com/aeongames/stegsolveplus/ui/color.png")), TransformPanel, ""); // NOI18N

        Metadatatxt.setEditable(false);
        Metadatatxt.setColumns(20);
        Metadatatxt.setRows(5);
        jScrollPane2.setViewportView(Metadatatxt);

        txtFileText01.setColumns(20);
        txtFileText01.setRows(5);
        jScrollPane3.setViewportView(txtFileText01);

        jLabel1.setText("File as Text: ");

        txtcharset01.setText("Single Byte Characters");

        txtcharset02.setText("Wide Character");

        txtFileText2.setColumns(20);
        txtFileText2.setRows(5);
        jScrollPane4.setViewportView(txtFileText2);

        jLabel2.setText("File as Text: ");

        javax.swing.GroupLayout ImgInfoPanelLayout = new javax.swing.GroupLayout(ImgInfoPanel);
        ImgInfoPanel.setLayout(ImgInfoPanelLayout);
        ImgInfoPanelLayout.setHorizontalGroup(
            ImgInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ImgInfoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(ImgInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 769, Short.MAX_VALUE)
                    .addComponent(jScrollPane3)
                    .addComponent(jScrollPane4)
                    .addGroup(ImgInfoPanelLayout.createSequentialGroup()
                        .addGroup(ImgInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(ImgInfoPanelLayout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtcharset01, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(ImgInfoPanelLayout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtcharset02, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        ImgInfoPanelLayout.setVerticalGroup(
            ImgInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ImgInfoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(ImgInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtcharset01))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(ImgInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtcharset02))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        AnalysisTabs.addTab("Text And Metadata", new javax.swing.ImageIcon(getClass().getResource("/com/aeongames/stegsolveplus/ui/filedata.png")), ImgInfoPanel); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(AnalysisTabs, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(AnalysisTabs, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        AnalysisTabs.getAccessibleContext().setAccessibleName("");
        AnalysisTabs.getAccessibleContext().setAccessibleDescription("");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pFooter, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(pFooter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
        Analyst.LoadImageData(getImageLoadCallback());
    }//GEN-LAST:event_formComponentShown

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.aeongames.edi.utils.visual.panels.JImageTabPane AnalysisTabs;
    private javax.swing.JPanel ImgInfoPanel;
    private com.aeongames.edi.utils.visual.TranslucentTextArea Metadatatxt;
    private com.aeongames.stegsolveplus.ui.ImagePreviewPanel Originalimg;
    private javax.swing.JPanel ThumbGridPanel;
    private javax.swing.JPanel TransformPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private com.aeongames.stegsolveplus.ui.Footer pFooter;
    private com.aeongames.edi.utils.visual.TranslucentTextArea txtFileText01;
    private com.aeongames.edi.utils.visual.TranslucentTextArea txtFileText2;
    private javax.swing.JLabel txtcharset01;
    private javax.swing.JLabel txtcharset02;
    // End of variables declaration//GEN-END:variables
}
