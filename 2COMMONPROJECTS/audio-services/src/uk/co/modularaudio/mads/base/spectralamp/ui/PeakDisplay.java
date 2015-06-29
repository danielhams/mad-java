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

package uk.co.modularaudio.mads.base.spectralamp.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mahout.math.map.OpenIntObjectHashMap;

import uk.co.modularaudio.mads.base.spectralamp.mu.SpectralAmpMadDefinition;
import uk.co.modularaudio.util.audio.spectraldisplay.ampscale.AmpScaleComputer;
import uk.co.modularaudio.util.audio.spectraldisplay.freqscale.FrequencyScaleComputer;
import uk.co.modularaudio.util.audio.spectraldisplay.runav.RunningAverageComputer;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.bufferedimage.TiledBufferedImage;

public class PeakDisplay
{
	private static Log log = LogFactory.getLog( PeakDisplay.class.getName() );

	private final int canvasWidth;
	private final int canvasHeight;

	private final BufferedImageAllocator bufferImageAllocator;
	private TiledBufferedImage tiledBufferedImage;
	private BufferedImage image;
	private Graphics2D imageGraphics2d;

	private final SpectralAmpMadUiInstance uiInstance;

	private final int maxNumBins;
	private final float[] runningBinPeaks;
	private final float[] previousBinPeaks;

	private int currentNumBins;
	private final float[] computedBins;

	private final BasicStroke wideLineStroke;
	private Stroke originalStroke;

	public PeakDisplay( final Component parent, final int canvasWidth, final int canvasHeight, final SpectralAmpMadUiInstance uiInstance )
	{
		this.bufferImageAllocator = uiInstance.getUiDefinition().getBufferedImageAllocator();

		this.canvasWidth = canvasWidth;
		this.canvasHeight = canvasHeight;

		this.uiInstance = uiInstance;

		this.maxNumBins = SpectralAmpMadDefinition.MAX_NUM_FFT_BINS;
		runningBinPeaks = new float[ maxNumBins ];
		previousBinPeaks = new float[ maxNumBins ];
		computedBins = new float[ maxNumBins ];

		wideLineStroke = new BasicStroke( 2 );
	}

	public void paint( final Graphics2D g2d )
	{
		if( image != null )
		{
			final int currentXOffset = 0;
			g2d.drawImage( image, currentXOffset, 0,  null );
		}
		else
		{
			g2d.setColor( SpectralAmpColours.BACKGROUND_COLOR );
//			g2d.setColor( SpectralAmpColours.RUNNING_PEAK_COLOR );
			g2d.fillRect( 0, 0, canvasWidth, canvasHeight );
		}
	}

	private void checkForImage()
	{
		if( image == null )
		{
			image = new BufferedImage( canvasWidth, canvasHeight, BufferedImage.TYPE_INT_RGB );
			imageGraphics2d = image.createGraphics();
			originalStroke = imageGraphics2d.getStroke();
		}
	}

	public void processNewAmps( final float[] amps )
	{
//		log.trace("Processing new amps");
		final int incomingNumBins = amps.length;
		if( incomingNumBins != currentNumBins )
		{
			clear();
			currentNumBins = incomingNumBins;
		}
		else
		{
			System.arraycopy( amps, 0, computedBins, 0, incomingNumBins );
			recomputeRunningPeaks();
			paintPeaksIntoBufferedImage( computedBins, incomingNumBins );
		}
	}

	protected void clear()
	{
		Arrays.fill( runningBinPeaks, 0.0f );
		Arrays.fill( previousBinPeaks, 0.0f );
		Arrays.fill( computedBins, 0.0f );
		clearBufferedImage();
	}

	private void recomputeRunningPeaks()
	{
		final RunningAverageComputer runAvComputer = uiInstance.getDesiredRunningAverageComputer();

		// add in previousBinPeaks to the running bin peaks using some weight
		runAvComputer.computeNewRunningAverages( currentNumBins, computedBins, runningBinPeaks );
	}

	private final int[] xPoints = new int[3];
	private final int[] yPoints = new int[3];

	private final int[] dxs = new int[3];
	private final int[] dys = new int[3];

	private void paintPeaksIntoBufferedImage( final float[] bins, final int numBins )
	{
		clearBufferedImage();

		paintMags( bins, numBins, true );
		System.arraycopy( bins, 0, previousBinPeaks, 0, maxNumBins );
		paintMags( runningBinPeaks, numBins, false );
	}

	private void clearBufferedImage()
	{
		checkForImage();
		imageGraphics2d.setColor( Color.BLACK );
		imageGraphics2d.fillRect( 0, 0, canvasWidth, canvasHeight );
	}

