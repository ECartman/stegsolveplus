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
 */
package com.aeongames.stegsolveplus.StegnoTools;

import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.EventListener;
import java.util.List;

/**
 * this interface. defines a Event that notifies that a Service or functionality
 * has trigger an event that provides a Image. 
 * the image might come in via A File, a URI/URL, a Image reference
 * @author Eduardo Vindas
 */
public interface ImageInputListener extends EventListener{

    /**
     * called by the Processor when it has compiled data from DnD
     * and relates to one or multiple files.
     * @param fileList the list of files that required to be handled. 
     */
    public void HandleFileList(List<Path> fileList);

    /**
     * called by the Processor when it has detected a URL information
     * @param FileLink the URL to handle.
     */
    public void HandleURL(URL FileLink);

    /**
     * called by the Processor when it has detected a URL information
     * @param FileLink the URI to handle.
     */
    public void HandleURI(URI FileLink);
    
}
