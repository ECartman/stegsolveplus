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
package com.aeongames.stegsolveplus.ui;

import com.aeongames.edi.utils.data.Pair;
import com.aeongames.edi.utils.error.ErrorData;
import com.aeongames.edi.utils.visual.ImageScaleComponents;
import com.aeongames.edi.utils.visual.Panels.ErrorGlassPane;
import com.aeongames.edi.utils.visual.Panels.ImagePanel;
import com.aeongames.stegsolveplus.ui.tabcomponents.Tab;
import com.aeongames.stegsolveplus.StegnoTools.StegnoAnalysis;
import com.aeongames.stegsolveplus.ui.tabcomponents.TabClose;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 *
 * @author Eduardo Vindas
 */
public class InvestigationTab extends Tab {

    public final class ChangePropertys {

        public static final String BUSY = "BUSY";
        public static final String STATEINFO = "STATE_STRING";
    }
    private boolean isBusy = true;
    private final StegnoAnalysis Analyst;
    private HashMap<String, ImagePreviewPanel> ThumbsReferences;
    private final PropertyChangeListener ThumbClickListener;
    private final Consumer<List<Pair<String, BufferedImage>>> Callback;

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
        SetTitleInternal(FilePath);
        Callback = getCallback();
        ThumbClickListener = generateThumbReader();
        Analyst = new StegnoAnalysis(Callback, FilePath);
        pFooter.SetProgressIndeterminate();
        prepareAnalysis();
    }

    public InvestigationTab(URL Link) {
        Link = Objects.requireNonNull(Link, "provided Link is null");
        initComponents();
        SetTitleInternal(Link);
        Callback = getCallback();
        ThumbClickListener = generateThumbReader();
        Analyst = new StegnoAnalysis(Callback, Link);
        pFooter.SetProgressIndeterminate();
        prepareAnalysis();
    }

    private PropertyChangeListener generateThumbReader() {
        return (evt) -> {
            if (evt.getPropertyName().equals(ImagePreviewPanel.ThumbClickEvent)) {
                if (evt.getSource() instanceof ImagePreviewPanel) {
                    var closeComponent = new TabClose(AnalysisTabs);
                    var imagep= new ImagePanel((Image) evt.getNewValue());
                    imagep.SetBackgroundPolicy(ImageScaleComponents.SCALE_ALWAYS);
                    imagep.SmoothWhenScale(false);
                    AnalysisTabs.addTab(evt.getOldValue().toString(),imagep);
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
            if(Analyst.isCancelled()){
                //if the task was cancelled that means *This* UI. is no longer valid. bail
                return;
            }
            if (List == null && Analyst.isDone()) {
                //fail. TODO: add the means to read error from the process.
                ThumbGridPanel.removeAll();
                var err = new ErrorGlassPane(new ErrorData("Error Loading file.", Analyst.exceptionNow().getMessage(), Analyst.exceptionNow()));
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
                    mapvalue.addPropertyChangeListener(ImagePreviewPanel.ThumbClickEvent, ThumbClickListener);
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

    private void prepareAnalysis() {
        List<String> names = StegnoAnalysis.getAnalysisTransformationNames();
        ThumbsReferences = new HashMap<>(names.size());
        for (String name : names) {
            var preview = new ImagePreviewPanel(name);
            preview.addPropertyChangeListener(ImagePreviewPanel.ThumbClickEvent, ThumbClickListener);
            ThumbsReferences.put(name, preview);
            ThumbGridPanel.add(preview);
        }
    }

    public void startAnalysis() {
        if (!Analyst.isDone()) {
            Analyst.execute();
        }
    }

    public void addBusyListener(PropertyChangeListener listener) {
        addPropertyChangeListener(ChangePropertys.BUSY, listener);
    }

    public void removeBusyListener(PropertyChangeListener listener) {
        removePropertyChangeListener(ChangePropertys.BUSY, listener);
    }

    private void SetTitleInternal(Path FilePath) {
        //assume the file is alredy non null. we are too deep if it is not a verification was missing before
        var Filename = FilePath.getFileName().toString().strip();
        pFooter.setFooterText(String.format("analyzing File: %s", Filename));
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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        AnalysisTabs = new com.aeongames.edi.utils.visual.Panels.JImageTabPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        ThumbGridPanel = new javax.swing.JPanel();
        pFooter = new com.aeongames.stegsolveplus.ui.Footer();

        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                formComponentShown(evt);
            }
        });

        AnalysisTabs.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);

        jPanel1.setOpaque(false);

        jScrollPane1.setOpaque(false);
        jScrollPane1.getVerticalScrollBar().setUnitIncrement(25);

        ThumbGridPanel.setLayout(new java.awt.GridLayout(0, 3));
        jScrollPane1.setViewportView(ThumbGridPanel);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 658, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 455, Short.MAX_VALUE)
        );

        AnalysisTabs.addTab("Transformations", new javax.swing.ImageIcon(getClass().getResource("/com/aeongames/stegsolveplus/ui/color.png")), jPanel1); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(AnalysisTabs, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pFooter, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(AnalysisTabs, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(pFooter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        AnalysisTabs.getAccessibleContext().setAccessibleName("");
        AnalysisTabs.getAccessibleContext().setAccessibleDescription("");
    }// </editor-fold>//GEN-END:initComponents

    private void formComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
        startAnalysis();
    }//GEN-LAST:event_formComponentShown

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.aeongames.edi.utils.visual.Panels.JImageTabPane AnalysisTabs;
    private javax.swing.JPanel ThumbGridPanel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private com.aeongames.stegsolveplus.ui.Footer pFooter;
    // End of variables declaration//GEN-END:variables

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
        System.gc();
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

}
