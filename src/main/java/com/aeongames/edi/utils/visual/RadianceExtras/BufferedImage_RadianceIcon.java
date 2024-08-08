package com.aeongames.edi.utils.visual.RadianceExtras;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.pushingpixels.radiance.common.api.icon.RadianceIcon;

public final class BufferedImage_RadianceIcon implements RadianceIcon{
	/**
	 * the Backing Image that represent this RadianceIcon {@link RadianceIcon}
	 */
	private final BufferedImage Backing_Image; 
    private int width;
    private int height;
	
	
	@SuppressWarnings("unused")
	private BufferedImage_RadianceIcon() throws IllegalArgumentException{
		throw new IllegalArgumentException("this call is not permited");
	}
	
	public BufferedImage_RadianceIcon(File ImageFile) throws IOException {
		Backing_Image = ImageIO.read(ImageFile);
		width=Backing_Image.getWidth();
		height=Backing_Image.getHeight();
	}
		
	public BufferedImage_RadianceIcon(Path PathToImage) throws IOException {
		this(PathToImage.toFile());
	}
	
	public BufferedImage_RadianceIcon(URL ResourseURL) throws IOException {
		Backing_Image = ImageIO.read(ResourseURL);
		width=Backing_Image.getWidth();
		height=Backing_Image.getHeight();
	}
	
	/**
	 * create a new instance of {@link BufferedImage_RadianceIcon} using the provided BufferedImage as base
	 * Do note that the {@code source} will not be copied, rather it will use the provided reference thus is Caller 
	 * responsibility to ensured that provided Image will persist as long as it is needed for this class. or otherwise
	 * create a deep copy prior calling this class.   
	 * @param source the {@link BufferedImage} to use on this class.
	 */
	public BufferedImage_RadianceIcon(BufferedImage source) {
		Backing_Image = source;
	}
	
    
    @Override
    public int getIconWidth() {
        return this.width;
    }

    @Override
    public int getIconHeight() {
        return this.height;
    }

    @Override
    public void setDimension(Dimension newDimension) {
        this.width = newDimension.width;
        this.height = newDimension.height;
    }

    @Override
    public boolean supportsColorFilter() {
        return false;
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void paintIcon(Component c, Graphics g, int x, int y) {
        g.drawImage(Backing_Image, x, y, getIconWidth(), getIconHeight(),
                null);
    }
}
