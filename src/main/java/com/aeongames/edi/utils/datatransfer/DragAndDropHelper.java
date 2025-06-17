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
package com.aeongames.edi.utils.datatransfer;

import com.aeongames.edi.utils.error.LoggingHelper;
import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.logging.Level;
import javax.swing.SwingUtilities;

/**
 *
 * this class defines a Listening service that will process Drag and Drop
 * Request for a a set of components. processing and orchestrating the request
 * from this class into specific data handlers.
 *
 * @author Eduardo Vindas
 * @version 1.7
 */
public class DragAndDropHelper implements DropTargetListener {

    //<editor-fold defaultstate="collapsed" desc="Properties">
    /**
     * the Logger name for This class
     */
    private static final String LOGGERNAME = "DataTransferLogger";
    /**
     * Drop Targets that have been registered by this Class.
     */
    protected final LinkedList<DropTarget> Targets;
    /**
     * a list of flavors to ignore for this particular Listener. this list is
     * intended for this {@Link DropTargetListener} to ignore Drop events
     */
    protected final LinkedHashSet<DataFlavor> FlavorsIgnore;
    /**
     * a list of unique values. that contains the Flavors handlers that are
     * instances that can handle specific flavor(s) of data. from the clipboard.
     * <br>
     * when a change on the Clipboard is to be handled the list (in order) and
     * check for each handler if they are fit to handle the flavor.
     * <br>
     * this process runs in "first come first serve". meaning. that if there are
     * multiple handlers for the same flavor the one that has registered with
     * hight priority(first encountered on the list) and is allocated first will
     * process the data.
     */
    private final LinkedHashSet<FlavorHandler> FlavorsListPriority;
    /**
     * a list of unique values. that contains all the dndEventListener that want
     * to listen for Events related to DnD to update the UI.
     */
    private final LinkedHashSet<DragDropEventListener> dndListeners;
    /**
     * a mapping for the {@code FlavorHandler} that wrap a
     * {@code FlavorProcessor} this is required for ease of adding or removing
     * handlers
     */
    private final HashMap<FlavorProcessor, FlavorHandler> MapProcessors;

    //</editor-fold>
    /**
     * create a new instance of this Class. receive multiple Flavors to be
     * ignored
     *
     * @param ignoreFlavors a {@code Params} array of DataFlavors that are
     * desired to be ignored.
     */
    public DragAndDropHelper(DataFlavor... ignoreFlavors) {
        Targets = new LinkedList<>();
        dndListeners = new LinkedHashSet<>();
        FlavorsIgnore = new LinkedHashSet<>();
        FlavorsListPriority = new LinkedHashSet<>();
        MapProcessors = new HashMap<>();
        if (ignoreFlavors != null && ignoreFlavors.length > 0) {
            Collections.addAll(FlavorsIgnore, ignoreFlavors);
        }
        // if no flavor is provided assume its A OK. just no flavors are to be ignored.
    }

    //<editor-fold defaultstate="collapsed" desc="Add/Remove Handlers">
    /**
     * Add a FlavorHandler to the list of handlers at the end of the list if no
     * prioritized otherwise add it at the start. NOTE: the order of the
     * handlers is important as it will be used to provide priority to the
     * handlers. the first handler in the list will be the first one to be
     * called. and so on.
     *
     * @param handler the FlavorProcessor to add
     * @param flavors the Flavor(s) (at the lest we need 1 otherwise the
     * function will throw a exception that we want to handle using the provided
     * FlavorProcessor
     * @param priority where to insert the handle. the priority means that it
     * will be added at the start of the List rather than appending it.
     * @throws IllegalStateException if the service is running or processing
     */
    private synchronized void addFlavorHandler(FlavorProcessor handler, boolean priority, DataFlavor... flavors) {
        Objects.requireNonNull(handler, "FlavorProcessor cannot be null");
        Objects.requireNonNull(flavors, "the Flavor cannot be null");
        if (MapProcessors.containsKey(handler)) {
            return;
        }
        //we at this time dont Expect to do Interruptions on the handling.
        var itemHandler = new FlavorHandler(null, handler, flavors);
        MapProcessors.put(handler, itemHandler);
        LoggingHelper.getLogger(LOGGERNAME).log(Level.INFO, "Registering FlavorProcessor");
        if (priority) {
            FlavorsListPriority.addFirst(itemHandler);
        } else {
            FlavorsListPriority.addLast(itemHandler);
        }
    }

