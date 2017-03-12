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

package uk.co.modularaudio.mads.base.spectralroller.ui;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.spectralroller.mu.SpectralRollerMadDefinition;
import uk.co.modularaudio.mads.base.spectralroller.mu.SpectralRollerMadInstance;
import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.mad.rollpainter.RollPainter;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacPanel;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;

public class SpectralRollerDisplayUiJComponent extends PacPanel
	implements IMadUiControlInstance<SpectralRollerMadDefinition, SpectralRollerMadInstance, SpectralRollerMadUiInstance>
{
	private static final long serialVersionUID = -3967953700161199566L;

	private static Log log = LogFactory.getLog(  SpectralRollerDisplayUiJComponent.class.getName( ) );

	private final SpectralRollerMadUiInstance uiInstance;

	private DataRate knownDataRate;

	private float currentCaptureMillis = 1.0f;
	private int captureRenderLength;

	// Ui display bits
	private boolean previouslyShowing;

	private final BufferedImageAllocator bufferImageAllocator;

	boolean needsRepaint;

	private Rectangle bounds;
	private int imageWidth, imageHeight;
	private float valueScaleForMargins;

	private final FrontEndRingHandler frontEndRingHandler;

	private SpectralRollerBufferSampleFactory bufferSampleFactory;
	private RollPainter<SpectralRollerBuffer, SpectralRollerBufferCleaner> rollPainter;

	public SpectralRollerDisplayUiJComponent(
			final SpectralRollerMadDefinition definition,
			final SpectralRollerMadInstance instance,
			final SpectralRollerMadUiInstance uiInstance,
			final int controlIndex )
	{
		this.setOpaque( true );
		this.uiInstance = uiInstance;

		this.frontEndRingHandler = new FrontEndRingHandler( instance, this );

		this.bufferImageAllocator = ((SpectralRollerMadUiDefinition)uiInstance.getUiDefinition()).getBufferedImageAllocator();

		uiInstance.setUiDisplay( this );
	}

	@Override
	public void doDisplayProcessing(final ThreadSpecificTemporaryEventStorage tempEventStorage ,
			final MadTimingParameters timingParameters ,
			final int U_currentGuiTime , final int framesSinceLastTick )
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
				g.drawImage( rollPainter.buffer1.bi, rollPainter.buffer1XOffset, 0, null );
			}
		}
		else
		{
			g.setColor( SpectralRollerColours.BACKGROUND_COLOR );
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
		valueScaleForMargins = (imageHeight - SpectralRollerScaleDisplay.SCALE_MARGIN * 2) / (float)imageHeight;
	}

	public void receiveStartup( final HardwareIOChannelSettings ratesAndLatency, final MadTimingParameters timingParameters )
	{
		knownDataRate = ratesAndLatency.getAudioChannelSetting().getDataRate();

		captureRenderLength = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( knownDataRate.getValue(), currentCaptureMillis );

		if( imageWidth > 0 && imageHeight > 0 )
		{
			bufferSampleFactory = new SpectralRollerBufferSampleFactory( uiInstance,
					bufferImageAllocator,
					frontEndRingHandler.getDisplayRingBuffer(),
					bounds,
					valueScaleForMargins );

			bufferSampleFactory.setCaptureRenderLength( captureRenderLength );
			try
			{
				rollPainter = new RollPainter<SpectralRollerBuffer, SpectralRollerBufferCleaner>( imageWidth, bufferSampleFactory );
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

	protected void setNeedsRepaint( final boolean need )
	{
		this.needsRepaint = need;
	}

	public FrontEndRingHandler getUiRingHandler()
	{
		return frontEndRingHandler;
	}

}
