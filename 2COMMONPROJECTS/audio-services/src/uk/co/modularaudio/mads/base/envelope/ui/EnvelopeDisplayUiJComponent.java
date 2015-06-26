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

package uk.co.modularaudio.mads.base.envelope.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.envelope.mu.Envelope;
import uk.co.modularaudio.mads.base.envelope.mu.EnvelopeDefaults;
import uk.co.modularaudio.mads.base.envelope.mu.EnvelopeMadDefinition;
import uk.co.modularaudio.mads.base.envelope.mu.EnvelopeMadInstance;
import uk.co.modularaudio.mads.base.envelope.mu.EnvelopeRuntime;
import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;
import uk.co.modularaudio.util.bufferedimage.AllocationBufferType;
import uk.co.modularaudio.util.bufferedimage.AllocationLifetime;
import uk.co.modularaudio.util.bufferedimage.AllocationMatch;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.bufferedimage.TiledBufferedImage;


public class EnvelopeDisplayUiJComponent extends JPanel
	implements IMadUiControlInstance<EnvelopeMadDefinition, EnvelopeMadInstance, EnvelopeMadUiInstance>, EnvelopeValueListener
{
	private static final Color DARK_RED_FILL = Color.RED.darker().darker();
	private static final Color ENVELOPE_LIMIT_HIGHLIGHT = Color.RED;

	private static final Color OTHER_WAVE_FILL = Color.GRAY.darker().darker();
	private static final Color OTHER_WAVE_HIGHLIGHT = Color.GRAY;

	private static final long serialVersionUID = -7926073751290765154L;

	private static Log log = LogFactory.getLog( EnvelopeDisplayUiJComponent.class.getName() );

	private final static int SAMPLE_RATE = DataRate.SR_44100.getValue();
	private final static int ENVELOPE_OUTPUT_LENGTH =
			AudioTimingUtils.getNumSamplesForMillisAtSampleRate( SAMPLE_RATE,
					EnvelopeDefaults.MAX_TIMESCALE_MILLIS * 5 );

	private final EnvelopeMadUiInstance uiInstance;
	private final BufferedImageAllocator imageAllocator;

	private boolean needsRepaint;

	private final Envelope uiEnvelope;
	private final Envelope renderingEnvelope = new Envelope();
	private final EnvelopeRuntime renderingEnvelopeRuntime = new EnvelopeRuntime();

	private TiledBufferedImage tiledBufferedImage;
	private BufferedImage envelopeImage;
	private int imageWidth;
	private int imageHeight;
	private Graphics2D envelopeImageGraphics;

	private final float[] envelopeRenderingOutput = new float[ ENVELOPE_OUTPUT_LENGTH ];
	private final float[] envelopeGateOutput = new float[ ENVELOPE_OUTPUT_LENGTH ];
	private int renderedEnvelopeLength;

	public EnvelopeDisplayUiJComponent( final EnvelopeMadDefinition definition,
			final EnvelopeMadInstance instance,
			final EnvelopeMadUiInstance uiInstance,
			final int controlIndex )
	{
		this.uiInstance = uiInstance;
		this.uiEnvelope = uiInstance.getEnvelope();

		imageAllocator = uiInstance.getUiDefinition().getBufferedImageAllocator();

		uiInstance.addEnvelopeListener( this );
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime )
	{
	}

	@Override
	public Component getControl()
	{
		return this;
	}

	@Override
	public void destroy()
	{
		uiInstance.removeEnvelopeListener( this );

		if( tiledBufferedImage != null )
		{
			try
			{
				imageAllocator.freeBufferedImage( tiledBufferedImage );
			}
			catch( final Exception e )
			{
				final String msg = "Exception caught freeing tiled image: " + e.toString();
				log.error( msg, e );
			}
		}
	}

	@Override
	public void paintComponent( final Graphics g )
	{
		final int width = getWidth();
		final int height = getHeight();

		checkForImage( width, height );

		g.setColor( Color.BLACK );
		g.fillRect( 0, 0, width, height );

		if( needsRepaint )
		{
			needsRepaint = false;
			repaintEnvelope();
		}

		if( envelopeImage != null )
		{
			g.drawImage( envelopeImage, 0, 0, null );
		}
	}

	private void checkForImage( final int width, final int height )
	{
		if( envelopeImage == null || (imageWidth != width || imageHeight != height ) )
		{
			try
			{
				final AllocationMatch localAllocationMatch = new AllocationMatch();
				tiledBufferedImage = imageAllocator.allocateBufferedImage( this.getClass().getSimpleName(),
						localAllocationMatch, AllocationLifetime.SHORT, AllocationBufferType.TYPE_INT_RGB, width, height );
				envelopeImage = tiledBufferedImage.getUnderlyingBufferedImage();
				envelopeImageGraphics = envelopeImage.createGraphics();
				imageWidth = width;
				imageHeight = height;
			}
			catch ( final Exception e )
			{
				final String msg = "Exception caught allocation image for adsr: " + e.toString();
				log.error( msg, e );
			}
		}
	}

	private void repaintEnvelope()
	{
		if( isShowing() )
		{
			checkForImage( getWidth(), getHeight() );
		}
		if( envelopeImage != null )
		{
			envelopeImageGraphics.setColor( Color.BLACK );
			envelopeImageGraphics.fillRect( 0, 0, imageWidth, imageHeight );

			envelopeImageGraphics.setColor( ENVELOPE_LIMIT_HIGHLIGHT );

			final int numAttackSamples = uiEnvelope.getAttackSamples();
			final int numDecaySamples = uiEnvelope.getDecaySamples();
			final int numReleaseSamples = (uiEnvelope.getSustainLevel() > 0.0f ? uiEnvelope.getReleaseSamples() : 0 );

			int numEnvSamples = numAttackSamples + numDecaySamples + numReleaseSamples;
			int numSustainSamples;
			int numPreSustainSamples;
			if (numEnvSamples > 0 )
			{
				numSustainSamples = numEnvSamples / 3;
				numPreSustainSamples = numSustainSamples / 2;
			}
			else
			{
				numEnvSamples = 3000;
				numSustainSamples = numEnvSamples;
				numPreSustainSamples = numSustainSamples / 2;
			}

			final int numEnvSamplesIncludingSustain = numAttackSamples + numDecaySamples + numSustainSamples;

			renderedEnvelopeLength = numPreSustainSamples + numEnvSamples + numSustainSamples;

			Arrays.fill( envelopeRenderingOutput, 0.0f );
//			log.debug("Rendered envelope length is " + renderedEnvelopeLength );

			// Basic idea, use the ADSR envelope to actually render with the values we have
			// Then we'll plot from the resulting floats onto the screen.
			renderingEnvelope.setAttackFromZero( false );
			renderingEnvelope.setAttackMillis( 0.0f );
			renderingEnvelope.setDecayMillis( 0.0f );
			renderingEnvelope.setSustainLevel( 0.5f );
			renderingEnvelope.setReleaseMillis( 0.0f );

			renderingEnvelopeRuntime.reset();
			renderingEnvelopeRuntime.trigger( renderingEnvelope );
			renderingEnvelopeRuntime.outputEnvelope( renderingEnvelope, envelopeGateOutput, envelopeRenderingOutput, 0, 7, 7 );

			// Now the envelope should be in sustain, output a bit into the real output at the front
			int curOutputIndex = 0;
			renderingEnvelopeRuntime.outputEnvelope( renderingEnvelope, envelopeGateOutput, envelopeRenderingOutput, curOutputIndex, numPreSustainSamples, numPreSustainSamples );
			curOutputIndex += numPreSustainSamples;

			// Now setup the sustain (as it's taken when the note starts)
			renderingEnvelope.setFromEnvelope( uiEnvelope );
			renderingEnvelopeRuntime.trigger( renderingEnvelope );

			// And output enough up to sustain
			renderingEnvelopeRuntime.outputEnvelope( renderingEnvelope,
					envelopeGateOutput, envelopeRenderingOutput,
					curOutputIndex,
					curOutputIndex + numEnvSamplesIncludingSustain,
					numEnvSamplesIncludingSustain );
			curOutputIndex += numEnvSamplesIncludingSustain;
			renderingEnvelopeRuntime.release( renderingEnvelope );

			if( numReleaseSamples > 0 )
			{
				renderingEnvelopeRuntime.outputEnvelope( renderingEnvelope, envelopeGateOutput, envelopeRenderingOutput,
						curOutputIndex,
						curOutputIndex + numReleaseSamples,
						numReleaseSamples );
			}

			int previousX = -2;
			int previousY = -2;

			int previousSampleIndex = 0;
			boolean drawingUserEnvelope = false;
			for( int i =0 ; i < imageWidth ; i++ )
			{
				final int indexIntoEnvelope = (int)(( i / (float)(imageWidth - 1)) * (renderedEnvelopeLength) );
				float maxValueFromSamples = 0.0f;
				float minValueFromSamples = 1.1f;
				int s = previousSampleIndex;
				do
				{
					final float sample = envelopeRenderingOutput[ s ];
					if( sample > maxValueFromSamples )
					{
						maxValueFromSamples = sample;
					}
					if( sample < minValueFromSamples )
					{
						minValueFromSamples = sample;
					}
					s++;
				}
				while( s < indexIntoEnvelope );

				previousSampleIndex = indexIntoEnvelope;
				if( !drawingUserEnvelope && indexIntoEnvelope >= numPreSustainSamples )
				{
					drawingUserEnvelope = true;
				}

				// Scale it to height
				final int newX = i;
				final int minY = imageHeight - (int)(minValueFromSamples * imageHeight);
				final int maxY = imageHeight - (int)(maxValueFromSamples * imageHeight);
				if( previousX == -2 )
				{
					previousX = newX-1;
					previousY = maxY;
				}
				if( drawingUserEnvelope )
				{
					envelopeImageGraphics.setColor( DARK_RED_FILL );
				}
				else
				{
					envelopeImageGraphics.setColor( OTHER_WAVE_FILL );
				}
				envelopeImageGraphics.drawLine( newX, imageHeight, newX, maxY );
				if( drawingUserEnvelope )
				{
					envelopeImageGraphics.setColor( ENVELOPE_LIMIT_HIGHLIGHT );
				}
				else
				{
					envelopeImageGraphics.setColor( OTHER_WAVE_HIGHLIGHT );
				}
				envelopeImageGraphics.drawLine( previousX, previousY, previousX, maxY );
				envelopeImageGraphics.drawLine( newX, minY, newX, maxY );

				previousX = newX;
				previousY = maxY;
			}

			repaint();
		}
	}

	@Override
	public void receiveEnvelopeChange()
	{
//		log.debug("Received envelope change");

		// Do a repaint
		needsRepaint = true;
		repaint();
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return false;
	}

	@Override
	public String getControlValue()
	{
		return "";
	}

	@Override
	public void receiveControlValue( final String value )
	{
	}
}
