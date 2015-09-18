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

package uk.co.modularaudio.mads.base.scopegen.ui.display;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Arrays;

import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.scopegen.mu.ScopeGenMadDefinition;
import uk.co.modularaudio.mads.base.scopegen.mu.ScopeGenMadInstance;
import uk.co.modularaudio.mads.base.scopegen.ui.ScopeGenCaptureLengthListener;
import uk.co.modularaudio.mads.base.scopegen.ui.ScopeGenColours;
import uk.co.modularaudio.mads.base.scopegen.ui.ScopeGenDataVisualiser;
import uk.co.modularaudio.mads.base.scopegen.ui.ScopeGenDisplayUiJComponent;
import uk.co.modularaudio.mads.base.scopegen.ui.ScopeGenMadUiInstance;
import uk.co.modularaudio.mads.base.scopegen.ui.ScopeGenSampleRateListener;
import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.mvc.displayslider.models.LogarithmicTimeMillis1To1000SliderModel;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;

public class ScopeWaveDisplay<D extends ScopeGenMadDefinition<D, I>,
	I extends ScopeGenMadInstance<D, I>,
	U extends ScopeGenMadUiInstance<D, I>>
	extends JPanel
	implements ScopeGenDataVisualiser, ScopeGenCaptureLengthListener, ScopeGenSampleRateListener
{
	private static final long serialVersionUID = 3612260008902851339L;

	private static Log log = LogFactory.getLog( ScopeWaveDisplay.class.getName() );

	private final U uiInstance;
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

	// Whether we draw the negative axis (bipolar, true)
	// or just the postive axis (unipolar, false)
	private boolean biUniPolar = true;

	public static final Color[] VIS_COLOURS = new Color[ScopeGenMadDefinition.NUM_VIS_CHANNELS];

	static
	{
		// Trigger + four signals
		VIS_COLOURS[0] = Color.decode( "#d3d3d3" );
		VIS_COLOURS[1] = Color.decode( "#d31b00" );
		VIS_COLOURS[2] = Color.decode( "#d38c00" );
		VIS_COLOURS[3] = Color.decode( "#c1d300" );
		VIS_COLOURS[4] = Color.decode( "#08af00" );

		// Alternate colour scheme
//		VIS_COLOURS[0] = Color.decode( "#d3d3d3" );
//		VIS_COLOURS[1] = Color.decode( "#d31b00" );
//		VIS_COLOURS[2] = Color.decode( "#bed300" );
//		VIS_COLOURS[3] = Color.decode( "#00d37d" );
//		VIS_COLOURS[4] = Color.decode( "#004dd3" );
	}

	private final float[][] internalChannelBuffers = new float[ScopeGenMadDefinition.NUM_VIS_CHANNELS][];

	private final int[][] channelValues = new int[ScopeGenMadDefinition.NUM_VIS_CHANNELS][];

	private int sampleRate = DataRate.CD_QUALITY.getValue();
	private int maxCaptureSamples = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( sampleRate,
			LogarithmicTimeMillis1To1000SliderModel.MAX_MILLIS );
	private int captureLengthSamples = AudioTimingUtils.getNumSamplesForMillisAtSampleRate(
			sampleRate, LogarithmicTimeMillis1To1000SliderModel.DEFAULT_MILLIS );

	private final boolean[] signalVisibility = new boolean[5];

	public ScopeWaveDisplay( final U uiInstance,
			final int numTimeMarkers,
			final int numAmpMarkers )
	{
		this.uiInstance = uiInstance;
		this.numTimeMarkers = numTimeMarkers;
		this.numAmpMarkers = numAmpMarkers;

		setOpaque( true );
		setBackground( ScopeGenColours.BACKGROUND_COLOR );

		uiInstance.addCaptureLengthListener( this );
		uiInstance.setScopeDataVisualiser( this );

		setupInternalChannelBuffers();

		Arrays.fill( signalVisibility, true );

		uiInstance.addSampleRateListener(this);
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
		g.setColor( ScopeGenColours.SCOPE_AXIS_DETAIL );

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
		for( int channel = 0 ; channel < ScopeGenMadDefinition.NUM_VIS_CHANNELS ; ++channel )
		{
			if( !signalVisibility[channel] ) continue;

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
		g.setColor( ScopeGenColours.BACKGROUND_COLOR );
		g.fillRect( 0, 0, width, height );

		g.setColor( ScopeGenColours.SCOPE_BODY );

		g.translate( 0, yOffset );

		paintGridLines( g );

		paintData( g );

		g.translate( 0, -yOffset );
	}

	private void setupInternalBuffersFromSize( final int width, final int height )
	{
		this.width = width - 1;
		this.height = height - 1;

		magsWidth = ScopeGenDisplayUiJComponent.getAdjustedWidthOfDisplay( this.width, numTimeMarkers );
		magsHeight = ScopeGenDisplayUiJComponent.getAdjustedHeightOfDisplay( this.height, numAmpMarkers );

		yOffset = this.height - magsHeight;

		horizPixelsPerMarker = ScopeGenDisplayUiJComponent.getAdjustedWidthBetweenMarkers( this.width, numTimeMarkers );
		vertPixelsPerMarker = ScopeGenDisplayUiJComponent.getAdjustedHeightBetweenMarkers( this.height, numAmpMarkers );

		for( int c = 0 ; c < ScopeGenMadDefinition.NUM_VIS_CHANNELS ; ++c )
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
	public void visualiseScopeBuffers( final float[][] frontEndBuffers,
			final int framesChangedOffset,
			final int framesChangedLength )
	{
		if( frontEndBuffers[0].length != internalChannelBuffers[0].length )
		{
			if( log.isErrorEnabled() )
			{
				log.error("Buffer lengths don't match feb.length(" +
						frontEndBuffers[0].length + ") vs icb.length(" +
						internalChannelBuffers[0].length + ")");
			}
		}
		else
		{
			for( int c = 0 ; c < ScopeGenMadDefinition.NUM_VIS_CHANNELS ; ++c )
			{
				System.arraycopy( frontEndBuffers[c], framesChangedOffset,
						internalChannelBuffers[c], framesChangedOffset,
						framesChangedLength );
			}
			calculateChannelValues( internalChannelBuffers, framesChangedOffset, framesChangedLength );
			repaint();
		}
	}

	private void calculateChannelValues( final float[][] channelBuffers,
			final int framesChangedOffset,
			final int framesChangedLength )
	{
		final float numSamplesPerPixel = captureLengthSamples / (float)magsWidth;

//		log.trace("Would attempt to revisualise with " + numSamplesPerPixel + " samples per pixel" );

		int startPixel = (int)Math.floor(framesChangedOffset / numSamplesPerPixel);
		startPixel = (startPixel < 0 ? 0 : startPixel);
		final int numPixels = (int)Math.ceil(framesChangedLength / numSamplesPerPixel);
		int endPixel = startPixel + numPixels;
		endPixel = (endPixel < magsWidth ? endPixel : magsWidth);
//		if( log.isTraceEnabled() )
//		{
//			log.trace( "Calculating channel values for frames " + framesChangedOffset + " of length " +
//					framesChangedLength + " pixels(" + startPixel + "->" + endPixel + ")");
//		}

		if( numSamplesPerPixel <= 1.0f )
		{
			for( int channel = 0 ; channel < ScopeGenMadDefinition.NUM_VIS_CHANNELS ; ++channel )
			{
				for( int x = startPixel ; x < endPixel ; ++x )
				{
					int startOffset = Math.round(x * numSamplesPerPixel);
					startOffset = (startOffset < 0 ? 0 : (startOffset >= captureLengthSamples ? captureLengthSamples - 1 : startOffset ) );
					float min = channelBuffers[channel][startOffset];
					float max = min;

					// Now convert to normalised value
					if( biUniPolar )
					{
						min = (min + 1.0f) / 2.0f;
						max = (max + 1.0f) / 2.0f;
					}
					else
					{
					}
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
			for( int channel = 0 ; channel < ScopeGenMadDefinition.NUM_VIS_CHANNELS ; ++channel )
			{
				for( int x = startPixel ; x < endPixel ; ++x )
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
					if( biUniPolar )
					{
						min = (min + 1.0f) / 2.0f;
						max = (max + 1.0f) / 2.0f;
					}
					else
					{
					}
					min = (min < 0.0f ? 0.0f : (min > 1.0f ? 1.0f : min));
					max = (max < 0.0f ? 0.0f : (max > 1.0f ? 1.0f : max));
					// And to pixel offset
					channelValues[channel][x*2] = (int)(min * magsHeight);
					channelValues[channel][(x*2)+1] = (int)(max * magsHeight);
				}
			}
		}
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
		calculateChannelValues( internalChannelBuffers, 0, captureLengthSamples );
		repaint();
	}

	@Override
	public void receiveSampleRateChange( final int sampleRate )
	{
		this.sampleRate = sampleRate;
		maxCaptureSamples = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( sampleRate,
				LogarithmicTimeMillis1To1000SliderModel.MAX_MILLIS );
		setupInternalChannelBuffers();
	}

	private final void setupInternalChannelBuffers()
	{
		if( internalChannelBuffers[0] == null ||
				internalChannelBuffers[0].length != maxCaptureSamples )
		{
			for( int c = 0 ; c < ScopeGenMadDefinition.NUM_VIS_CHANNELS ; ++c )
			{
				internalChannelBuffers[c] = new float[maxCaptureSamples];
			}
		}
	}

	public void setSignalVisibility( final int signal, final boolean active )
	{
		signalVisibility[signal] = active;
		repaint();
	}

	public void setBiUniPolar( final boolean active )
	{
		biUniPolar = active;
		calculateChannelValues( internalChannelBuffers, 0, captureLengthSamples );
		repaint();
	}

}
