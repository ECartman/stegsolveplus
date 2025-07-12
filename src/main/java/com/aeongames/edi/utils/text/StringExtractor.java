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
package com.aeongames.edi.utils.text;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * this class defines a Helper class that can be used to process Bytes or raw
 * data and process it into string. the fact that the data might or might not be
 * string is not important as it is intended to look for Strings on raw bytes.
 *
 * @author Eduardo Vindas Cordoba
 */
public class StringExtractor {

    /**
     * Reference to UTF Character set. {@link StandardCharsets#UTF_8}
     */
    public static final Charset UTF_8 = StandardCharsets.UTF_8;
    /**
     * Reference to UTF Character set. {@link StandardCharsets#UTF_16}
     */
    public static final Charset UTF_16 = StandardCharsets.UTF_16;
    /**
     * Reference to UTF Character set. {@link StandardCharsets#UTF_16BE}
     */
    public static final Charset UTF_16BE = StandardCharsets.UTF_16BE;
    /**
     * Reference to UTF Character set. {@link StandardCharsets#UTF_16LE}
     */
    public static final Charset UTF_16LE = StandardCharsets.UTF_16LE;
    /**
     * ASCII_PLUS is a character set that is compatible with ASCII and can be
     * used to encode ASCII And a selected set of characters more Reference to
     * UTF Character set. {@link StandardCharsets#ISO_8859_1}
     */
    public static final Charset ASCII_PLUS = StandardCharsets.ISO_8859_1;
    /**
     * Reference to UTF Character set. {@link StandardCharsets#US_ASCII}
     */
    public static final Charset ASCII = StandardCharsets.US_ASCII;
    /**
     * the default min length for strings.
     */
    public static final int DEF_MINLEN = 3;
    /**
     * this instance minimal length for a string
     */
    private final int MinLen;
    /**
     * this instance character set to use to look and convert raw data into
     * strings
     */
    private final Charset charset;
    /**
     * consider only Latin characters?
     */
    private boolean onlyLatinChars = false;
    /**
     * first character valid printable ASCII character
     */
    public static final int FIRST_LATIN_CHAR = 32;//0x20
    /**
     * last printable Character that is valid on the ASCII Table
     */
    public static final int LAST_LATIN_CHAR = 0x7e;

    /**
     * creates a new instance of StringExtractor that will use the defined
     * character set and look for string that at least contains a certain amount
     * of characters.
     *
     * @param charSet the characters set to decode raw bytes into characters
     * @param minlen the minimal acceptable size for a string.
     */
    public StringExtractor(Charset charSet, int minlen, boolean onlyLatinChars) {
        charset = Objects.requireNonNullElse(charSet, StandardCharsets.UTF_8);
        MinLen = minlen;
        this.onlyLatinChars = onlyLatinChars;
    }

    /**
     * creates a new instance of StringExtractor that will use the defined
     * character set and look for string that at least contains
     * {@code DEF_MINLEN} of characters.
     *
     * @param charSet the characters set to decode raw bytes into characters
     */
    public StringExtractor(Charset charSet) {
        this(charSet, DEF_MINLEN, false);
    }

    /**
     * creates a new instance of StringExtractor that will use {@code UTF-8} and
     * look for string that at least contains {@code DEF_MINLEN} of characters.
     *
     * @param charSet the characters set to decode raw bytes into characters
     */
    public StringExtractor() {
        this(StandardCharsets.UTF_8);
    }

    /**
     * checks whenever or not the character provided is "printable" meaning that
     * is not a ISO Control Character (excluding new line and tab)
     *
     * @param ch the character to check
     * @param onlyLatin consider any character that is not within
     * {@code FIRST_LATIN_CHAR} and {@code LAST_LATIN_CHAR}
     * @return true if the character can be printed or is on the range. false
     * otherwise.
     */
    private static boolean isPrintable(char ch, boolean onlyLatin) {
        if (onlyLatin) {
            return (ch >= FIRST_LATIN_CHAR && ch <= LAST_LATIN_CHAR) || ch == '\n' || ch == '\r' && ch == '\t';
        }
        return !(Character.isISOControl(ch) && ch != '\n' && ch != '\r' && ch != '\t');
    }

