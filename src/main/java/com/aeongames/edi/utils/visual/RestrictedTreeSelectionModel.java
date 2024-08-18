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

import java.util.ArrayList;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * this class is created in order to avoid the user to select non leaf nodes on
 * the tree of solution so the category is no added to the resolution
 *
 * @author Eduardo Vindas.
 */
public class RestrictedTreeSelectionModel extends DefaultTreeSelectionModel {

    public RestrictedTreeSelectionModel() {
        super();
    }

    @Override
    public void setSelectionPaths(TreePath[] pPaths) {
        ArrayList<TreePath> temp = new ArrayList<>();
        for (int i = 0, n = pPaths != null ? pPaths.length : 0; i < n;
                i++) {
            Object lastPathComponent = pPaths[i].getLastPathComponent();
            if (lastPathComponent instanceof TreeNode treeNode) {
                if (treeNode.isLeaf()) {
                    temp.add(pPaths[i]);
                }
            }
        }
        if (!temp.isEmpty()) {
            super.setSelectionPaths(temp.toArray(TreePath[]::new));
        }
    }

    @Override
    public void addSelectionPath(TreePath path) {
        var component = path.getLastPathComponent();
        if (component instanceof TreeNode treeNode) {
            if (treeNode.isLeaf()) {
                super.addSelectionPath(path);
            }
        }
    }
}
