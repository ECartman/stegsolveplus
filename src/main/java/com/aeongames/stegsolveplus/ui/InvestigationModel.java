/* 
 *  Copyright © 2025 Eduardo Vindas Cordoba. All rights reserved.
 *  
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 * 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aeongames.stegsolveplus.ui;

import com.aeongames.edi.utils.pojo.PropertyPojo;
import com.aeongames.edi.utils.visual.link.JtextComponentAppendUpdateBind;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import java.util.List;
import java.util.Objects;
import javax.swing.text.JTextComponent;

/**
 *
 * @author cartman
 */
public class InvestigationModel {

    //lets link a Pojo to the text 
    private JtextComponentAppendUpdateBind bind = null;
    private JtextComponentAppendUpdateBind filetextfirstbind = null;    
    private JtextComponentAppendUpdateBind filetextsecondtbind = null;
    private PropertyPojo<String> MetadataTransport = PropertyPojo.newStringPojo();
    private PropertyPojo<String> FileBinarytoStrings = PropertyPojo.newStringPojo();    
    private PropertyPojo<String> FileSecondBinarytoStrings = PropertyPojo.newStringPojo();
    private PropertyPojo<String> StatusBarText;

    public PropertyPojo<String> getSingleByteTextPojo(){
        return FileBinarytoStrings;
    }
    
    public PropertyPojo<String> getWideTextPojo(){
        return FileSecondBinarytoStrings;
    }
    
    public PropertyPojo<String> getstatusPojo(){
        return StatusBarText;
    }
    
    public PropertyPojo<String> getMetadataPojo(){
        return MetadataTransport;
    }
    
    public void linkMetadataComponent(JTextComponent Metadatatxt) {
        if (Objects.nonNull(bind)) {
            bind.Unbound();
        }
        bind = new JtextComponentAppendUpdateBind(Metadatatxt, MetadataTransport);
    }
    
    public void linkFiletxtFirst(JTextComponent textFileStrings ) {
        if (Objects.nonNull(filetextfirstbind)) {
            filetextfirstbind.Unbound();
        }
        bind = new JtextComponentAppendUpdateBind(textFileStrings, FileBinarytoStrings);
    }
    
    public void linkFiletxtSecond(JTextComponent textFileStrings ) {
        if (Objects.nonNull(filetextsecondtbind)) {
            filetextsecondtbind.Unbound();
        }
        bind = new JtextComponentAppendUpdateBind(textFileStrings, FileSecondBinarytoStrings);
    }

    public void setStatusBarTextPojo(PropertyPojo<String> statusbar) {
        StatusBarText = Objects.requireNonNull(statusbar, "the Status Bar Pojo Should not be null");
    }

    public void ReportStatus(List<String> chunks) {
        if (Objects.nonNull(StatusBarText)) {
            chunks.forEach((t) -> {
                StatusBarText.setValue(t);
            });
        }
    }

    public void ReportMetadata(Metadata imageMetadata) {
        for (Directory directory : imageMetadata.getDirectories()) {
            for (Tag tag : directory.getTags()) {
                MetadataTransport.setValue(String.format("%s::\t %s:=\t %s%s", directory.getName(), tag.getTagName(), tag.getDescription(), "\n"));
            }
            if (directory.hasErrors()) {
                for (String error : directory.getErrors()) {
                    MetadataTransport.setValue("ERROR: " + error);
                }
            }
        }
    }
}
