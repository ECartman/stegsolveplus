/*
 * 
 * Copyright © 2008-2011,2024-2025 Eduardo Vindas Cordoba. All rights reserved.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
/**
 * Html2Text.java
 * based on Real Java How to implementation
 * Created on 09/11/2010, 02:21:18 PM
 */
package com.aeongames.edi.utils.text;

import com.aeongames.edi.utils.error.LoggingHelper;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Objects;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

public class Html2Text extends HTMLEditorKit.ParserCallback {

    /**
     * error logging object.
     */
    private static final Logger LOGGER = LoggingHelper.getClassLoggerForMe();
    /**
     * a bool that determine if we should just ignore tags that have special
     * meaning such as:
     * <ul>
     * <li>p</li>
     * <li>ol</li>
     * <li>ul</li>
     * <li>li</li>
     * <li>dd</li>
     * </ul>
     */
    private boolean ignoretags = false;

    /**
     * the buffer that contains the text.
     */
    private final StringBuffer stringBuffer;
    private Stack<IndexType> indentStack;

    private static class IndexType {

        String type;
        int counter; // used for ordered lists

        IndexType(String type) {
            this.type = type;
            counter = 0;
        }
    }

    /**
     * this create a new instance and will listen and process the tag effect on
     * the Text.
     */
    public Html2Text() {
        stringBuffer = new StringBuffer();
        indentStack = new Stack<>();
    }

    /**
     * creates a new instance and will listen and process. the tags if the
     * parameters is set to process the tags.
     *
     * @param ignoreTags true to ignore the tags. false to process the tags
     * (default)
     */
    public Html2Text(boolean ignoreTags) {
        this();
        ignoretags = ignoreTags;
    }

    /**
     * Parses the HTML String into a plain text string.
     *
     * @param html the HTML text to parse to plain text
     * @return the Plain text resulting of parsing the HTML.
     */
    public static String convert(String html) {
        Html2Text parser = new Html2Text();
        Reader in = new StringReader(html);
        try {
            // the HTML to convert
            parser.parse(in);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "error Parsing", e);
        } finally {
            try {
                in.close();
            } catch (IOException ioe) {
                LOGGER.log(Level.WARNING, "error in.close();", ioe);
            }
        }
        return parser.getText();
    }

    public void parse(Reader in) throws IOException {
        ParserDelegator delegator = new ParserDelegator();
        // the third parameter is TRUE to ignore charset directive
        delegator.parse(in, this, Boolean.TRUE);
    }

    @Override
    public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
        if (ignoretags) {
            return;
        }
        switch (t.toString()) {
            case "dd"://add identation and newline
            case "ol"://add identation and newline
            case "ul"://add identation and newline
                indentStack.push(new IndexType(t.toString()));
            case "dl"://add newline
            case "dt"://add newline
                newLine();
                break;
            case "p":
                if (stringBuffer.length() > 0 && !stringBuffer.substring(stringBuffer.length() - 1).equals("\n")) {
                    newLine();
                }
                newLine();
                break;
            case "li":
                IndexType parent = indentStack.peek();
                if (parent.type.equals("ol")) {
                    String numberString = "" + (++parent.counter) + ".";
                    stringBuffer.append(numberString);
                    for (int i = 0; i < (4 - numberString.length()); i++) {
                        stringBuffer.append(" ");
                    }
                } else {
                    stringBuffer.append("*   ");
                }
                indentStack.push(new IndexType("li"));
                break;
            default:
                break;
        }
    }

    private void newLine() {
        stringBuffer.append("\n");
        for (var indentStack1 : indentStack) {
            stringBuffer.append(" ");
        }
    }

    @Override
    public void handleEndTag(HTML.Tag t, int pos) {
        if (ignoretags) {
            return;
        }
        switch (t.toString()) {
            case "ol":
            case "ul":
            case "li":
                indentStack.pop();
            case "p":
                newLine();
                break;
            case "dd":
                indentStack.pop();
                break;
            default:
                break;
        }
    }

    @Override
    public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) {
        if (ignoretags) {
            return;
        }
        if (Objects.equals(HTML.Tag.BR, t)) {
            newLine();
        }
    }

    @Override
    public void handleText(char[] text, int pos) {
        stringBuffer.append(text);
    }

    public String getText() {
        return stringBuffer.toString();
    }
}
