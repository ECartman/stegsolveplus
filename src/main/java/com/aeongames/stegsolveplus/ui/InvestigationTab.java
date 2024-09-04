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

import com.aeongames.stegsolveplus.ui.tabcomponents.Tab;
import com.aeongames.stegsolveplus.StegnoTools.StegnoAnalist;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ed
 */
public class InvestigationTab extends Tab {

    final class ChangePropertys {

        public static final String BUSY = "BUSY";
        public static final String STATEINFO = "STATE_STRING";
    }
    private boolean isBusy = true;

    private final StegnoAnalist Analist;
    /**
     * for now use a PropertyChangeListener, this Object is to be notified for
     * changes on the sate of this tab. (loads data, is done is idle, etc...)
     */
    private final PropertyChangeSupport propertySupport;

    /**
     * Creates new form InvestigationTab
     *
     * @param FilePath the filePath to investigate
     * @throws java.io.FileNotFoundException
     */
    public InvestigationTab(Path FilePath) throws FileNotFoundException {
        FilePath = Objects.requireNonNull(FilePath, "provided path is null");
        propertySupport = new PropertyChangeSupport(this);
        if (!Files.exists(FilePath)) {
            throw new FileNotFoundException("the provided path is invalid");
        }
        initComponents();
        _setTitle(FilePath);
        Analist = new StegnoAnalist(FilePath);
        pFooter.SetProgressIndeterminate();
    }

    public boolean IsAnalizing(Path OtherFile) {
        if (OtherFile == null) {
            return false;
        }
        var path = Analist.getFilePath();
        if (path != null) {
            path = path.toAbsolutePath();
            OtherFile = OtherFile.toAbsolutePath();
            boolean result = false;
            try {
                result = Files.isSameFile(path, OtherFile);
            } catch (IOException ex) {
            }
            return result;
        }else{
          return OtherFile.toAbsolutePath().toString().equals(Analist.getAnalisisSource());
        }
    }

    public boolean IsAnalizing(URL OtherFile) {
        return OtherFile.toString().equals(Analist.getAnalisisSource());
    }

    public void RunAnalist(boolean NewThread) {
        if (Objects.isNull(Analist)) {
            fireTabSpecificPropertyChange(ChangePropertys.STATEINFO, null, "analysis CANNOT be performed");
            return;
        }
        fireTabSpecificPropertyChange(ChangePropertys.STATEINFO, null, "Starting analysis");
        try {
            //TODO: move this to be done in Parallel.
            var list = Analist.RunTrasFormations(true);
            //jImageTabPane1.addTab("Unedited", new javax.swing.ImageIcon(getClass().getResource("/com/aeongames/stegsolveplus/ui/color.png")),new ImagePanel(Analist.getUnedited()));
            PanenPlanes.add(new ImagePreviewPanel("Original", Analist.getUnedited()));
            if (list != null) {
                for (var pair : list) {
                    var preview = new ImagePreviewPanel(pair.getLeft(), pair.getRight());
                    PanenPlanes.add(preview);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(InvestigationTab.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void addListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }

    public void removeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }

    private void _setTitle(Path FilePath) {
        //assume the file is alredy non null. we are too deep if it is not a verification was missing before
        var Filename = FilePath.getFileName().toString().strip();
        pFooter.setFooterText(String.format("analizing File: %s", Filename));
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

        jImageTabPane1 = new com.aeongames.edi.utils.visual.Panels.JImageTabPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        PanenPlanes = new javax.swing.JPanel();
        pFooter = new com.aeongames.stegsolveplus.ui.Footer();

        jImageTabPane1.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);

        jPanel1.setOpaque(false);

        jScrollPane1.setOpaque(false);
        jScrollPane1.getVerticalScrollBar().setUnitIncrement(25);

        PanenPlanes.setLayout(new java.awt.GridLayout(0, 3));
        jScrollPane1.setViewportView(PanenPlanes);

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

        jImageTabPane1.addTab("Transformations", new javax.swing.ImageIcon(getClass().getResource("/com/aeongames/stegsolveplus/ui/color.png")), jPanel1); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jImageTabPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pFooter, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jImageTabPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(pFooter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jImageTabPane1.getAccessibleContext().setAccessibleName("Tabs for image analisis.");
        jImageTabPane1.getAccessibleContext().setAccessibleDescription("");
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel PanenPlanes;
    private com.aeongames.edi.utils.visual.Panels.JImageTabPane jImageTabPane1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private com.aeongames.stegsolveplus.ui.Footer pFooter;
    // End of variables declaration//GEN-END:variables

    //TODO::do the actual cleanup. pop if wants to keep work open?
    @Override
    public boolean Close(boolean force) {
        // Analist.clear? ;
        return true;
    }

    @Override
    protected void setBusy() {
        SetCursorBusy();
        var oldstate = isBusy;
        isBusy = true;
        propertySupport.firePropertyChange(ChangePropertys.BUSY, oldstate, isBusy);
    }

    @Override
    protected void setAvailable() {
        var oldstate = isBusy;
        isBusy = false;
        propertySupport.firePropertyChange(ChangePropertys.BUSY, oldstate, isBusy);
        ClearCursor();
    }

    private synchronized void fireTabSpecificPropertyChange(String propertyName, Object oldValue, Object newValue) {
        //trigger OUR own property support. 
        propertySupport.firePropertyChange(propertyName, oldValue, newValue);
        //trigger on the UI general listeners. 
        firePropertyChange(propertyName, oldValue, newValue);
    }

}
