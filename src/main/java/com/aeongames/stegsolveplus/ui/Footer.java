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

import com.aeongames.edi.utils.pojo.BooleanPropertyPojo;
import com.aeongames.edi.utils.pojo.PropertyPojo;
import com.aeongames.edi.utils.text.LabelText;
import com.aeongames.edi.utils.visual.link.BaseBinder;
import com.aeongames.edi.utils.visual.link.JLabelComponentBind;
import com.aeongames.edi.utils.visual.link.JprogressComponentBind;
import com.aeongames.edi.utils.visual.link.MCBoolProbarIndeterminate;
import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

/**
 *
 * @author Eduardo Vindas
 */
public class Footer extends javax.swing.JPanel {

    private final class StatusPojo {

        public final ArrayList<BaseBinder<?, ? extends JComponent>> Bindings;
        private final BooleanPropertyPojo Indeterminated = new BooleanPropertyPojo();
        private final PropertyPojo<Integer> Progressamt = new PropertyPojo<>(0);
        private final PropertyPojo<String> StatusBarText = PropertyPojo.newStringPojo();

        public StatusPojo() {
            Bindings = new ArrayList<>(3);
            StatusBarText.setValue("Welcome To StegnoSolve+");
        }

        //<editor-fold defaultstate="collapsed" desc="Binds">
        /**
         * bind Frame Components to be disabled or enabled if the Frame is busy
         * or not.
         *
         * @param tobind the progress bar to bind.
         * @return the binding if desired to be used or tracked by caller.
         */
        MCBoolProbarIndeterminate bindIndeterminateProgressBar(JProgressBar tobind) {
            var statusBind = new MCBoolProbarIndeterminate(tobind, Indeterminated);
            Bindings.add(statusBind);
            return statusBind;
        }

        JLabelComponentBind BindStatusBar(JLabel label) {
            var lbind = new JLabelComponentBind(label, StatusBarText);
            Bindings.add(lbind);
            return lbind;
        }

        JprogressComponentBind BindProgressBar(JProgressBar bar) {
            var lbind = new JprogressComponentBind(bar, Progressamt);
            Bindings.add(lbind);
            return lbind;
        }
        //</editor-fold>
    }

    private class RestrictedLabel extends JLabel {

        public RestrictedLabel() {
            addComponentListener(new java.awt.event.ComponentAdapter() {
                @Override
                public void componentResized(java.awt.event.ComponentEvent evt) {
                    resetText();
                }
            });
        }

        String longText;

        public void resetText() {
            super.setText(LabelText.getTrimmedtoComponentsize(longText, this, 654));
        }

        @Override
        public void setText(String text) {
            longText = text;
            super.setText(LabelText.getTrimmedtoComponentsize(text, this, 654));
        }

        @Override
        public String getText() {
            return longText;
        }

    }

    private final StatusPojo myBidings = new StatusPojo();

    public enum ProgressState {
        Idle,
        Indetermine,
        Progressing,
        Complete,
        INVALID
    }

    /**
     * Creates new form Footer
     */
    public Footer() {
        initComponents();
        myBidings.BindStatusBar(txtFooter);
        myBidings.bindIndeterminateProgressBar(AppProgressBar);
        myBidings.BindProgressBar(AppProgressBar);
    }

    public ProgressState SetProgressIndeterminate() {
        myBidings.Indeterminated.setValue(true);
        return ProgressState.Indetermine;
    }

    public ProgressState SetProgress(int CurrentProgress) {
        if (CurrentProgress >= 0 && CurrentProgress <= 100) {
            myBidings.Indeterminated.setValue(false);
            myBidings.Progressamt.setValue(CurrentProgress);
            ProgressState currentState;
            currentState = switch (myBidings.Progressamt.getValue()) {
                case 0 ->
                    ProgressState.Idle;
                case 100 ->
                    ProgressState.Complete;
                default /*whatever in between 0-100*/ ->
                    ProgressState.Progressing;
            };
            return currentState;
        }
        if (CurrentProgress == -1) {
            return SetProgressIndeterminate();
        }
        return ProgressState.INVALID;
    }

    public void setFooterText(String newText) {
        txtFooter.setToolTipText(newText);
        myBidings.StatusBarText.setValue(newText);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        AppProgressBar = new javax.swing.JProgressBar();
        txtFooter = new RestrictedLabel();

        txtFooter.setText(myBidings.StatusBarText.getValue());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(txtFooter, javax.swing.GroupLayout.DEFAULT_SIZE, 654, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(AppProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(AppProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(txtFooter, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JProgressBar AppProgressBar;
    private javax.swing.JLabel txtFooter;
    // End of variables declaration//GEN-END:variables
}
