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
package com.aeongames.edi.utils.File;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Properties;

/**
 * this class is intended to save and load data from one or multiple properties
 * file the property file is a text file that use a pattern:
 * <br>
 * variable=value
 * <br>
 * ...
 * <br>
 * #comment ...
 * <br>
 * and so on... it is useful to read user editable settings . this class has
 * also been adapted to use a XML schema implemented by sun now oracle...
 *
 * @author Eduardo Vindas
 * @version 1.9
 */
public final class PropertiesHelper {

    /**
     * possible Save Response.
     */
    public enum Response {

        SAVED,
        NOREQUIRED,
        ERROR,
        FILENOTSET;
    }
    private Path SaveLocation = null;
    /**
     * whenever or not the data was read and therefore required to be recorded
     * as XML
     */
    private boolean ReadSafeAsXML = false;
    /**
     * a simple date format.
     */
    private static final SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
    /**
     * refer to {@link java.util.Properties} for more details on this Object.
     * this Properties Table hold all the properties loaded on this class. and
     * that might been concatenated into it.
     */
    private final Properties PropertiesTable;
    /**
     * a boolean to determine if the underline properties has non Commit changes
     * note: loading properties into this class from file dies not cause Changes
     * to be noted as non commit.
     */
    private boolean HasChanges = false;
    /**
     * a list of files loaded on this Property object
     */
    private final HashMap<String, ResourceInfo> LoadedResources = new HashMap<>();
    private final HashMap<Path, Boolean> LoadedFiles = new HashMap<>();

    private final class ResourceInfo {

        private final Class<? extends Object> LoadClass;
        private final boolean isXml;

        ResourceInfo(Class<? extends Object> mClass, boolean isXml) {
            LoadClass = mClass;
            this.isXml = isXml;
        }
    }

    /**
     * creates a new instance of this class that loads its properties from the
     * specified file
     *
     * @param File the file to load settings from.
     * @throws IOException there is a error loading the data from the file
     */
    public PropertiesHelper(Path File) throws IOException {
        this(File, false);
    }

    public PropertiesHelper(Path file, boolean XML) throws IOException {
        PropertiesTable = new Properties(10);
        loadFileProperties(file, false, XML);
    }

    public PropertiesHelper(String Resource, boolean XML, Class InternalLoadClass) throws IOException {
        PropertiesTable = new Properties(10);
        if (Objects.nonNull(Resource) && !Resource.strip().isEmpty()) {
            loadResouceProperties(Resource, InternalLoadClass, false, XML);
        }
    }

    /**
     * Loads And if specified Concatenates all the settings already loaded.
     * please be aware that this will rewrite settings that have not been saved
     * if settings with the same name exists. also if Concatenate is set to
     * false all new settings will be dropped.
     *
     * @param Filepath the File Path to read.
     * @param concat if settings are to be Added to the ones already available.
     * @param XML if a XML file will be loaded or a prop file
     * @throws java.io.IOException
     */
    public synchronized void loadFileProperties(Path Filepath, boolean concat, boolean XML) throws java.io.IOException {
        var Absolute = loadFilePropertiesInternal(Filepath, concat, XML);
        LoadedFiles.put(Absolute, XML);
    }

    private Path loadFilePropertiesInternal(Path Filepath, boolean concat, boolean XML) throws java.io.IOException {
        if (!Files.exists(Filepath) || !Files.isRegularFile(Filepath)) {
            throw new FileNotFoundException("File Does not Exist or is A Directory");
        }
        if (!Files.isReadable(Filepath)) {
            throw new IOException("File Cannot be Read");
        }
        if (!concat) {
            PropertiesTable.clear();
            HasChanges = false;
        }
        Filepath = Filepath.toAbsolutePath();
        if (!XML) {
            PropertiesTable.load(Files.newInputStream(Filepath));
        } else {
            PropertiesTable.loadFromXML(Files.newInputStream(Filepath));
        }
        if (SaveLocation == null) {
            ReadSafeAsXML = XML;
            SaveLocation = Filepath;
        }
        return Filepath;
    }

    /**
     *
     * @param Resource the internal resource to read
     * @param classfrom the class calling to read
     * @param concat if concatenate the values or load from 0 and remove all new
     * variables and changes
     * @param XML if the read file is XML or props file
     * @throws java.io.IOException if the resource cannot be loaded or found.
     */
    public synchronized void loadResouceProperties(String Resource, Class classfrom, boolean concat, boolean XML) throws java.io.IOException {
        loadResourceInternal(Resource, classfrom, concat, XML);
        LoadedResources.put(Resource, new ResourceInfo(classfrom, XML));
    }

    private void loadResourceInternal(String Resource, Class classfrom, boolean concat, boolean XML) throws IOException {
        if (!concat) {
            HasChanges = false;
            PropertiesTable.clear();
        }
        classfrom = Objects.requireNonNullElse(classfrom, this.getClass());
        var resource = classfrom.getResourceAsStream(Resource);
        if (resource == null) {
            throw new IOException("Cannot Load The Resource");
        }
        if (!XML) {
            PropertiesTable.load(resource);
        } else {
            PropertiesTable.loadFromXML(resource);
        }
    }

