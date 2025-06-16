/*
 *  Copyright © 2024,2025 Eduardo Vindas. All rights reserved.
 *  
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package com.aeongames.edi.utils.error;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;

/**
 * this class defines a Object that Holds a error data and details 
 * of a error to be reported and show on UI. 
 * @author Eduardo Vindas
 * @version 1.3
 */
public class ErrorData {
    /**
     * the error title. 
     */
    private final String ErrorTitle;
    /**
     * the Error message. 
     */
    private final String ErrorMessage;
    /**
     * the Error reference itself.
     */
    private final Throwable error;

    /**
     * creates a new instance of ErrorData and initialize the Properties
     * of this object based on the {@code err} provided
     * @param err the {@link Throwable} to report to this Error Data Object
     * @throws NullPointerException if {@code err} is null
     */
    public ErrorData(final Throwable err) {
        this(null, null, err);
    }
    
    /**
     * creates a new instance of ErrorData and initialize the Properties
     * of this object based on the {@code err} provided
     * @param Message the message to report on this Error Data. we suggest to be
     * this to be somewhat verbose. 
     * @param err the {@link Throwable} to report to this Error Data Object
     * @throws NullPointerException if {@code err} is null
     */
    public ErrorData(String Message, Throwable err) {
        this(null, Message, err);
    }
    
    /**
     * creates a new instance of ErrorData and initialize the Properties
     * of this object based on the {@code err} provided
     * @param title the title of this Error Data. we suggest something shorter than 150 characters.
     * @param Message the message to report on this Error Data. we suggest to be
     * this to be somewhat verbose. 
     * @param err the {@link Throwable} to report to this Error Data Object
     * @throws NullPointerException if {@code err} is null
     */
    public ErrorData(String title, String Message, Throwable err) {
        error = Objects.requireNonNull(err, "the error cannot be null");
        if(Objects.isNull(title)|| title.isBlank()){
            StringBuilder builder = new StringBuilder(error.getClass().getName());
            builder.append(" on Execution");
            title= builder.toString();
        }
        ErrorTitle = title;
        if (Objects.nonNull(Message) && !Message.isBlank()) {
            ErrorMessage = Message;
        } else {
            ErrorMessage = Objects.requireNonNullElse(error.getMessage(),
                    Objects.requireNonNullElse(error.getCause().getMessage(),
                            title));
        }
    }

    /**
     * return a String that represent the Error Title
     * we guarantee this should never be null;
     * @return the ErrorTittle
     */
    public String getErrorTittle() {
        return ErrorTitle;
    }

    /**
     * return a String that represent the Error Message
     * we guarantee this should never be null;
     * @return the ErrorMessage
     */
    public String getErrorMessage() {
        return ErrorMessage;
    }

    /**
     * builds and returns a String representation of the stack when the error
     * was captured.
     * @return a String with the output from the Error.
     * @see Throwable#printStackTrace(java.io.PrintWriter)
     */
    public String getErrorStack() {
        String ErrorStackString = "";
        try (StringWriter writer = new StringWriter()) {
            try (PrintWriter out = new PrintWriter(writer)) {
                error.printStackTrace(out);
                if (error.getCause() != null) {
                    error.getCause().printStackTrace(out);
                }
                //out.flush(); //StringWritter flush does nothing.
                ErrorStackString = writer.toString();
            }
        } catch (IOException e) {
        }
        return ErrorStackString;
    }
}
