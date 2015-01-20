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
			OscilloscopeV2MadDefinition definition,
			OscilloscopeV2MadInstance instance,
			OscilloscopeV2MadUiInstance uiInstance,
			int controlIndex )
	{
		this.uiInstance = uiInstance;
		uiInstance.registerScopeDataListener( this );
		
		this.bufferImageAllocator = uiInstance.getUiDefinition().getBufferedImageAllocator();
	}

	@Override
	public void doDisplayProcessing(ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime)
	{
		boolean showing = isShowing();
		
		if( previouslyShowing != showing )
		{
//			log.debug("Display sending ui active(" + showing +")");
			uiInstance.sendUiActive( showing );
			previouslyShowing = showing;
		}
	}
	
	public void paintNewWaveFromSamples( int numSamples,
			boolean data0Connected, float[] samples0,
			boolean data1Connected, float[] samples1 )
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
	public void paint( Graphics g )
	{
		checkImage();

		g.drawImage( outBufferedImage, 0,  0, null );
	}

	private void checkImage()
	{
		int myWidth = getWidth();
		int myHeight = getHeight();
		if( outBufferedImage == null || (outBufferedImage.getWidth() != myWidth || outBufferedImage.getHeight() != myHeight ) )
		{
			try
			{
				AllocationMatch localAllocationMatch = new AllocationMatch();
				tiledBufferedImage = bufferImageAllocator.allocateBufferedImage( this.getClass().getSimpleName(),
						localAllocationMatch,
						AllocationLifetime.SHORT,
						AllocationBufferType.TYPE_INT_RGB,
						myWidth,
						myHeight );
				outBufferedImage = tiledBufferedImage.getUnderlyingBufferedImage();
			}
			catch( Exception e )
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

	private void paintIntoImageFromSamples( Graphics g,
			int numSamples,
			boolean data0Connected, float[] samples0,
			boolean data1Connected, float[] samples1 )
	{
//		Graphics2D g2d = (Graphics2D)g;
//		Rectangle clipBounds = g.getClipBounds();
		Font f = g.getFont();
		Font newFont = f.deriveFont( 12 );
		g.setFont( newFont );
		int x = 0;
		int y = 0;
		
		int width = getWidth();
		int height = getHeight();
		
		int plotWidth = width - (SCALE_WIDTH * 2);
		int plotStart = SCALE_WIDTH;
		int plotHeight = getHeight();

//		g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
		g.setColor( Color.BLACK );
		g.fillRect( x, y, width, height );
		
		int numSamplesInBuffer = numSamples;
		
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

	private void plotAllDataValues( Graphics g,
			int plotWidth,
			int plotStart,
			int plotHeight,
			int numSamplesInBuffer,
			float[] dataToPlot,
			boolean firstMags )
	{
		previousX = -1;
		previousY = -1;
		int maxIndex = numSamplesInBuffer - 1;
		float numSamplesPerPixel = numSamplesInBuffer / (float)plotWidth;
		float numToSkipFloat = numSamplesPerPixel / 4;
		int numToSkipInt = (int)numToSkipFloat;
		numToSkipInt = (numToSkipInt < 1 ? 1 : numToSkipInt );

		for( int i = 0 ; i < maxIndex ; i+= numToSkipInt )
		{
			double pixelIndex = ((float)i / maxIndex) * plotWidth;
			int intPixelIndex = (int)pixelIndex;

			float minPixelVal = Float.MAX_VALUE;
			float maxPixelVal = -99999999999.9f;
			if( numToSkipInt <= 4 )
			{
				float curVal = dataToPlot[ i ];

				int actualY = (int)( ((curVal + 1.0)  / 2.0) * (double)(plotHeight - 1));
				int curX = SCALE_WIDTH + intPixelIndex;
				int curY = (plotHeight - 1) - actualY;
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
					float curVal = dataToPlot[ i + ss ];
					if( curVal < minPixelVal ) minPixelVal = curVal;
					if( curVal > maxPixelVal ) maxPixelVal = curVal;
				}

				// We need it to go from 0 to height
				int actualMinY = (int)( ((minPixelVal + 1.0)  / 2.0) * (double)(plotHeight - 1));
				int actualMaxY = (int)( ((maxPixelVal + 1.0)  / 2.0) * (double)(plotHeight - 1));
				int curX = SCALE_WIDTH + intPixelIndex;
				int curMinY = (plotHeight - 1) - actualMinY;
				int curMaxY = (plotHeight - 1) - actualMaxY;
				g.drawLine( curX, curMinY, curX, curMaxY );
			}

		}
	}
	
	private String previousPositiveMagStr = null;
	private float previousPositiveMag = Float.MAX_VALUE;
	private String previousNegativeMagStr = null;
	private float previousNegativeMag = Float.MAX_VALUE;

	private void drawScale( Graphics g, int height, float maxMag, int xOffset )
	{
		FontMetrics fm = g.getFontMetrics();
		// Do zero, and plus and minus maximum
		int middle = height / 2;
		int halfFontHeight = fm.getAscent() / 2;
		g.drawString( "0.0", xOffset, middle + halfFontHeight );
		int top = 0 + fm.getHeight();
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
		int bottom = height - halfFontHeight;
		g.drawString( previousNegativeMagStr, xOffset, bottom );
	}

	@Override
	public void processScopeData( int numSamples, boolean data0Connected, float[] data0, boolean data1Connected, float[] data1 )
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
			catch( Exception e )
			{
				String msg = "Unable to free tiled image: " + e.toString();
				log.error( msg, e );
			}
		}
		
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return true;
	}
}
