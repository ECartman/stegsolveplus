/* 
 *  Copyright © 2024 Eduardo Vindas Cordoba. All rights reserved.
 *  
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package com.aeongames.stegsolveplus.ui;

import com.aeongames.edi.utils.error.LoggingHelper;
import com.aeongames.edi.utils.visual.Panels.JAeonTabPane;
import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.swing.SwingUtilities;

/**
 * this class is Designed to Help when a File, link or a supported flavor is
 * Dragged from outside of the application into it. we then check if the data is
 * acceptable and if so we will process as we need to load images, or links. (or
 * any other of supported data)
 *
 * @author Eduardo Vindas
 */
public abstract class DragAndDrop implements DropTargetListener {

    /**
     * the DropTargets that can receive the Drop into. it is an array as we may
     * support multiple drops components
     */
    protected Map<Component, DropTarget> Targets = new HashMap<>();
    /**
     * a list of flavors to ignore for this particular Listener.
     */
    protected List<DataFlavor> FlavorsIgnore = new LinkedList<>();

    public DragAndDrop() {
        //TODO: maybe move this 
        FlavorsIgnore.add(JAeonTabPane.J_AEON_TAB_FLAVOR);
    }

    public boolean RegisterTarget(Component comp) {
        if (Targets.containsKey(comp)) {
            return false;
        }
        var newdrop = new DropTarget(comp, DnDConstants.ACTION_COPY_OR_MOVE, this);
        Targets.put(comp, newdrop);
        return true;
    }

    public DropTarget getDropTargetFor(Component comp) {
        if (Targets.containsKey(comp)) {
            return Targets.get(comp);
        }
        return null;
    }

