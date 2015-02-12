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

package uk.co.modularaudio.mads.base.audioanalyser.ui.tabbedpane.oscope;

import java.awt.Color;
import java.awt.Graphics;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.audioanalyser.ui.AudioAnalyserDataBuffers;
import uk.co.modularaudio.mads.base.audioanalyser.ui.AudioAnalyserUiBufferState;
import uk.co.modularaudio.mads.base.audioanalyser.ui.BufferFreezeListener;
import uk.co.modularaudio.mads.base.audioanalyser.ui.BufferStateListener;
import uk.co.modularaudio.mads.base.audioanalyser.ui.BufferZoomAndPositionListener;
import uk.co.modularaudio.mads.base.audioanalyser.ui.tabbedpane.AudioAnalyserDisplay;
import uk.co.modularaudio.util.audio.gui.mad.rollpainter.RollPainter;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacPanel;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;

public class WaveDisplay extends PacPanel
implements AudioAnalyserDisplay, BufferZoomAndPositionListener, BufferFreezeListener, BufferStateListener
{
	private static final long serialVersionUID = -7355046237468407858L;

	private static Log log = LogFactory.getLog( WaveDisplay.class.getName() );

	private final AudioAnalyserUiBufferState uiBufferState;
	//	private final BufferedImageAllocator bia;

	private RollPainter<WaveDisplayBufferedImage, WaveDisplayBufferedImageClearer> rollPainter;
	private WaveDisplaySampleFactory sampleFactory;

	public final static int DISPLAY_WIDTH = 449;
	//	public final static int DISPLAY_WIDTH = 1066;
	public final static int DISPLAY_HEIGHT = 171;

	public enum DisplayTypeEnum
	{
		RAW,
		dB,
		ThreeSpeech,
		ThreeMusic
	};

	private final DisplayPresentationProcessor[] typeToDisplayProcessor = new DisplayPresentationProcessor[4];

	private DisplayTypeEnum curDisplayType;

	public WaveDisplay( final AudioAnalyserUiBufferState uiBufferState, final BufferedImageAllocator bia )
	{
		this.uiBufferState = uiBufferState;
		uiBufferState.addBufferStateListener( this );
		//		this.bia = bia;
		setOpaque(true);
		//		setBackground(AAColours.BACKGROUND);
		setBackground( Color.BLUE );

		try
		{
			sampleFactory = new WaveDisplaySampleFactory( bia, uiBufferState, DISPLAY_WIDTH, DISPLAY_HEIGHT );
			rollPainter = new RollPainter<WaveDisplayBufferedImage, WaveDisplayBufferedImageClearer>( DISPLAY_WIDTH, sampleFactory );

			curDisplayType = DisplayTypeEnum.RAW;
			setDisplayType( curDisplayType );
		}
		catch (final DatastoreException e)
		{
			if( log.isErrorEnabled() )
			{
				log.error("DatastoreException caught initialising roll painter: " + e.toString(), e);
			}
		}
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime )
	{
		if( rollPainter.checkAndUpdate() )
		{
			repaint();
		}
	}

	@Override
	protected void paintComponent(final Graphics g)
	{
		if( rollPainter != null )
		{
			if( rollPainter.buffer0Visible() )
			{
				g.drawImage( rollPainter.buffer0.bi, rollPainter.buffer0XOffset, 0, null );
			}
			if( rollPainter.buffer1Visible() )
			{
				g.drawImage( rollPainter.buffer1.bi, rollPainter.buffer1XOffset, 0, null );
			}
		}
		else
		{
			//			g.setColor( AAColours.BACKGROUND );
			g.setColor( Color.BLUE );
			g.fillRect( 0, 0, DISPLAY_WIDTH - 1, DISPLAY_HEIGHT - 1 );
		}
	}

	@Override
	public void setBounds(final int x, final int y, final int width, final int height)
	{
		super.setBounds(x, y, width, height);
//		if( log.isDebugEnabled() )
//		{
//			log.debug("Bounds set to " + x + ", " + y + "-" + width + "," + height );
//		}
	}

	@Override
	public void setNeedsFullUpdate()
	{
		sampleFactory.setNeedsFullUpdate();
		if( rollPainter.checkAndUpdate() )
		{
			repaint();
		}
	}

	@Override
	public void receiveFreezeStateChange(final boolean frozen)
	{
		if( !frozen )
		{
			setNeedsFullUpdate();
		}
	}

	@Override
	public void receiveZoomAndPositionUpdate()
	{
		setNeedsFullUpdate();
	}

	public final void setDisplayType( final DisplayTypeEnum displayTypeEnum )
	{
		curDisplayType = displayTypeEnum;

		final AudioAnalyserDataBuffers dataBuffers = uiBufferState.getDataBuffers();

		final int dtOrdinal = displayTypeEnum.ordinal();
		DisplayPresentationProcessor dpp = typeToDisplayProcessor[ dtOrdinal ];

		if( dpp == null )
		{
			switch( displayTypeEnum )
			{
				case ThreeMusic:
				{
					dpp = new DisplayPresentationProcessorThreeMusic( uiBufferState, DISPLAY_WIDTH, DISPLAY_HEIGHT);
					break;
				}
				case ThreeSpeech:
				{
					dpp = new DisplayPresentationProcessorThreeSpeech( uiBufferState, DISPLAY_WIDTH, DISPLAY_HEIGHT);
					break;
				}
				case dB:
				{
					dpp = new DisplayPresentationProcessorDb( uiBufferState, DISPLAY_WIDTH, DISPLAY_HEIGHT);
					break;
				}
				case RAW:
				default:
				{
					dpp = new DisplayPresentationProcessorLinear( uiBufferState, DISPLAY_WIDTH, DISPLAY_HEIGHT);
					break;
				}
			}
			typeToDisplayProcessor[ dtOrdinal ] = dpp;
		}
		dpp.resetForSwitch();
		dataBuffers.setAdditionalDataBuffers( dpp.getAdditionalDataBuffers() );
		sampleFactory.setPresentationProcessor( dpp );
		setNeedsFullUpdate();
	}

	@Override
	public void receiveStartup( final HardwareIOChannelSettings ratesAndLatency, final MadTimingParameters timingParameters )
	{
	}

	@Override
	public void receiveStop()
	{
	}

	@Override
	public void receiveDestroy()
	{
		try
		{
			rollPainter.cleanup();
		}
		catch (final DatastoreException e)
		{
			if( log.isErrorEnabled() )
			{
				log.error("DatastoreException caught cleaning up roll painter: " + e.toString(), e );
			}
		}
	}
}
