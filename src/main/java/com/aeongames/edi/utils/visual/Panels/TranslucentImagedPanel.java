/*
 *  Copyright © 2012,2024 Eduardo Vindas. All rights reserved.
 *  
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package com.aeongames.edi.utils.visual.Panels;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

/**
 * a Panel that Renders a Image with a Background that support levels of Transparency
 * thus this panel can render a Image and being totally opaque or Totally transparent
 * or a level in between. 
 * this panel also support rendering the panel as a "Round Rectangle"
 * @author Eduardo Vindas
 */
public class TranslucentImagedPanel extends ImagePanel {

    public static final int DEFAULTARC = 0;
    public static final short DEFTRANSPARENCY = 190;
    public static final short MIN_ALPHA = 0, MAX_ALPHA = 0xFF;
    public static final Color DEFAULT_COLOR = new Color(219, 229, 241, DEFTRANSPARENCY); //r,g,b,alpha
    private int arcWidth = DEFAULTARC,
            arcHeight = DEFAULTARC;
    private int alphaIntensity = DEFTRANSPARENCY;
    private Color ColorOverride = DEFAULT_COLOR;

    public TranslucentImagedPanel() {
        super();
        super.setOpaque(false);
    }

    public TranslucentImagedPanel(Image todisplay) {
        super(todisplay);
        super.setOpaque(false);
    }

    private TranslucentImagedPanel(Image todisplay, float Imagealpha) {
        super(todisplay, Imagealpha);
        super.setOpaque(false);
    }

    /**
     * unlike the original implementation on this case if you set opaque or not
     * will result in either begin completely opaque or complete transparent on
     * the internal alpha level. so it will not call the parent implementation.
     * however the result will appear to be the same NOTE: on this case it does
     * not affect the image!
     *
     * @param isOpaque
     */
    @Override
    public void setOpaque(boolean isOpaque) {
        if (isOpaque) {
            setPanelTrasparency(MAX_ALPHA);
        } else {
            setPanelTrasparency(MIN_ALPHA);
        }
    }

    public void setPanelTrasparency(short newAlphaLevel) {
        if (newAlphaLevel == alphaIntensity) {
            return;
        }
        if (newAlphaLevel >= MIN_ALPHA && newAlphaLevel <= MAX_ALPHA) {
            alphaIntensity = newAlphaLevel;
            updatecolor();
        }
        repaint();
    }

    private void updatecolor() {
        var backColor = ColorOverride == null ? getBackground() : ColorOverride;
        var colorvalue = 0x00FFFFFF & backColor.getRGB();//strip alpha if any
        colorvalue = ((alphaIntensity & 0xFF) << 24) | colorvalue;
        ColorOverride = new Color(colorvalue, true);
    }

    public int getpanelAlpha() {
        return alphaIntensity;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Color temp = g.getColor();
        g.setColor(ColorOverride);
        g.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), arcWidth, arcHeight);
        g.setColor(temp);
    }

    public void setuniformarc(int arc) {
        if (arc > 0) {
            arcWidth = arcHeight = arc;
        }
    }

    /**
     * @param arcWidth the arcWidth to set
     */
    public void setArcWidth(int arcWidth) {
        if (arcWidth > 0) {
            this.arcWidth = arcWidth;
        }
    }

    /**
     * @param arcHeight the arcHeight to set
     */
    public void setArcHeight(int arcHeight) {
        if (arcHeight > 0) {
            this.arcHeight = arcHeight;
        }
    }

    @Override
    public void setBackground(Color bg) {
        if (bg != null) {
            setColor(bg);
        } else {
            setColor(DEFAULT_COLOR);
        }
        super.setBackground(ColorOverride);
    }

    public final void setColor(Color col) {
        setColor(col, false);
    }

    public final void setColor(Color col, boolean UseColorAlpha) {
        if (col != null) {
            if (alphaIntensity == 0xFF && col.getAlpha() == 0xFF) {
                ColorOverride = col;
            }
            if (col.getAlpha() == 0xFF || !UseColorAlpha) {
                var colorvalue = 0x00FFFFFF & col.getRGB();//strip alpha if any
                colorvalue = ((alphaIntensity & 0xFF) << 24) | colorvalue;
                ColorOverride = new Color(colorvalue, true);
            } else if (UseColorAlpha) {
                ColorOverride = col;
                alphaIntensity = ColorOverride.getAlpha();
            }
            this.repaint();
        }
    }
}
