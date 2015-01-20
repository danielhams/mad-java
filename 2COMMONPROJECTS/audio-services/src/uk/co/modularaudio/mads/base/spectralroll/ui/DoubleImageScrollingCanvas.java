/**
 *
 * Copyright (C) 2015 - Daniel Hams, Modular Audio Limited
 *                      daniel.hams@gmail.com
 *
 * Mad is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Mad is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Mad.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.co.modularaudio.mads.base.spectralroll.ui;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.logdisplay.ampscale.AmpScaleComputer;
import uk.co.modularaudio.util.audio.logdisplay.freqscale.FrequencyScaleComputer;
import uk.co.modularaudio.util.bufferedimage.AllocationBufferType;
import uk.co.modularaudio.util.bufferedimage.AllocationLifetime;
import uk.co.modularaudio.util.bufferedimage.AllocationMatch;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.bufferedimage.TiledBufferedImage;

public class DoubleImageScrollingCanvas
{
	private static Log log = LogFactory.getLog( DoubleImageScrollingCanvas.class.getName() );
	
	private final SpectralRollMadUiInstance uiInstance;
	
	private int canvasWidth = -1;
	private int canvasHeight = -1;
	private BufferedImageAllocator bufferedImageAllocator = null;
	private TiledBufferedImage[] tiledImages = new TiledBufferedImage[2];
	private BufferedImage[] images = new BufferedImage[2];
	private Graphics2D[] g2ds = new Graphics2D[2];
	private int currentImageNum = 0;
	private int currentPixelIndex = -1;
	
	private int pixelWidth = 2;
	
	// Debugging the scaling
	private float smallestVal = Float.MAX_VALUE;
	private float largestVal = Float.MIN_VALUE;
	
	private int[] pixelColourArray = new int[3];
	
	public DoubleImageScrollingCanvas( SpectralRollMadUiInstance uiInstance,
			int canvasWidth, int canvasHeight )
	{
		this.uiInstance = uiInstance;
		
		this.canvasWidth = canvasWidth;
		this.canvasHeight = canvasHeight;
		this.bufferedImageAllocator = uiInstance.getUiDefinition().getBufferedImageAllocator();
		
		AllocationMatch localMatch = new AllocationMatch();
		
		for( int i = 0 ; i < 2 ; i++ )
		{
			try
			{
				tiledImages[i] = bufferedImageAllocator.allocateBufferedImage( this.getClass().getSimpleName(),
						localMatch,
						AllocationLifetime.SHORT,
						AllocationBufferType.TYPE_INT_RGB,
						canvasWidth, canvasHeight );
				images[i] = tiledImages[ i ].getUnderlyingBufferedImage();
				g2ds[i] = images[i].createGraphics();
			}
			catch ( Exception e)
			{
				String msg = "Exception caught allocating image for scrolling canvas: " + e.toString();
				log.error( msg,  e );
			}
		}
		currentImageNum = 0;
		currentPixelIndex = 0;
		
//		freqScaleComputer = new LogarithmicFreqScaleComputer();
//		ampScaleComputer = new LogarithmicAmpScaleComputer();
	}
	
	public void paint( Graphics2D g2d )
	{
		// Paint the current image one the right so that currentPixelIndex lines up with the right hand side
		// then paint the other image to it's right lined up
		int currentXOffset = canvasWidth - (currentPixelIndex);
		g2d.drawImage( getCurrentBufferedImage(), currentXOffset, 0,  null );
		int otherXOffset = currentXOffset - canvasWidth;
		g2d.drawImage( getOtherBufferedImage(), otherXOffset, 0, null );
	}
	
	public void processNewAmps( float[] amps )
	{
		int incomingNumBins = amps.length;
		BufferedImage bi = getCurrentBufferedImage();
		paintMagsIntoBufferedImageAtIndex( amps, incomingNumBins, bi, currentPixelIndex );
		currentPixelIndex += pixelWidth;
		if( currentPixelIndex > canvasWidth - 1 )
		{
			currentPixelIndex = 0;
			swapImages();
		}
	}
	
	public BufferedImage getCurrentBufferedImage()
	{
		return images[ currentImageNum ];
	}

	public BufferedImage getOtherBufferedImage()
	{
		return images[ (currentImageNum == 0 ? 1 : 0 ) ];
	}

	public void swapImages()
	{
		currentImageNum = ( currentImageNum == 1 ? 0 : 1 );
	}
	
	private void paintMagsIntoBufferedImageAtIndex( float[] mags,
			int numMags,
			BufferedImage spectrumCanvas,
			int pixelIndex )
	{
		FrequencyScaleComputer freqScaleComputer = uiInstance.getDesiredFreqScaleComputer();
		AmpScaleComputer ampScaleComputer = uiInstance.getDesiredAmpScaleComputer();
		
		WritableRaster raster = spectrumCanvas.getRaster();
//		DataBuffer underlyingDataBuffer = raster.getDataBuffer();
//		DataBufferInt intDataBuffer = (DataBufferInt)underlyingDataBuffer;
//		int[] intArray = intDataBuffer.getData();
		for( int i = 0 ; i < canvasHeight; i++ )
		{
			int actualVal = (canvasHeight - 1) - i;
			int pixelYStartIndex = i;

			int binIndex = freqScaleComputer.displayBinToSpectraBin( numMags, canvasHeight, actualVal );
			float rawVal = mags[ binIndex ];
			if( rawVal < smallestVal )
			{
				smallestVal = rawVal;
			}
			
			if( rawVal > largestVal )
			{
				largestVal = rawVal;
			}
			
			float scaledVal = ampScaleComputer.scaleIt( rawVal );

//			rgb = newColourFor( scaledVal );
			setPixelColourArrayFor( scaledVal );

			for( int p = 0 ; p < pixelWidth ; p++ )
			{
//				spectrumCanvas.setRGB( pixelIndex + p, pixelYStartIndex, rgb);
				raster.setPixel( pixelIndex + p, pixelYStartIndex, pixelColourArray );
			}
		}
//		log.debug( "SV(" + smallestVal + ") LV(" + largestVal + ")");
	}
	
	private void setPixelColourArrayFor( float val )
	{
		// Is between 0 -> 1
		// Divide into two sections
		// 0 -> 0.5 up to 255 grey
		// 0.5 -> 1.0 from grey to red
		int red = 0;
		int green = 0;
		int blue = 0;
		if( val < 0.0f )
		{
		}
		else if( val < 0.5f )
		{
			int greyLevel = (int)( (val * 2) * 255);
			red = greyLevel;
			green = greyLevel;
			blue = greyLevel;
		}
		else if( val <= 1.0f )
		{
			int redAmount = (int)(((val - 0.5f) * 2) * 255);
			int greyLevel = 255 - redAmount;
			red = 255;
			green = greyLevel;
			blue = greyLevel;
		}
		else
		{
			red = 255;
			green = 0;
			blue = 0;
		}
		pixelColourArray[0] = red;
		pixelColourArray[1] = green;
		pixelColourArray[2] = blue;
	}
	
	public void clear()
	{
		g2ds[0].clearRect( 0, 0, canvasWidth, canvasHeight );
		g2ds[1].clearRect( 0, 0, canvasWidth, canvasHeight );
	}
	
	public void destroy()
	{
		try
		{
			if( tiledImages[0] != null )
			{
				bufferedImageAllocator.freeBufferedImage( tiledImages[0] );
				images[0] = null;
				g2ds[0] = null;
			}
			if( tiledImages[1] != null )
			{
				bufferedImageAllocator.freeBufferedImage( tiledImages[1] );
				images[1] = null;
				g2ds[1] = null;
			}
			
		}
		catch(Exception e)
		{
			String msg = "Failed to release tiled images: " + e.toString();
			log.error( msg, e );
		}
	}
}
