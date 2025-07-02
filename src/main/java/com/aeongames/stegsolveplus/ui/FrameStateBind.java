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
package com.aeongames.stegsolveplus.ui;

import com.aeongames.edi.utils.pojo.BooleanPropertyPojo;
import com.aeongames.edi.utils.visual.link.BaseBinder;
import com.aeongames.edi.utils.visual.link.MCBoolCompEnableBind;
import com.aeongames.stegsolveplus.ui.tabcomponents.JStegnoTabbedPane;
import java.util.ArrayList;
import java.util.Objects;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * this class contains bindings for POJO into Frame components.
 *
 * @author Eduardo Vindas C
 */
public class FrameStateBind {

    /**
     * a list that tracks registered bindings.
     */
    public final ArrayList<BaseBinder<?, ? extends JComponent>> Bindings;
    /**
     * this POJO represent the Enable state of the Frame. and
     */
    private final BooleanPropertyPojo FrameEnabled = new BooleanPropertyPojo();
    private final JStegnoTabbedPane mainFrameTabPane;
    public FrameStateBind(JStegnoTabbedPane mainTabPane) {
        mainFrameTabPane= Objects.requireNonNull(mainTabPane, "the Tab Pane Cannot be Null");
        Bindings = new ArrayList<>(3);
    }
    public void setFrameEnabled(boolean enabledState) {
        if (SwingUtilities.isEventDispatchThread()) {
            //TODO: do other Verifications. 
            FrameEnabled.setValue(enabledState);
        } else {
            throw new IllegalThreadStateException("this function needs to be called from EDT");
        }
    }
    public boolean getFrameEnabled() {
        var value = FrameEnabled.getValue();
        if (Objects.isNull(value)) {
            return false;
        }
        return value;
    }
    public void updateFrameEnabled() {
        var isbusy = false;
        for (var index = 0; index < mainFrameTabPane.getTabCount(); index++) {
            if (mainFrameTabPane.getComponentAt(index) instanceof InvestigationTab tab) {
                if (isbusy = tab.isBusy()) {
                    break;
                }
            }
        }
        if (!Objects.equals(!isbusy, FrameEnabled.getValue())) {
            setFrameEnabled(!isbusy);
        }
    }
    
    /**
     * bind Frame Components to be disabled or enabled if the Frame is busy or
     * not.
     *
     * @param comps the components to bind to the Pojo.
     * @return the binding if desired to be used or tracked by caller.
     */
    MCBoolCompEnableBind bindEnabledComp(JComponent... comps) {
        Objects.requireNonNull(comps, "you need to provide at least 1 item");
        if (comps.length < 1) {
            throw new IllegalArgumentException("you need to provide at least 1 item");
        }
        var binding = new MCBoolCompEnableBind(comps[0], FrameEnabled);
        for (int i = 1; i < comps.length; i++) {
            binding.addComponent(comps[i]);
        }
        Bindings.add(binding);
        return binding;
    }

}
