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

package uk.co.modularaudio.mads.base.waveroller.ui;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.waveroller.mu.WaveRollerMadDefinition;
import uk.co.modularaudio.mads.base.waveroller.mu.WaveRollerMadInstance;
import uk.co.modularaudio.util.audio.buffer.UnsafeFloatRingBuffer;
import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.mad.rollpainter.RollPainter;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacPanel;
import uk.co.modularaudio.util.audio.mad.buffer.BackendToFrontendDataRingBuffer;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;

public class WaveRollerDisplayUiJComponent extends PacPanel
	implements IMadUiControlInstance<WaveRollerMadDefinition, WaveRollerMadInstance, WaveRollerMadUiInstance>,
	WaveRollerDataListener
{
	private static final long serialVersionUID = -1183011558795174539L;

	private static Log log = LogFactory.getLog(  WaveRollerDisplayUiJComponent.class.getName( ) );

	private final WaveRollerMadInstance instance;
	private final WaveRollerMadUiInstance uiInstance;

	private DataRate knownDataRate;

	private int maxCaptureBufferLength;
	private float currentCaptureMillis = 1.0f;
	private int captureRenderLength;

	private UnsafeFloatRingBuffer displayRingBuffer;
	private BackendToFrontendDataRingBuffer instanceRingBuffer;

	// Ui display bits
	private boolean previouslyShowing;

	private final BufferedImageAllocator bufferImageAllocator;

	boolean needsRepaint;

	private Rectangle bounds;
	private int imageWidth, imageHeight;
	private float valueScaleForMargins;

	private WaveRollerBufferSampleFactory bufferSampleFactory;
	private RollPainter<WaveRollerBuffer, WaveRollerBufferCleaner> rollPainter;

	public WaveRollerDisplayUiJComponent(
			final WaveRollerMadDefinition definition,
			final WaveRollerMadInstance instance,
			final WaveRollerMadUiInstance uiInstance,
			final int controlIndex )
	{
		this.setOpaque( true );
		this.instance = instance;
		this.uiInstance = uiInstance;

		uiInstance.setScopeDataListener( this );

		this.bufferImageAllocator = uiInstance.getUiDefinition().getBufferedImageAllocator();
	}

	@Override
	public void doDisplayProcessing(final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime)
	{
		final boolean showing = isShowing();

		if( previouslyShowing != showing )
		{
			uiInstance.sendUiActive( showing );
			previouslyShowing = showing;
			if( showing )
			{
				bufferSampleFactory.resetForFullRepaint();
			}
		}


		if( rollPainter != null )
		{
			if( rollPainter.checkAndUpdate() )
			{
				repaint();
			}
		}
	}

	@Override
	public Component getControl()
	{
		return this;
	}

	@Override
	public void paint( final Graphics g )
	{
		if( rollPainter != null )
		{
			if( rollPainter.buffer0Visible() )
			{
				g.drawImage( rollPainter.buffer0.bi, rollPainter.buffer0XOffset, 0, null );
			}
			if( rollPainter.buffer1Visible() )
			{
				g.drawImage(  rollPainter.buffer1.bi, rollPainter.buffer1XOffset, 0, null );
			}
		}
		else
		{
			g.setColor( WaveRollerColours.BACKGROUND_COLOR );
			final Rectangle b = getBounds();
			g.fillRect( 0,  0, b.width, b.height );
		}
	}

	@Override
	public void destroy()
	{
		try
		{
			if( rollPainter != null )
			{
				rollPainter.cleanup();
				rollPainter = null;
			}
			if( bufferSampleFactory != null )
			{
				bufferSampleFactory = null;
			}
		}
		catch( final Exception e )
		{
			final String msg = "Unable to free tiled images: " + e.toString();
			log.error( msg, e );
		}
	}

	@Override
	public void setBounds(final Rectangle r)
	{
		super.setBounds(r);
		this.bounds = r;

		imageWidth = r.width;
		imageHeight = r.height;
		valueScaleForMargins = (imageHeight - WaveRollerScaleDisplay.SCALE_MARGIN * 2) / (float)imageHeight;
	}

	@Override
	public void receiveStartup( final HardwareIOChannelSettings ratesAndLatency, final MadTimingParameters timingParameters )
	{
		knownDataRate = ratesAndLatency.getAudioChannelSetting().getDataRate();

		maxCaptureBufferLength = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( knownDataRate.getValue(),
				WaveRollerMadUiInstance.MAX_CAPTURE_MILLIS + 100 );

		captureRenderLength = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( knownDataRate.getValue(), currentCaptureMillis );

		displayRingBuffer = new UnsafeFloatRingBuffer( maxCaptureBufferLength, true );

		instanceRingBuffer = instance.getDataRingBuffer();

		if( imageWidth > 0 && imageHeight > 0 )
		{
			bufferSampleFactory = new WaveRollerBufferSampleFactory( uiInstance,
					bufferImageAllocator,
					displayRingBuffer,
					bounds,
					valueScaleForMargins );

			bufferSampleFactory.setCaptureRenderLength( captureRenderLength );
			try
			{
				rollPainter = new RollPainter<WaveRollerBuffer, WaveRollerBufferCleaner>( imageWidth, bufferSampleFactory );
			}
			catch( final Exception e )
			{
				if( log.isErrorEnabled() )
				{
					log.error( "Exception caught setting up roll painter: " + e.toString(), e );
				}
			}
		}
	}

	@Override
	public void receiveStop()
	{
		if( rollPainter != null)
		{
			try
			{
				rollPainter.cleanup();
			}
			catch (final DatastoreException e)
			{
				if( log.isErrorEnabled() )
				{
					log.error("DatastoreException caught cleaning up roll painter on stop: " + e.toString(), e );
				}
			}
			rollPainter = null;
			bufferSampleFactory = null;
		}
	}

	@Override
	public void receiveBufferIndexUpdate( final long indexUpdateTimestamp, final int writeIndex )
	{
		final int numReadable = instanceRingBuffer.frontEndGetNumReadableWithWriteIndex( writeIndex );

		final int spaceAvailable = displayRingBuffer.getNumWriteable();
		if( spaceAvailable < numReadable )
		{
			final int spaceToFree = numReadable - spaceAvailable;
			displayRingBuffer.moveForward( spaceToFree );
		}

		// Add on the new data
		final int numRead = instanceRingBuffer.frontEndReadToRingWithWriteIndex( writeIndex, displayRingBuffer,  numReadable );
		if( numRead != numReadable )
		{
			if( log.isErrorEnabled() )
			{
				log.error("Failed reading from data ring buffer - expected " + numReadable + " and received " + numRead);
			}
			// Zero buffer and set to full
			Arrays.fill( displayRingBuffer.buffer, 0.0f );
			displayRingBuffer.readPosition = 0;
			displayRingBuffer.writePosition = displayRingBuffer.bufferLength - 1;
		}
//		log.debug("SCS GUI Added " + numRead + " samples to display ring - writePosition is now "  +displayRingBuffer.writePosition );

		needsRepaint = true;
	}

	@Override
	public void setCaptureTimeMillis( final float captureMillis )
	{
		currentCaptureMillis = captureMillis;
		if( knownDataRate != null && bufferSampleFactory != null )
		{
			captureRenderLength = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( knownDataRate.getValue(), currentCaptureMillis );
			bufferSampleFactory.setCaptureRenderLength( captureRenderLength );
		}
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return true;
	}

}
