/*
 *
 * Copyright © 2008-2012 Eduardo Vindas. All rights reserved.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.aeongames.edi.utils.text;

import java.awt.Toolkit;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 * A Document Filter that Specifically limits the amount of Characters it can be
 * added into a Document (or for the document to contain) the intended usage is
 * to setup a limit text input or prevent Clipboard to input more text that it
 * should be able to handle on a particular Document UI component, such as
 * {@link JTextField}
 *
 * @version 1.0
 * @author Eduardo Vindas C
 */
public class DocumentDelimiterFilter extends DocumentFilter {

    private boolean Enforce = true;
    private int maxCharacters;

    /**
     * creates a new instance of Document Delimiter Filter that accepts up to
     * the desired MaxChars, by default the filter will be Enforced.
     *
     * @param maxChars the max amount for this document to handle.
     */
    public DocumentDelimiterFilter(int maxChars) {
        this(maxChars, true);
    }

    /**
     * creates a new instance of Document Delimiter Filter that accepts up to
     * the desired MaxChars and enforces as it is setup by enforce
     *
     * @param maxChars the max amount for this document to handle.
     * @param enforce set if it is required to enforce the limit
     */
    public DocumentDelimiterFilter(int maxChars, boolean enforce) {
        if (maxChars < 0) {
            throw new IllegalArgumentException("Invalid Delimiter Param");
        }
        maxCharacters = maxChars;
        Enforce = enforce;
    }

    /**
     * {@inheritDoc }
     * <p>
     * This rejects the entire insertion if it would make the contents too long.
     * so we disallow and sent a beep to the pc however if the enforce variable
     * is set to false the rule will be bypassed, however a warning might be
     * called.
     */
    @Override
    public void insertString(FilterBypass fb, int offs, String str, AttributeSet a) throws BadLocationException {
        boolean disallow = (fb.getDocument().getLength() + str.length()) > maxCharacters;
        if (disallow) {
            Toolkit.getDefaultToolkit().beep();
        }
        if (!disallow || !Enforce) {
            super.insertString(fb, offs, str, a);
        }
    }

    /**
     * {@inheritDoc }
     * <p>
     * this rejects the entire replacement if it would make the contents too
     * long. Another option would be to truncate the replacement string so the
     * contents would be exactly maxCharacters in length. unless the enforce is
     * false.
     */
    @Override
    public void replace(FilterBypass fb, int offs, int length, String str,
            AttributeSet a) throws BadLocationException {
        if (str != null) {
            boolean disallow = (fb.getDocument().getLength() + str.length() - length) > maxCharacters;
            if (disallow) {
                Toolkit.getDefaultToolkit().beep();
            }
            if (!disallow || !Enforce) {
                super.replace(fb, offs, length, str, a);
            }
        } else {
            super.replace(fb, offs, length, str, a);
        }
    }
}
