/*
 *  Copyright © 2024 Eduardo Vindas. All rights reserved.
 *  
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package com.aeongames.edi.utils.visual.Panels;

import com.aeongames.edi.utils.error.ErrorData;
import com.aeongames.edi.utils.visual.SimpleCloseButton;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Objects;

/**
 *
 * @author Eduardo V<cartman AeonGames.com>
 */
public class ErrorGlassPane extends javax.swing.JPanel {

    private ErrorData errdata;

    /**
     * Creates new form ErrorGlassPane
     */
    public ErrorGlassPane(ErrorData err/*,CloseListener listener*/) {
        errdata = Objects.requireNonNull(err, "the Error Data cannot be Null");
        initComponents();

        addMouseListener(new MouseAdapter() {
        });
        addMouseMotionListener(new MouseMotionAdapter() {
        });
        addKeyListener(new KeyAdapter() {
        });
        setVisible(false);
    }

    public void ChangeError(ErrorData err/*,CloseListener listener*/) {
        errdata = Objects.requireNonNull(err, "the Error Data cannot be Null");
        update();
    }

    private void update() {
        //errdata cannot... should not be null at this point. 
        LbErrorTittle.setText(errdata.getErrorTittle());
        LbErrorTittle.setToolTipText(errdata.getErrorTittle());
        txtmessage.setText(errdata.getErrorMessage());
        txtstack.setText(errdata.getErrorStack());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        translucentpanel1 = new com.aeongames.edi.utils.visual.translucentpanel();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(30, 0), new java.awt.Dimension(30, 0), new java.awt.Dimension(32767, 0));
        jPanel1 = new javax.swing.JPanel();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 60), new java.awt.Dimension(0, 60), new java.awt.Dimension(0, 32767));
        translucentpanel2 = new com.aeongames.edi.utils.visual.translucentpanel();
        jPanel2 = new javax.swing.JPanel();
        LbErrorTittle = new javax.swing.JLabel();
        closebt = new SimpleCloseButton();
        jLabel2 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        pstack = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtstack = new javax.swing.JTextArea();
        jScrollPane3 = new javax.swing.JScrollPane();
        txtmessage = new com.aeongames.edi.utils.visual.TranslucentTextPane();
        filler4 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 60), new java.awt.Dimension(0, 60), new java.awt.Dimension(0, 32767));
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(30, 0), new java.awt.Dimension(30, 0), new java.awt.Dimension(32767, 0));

        setFocusCycleRoot(true);
        setOpaque(false);
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                formComponentShown(evt);
            }
        });

        translucentpanel1.setBackground(new java.awt.Color(0, 0, 0));
        translucentpanel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                translucentpanel1MouseClicked(evt);
            }
        });
        translucentpanel1.setLayout(new javax.swing.BoxLayout(translucentpanel1, javax.swing.BoxLayout.LINE_AXIS));
        translucentpanel1.add(filler1);

        jPanel1.setOpaque(false);
        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.PAGE_AXIS));
        jPanel1.add(filler3);

        translucentpanel2.setArcHeight(17);
        translucentpanel2.setArcWidth(17);
        translucentpanel2.addMouseListener(new MouseAdapter() {
        });

        jPanel2.setOpaque(false);

        LbErrorTittle.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        LbErrorTittle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LbErrorTittle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/aeongames/hp/descriptor/Resources/Images/no.png"))); // NOI18N
        LbErrorTittle.setText("Error");
        LbErrorTittle.setToolTipText("Error on Execution");
        if(errdata!=null&&errdata.getErrorTittle()!=null){
            LbErrorTittle.setText(errdata.getErrorTittle());
            LbErrorTittle.setToolTipText(errdata.getErrorTittle());
        }

        closebt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closebtActionPerformed(evt);
            }
        });
        closebt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                closebtKeyTyped(evt);
            }
        });

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/aeongames/hp/descriptor/Resources/Images/error.png"))); // NOI18N

        jPanel5.setOpaque(false);
        jPanel5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPanel5ShowStack(evt);
            }
        });

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/aeongames/hp/descriptor/Resources/Images/rightarrow.png"))); // NOI18N
        jLabel1.setText("Click Here To Show Error Stack");
        jLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel1ShowStack(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addContainerGap(243, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1)
        );

        pstack.setOpaque(false);

        txtstack.setEditable(false);
        txtstack.setColumns(20);
        txtstack.setRows(5);
        txtstack.setText(errdata!=null? errdata.getErrorStack():"");
        //txtstack.putClientProperty(LafWidget.TEXT_EDIT_CONTEXT_MENU, Boolean.TRUE);
        jScrollPane2.setViewportView(txtstack);

        javax.swing.GroupLayout pstackLayout = new javax.swing.GroupLayout(pstack);
        pstack.setLayout(pstackLayout);
        pstackLayout.setHorizontalGroup(
            pstackLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2)
        );
        pstackLayout.setVerticalGroup(
            pstackLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE)
        );

        txtmessage.setEditable(false);
        txtmessage.setContentType("text/html"); // NOI18N
        txtmessage.setText(errdata!=null? errdata.getErrorMessage(): "Error");
        jScrollPane3.setViewportView(txtmessage);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pstack, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(LbErrorTittle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(closebt, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane3)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(LbErrorTittle, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(closebt, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(pstack, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pstack.setVisible(false);

        javax.swing.GroupLayout translucentpanel2Layout = new javax.swing.GroupLayout(translucentpanel2);
        translucentpanel2.setLayout(translucentpanel2Layout);
        translucentpanel2Layout.setHorizontalGroup(
            translucentpanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        translucentpanel2Layout.setVerticalGroup(
            translucentpanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jPanel1.add(translucentpanel2);
        jPanel1.add(filler4);

        translucentpanel1.add(jPanel1);
        translucentpanel1.add(filler2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(translucentpanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 483, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(translucentpanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void closebtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closebtActionPerformed
        setVisible(false);
    }//GEN-LAST:event_closebtActionPerformed

    private void jLabel1ShowStack(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel1ShowStack
        if (evt.getClickCount() >= 1) {
            pstack.setVisible(!pstack.isVisible());
        }
        evt.consume();
    }//GEN-LAST:event_jLabel1ShowStack

    private void jPanel5ShowStack(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel5ShowStack
        if (evt.getClickCount() >= 1) {
            pstack.setVisible(!pstack.isVisible());
        }
        evt.consume();
    }//GEN-LAST:event_jPanel5ShowStack

    private void translucentpanel1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_translucentpanel1MouseClicked
        if (evt.getButton() == MouseEvent.BUTTON1 && evt.getClickCount() >= 1) {
            setVisible(false);
            evt.consume();
        }
    }//GEN-LAST:event_translucentpanel1MouseClicked

    private void closebtKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_closebtKeyTyped
        if (evt.getKeyChar() == KeyEvent.VK_ENTER) {
            setVisible(false);
            evt.consume();
        }
    }//GEN-LAST:event_closebtKeyTyped

    private void formComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
        closebt.requestFocusInWindow();
    }//GEN-LAST:event_formComponentShown
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel LbErrorTittle;
    private javax.swing.JButton closebt;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler2;
    private javax.swing.Box.Filler filler3;
    private javax.swing.Box.Filler filler4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JPanel pstack;
    private com.aeongames.edi.utils.visual.translucentpanel translucentpanel1;
    private com.aeongames.edi.utils.visual.translucentpanel translucentpanel2;
    private com.aeongames.edi.utils.visual.TranslucentTextPane txtmessage;
    private javax.swing.JTextArea txtstack;
    // End of variables declaration//GEN-END:variables

}
