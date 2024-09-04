/* 
 *  Copyright © 2024 Eduardo Vindas Cordoba. All rights reserved.
 *  
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package com.aeongames.stegsolveplus.ui;

import com.aeongames.edi.utils.File.PropertiesHelper;
import com.aeongames.edi.utils.error.LoggingHelper;
import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;

/**
 * a Helper class to handle Drop (from the Drag and Drop) into an application
 * this class simplify handling drag and drop events. to listen and handle
 * specific events. for this class currently it handles Files, URL and Text
 * other events might require further code to be added.
 *
 * @author Eduardo Vindas
 */
public abstract class DragAndDrop implements DropTargetListener {

    /**
     * the DropTargets that register **this** listener instance. and that can
     * receive the Drop into. it is an array as we may support multiple drops
     * components, do note that it is possible for users of this Listener to
     * register the Listener outside of this class. and thus this class will be
     * unable to Un-Register the target.
     */
    protected Map<Component, DropTarget> Targets = new HashMap<>();
    /**
     * a list of flavors to ignore for this particular Listener. this list is
     * intended for **this** listener to ignore Drop events
     *
     */
    protected List<DataFlavor> FlavorsIgnore = new LinkedList<>();
    //TODO: move the Regex from the inline to be read from the Property file.
    private final PropertiesHelper RegexSettings;

    /**
     * create a new instance of this Class. receive a list of DataFlavors to
     * ignore
     *
     * @param listToFill a List of DataFlavors to be ignored by this class.
     */
    public DragAndDrop(List<DataFlavor> listToFill) {
        Objects.requireNonNull(listToFill, "invalid List");
        FlavorsIgnore.addAll(listToFill);
        RegexSettings = loadProperties();
    }

    /**
     * create a new instance of this Class. receive multiple Flavors to be
     * ignored
     *
     * @param ignoreFlavors a <code>Params</code> array of DataFlavors that are
     * desired to be ignored.
     */
    public DragAndDrop(DataFlavor... ignoreFlavors) {
        if (ignoreFlavors != null && ignoreFlavors.length > 0) {
            FlavorsIgnore.addAll(Arrays.asList(ignoreFlavors));
        }
        RegexSettings = loadProperties();
        // if no flavor is provided assume its A OK. just no flavors are to be ignored.
    }

    private PropertiesHelper loadProperties() {
        PropertiesHelper res = null;
        try {
            res = new PropertiesHelper("/com/aeongames/stegsolveplus/text/Regex.properties", false, DragAndDrop.class);
        } catch (IOException ex) {
            LoggingHelper.getLogger(DragAndDrop.class.getName()).log(Level.SEVERE, "unable to load Resource", ex);
        }
        return res;
    }

    /**
     * Registers a Drop target that will Listen to events with **this** instance
     * and process the Drag and Drop events.
     *
     * @param UIComponent the component that was registered to this listener.
     * @return false if Registration Fails because it is Already registered. and
     * true if able to be registered.
     */
    public boolean RegisterTarget(Component UIComponent) {
        if (Targets.containsKey(UIComponent)) {
            return false;
        }
        var newdrop = new DropTarget(UIComponent, DnDConstants.ACTION_COPY_OR_MOVE, this);
        Targets.put(UIComponent, newdrop);
        return true;
    }

