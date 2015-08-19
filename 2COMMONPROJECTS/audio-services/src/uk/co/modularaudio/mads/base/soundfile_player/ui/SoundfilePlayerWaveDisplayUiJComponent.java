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

import java.awt.Component;
import java.awt.Graphics;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.controller.advancedcomponents.AdvancedComponentsFrontController;
import uk.co.modularaudio.mads.base.soundfile_player.mu.SoundfilePlayerMadDefinition;
import uk.co.modularaudio.mads.base.soundfile_player.mu.SoundfilePlayerMadInstance;
import uk.co.modularaudio.mads.base.soundfile_player.ui.SoundfilePlayerZoomToggleGroupUiJComponent.ZoomLevel;
import uk.co.modularaudio.mads.base.soundfile_player.ui.rollpainter.SoundfileDisplayBuffer;
import uk.co.modularaudio.mads.base.soundfile_player.ui.rollpainter.SoundfileDisplayBufferClearer;
import uk.co.modularaudio.mads.base.soundfile_player.ui.rollpainter.SoundfileDisplaySampleFactory;
import uk.co.modularaudio.service.audioanalysis.AnalysedData;
import uk.co.modularaudio.service.audioanalysis.AnalysisFillCompletionListener;
import uk.co.modularaudio.service.blockresampler.BlockResamplingClient;
import uk.co.modularaudio.service.samplecaching.SampleCacheClient;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.mad.rollpainter.RollPainter;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacPanel;
import uk.co.modularaudio.util.audio.mad.MadInstance.InstanceLifecycleListener;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.math.AudioMath;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;

