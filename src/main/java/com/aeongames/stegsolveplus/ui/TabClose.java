/*
 * 
 * Copyright © 2008-2010,2014,2024 Eduardo Vindas. All rights reserved.
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

import com.aeongames.edi.utils.visual.TabCloseComp;
import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.JTabbedPane;

/**
 * TabClose.java a class that extend TabCloseComp that fits the needs for
 * closing tab that extend Tab.
 *
 * @author Eduardo Vindas C
 */
public class TabClose extends TabCloseComp {

    // <editor-fold defaultstate="collapsed" desc="Constructors"> 
    /**
     * Creates new form TabCloseComp this requires a JtabbedPane to be parsed by
     * a parameter.
     *
     * @param pane
     */
    public TabClose(JTabbedPane pane) {
        super(pane);
    }

    public TabClose(JTabbedPane pane, Color X_Color) {
        super(pane, X_Color);
    }

    public TabClose(JTabbedPane pane, Icon icon) {
        super(pane, icon);
    }

    public TabClose(JTabbedPane pane, Color X_Color, Icon icon) {
        super(pane, X_Color, icon);
    }
    // </editor-fold> 

    @Override
    protected boolean Close(int TabIndex) {
        if (mainpane.getTabComponentAt(TabIndex) != this) {
            return false;
        }

        boolean remove;
        if (mainpane.getComponentAt(TabIndex) instanceof Tab tab) {
            try {
                remove = tab.Close(false);
            } catch (Exception e) {
                //something failed this is a not apropiate aproach try chaning on a better way to log issues
                Logger.getLogger(TabClose.class.getName()).log(Level.SEVERE, null, e);
                remove = true;
            }
        } else {
            //we dont need to do further cleanup
            remove = true;
        }
        if (remove) {
            mainpane.removeTabAt(TabIndex);
        }
        return remove;
    }
}