    private List<String> tryDecode(byte[] bytes, Charset charset) {
        LinkedList<String> list = new LinkedList<>();
        StringBuilder builder = new StringBuilder();
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        CharBuffer charBuffer = CharBuffer.allocate(bytes.length * MaxBytesPerchar(charset));
        CharsetDecoder decoder = charset.newDecoder()
                .onMalformedInput(CodingErrorAction.IGNORE)
                .onUnmappableCharacter(CodingErrorAction.IGNORE);

        var result = decoder.decode(byteBuffer, charBuffer, false);
        if (result.isError()) {
            //there is a error reading the data. 
        }
        if (result.isUnderflow() && byteBuffer.hasRemaining()) {
            // Not enough bytes for a character yet, compact and continue
            byteBuffer.compact();
        }
        decoder.reset();
        charBuffer.flip();
        while (charBuffer.hasRemaining()) {
            char c = charBuffer.get();
            if (isPrintable(c, onlyLatinChars)) {
                builder.append(c);
            } else if (builder.length() >= MinLen) {
                var resultstr = builder.toString();
                if (!resultstr.isBlank()) {
                    list.add(resultstr);
                }
                builder.delete(0, builder.length());
            }
        }
        byteBuffer.compact();
        if (!builder.isEmpty() && builder.length() >= MinLen) {
            var stringresult = builder.toString();
            if (!stringresult.isBlank()) {
                list.add(stringresult);
            }
        }
        return list;
    }

    private void DecoderReport(byte[] bytes, Charset charset, Consumer<String> ReporterFuntion) {
        StringBuilder builder = new StringBuilder();
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        CharBuffer charBuffer = CharBuffer.allocate(bytes.length * MaxBytesPerchar(charset));
        CharsetDecoder decoder = charset.newDecoder()
                .onMalformedInput(CodingErrorAction.IGNORE)
                .onUnmappableCharacter(CodingErrorAction.IGNORE);

        var result = decoder.decode(byteBuffer, charBuffer, false);
        if (result.isError()) {
            //there is a error reading the data. 
        }
        if (result.isUnderflow() && byteBuffer.hasRemaining()) {
            // Not enough bytes for a character yet, compact and continue
            byteBuffer.compact();
        }
        decoder.reset();
        charBuffer.flip();
        while (charBuffer.hasRemaining()) {
            char c = charBuffer.get();
            if (isPrintable(c, onlyLatinChars)) {
                builder.append(c);
            } else if (builder.length() >= MinLen) {
                var resultstr = builder.toString();
                if (!resultstr.isBlank() && resultstr.trim().length() >= MinLen) {
                    ReporterFuntion.accept(resultstr);
                }
                builder.delete(0, builder.length());
            }
        }
        byteBuffer.compact();
        if (!builder.isEmpty() && builder.length() >= MinLen) {
            var stringresult = builder.toString();
            if (!stringresult.isBlank()) {
                ReporterFuntion.accept(stringresult);
            }
        }
    }

    /**
     * get the maximum amount of bytes required to contain a single character
     * for the specified Character Set.
     *
     * @param Testingset the character set to test.
     * @return the number of bytes that are up to required to read a single
     * character.
     */
    public static int MaxBytesPerchar(Charset Testingset) {
        return (int) Math.ceil(Testingset.newEncoder().maxBytesPerChar());
    }

    /**
     * get the absolutely minimum amount of bytes required to contain a single
     * character for the specified character set.
     *
     * @param Testingset the character set to test.
     * @return the min amount to read a single character.
     */
    public static int MinBytesPerchar(Charset TestingSet) {
        CharsetEncoder encoder = TestingSet.newEncoder();
        var FlooredMedian = (int) Math.floor(encoder.averageBytesPerChar());
        int abitrarycharSize = Integer.MAX_VALUE;
        int abitrarycharSize2 = Integer.MAX_VALUE;
        if (encoder.canEncode('a')) {
            try {
                abitrarycharSize = encoder.encode(CharBuffer.wrap(new char[]{'a'})).array().length;
                abitrarycharSize2 = encoder.encode(CharBuffer.wrap(new char[]{'Y'})).array().length;
            } catch (CharacterCodingException ex) {
            }
        }
        return Math.min(FlooredMedian, Math.min(abitrarycharSize, abitrarycharSize2));
    }

    public void readFromBuffer(BufferedInputStream bis, Consumer<String> ReporterFuntion) throws IOException {
        var byteschar = MinBytesPerchar(charset);
        var buffersize = 4096;
        if (byteschar > 1 && buffersize % byteschar != 0) {
            //set the buffer size closest to og buffersize but that is multiple of byteschar
            buffersize = ((int) buffersize / byteschar) * byteschar;
        }
        byte[] buffer = new byte[buffersize];
        int bytesRead;
        while ((bytesRead = bis.read(buffer)) != -1) {
            ByteArrayOutputStream candidate = new ByteArrayOutputStream();
            candidate.write(buffer, 0, bytesRead);
            DecoderReport(candidate.toByteArray(), charset, ReporterFuntion);
        }
    }

}
