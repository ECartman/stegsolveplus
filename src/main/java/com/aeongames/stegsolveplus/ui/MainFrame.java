/* 
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

import com.aeongames.edi.utils.DnD.DragAndDrop;
import com.aeongames.edi.utils.data.Pair;
import com.aeongames.edi.utils.error.LoggingHelper;
import com.aeongames.edi.utils.visual.ImageScaleComponents;
import com.aeongames.edi.utils.visual.Panels.JAeonTabPane;
import com.aeongames.stegsolveplus.StegnoTools.StegnoAnalysis;
import com.aeongames.stegsolveplus.ui.tabcomponents.JStegnoTabbedPane;
import java.awt.Color;
import java.awt.IllegalComponentStateException;
import java.awt.Image;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.RecursiveTask;
import java.util.logging.Level;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.pushingpixels.radiance.theming.api.skin.RadianceNightShadeLookAndFeel;

/**
 * the Main Windows(frame) for the application. this application is intended to
 * have as few windows as possible and most data to be review within tabs on the
 * Main Windows. with the exceptions of "opening files" and a few particular
 * exceptions.
 *
 * @author Eduardo Vindas
 */
public class MainFrame extends javax.swing.JFrame {

    public static final String APP_NAME = "StegnoSolver+ (ALPHA)";
    public static ImageIcon APP_ICON = LoadAppIcon();

    private static ImageIcon LoadAppIcon() {
        var resource = MainFrame.class.getResource("/com/aeongames/stegsolveplus/ui/OIG3.jpg");
        return resource == null ? null : new javax.swing.ImageIcon(resource);
    }
    /**
     * counts the amount of "busy tabs" that are currently registered.
     */
    private int BusyTabs;
    /**
     * TODO: better approach using javaFX or Low level?
     */
    private boolean HackishOpenFile;
    /**
     * Drag and Drop Helper to handle File Loading from System Dragging images
     */
    private DragAndDrop DragAndDrophelper;

    private final PropertyChangeListener BusyStateCallback;

    /**
     * Creates new form MainFrame
     */
    public MainFrame() {
        BusyTabs = 0;
        BusyStateCallback = getBusyStateCallback();
        initComponents();
        if (APP_ICON != null) {
            this.setIconImage(APP_ICON.getImage());
        }
        EnableDragAndDrop();
    }

