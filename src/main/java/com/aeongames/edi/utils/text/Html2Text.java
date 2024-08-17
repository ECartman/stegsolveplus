/*
 * 
 * Copyright © 2010-2024 Eduardo Vindas. All rights reserved.
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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

public class Html2Text extends HTMLEditorKit.ParserCallback {

    private static final Logger log = Logger.getLogger(Html2Text.class.getName());

    static {
        try {
            if (!new java.io.File("error").exists() || !new java.io.File("error").isDirectory()) {
                new java.io.File("error").mkdir();
            }
            log.addHandler(new java.util.logging.FileHandler("error/Html2Text%g.log"));
        } catch (IOException | SecurityException ex1) {
        }
    }
    private boolean ignoretags = false;
    private StringBuffer stringBuffer;
    private Stack<IndexType> indentStack;

    public static class IndexType {

        public String type;
        public int counter; // used for ordered lists

        public IndexType(String type) {
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
     * depending on the parameter value we will acknowledge the parameter if is
     * send as true we will ignore the tags and will not be process meaning for
     * example &lt; br/ &gt; will be ignore and will not be change into a "\n"
     *
     * @param ignoretagseffect
     */
    public Html2Text(boolean ignoretagseffect) {
        stringBuffer = new StringBuffer();
        indentStack = new Stack<>();
        ignoretags = ignoretagseffect;
    }

    public static String convert(String html) {
        Html2Text parser = new Html2Text();
        Reader in = new StringReader(html);
        try {
            // the HTML to convert
            parser.parse(in);
        } catch (IOException e) {
            log.log(Level.SEVERE, "error Parsing", e);
        } finally {
            try {
                in.close();
            } catch (IOException ioe) {
                log.log(Level.WARNING, "error in.close();", ioe);
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
        if (!ignoretags) {
            switch (t.toString()) {
                case "p":
                    if (stringBuffer.length() > 0 && !stringBuffer.substring(stringBuffer.length() - 1).equals("\n")) {
                        newLine();
                    }   newLine();
                    break;
                case "ol":
                    indentStack.push(new IndexType("ol"));
                    newLine();
                    break;
                case "ul":
                    indentStack.push(new IndexType("ul"));
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
                    }   indentStack.push(new IndexType("li"));
                    break;
                case "dl":
                    newLine();
                    break;
                case "dt":
                    newLine();
                    break;
                case "dd":
                    indentStack.push(new IndexType("dd"));
                    newLine();
                    break;
                default:
                    break;
            }
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
        if (!ignoretags){
            switch (t.toString()) {
                case "p":
                    newLine();
                    break;
                case "ol":
                    indentStack.pop();
                    newLine();
                    break;
                case "ul":
                    indentStack.pop();
                    newLine();
                    break;
                case "li":
                    indentStack.pop();
                    newLine();
                    break;
                case "dd":
                    indentStack.pop();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) {
        if (!ignoretags) {
            if (t.toString().equals("br")) {
                newLine();
            }
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
