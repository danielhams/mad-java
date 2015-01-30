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
import java.awt.Rectangle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.controller.advancedcomponents.AdvancedComponentsFrontController;
import uk.co.modularaudio.mads.base.soundfile_player.mu.SoundfilePlayerMadDefinition;
import uk.co.modularaudio.mads.base.soundfile_player.mu.SoundfilePlayerMadInstance;
import uk.co.modularaudio.mads.base.soundfile_player.ui.rollpainter.SoundfileDisplayBuffer;
import uk.co.modularaudio.mads.base.soundfile_player.ui.rollpainter.SoundfileDisplayBufferClearer;
import uk.co.modularaudio.mads.base.soundfile_player.ui.rollpainter.SoundfileDisplaySampleFactory;
import uk.co.modularaudio.service.blockresampler.BlockResamplingClient;
import uk.co.modularaudio.service.samplecaching.SampleCacheClient;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.mad.rollpainter.RollPainter;
import uk.co.modularaudio.util.audio.gui.paccontrols.PacPanel;
import uk.co.modularaudio.util.audio.mad.MadInstance.InstanceLifecycleListener;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;

public class SoundfilePlayerWaveDisplayUiJComponent extends PacPanel
	implements IMadUiControlInstance<SoundfilePlayerMadDefinition, SoundfilePlayerMadInstance, SoundfilePlayerMadUiInstance>,
	SoundfileSampleEventListener,
	ZoomDataListener, InstanceLifecycleListener
{
	private static final long serialVersionUID = -580564924377154659L;

	private static Log log = LogFactory.getLog( SoundfilePlayerWaveDisplayUiJComponent.class.getName() );

	private final SoundfilePlayerMadUiInstance uiInstance;
	private final AdvancedComponentsFrontController advancedComponentsFrontController;

	private final BufferedImageAllocator bia;

	private int displayWidth;
	private int displayWidthMinusOneOverTwo;
	private int displayHeight;

	private boolean active;

	private float currentZoomMillis = 0.0f;

	private SoundfileDisplaySampleFactory rpSampleFactory;
	private RollPainter<SoundfileDisplayBuffer, SoundfileDisplayBufferClearer> rollPainter;

	private BlockResamplingClient rss;

	private final static Color CURRENT_POSITION_COLOUR = new Color( 0.4f, 0.2f, 0.2f );

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
		uiInstance.setZoomDataListener( this );
		uiInstance.addLifecycleListener( this );
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
	public void paintComponent(final Graphics g)
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
			g.setColor( Color.BLACK );
			g.fillRect(0, 0, displayWidth, displayHeight );
		}
		g.setColor( CURRENT_POSITION_COLOUR );
		g.drawLine( displayWidthMinusOneOverTwo,  0, displayWidthMinusOneOverTwo, displayHeight );
	}

	@Override
	public void setBounds( final Rectangle r )
	{
		super.setBounds( r );
		displayWidth = r.width;
		displayHeight = r.height;
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
			}
		}
	}

	@Override
	public void setZoomMillis(final float zoomMillis)
	{
//		log.debug("Received zoom of " + zoomMillis + " millis");
		currentZoomMillis = zoomMillis;
		if( uiInstance.knownDataRate != null )
		{
			setSampleFactoryCaptureLengthMillis();
		}
	}

	@Override
	public void receiveSampleChangeEvent( final BlockResamplingClient newSample)
	{
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

}