    /**
     * Un-Registers if registered the provided component. by Un-Registers it
     * also imply:
     * <br>
     * the component removes this listener (calling
     * {@link DropTarget#removeDropTargetListener(DropTargetListener)})
     *
     * <br>
     * the DropTarget also Notifies its removal.
     * {@link DropTarget#removeNotify()})
     * <br>
     * set the DropTarget as Inactive {@link DropTarget#setActive(boolean)}
     * <br>
     * and Removes the DropTarget of the list of registered DropTarget on
     * **this** instance.
     *
     * @param UIComponent the component that was registered to this listener.
     * @return true if the component was Registered and was removed. false if
     * the component was not listed at all.
     */
    public boolean UnRegisterTarget(Component UIComponent) {
        if (Targets.containsKey(UIComponent)) {
            Targets.get(UIComponent).removeDropTargetListener(this);
            Targets.get(UIComponent).removeNotify();
            Targets.get(UIComponent).setActive(false);
            Targets.remove(UIComponent);
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void dragEnter(DropTargetDragEvent dtde) {
        var log = LoggingHelper.getLogger(DragAndDrop.class.getName());
        if (ignoreFlavor(dtde)) {
            log.log(Level.INFO, "DragEnter is to be Rejected");
            dtde.rejectDrag();
            return;
        }
        // trigger update to the UI.
        triggerDragdetected(dtde);
        log.log(Level.INFO, "DragEnter at Location: {0}", dtde.getLocation());
        dtde.acceptDrag(DnDConstants.ACTION_COPY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dragOver(DropTargetDragEvent dtde) {
        var log = LoggingHelper.getLogger(DragAndDrop.class.getName());
        if (ignoreFlavor(dtde)) {
            log.log(Level.INFO, "dragOver is to be Rejected");
            dtde.rejectDrag();
            return;
        }
        log.log(Level.FINE, "dragOver at Location: {0}", dtde.getLocation());
        dtde.acceptDrag(DnDConstants.ACTION_COPY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
        var log = LoggingHelper.getLogger(DragAndDrop.class.getName());
        if (ignoreFlavor(dtde)) {
            log.log(Level.INFO, "dropActionChanged is to be Rejected");
            dtde.rejectDrag();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void dragExit(DropTargetEvent dte) {
        var log = LoggingHelper.getLogger(DragAndDrop.class.getName());
        log.log(Level.INFO, "dragExit at Component: {0}", dte.getDropTargetContext().getComponent().toString());
        // The D&D went outside of the app Area. and thus we can disenagage.
        triggerDragExit(dte);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void drop(DropTargetDropEvent dtde) {
        var log = LoggingHelper.getLogger(DragAndDrop.class.getName());
        log.log(Level.INFO, "Drop trigger at: {0}", dtde.getLocation());
        // list flavors that the data has. and log them
        var handled = false;
        var flavors = dtde.getTransferable().getTransferDataFlavors();// dtde.getCurrentDataFlavors();
        var supports = CheckForSupportedFlavors(flavors);
        if (supports.hasSupported()) {
            dtde.acceptDrop(DnDConstants.ACTION_COPY);
            List<Path> fileList = null;
            URL FileLink = null;
            String DropData = null;
            if (supports.isFileBacked()) {
                fileList = preProcessFile(supports, dtde.getTransferable());
            }
            if (fileList == null && supports.isURLBacked()) {
                FileLink = preProcessURL(supports.URLflavor, dtde.getTransferable());
                //check if the URL is a File on the system. 
                try {
                    var scheme = FileLink.toURI().getScheme();
                    if (scheme != null ? scheme.equalsIgnoreCase("file") : false) {
                        fileList = new LinkedList<>();
                        fileList.add(Path.of(FileLink.toURI()));
                        //FileLink = null;
                    }
                } catch (URISyntaxException ex) {
                }
            }
            if (fileList == null && FileLink == null && supports.isTextBacked()) {
                var Result = preProcessText(supports.Textflavor, dtde.getTransferable());
                switch (Result) {
                    case Path path -> {
                        fileList = new LinkedList<>();
                        fileList.add(path);
                    }
                    case URL url ->
                        FileLink = url;
                    case String data ->
                        DropData = data;
                    case LinkedList<?> List -> {
                        fileList = new LinkedList<>();
                        for (Object element : List) {
                            if (element instanceof Path file) {
                                fileList.add(file);
                            }
                        }
                    }
                    default -> {
                    }
                }
            }
            if (fileList != null && !fileList.isEmpty()) {
                NotifyFoundPaths(fileList);
                handled = true;
            } else if (FileLink != null) {
                NotifyFoundUrl(FileLink);
                handled = true;
            } else {
                //for now nothing. 
            }
        } else {
            dtde.acceptDrop(DnDConstants.ACTION_NONE);
        }

        if (!handled) {
            // we are unable to handle this type of drop thus lets reject it.
            dtde.dropComplete(false);
        } else {
            dtde.dropComplete(true);
        }
        DropComplete(dtde);
    }

    /**
     * check if the list of flavors are supported by this application. if there
     * is
     *
     * @param flavors
     * @return FlavorSupportedDrop a class that informs if we found supported
     * Flavors for this app
     */
    private FlavorSupportedDrop CheckForSupportedFlavors(DataFlavor[] flavors) {
        var log = LoggingHelper.getLogger(DragAndDrop.class.getName());
        var flavorSupport = new FlavorSupportedDrop();
        for (var supportedflavor : flavors) {
            if (supportedflavor.isFlavorJavaFileListType()) {
                log.log(Level.INFO, "data is File Type: {0}", supportedflavor);
                flavorSupport.setFileBacked(supportedflavor);
            } else if (supportedflavor.isMimeTypeEqual("application/x-java-url")) {
                log.log(Level.INFO, "data is URL Type: {0}", supportedflavor);
                flavorSupport.setURLBacked(supportedflavor);
            } else if (supportedflavor.isFlavorTextType()) {
                // we COULD support any sort of text and read it from the underline
                // source. and support (wrapped on a input stream.) but to the best of my
                // knowledge
                // the underline Java code does this alredy. and thus we will take advantage
                // of that and just check if the RepresentationClass is String.
                if (supportedflavor.getRepresentationClass() == String.class) {
                    log.log(Level.INFO, "data is String Type Mime: {0}", supportedflavor.getMimeType());
                    flavorSupport.setTextBacked(supportedflavor);
                } else {
                    log.log(Level.INFO, "non handled Text Flavor: {0}", supportedflavor);
                }
            } else {
                log.log(Level.INFO, "Unsupported Flavor: {0}", supportedflavor);
            }
            if (flavorSupport.isFileBacked() && flavorSupport.isTextBacked() && flavorSupport.isURLBacked()) {
                log.log(Level.INFO, "found all the application supported flavors");
                break;
            }
        }
        return flavorSupport;
    }

    /**
     * a Class that holds found Drop flavors that are supported by this
     * application.
     */
    private class FlavorSupportedDrop {

        private DataFlavor Fileflavor;
        private DataFlavor Textflavor;
        private DataFlavor URLflavor;

        private FlavorSupportedDrop() {
            Fileflavor = Textflavor = null;
        }

        private void setFileBacked(DataFlavor filebased) {
            Fileflavor = filebased;
        }

        private void setTextBacked(DataFlavor textbased) {
            Textflavor = textbased;
        }

        private void setURLBacked(DataFlavor supportedflavor) {
            URLflavor = supportedflavor;
        }

        private boolean hasSupported() {
            return Objects.nonNull(Fileflavor) || Objects.nonNull(Textflavor);
        }

        private boolean isFileBacked() {
            return Objects.nonNull(Fileflavor);
        }

        private boolean isTextBacked() {
            return Objects.nonNull(Textflavor);
        }

        private boolean isURLBacked() {
            return Objects.nonNull(URLflavor);
        }
    }

    /**
     * https://learn.microsoft.com/en-us/windows/win32/fileio/naming-a-file
     * Note: some characters mighr require further scaping. option A:
     * (?:"?(?:\\\\\?\\)?(?:[a-zA-Z]:[\\/]|[\\]{2})
     * (?:[^\\/"<>:\\|?\\*]+[\\/])*) [^\\/"<>:\\|?\\* ]+(?:"|(?!\s))? lets allow
     * matching UNC (Start) path for example: "\\networkshare\xxxxxx" lets allow
     * matching C:\ or C:/ (either "\/") lets match whatever path that does not
     * contain the Reserved characters (according to MS documentations) support
     * Extended path ("\\?\") option B:
     * (?:"?(?:\\\\\?\\)?(?:[a-zA-Z]:[\\/]|[\\]{2})
     * (?:[-\u4e00-\u9fa5\w\s.()~!@#$%^&()\[\]{}+=]+[\\/])+
     * (?:[-\u4e00-\u9fa5\w\s.()~!@#$%^&()\[\]{}+=]+)(?:\"|(?!\s)) lets
     * basically reverse: * // //option B: //
     */
    /**
     * NOTE: on windows (modern) path limit is 0x7FFF (extended-length) or
     * otherwise 260 (MAX_PATH) characters according to MS doco. a Extended path
     * should use the pprefix "\\?\"
     */
    public static final Pattern WindowsPattern = Pattern.compile("\\\"?((?:\\\\\\\\\\?\\\\)?(?:[a-zA-Z]:[\\\\/]|[\\\\]{2})(?:[^\\\\/\\\"<>:\\|\\?\\*\\r\\n]+[\\\\/])*[^\\\\/\\\"<>:\\|\\?\\*\\r\\n ]+)(?:\\\")?");
    public static final Pattern LinuxPattern = Pattern.compile("^(/[^/\\x00 ]*)+/?$");
    public static final Pattern URLPattern = Pattern.compile("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");

    /**
     * Process a Drop action which Flavor is MimeType text. (note there are
     * several Text mime's types )
     *
     * @param TextFlavor the specific flavor that is of type Text.
     * @param dtde the Drop event.
     */
    private Object preProcessText(DataFlavor TextFlavor, Transferable DropTransfeable) {
        var log = LoggingHelper.getLogger(DragAndDrop.class.getName());
        Object ob = null;
        try {
            ob = DropTransfeable.getTransferData(TextFlavor);
        } catch (UnsupportedFlavorException ex) {
            log.log(Level.SEVERE, "Unsuported Flavor", ex);
        } catch (IOException ex) {
            log.log(Level.SEVERE, "IO unable to transfer data?", ex);
        }
        if (ob == null) {
            log.log(Level.WARNING, "The Transferible Object cannot be read");
            return null;
        } else if (ob instanceof String Data) {
            Data = Data.strip();
            //a String Could be a path to a file or a URL. 
            //if neither. COULD be a string representation of the file. content 
            //but we will NOT support that. 
            if (URLPattern.matcher(Data).matches()) {
                URI uri = URI.create(Data);
                var scheme = uri.getScheme();
                if (scheme != null ? scheme.equalsIgnoreCase("file") : false) {
                    //the URL is a File we should be able to open it with the 
                    //filesystem 
                    return Path.of(uri);
                } else {
                    try {
                        return uri.toURL();
                    } catch (MalformedURLException ex) {
                        log.log(Level.SEVERE, "unable to transform the URI to URL", ex);
                    }
                }
            }
            var iswin = System.getProperty("os.name", "generic").toLowerCase().strip().contains("window");
            /**
             * TODO: it seems that Parsing string into Linux might need further
             * computation seemly as utf8->utf16 might work funky.. but need
             * testing.
             */
            var matcher = iswin
                    ? WindowsPattern.matcher(Data)
                    : LinuxPattern.matcher(Data);
            var fileList = new LinkedList<Path>();
            var charlimit = iswin ? 0x7FFF : 0xFF;
            for (int matched = 0; matcher.find(); matched++) {
                var matchedstring = matcher.group();
                if (matchedstring.length() <= charlimit) {
                    fileList.add(Path.of(matchedstring));
                }
            }
            if (!fileList.isEmpty()) {
                return fileList;
            }
            log.log(Level.WARNING, "No Configuration matched the Text provided: {0}", Data);
            return Data;
        }
        return null;
    }

    private URL preProcessURL(DataFlavor TextFlavor, Transferable DropTransfeable) {
        var log = LoggingHelper.getLogger(DragAndDrop.class.getName());
        Object ob = null;
        try {
            ob = DropTransfeable.getTransferData(TextFlavor);
        } catch (UnsupportedFlavorException ex) {
            log.log(Level.SEVERE, "Unsuported Flavor", ex);
        } catch (IOException ex) {
            log.log(Level.SEVERE, "IO unable to transfer data?", ex);
        }
        URL linkreturn = null;
        switch (ob) {//it seems netbeans throw a warning here. but seem OK java 21+
            case null ->
                log.log(Level.WARNING, "The Transferible Object cannot be read");
            case URL linkUrl ->
                linkreturn = linkUrl;
            case URI linkURI -> {
                try {
                    linkreturn = linkURI.toURL();
                } catch (MalformedURLException e) {
                    log.log(Level.WARNING, "The Transferible Object defines a Malformed URL", e);
                }
            }
            default -> {
                String tmp = Objects.requireNonNullElse(ob, "unknown").getClass().getName();
                log.log(Level.WARNING, "The Transferible Object is NOT URL! reports as: {0}", tmp);
            }
        }
        return linkreturn;
    }

    private List<Path> preProcessFile(FlavorSupportedDrop FlavorSupp, Transferable DropTransfeable) {
        var startTime = Instant.now();
        var log = LoggingHelper.getLogger(DragAndDrop.class.getName());
        Object ob = null;
        try {
            ob = DropTransfeable.getTransferData(FlavorSupp.Fileflavor);
        } catch (UnsupportedFlavorException ex) {
            log.log(Level.SEVERE, "Unsuported Flavor", ex);
        } catch (IOException ex) {
            log.log(Level.SEVERE, "IO unable to transfer data?", ex);
        }
        var endTime = Instant.now();
        var elapsed = Duration.between(startTime, endTime);
        log.log(Level.INFO, "elapsed arround: {0} nanos", elapsed.toMillis());
        if (ob == null) {
            log.log(Level.WARNING, "The Transferible Object cannot be read");
            return null;
        }
        if (ob instanceof List<?> FileList) {
            /*
             * we cannot at runtime safely cast to file but we can almost be
             * assure they are files...
             * there are some code we could add for example streaming the list
             * into another. but that is wasting computation. for the needs
             * also do note this code is likely executing on the EDT AND the OS
             * DnD we could be hanging another app or even the OS...
             * and thus we need to move FAST!
             */
            /**
             * it is possible for Flavors available to be better to handle this
             * case for example. a File can be created for a URL lets say for
             * instance we dragged a Text that is a &lt;a&gt; (<a></a>)and thus
             * it is possible for that link to consider itself several flavors
             * INCLUDING a File. but the actual file would contains basically
             * text that links to an URL for example:
             * <p>
             * <code>
             * [InternetShortcut]
             * URL=https://commons.wikimedia.org/wiki/File:Vincent_van_Gogh_-_De_slaapkamer_-_Google_Art_Project.jpg
             * </code> and that would be the content and thus. this would NOT
             * meet our goal and in fact might be wasteful. if the list
             * therefore only contain files that refer to a .URL we will ignore
             * them and if the list ONLY contains such we shall then return
             * false. thus the parent method should handle another way to read
             * the desire data (opening the URL)
             */
            List<Path> ValidFiles = new ArrayList<>();
            for (var objFile : FileList) {
                if (objFile instanceof File DndFile) {
                    var isurl = DndFile.getName().matches("(?i).*\\.url");
                    if (!isurl && DndFile.exists() && DndFile.canRead()) {
                        ValidFiles.add(DndFile.toPath());
                    }
                }
            }
            if (!ValidFiles.isEmpty()) {
                return ValidFiles;
            }
        }
        return null;
    }

    private void triggerDragdetected(final DropTargetDragEvent dtde) {
        if (SwingUtilities.isEventDispatchThread()) {
            triggerDragdetectedImp(dtde);
            return;
        }
        try {
            SwingUtilities.invokeAndWait(() -> this.triggerDragdetected(dtde));
        } catch (InterruptedException ex) {
            LoggingHelper.getLogger(DragAndDrop.class.getName()).log(Level.SEVERE, "A call to UI was Interrupted", ex);
        } catch (InvocationTargetException ex) {
            LoggingHelper.getLogger(DragAndDrop.class.getName()).log(Level.SEVERE, "Could not invoke the UI", ex);
        }
    }

    private void triggerDragExit(final DropTargetEvent dte) {
        if (SwingUtilities.isEventDispatchThread()) {
            triggerDragExitImp(dte);
            return;
        }
        try {
            // call this function again but from EDT
            SwingUtilities.invokeAndWait(() -> this.triggerDragExit(dte));
        } catch (InterruptedException ex) {
            LoggingHelper.getLogger(DragAndDrop.class.getName()).log(Level.SEVERE, "A call to UI was Interrupted", ex);
        } catch (InvocationTargetException ex) {
            LoggingHelper.getLogger(DragAndDrop.class.getName()).log(Level.SEVERE, "Could not invoke the UI", ex);
        }
    }

    /**
     * triggered by the Event Dispatch thread when a null null null null null
     * null null null null null null null null null null null null null null
     * null null null null null null null null null null null null null null     {@link DropTargetListener#dragEnter(java.awt.dnd.DropTargetDragEvent)
     * }
     * event happens. this method is ensured to be called by the EDT. note this
     * method will block the thread that trigger the Drag and Drop (if that
     * thread is NOT the EDT) and activity takes too long can cause problems on
     * the OS Drag And Drop functionality. thus make sure this method returns as
     * fast as possible. also note. the even is unfiltered. Accepting or
     * rejecting the Event is not required to be performed as this parent class
     * accepts the event.
     *
     * @param dtde the event details data.
     */
    public abstract void triggerDragdetectedImp(DropTargetDragEvent dtde);

    /**
     * triggered by the Event Dispatch thread when a
     * {@link DropTargetListener#dragExit(java.awt.dnd.DropTargetEvent)} event
     * happens. this method is ensured to be called by the EDT. note this method
     * will block the thread that trigger the Drag and Drop (if that thread is
     * NOT the EDT) and activity takes too long can cause problems on the OS
     * Drag And Drop functionality. thus make sure this method returns as fast
     * as possible. also note. the even is unfiltered.
     *
     * @param dte the event details data
     */
    public abstract void triggerDragExitImp(DropTargetEvent dte);

    /**
     * a method to be implemented by child classes. 
     * we ensure that the parameter is not null, but not that the list is not empty. 
     * we however do NOT warrantee that the Paths exists or that are valid 
     * thus the  implementer need to check whenever or not the path exist, can be read
     * and such. 
     * @param fileList a NON-null list that contains the paths desired to be loaded. 
     */
    public abstract void NotifyFoundPaths(List<Path> fileList);

    public abstract void NotifyFoundUrl(URL link);

    public abstract void DropComplete(DropTargetDropEvent dtde);

    /**
     * if the event contains AT LEAST one of the Flavors to ignore. will return
     * true.
     *
     * @param dtde the Event to analize.
     * @return true if ANY of the flavors match.
     */
    private boolean ignoreFlavor(DropTargetDragEvent dtde) {
        // we asume is not null
        for (var flavor : dtde.getCurrentDataFlavors()) {
            if (FlavorsIgnore.contains(flavor)) {
                return true;
            }
        }
        return false;
    }

}