    public boolean UnRegisterTarget(Component comp) {
        if (Targets.containsKey(comp)) {
            Targets.get(comp).removeDropTargetListener(this);
            Targets.get(comp).removeNotify();
             Targets.get(comp).setActive(false);
            Targets.remove(comp);
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void dragEnter(DropTargetDragEvent dtde) {
        var log = LoggingHelper.getLogger(DragAndDrop.class.getName());
        log.entering(DragAndDrop.class.getName(), "<<Drag and Drop>> --> dragEnter");
        if (ignoreFlavor(dtde)) {
            log.log(Level.INFO, "DragEnter is to be Rejected");
            dtde.rejectDrag();
            return;
        }
        //trigger update to the UI. 
        triggerDragdetected(dtde);
        log.log(Level.INFO, "DragEnter at Location: {0}", dtde.getLocation());
        dtde.acceptDrag(DnDConstants.ACTION_COPY);
        /*
        we will allow to drag anything. but maybe if needed we could deny from the start? 
        if so then we might desire to allow this if else here. 
        if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            dtde.acceptDrag(DnDConstants.ACTION_COPY);
        } else {
            dtde.rejectDrag();
        }
         */
        log.exiting(DragAndDrop.class.getName(), "<<Drag and Drop>> --> dragEnter");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dragOver(DropTargetDragEvent dtde) {
        var log = LoggingHelper.getLogger(DragAndDrop.class.getName());
        log.entering(DragAndDrop.class.getName(), "<<Drag and Drop>> --> dragOver");
        if (ignoreFlavor(dtde)) {
            log.log(Level.INFO, "dragOver is to be Rejected");
            dtde.rejectDrag();
            return;
        }
        log.log(Level.INFO, "dragOver at Location: {0}", dtde.getLocation());
        dtde.acceptDrag(DnDConstants.ACTION_COPY);
        //this one will trigger when a Drag is moved but has not finish (actively dragging)
        //we dont have a action to do here. 
        log.exiting(DragAndDrop.class.getName(), "<<Drag and Drop>> --> dragOver");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
        var log = LoggingHelper.getLogger(DragAndDrop.class.getName());
        if (ignoreFlavor(dtde)) {
            log.log(Level.INFO, "dropActionChanged is to be Rejected");
            dtde.rejectDrag();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void dragExit(DropTargetEvent dte) {
        var log = LoggingHelper.getLogger(DragAndDrop.class.getName());
        log.entering(DragAndDrop.class.getName(), "<<Drag and Drop>> --> dragExit");
        //The D&D went outside of the app Area. and thus we can disenagage. 
        triggerDragExit(dte);
        log.exiting(DragAndDrop.class.getName(), "<<Drag and Drop>> --> dragExit");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void drop(DropTargetDropEvent dtde) {
        var log = LoggingHelper.getLogger(DragAndDrop.class.getName());
        log.entering(DragAndDrop.class.getName(), "<<Drag and Drop>> --> Drop");
        log.log(Level.INFO, "Drop trigger at: {0}", dtde.getLocation());
        //list flavors that the data has. and log them
        var handled = false;
        var flavors = dtde.getCurrentDataFlavors();
        for (var supportedflavor : flavors) {
            log.log(Level.INFO, "DragflavorSupport: {0}", supportedflavor);
            if (supportedflavor.isFlavorJavaFileListType()) {
                dtde.acceptDrop(DnDConstants.ACTION_COPY);
                //process files
                handled = true;
                break;
            } else if (supportedflavor.isFlavorTextType()) {
                dtde.acceptDrop(DnDConstants.ACTION_COPY);
                //process text
                handled = true;
                break;
            }
        }
        if (!handled) {
            //we are unable to handle this type of drop thus lets reject it. 
            dtde.rejectDrop();
        } else {
            dtde.dropComplete(true);
        }

        log.exiting(DragAndDrop.class.getName(), "<<Drag and Drop>> --> Drop");
    }

    private void triggerDragdetected(final DropTargetDragEvent dtde) {
        if (SwingUtilities.isEventDispatchThread()) {
            triggerDragdetectedImp(dtde);
            return;
        }
        try {
            //call this function again but from EDT
            SwingUtilities.invokeAndWait(() -> this.triggerDragdetected(dtde));
        } catch (InterruptedException ex) {
            LoggingHelper.getLogger(DragAndDrop.class.getName()).log(Level.SEVERE, "A call to UI was Interrupted", ex);
        } catch (InvocationTargetException ex) {
            LoggingHelper.getLogger(DragAndDrop.class.getName()).log(Level.SEVERE, "Could not invoke the UI", ex);
        }
    }

    private void triggerDragExit(final DropTargetEvent dte) {
        if (SwingUtilities.isEventDispatchThread()) {
            triggerDragExitImp(dte);
            return;
        }
        try {
            //call this function again but from EDT
            SwingUtilities.invokeAndWait(() -> this.triggerDragExit(dte));
        } catch (InterruptedException ex) {
            LoggingHelper.getLogger(DragAndDrop.class.getName()).log(Level.SEVERE, "A call to UI was Interrupted", ex);
        } catch (InvocationTargetException ex) {
            LoggingHelper.getLogger(DragAndDrop.class.getName()).log(Level.SEVERE, "Could not invoke the UI", ex);
        }
    }

    /**
     * triggered by the Event Dispatch thread when a {@link DropTargetListener#dragEnter(java.awt.dnd.DropTargetDragEvent)
     * }
     * event happens. this method is ensured to be called by the EDT. note this
     * method will block the thread that trigger the Drag and Drop (if that
     * thread is NOT the EDT) and activity takes too long can cause problems on
     * the OS Drag And Drop functionality. thus make sure this method returns as
     * fast as possible. also note. the even is unfiltered. Accepting or
     * rejecting the Event is not required to be performed as this parent class
     * accepts the event.
     *
     * @param dtde the event details data.
     */
    public abstract void triggerDragdetectedImp(DropTargetDragEvent dtde);

    /**
     * triggered by the Event Dispatch thread when a
     * {@link DropTargetListener#dragExit(java.awt.dnd.DropTargetEvent)} event
     * happens. this method is ensured to be called by the EDT. note this method
     * will block the thread that trigger the Drag and Drop (if that thread is
     * NOT the EDT) and activity takes too long can cause problems on the OS
     * Drag And Drop functionality. thus make sure this method returns as fast
     * as possible. also note. the even is unfiltered.
     *
     * @param dte the event details data
     */
    public abstract void triggerDragExitImp(DropTargetEvent dte);

    private boolean ignoreFlavor(DropTargetDragEvent dtde) {
        //we asume is not null
        for (var flavor : dtde.getCurrentDataFlavors()) {
            if (FlavorsIgnore.contains(flavor)) {
                return true;
            }
        }
        return false;
    }

}