public class SoundfilePlayerWaveDisplayUiJComponent extends PacPanel
	implements IMadUiControlInstance<SoundfilePlayerMadDefinition, SoundfilePlayerMadInstance, SoundfilePlayerMadUiInstance>,
	SoundfileSampleEventListener,
	ZoomListener,
	InstanceLifecycleListener,
	AnalysisFillCompletionListener
{
	private static final long serialVersionUID = -580564924377154659L;

	private static final float DESIRED_WAVE_DB = -9.0f;

	private static Log log = LogFactory.getLog( SoundfilePlayerWaveDisplayUiJComponent.class.getName() );

	public final static int REQUIRED_WIDTH = 441;
	public final static int REQUIRED_HEIGHT= 145;

	private final SoundfilePlayerMadUiInstance uiInstance;
	private final AdvancedComponentsFrontController advancedComponentsFrontController;

	private final BufferedImageAllocator bia;

	private int displayWidth;
	private int displayWidthMinusOneOverTwo;
	private int displayHeight;

	private boolean active;

	private float currentZoomMillis = SoundfilePlayerZoomToggleGroupUiJComponent.ZoomLevel.ZOOMED_DEFAULT.getMillisForLevel();

	private SoundfileDisplaySampleFactory rpSampleFactory;
	private RollPainter<SoundfileDisplayBuffer, SoundfileDisplayBufferClearer> rollPainter;

	private BlockResamplingClient rss;

	public SoundfilePlayerWaveDisplayUiJComponent(
			final SoundfilePlayerMadDefinition definition,
			final SoundfilePlayerMadInstance instance,
			final SoundfilePlayerMadUiInstance uiInstance,
			final int controlIndex )
	{
		this.setOpaque(true);
		this.uiInstance = uiInstance;
		this.advancedComponentsFrontController = instance.getAdvancedComponentsFrontController();

		this.bia = uiInstance.getUiDefinition().getBufferedImageAllocator();

		uiInstance.addSampleEventListener( this );
		uiInstance.addZoomListener( this );
		uiInstance.addLifecycleListener( this );
		uiInstance.addAnalysisFillListener( this );

		setupSampleFactory( REQUIRED_WIDTH, REQUIRED_HEIGHT );
	}

	@Override
	public void doDisplayProcessing(
			final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime)
	{
		if( isShowing() )
		{
			if( !active )
			{
				active = true;
				// Fake a position update so we don't do a big silly jump
				if( rss != null )
				{
//					log.debug("Faking position update to get full repaint: " + rss.getFramePosition() );
					receiveAbsPositionEvent( rss.getFramePosition() );
				}
				rpSampleFactory.resetForFullRepaint();
				uiInstance.sendActive( active );
			}
		}
		else
		{
			if( active )
			{
				active = false;
				uiInstance.sendActive( active );
			}
		}

		if( rollPainter != null )
		{
			if( rollPainter.checkAndUpdate() )
			{
				if( active )
				{
					repaint();
				}
			}
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
		if( rollPainter != null )
		{
			try
			{
				rollPainter.cleanup();
			}
			catch (final DatastoreException e)
			{
				if( log.isErrorEnabled() )
				{
					log.error("Exception caught during roll painter cleanup: " + e.toString(), e );
				}
			}
			rollPainter = null;
		}
		uiInstance.removeFileInfoReceiver( this );
	}

	@Override
	public void paint(final Graphics g)
	{
		if( rollPainter != null )
		{
			if( rollPainter.buffer0Visible() )
			{
				g.drawImage( rollPainter.buffer0.bi, rollPainter.buffer0XOffset, 0, null );
			}
			if( rollPainter.buffer1Visible() )
			{
				g.drawImage( rollPainter.buffer1.bi,  rollPainter.buffer1XOffset, 0, null );
			}
		}
		else
		{
			g.setColor( SoundfilePlayerColorDefines.WAVE_DISPLAY_BACKGROUND_COLOR );
			g.fillRect(0, 0, displayWidth, displayHeight );
		}
		g.setColor( SoundfilePlayerColorDefines.WAVE_DISPLAY_CURRENT_POSITION_COLOUR );
		g.drawLine( displayWidthMinusOneOverTwo,  0, displayWidthMinusOneOverTwo, displayHeight );
	}

	private void setupSampleFactory( final int width, final int height )
	{
		displayWidth = width;
		displayHeight = height;
		if( displayWidth % 2 == 0 )
		{
			log.warn("WARNING - DISPLAY WIDTH EVEN");
		}
		displayWidthMinusOneOverTwo = (displayWidth - 1) / 2;

		try
		{
			rpSampleFactory = new SoundfileDisplaySampleFactory( advancedComponentsFrontController.getSampleCachingService(),
					bia,
					displayWidth,
					displayHeight,
					uiInstance );
			rollPainter = new RollPainter<SoundfileDisplayBuffer, SoundfileDisplayBufferClearer>( displayWidth, rpSampleFactory );

			setSampleFactoryCaptureLengthMillis();
		}
		catch( final Exception e )
		{
			if( log.isErrorEnabled() )
			{
				log.error("Exception caught setting up roll painter during bounds set: " + e.toString(), e );
			}
		}
	}

	private void setSampleFactoryCaptureLengthMillis()
	{
		if( uiInstance != null && uiInstance.knownDataRate != null )
		{
//			log.debug("Setting capture render length for SR(" + uiInstance.knownDataRate.getValue() + ") ms(" + currentZoomMillis + ")" );
			if( rpSampleFactory != null )
			{
				rpSampleFactory.setCaptureLengthMillis( currentZoomMillis );
				if( rollPainter.checkAndUpdate() )
				{
					repaint();
				}
			}
		}
	}

	@Override
	public void receiveSampleChangeEvent( final BlockResamplingClient newSample)
	{
		log.trace("Receive sample change event");
		rss = newSample;
		if( newSample != null )
		{
			final SampleCacheClient scc = newSample.getSampleCacheClient();
			rpSampleFactory.setSampleCacheClient( scc );
		}
		else
		{
			rpSampleFactory.setSampleCacheClient( null );
		}

		repaint();
	}

	@Override
	public void receiveDeltaPositionEvent( final long newPosition )
	{
		rpSampleFactory.setCurrentPosition( newPosition );
	}

	@Override
	public void receiveAbsPositionEvent( final long newPosition )
	{
		rpSampleFactory.setCurrentPosition( newPosition );
		rpSampleFactory.resetForFullRepaint();
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return true;
	}

	@Override
	public void receiveStartup( final HardwareIOChannelSettings hardwareChannelSettings,
			final MadTimingParameters timingParameters,
			final MadFrameTimeFactory frameTimeFactory )
	{
		setSampleFactoryCaptureLengthMillis();
	}

	@Override
	public void receiveStop()
	{
	}

	@Override
	public void receiveCacheRefreshCompletionEvent()
	{
		log.debug("Received cache refresh completion event");
		rpSampleFactory.resetForFullRepaint();
		if( rollPainter.checkAndUpdate() )
		{
			this.repaint();
		}
	}

	@Override
	public void receiveAnalysisBegin()
	{
		rpSampleFactory.setDisplayMultiplier( 1.0f );
		rpSampleFactory.resetForFullRepaint();
	}

	@Override
	public void receivePercentageComplete( final int percentageComplete )
	{
	}

	@Override
	public void notifyAnalysisFailure()
	{
		rpSampleFactory.setDisplayMultiplier( 1.0f );
		rpSampleFactory.resetForFullRepaint();
	}

	@Override
	public void receiveAnalysedData( final AnalysedData analysedData )
	{
		final float peakRmsDb = analysedData.getRmsPeakDb();

		final float adjustmentDb = DESIRED_WAVE_DB - peakRmsDb;
		final float adjustmentMultiplier = AudioMath.dbToLevelF( adjustmentDb );

		rpSampleFactory.setDisplayMultiplier( adjustmentMultiplier );
		rpSampleFactory.resetForFullRepaint();
		if( rollPainter.checkAndUpdate() )
		{
			this.repaint();
		}
	}

	@Override
	public void receiveZoomLevel( final ZoomLevel zoomLevel )
	{
		currentZoomMillis = zoomLevel.getMillisForLevel();
		if( uiInstance.knownDataRate != null )
		{
			setSampleFactoryCaptureLengthMillis();
		}
	}
}
