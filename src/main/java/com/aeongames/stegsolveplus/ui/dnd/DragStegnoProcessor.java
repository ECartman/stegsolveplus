/* 
 *  Copyright © 2024-2025 Eduardo Vindas Cordoba. All rights reserved.
 *  
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package com.aeongames.stegsolveplus.ui.dnd;

import com.aeongames.edi.utils.datatransfer.DataTransferException;
import com.aeongames.edi.utils.datatransfer.FlavorProcessor;
import com.aeongames.edi.utils.file.PropertiesHelper;
import com.aeongames.edi.utils.error.LoggingHelper;
import com.aeongames.edi.utils.threading.StopSignalProvider;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * a FlavorProcessor that is designed to process Events that might relate to a
 * Image, this Processor Handles several flavors. we might want to change that one
 * per processor 
 * and handle. them as required. for this class currently it handles
 * Files, URL and Text other events might require further code to be added.
 *
 * important. unlike the Clipboard Listener. DnD events are Event based. and
 * most be handled on the EDT. if we try to handle the data on another Thread
 * and allow the EDT to disengage it will notify the source of the Drag. within
 * the same app that might be acceptable but in a IPC scenario it causes the
 * data to be loss or any stream to become invalid. thus we MUST handle the
 * event on the EDT. to that end we will handle the events by reading them as
 * references to a file or URL. (and TODO: handle as Image Binary Stream that we
 * can load into memory) we then notify a listener. and the listener should
 * spawn a Thread to handle the File, url, or reference as it deems required.
 *
 * @author Eduardo Vindas
 * @version 1.5
 */
public class DragStegnoProcessor implements FlavorProcessor {

    /**
     * the Logger name for This class
     */
    private static final String LOGGERNAME = "DataTransferLogger";
    /**
     * the flavors this Class can handle. it is important to note the order also
     */
    public static final DataFlavor[] Myflavors = new DataFlavor[]{
        DataFlavor.javaFileListFlavor,
        new DataFlavor("application/x-java-url;class=java.net.URL", "URL Flavor"),
        new DataFlavor("text/uri-list;class=java.lang.String", "URI Flavor"),
        DataFlavor.stringFlavor // this might be slow for Long chunks of text that might represent files.
    //DataFlavor.getTextPlainUnicodeFlavor(), //TODO: Support better this flavor. 
    // DataFlavor.imageFlavor   //TODO:  Support this Flavor! 
    };

    private final ImageInputListener ImageListener;

    //<editor-fold defaultstate="collapsed" desc="Regex Property File">
    /**
     * the location of the Regex Properties
     */
    private static final String RESOURCEPATH = "/com/aeongames/stegsolveplus/text/Regex.properties";
    private static final String WINDOWSPATHSETTING = "WindowsPathPattern";
    private static final String LINUXPATHSETTING = "LinuxPathPattern";
    private static final String URL_PATTERN_SETTING = "URLPattern";
    /**
     * the Helper to load the Properties settings (regex on this case)
     */
    private static final PropertiesHelper REGEX_RESOURCE;

    static {
        PropertiesHelper res = null;
        try {
            res = new PropertiesHelper(RESOURCEPATH, false, DragStegnoProcessor.class);
        } catch (IOException ex) {
            LoggingHelper.getLogger(LOGGERNAME).log(Level.SEVERE, "unable to load Regular Expresion Resource app might crash", ex);
        }
        REGEX_RESOURCE = res;
    }
    //</editor-fold >

    //<editor-fold defaultstate="collapsed" desc="Patterns">
    /**
     * File Patterns for Windows Files.
     */
    public static final Pattern WINDOWS_PATTERN = Pattern.compile(REGEX_RESOURCE.getProperty(WINDOWSPATHSETTING));
    /**
     * Linux file pattern
     */
    public static final Pattern LINUX_PATTERN = Pattern.compile(REGEX_RESOURCE.getProperty(LINUXPATHSETTING));
    /**
     * URL pattern. here we narrow a lot of what is actually allowed for a URL
     * pattern to HTTP,HTTPS, FTP and file URL. this is done as those are the
     * only ones we want to handle on the app. (unless modified on the Property
     * file)
     */
    public static final Pattern URL_PATTERN = Pattern.compile(REGEX_RESOURCE.getProperty(URL_PATTERN_SETTING));
    //</editor-fold >

