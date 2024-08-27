/* 
 * Copyright © 2024 Eduardo Vindas. All rights reserved.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.aeongames.stegsolveplus.ui.tabcomponents;

import com.aeongames.edi.utils.visual.Panels.JAeonTabPane;
import java.awt.Component;
import java.util.Objects;
import javax.swing.Icon;

/**
 * a TabPane that supports the TabClose component, besides image background 
 * and Drag and drop. 
 * @author Eduardo Vindas
 * @see JAeonTabPane
 */
public class JStegnoTabbedPane extends JAeonTabPane {

    /**
     * {@inheritDoc}
     */
    @Override
    public void insertTab(String title, Icon icon, Component component, String tip, int index) {
        Objects.requireNonNull(component, "The Tab to be included is Null");
        title = Objects.requireNonNullElse(title, component.getName());
        tip = Objects.requireNonNullElse(tip, title);
        //add the tabbed panel.
        super.insertTab(title, icon, component, tip, index);
        //create and add the Tab component of the panel.
        var closeComponent = new TabClose(this);
        setTabComponentAt(index, closeComponent);
        //update the information on the component.
        closeComponent.Update(index);
        updateTabTitles(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeTabAt(int index) {
        var component = getComponentAt(index);
        if (component instanceof Tab tab) {
            tab.setTabID(-1);
        }
        super.removeTabAt(index);
        updateTabTitles(index);
    }

    /**
     * request the tabCloseComponent to update its title and information to be
     * updated. TODO:: this can be implemented on 2 ways one setting the
     * information on the tab close component. the other as a "change trigger or
     * change listener fire event." either approach works.
     *
     * @param DirtyIndex the start index where the change took effect.
     */
    private void updateTabTitles(int DirtyIndex) {
        for (int x = DirtyIndex; x < getTabCount(); x++) {
            var component = getComponentAt(x);
            if (component instanceof Tab tab) {
                tab.setTabID(x);
                var newtitle =tab.getTitle();
                setTitleAt(x,newtitle );
                setToolTipTextAt(x,newtitle);
            } else if (component != null) {
                var oldtitle = component.getName();
                oldtitle = Objects.requireNonNullElse(oldtitle, getTitleAt(x));
                int index = oldtitle.indexOf(".");
                if (index > -1) {
                    oldtitle = oldtitle.substring(index);
                }
                oldtitle = String.format("%d.%s", (x + 1), oldtitle);
                setTitleAt(x, oldtitle);
                setToolTipTextAt(x,oldtitle);
            }
            if (getTabComponentAt(x) instanceof TabClose tabClose) {
                tabClose.Update();
            }
        }
    }
}