    /**
     * Add a Flavor processor to the list of handlers at the end of the list.
     * NOTE: the order of the handlers is important as it will be used to
     * provide priority to the handlers. the first handler in the list will be
     * the first one to be called. and so on.
     *
     * @param flavors the Flavor(s) that we want to handle using the provided
     * FlavorProcessor
     * @param handler the FlavorProcessor to register with the provided data
     * flavor
     * @throws IllegalStateException if the service is running or processing
     */
    public final void addFlavorHandler(FlavorProcessor handler, DataFlavor... flavors) {
        addFlavorHandler(handler, false, flavors);
    }

    /**
     * Add a FlavorHandler to the list of handlers at the start of the
     * list.NOTE: the order of the handlers is important as it will be used to
     * provide priority to the handlers. the first handler in the list will be
     * the first one to be called. and so on.
     *
     * @param flavors the Flavor(s) that we want to handle using the provided
     * FlavorProcessor
     * @param handler the FlavorProcessor to add
     * @throws IllegalStateException if the service is running or processing
     */
    public final void addPriorityFlavorHandler(FlavorProcessor handler, DataFlavor... flavors) {
        addFlavorHandler(handler, true, flavors);
    }

    /**
     * removes the specified FlavorProcessor from the list of handling Flavors.
     * if and only if:
     * <ul>
     * <li>the service is <strong>Not</strong> online</li>
     * <li>the service is <strong>Not</strong> processing data</li>
     * </ul>
     *
     * @param handler the {@code FlavorProcessor} to be excluded.
     * @return true if item was removed false otherwise.
     * @throws IllegalStateException if the Service is currently processing data
     * or is online.
     */
    public synchronized boolean RemoveFlavorHandler(FlavorProcessor handler) {
        LoggingHelper.getLogger(LOGGERNAME).log(Level.INFO, "Removing FlavorProcessor");
        var tmp = MapProcessors.remove(handler);
        if (tmp != null) {
            return FlavorsListPriority.remove(tmp);
        }
        return false;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Target Registration">
    /**
     * Registers a Drop target that will Listen to events with **this** instance
     * and process the Drag and Drop events.
     *
     * @param UIComponent the component that was registered to this listener.
     * @return false if Registration Fails because it is Already registered. and
     * true if able to be registered.
     */
    public boolean RegisterTarget(final Component UIComponent) {
        var matches = Targets.stream().filter((target) -> {
            return Objects.equals(target.getComponent(), UIComponent);
        }).count();
        if (matches > 0) {
            return false;
        }
        var newdrop = new DropTarget(UIComponent, DnDConstants.ACTION_COPY_OR_MOVE, this);
        Targets.add(newdrop);
        return true;
    }

    /**
     * Un-Registers if registered the provided component. by Un-Registers it
     * also imply:
     * <br>
     * the component removes this listener (calling
     * {@link DropTarget#removeDropTargetListener(DropTargetListener)})
     *
     * <br>
     * the DropTarget also Notifies its removal.
     * {@link DropTarget#removeNotify()})
     * <br>
     * set the DropTarget as Inactive {@link DropTarget#setActive(boolean)}
     * <br>
     * and Removes the DropTarget of the list of registered DropTarget on
     * **this** instance.
     *
     * @param UIComponent the component that was registered to this listener.
     * @return true if the component was Registered and was removed. false if
     * the component was not listed at all.
     */
    public boolean unRegisterTarget(Component UIComponent) {
        var matches = Targets.stream().filter((target) -> {
            return Objects.equals(target.getComponent(), UIComponent);
        });
        boolean change = false;
        for (var target : matches.toList()) {
            target.removeDropTargetListener(this);
            target.removeNotify();
            target.setActive(false);
            change = change || Targets.remove(target);
        }
        return change;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Drag Events">    
    /**
     * {@inheritDoc}
     * <P>
     * process the Event of a Drag Entering the app if the flavor is to be
     * ignored. we don't process the drag.
     */
    @Override
    public final void dragEnter(DropTargetDragEvent dtde) {
        var log = LoggingHelper.getLogger(LOGGERNAME);
        if (ignoreFlavor(dtde)) {
            log.log(Level.INFO, "DragEnter is to be Rejected");
            dtde.rejectDrag();
            return;
        }
        // trigger update to the UI.
        triggerDragEvent(dtde.getDropTargetContext().getComponent());
        log.log(Level.INFO, "DragEnter at Location: {0}", dtde.getLocation());
        dtde.acceptDrag(DnDConstants.ACTION_COPY);
    }

    /**
     * {@inheritDoc}
     * <P>
     * process the Event of a Drag Over the app if the flavor is to be ignored.
     * we don't process the drag.
     */
    @Override
    public void dragOver(DropTargetDragEvent dtde) {
        var log = LoggingHelper.getLogger(LOGGERNAME);
        if (ignoreFlavor(dtde)) {
            log.log(Level.INFO, "dragOver is to be Rejected");
            dtde.rejectDrag();
            return;
        }
        log.log(Level.FINE, "dragOver at Location: {0}", dtde.getLocation());
        dtde.acceptDrag(DnDConstants.ACTION_COPY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
        var log = LoggingHelper.getLogger(LOGGERNAME);
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
        var log = LoggingHelper.getLogger(LOGGERNAME);
        log.log(Level.INFO, "dragExit at Component: {0}", dte.getDropTargetContext().getComponent().getClass().getName());
        // The D&D went outside of the app Area. and thus we can disenagage.
        triggerDragExitEvent(dte.getDropTargetContext().getComponent());
    }

    /**
     * The getTransferable method returns a Transferable (not necessarily the
     * one the DragSource registered, it may be a proxy, and certainly shall be
     * in the inter-JVM case) to enable data transfers to occur via its
     * getTransferData method. Note that it is illegal to invoke getTransferable
     * without first invoking an acceptDrop.
     * https://docs.oracle.com/javase/1.5.0/docs/guide/dragndrop/spec/dnd1.html
     * (doc doc.) and java doc also suggest this still applicable. thus. we
     * likely will required to review FlavorsListPriority against the DTDE list
     * at the least twice. here and when we are to consume the data.
     *
     * to process a DnD unlike clipboard. we need to first accept or deny the
     * drop or at least that seems to be the case according to the limited
     * documentation
     *
     * @return a FlavorHandler if we can drop the info. with any FlavorHandler
     * null if is not possible
     */
    private FlavorHandler isDroppable(DropTargetDropEvent dtde) {
        var flavors = dtde.getCurrentDataFlavors();
        DebugLogFlavors(flavors);
        FlavorHandler detected = null;
        for (var flavorHandler : FlavorsListPriority) {
            if (flavorHandler.canConsume(flavors)) {
                detected = flavorHandler;
                break;
            }
        }
        return detected;
    }
    
    
    private void DebugLogFlavors(DataFlavor flavors[]) {
        if (!LoggingHelper.RunningInDebugMode()) {
            return;
        }
        if(flavors.length==0){
             LoggingHelper.getLogger(LOGGERNAME+".debug").log(Level.INFO,"Edge case: there are no Data Flavors provided.");
        }
        HashMap<String,LinkedHashSet<String>> flavorsNames = new HashMap<>();
        for (DataFlavor flavor : flavors) {
            if (flavor == null) {
                continue;
            }
            var list= flavorsNames.get(flavor.getHumanPresentableName());
            if(list==null){
                list = new LinkedHashSet<>();
            }
            var added = list.add(flavor.getDefaultRepresentationClassAsString());
            flavorsNames.put(flavor.getHumanPresentableName(), list);
                    
            if (added) {
                LoggingHelper.getLogger(LOGGERNAME+".debug").log(Level.INFO, "Flavor: {0} :: Flavor Class: {1}\nMIME:{2}",
                        new Object[]{flavor.getHumanPresentableName(),
                            flavor.getDefaultRepresentationClassAsString(),
                        flavor.getMimeType()
                        });
            }
        }
        flavorsNames.clear();
    }

    /**
     * logs the information for this Dropping action.
     *
     * @param dropAction the flag that was reported.
     */
    private void logDropActionType(int dropAction) {
        var log = LoggingHelper.getLogger(LOGGERNAME);
        StringBuilder Loginfo = new StringBuilder();
        if ((dropAction | DnDConstants.ACTION_LINK) == DnDConstants.ACTION_LINK) {
            Loginfo.append("Contains \"ACTION_LINK\"");
            Loginfo.append(" ");
        }
        if ((dropAction | DnDConstants.ACTION_COPY) == DnDConstants.ACTION_COPY) {
            Loginfo.append("Contains \"ACTION_COPY\"");
            Loginfo.append(" ");
        }
        if ((dropAction | DnDConstants.ACTION_MOVE) == DnDConstants.ACTION_MOVE) {
            Loginfo.append("Contains \"ACTION_MOVE\"");
            Loginfo.append(" ");
        }
        if (dropAction == DnDConstants.ACTION_NONE) {
            Loginfo.append("Contains \"ACTION_NONE\"");
        }
        log.log(Level.INFO, "Drop Action reports the following flags: {0}",
                Loginfo.toString().strip());
    }

    /**
     * {@inheritDoc}
     * <p>
     * as per {@link DropTargetListener#drop(java.awt.dnd.DropTargetDropEvent)}
     * we need to carefully process this request in the order of
     * <br> {@link DropTargetDropEvent#acceptDrop(int)} -> consume ->
     * {@link DropTargetDropEvent#dropComplete(boolean)} or otherwise
     * {@link DropTargetDropEvent#rejectDrop()} and return
     *
     * @param dtde the DropTargetDropEvent that is triggering a Data Transfer on
     * the Drag and Drop functionality.
     */
    @Override
    public void drop(DropTargetDropEvent dtde) {
        var log = LoggingHelper.getLogger(LOGGERNAME);
        log.log(Level.INFO, "Drop trigger at: {0}", dtde.getLocation());
        var detected = isDroppable(dtde);        
        //at this point we know that we can support the drop. therefore accept it
        logDropActionType(dtde.getDropAction());
        if (Objects.isNull(detected)) {
            //we dont known, the Source did not provided flavors or we dont support
            //this flavor as reported. thus reject it. 
            dtde.rejectDrop();
            triggerDropComplete(dtde.getDropTargetContext().getComponent());
            return;
        }
        //this implementation should be able to handle any sort of action.
        dtde.acceptDrop(dtde.getDropAction());
        Transferable contents = null;
        try {
            contents = dtde.getTransferable();
        } catch (InvalidDnDOperationException ex) {
            log.log(Level.SEVERE, "we meet a error attempting to process the DnD action", ex);
        }
        boolean handled = false;
        if (Objects.nonNull(contents)) {
            handled = processDrop(detected, contents);
        }
        dtde.dropComplete(handled);
        //Notify the UI (if needs be) that the Drag/drop is complete
        triggerDropComplete(dtde.getDropTargetContext().getComponent());
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Process Drop Event">
    /**
     * process and handles the DnD drop event calling the prefer handler first.
     * if it fails it tries to call the next one from the
     * {@code FlavorsListPriority}
     *
     * @param detected the detected FlavorHandler. that we should prioritize to
     * use
     * @param contents the transferable object from which we read the DnD data
     * @return whenever we succeed to handle the DnD. returns false if we
     * exausted all handles and the data could not be handled.
     */
    private boolean processDrop(FlavorHandler detected, Transferable contents) {
        //first try to process using the detected if works. fine otherwise loop all flavors
        boolean bail = runDrop(detected, contents);
        if (bail || FlavorsListPriority.size() == 1) {
            return bail;
        }
        //if there are more handles. and the first one did not handle. then lets
        //process in the priority order minus the one we alredy handled.
        for (FlavorHandler handler : FlavorsListPriority) {
            if (Objects.equals(handler, detected)) {
                continue;
            }
            bail = runDrop(handler, contents);
            if (bail) {
                break;
            }
        }
        return bail;
    }

    /**
     * Executes the Handler. while catching error. and logging if they happen.
     *
     * @param handler the handler to call
     * @param contentst the content to delegate
     * @return whenever or not the execution succeed.
     */
    private boolean runDrop(FlavorHandler handler, Transferable contents) {
        try {
            var result = handler.handleFlavor(contents);
            if (result) {
                LoggingHelper.getLogger(LOGGERNAME).info("Drag And Drop Data Handled");
                return true;
            }
        } catch (DataTransferException Cex) {
            LoggingHelper.getLogger(LOGGERNAME)
                    .log(Level.SEVERE, "Drag And Drop Exception detected Will return", Cex);
        } catch (Throwable ex) {
            //capture all other errors and log em 
            //we do this as handlers might not have handled the error.
            //but are NOT errors that we should care for.
            LoggingHelper.getLogger(LOGGERNAME)
                    .log(Level.SEVERE, "Error Has been catch at runDrop", ex);
        }
        return false;
    }
    //</editor-fold>

    /**
     * check if the provided Drop Event contains at least a flavor that we don't
     * ignore. if so return false, and true if all flavors are to be ignored.
     *
     * @param dtde the DropTargetDragEvent Event to analyze.
     * @return true if all Flavors for this actions are to be ignore. false if
     * at the least there is 1 flavor that is not to be ignored.
     */
    private boolean ignoreFlavor(DropTargetDragEvent dtde) {
        if (dtde.getCurrentDataFlavors() == null
                || dtde.getCurrentDataFlavors().length == 0) {
            //weird edge case. when dragging a image from the browser
            //on ocations can be done but it does not want to disclose the flavor.
            return false;
        }
        for (var flavor : dtde.getCurrentDataFlavors()) {
            if (!FlavorsIgnore.contains(flavor)) {
                return false;
            }
        }
        return true;
    }

    //<editor-fold defaultstate="collapsed" desc="UI events">
    private void triggerDragEvent(final Component component) {
        for (DragDropEventListener dndListener : dndListeners) {
            if (SwingUtilities.isEventDispatchThread()) {
                dndListener.dragEvent(component);
            } else {
                SwingUtilities.invokeLater(() -> {
                    dndListener.dragEvent(component);
                });
            }
        }
    }

    private void triggerDragExitEvent(final Component component) {
        for (DragDropEventListener dndListener : dndListeners) {
            if (SwingUtilities.isEventDispatchThread()) {
                dndListener.dragExitEvent(component);
            } else {
                SwingUtilities.invokeLater(() -> {
                    dndListener.dragExitEvent(component);
                });
            }
        }
    }

    private void triggerDropComplete(Component component) {
        for (DragDropEventListener dndListener : dndListeners) {
            if (SwingUtilities.isEventDispatchThread()) {
                dndListener.dropCompleteEvent(component);
            } else {
                SwingUtilities.invokeLater(() -> {
                    dndListener.dropCompleteEvent(component);
                });
            }
        }
    }

    public synchronized boolean registerEventListener(DragDropEventListener eventListener) {
        Objects.requireNonNull(eventListener, "the eventListener cannot be null");
        return dndListeners.add(eventListener);
    }

    public synchronized boolean removeEventListener(DragDropEventListener eventListener) {
        Objects.requireNonNull(eventListener, "the eventListener cannot be null");
        return dndListeners.remove(eventListener);
    }
    //</editor-fold>

}
