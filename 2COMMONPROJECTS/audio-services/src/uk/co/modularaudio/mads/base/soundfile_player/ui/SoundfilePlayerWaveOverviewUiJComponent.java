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

package uk.co.modularaudio.mads.base.soundfile_player.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.soundfile_player.mu.SoundfilePlayerMadDefinition;
import uk.co.modularaudio.mads.base.soundfile_player.mu.SoundfilePlayerMadInstance;
import uk.co.modularaudio.service.audioanalysis.AnalysedData;
import uk.co.modularaudio.service.audioanalysis.AnalysisFillCompletionListener;
import uk.co.modularaudio.service.blockresampler.BlockResamplingClient;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacPanel;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class SoundfilePlayerWaveOverviewUiJComponent extends PacPanel
	implements IMadUiControlInstance<SoundfilePlayerMadDefinition, SoundfilePlayerMadInstance, SoundfilePlayerMadUiInstance>,
	SoundfileSampleEventListener, AnalysisFillCompletionListener
{
	private static final long serialVersionUID = -725580571613103896L;

	private static Log log = LogFactory.getLog( SoundfilePlayerWaveOverviewUiJComponent.class.getName() );

	private final static int WAVE_OVERVIEW_INTRO_PIXELS = 5;
	private final static int WAVE_OVERVIEW_BORDER_PIXELS = 3;

	private final SoundfilePlayerMadUiInstance uiInstance;

	private final WaveOverviewPositionClickListener waveOverviewPositionClickListener;

	private long currentSampleNumFrames;
	private int desiredPositionOffset;

	private int displayedPositionOffset;

	private int lastWidth;
	private int lastHeight;
	private int lastOverviewWidth;
	private int lastOverviewHeight;

	private int internalPercentComplete = -1;

	private BufferedImage staticThumbnail;

	public SoundfilePlayerWaveOverviewUiJComponent(
			final SoundfilePlayerMadDefinition definition,
			final SoundfilePlayerMadInstance instance,
			final SoundfilePlayerMadUiInstance uiInstance,
			final int controlIndex )
	{
		this.uiInstance = uiInstance;

		setBackground( Color.ORANGE );
		setOpaque( true );

		uiInstance.addSampleEventListener( this );

		waveOverviewPositionClickListener = new WaveOverviewPositionClickListener( this );

		this.addMouseListener( waveOverviewPositionClickListener );

		uiInstance.addAnalysisFillListener( this );
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return true;
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime )
	{
		if( displayedPositionOffset != desiredPositionOffset )
		{
			repaint();
		}
	}

	@Override
	public Component getControl()
	{
		return this;
	}

	@Override
	public void destroy()
	{
		// Do nothing
	}

	@Override
	public void paintComponent(final Graphics g)
	{
		final int xWaveOffset = WAVE_OVERVIEW_BORDER_PIXELS + WAVE_OVERVIEW_INTRO_PIXELS;
		g.setColor( SoundfilePlayerColorDefines.WAVE_DISPLAY_BACKGROUND_COLOR );
		g.fillRect( 1, 1, lastWidth-1, lastHeight-1 );
		g.setColor( SoundfilePlayerColorDefines.WAVE_DISPLAY_BORDER_COLOR );
		g.drawRect( 0, 0, lastWidth, lastHeight );

		if( staticThumbnail != null )
		{
			g.drawImage( staticThumbnail,
					xWaveOffset,
					WAVE_OVERVIEW_BORDER_PIXELS,
					null );
		}
		else
		{
			g.setColor( SoundfilePlayerColorDefines.WAVE_DISPLAY_WAVE_BG_COLOR );
			g.fillRect( xWaveOffset, WAVE_OVERVIEW_BORDER_PIXELS, lastOverviewWidth, lastOverviewHeight );
			if( internalPercentComplete >= 0 )
			{
				g.setColor( SoundfilePlayerColorDefines.WAVE_DISPLAY_WAVE_FG_COLOR );
				final int yOffset = WAVE_OVERVIEW_BORDER_PIXELS + (lastOverviewHeight / 2);
				final int widthOfLine = (lastOverviewWidth * internalPercentComplete) / 100;
				g.drawLine( xWaveOffset,
						yOffset,
						xWaveOffset + widthOfLine,
						yOffset );
			}
		}

		g.setColor( SoundfilePlayerColorDefines.WAVE_DISPLAY_CURRENT_POSITION_COLOUR );
		final int actualPos = xWaveOffset + desiredPositionOffset;
		g.drawLine( actualPos, WAVE_OVERVIEW_BORDER_PIXELS, actualPos, lastOverviewHeight );

		displayedPositionOffset = desiredPositionOffset;
	}

	@Override
	public void receiveSampleChangeEvent( final BlockResamplingClient newSample )
	{
		currentSampleNumFrames = newSample.getTotalNumFrames();
		final long position = newSample.getFramePosition();
		recomputeDesiredPositionOffset( position );
		repaint();
	}

	private void recomputeDesiredPositionOffset( final long newPosition )
	{
		final float normalisedPos = ((float)newPosition) / currentSampleNumFrames;

		desiredPositionOffset = (int)(lastOverviewWidth * normalisedPos);
	}

	@Override
	public void receiveDeltaPositionEvent( final long newPosition )
	{
		recomputeDesiredPositionOffset( newPosition );
	}

	@Override
	public void receiveAbsPositionEvent( final long newPosition )
	{
		recomputeDesiredPositionOffset( newPosition );
	}

	@Override
	public void setBounds( final int x, final int y, final int width, final int height )
	{
		super.setBounds( x, y, width, height );
		this.lastWidth = width-1;
		this.lastHeight = height-1;

		this.lastOverviewWidth = lastWidth - (2 * WAVE_OVERVIEW_INTRO_PIXELS) - (2 * WAVE_OVERVIEW_BORDER_PIXELS);
		this.lastOverviewHeight = lastHeight - (2 * WAVE_OVERVIEW_BORDER_PIXELS);
//		if( log.isTraceEnabled() )
//		{
//			log.trace("Overview w/h is(" + lastOverviewWidth + ", " + lastOverviewHeight + ")");
//		}
	}

	public void handleOverviewClickAtPoint( final Point point )
	{
		final int clickX = point.x;
		final float normalisedPosition = (clickX - WAVE_OVERVIEW_INTRO_PIXELS - WAVE_OVERVIEW_BORDER_PIXELS) /
				(float)lastOverviewWidth;
//		if( log.isDebugEnabled() )
//		{
//			log.debug("Received click at " + point.x + " normalised to " + normalisedPosition );
//		}
		uiInstance.receiveOverviewPositionRequest( normalisedPosition );
	}

	@Override
	public void receiveCacheRefreshCompletionEvent()
	{
	}

	@Override
	public void receiveAnalysedData( final AnalysedData analysedData )
	{
		// Pull in the image file just generated as the thumbnail
		internalPercentComplete = -1;

		final String pathToThumbnail = analysedData.getPathToStaticThumbnail();

		try
		{
			staticThumbnail = ImageIO.read( new File( pathToThumbnail ) );
			if( log.isDebugEnabled() )
			{
				log.debug("Read in static thumbnail from " + pathToThumbnail );
			}
		}
		catch( final IOException e )
		{
			final String msg = "Exception caught loading static thumbnail: " + e.toString();
			log.error( msg, e );
		}

		repaint();
	}

	@Override
	public void notifyAnalysisFailure()
	{
		// Clear any state where we were waiting for analysis data
		internalPercentComplete = -1;
		repaint();
	}

	@Override
	public void receivePercentageComplete( final int percentageComplete )
	{
		// Set the internal variable to non-negative to indicate
		// we are processing something
		internalPercentComplete  = percentageComplete;
		repaint();
	}

	@Override
	public void receiveAnalysisBegin()
	{
		internalPercentComplete = -1;
		staticThumbnail = null;
		repaint();
	}
}