    /**
     * if Dragged Items are files that needs to be stored for performance
     * reasons for example if the resource is a network file or temporal
     * resource and we need to analyze over time. we will required to store
     * temporary. thus using this directory.
     */
    private final Path ANALISIS_DIRECTORY;

    /**
     * create a new instance of this Class.receive multiple Flavors to be
     * ignored
     *
     * @param listener the listener to get notifications when a DnD item is
     * found
     */
    public DragStegnoProcessor(ImageInputListener listener) {
        ANALISIS_DIRECTORY = prepareTmpFolder();
        ImageListener = Objects.requireNonNull(listener, "The listener Cannot be null");
    }

    private Path prepareTmpFolder() {
        Path tmp = null;
        try {
            tmp = Files.createTempDirectory("ImageAnalisis");
            tmp.toFile().deleteOnExit();
        } catch (IOException ex) {
            LoggingHelper.getLogger(DragStegnoProcessor.class.getName()).log(Level.SEVERE, "Unable to create TMP folder to analisis", ex);
        }
        return tmp;
    }

    /**
     * gets the Object from the Transferable data.
     *
     * @param flav the flavor to request the data from
     * @param DropTransfeable the Transferable data.
     * @return a Object that can be null or a representation of the flavor
     * class.
     */
    private Object getTransferibleData(DataFlavor flav, Transferable DropTransfeable) {
        var log = LoggingHelper.getLogger(LOGGERNAME);
        Object ob = null;
        try {
            ob = DropTransfeable.getTransferData(flav);
        } catch (UnsupportedFlavorException ex) {
            log.log(Level.SEVERE, "Unsuported Flavor", ex);
        } catch (IOException ex) {
            log.log(Level.SEVERE, "IO unable to transfer data?", ex);
        }
        if (ob == null) {
            log.log(Level.WARNING, "The Transferible Object cannot be read");
        }
        return ob;
    }

    //<editor-fold defaultstate="collapsed" desc="File Related Functions">
    /**
     * check if the provided URL refer or targets a File
     *
     * @param url the URL to check
     * @return null if the URL is not a file. otherwise return a Path Reference.
     */
    private Path CheckifFile(URL url) {
        try {
            return CheckifFile(url.toURI());
        } catch (URISyntaxException ex) {
            LoggingHelper.getLogger(LOGGERNAME).log(Level.SEVERE, "unable to transform the URL to URI", ex);
            return null;
        }
    }

    /**
     * check if the provided URI refer or targets a File
     *
     * @param uri the URI to check
     * @return null if the URI is not a file. otherwise return a Path Reference.
     */
    private Path CheckifFile(URI uri) {
        var scheme = uri.getScheme();
        if (scheme != null ? scheme.equalsIgnoreCase("file") : false) {
            return Path.of(uri);
        } else {
            return null;
        }
    }

    /**
     * Process the Flavor assuming the flavor relates to a java File list.
     *
     * @param Flavor the flavor to handle
     * @param DropTransfeable the Transferable Object to handle.
     * @return a list if files. or null if not data is provided.
     */
    private List<Path> preProcessFile(DataFlavor Flavor, Transferable DropTransfeable) {
        var ob = getTransferibleData(Flavor, DropTransfeable);
        if (ob instanceof List<?> FileList) {
            List<Path> ValidFiles = new ArrayList<>(FileList.size());
            for (var objFile : FileList) {
                if (objFile instanceof File DndFile
                        && !DndFile.getName().matches("(?i).*\\.url")) {
                    var pathForFile = PrepareFile(DndFile);
                    ValidFiles.add(pathForFile);
                }
            }
            if (!ValidFiles.isEmpty()) {
                return ValidFiles;
            }
        }
        return null;
    }

