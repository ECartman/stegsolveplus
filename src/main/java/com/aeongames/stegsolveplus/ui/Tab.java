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
package com.aeongames.stegsolveplus.ui;

import static org.pushingpixels.radiance.theming.api.RadianceThemingCortex.GlobalScope;

/**
 * this (abstract) class is a template to create tabs that are based on Swing on
 * {@link  javax.swing.JPanel}, this class is to help and automate the process
 * of setting and updating the title on a {@link javax.swing.JTabbedPane}. as
 * well as registering and adding the close Component.
 *
 * @author Eduardo Vindas C
 */
public abstract class Tab extends javax.swing.JPanel {

    /**
     * The index of this tab that is reported back into this class. or setup
     * manually. this value is only used for Identification on the title
     */
    private int TabIndex;

    /**
     * this String is a variable used to identify a instance of a tab(panel)
     * that can be composed on run-time to show other details such as the index.
     */
    private String TabTitle;

    /**
     * this value represent the setting whenever or not the tittle can be
     * changed after it has been setup (via constructor)
     */
    private boolean AllowTitleChange = true;

    /**
     * a binary that determine if the title of the tab needs to be composed to
     * contain the tab ID.
     */
    private boolean ConcatIdOnTitle = true;

    /**
     * {@inheritDoc}
     *
     * @param title Title for this Panel to be used on
     * {@link  javax.swing.JPanel#getName()}
     */
    protected Tab(String title) {
        TabTitle = title;
        super.setName(title);
    }

    /**
     * {@inheritDoc}
     */
    protected Tab() {
    }

    /**
     * set this component Cursor as the default
     * {@code java.awt.Cursor.DEFAULT_CURSOR}
     */
    protected void ClearCursor() {
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }

    /**
     * sets the cursor to busy.
     */
    protected void SetCursorBusy() {
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
    }

    /**
     * blocks the inputs and set the cursor to busy. since this class has no
     * inputs and the children Classes are the ones that contains and will
     * contain more components its up to them to define the correct behavior
     * remember to use {@link #busyCursor()}, or {@link #clearCursor()} as
     * required.
     */
    protected abstract void setBusy();

    /**
     * sets the input and cursors as available remember to use
     * {@link #busyCursor()}, or {@link #clearCursor()} as required.
     */
    protected abstract void setAvailable();

    /**
     * {@inheritDoc} gathers the Title/Name of this Tab
     *
     * @return the name/Title of this tab.
     */
    @Override
    public final String getName() {
        var tmptitle = TabTitle;
        if (tmptitle == null) {
            tmptitle = super.getName();
        }
        if (ConcatIdOnTitle && TabIndex>=0) {
            tmptitle = String.format("%d.%s", (TabIndex + 1), TabTitle);
        }
        return tmptitle;
    }

    /**
     * gathers the Title/Name of this Tab
     *
     * @return the tab tittle
     */
    public final String getTitle() {
        return getName();
    }

    /**
     * tell us whenever if the change is allow or not for the tittle
     *
     * @return true if tittle can be edited false otherwise
     */
    public boolean allowChange() {
        return AllowTitleChange;
    }

    /**
     * set whenever the tab name change is allow.
     *
     * @param changeallow sets whenever the title can be changed or not.
     */
    protected final void setAllowChange(boolean changeallow) {
        AllowTitleChange = changeallow;
    }

    /**
     * sets if able the LAFwidget menu this menu allow to do typical clipboard
     * task such as copy, paste,cut, delete, select all.
     *
     * @param component sets a pop up for text components.
     */
    protected final void putPopMenu(javax.swing.text.JTextComponent component) {
        GlobalScope.setTextEditContextMenuPresence(true);
        // component.putClientProperty(RadianceSynapse.TEXT_EDIT_CONTEXT_MENU,true);
    }

    /**
     * if allowed, changes the title of the Component by setting its internal
     * title string and the component name as well.
     *
     * @param newTitle the new value for the Tab title as well as used as
     * Component name.
     * @return true if it changed the value, false if not allowed or no change
     * was required (the old and new title are the same).
     */
    public boolean setTitle(String newTitle) {
        if (AllowTitleChange && (TabTitle == null || !TabTitle.equals(newTitle)) ) {
            TabTitle = newTitle;
            super.setName(TabTitle);
            return true;
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc} set the Title/Name of this Tab
     *
     * @param name the new Name/Title to set
     */
    @Override
    public void setName(String name) {
        if (AllowTitleChange) {
            TabTitle = name;
            super.setName(TabTitle);
        }
    }

    /**
     * sets this case tittle and the tittle is Also used on this class
     * .toString() Careful using this version, Use with caution.
     *
     * @param newTitle
     */
    protected void _InternalSetTitle(String newTitle) {
        TabTitle = newTitle;
        super.setName(TabTitle);
    }

    /**
     * sets the id(the tab index on the tab pane)
     *
     * @param location the location for this tab
     */
    public void setTabID(int location) {
        this.TabIndex = location;
    }

    /**
     * this method informs whenever or not this tab can be drag and dropped to a
     * different position within the JTabbedPane.
     *
     * @return true if this tab is draggable. false otherwise.
     */
    public boolean isDraggable() {
        return true;
    }

    /**
     * request focus on window if require to put a the focus on a particular
     * place, by all means you might overdrive the method. be careful thought.
     *
     * @see #requestFocusInWindow()
     * @return the result of requesting Focus to this Component.
     */
    public boolean setFocus() {
        return requestFocusInWindow();
    }

    /**
     * this function is called when a tab is to be removed from the tabPane and
     * is intended to do 2 task: - ask the Tab if it can be closed or if we
     * should held the tab open. - and perform cleanup of resources that a tab
     * might held.
     *
     * suggestion, do not call any UI changes to parent components as it might
     * cause undesired recursion.
     *
     * @param force if the tab is to be forcefully closed. (a warning or
     * question to ask for confirmation should be ignored and cleanup should be
     * performed)
     * @return true if the tab can be closed. false if the tab should be held
     * open
     */
    public abstract boolean Close(boolean force);
}
