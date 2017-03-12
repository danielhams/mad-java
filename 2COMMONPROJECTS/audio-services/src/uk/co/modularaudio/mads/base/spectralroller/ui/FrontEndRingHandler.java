package uk.co.modularaudio.mads.base.spectralroller.ui;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.spectralroller.mu.SpectralRollerMadInstance;
import uk.co.modularaudio.util.audio.buffer.UnsafeFloatRingBuffer;
import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.mad.buffer.BackendToFrontendDataRingBuffer;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;

public class FrontEndRingHandler
{
	private final static Log log = LogFactory.getLog( FrontEndRingHandler.class );

	private final SpectralRollerMadInstance instance;
	private final SpectralRollerDisplayUiJComponent uiDisplay;

	private UnsafeFloatRingBuffer displayRingBuffer = null;
	private BackendToFrontendDataRingBuffer instanceRingBuffer = null;

	public FrontEndRingHandler( final SpectralRollerMadInstance instance,
			final SpectralRollerDisplayUiJComponent uiDisplay )
	{
		this.instance = instance;
		this.uiDisplay = uiDisplay;
	}

	public void receiveStartup( final HardwareIOChannelSettings ratesAndLatency,
			final MadTimingParameters timingParameters )
	{
		final DataRate knownDataRate = ratesAndLatency.getAudioChannelSetting().getDataRate();

		final int maxCaptureBufferLength = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( knownDataRate.getValue(),
				SpectralRollerMadUiInstance.MAX_CAPTURE_MILLIS + 100 );

		displayRingBuffer = new UnsafeFloatRingBuffer( maxCaptureBufferLength, true );

		instanceRingBuffer = instance.getDataRingBuffer();
	}

	public void receiveStop()
	{
	}

	public void receiveBufferIndexUpdate( final int U_updateTimestamp,
			final int writeIndex )
	{
		final int numReadable = instanceRingBuffer.frontEndGetNumReadableWithWriteIndex( writeIndex );

		final int spaceAvailable = displayRingBuffer.getNumWriteable();
		final int spaceToFree = numReadable - spaceAvailable;
		if( spaceToFree > 0 )
		{
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

		uiDisplay.setNeedsRepaint( true );
	}

	public UnsafeFloatRingBuffer getDisplayRingBuffer()
	{
		return displayRingBuffer;
	}

}