	private void paintMags(final float[] bins, final int numBins, final boolean drawSolid )
	{
		final AmpScaleComputer ampScaleComputer = uiInstance.getDesiredAmpScaleComputer();
		final FrequencyScaleComputer freqScaleComputer = uiInstance.getDesiredFreqScaleComputer();

		xPoints[0] = -1;
		xPoints[1] = -1;
		xPoints[2] = -1;
		yPoints[0] = -1;
		yPoints[1] = -1;
		yPoints[2] = -1;

		int originx = 0;
		int originy = canvasHeight;

		int previousBinDrawn = -1;

		if( drawSolid )
		{
			imageGraphics2d.setStroke( originalStroke );
		}
		else
		{
			imageGraphics2d.setStroke( wideLineStroke );
		}

//		WritableRaster wr = image.getRaster();
//		DataBuffer db = wr.getDataBuffer();
//		DataBufferInt dbi = (DataBufferInt)db;
//		int[] rawPixelData = dbi.getData();

		for( int i = 0 ; i < canvasWidth ; i++ )
		{
			final int whichBin = freqScaleComputer.displayBinToSpectraBin( numBins, canvasWidth, i );

			if( whichBin != previousBinDrawn )
			{
				xPoints[0]= xPoints[1];
				yPoints[0] = yPoints[1];
				xPoints[1] = xPoints[2];
				yPoints[1] = yPoints[2];

				previousBinDrawn = whichBin;

				final float valForBin = bins[ whichBin ];
				xPoints[2] = i;
				final float ampScaledValue = ampScaleComputer.scaleIt( valForBin );
				final int windowY = windowScaleY( ampScaledValue );
				yPoints[2] = windowY;

				if( xPoints[1] != -1 )
				{
					dxs[0] = originx;
					dys[0] = originy;
					dxs[1] = xPoints[1];
					dys[1] = yPoints[1];
					dxs[2] = xPoints[2];
					dys[2] = yPoints[2];

					if( dys[0] > 0 && dys[1] > 0 && dys[2] > 0 )
					{
						Color color = null;

						if( !drawSolid )
						{
							color = SpectralAmpColours.RUNNING_PEAK_COLOR;
						}
						else
						{
							// Calculate a colour from the log value
							color = colorFor( ampScaledValue );
						}
						imageGraphics2d.setColor( color );
						if( drawSolid )
						{
							// If we decompose into line drawing, draw line, not a polygon
							fillPolygonOrDrawLine( imageGraphics2d, dxs, dys, 3 );


//							imageGraphics2d.fillPolygon( dxs, dys, 3 );
	//						fillPolygon( rawPixelData, dxs, dys, 3, intPackedColor );
	//						fillWritableRasterPolygon( wr, dxs, dys, 3, intPackedColor );
							dxs[0] = originx;
							dys[0] = originy;
							dxs[1] = xPoints[2];
							dys[1] = yPoints[2];
							dxs[2] = xPoints[2];
							dys[2] = canvasHeight;
//							imageGraphics2d.fillPolygon( dxs, dys, 3 );
	//						fillPolygon( rawPixelData, dxs, dys, 3, intPackedColor );
	//						fillWritableRasterPolygon( wr, dxs, dys, 3, intPackedColor );

							fillPolygonOrDrawLine( imageGraphics2d, dxs, dys, 3 );
						}
						else
						{
							// Running peaks, just draw lines
							imageGraphics2d.drawLine(xPoints[1], yPoints[1], xPoints[2], yPoints[2] );
						}
					}
					originx = xPoints[2];
					originy = canvasHeight;
				}
			}
		}
	}

	private void fillPolygonOrDrawLine( final Graphics2D imageGraphics2d, final int[] dxs, final int[] dys, final int numPoints )
	{
		int firstDiff = dxs[1] - dxs[0];
		firstDiff = (firstDiff < 0 ? -firstDiff : firstDiff );
		int secondDiff = dxs[2] - dxs[0];
		secondDiff = (secondDiff < 0 ? -secondDiff : secondDiff );
		int thirdDiff = dxs[2] - dxs[1];
		thirdDiff = (thirdDiff < 0 ? -thirdDiff : thirdDiff);
		final int maxXDiff = ( firstDiff > secondDiff ? (firstDiff > thirdDiff ? firstDiff : thirdDiff ) : (secondDiff > thirdDiff ? secondDiff : thirdDiff ) );

		if( maxXDiff == 1 )
		{
			final int minY = (dys[0] < dys[1] ? (dys[0] < dys[2] ? dys[0] : dys[2] ) : (dys[1] < dys[2] ? dys[1] : dys[2] ) );
			final int maxY = (dys[0] > dys[1] ? (dys[0] > dys[2] ? dys[0] : dys[2] ) : (dys[1] > dys[2] ? dys[1] : dys[2] ) );
			final int lineX = dxs[0];
			imageGraphics2d.drawLine( lineX, minY, lineX, maxY );
		}
		else
		{
			imageGraphics2d.fillPolygon( dxs, dys, 3 );
		}
	}

	private int windowScaleY( final float ampScaledVal )
	{
		return canvasHeight - (int)(ampScaledVal * canvasHeight );
	}

	private final OpenIntObjectHashMap<Color> colorMap = new OpenIntObjectHashMap<Color>();

	private Color colorFor( final float val )
	{
		final int intVal = newColourFor( val );
		Color retVal = colorMap.get( intVal );
		if( retVal == null )
		{
			retVal = new Color( intVal );
			colorMap.put( intVal, retVal );
		}
		return retVal;
	}

	private int newColourFor( final float val )
	{
		// Is between 0 -> 1
		// Divide into two sections
		// 0 -> 0.5 up to 255 grey
		// 0.5 -> 1.0 from grey to red
		if( val < 0.0f )
		{
			return 0;
		}
		else if( val < 0.5f )
		{
			final int greyLevel = (int)( (val * 2) * 255);
			return (greyLevel << 16) |(greyLevel << 8) | (greyLevel );
		}
		else if( val <= 1.0f )
		{
			final int redAmount = (int)(((val - 0.5f) * 2) * 255);
			final int greyLevel = 255 - redAmount;
			return (255 << 16) | (greyLevel << 8) | (greyLevel);
		}
		else
		{
			return 0xff0000;
		}
	}

	public void destroy()
	{
		if( tiledBufferedImage != null )
		{
			try
			{
				image = null;
				bufferImageAllocator.freeBufferedImage( tiledBufferedImage );
				tiledBufferedImage = null;
			}
			catch( final Exception e )
			{
				final String msg = "Unable to free tiled image: " + e.toString();
				log.error( msg, e );
			}
		}
	}
}