    private PropertyChangeListener getBusyStateCallback() {
        return (evt) -> {
            if (evt.getPropertyName().equals(InvestigationTab.ChangePropertys.BUSY) && evt.getSource() instanceof InvestigationTab && evt.getNewValue() instanceof Boolean newvalue) {
                if (!newvalue) {//not busy
                    SetMenuStatus(--BusyTabs == 0);
                } else {
                    SetMenuStatus(newvalue);
                }
            }
        };
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        MainTabPane = new com.aeongames.stegsolveplus.ui.tabcomponents.JStegnoTabbedPane();
        MainMenu = new javax.swing.JMenuBar();
        FileMenu = new javax.swing.JMenu();
        MOpenFile = new javax.swing.JMenuItem();
        MOpenLink = new javax.swing.JMenuItem();
        MOpenClipboard = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        MbExit = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle(APP_NAME);
        setMinimumSize(new java.awt.Dimension(370, 510));
        setName("MainFrame"); // NOI18N
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        MainTabPane.setBackgroundPolicy(ImageScaleComponents.SCALE_ALWAYS);
        MainTabPane.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                MainTabPanePropertyChange(evt);
            }
        });

        FileMenu.setText("File");
        FileMenu.setToolTipText("File Menu");

        MOpenFile.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        MOpenFile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/aeongames/stegsolveplus/ui/file_open_20dp_opsz20.png"))); // NOI18N
        MOpenFile.setText("Open File");
        MOpenFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MOpenFileActionPerformed(evt);
            }
        });
        FileMenu.add(MOpenFile);

        MOpenLink.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        MOpenLink.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/aeongames/stegsolveplus/ui/link.png"))); // NOI18N
        MOpenLink.setText("Open Link");
        MOpenLink.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MOpenLinkActionPerformed(evt);
            }
        });
        FileMenu.add(MOpenLink);

        MOpenClipboard.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        MOpenClipboard.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/aeongames/stegsolveplus/ui/paste.png"))); // NOI18N
        MOpenClipboard.setText("Open Clipboard");
        MOpenClipboard.setEnabled(false);
        MOpenClipboard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MOpenClipboardActionPerformed(evt);
            }
        });
        FileMenu.add(MOpenClipboard);
        FileMenu.add(jSeparator1);

        MbExit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_DOWN_MASK));
        MbExit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/aeongames/stegsolveplus/ui/exitsmall.png"))); // NOI18N
        MbExit.setText(String.format("Exit %s",APP_NAME)
        );
        MbExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MbExitActionPerformed(evt);
            }
        });
        FileMenu.add(MbExit);

        MainMenu.add(FileMenu);

        jMenu2.setText("Actions");

        jMenuItem1.setText("Run Image Study");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem1);

        MainMenu.add(jMenu2);

        setJMenuBar(MainMenu);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(MainTabPane, javax.swing.GroupLayout.DEFAULT_SIZE, 1006, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(MainTabPane, javax.swing.GroupLayout.DEFAULT_SIZE, 589, Short.MAX_VALUE)
        );

        setSize(new java.awt.Dimension(1022, 620));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        var currentTab = MainTabPane.getSelectedComponent();
        if (currentTab instanceof InvestigationTab ITab) {
            /*      ITab.startAnalysis();*/
        }
        //SetDefOSUI();
        SetNimbusUI();
        this.setVisible(true);
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    /**
     * trigger by click on Open Action on the Menu bar.
     *
     * @param evt not used
     */
    private void MOpenFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MOpenFileActionPerformed
        SetMenuStatus(false);
        if (HackishOpenFile) {
            SetDefOSUI();
        }
        JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));
        var list2 = StegnoAnalysis.ValidImagesFiles;
        fileChooser.setFileFilter(new FileNameExtensionFilter(ValidFileTypes(list2), list2));
        fileChooser.setMultiSelectionEnabled(true);
        int rVal = fileChooser.showOpenDialog(this);
        if (HackishOpenFile) {
            setRadianceUI();
        }
        System.setProperty("user.dir", fileChooser.getCurrentDirectory().getAbsolutePath());
        if (rVal == JFileChooser.APPROVE_OPTION) {
            var selecteddata = fileChooser.getSelectedFiles();
            if (!loadImages(selecteddata)) {
                SetMenuStatus(true);
            }
            //SetMenuStatus(true);
        } else {
            SetMenuStatus(true);
        }
    }//GEN-LAST:event_MOpenFileActionPerformed

    private void MOpenLinkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MOpenLinkActionPerformed
        SetMenuStatus(false);
        var UIresponse = JOptionPane.showInputDialog(this,
                "Please Provide a Image Url to Load",
                "URL Request", JOptionPane.QUESTION_MESSAGE,
                new ImageIcon(this.getIconImage().getScaledInstance(50, 50, Image.SCALE_FAST),
                        "AppIcon"), null, null);
        var responce = UIresponse == null ? null : UIresponse.toString().strip();
        if (responce != null) {
            var matcher = DragAndDrop.URL_PATTERN.matcher(responce);
            if (matcher.matches()) {
                URI uri = URI.create(responce);
                var scheme = uri.getScheme();
                if (scheme != null ? scheme.equalsIgnoreCase("file") : false) {
                    var list = new ArrayList<Path>();
                    list.add(Path.of(uri));
                    if (loadImages(list)) {
                        return;
                    }
                } else {
                    try {
                        if (loadUrl(uri.toURL())) {
                            return;
                        }
                    } catch (MalformedURLException ex) {
                        LoggingHelper.getLogger(MainFrame.class.getName()).log(Level.SEVERE, "unable to transform the URI to URL", ex);
                    }
                }
            }
        }
        SetMenuStatus(true);
    }//GEN-LAST:event_MOpenLinkActionPerformed

    private void MainTabPanePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_MainTabPanePropertyChange
        if (JStegnoTabbedPane.TAB_REMOVED_EVT.equals(evt.getPropertyName())) {
            if ((int) evt.getNewValue() == 0) {//this should never be null. 
                EnableDragAndDrop();
            }
        }
    }//GEN-LAST:event_MainTabPanePropertyChange

    private void MOpenClipboardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MOpenClipboardActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_MOpenClipboardActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        CloseRequested();
    }//GEN-LAST:event_formWindowClosing

    private void MbExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MbExitActionPerformed
        CloseRequested();
    }//GEN-LAST:event_MbExitActionPerformed

    private String ValidFileTypes(String list2[]) {
        var descriptor = new StringBuilder("Images (");
        for (int index = 0; index < list2.length; index++) {
            descriptor.append(list2[index]);
            if (index + 1 < list2.length) {
                descriptor.append(',').append(' ');
            }
        }
        descriptor.append(')');
        return descriptor.toString();
    }

    /**
     * Checks whenever or not the File can be used. if the file is ready. check
     * if we already have a tab for it. and if we do changes the tab to that
     * particular file
     */
    private boolean loadImages(File[] selecteddata) {
        var pathList = new ArrayList<Path>(selecteddata.length);
        for (var file : selecteddata) {
            pathList.add(file.toPath());
        }
        return loadImages(pathList);
    }

    private boolean loadImages(List<Path> FileList) {
        //do the fast check if already has a tab avail 
        for (var iterator = FileList.iterator(); iterator.hasNext();) {
            Path next = iterator.next();
            if (CheckIfTabForPathExist(next)) {
                iterator.remove();
            }
        }
        if (FileList.isEmpty()) {
            return false;
        }
        final var taskStack = new Stack<RecursiveTask<Pair<Path, Boolean>>>();
        for (final var file : FileList) {
            taskStack.push(new RecursiveTask<Pair<Path, Boolean>>() {
                @Override
                protected Pair<Path, Boolean> compute() {
                    var created = new Pair<>(file,
                            Files.exists(file)
                            && Files.isRegularFile(file)
                            && Files.isReadable(file));
                    return created;
                }
            }).fork();
        }
        if (taskStack.isEmpty()) {
            return false;
        }
        new Thread(() -> {
            while (!taskStack.isEmpty()) {
                final var Checkresult = taskStack.pop().join();
                if (Checkresult.getRight()) {
                    try {
                        SwingUtilities.invokeAndWait(() -> {
                            newFileTab(Checkresult.getLeft());
                        });
                    } catch (InterruptedException | InvocationTargetException ex) {
                        LoggingHelper.getLogger(MainFrame.class.getName())
                                .log(Level.INFO, "UI interrupted or error on call", ex);
                    }
                }
            }
            SwingUtilities.invokeLater(() -> {
                SetMenuStatus(BusyTabs == 0);
            });
        }).start();
        return true;
    }

    /**
     * checks if there is already a Tab for the provided Path. if there is
     * changes the focus to that Tab.
     *
     * @param file the path to check
     * @return true if found false otherwise.
     */
    private boolean CheckIfTabForPathExist(final Path file) {
        int tabindx;
        if ((tabindx = hasTabforFile(file)) >= 0) {
            MainTabPane.setSelectedIndex(tabindx);
            return true;
        }
        return false;
    }

    /**
     * Creates a new Tab for the specified Path. we assume the path:
     * <pre>
     * is not null.
     * is valid path
     * the underline file exist
     * the underline file can be read
     * </pre>
     *
     * @param file the path to use on analysis.
     * @return true if the tab was sucessfully created false otherwise.
     */
    private boolean newFileTab(final Path file) {
        var success = false;
        if (Files.exists(file) && Files.isRegularFile(file) && Files.isReadable(file)) {
            InvestigationTab tab = new InvestigationTab(file);
            tab.addBusyListener(BusyStateCallback);
            BusyTabs++;
            success = addTab(tab);
        } else {
            LoggingHelper.getLogger(MainFrame.class.getName()).log(Level.WARNING, "the Path {0} Does not exist, is not a File. or Cannot be Read", file.toString());
        }
        return success;
    }

    private void ProcessDropedFiles(final List<Path> FileList) {
        if (SwingUtilities.isEventDispatchThread()) {
            SetMenuStatus(false);
            if (!loadImages(FileList)) {
                SetMenuStatus(true);
            }
            return;
        }
        try {
            SwingUtilities.invokeAndWait(() -> this.ProcessDropedFiles(FileList));
        } catch (InterruptedException ex) {
            LoggingHelper.getLogger(MainFrame.class.getName()).log(Level.SEVERE, "A call to UI was Interrupted", ex);
        } catch (InvocationTargetException ex) {
            LoggingHelper.getLogger(MainFrame.class.getName()).log(Level.SEVERE, "Could not invoke the UI", ex);
        }
    }

    private void ProcessDropedLinks(final URL link) {
        if (SwingUtilities.isEventDispatchThread()) {
            SetMenuStatus(false);
            if (!loadUrl(link)) {
                SetMenuStatus(true);
            }
            return;
        }
        try {
            SwingUtilities.invokeAndWait(() -> this.ProcessDropedLinks(link));
        } catch (InterruptedException ex) {
            LoggingHelper.getLogger(MainFrame.class.getName()).log(Level.SEVERE, "A call to UI was Interrupted", ex);
        } catch (InvocationTargetException ex) {
            LoggingHelper.getLogger(MainFrame.class.getName()).log(Level.SEVERE, "Could not invoke the UI", ex);
        }
    }

    private boolean addTab(InvestigationTab tab) {
        boolean Tabcreated = false;
        if (tab != null) {
            MainTabPane.add(tab);
            MainTabPane.setSelectedComponent(tab);
            if (getRootPane().getGlassPane() instanceof GlassFileDnDPanel panel) {
                DragAndDrophelper.UnRegisterTarget(panel);
                panel.setVisible(false);
            }
            Tabcreated = true;// we might want to do something extra here. but this will do for now. 
        }
        return Tabcreated;
    }

    private boolean loadUrl(URL Link) {
        int tabindx;
        if ((tabindx = hasTabforLink(Link)) >= 0) {
            MainTabPane.setSelectedIndex(tabindx);
            return false;
        }
        InvestigationTab tab = null;
        tab = new InvestigationTab(Link);
        tab.addBusyListener(BusyStateCallback);
        BusyTabs++;
        return addTab(tab);
    }

    private void SetMenuStatus(boolean status) {
        MOpenFile.setEnabled(status);
        MOpenLink.setEnabled(status);
        //this is not ready to be changed.
        //MOpenClipboard.setEnabled(status);
    }

    private int hasTabforFile(Path pathFile) {
        for (var index = 0; index < MainTabPane.getTabCount(); index++) {
            if (MainTabPane.getComponentAt(index) instanceof InvestigationTab tab) {
                if (tab.IsAnalizing(pathFile)) {
                    return index;
                }
            }
        }
        return -1;
    }

    private int hasTabforLink(URL Link) {
        for (var index = 0; index < MainTabPane.getTabCount(); index++) {
            if (MainTabPane.getComponentAt(index) instanceof InvestigationTab tab) {
                if (tab.IsAnalizing(Link)) {
                    return index;
                }
            }
        }
        return -1;
    }

    private void EnableDragAndDrop() {
        if (DragAndDrophelper == null) {
            DragAndDrophelper = new DragAndDrop(JAeonTabPane.J_AEON_TAB_FLAVOR) {
                @Override
                public void triggerDragdetectedImp(DropTargetDragEvent dtde) {
                    var source = dtde.getDropTargetContext().getComponent();
                    if (source instanceof GlassFileDnDPanel panel) {
                        panel.setInvisible(false);
                    }
                }

                @Override
                public void triggerDragExitImp(DropTargetEvent dte) {
                    var source = dte.getDropTargetContext().getComponent();
                    if (source instanceof GlassFileDnDPanel panel) {
                        panel.setInvisible(true);
                    }
                }

                @Override
                public void NotifyFoundPaths(List<Path> fileList) {
                    //maybe we should disable if Busy Tabs > 0
                    ProcessDropedFiles(fileList);
                }

                @Override
                public void NotifyFoundUrl(URL link) {
                    //maybe we should disable if Busy Tabs > 0
                    ProcessDropedLinks(link);
                }

                @Override
                public void DropComplete(DropTargetDropEvent dtde) {
                    var source = dtde.getDropTargetContext().getComponent();
                    if (source instanceof GlassFileDnDPanel panel) {
                        panel.setInvisible(true);
                    }
                }
            };
        }
        GlassFileDnDPanel glasspane;
        if (getRootPane().getGlassPane() instanceof GlassFileDnDPanel glass) {
            glasspane = glass;
        } else {
            glasspane = new GlassFileDnDPanel(DragAndDrophelper);
            getRootPane().setGlassPane(glasspane);
        }
        DragAndDrophelper.RegisterTarget(glasspane);
        glasspane.setVisible(true);
        glasspane.setInvisible(true);
        //also register *the Window so we can trigger DnD events from toolbar
        DragAndDrophelper.RegisterTarget(this);
    }

    private void SetNimbusUI() {
        for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) {
                //nimbus dark mode.
                UIManager.put("control", new Color(128, 128, 128));
                UIManager.put("info", new Color(128, 128, 128));
                UIManager.put("nimbusBase", new Color(18, 30, 49));
                UIManager.put("nimbusAlertYellow", new Color(248, 187, 0));
                UIManager.put("nimbusDisabledText", new Color(128, 128, 128));
                UIManager.put("nimbusFocus", new Color(115, 164, 209));
                UIManager.put("nimbusGreen", new Color(176, 179, 50));
                UIManager.put("nimbusInfoBlue", new Color(66, 139, 221));
                UIManager.put("nimbusLightBackground", new Color(18, 30, 49));
                UIManager.put("nimbusOrange", new Color(191, 98, 4));
                UIManager.put("nimbusRed", new Color(169, 46, 34));
                UIManager.put("nimbusSelectedText", new Color(255, 255, 255));
                UIManager.put("nimbusSelectionBackground", new Color(104, 93, 156));
                UIManager.put("text", new Color(230, 230, 230));
                SetUIClass(info.getClassName());
                break;
            }
        }
    }

    private void SetDefOSUI() {
        this.setVisible(false);
        SetUIClass(UIManager.getSystemLookAndFeelClassName());
        //this.setVisible(true);
    }

    private void setRadianceUI() {
        this.setVisible(false);
        SetUIClass(RadianceNightShadeLookAndFeel.class.getName());
        this.setVisible(true);
    }

    private void SetUIClass(String Name) {
        dispose();
        trySetLaFByName(Name);
        javax.swing.SwingUtilities.updateComponentTreeUI(this);
        var supdeco = UIManager.getLookAndFeel().getSupportsWindowDecorations();
        try {
            setUndecorated(supdeco);
        } catch (IllegalComponentStateException err) {
        }
        try {
            getRootPane().setWindowDecorationStyle(supdeco ? JRootPane.FRAME : JRootPane.NONE);
        } catch (IllegalComponentStateException err) {
        }
        this.revalidate();
    }

    private void CloseRequested() {
        if (MainTabPane.getTabCount() == 0) {
            this.setVisible(false);
            this.dispose();
            System.exit(0);
            return;
        }
        CloseDialog Dialog = new CloseDialog(this, true);
        Dialog.setLocationRelativeTo(this);
        Dialog.setVisible(true);
        if (Dialog.getSelectedOption() == CloseDialog.RET_EXIT) {
            try {
                for (var index = 0; index < MainTabPane.getTabCount(); index++) {
                    if (MainTabPane.getComponentAt(index) instanceof InvestigationTab tab) {
                        tab.Close(true);
                    }
                }
                this.setVisible(false);
                this.dispose();
                //Do whatever other cleanup might still pending
                //bye
                System.exit(0);
            } catch (Throwable err) {
                LoggingHelper.getLogger(MainFrame.class.getName()).log(Level.SEVERE, "error while closing", err);
                System.exit(-2);
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Start Up Functions">
    /**
     * Initialize the LAF for the application. this function needs to be called
     * on the EDT
     */
    private static void InitLAF() {
        String LaFName = RadianceNightShadeLookAndFeel.class.getName();
        if (!trySetLaFByName(LaFName)) {
            trySetLaFByName(UIManager.getSystemLookAndFeelClassName());
        }
    }

    private static boolean trySetLaFByName(String Name) {
        var result = false;
        try {
            UIManager.setLookAndFeel(Name);
            result = true;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            //unable to set the UI LAF we could try just allowing the defaults. 
            LoggingHelper.getLogger(MainFrame.class.getName()).log(Level.SEVERE, "Unable to setup the UI LaF", e);
        }

        if (UIManager.getLookAndFeel().getSupportsWindowDecorations()) {
            JFrame.setDefaultLookAndFeelDecorated(true);
            javax.swing.JDialog.setDefaultLookAndFeelDecorated(true);
        } else {
            JFrame.setDefaultLookAndFeelDecorated(false);
            javax.swing.JDialog.setDefaultLookAndFeelDecorated(false);
        }
        return result;
    }

    private static void ParseParams(String[] params) {
        //TODO: Implement
    }

    /**
     * Launches the Application.
     *
     * @param args The Console parameters for this application. TODO use the
     * arguments someway.
     *
     */
    public static void main(String[] args) {
        ParseParams(args);
        SwingUtilities.invokeLater(() -> {
            InitLAF();
            try {
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
            } catch (Exception e) {
                //log the error
                LoggingHelper.getLogger(MainFrame.class.getName()).log(Level.SEVERE, "Exception at Main, Something crashed", e);
                throw e;
            }
        });
    }
    // </editor-fold>  

    // <editor-fold defaultstate="collapsed" desc="UI components">    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu FileMenu;
    private javax.swing.JMenuItem MOpenClipboard;
    private javax.swing.JMenuItem MOpenFile;
    private javax.swing.JMenuItem MOpenLink;
    private javax.swing.JMenuBar MainMenu;
    private com.aeongames.stegsolveplus.ui.tabcomponents.JStegnoTabbedPane MainTabPane;
    private javax.swing.JMenuItem MbExit;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    // End of variables declaration//GEN-END:variables

    // </editor-fold>  
}
