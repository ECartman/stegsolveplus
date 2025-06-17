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

import java.awt.Color;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.UIManager;
import java.util.logging.Level;
import java.awt.IllegalComponentStateException;
import javax.swing.UnsupportedLookAndFeelException;
import static com.aeongames.stegsolveplus.ui.MainFrame.UIlogger;
import org.pushingpixels.radiance.theming.api.skin.RadianceNightShadeLookAndFeel;

/**
 * Misc Functions for LAF 
 * @author Eduardo Vindas
 */
public class LafFunctions {

    private LafFunctions() {
        throw new IllegalCallerException("should not be instanciated");
    }

    // <editor-fold defaultstate="collapsed" desc="Start Up Functions">
    /**
     * Initialize the LAF for the application. this function needs to be called
     * on the EDT
     */
    public static void InitLAF() {
        String LaFName = RadianceNightShadeLookAndFeel.class.getName();
        if (!trySetLaFByName(LaFName)) {
            trySetLaFByName(UIManager.getSystemLookAndFeelClassName());
        }
    }

    static boolean trySetLaFByName(String Name) {
        var result = false;
        try {
            UIManager.setLookAndFeel(Name);
            result = true;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            //unable to set the UI LAF we could try just allowing the defaults. 
            UIlogger.log(Level.SEVERE, "Unable to setup the UI LaF", e);
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
    // </editor-fold>  
    
        // <editor-fold defaultstate="collapsed" desc="LAF">
    static void SetNimbusUI(JFrame frame) {
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
                SetUIClass(frame,info.getClassName());
                break;
            }
        }
    }

    static void SetDefOSUI(JFrame frame) {
        frame.setVisible(false);
        SetUIClass(frame,UIManager.getSystemLookAndFeelClassName());
        //this.setVisible(true);
    }

    static void setRadianceUI(JFrame frame) {
        frame.setVisible(false);
        SetUIClass(frame,RadianceNightShadeLookAndFeel.class.getName());
        frame.setVisible(true);
    }

    static void SetUIClass(JFrame frame,String Name) {
        frame.dispose();
        trySetLaFByName(Name);
        javax.swing.SwingUtilities.updateComponentTreeUI(frame);
        var supdeco = UIManager.getLookAndFeel().getSupportsWindowDecorations();
        try {
            frame.setUndecorated(supdeco);
        } catch (IllegalComponentStateException err) {
        }
        try {
            frame.getRootPane().setWindowDecorationStyle(supdeco ? JRootPane.FRAME : JRootPane.NONE);
        } catch (IllegalComponentStateException err) {
        }
        frame.revalidate();
    }
    // </editor-fold >

}