    /**
     * prepares a file to be used. if the file exist and can be read. we will
     * check if this file is a Temporal file. if so we made a copy because the
     * Source that Started the drag of the image MAY OR MIGHT NOT Delete the
     * file or overwrite. to avoid this we will check if the image dragged is on
     * the TMP folder. if so we make a copy
     * <br>
     * Performance Considerations: this Function. unfortunately. requires to
     * check if the File Exist and if it can be read. this check can be slow due
     * the need to check on the FileSystem. also given that the source Could
     * remove the file upon return from the Drag and Drop action. it might be
     * needed for us to held the Thread hostage.
     * <br>
     *
     * @param Potentialfile the file to analyze
     * @return a Path that target the original file OR if the file is at the
     * temporal folder a copy of that that we can use securely.
     */
    private Path PrepareFile(File Potentialfile) {
        var log = LoggingHelper.getLogger(LOGGERNAME);
        var pathForFile = Potentialfile.toPath();
        if (!Potentialfile.exists() || !Potentialfile.canRead()) {
            return pathForFile;
        }
        //the Source that Started the drag of the image MAY OR MIGHTNOT 
        //Delete the file or overwrite. to avoid this we will check if 
        //the image dragged is on the TMP folder. if so we make a copy
        //and use the copy (if sucessful) otherwise we consede and 
        //use the file we set to use
        if (ANALISIS_DIRECTORY != null) {
            var tmpfolder = ANALISIS_DIRECTORY.getParent();
            if (tmpfolder.equals(pathForFile.getParent())) {
                try {
                    var tmpcopy = Files.createTempFile(ANALISIS_DIRECTORY, pathForFile.getFileName().toString(), null);
                    tmpcopy.toFile().deleteOnExit();
                    Files.copy(pathForFile, tmpcopy, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
                    return tmpcopy;
                } catch (IOException ex) {
                    log.log(Level.SEVERE, "Fail to create a TMP copy", ex);
                }
            }
        }
        return pathForFile;
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Process URL/URI">
    private URL preProcessURL(DataFlavor flavor, Transferable DropTransfeable) {
        var ob = getTransferibleData(flavor, DropTransfeable);
        URL linkreturn = null;
        switch (ob) {
            case URL linkUrl ->
                linkreturn = linkUrl;
            case URI linkURI -> {
                try {
                    linkreturn = linkURI.toURL();
                } catch (MalformedURLException e) {
                    LoggingHelper.getLogger(LOGGERNAME).log(Level.WARNING, "The Transferible Object defines a Malformed URL", e);
                }
            }
            default -> {
                String tmp = Objects.isNull(ob) ? "Null Value" : ob.getClass().getName();
                LoggingHelper.getLogger(LOGGERNAME).log(Level.WARNING, "The Transferible Object is NOT URL! reports as: {0}", tmp);
            }
        }
        return linkreturn;
    }

    private URI preProcessURI(DataFlavor flavor, Transferable DropTransfeable) {
        var ob = getTransferibleData(flavor, DropTransfeable);
        URI linkreturn = null;
        switch (ob) {
            case URI linkURI -> {
                linkreturn = linkURI;
            }
            default -> {
                String tmp = Objects.isNull(ob) ? "Null Value" : ob.getClass().getName();
                LoggingHelper.getLogger(LOGGERNAME).log(Level.WARNING, "The Transferible Object is NOT URL! reports as: {0}", tmp);
            }
        }
        return linkreturn;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="ProcessFlavor">
    @Override
    public boolean handleFlavor(DataFlavor flavor, StopSignalProvider stopProvider, Transferable transferData) throws DataTransferException {
        if (flavor.isFlavorJavaFileListType()) {
            //try to pull files. 
            List<Path> fileList = preProcessFile(flavor, transferData);
            if (fileList != null) {
                publishFiles(fileList);
                return true;
            }
        }
        //it is posible for the path list to be a file that was in reality a URL 
        //Or fail to read. in such cases lets review if we can read URL or URI data.
        if (transferData.isDataFlavorSupported(Myflavors[1])) {
            flavor = Myflavors[1];
            URL FileLink;
            if ((FileLink = preProcessURL(flavor, transferData)) != null) {
                //check if the URL is a File on the system.
                var path = CheckifFile(FileLink);
                if (path != null) {
                    publishFiles(List.of(path));
                    return true;
                }
                publishURL(FileLink);
                return true;
            }
        }
        if (transferData.isDataFlavorSupported(Myflavors[2])) {
            flavor = Myflavors[2];
            URI FileLink;
            if ((FileLink = preProcessURI(flavor, transferData)) != null) {
                //check if the URL is a File on the system.
                var path = CheckifFile(FileLink);
                if (path != null) {
                    publishFiles(List.of(path));
                    return true;
                }
                publishURI(FileLink);
                return true;
            }
        }
        if (transferData.isDataFlavorSupported(Myflavors[3])) {
            flavor = Myflavors[3];
            switch (preProcessText(flavor, transferData)) {
                case Path path -> {
                    publishFiles(List.of(path));
                    return true;
                }
                case URI uri -> {
                    publishURI(uri);
                    return true;
                }
                case URL url -> {
                    publishURL(url);
                    return true;
                }
                case String data -> {
                    //TODO: Remove or handle it better this is REALLY bad and slow. 
                    //we might desire to handle the drag of text as a stream
                    String DropData = data;
                    publishTextData(DropData);
                    return true;
                }
                case LinkedList<?> List -> {
                    List<Path> fileList = new LinkedList<>();
                    for (Object element : List) {
                        if (element instanceof Path file) {
                            fileList.add(file);
                        }
                    }
                    if (!fileList.isEmpty()) {
                        publishFiles(fileList);
                        return true;
                    }
                }
                default -> {
                }
            }
        }
        //TODO ADD other Flavors
        LoggingHelper.getLogger(LOGGERNAME).log(Level.INFO, "Detected Something that cannot be handled:");
        return false;
    }
    //</editor-fold>

    /**
     * Process a Drop action which Flavor is MimeType text. (note there are
     * several Text mime's types )
     *
     * @param TextFlavor the specific flavor that is of type Text.
     * @param dtde the Drop event.
     */
    private Object preProcessText(DataFlavor flavor, Transferable DropTransfeable) {
        var ob = getTransferibleData(flavor, DropTransfeable);
        if (ob == null) {
            return null;
        }
        if (ob instanceof String Data) {
            Data = Data.strip();
            //a String Could be a path to a file or a URL. 
            //if neither. COULD be a string representation of the file. content 
            //but we will NOT support that. 
            if (URL_PATTERN.matcher(Data).matches()) {
                URI uri = URI.create(Data);
                var path = CheckifFile(uri);
                if (path != null) {
                    return path;
                }
                return uri;
            }
            var iswin = System.getProperty("os.name", "generic").toLowerCase().strip().contains("window");
            /**
             * TODO: it seems that Parsing string into Linux might need further
             * computation seemly as utf8->utf16 might work funky.. but need
             * testing. we already know how to do this from Clipboard tool but
             * we need testing to determine which approach is better.
             */
            var matcher = iswin
                    ? WINDOWS_PATTERN.matcher(Data)
                    : LINUX_PATTERN.matcher(Data);
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
            LoggingHelper.getLogger(LOGGERNAME).log(Level.WARNING, "No Configuration matched the Text provided: {0}", Data);
            return Data;
        }
        return null;
    }

    private void publishFiles(final List<Path> fileList) {
        ImageListener.HandleFileList(fileList);
    }

    private void publishURL(final URL FileLink) {
        ImageListener.HandleURL(FileLink);
    }

    private void publishURI(final URI FileLink) {
        ImageListener.HandleURI(FileLink);
    }
    private void publishTextData(final String data) {
        ImageListener.HandleTextData(data);
    }

}
