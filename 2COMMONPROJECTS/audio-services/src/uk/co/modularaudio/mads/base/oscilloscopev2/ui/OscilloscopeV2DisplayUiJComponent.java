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

package uk.co.modularaudio.mads.base.oscilloscopev2.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.oscilloscopev2.mu.OscilloscopeV2MadDefinition;
import uk.co.modularaudio.mads.base.oscilloscopev2.mu.OscilloscopeV2MadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.paccontrols.PacComponent;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.bufferedimage.AllocationBufferType;
import uk.co.modularaudio.util.bufferedimage.AllocationLifetime;
import uk.co.modularaudio.util.bufferedimage.AllocationMatch;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.bufferedimage.TiledBufferedImage;
import uk.co.modularaudio.util.math.MathFormatter;

public class OscilloscopeV2DisplayUiJComponent extends PacComponent
	implements IMadUiControlInstance<OscilloscopeV2MadDefinition, OscilloscopeV2MadInstance, OscilloscopeV2MadUiInstance>,
	OscilloscopeV2ScopeDataListener
{
	private static final long serialVersionUID = -1183011558795174539L;

	private static Log log = LogFactory.getLog(  OscilloscopeV2DisplayUiJComponent.class.getName( ) );

	private static final int SCALE_WIDTH = 40;

	private OscilloscopeV2MadUiInstance uiInstance = null;

	private boolean previouslyShowing = false;

	private BufferedImageAllocator bufferImageAllocator = null;
	private TiledBufferedImage tiledBufferedImage = null;
	private BufferedImage outBufferedImage = null;
	private Graphics outBufferedImageGraphics = null;

	public OscilloscopeV2DisplayUiJComponent(
			final OscilloscopeV2MadDefinition definition,
			final OscilloscopeV2MadInstance instance,
			final OscilloscopeV2MadUiInstance uiInstance,
			final int controlIndex )
	{
		this.uiInstance = uiInstance;
		uiInstance.registerScopeDataListener( this );

		this.bufferImageAllocator = uiInstance.getUiDefinition().getBufferedImageAllocator();
	}

	@Override
	public void doDisplayProcessing(final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime)
	{
		final boolean showing = isShowing();

		if( previouslyShowing != showing )
		{
//			log.debug("Display sending ui active(" + showing +")");
			uiInstance.sendUiActive( showing );
			previouslyShowing = showing;
		}
	}

	public void paintNewWaveFromSamples( final int numSamples,
			final boolean data0Connected, final float[] samples0,
			final boolean data1Connected, final float[] samples1 )
	{
		checkImage();

		paintIntoImageFromSamples( outBufferedImageGraphics, numSamples,
				data0Connected, samples0,
				data1Connected, samples1 );
	}

	@Override
	public Component getControl()
	{
		return this;
	}

	@Override
	public void paint( final Graphics g )
	{
		checkImage();

		g.drawImage( outBufferedImage, 0,  0, null );
	}

	private void checkImage()
	{
		final int myWidth = getWidth();
		final int myHeight = getHeight();
		if( outBufferedImage == null || (outBufferedImage.getWidth() != myWidth || outBufferedImage.getHeight() != myHeight ) )
		{
			try
			{
				final AllocationMatch localAllocationMatch = new AllocationMatch();
				tiledBufferedImage = bufferImageAllocator.allocateBufferedImage( this.getClass().getSimpleName(),
						localAllocationMatch,
						AllocationLifetime.SHORT,
						AllocationBufferType.TYPE_INT_RGB,
						myWidth,
						myHeight );
				outBufferedImage = tiledBufferedImage.getUnderlyingBufferedImage();
			}
			catch( final Exception e )
			{
			}

			if( outBufferedImage != null )
			{
				outBufferedImageGraphics = outBufferedImage.createGraphics();
				outBufferedImageGraphics.setColor( Color.black );
				outBufferedImageGraphics.fillRect( 0, 0, myWidth, myHeight );
			}
		}
	}

	private int previousX = -1;
	private int previousY = -1;

	private void paintIntoImageFromSamples( final Graphics g,
			final int numSamples,
			final boolean data0Connected, final float[] samples0,
			final boolean data1Connected, final float[] samples1 )
	{
//		Graphics2D g2d = (Graphics2D)g;
//		Rectangle clipBounds = g.getClipBounds();
		final Font f = g.getFont();
		final Font newFont = f.deriveFont( 12 );
		g.setFont( newFont );
		final int x = 0;
		final int y = 0;

		final int width = getWidth();
		final int height = getHeight();

		final int plotWidth = width - (SCALE_WIDTH * 2);
		final int plotStart = SCALE_WIDTH;
		final int plotHeight = getHeight();

//		g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
		g.setColor( Color.BLACK );
		g.fillRect( x, y, width, height );

		final int numSamplesInBuffer = numSamples;

		if( data0Connected )
		{
			// Float data draw
			g.setColor( Color.RED );
			drawScale( g, height, 1.0f, 0 );
			plotAllDataValues( g, plotWidth, plotStart, plotHeight, numSamplesInBuffer, samples0, true );
		}

		if( data1Connected )
		{
			g.setColor( Color.yellow );
			drawScale( g, height, 1.0f, plotStart + plotWidth );
//			plotAllDataValues( g, plotWidth, plotStart, plotHeight, numSamplesInBuffer, samples1, false );
			plotAllDataValues( g, plotWidth, plotStart, plotHeight, numSamplesInBuffer, samples1, false );
		}

	}

	private void plotAllDataValues( final Graphics g,
			final int plotWidth,
			final int plotStart,
			final int plotHeight,
			final int numSamplesInBuffer,
			final float[] dataToPlot,
			final boolean firstMags )
	{
		previousX = -1;
		previousY = -1;
		final int maxIndex = numSamplesInBuffer - 1;
		final float numSamplesPerPixel = numSamplesInBuffer / (float)plotWidth;
		final float numToSkipFloat = numSamplesPerPixel / 4;
		int numToSkipInt = (int)numToSkipFloat;
		numToSkipInt = (numToSkipInt < 1 ? 1 : numToSkipInt );

		for( int i = 0 ; i < maxIndex ; i+= numToSkipInt )
		{
			final double pixelIndex = ((float)i / maxIndex) * plotWidth;
			final int intPixelIndex = (int)pixelIndex;

			float minPixelVal = Float.MAX_VALUE;
			float maxPixelVal = -99999999999.9f;
			if( numToSkipInt <= 4 )
			{
				final float curVal = dataToPlot[ i ];

				final int actualY = (int)( ((curVal + 1.0)  / 2.0) * (plotHeight - 1));
				final int curX = SCALE_WIDTH + intPixelIndex;
				final int curY = (plotHeight - 1) - actualY;
				if( curX != previousX || curY != previousY )
				{
					if( previousX != -1 )
					{
						g.drawLine( previousX, previousY, curX, curY );
					}
					previousX = curX;
					previousY = curY;
				}
			}
			else
			{
				for( int ss = 0 ; ss < numToSkipInt && (i + ss) < maxIndex ; ss++ )
				{
					final float curVal = dataToPlot[ i + ss ];
					if( curVal < minPixelVal ) minPixelVal = curVal;
					if( curVal > maxPixelVal ) maxPixelVal = curVal;
				}

				// We need it to go from 0 to height
				final int actualMinY = (int)( ((minPixelVal + 1.0)  / 2.0) * (plotHeight - 1));
				final int actualMaxY = (int)( ((maxPixelVal + 1.0)  / 2.0) * (plotHeight - 1));
				final int curX = SCALE_WIDTH + intPixelIndex;
				final int curMinY = (plotHeight - 1) - actualMinY;
				final int curMaxY = (plotHeight - 1) - actualMaxY;
				g.drawLine( curX, curMinY, curX, curMaxY );
			}

		}
	}

	private String previousPositiveMagStr = null;
	private float previousPositiveMag = Float.MAX_VALUE;
	private String previousNegativeMagStr = null;
	private float previousNegativeMag = Float.MAX_VALUE;

	private void drawScale( final Graphics g, final int height, final float maxMag, final int xOffset )
	{
		final FontMetrics fm = g.getFontMetrics();
		// Do zero, and plus and minus maximum
		final int middle = height / 2;
		final int halfFontHeight = fm.getAscent() / 2;
		g.drawString( "0.0", xOffset, middle + halfFontHeight );
		final int top = 0 + fm.getHeight();
		// Only re-create the string when we need to
		if( previousPositiveMagStr == null || previousPositiveMag != maxMag )
		{
//			previousPositiveMagStr = MathFormatter.floatPrint( maxMag, 2 );
			previousPositiveMagStr = MathFormatter.fastFloatPrint( maxMag, 2, true );
			previousPositiveMag = maxMag;
		}
		g.drawString( previousPositiveMagStr, xOffset, top );

		if( previousNegativeMagStr == null || previousNegativeMag != -maxMag )
		{
//			previousNegativeMagStr = MathFormatter.floatPrint( -maxMag, 2 );
			previousNegativeMagStr = MathFormatter.fastFloatPrint( -maxMag, 2, true );
			previousNegativeMag = -maxMag;
		}
		final int bottom = height - halfFontHeight;
		g.drawString( previousNegativeMagStr, xOffset, bottom );
	}

	@Override
	public void processScopeData( final int numSamples, final boolean data0Connected, final float[] data0, final boolean data1Connected, final float[] data1 )
	{
		paintNewWaveFromSamples( numSamples, data0Connected, data0, data1Connected, data1 );
		repaint();
	}

	@Override
	public void destroy()
	{
		if( tiledBufferedImage != null )
		{
			try
			{
				outBufferedImageGraphics = null;
				outBufferedImage = null;
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

	@Override
	public boolean needsDisplayProcessing()
	{
		return true;
	}

	@Override
	public String getControlValue()
	{
		return "";
	}
}
