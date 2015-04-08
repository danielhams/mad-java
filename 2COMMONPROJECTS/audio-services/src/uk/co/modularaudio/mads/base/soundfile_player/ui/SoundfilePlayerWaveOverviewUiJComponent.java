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

import uk.co.modularaudio.mads.base.soundfile_player.mu.SoundfilePlayerMadDefinition;
import uk.co.modularaudio.mads.base.soundfile_player.mu.SoundfilePlayerMadInstance;
import uk.co.modularaudio.service.blockresampler.BlockResamplingClient;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacPanel;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class SoundfilePlayerWaveOverviewUiJComponent extends PacPanel
	implements IMadUiControlInstance<SoundfilePlayerMadDefinition, SoundfilePlayerMadInstance, SoundfilePlayerMadUiInstance>,
	SoundfileSampleEventListener
{
	private static final long serialVersionUID = -725580571613103896L;

//	private static Log log = LogFactory.getLog( SoundfilePlayerWaveOverviewUiJComponent.class.getName() );

	private final static int WAVE_OVERVIEW_INTRO_PIXELS = 5;
	private final static int WAVE_OVERVIEW_BORDER_PIXELS = 3;

	private long currentSampleNumFrames;
	private int desiredPositionOffset;

	private int displayedPositionOffset;

	private int lastWidth;
	private int lastHeight;
	private int lastOverviewWidth;
	private int lastOverviewHeight;

	public SoundfilePlayerWaveOverviewUiJComponent(
			final SoundfilePlayerMadDefinition definition,
			final SoundfilePlayerMadInstance instance,
			final SoundfilePlayerMadUiInstance uiInstance,
			final int controlIndex )
	{
		setBackground( Color.ORANGE );
		setOpaque( true );

		uiInstance.addSampleEventListener( this );
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
	}

	@Override
	public void paintComponent(final Graphics g)
	{
		g.setColor( SoundfilePlayerColorDefines.WAVE_DISPLAY_BACKGROUND_COLOR );
		g.fillRect( 1, 1, lastWidth-1, lastHeight-1 );
		g.setColor( SoundfilePlayerColorDefines.WAVE_DISPLAY_BORDER_COLOR );
		g.drawRect( 0, 0, lastWidth, lastHeight );

		g.setColor( SoundfilePlayerColorDefines.WAVE_DISPLAY_CURRENT_POSITION_COLOUR );
		final int actualPos = WAVE_OVERVIEW_BORDER_PIXELS + WAVE_OVERVIEW_INTRO_PIXELS + desiredPositionOffset;
		g.drawLine( actualPos, WAVE_OVERVIEW_BORDER_PIXELS, actualPos, lastOverviewHeight );

		displayedPositionOffset = desiredPositionOffset;
	}

	@Override
	public void receiveSampleChangeEvent( final BlockResamplingClient newSample )
	{
//		log.debug("Received notification of sample change to " +
//				newSample.getSampleCacheClient().getLibraryEntry().getTitle() );
		currentSampleNumFrames = newSample.getTotalNumFrames();
	}

	private void recomputeDesiredPositionOffset( final long newPosition )
	{
		final float normalisedPos = ((float)newPosition) / currentSampleNumFrames;

		desiredPositionOffset = (int)(lastOverviewWidth * normalisedPos);
	}

	@Override
	public void receiveDeltaPositionEvent( final long newPosition )
	{
//		log.trace("Received delta position event: " + newPosition );
		recomputeDesiredPositionOffset( newPosition );
	}

	@Override
	public void receiveAbsPositionEvent( final long newPosition )
	{
//		log.trace("Received abs position event: " + newPosition );
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
//		log.debug("Overview w/h is(" + lastOverviewWidth + ", " + lastOverviewHeight + ")");
	}
}
