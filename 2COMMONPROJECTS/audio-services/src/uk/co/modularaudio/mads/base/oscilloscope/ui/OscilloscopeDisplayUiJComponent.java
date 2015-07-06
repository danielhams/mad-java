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

package uk.co.modularaudio.mads.base.oscilloscope.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.oscilloscope.mu.OscilloscopeMadDefinition;
import uk.co.modularaudio.mads.base.oscilloscope.mu.OscilloscopeMadInstance;
import uk.co.modularaudio.mads.base.oscilloscope.mu.OscilloscopeWriteableScopeData;
import uk.co.modularaudio.mads.base.oscilloscope.mu.OscilloscopeWriteableScopeData.FloatType;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacPanel;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.bufferedimage.AllocationBufferType;
import uk.co.modularaudio.util.bufferedimage.AllocationLifetime;
import uk.co.modularaudio.util.bufferedimage.AllocationMatch;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.bufferedimage.TiledBufferedImage;
import uk.co.modularaudio.util.math.MathFormatter;

public class OscilloscopeDisplayUiJComponent extends PacPanel
	implements IMadUiControlInstance<OscilloscopeMadDefinition, OscilloscopeMadInstance, OscilloscopeMadUiInstance>,
	ScopeDataListener
{
	private static final long serialVersionUID = -1183011558795174539L;

	private static Log log = LogFactory.getLog(  OscilloscopeDisplayUiJComponent.class.getName( ) );

	private static final int SCALE_WIDTH = 40;

	private final OscilloscopeMadUiInstance uiInstance;

	private float maxMag0 = 0.5f;
	private float maxMag1 = 0.5f;

	private float newMaxMag0 = 0.5f;
	private float newMaxMag1 = 0.5f;

	private boolean previouslyShowing;

	private final BufferedImageAllocator bufferImageAllocator;
	private TiledBufferedImage tiledBufferedImage;
	private BufferedImage outBufferedImage;
	private Graphics outBufferedImageGraphics;

	public OscilloscopeDisplayUiJComponent(
			final OscilloscopeMadDefinition definition,
			final OscilloscopeMadInstance instance,
			final OscilloscopeMadUiInstance uiInstance,
			final int controlIndex )
	{
		setOpaque( true );

		this.uiInstance = uiInstance;
		uiInstance.registerScopeDataListener( this );

		this.bufferImageAllocator = uiInstance.getUiDefinition().getBufferedImageAllocator();
	}

	@Override
	public void doDisplayProcessing(final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime)
	{
//		log.debug("Receiving display tick");
		final boolean showing = isShowing();

		if( previouslyShowing != showing )
		{
//			log.debug("Display sending active of " + showing );
			uiInstance.sendUiActive( showing );
			previouslyShowing = showing;
		}
	}

	public void paintNewWave( final OscilloscopeWriteableScopeData currentScopeData )
	{
		checkImage();

		paintIntoImage( outBufferedImageGraphics, currentScopeData );
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
		if( outBufferedImage == null )
		{
			final int myWidth = getWidth();
			final int myHeight = getHeight();
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
				outBufferedImageGraphics = outBufferedImage.createGraphics();
			}
			catch( final Exception e )
			{
				final String msg = "Unable to allocate tiled image: " + e.toString();
				log.error( msg, e );
			}

		}
	}

	private int previousX = -1;
	private int previousY = -1;

	private void paintIntoImage( final Graphics g, final OscilloscopeWriteableScopeData scopeData )
	{
//		Graphics2D g2d = (Graphics2D)g;
//		Rectangle clipBounds = g.getClipBounds();
		final Font f = g.getFont();
		final Font newFont = f.deriveFont( 12 );
		g.setFont( newFont );
		final int x = 0;
		final int y = 0;

		newMaxMag0 = 0.5f;
		newMaxMag1 = 0.5f;

		final int width = getWidth();
		final int height = getHeight();

		final int plotWidth = width - (SCALE_WIDTH * 2);
		final int plotStart = SCALE_WIDTH;
		final int plotHeight = getHeight();

//		g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
		g.setColor( Color.BLACK );
		g.fillRect( x, y, width, height );

		if( scopeData != null )
		{

			final int numSamplesInBuffer = scopeData.desiredDataLength;

			final float[] float0Data = scopeData.floatBuffer0;
			final FloatType float0Type = scopeData.floatBuffer0Type;
			switch( float0Type )
			{
				case AUDIO:
					maxMag0 = 1.0f;
					break;
				case CV:
					break;
			}
			final boolean displayFloat0 = scopeData.float0Written;

			final float[] float1Data = scopeData.floatBuffer1;
			final FloatType float1Type = scopeData.floatBuffer1Type;
			switch( float1Type )
			{
				case AUDIO:
					maxMag1 = 1.0f;
					break;
				case CV:
					break;
			}
			final boolean displayFloat1 = scopeData.float1Written;


			if( displayFloat0 )
			{
				// Float data draw
				g.setColor( Color.RED );
				drawScale( g, height, maxMag0, 0 );
				plotAllDataValues( g, plotWidth, plotStart, plotHeight, numSamplesInBuffer, float0Data, true );
			}

			if( displayFloat1 )
			{
				// CV data draw
				g.setColor( Color.YELLOW );
				drawScale( g, height, maxMag1, plotStart + plotWidth );
				plotAllDataValues( g, plotWidth, plotStart, plotHeight, numSamplesInBuffer, float1Data, false );
			}
			maxMag0 = newMaxMag0;
			maxMag1 = newMaxMag1;
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
		for( int i = 0 ; i < numSamplesInBuffer ; i++ )
		{
			final double pixelIndex = ((float)i / numSamplesInBuffer) * plotWidth;
			final int intPixelIndex = (int)pixelIndex;
			final float origVal = dataToPlot[ i ];
			final float scaledVal = origVal / (firstMags ? maxMag0 : maxMag1 );
//				log.debug("MaxMag0 is " + maxMag0);
			// Curval goes from +1.0 to -1.0
			// We need it to go from 0 to height

			final int actualY = (int)( ((scaledVal + 1.0)  / 2.0) * (plotHeight - 1));
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
			final float absY = Math.abs( origVal );
			if( firstMags )
			{
				if( absY > newMaxMag0 )
				{
					newMaxMag0 = absY;
				}
			}
			else
			{
				if( absY > newMaxMag1 )
				{
					newMaxMag1 = absY;
				}
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
			previousPositiveMagStr = MathFormatter.fastFloatPrint( maxMag, 2, true );
			previousPositiveMag = maxMag;
		}
		g.drawString( previousPositiveMagStr, xOffset, top );

		if( previousNegativeMagStr == null || previousNegativeMag != -maxMag )
		{
			previousNegativeMagStr = MathFormatter.fastFloatPrint( -maxMag, 2, true );
			previousNegativeMag = -maxMag;
		}
		final int bottom = height - halfFontHeight;
		g.drawString( previousNegativeMagStr, xOffset, bottom );
	}

	@Override
	public void processScopeData( final OscilloscopeWriteableScopeData scopeData )
	{
//		log.debug("Painting new wave");
		paintNewWave( scopeData );
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
				final String msg = "Exception caught freeing tiled image: " + e.toString();
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
	public void setCaptureTimeMillis( final float captureMillis )
	{
	}
}
