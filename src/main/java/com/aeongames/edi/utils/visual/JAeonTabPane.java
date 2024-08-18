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

    // <editor-fold defaultstate="collapsed" desc="variables">
    /**
     * a transparent panel that is use to paint transparent components and
     * images.
     */
    private final GhostGlassPane glassPane = new GhostGlassPane();
    /**
     * the flavor of the data on DND
     */
    private final DataFlavor FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, DATA_FLAVOR_NAME);
    /**
     * name of this type of data from this class
     */
    private static final String DATA_FLAVOR_NAME = "JAeonTabPane";
    /**
     * the images to show when we want to drag in order to inform...
     */
    private static final Image arrows[] = new Image[]{
        java.awt.Toolkit.getDefaultToolkit().getImage(JAeonTabPane.class.getResource("/com/aeongames/stegsolveplus/ui/downarrow.png")),
        java.awt.Toolkit.getDefaultToolkit().getImage(JAeonTabPane.class.getResource("/com/aeongames/stegsolveplus/ui/leftarrow.png")),
        java.awt.Toolkit.getDefaultToolkit().getImage(JAeonTabPane.class.getResource("/com/aeongames/stegsolveplus/ui/rightarrow.png")),
        java.awt.Toolkit.getDefaultToolkit().getImage(JAeonTabPane.class.getResource("/com/aeongames/stegsolveplus/ui/uparrow.png"))
    };
    /**
     * the index from we will drag the tab.
     */
    private int dragIndex = -1;
    /**
     * determines whenever is fine to paint the scroll area components
     */
    private boolean isPaintScrollArea = true;
    /**
     * determines if we should paint the ghost of the component we are dragging
     */
    private boolean hasGhost = true;
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
     * the width we will draw the line for the user to know where to place the
     * tab.
     */
    private static final int LINEWIDTH = 3;
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
        hasGhost = flag;
    }

    /**
     * set whenever we want to paint the sides of the tab pane when dragging
     *
     * @param flag the new value if to paint the sides of the tab pane when
     * dragging
     */
    public void setPaintScrollArea(boolean flag) {
        isPaintScrollArea = flag;
    }

    /**
     * returns whenever we are painting the ghost image of the tab or not,
     *
     * @return whenever we are painting the ghost image of the tab or not
     */
    public boolean hasGhost() {
        return hasGhost;
    }

    /**
     * get whenever we want to paint the sides of the tab pane when dragging
     *
     * @return
     */
    public boolean isPaintScrollArea() {
        return isPaintScrollArea;
    }
    // </editor-fold>

    /**
     * causes the Tab pane to scroll if the
     * {@link javax.swing.JTabbedPane#SCROLL_TAB_LAYOUT} policy is enabled on
     * this Pane. is required to determine whenever is require to scroll forward
     * of backwards. (via the parameter.)
     *
     * @param forward {@code boolean} to determine whenever is require to scroll
     * forward or backwards.
     */
    protected final void scrolldirection(boolean forward) {
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
     * the parameter.
     * WE ASSUME the provided value is NOT null.
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
    
    /**
     * initializes the Drag And Drop Component.
     *
     */
    private void initdnd() {
        final DragSourceListener dsl = new DragSourceListener() {

            /**
             * {@inheritDoc}
             *
             * for THIS particular Listener we just update the Cursor. when it
             * enter a Drop-able point
             */
            @Override
            public void dragEnter(DragSourceDragEvent e) {
                e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
            }

            /**
             * {@inheritDoc}
             *
             * if Drag is finish or Exit the Zone of interest hide the highlight
             * Rectangle. and of course repaint the glass pane
             */
            @Override
            public void dragExit(DragSourceEvent e) {
                e.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
                lineRect.setRect(0, 0, 0, 0);
                glassPane.setPoint(new Point(-1000, -1000));
                glassPane.repaint();
            }

            /**
             * {@inheritDoc}
             *
             */
            @Override
            public void dragOver(DragSourceDragEvent e) {
                var DragLocation = e.getLocation();
                //relativisize the point from the screen to the GlassPane
                SwingUtilities.convertPointFromScreen(DragLocation, glassPane);
                int targetIdx = getTargetTabIndex(DragLocation);
                //if(getTabAreaBounds().contains(tabPt) && targetIdx>=0 &&
                if (getTabAreaBounds().contains(DragLocation) && targetIdx >= 0
                        && targetIdx != dragIndex && targetIdx != dragIndex + 1) {
                    e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
                    glassPane.setCursor(DragSource.DefaultMoveDrop);
                } else {
                    e.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
                    glassPane.setCursor(DragSource.DefaultMoveNoDrop);
                }
            }

            @Override
            public void dragDropEnd(DragSourceDropEvent e) {
                lineRect.setRect(0, 0, 0, 0);
                dragIndex = -1;
                glassPane.setVisible(false);
                if (hasGhost()) {
                    glassPane.setVisible(false);
                    glassPane.setImage(null);
                }
            }

            @Override
            public void dropActionChanged(DragSourceDragEvent e) {
            }
        };
        final Transferable t = new Transferable() {

            @Override
            public Object getTransferData(DataFlavor flavor) {
                return JAeonTabPane.this;
            }

            @Override
            public DataFlavor[] getTransferDataFlavors() {
                DataFlavor[] f = new DataFlavor[1];
                f[0] = FLAVOR;
                return f;
            }

            @Override
            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return flavor.getHumanPresentableName().equals(DATA_FLAVOR_NAME);
            }
        };
        final DragGestureListener dgl = (DragGestureEvent e) -> {
            if (getTabCount() <= 1) {
                return;
            }
            Point tabPt = e.getDragOrigin();
            dragIndex = indexAtLocation(tabPt.x, tabPt.y);
            //"disabled tab problem".
            if (dragIndex < 0 || !isEnabledAt(dragIndex)) {
                return;
            }
            initGlassPane(e.getComponent(), e.getDragOrigin());
            try {
                e.startDrag(DragSource.DefaultMoveDrop, t, dsl);
            } catch (InvalidDnDOperationException idoe) {

            }
        };

        new DropTarget(glassPane, DnDConstants.ACTION_COPY_OR_MOVE, new CDropTargetListener(), true);
        new DragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, dgl);
    }

    private void initGlassPane(Component c, Point tabPt) {
        if (getRootPane().getGlassPane() != glassPane) {
            getRootPane().setGlassPane(glassPane);
        }
        if (hasGhost()) {
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
        }
        Point glassPt = SwingUtilities.convertPoint(c, tabPt, glassPane);
        glassPane.setPoint(glassPt);
        glassPane.setVisible(true);
    }

    /**
     * creates the rectangles where they are required in order to scroll the
     * tabs. also they will calculate if we require to scroll the component
     * backwards or forward.
     */
    private void CompAndScroll(Point glassPt) {
        int buttonsize = 30; //xxx magic number of scroll button size
        int rwh = 20;
        Rectangle r = getTabAreaBounds();
        if (tabPlacement == TOP || tabPlacement == BOTTOM) {
            rBackward.setBounds(r.x, r.y, rwh, r.height);
            rForward.setBounds(r.x + r.width - rwh - buttonsize, r.y, rwh, r.height);
        } else if (tabPlacement == LEFT || tabPlacement == RIGHT) {
            rBackward.setBounds(r.x, r.y, r.width, rwh);
            rForward.setBounds(r.x, r.y + r.height - rwh - buttonsize, r.width, rwh);
        }
        if (rBackward.contains(glassPt)) {
            scrolldirection(false);
        } else if (rForward.contains(glassPt)) {
            scrolldirection(true);
        }
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
    protected final Rectangle getTabAreaBounds() {
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
     * returns the color we use to draw the line where the tabs can be placed.
     *
     * @return the lineColor
     */
    protected Color getLineColor() {
        return lineColor;
    }

    /**
     * Sets the color to highlight where tabs can be placed. the value if null
     * will be ignored
     *
     * @param newColor the new lineColor to set
     */
    public void setLineColor(Color newColor) {
        lineColor = Objects.requireNonNullElse(newColor, lineColor);
    }

    //depth analizis require here.
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

        @Override
        public void dragExit(DropTargetEvent e) {
        }

        @Override
        public void dropActionChanged(DropTargetDragEvent e) {
        }

        @Override
        public void dragOver(final DropTargetDragEvent e) {
            var DragLocation = e.getLocation();
            if (getTabPlacement() == JTabbedPane.TOP || getTabPlacement() == JTabbedPane.BOTTOM) {
                initTargetLeftRightLine(getTargetTabIndex(DragLocation));
            } else {
                initTargetTopBottomLine(getTargetTabIndex(DragLocation));
            }
            Point pt_ = new Point();
            if (hasGhost()) {
                glassPane.setPoint(DragLocation);
            }
            if (!pt_.equals(DragLocation)) {
                glassPane.repaint();
            }
            pt_ = DragLocation;
            CompAndScroll(DragLocation);
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
        boolean isTopOrBottom = getTabPlacement() == JTabbedPane.TOP || getTabPlacement() == JTabbedPane.BOTTOM;
        Rectangle last = null;
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
     * @param prev the previous index of the tab.
     * @param next the new index of the tab.
     */
    protected void TransferTab(int prev, int next) {
        if (next < 0 || prev == next) {
            return;
        }
        Component cmp = getComponentAt(prev);
        Component tab = getTabComponentAt(prev);
        String str = getTitleAt(prev);
        Icon icon = getIconAt(prev);
        String tip = getToolTipTextAt(prev);
        boolean flg = isEnabledAt(prev);
        int tgtindex = prev > next ? next : next - 1;
        remove(prev);
        insertTab(str, icon, cmp, tip, tgtindex);
        setEnabledAt(tgtindex, flg);
        //When you drag'n'drop a disabled tab, it finishes enabled and selected.
        if (flg) {
            setSelectedIndex(tgtindex);
        }
        setTabComponentAt(tgtindex, tab);
    }

    private void initTargetLeftRightLine(int next) {
        if (next < 0 || dragIndex == next || next - dragIndex == 1) {
            lineRect.setRect(0, 0, 0, 0);
        } else if (next == 0) {
            Rectangle r = SwingUtilities.convertRectangle(this, getBoundsAt(0), glassPane);
            lineRect.setRect(r.x - LINEWIDTH / 2, r.y, LINEWIDTH, r.height);
        } else {
            Rectangle r = SwingUtilities.convertRectangle(this, getBoundsAt(next - 1), glassPane);
            lineRect.setRect(r.x + r.width - LINEWIDTH / 2, r.y, LINEWIDTH, r.height);
        }
    }

    private void initTargetTopBottomLine(int next) {
        if (next < 0 || dragIndex == next || next - dragIndex == 1) {
            lineRect.setRect(0, 0, 0, 0);
        } else if (next == 0) {
            Rectangle r = SwingUtilities.convertRectangle(this, getBoundsAt(0), glassPane);
            lineRect.setRect(r.x, r.y - LINEWIDTH / 2, r.width, LINEWIDTH);
        } else {
            Rectangle r = SwingUtilities.convertRectangle(this, getBoundsAt(next - 1), glassPane);
            lineRect.setRect(r.x, r.y + r.height - LINEWIDTH / 2, r.width, LINEWIDTH);
        }
    }

    /**
     * this inner class is designed as a JPanel that will be show as a
     * transparent panel (as the "GlassPane") that will paint the "ghost" images
     * for the drag and drop and show a representation of the tab we are
     * dragging also can e used to paint some other transparent effects.
     */
    protected class GhostGlassPane extends JPanel {

        private final AlphaComposite composite;
        private Point location = new Point(0, 0);
        private BufferedImage TabGhost = null;
        private final float transparencyfactor = 0.5f;

        public GhostGlassPane() {
            setOpaque(false);
            composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparencyfactor);
        }

        public void setImage(BufferedImage draggingGhost) {
            TabGhost = draggingGhost;
        }

        public void setPoint(Point location) {
            this.location = location;
        }

        @Override
        public void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setComposite(composite);
            if (isPaintScrollArea() && getTabLayoutPolicy() == SCROLL_TAB_LAYOUT) {
                if (tabPlacement == BOTTOM || tabPlacement == TOP) {
                    g2.drawImage(arrows[1], rBackward.x, rBackward.y, null);
                    double initpos = (rForward.getX() + rForward.getWidth()) - arrows[2].getWidth(null);
                    g2.drawImage(arrows[2], (int) initpos, rForward.y, null);
                } else {
                    g2.drawImage(arrows[3], rBackward.x, rBackward.y, null);
                    double initpos = (rForward.getY() + rForward.getHeight()) - arrows[0].getHeight(null);
                    g2.drawImage(arrows[0], rForward.x, (int) initpos, null);

                }
                //debug
//                g2.setPaint(Color.GREEN);
//                g2.fill(rBackward);
//                g2.fill(rForward);
            }
            if (TabGhost != null) {
                double xx = location.getX() - (TabGhost.getWidth() / 2d);
                double yy = location.getY() - (TabGhost.getHeight() / 2d);
                g2.drawImage(TabGhost, (int) xx, (int) yy, null);
            }
            if (dragIndex >= 0) {
                g2.setPaint(getLineColor());
                g2.fill(lineRect);
            }
        }
    }
}
