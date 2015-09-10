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

package uk.co.modularaudio.mads.base.scope.ui.display;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Arrays;

import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.scope.mu.ScopeMadDefinition;
import uk.co.modularaudio.mads.base.scope.ui.CaptureLengthListener;
import uk.co.modularaudio.mads.base.scope.ui.ScopeColours;
import uk.co.modularaudio.mads.base.scope.ui.ScopeDataVisualiser;
import uk.co.modularaudio.mads.base.scope.ui.ScopeDisplayUiJComponent;
import uk.co.modularaudio.mads.base.scope.ui.ScopeMadUiInstance;
import uk.co.modularaudio.mads.base.specampgen.ui.SampleRateListener;
import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.mvc.displayslider.models.LogarithmicTimeMillis1To1000SliderModel;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;

public class ScopeWaveDisplay extends JPanel
	implements ScopeDataVisualiser, CaptureLengthListener, SampleRateListener
{
	private static final long serialVersionUID = 3612260008902851339L;

	private static Log log = LogFactory.getLog( ScopeWaveDisplay.class.getName() );

	private final ScopeMadUiInstance uiInstance;
	private boolean previouslyShowing;

	private final int numTimeMarkers;
	private final int numAmpMarkers;

	// Setup when setBounds is called.
	private int width;
	private int height;

	private int magsWidth;
	private int magsHeight;

	private int yOffset;

	private int horizPixelsPerMarker;
	private int vertPixelsPerMarker;

	private static final Color[] VIS_COLOURS = new Color[ScopeMadDefinition.NUM_VIS_CHANNELS];

	static
	{
		// Red
		VIS_COLOURS[0] = Color.decode( "#FF5555" );
		// Green
		VIS_COLOURS[1] = Color.decode( "#55FF55" );
		// Blue
		VIS_COLOURS[2] = Color.decode( "#5555FF" );
		// Purple
		VIS_COLOURS[3] = Color.decode( "#FF55FF" );
	}

	private final float[][] internalChannelBuffers = new float[ScopeMadDefinition.NUM_VIS_CHANNELS][];

	private final int[][] channelValues = new int[ScopeMadDefinition.NUM_VIS_CHANNELS][];

	private int captureLengthSamples;

	public ScopeWaveDisplay( final ScopeMadUiInstance uiInstance,
			final int numTimeMarkers,
			final int numAmpMarkers )
	{
		this.uiInstance = uiInstance;
		this.numTimeMarkers = numTimeMarkers;
		this.numAmpMarkers = numAmpMarkers;

		setOpaque( true );
		setBackground( ScopeColours.BACKGROUND_COLOR );

		uiInstance.addCaptureLengthListener( this );
		uiInstance.setScopeDataVisualiser( this );

		final int maxSamples = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( DataRate.CD_QUALITY.getValue(),
				LogarithmicTimeMillis1To1000SliderModel.MAX_MILLIS );
		setupInternalChannelBuffers( maxSamples );
	}

	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime )
	{
		final boolean showing = isShowing();

		if( previouslyShowing != showing )
		{
			uiInstance.sendUiActive( showing );
			previouslyShowing = showing;
		}
	}

	private void paintGridLines( final Graphics g )
	{
		g.setColor( ScopeColours.SCOPE_AXIS_DETAIL );

		// Draw the axis lines
		for( int i = 0 ; i < numAmpMarkers ; ++i )
		{
			final int lineY = (vertPixelsPerMarker * i);
			g.drawLine( 0, lineY, magsWidth - 1, lineY );
		}

		for( int j = 0 ; j < numTimeMarkers ; ++j )
		{
			final int lineX = horizPixelsPerMarker * j;
			g.drawLine( lineX, 0, lineX, magsHeight );
		}
	}

	private void paintData( final Graphics g )
	{
		for( int channel = 0 ; channel < ScopeMadDefinition.NUM_VIS_CHANNELS ; ++channel )
		{
			g.setColor( VIS_COLOURS[channel] );

			int previousMinY = -1;
			int previousMaxY = -1;
			for( int x = 0 ; x < magsWidth ; ++x )
			{
				final int minY = channelValues[channel][x*2];
				final int maxY = channelValues[channel][(x*2)+1];

				int drawMinY = minY;
				int drawMaxY = maxY;
				if( previousMinY != -1 )
				{
					if( previousMinY > drawMaxY )
					{
						drawMaxY = previousMinY;
					}

					if( previousMaxY < drawMinY )
					{
						drawMinY = previousMaxY;
					}
				}

				g.drawLine( x, magsHeight - drawMinY, x, magsHeight - drawMaxY );

				previousMinY = minY;
				previousMaxY = maxY;
			}
		}
	}

	@Override
	public void paint( final Graphics g )
	{
		g.setColor( ScopeColours.BACKGROUND_COLOR );
		g.fillRect( 0, 0, width, height );

		g.setColor( ScopeColours.SCOPE_BODY );

		g.translate( 0, yOffset );

		paintGridLines( g );

		paintData( g );

		g.translate( 0, -yOffset );
	}

	private void setupInternalBuffersFromSize( final int width, final int height )
	{
		this.width = width - 1;
		this.height = height - 1;

		magsWidth = ScopeDisplayUiJComponent.getAdjustedWidthOfDisplay( this.width, numTimeMarkers );
		magsHeight = ScopeDisplayUiJComponent.getAdjustedHeightOfDisplay( this.height, numAmpMarkers );

		yOffset = this.height - magsHeight;

		horizPixelsPerMarker = ScopeDisplayUiJComponent.getAdjustedWidthBetweenMarkers( this.width, numTimeMarkers );
		vertPixelsPerMarker = ScopeDisplayUiJComponent.getAdjustedHeightBetweenMarkers( this.height, numAmpMarkers );

		for( int c = 0 ; c < ScopeMadDefinition.NUM_VIS_CHANNELS ; ++c )
		{
			// Enough space for a min and max per point
			channelValues[c] = new int[ 2 * magsWidth ];
			Arrays.fill( channelValues[c], magsHeight / 2 );
		}
	}

	@Override
	public void setBounds( final Rectangle r )
	{
		super.setBounds( r );
		setupInternalBuffersFromSize( r.width, r.height );
	}

	@Override
	public void setBounds( final int x, final int y, final int width, final int height )
	{
		super.setBounds( x, y, width, height );
		setupInternalBuffersFromSize( width, height );
	}

	@Override
	public void visualiseScopeBuffers( final float[][] frontEndBuffers )
	{
		for( int c = 0 ; c < ScopeMadDefinition.NUM_VIS_CHANNELS ; ++c )
		{
			if( frontEndBuffers[c].length != internalChannelBuffers[c].length )
			{
				if( log.isErrorEnabled() )
				{
					log.error("Buffer lengths don't match feb.length(" +
							frontEndBuffers[c].length + ") vs icb.length(" +
							internalChannelBuffers[c].length + ")");
				}
			}
			else
			{
				System.arraycopy( frontEndBuffers[c], 0, internalChannelBuffers[c], 0, frontEndBuffers[c].length );
			}
		}
		calculateChannelValues( internalChannelBuffers );
	}

	private void calculateChannelValues( final float[][] channelBuffers )
	{
		final float numSamplesPerPixel = captureLengthSamples / (float)magsWidth;

//		log.trace("Would attempt to revisualise with " + numSamplesPerPixel + " samples per pixel" );

		if( numSamplesPerPixel <= 1.0f )
		{
			for( int channel = 0 ; channel < ScopeMadDefinition.NUM_VIS_CHANNELS ; ++channel )
			{
				for( int x = 0 ; x < magsWidth ; ++x )
				{
					int startOffset = Math.round(x * numSamplesPerPixel);
					startOffset = (startOffset < 0 ? 0 : (startOffset >= captureLengthSamples ? captureLengthSamples - 1 : startOffset ) );
					float min = channelBuffers[channel][startOffset];
					float max = min;

					// Now convert to normalised value
					min = (min + 1.0f) / 2.0f;
					max = (max + 1.0f) / 2.0f;
					min = (min < 0.0f ? 0.0f : (min > 1.0f ? 1.0f : min));
					max = (max < 0.0f ? 0.0f : (max > 1.0f ? 1.0f : max));
					// And to pixel offset
					channelValues[channel][x*2] = (int)(min * magsHeight);
					channelValues[channel][(x*2)+1] = (int)(max * magsHeight);
				}
			}
		}
		else
		{
			for( int channel = 0 ; channel < ScopeMadDefinition.NUM_VIS_CHANNELS ; ++channel )
			{
				for( int x = 0 ; x < magsWidth ; ++x )
				{
					final int startOffset = (int)(x * numSamplesPerPixel);
					final int endOffset = (int)((x+1) * numSamplesPerPixel);

					float min = 1.1f;
					float max = -1.1f;

					for( int s = startOffset ; s < endOffset ; ++s )
					{
						final float val = channelBuffers[channel][s];
						if( val > max )
						{
							max = val;
						}
						if( val < min )
						{
							min = val;
						}
					}
					// Now convert to normalised value
					min = (min + 1.0f) / 2.0f;
					max = (max + 1.0f) / 2.0f;
					min = (min < 0.0f ? 0.0f : (min > 1.0f ? 1.0f : min));
					max = (max < 0.0f ? 0.0f : (max > 1.0f ? 1.0f : max));
					// And to pixel offset
					channelValues[channel][x*2] = (int)(min * magsHeight);
					channelValues[channel][(x*2)+1] = (int)(max * magsHeight);
				}
			}
		}
		repaint();
	}

	@Override
	public void receiveCaptureLengthMillis( final float captureMillis )
	{
		// Don't care
	}

	@Override
	public void receiveCaptureLengthSamples( final int captureSamples )
	{
//		log.trace("Received capture length samples of " + captureSamples );
		this.captureLengthSamples = captureSamples;
		calculateChannelValues( internalChannelBuffers );
		repaint();
	}

	@Override
	public void receiveSampleRateChange( final int sampleRate )
	{
		final int maxSamples = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( sampleRate,
				LogarithmicTimeMillis1To1000SliderModel.MAX_MILLIS );
		setupInternalChannelBuffers( maxSamples );
	}

	private final void setupInternalChannelBuffers( final int maxSamples )
	{
		if( internalChannelBuffers[0] == null ||
				internalChannelBuffers[0].length != maxSamples )
		{
			for( int c = 0 ; c < ScopeMadDefinition.NUM_VIS_CHANNELS ; ++c )
			{
				internalChannelBuffers[c] = new float[maxSamples];
			}
		}
	}
}
