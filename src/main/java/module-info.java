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
/*
due the usage of Metadata Extractor we need to abstain from using the Module functionality 
as this package is not modular. */
module StegnoSolverPlus {
    requires java.base;
    requires transitive java.desktop;
    requires transitive java.datatransfer;
    requires transitive java.logging;
    requires transitive java.management;
    requires org.pushingpixels.radiance.common;
    requires org.pushingpixels.radiance.theming;
    requires org.pushingpixels.radiance.component;
    requires org.apache.commons.text;
    requires org.apache.commons.lang3;
    exports com.aeongames.stegsolveplus.app;
    requires com.aeongames.edi.utils;
    requires com.aeongames.edi.utils.pojo;
    requires metadata.extractor; //--> this module does not use Java Modules. thus this will show a warning. 
                                 //either we patch it ourselves or live with the warning or remove modularity from this project. 
}
