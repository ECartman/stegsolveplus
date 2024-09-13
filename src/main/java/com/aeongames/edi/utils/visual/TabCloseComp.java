/*
 * 
 * Copyright © 2011-2024 Eduardo Vindas. All rights reserved.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

 /*
 * TabCloseComp.java
 */
package com.aeongames.edi.utils.visual;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.util.Objects;
import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JTabbedPane;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 * this is a java UI component that is designed to assist on tabbedPanes. to add
 * the Close Facility on the Tab UI. this component replace the Default Tab
 * (here we refer to tabs as the slip not the Panel display on the tabbed pane)
 * <pre>
 * {@code
 * (using this)  (normal tabs)
 * +---------+---------+----------+--+
 * | tab 1(X)|  tab2   |  tab3    |  |  <-- this is the rectangle we draw (assuming the tabs are placed on top.)
 * +---------+---------+----------+--+
 * | panel                           | <- the rest of the pane.
 * | ...                             |
 * +---------------------------------+
 * }
 * </pre>
 *
 * @author Eduardo Vindas C
 */
public class TabCloseComp extends javax.swing.JPanel {

    protected float Xgap = 2f;
    protected float Ygap = 3f;
    protected Icon TabCIcon = null;
    protected JTabbedPane mainpane;
    protected Color XColor = Color.lightGray;

    /**
     * default constructor here to ensure it cannot be called by reflections
     */
    private TabCloseComp() {
        throw new IllegalAccessError("Ilegal Call To default constructor; Reflections?");
    }

    /**
     * Creates new form TabCloseComp this requires a JtabbedPane to be parsed by
     * a parameter.
     */
    public TabCloseComp(JTabbedPane pane) {
        mainpane = Objects.requireNonNull(pane, "TabbedPane is null");
        initComponents();
        checkCondition();
    }

    public TabCloseComp(JTabbedPane pane, Color X_Color) {
        mainpane = Objects.requireNonNull(pane, "TabbedPane is null");
        if (X_Color != null) {
            XColor = X_Color;
        }
        initComponents();
        checkCondition();
    }

    public TabCloseComp(JTabbedPane pane, Icon icon) {
        mainpane = Objects.requireNonNull(pane, "TabbedPane is null");
        TabCIcon = icon;
        initComponents();
        checkCondition();
    }

    public TabCloseComp(JTabbedPane pane, Color X_Color, Icon icon) {
        if (pane == null) {
            throw new NullPointerException("TabbedPane is null");
        }
        mainpane = pane;
        if (X_Color != null) {
            XColor = X_Color;
        }
        TabCIcon = icon;
        initComponents();
        checkCondition();
    }

    public void setIcon(Icon icon) {
        TabCIcon = icon;
        Iconlb.setIcon(TabCIcon);
        checkCondition();
    }

    private void checkCondition() {
        Iconlb.setVisible(TabCIcon != null);
    }

    /**
     * gets the TabIcon if any. 
     * @return the Icon if any otherwise returns null.
     */
    public final Icon getIcon() {
        return TabCIcon;
    }

    /**
     * changes the Color of the X for a Tab component. 
     * if null is provided the call is ignored.
     * @param newXColor the new Color to setup, Null is OK but will be ignored.
     */
    public void setXColor(Color newXColor) {
        XColor = Objects.requireNonNullElse(newXColor,XColor);
    }

    /**
     * gets the Color that the X to render (to close the tab) will be render. 
     * by Default {@link Color#lightGray} when idle.
     * @return the Color used to render the X when not mouse over
     */
    public final Color getColorX() {
        return XColor;
    }

    public void changeTabPane(JTabbedPane newPane) {
        mainpane = newPane;
    }

    public void setTitle(String text) {
        LbTittle.setText(text.strip());
    }

    public final String getTitle() {
        return LbTittle.getText();
    }

    public final void Update() {
        var newIndex = mainpane.indexOfTabComponent(this);
        setTitle(mainpane.getTitleAt(newIndex));
        setIcon(mainpane.getIconAt(newIndex));
    }

    public final void Update(int newTabIndex) {
        if (mainpane.getTabComponentAt(newTabIndex) != this) {
            throw new RuntimeException("The provided index does not belong to this instance");
        }
        setTitle(mainpane.getTitleAt(newTabIndex));
        setIcon(mainpane.getIconAt(newTabIndex));
    }

    /**
     * Closes the Tab if and only if the tab at the provided index is <Strong>
     * the tab related to this component</Strong>
     *
     * @param TabIndex the index of the tab to close
     * @return true if we removed the tab. false if the tab is not related to
     * this close component.
     */
    protected boolean Close(int TabIndex) {
        if (mainpane.getTabComponentAt(TabIndex) != this) {
            return false;
        }
        mainpane.removeTabAt(TabIndex);
        return true;
    }

    /**
     * Removes The tab Component if is registered otherwise returns false;
     * @return True if the tab was located and removed. false otherwise
     */
    protected final boolean Close() {
        int myTabComponent = mainpane.indexOfTabComponent(this);
        return myTabComponent != -1 ? Close(myTabComponent) : false;
    }

    private class TabButton extends JButton {

        public TabButton() {
            setToolTipText("close this tab");
            setUI(new BasicButtonUI());
            //Make it transparent
            setContentAreaFilled(false);
            //No need to be focusable
            setFocusable(false);
//            setBorder(BorderFactory.createEtchedBorder());
            setBorderPainted(false);
            //Making nice rollover effect
            //we use the same listener for all buttons
            addMouseListener(buttonMouseListener);
            setRolloverEnabled(true);
            //Close the proper tab by clicking the button
            addActionListener(e -> Close());
        }

        //paint the cross
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//            shift the image for pressed buttons
            if (getModel().isPressed()) {
                g2.setStroke(new BasicStroke(2));
            } else {
                g2.setStroke(new BasicStroke(3));
            }
            g2.setColor(XColor);
            if (getModel().isRollover()) {
                g2.setColor(Color.RED);
            }
            float Width = getWidth() - Xgap;
            float Height = getHeight() - Ygap;
            g2.draw(new Line2D.Float(Xgap, Ygap, Width, Height));
            g2.draw(new Line2D.Float(Width, Ygap, Xgap, Height));
            g2.dispose();

        }
    }
    private final static MouseListener buttonMouseListener = new MouseAdapter() {

        @Override
        public void mouseEntered(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton button) {
                button.setBorderPainted(true);
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton button) {
                button.setBorderPainted(false);
            }
        }
    };

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        Iconlb = new javax.swing.JLabel();
        LbTittle = new javax.swing.JLabel();
        jButton1 = new TabButton();

        setOpaque(false);

        Iconlb.setIcon(TabCIcon);

        jButton1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jButton1.setFocusable(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(Iconlb, javax.swing.GroupLayout.DEFAULT_SIZE, 15, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(LbTittle, javax.swing.GroupLayout.DEFAULT_SIZE, 15, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(Iconlb, javax.swing.GroupLayout.DEFAULT_SIZE, 14, Short.MAX_VALUE)
            .addComponent(LbTittle, javax.swing.GroupLayout.DEFAULT_SIZE, 14, Short.MAX_VALUE)
            .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 14, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel Iconlb;
    private javax.swing.JLabel LbTittle;
    private javax.swing.JButton jButton1;
    // End of variables declaration//GEN-END:variables
}