    /**
     * reloads all settings from files, will clear all current changes and will
     * remove any cache. be aware if you need to do a cached task you need to
     * reset to do a cache.
     *
     * @return
     * @throws java.io.IOException if one or more files cannot be loaded.
     */
    public synchronized int ReLoadSettings() throws java.io.IOException {
        int LoadFails = 0;
        PropertiesTable.clear();
        HasChanges = false;
        //check if we have been saving into the File 
        if (SaveLocation != null && LoadedFiles.isEmpty()) {
            try {
                loadFilePropertiesInternal(SaveLocation, true, ReadSafeAsXML);
            } catch (IOException e) {
                throw e;
            }
            return 0;
        }
        //if we did not save or are not saving the properties into a file then load
        //them again from the sources we have registered.
        for (var entry : LoadedResources.entrySet()) {
            try {
                loadResourceInternal(entry.getKey(), entry.getValue().LoadClass, true, entry.getValue().isXml);
            } catch (IOException e) {
                LoadFails++;
            }
        }
        for (var entry : LoadedFiles.entrySet()) {
            try {
                loadFilePropertiesInternal(entry.getKey(), true, entry.getValue());
            } catch (IOException e) {
                LoadFails++;
            }
        }
        return LoadFails;
    }

    /**
     * returns true if changes were done but are not stored on file.
     *
     * @return
     */
    public synchronized boolean HasUnsavedChanges() {
        return HasChanges;
    }

    /**
     * returns the Location (if any) where the settings will be saved. IF there
     * are no Files loaded and or all resources are internal. no changes to the
     * Properties will be saved.
     *
     * @return null if no file backs this settings and are all from internal
     * resources otherwise a string with the file path.
     */
    public synchronized Path getInPlaseSaveLocation() {
        return SaveLocation;
    }

    /**
     * return a list containing the filenames or file path of the files loaded
     * into this object if none is avail it returns a empty list
     *
     * @return String list with all the names of the files loaded.
     */
    public synchronized LinkedList<Path> getAllFilesLoaded() {
        LinkedList<Path> filelist = new LinkedList<>();
        for (var entry : LoadedFiles.entrySet()) {
            filelist.add(entry.getKey());
        }
        return filelist;
    }

    /**
     *
     * @param FileLocation the file to record into
     * @param HeaderInfo a header comment.
     * @param XML whenever to save with XML pattern or not.
     * @return whenever or not successful, also might return false if the save
     * is not needed.(values have not changed)
     */
    public synchronized Response SaveIfNeeded(Path FileLocation, String HeaderInfo, boolean XML) {
        if (!HasChanges) {
            return Response.NOREQUIRED;
        }
        return saveCopy(FileLocation, HeaderInfo, XML) ? Response.SAVED : Response.ERROR;
    }
    
    /**
     * saves the settings to the specified Settings file (that is manually
     * specified or the same file from the settings were loaded) if no file is
     * set will return false and no settings will be recorded. this method will
     * and cannot record to internal resources.
     *
     * @return whenever or not successful, also might return false if the save
     * is not needed.(values have not changed)
     */
    public synchronized Response SaveIfNeeded() {
        if (HasChanges) {
            if (SaveLocation != null) {
                return saveCopy(SaveLocation,
                        String.format("Property File Last Modification: %s",
                                format.format(Calendar.getInstance().getTime())),
                        ReadSafeAsXML) ? Response.SAVED : Response.ERROR;
            } else {
                return Response.FILENOTSET;
            }
        } else {
            return Response.NOREQUIRED;
        }
    }

    /**
     * for performance considerations please consider using
     * {@link #SaveIfNeeded(java.nio.file.Path, java.lang.String, boolean)}
     *
     * @param FileLocation the path to create a copy of the properties as a property file
     * @param HeaderInfo the String to record as header
     * @param XML if desired to record as XML 
     * @return if success or not to record. 
     */
    public synchronized boolean saveCopy(Path FileLocation, String HeaderInfo, boolean XML) {
        if (!Files.isWritable(FileLocation)) {
            return false;
        }
        try {
            if (!XML) {
                PropertiesTable.store(Files.newOutputStream(FileLocation), HeaderInfo);
            } else {
                PropertiesTable.storeToXML(Files.newOutputStream(FileLocation), HeaderInfo);
            }
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    /**
     * reads and gather a property, if it is not found or the property is null
     * returns null
     *
     * @param KeyName they property name
     * @return the String value for that key. or otherwise if not found a null
     */
    public synchronized String getProperty(String KeyName) {
        return PropertiesTable.getProperty(KeyName);
    }

    /**
     * Changes the Value or include properties to the internal properties. if
     * the Value is Null this function may fail please refer to
     * {@link java.util.Properties#setProperty(java.lang.String, java.lang.String)}
     *
     * @param KeyName the key name of the Property to change or include
     * @param Value the new value to include.
     * @return the old value or Null if none exists.
     */
    public synchronized Object Setproperty(String KeyName, String Value) {
        //check if the new value is the same as old: 
        var oldval = PropertiesTable.getProperty(KeyName);
        if (oldval == null && Value == null) {
            //nothing changed
            return null;
        }
        //we dont desire empty NEW properties 
        //also we dont desire to strip whatever will be saved. only to check.
        var tmpValue = Value.strip();
        if (oldval == null && (tmpValue.isBlank() || tmpValue.isEmpty())) {
            //nothing changed
            return null;
        }
        if (oldval != null && Value.equals(oldval)) {
            //nothing changed
            return oldval;
        }
        HasChanges = true;
        return PropertiesTable.setProperty(KeyName, Value);
    }
}
