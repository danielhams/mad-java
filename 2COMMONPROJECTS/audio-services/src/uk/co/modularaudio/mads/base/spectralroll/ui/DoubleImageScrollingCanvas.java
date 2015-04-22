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

import uk.co.modularaudio.util.audio.spectraldisplay.ampscale.AmpScaleComputer;
import uk.co.modularaudio.util.audio.spectraldisplay.freqscale.FrequencyScaleComputer;
import uk.co.modularaudio.util.bufferedimage.AllocationBufferType;
import uk.co.modularaudio.util.bufferedimage.AllocationLifetime;
import uk.co.modularaudio.util.bufferedimage.AllocationMatch;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.bufferedimage.TiledBufferedImage;

public class DoubleImageScrollingCanvas
{
	private static Log log = LogFactory.getLog( DoubleImageScrollingCanvas.class.getName() );

	private final SpectralRollMadUiInstance uiInstance;

	private final int canvasWidth;
	private final int canvasHeight;
	private final BufferedImageAllocator bufferedImageAllocator;
	private final TiledBufferedImage[] tiledImages = new TiledBufferedImage[2];
	private final BufferedImage[] images = new BufferedImage[2];
	private final Graphics2D[] g2ds = new Graphics2D[2];
	private int currentImageNum;
	private int currentPixelIndex;

	private final static int PIXEL_WIDTH = 2;

	// Debugging the scaling
	private float smallestVal = Float.MAX_VALUE;
	private float largestVal = Float.MIN_VALUE;

	private int[][] prevColourArray;
	private final int[] pixelColourArray = new int[3];
	private final int[] mixColourArray = new int[3];

	public DoubleImageScrollingCanvas( final SpectralRollMadUiInstance uiInstance,
			final int canvasWidth, final int canvasHeight )
	{
		this.uiInstance = uiInstance;

		this.canvasWidth = canvasWidth;
		this.canvasHeight = canvasHeight;
		this.bufferedImageAllocator = uiInstance.getUiDefinition().getBufferedImageAllocator();

		final AllocationMatch localMatch = new AllocationMatch();

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
			catch ( final Exception e)
			{
				final String msg = "Exception caught allocating image for scrolling canvas: " + e.toString();
				log.error( msg,  e );
			}
		}
		currentImageNum = 0;
		currentPixelIndex = 0;

//		freqScaleComputer = new LogarithmicFreqScaleComputer();
//		ampScaleComputer = new LogarithmicAmpScaleComputer();
	}

	public void paint( final Graphics2D g2d )
	{
		// Paint the current image one the right so that currentPixelIndex lines up with the right hand side
		// then paint the other image to it's right lined up
		final int currentXOffset = canvasWidth - (currentPixelIndex);
		g2d.drawImage( getCurrentBufferedImage(), currentXOffset, 0,  null );
		final int otherXOffset = currentXOffset - canvasWidth;
		g2d.drawImage( getOtherBufferedImage(), otherXOffset, 0, null );
	}

	public void processNewAmps( final float[] amps )
	{
		final int incomingNumBins = amps.length;
		final BufferedImage bi = getCurrentBufferedImage();
		paintMagsIntoBufferedImageAtIndex( amps, incomingNumBins, bi, currentPixelIndex );
		currentPixelIndex += PIXEL_WIDTH;
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

	private void paintMagsIntoBufferedImageAtIndex( final float[] mags,
			final int numMags,
			final BufferedImage spectrumCanvas,
			final int pixelIndex )
	{
		if( prevColourArray == null || prevColourArray.length != canvasHeight )
		{
			prevColourArray = new int[canvasHeight][3];
		}
		final FrequencyScaleComputer freqScaleComputer = uiInstance.getDesiredFreqScaleComputer();
		final AmpScaleComputer ampScaleComputer = uiInstance.getDesiredAmpScaleComputer();

		final WritableRaster raster = spectrumCanvas.getRaster();
//		DataBuffer underlyingDataBuffer = raster.getDataBuffer();
//		DataBufferInt intDataBuffer = (DataBufferInt)underlyingDataBuffer;
//		int[] intArray = intDataBuffer.getData();
		for( int i = 0 ; i < canvasHeight; i++ )
		{
			final int actualVal = (canvasHeight - 1) - i;
			final int pixelYStartIndex = i;

			final int binIndex = freqScaleComputer.displayBinToSpectraBin( numMags, canvasHeight, actualVal );
			final float rawVal = mags[ binIndex ];
			if( rawVal < smallestVal )
			{
				smallestVal = rawVal;
			}

			if( rawVal > largestVal )
			{
				largestVal = rawVal;
			}

			final float scaledVal = ampScaleComputer.scaleIt( rawVal );

//			rgb = newColourFor( scaledVal );
			setPixelColourArrayFor( scaledVal );

			for( int p = 0 ; p < PIXEL_WIDTH ; ++p )
			{
				final float ratio = ( p == (PIXEL_WIDTH - 1) ?
						1.0f : (p+1/(float)PIXEL_WIDTH) );
//				log.debug("For p = " + p + " ratio is " + ratio );

				for( int c = 0 ; c < 3 ; ++c )
				{
					mixColourArray[c] = (int)((prevColourArray[i][c] * (1.0f-ratio)) +
							pixelColourArray[c] * ratio);
				}
				raster.setPixel( pixelIndex + p, pixelYStartIndex, mixColourArray );
			}
			for( int c = 0 ; c < 3 ; ++c )
			{
				prevColourArray[i][c] = pixelColourArray[c];
			}

//			for( int p = 0 ; p < PIXEL_WIDTH ; ++p )
//			{
//				if( p == 0 ) continue;
//				raster.setPixel( pixelIndex + p, pixelYStartIndex, pixelColourArray );
//			}
		}
//		log.debug( "SV(" + smallestVal + ") LV(" + largestVal + ")");
	}

	private void setPixelColourArrayFor( final float val )
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
			final int greyLevel = (int)( (val * 2) * 255);
			red = greyLevel;
			green = greyLevel;
			blue = greyLevel;
		}
		else if( val <= 1.0f )
		{
			final int redAmount = (int)(((val - 0.5f) * 2) * 255);
			final int greyLevel = 255 - redAmount;
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
		catch(final Exception e)
		{
			final String msg = "Failed to release tiled images: " + e.toString();
			log.error( msg, e );
		}
	}
}
