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
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.aeongames.edi.utils.datatransfer;

import java.awt.Component;

/**
 *
 * @author cartman
 */
public interface dndEventListener {
    /**
     * triggered by the Event Dispatch thread when a
     * {@link DropTargetListener#dragEnter(java.awt.dnd.DropTargetDragEvent)}
     * event happens. this method is ensured to be called by the EDT. note this
     * method will block the thread that trigger the Drag and Drop (if that
     * thread is NOT the EDT) and activity takes too long can cause problems on
     * the OS Drag And Drop functionality. thus make sure this method returns as
     * fast as possible. also note. the even is unfiltered. Accepting or
     * rejecting the Event is not required to be performed as this parent class
     * accepts the event.
     *
     * @param AffecteDnDComponent the Component where the DnD event is affecting
     */
    public void dragEvent(Component AffecteDnDComponent);

    /**
     * triggered by the Event Dispatch thread when a
     * {@link DropTargetListener#dragExit(java.awt.dnd.DropTargetEvent)} event
     * happens. this method is ensured to be called by the EDT. note this method
     * will block the thread that trigger the Drag and Drop (if that thread is
     * NOT the EDT) and activity takes too long can cause problems on the OS
     * Drag And Drop functionality. thus make sure this method returns as fast
     * as possible. also note. the even is unfiltered.
     *
     * @param AffecteDnDComponent the Component where the DnD event is affecting
     */
    public void dragExitEvent(Component AffecteDnDComponent);

    /**
     * a method to be implemented by child classes. we ensure that the parameter
     * is not null, but not that the list is not empty. we however do NOT
     * warrantee that the Paths exists or that are valid thus the implementer
     * need to check whenever or not the path exist, can be read and such.
     * <strong>we do not warrantee this will be called from EDT</strong>
     *
     * @param fileList a NON-null list that contains the paths desired to be
     * loaded.
     */
    
    /**
     * triggered by the Event Dispatch thread when a
     * {@link DropTargetListener#drop(java.awt.dnd.DropTargetDropEvent)} event
     * concludes its execution and the Drop is consumed. this method is ensured
     * to be called by the EDT. note this method will block the thread that
     * trigger the Drag and Drop (if that thread is NOT the EDT) and activity
     * takes too long can cause problems on the OS Drag And Drop functionality.
     * thus make sure this method returns as fast as possible. also note. the
     * even is unfiltered.
     *
     * @param AffecteDnDComponent the Component where the DnD event is affecting
     */
    public void dropCompleteEvent(Component AffecteDnDComponent);
}
