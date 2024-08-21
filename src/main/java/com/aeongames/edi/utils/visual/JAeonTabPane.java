/*
 *
 * Copyright © 2008-2011,2024 Eduardo Vindas. All rights reserved.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.aeongames.edi.utils.visual;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.Objects;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

/**
 * this class is designed to show a image instead of the ye old boring
 * background whenever there is not tabs begin display do note this class is
 * design to draw a image whenever there is no other thing on the tab pane as
 * per such a Tab also will support Drag and Drop of tabs.
 *
 * @author Eduardo Vindas
 */
public class JAeonTabPane extends JImageTabPane {

    // <editor-fold defaultstate="collapsed" desc="Constants">
    /**
     * name of this type of data from this class
     */
    private static final String DATA_FLAVOR_NAME = "JAeonTabPane";
    /**
     * the images to show when we want to drag in order to inform...
     */
    private static final Image DOWNIMG = java.awt.Toolkit.getDefaultToolkit().getImage(JAeonTabPane.class.getResource("/com/aeongames/stegsolveplus/ui/downarrow.png")),
            LEFTIMG = java.awt.Toolkit.getDefaultToolkit().getImage(JAeonTabPane.class.getResource("/com/aeongames/stegsolveplus/ui/leftarrow.png")),
            RIGHTIMG = java.awt.Toolkit.getDefaultToolkit().getImage(JAeonTabPane.class.getResource("/com/aeongames/stegsolveplus/ui/rightarrow.png")),
            UPIMG = java.awt.Toolkit.getDefaultToolkit().getImage(JAeonTabPane.class.getResource("/com/aeongames/stegsolveplus/ui/uparrow.png"));
    /**
     * the width we will draw the line for the user to know where to place the
     * tab.
     */
    private static final int LINEWIDTH = 3;
    /**
     * the flavor of the data on DND
     *
     * @see JAeonTabPane#DATA_FLAVOR_NAME
     */
    private static final DataFlavor J_AEON_TAB_FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, DATA_FLAVOR_NAME);
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="ReadOnly">
    /**
     * a transparent panel that is use to paint transparent components and
     * images.
     */
    private final GhostGlassPane glassPane = new GhostGlassPane();

    /* Drag and Drop operations to track the state of the user's gesture */
    private final DragSourceListener dsl = new DragSourceListener() {

        /**
         * for this particular Listener we just update the Cursor. when it enter
         * a Drop-able point
         *
         * @see DragSourceListener#dragEnter(java.awt.dnd.DragSourceDragEvent)
         */
        @Override
        public void dragEnter(DragSourceDragEvent e) {
            e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
        }

        /**
         * if Drag is finish or Exit the Zone of interest hide the highlight
         * Rectangle. and of course repaint the glass pane
         *
         * @see DragSourceListener#dragExit(java.awt.dnd.DragSourceEvent)
         */
        @Override
        public void dragExit(DragSourceEvent e) {
            e.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
            lineRect.setRect(0, 0, 0, 0);
            glassPane.setPoint(new Point(-1000, -1000));
            glassPane.repaint();
        }

        /**
         * for this particular Listener we just update the Cursor. when it drags
         * Over valid Positions it set the cursor where it allow the drop and
         * "No Drop" otherwise (outside of the section)
         *
         * @see DragSourceListener#dragOver(java.awt.dnd.DragSourceDragEvent)
         */
        @Override
        public void dragOver(DragSourceDragEvent e) {
            var DragLocation = e.getLocation();
            SwingUtilities.convertPointFromScreen(DragLocation, glassPane);
            int targetIdx = getTargetTabIndex(DragLocation);
            if (getTabAreaRectangle().contains(DragLocation) && targetIdx >= 0
                    && targetIdx != dragIndex && targetIdx != dragIndex + 1) {
                e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
                glassPane.setCursor(DragSource.DefaultMoveDrop);
            } else {
                e.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
                glassPane.setCursor(DragSource.DefaultMoveNoDrop);
            }
        }

        /**
         * if Drag has ended hide the highlight Rectangle. and of course repaint
         * the glass pane
         *
         * @see DragSourceListener#dragDropEnd(java.awt.dnd.DragSourceDropEvent)
         */
        @Override
        public void dragDropEnd(DragSourceDropEvent e) {
            lineRect.setRect(0, 0, 0, 0);
            dragIndex = -1;
            glassPane.setVisible(false);
            if (DrawsGhost()) {
                glassPane.setVisible(false);
                glassPane.setImage(null);
            }
        }

        /**
         * this particular implementation does nothing.
         *
         * @see
         * DragSourceListener#dropActionChanged(java.awt.dnd.DragSourceDragEvent)
         */
        @Override
        public void dropActionChanged(DragSourceDragEvent e) {
        }
    };
    /* drag and Drop Transferible details */
    private final Transferable dndTransferable = new Transferable() {

        @Override
        public Object getTransferData(DataFlavor flavor) {
            return JAeonTabPane.this;
        }

        /**
         * Returns an array of DataFlavor objects indicating the flavors the
         * data can be provided in {@link #SupportFlavors}. currently supports:
         * <ul>
         * <li>{@link JAeonTabPane#J_AEON_TAB_FLAVOR}</li>
         * </ul>
         *
         * @return an array of data flavors in which this data can be
         * transferred
         */
        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return SupportFlavors;
        }

        /**
         * Returns whether or not the specified data flavor is supported for
         * this object, the supported list is at {@link #SupportFlavors}.
         *
         *
         * @param flavor the requested flavor for the data
         * @return boolean indicating whether or not the data flavor is in
         * {@link #SupportFlavors}.
         */
        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            for (DataFlavor SupportFlavor : SupportFlavors) {
                if (SupportFlavor.equals(flavor)) {
                    return true;
                }
                if (flavor.getHumanPresentableName().equals(SupportFlavor.getHumanPresentableName())) {
                    return true;
                }
            }
            return false;
        }
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="variables">
    /**
     * the index from we will drag the tab.
     */
    private int dragIndex = -1;
    /**
     * determines whenever is fine to paint the scroll area components
     */
    private boolean isPaintScrollArea = true;
    /**
     * when Drag and Dropping we should move the Tab Component too. defaults to
     * false.
     */
    private boolean moveTabComponent = false;
    /**
     * determines if we should paint the ghost of the component we are dragging
     */
    private boolean ShouldDrawGhost = true;
    /**
     * this rectangle are used with 2 purpose to know when to move next or back
     * on a tab scroll and to draw something and allow the User to know there is
     * more space to move into.
     */
    protected final Rectangle rForward = new Rectangle();
    /**
     * this rectangle are used with 2 purpose to know when to move next or back
     * on a tab scroll and to draw something and allow the User to know there is
     * more space to move into.
     */
    protected final Rectangle rBackward = new Rectangle();
    /**
     * a rectangle that we will draw on the place or places we are able to drop
     * our data (tab)
     */
    private final Rectangle lineRect = new Rectangle();
    /**
     * the color we want to draw our Rectangle to know we can place a Tab we
     * dragged let set it a default as a nice "blue??" we will allow change so
     * the implementation color can be changed where required.
     */
    private Color lineColor = new Color(0, 100, 255);

    /**
     * the supported flavors of data to accept on D&D for this TabPane.
     */
    private DataFlavor[] SupportFlavors = {J_AEON_TAB_FLAVOR};
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Constructors">
    /**
     * creates a new instance of the Aeon tab pane using the Default Logo
     */
    public JAeonTabPane() {
        super();
        initdnd();
    }

    /**
     * creates a new instance of JAeonTabPane using the provided image parsed by
     * parameter.
     *
     * @param todisplay <code>java.awt.Image</code>, the image to display on the
     * pane
     */
    public JAeonTabPane(Image todisplay) {
        super(todisplay);
        initdnd();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="small setters and getters">
    /**
     * set the variable for paint the ghost image shown when we drag a tab.
     *
     * @param flag
     */
    public void setPaintGhost(boolean flag) {
        ShouldDrawGhost = Objects.requireNonNullElse(flag, ShouldDrawGhost);
    }

    /**
     * sets whenever or not when Dropping a tab it should also move the
     * TabComponent
     *
     * @see JTabbedPane#getTabComponentAt(int)
     * @param newSetting
     */
    protected void setMoveTabComponent(boolean newSetting) {
        moveTabComponent = newSetting;
    }

    /**
     * set whenever we want to paint the sides of the tab pane when dragging
     *
     * @param flag the new value if to paint the sides of the tab pane when
     * dragging
     */
    public void setPaintScrollArea(boolean flag) {
        isPaintScrollArea = Objects.requireNonNullElse(flag, isPaintScrollArea);
    }

    /**
     * returns whenever we are painting the ghost image of the tab or not,
     *
     * @return whenever we are painting the ghost image of the tab or not
     */
    public boolean DrawsGhost() {
        return ShouldDrawGhost;
    }

    /**
     * get whenever we want to paint the sides of the tab pane when dragging
     *
     * @return
     */
    public boolean isPaintScrollArea() {
        return isPaintScrollArea;
    }

    /**
     * returns the {@link Color} to use on the highlight position where the tabs
     * can be placed.
     *
     * @return the lineColor
     */
    protected Color getLineColor() {
        return lineColor;
    }

    /**
     * Sets the {@link Color} to use on the highlight position where tabs can be
     * placed. the value if null will be ignored
     *
     * @param newColor the new lineColor to set
     */
    public void setLineColor(Color newColor) {
        lineColor = Objects.requireNonNullElse(newColor, lineColor);
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="TabScrolling">
    /**
     * creates and or updates the rBackward and rForward boxes. then if the Drag
     * is within the bounds of this boxes it triggers Scrolling on this pane if
     * required.
     */
    private void ScrollCheckAndMove(Point glassPt) {
        int buttonsize = 30; //xxx magic number of scroll button size
        int rwh = 20;
        Rectangle r = getTabAreaRectangle();
        if (tabPlacement == TOP || tabPlacement == BOTTOM) {
            rBackward.setBounds(r.x, r.y, rwh, r.height);
            rForward.setBounds(r.x + r.width - rwh - buttonsize, r.y, rwh, r.height);
        } else if (tabPlacement == LEFT || tabPlacement == RIGHT) {
            rBackward.setBounds(r.x, r.y, r.width, rwh);
            rForward.setBounds(r.x, r.y + r.height - rwh - buttonsize, r.width, rwh);
        }
        if (rBackward.contains(glassPt)) {
            scrollTabs(false);
        } else if (rForward.contains(glassPt)) {
            scrollTabs(true);
        }
    }

    /**
     * causes the Tab pane to scroll if the
     * {@link javax.swing.JTabbedPane#SCROLL_TAB_LAYOUT} policy is enabled on
     * this Pane. is required to determine whenever is require to scroll forward
     * of backwards. (via the parameter.)
     *
     * @param forward {@code boolean} to determine whenever is require to scroll
     * forward or backwards.
     */
    protected final void scrollTabs(boolean forward) {
        if (getTabLayoutPolicy() == SCROLL_TAB_LAYOUT) {
            if (forward) {
                TriggerAction("scrollTabsForwardAction");
            } else {
                TriggerAction("scrollTabsBackwardAction");
            }
        }
    }

    /**
     * triggers an action from the action map with the name that is provided on
     * the parameter. WE ASSUME the provided value is NOT null.
     */
    private void TriggerAction(String actionKey) {
        ActionMap map = getActionMap();
        if (map != null) {
            Action action = map.get(actionKey);
            if (action != null && action.isEnabled()) {
                action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null, 0, 0));
            }
        }
    }
    //</editor-fold>

    /**
     * Sets up (if not done already) the GlassPane for this Instance. and
     * Updates the GlassPane if it needs to Draw a Ghost. DrawsGhost returns
     * true. we draw the Ghost Image of the tab. on the Glass Pane. and finally.
     * regardless
     *
     */
    private void updateGlassPane(Component c, Point tabPt) {
        if (getRootPane().getGlassPane() != glassPane) {
            getRootPane().setGlassPane(glassPane);
        }
        if (DrawsGhost()) {
            Rectangle rect = getBoundsAt(dragIndex);
            BufferedImage image = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics g = image.getGraphics();
            c.paint(g);
            if (rect != null) {
                rect.x = rect.x < 0 ? 0 : rect.x;
                rect.y = rect.y < 0 ? 0 : rect.y;
                image = image.getSubimage(rect.x, rect.y, rect.width, rect.height);
                glassPane.setImage(image);
            }
            Point glassPt = SwingUtilities.convertPoint(c, tabPt, glassPane);
            glassPane.setPoint(glassPt);
        }
        glassPane.setVisible(true);
    }

    /**
     * gets a Rectangle that defines the Area where the tabs are drawn and they
     * can be moved and such. ("tabs space")
     * <pre>
     * {@code
     *
     * +---------+---------+----------+--+
     * | tab 1   |  tab2   |  tab3    |  |  <-- this is the rectangle we calcualate (assuming the tabs are placed on top.)
     * +---------+---------+----------+--+
     * | panel                           | <- the rest of the pane.
     * | ...                             |
     * +---------------------------------+
     * }
     * </pre>
     * <strong>NOTE:</strong> the Rectangle Can be in muliple positions. not
     * only at the top. this method take note of this. <br>
     * {@link javax.swing.SwingConstants#TOP}<br>
     * {@link javax.swing.SwingConstants#BOTTOM}<br>
     * {@link javax.swing.SwingConstants#RIGHT}<br>
     * {@link javax.swing.SwingConstants#LEFT}<br>
     * and just get a rectangle from the space where the tab component is
     * show... or so i recall :P
     * <strong>Limitations:</strong> this method depends that the Tabs are
     * uniform (thus if the UI changes the height or width per tab) this method
     * would not be accurate. also if the Layout is not SCROLL_TAB_LAYOUT the
     * calculation might not work correctly and thus thus needs to... calcuale
     * some other way?
     *
     * @return
     */
    protected final Rectangle getTabAreaRectangle() {
        Rectangle tabbedRect = SwingUtilities.convertRectangle(this, getBounds(), glassPane);
        Component comp = getSelectedComponent();
        int idx = 0;
        while (comp == null && idx < getTabCount()) {
            comp = getComponentAt(idx++);
        }
        Rectangle compRect = (comp == null) ? new Rectangle() : comp.getBounds();

        switch (tabPlacement) {
            case BOTTOM:
                tabbedRect.y = tabbedRect.y + compRect.y + compRect.height;
            //fallthought to top code as we also need this same calculation for bottom.
            case TOP:
                tabbedRect.height = tabbedRect.height - compRect.height;
                break;
            case RIGHT:
                tabbedRect.x = tabbedRect.x + compRect.x + compRect.width;
            //fallthought to Left code as we also need this same calculation for Left.
            case LEFT:
                tabbedRect.width = tabbedRect.width - compRect.width;
                break;
            default:
                //well to my understanding Tabbed panes are rectanbles this this should not hit. are rectangle 
                break;
        }
        tabbedRect.grow(2, 2);
        return tabbedRect;
    }

    /**
     * calculates the "Drop tab" highlight rectangle. in the position in between
     * tabs where the drop will put the tab that is being drag
     *
     * @param DropIndex the index where the tab would be dropped
     */
    private Rectangle getTargetRectangle(int DropIndex) {
        if (DropIndex < 0 || dragIndex == DropIndex || DropIndex - dragIndex == 1) {
            return new Rectangle();
        }
        var DrawAt = DropIndex - 1;
        DrawAt = DrawAt < 0 ? 0 : DrawAt;
        Rectangle r = SwingUtilities.convertRectangle(this, getBoundsAt(DrawAt), glassPane);
        boolean isTopOrBottom = tabPlacement == JTabbedPane.TOP || tabPlacement == JTabbedPane.BOTTOM;
        double rx, ry;
        if (DropIndex == 0) {
            rx = isTopOrBottom ? r.x - LINEWIDTH / 2 : r.x;
            ry = isTopOrBottom ? r.y : r.y - LINEWIDTH / 2;
        } else {
            rx = isTopOrBottom ? r.x + r.width - LINEWIDTH : r.x;
            ry = isTopOrBottom ? r.y : r.y + r.height - LINEWIDTH / 2;
        }
        var rWidth = isTopOrBottom ? LINEWIDTH : r.width;
        var rHeight = isTopOrBottom ? r.height : LINEWIDTH;
        r.setRect(rx, ry, rWidth, rHeight);
        return r;
    }

    /**
     * gets the TARGET index for the Position selected. this method is intended
     * to highlight the area where the TAB WOULD land (the in between tabs) thus
     * for example. if we want a tab to land as first tab the "in between" when
     * drag in a tab is anywhere in the first part of the First tab. now if we
     * want to land in the second tab. from a 4th. we drag the tab in the in
     * between of the first and second tab. and this would land it to the second
     * position (index = 1 )
     *
     *
     * @param RelativePoint a Point that represent a position of a dragging or
     * mouse
     * @return the TARGET index where the drag would land.
     */
    private int getTargetTabIndex(Point RelativePoint) {
        Point tabPt = SwingUtilities.convertPoint(glassPane, RelativePoint, JAeonTabPane.this);
        //after checking the couse code for what the next line does. it basically the same we do. 
        //but we need to do a calculation to the rectangle so we can drop "in the inbetween" 
        // and thus if we use tabForCoordinate we would loose performance. 
        //var index = this.getUI().tabForCoordinate(this,tabPt.x,tabPt.y);        
        boolean isTopOrBottom = tabPlacement == JTabbedPane.TOP || tabPlacement == JTabbedPane.BOTTOM;
        Rectangle last = new Rectangle();//initialize here so if no tab is accesible. does not cause a null error later on.
        for (int i = 0; i < getTabCount(); i++) {
            Rectangle r = getBoundsAt(i);//r could be null if the tab is not visible
            if (r == null) {
                break;//break from the loop as moving forward no tab is visible
            }
            last = r;
            if (isTopOrBottom) {
                //move the rectangle to be inbetween tabs
                var moved = r.x - r.width / 2;
                r.setRect(moved, r.y, r.width, r.height);
            } else {
                //move the rectangle to be inbetween tabs
                var moved = r.y - r.height / 2;
                r.setRect(r.x, moved, r.width, r.height);
            }
            if (r.contains(tabPt)) {
                return i;
            }
        }
        //not found. check if we moved beyond the last tab.
        Rectangle r = getBoundsAt(getTabCount() - 1);
        if (r == null) {
            r = last;
        }
        if (isTopOrBottom) {
            r.setRect(r.x + r.width / 2, r.y, r.width, r.height);
        } else {
            r.setRect(r.x, r.y + r.height / 2, r.width, r.height);
        }
        return r.contains(tabPt) ? getTabCount() : -1;
    }

    /**
     * transport or "moves" the tab from one place to another within the pane or
     * another pane of the same type.
     *
     * @param prevIndex the previous index of the tab.
     * @param newIndex the new index of the tab.
     */
    protected final void TransferTab(int prevIndex, int newIndex) {
        if (newIndex < 0 || prevIndex == newIndex) {
            return;
        }
        int tgtindex = prevIndex > newIndex ? newIndex : newIndex - 1;
        Component cmp = getComponentAt(prevIndex);
        Component tabComp = getTabComponentAt(prevIndex);
        String str = getTitleAt(prevIndex);
        Icon icon = getIconAt(prevIndex);
        String tip = getToolTipTextAt(prevIndex);
        boolean flg = isEnabledAt(prevIndex);
        removeTabAt(prevIndex);
        insertTab(str, icon, cmp, tip, tgtindex);
        setEnabledAt(tgtindex, flg);
        //When you drag'n'drop a disabled tab, it finishes enabled and selected.
        if (flg) {
            setSelectedIndex(tgtindex);
        }
        if (moveTabComponent) {
            setTabComponentAt(tgtindex, tabComp);
        }
        PostDropActivity(tgtindex);
    }

    /**
     * this function does nothing for this instance. Child Classes might want to 
     * run post Move Activity to update tab information, or any other activity 
     * might be desired. 
     * @param IndexOfDroppedTab the index where the tab was moved into.
     */
    protected void PostDropActivity(int IndexOfDroppedTab) {}

    /**
     * Initialize the Drag Source (to allow to Drag From **THIS** component, the
     * tabs. into This same component or Other from this class. then Initialize
     * the GlassPane as Drop Target so Drops can be done into this pane. (this
     * last part requires work to support other things to be dropped into.)
     */
    private void initdnd() {
        //listen to "drag acitivty start"
        final DragGestureListener dgl = (var DragGestureEvt) -> {
            //if there are only 1 tab or none. dont allow drag
            if (getTabCount() <= 1) {
                return;
            }
            Point tabPt = DragGestureEvt.getDragOrigin();
            dragIndex = indexAtLocation(tabPt.x, tabPt.y);
            //dont allow drag if the index is invalid or the tab is disabled
            if (dragIndex < 0 || !isEnabledAt(dragIndex)) {
                return;
            }
            //update the glass pane and show the ghost if needed
            updateGlassPane(DragGestureEvt.getComponent(), DragGestureEvt.getDragOrigin());
            try {
                //start the D&D action.
                DragGestureEvt.startDrag(DragSource.DefaultMoveDrop, dndTransferable, dsl);
            } catch (InvalidDnDOperationException idoe) {
            }
        };
        new DragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, dgl);
        //setup the D&D Source and Target. given we D&D tabs within the Same Pane we set both here. 
        var dptarget = new DropTarget(glassPane, DnDConstants.ACTION_COPY_OR_MOVE, new CDropTargetListener(), true);
    }

    //Documentation Pending after this point
    /**
     * this inner class listens for drop actions and some drag events. listens
     * the changes and events trigger when a drop is done or a drop target is
     * pointed.
     */
    protected class CDropTargetListener implements DropTargetListener {

        @Override
        public void dragEnter(DropTargetDragEvent e) {
            if (isDragAcceptable(e)) {
                e.acceptDrag(e.getDropAction());
            } else {
                e.rejectDrag();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void dragExit(DropTargetEvent dte) {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void dropActionChanged(DropTargetDragEvent dtde) {
        }

        @Override
        public void dragOver(final DropTargetDragEvent e) {
            var DragLocation = e.getLocation();
            lineRect.setBounds(getTargetRectangle(getTargetTabIndex(DragLocation)));
            if (DrawsGhost()) {
                glassPane.setPoint(DragLocation);
                glassPane.repaint();
            }
            ScrollCheckAndMove(DragLocation);
        }

        @Override
        public void drop(DropTargetDropEvent e) {
            if (isDropAcceptable(e)) {
                var DragLocation = e.getLocation();
                TransferTab(dragIndex, getTargetTabIndex(DragLocation));
                e.dropComplete(true);
            } else {
                e.dropComplete(false);
            }
            repaint();
        }

        private boolean isDragAcceptable(DropTargetDragEvent e) {
            Transferable t = e.getTransferable();
            if (t == null) {
                return false;
            }
            DataFlavor[] f = e.getCurrentDataFlavors();
            return t.isDataFlavorSupported(f[0]) && dragIndex >= 0;
        }

        private boolean isDropAcceptable(DropTargetDropEvent e) {
            Transferable t = e.getTransferable();
            if (t == null) {
                return false;
            }
            DataFlavor[] f = t.getTransferDataFlavors();
            return t.isDataFlavorSupported(f[0]) && dragIndex >= 0;
        }
    }

    /**
     * this inner class is designed as a JPanel that will be show as a
     * transparent panel (as the "GlassPane") that will paint the "ghost" images
     * for the drag and drop and show a representation of the tab we are
     * dragging also can e used to paint some other transparent effects.
     */
    protected class GhostGlassPane extends JPanel {

        /**
         * the default factor of transparency to draw graphics on this Pane
         */
        private final static float ALPHA_FACTOR = 0.5f;
        private final AlphaComposite composite;
        /**
         * Location where to Paint the TabGhost if not null.
         */
        private Point location = new Point(0, 0);
        private BufferedImage TabGhost = null;

        public GhostGlassPane() {
            super.setOpaque(false);
            composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, ALPHA_FACTOR);
        }

        /**
         * set the image to draw as a ghost
         *
         * @param draggingGhost the image to draw. set to null if nothing is to
         * be drawn.
         */
        public void setImage(BufferedImage draggingGhost) {
            TabGhost = draggingGhost;
        }

        /**
         * sets the new position of the Ghost.
         *
         * @param location the new position to use to draw the ghost.
         */
        public void setPoint(Point location) {
            this.location = location;
        }

        /**
         * this is a Glass Panel. this call is ignored.
         *
         * @param isOpaque the new value for Opaque, this value is ignored
         */
        @Override
        public void setOpaque(boolean isOpaque) {
        }

        /**
         * paint *This* component. this Panel Is unique. and we don't paint the
         * background or content this is a transparent Pane. and thus we don't
         * call Parent method.
         *
         * @param g a instance of Graphics to paint into
         */
        @Override
        public void paintComponent(Graphics g) {
            if (g instanceof Graphics2D g2) {
                g2.setComposite(composite);
                if (dragIndex >= 0) {
                    g2.setPaint(getLineColor());
                    g2.fill(lineRect);
                }
                //debug
//               g2.setPaint(Color.GREEN);
//               g2.fill(rBackward);
//               g2.fill(rForward);
            }
            //if(g==null) return;// this check should not be required as Swing should ensure that does not happen. 
            if (JAeonTabPane.this.isPaintScrollArea()
                    && JAeonTabPane.this.getTabLayoutPolicy() == SCROLL_TAB_LAYOUT) {
                if (tabPlacement == BOTTOM || tabPlacement == TOP) {
                    var centerpoint = getCenterPosition(rBackward, LEFTIMG.getWidth(null), LEFTIMG.getHeight(null));
                    g.drawImage(LEFTIMG, centerpoint.x, centerpoint.y, null);
                    centerpoint = getCenterPosition(rForward, RIGHTIMG.getWidth(null), RIGHTIMG.getHeight(null));
                    g.drawImage(RIGHTIMG, centerpoint.x, centerpoint.y, null);
                } else {
                    g.drawImage(UPIMG, rBackward.x, rBackward.y, null);
                    double initpos = (rForward.getY() + rForward.getHeight()) - DOWNIMG.getHeight(null);
                    g.drawImage(DOWNIMG, rForward.x, (int) initpos, null);
                }
            }
            if (TabGhost != null) {
                double xx = location.getX() - (TabGhost.getWidth() / 2d);
                double yy = location.getY() - (TabGhost.getHeight() / 2d);
                g.drawImage(TabGhost, (int) xx, (int) yy, null);
            }
        }

        /**
         * returns a point where the Inner Rectangle is Center on the Outer.
         *
         * @param Outer the Outer Rectangle
         * @param Width the Width of the image to draw within (hopefully) Outer
         * Rect
         * @param Height the Height of the image to draw within (hopefully)
         * Outer Rect
         * @return a point where the Inner Rectangle is Center on the Outer.
         */
        private Point getCenterPosition(Rectangle Outer, int Width, int Height) {
            var CenteredY = Outer.y;
            if (Outer.getHeight() > Height) {
                CenteredY = (int) (Outer.getCenterY() - Height / 2.0);
            }
            var CenteredX = Outer.x;
            if (Outer.getWidth() > Width) {
                CenteredX = (int) (Outer.getCenterX() - Width / 2.0);
            }
            return new Point(CenteredX, CenteredY);
        }
    }
}
