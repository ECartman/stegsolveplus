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
package com.aeongames.stegsolveplus.app;

import com.aeongames.stegsolveplus.ui.LafFunctions;
import com.aeongames.stegsolveplus.ui.MainFrame;
import static com.aeongames.stegsolveplus.ui.MainFrame.UIlogger;
import java.util.logging.Level;
import javax.swing.SwingUtilities;

/**
 *
 * this is a class that will not have instances that 
 * defines the Main Function as entry point
 * @author Eduardo Vindas 
 */
public class Entry {
        
    /**
     * Launches the Application.
     * @param args The Console parameters for this application. TODO use the
     * arguments someway.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LafFunctions.InitLAF();
            try {
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
            } catch (Exception e) {
                //log the error
                UIlogger.log(Level.SEVERE, "Exception at Main, Something crashed", e);
                throw e;
            }
        });
    }

}
