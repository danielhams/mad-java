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

package uk.co.modularaudio.mads.base.spectralamp.ui;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.spectralamp.mu.SpectralAmpMadDefinition;
import uk.co.modularaudio.mads.base.spectralamp.mu.SpectralAmpMadInstance;
import uk.co.modularaudio.mads.base.spectralamp.mu.SpectralAmpIOQueueBridge;
import uk.co.modularaudio.mads.base.spectralamp.util.SpecDataListener;
import uk.co.modularaudio.mads.base.spectralamp.util.SpectralPeakAmpAccumulator;
import uk.co.modularaudio.util.audio.buffer.UnsafeFloatRingBuffer;
import uk.co.modularaudio.util.audio.fft.FftWindow;
import uk.co.modularaudio.util.audio.fft.HannFftWindow;
import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiInstance;
import uk.co.modularaudio.util.audio.logdisplay.ampscale.AmpScaleComputer;
import uk.co.modularaudio.util.audio.logdisplay.freqscale.FrequencyScaleComputer;
import uk.co.modularaudio.util.audio.logdisplay.runav.RunningAverageComputer;
import uk.co.modularaudio.util.audio.mad.buffer.BackendToFrontendDataRingBuffer;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEventUiConsumer;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.stft.StftParameters;
import uk.co.modularaudio.util.audio.stft.streaming.StreamingWolaProcessor;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;

public class SpectralAmpMadUiInstance extends
		AbstractNonConfigurableMadUiInstance<SpectralAmpMadDefinition, SpectralAmpMadInstance>
		implements IOQueueEventUiConsumer<SpectralAmpMadInstance>
{
	private static Log log = LogFactory.getLog( SpectralAmpMadUiInstance.class.getName() );

	public static final float MAX_CAPTURE_MILLIS = 5000.0f;

	private DataRate dataRate = DataRate.SR_44100;
	private int maxCaptureBufferLength;
	private UnsafeFloatRingBuffer frontendRingBuffer;
	private BackendToFrontendDataRingBuffer backendRingBuffer;

	// Things the UI sets
	private int desiredFftSize = 0;
	private FrequencyScaleComputer desiredFreqScaleComputer = null;
	private AmpScaleComputer desiredAmpScaleComputer = null;
	private RunningAverageComputer desiredRunningAverageComputer = null;

	private StreamingWolaProcessor wolaProcessor = null;
	private SpecDataListener specDataListener = null;
	private float[][] wolaArray = new float[1][];
	private SpectralPeakAmpAccumulator peakAmpAccumulator;

	public SpectralAmpMadUiInstance( SpectralAmpMadInstance instance,
			SpectralAmpMadUiDefinition uiDefinition )
	{
		super( uiDefinition.getCellSpan(), instance, uiDefinition );
		initialiseBuffers();
	}

	@Override
	public void receiveStartup( final HardwareIOChannelSettings ratesAndLatency,
			final MadTimingParameters timingParameters, final MadFrameTimeFactory frameTimeFactory )
	{
		super.receiveStartup( ratesAndLatency, timingParameters, frameTimeFactory );
		dataRate = ratesAndLatency.getAudioChannelSetting().getDataRate();
		initialiseBuffers();
	}

	private void initialiseBuffers()
	{
		maxCaptureBufferLength = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( dataRate.getValue(),
				MAX_CAPTURE_MILLIS + 100 );
		frontendRingBuffer = new UnsafeFloatRingBuffer( maxCaptureBufferLength, true );
		backendRingBuffer = instance.getDataRingBuffer();
	}

	@Override
	public void doDisplayProcessing( ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters, final long currentGuiTick )
	{
		localQueueBridge.receiveQueuedEventsToUi( tempEventStorage, instance, this );

		super.doDisplayProcessing( tempEventStorage, timingParameters, currentGuiTick );

		if( peakAmpAccumulator.hasNewAmps() )
		{
			float[][] computedAmps = peakAmpAccumulator.getComputedAmpsMarkTaken();
			specDataListener.processScopeData( computedAmps[0] );
			// log.trace("Got new amps - first value is " + computedAmps[0][0]
			// );
		}
	}

	public void reinitialiseFrequencyProcessor()
	{
		try
		{
			int fftSize = desiredFftSize;
			int windowLength = (fftSize >= SpectralAmpMadDefinition.MAX_WINDOW_LENGTH ? SpectralAmpMadDefinition.MAX_WINDOW_LENGTH
					: fftSize);
			FftWindow hannWindow = new HannFftWindow( windowLength );

			StftParameters params = new StftParameters( dataRate, 1, windowLength,
					SpectralAmpMadDefinition.NUM_OVERLAPS, fftSize, hannWindow );

			peakAmpAccumulator = new SpectralPeakAmpAccumulator();
			wolaProcessor = new StreamingWolaProcessor( params, peakAmpAccumulator );
		}
		catch (Exception e)
		{
			String msg = "Exception caught reinitialising frequency processor" + e.toString();
			log.error( msg, e );
		}
	}

	public void setDesiredFftSize( int resolution )
	{
		this.desiredFftSize = resolution;
		reinitialiseFrequencyProcessor();
	}

	@Override
	public void consumeQueueEntry( SpectralAmpMadInstance instance, IOQueueEvent nextOutgoingEntry )
	{
		switch (nextOutgoingEntry.command)
		{
			case SpectralAmpIOQueueBridge.COMMAND_OUT_RINGBUFFER_WRITE_INDEX:
			{
				long value = nextOutgoingEntry.value;
				int bufferNum = (int) ((value) & 0xFFFFFFFF);
				int ringBufferIndex = (int) ((value >> 32) & 0xFFFFFFFF);

				if (bufferNum == 0)
				{
					receiveBufferIndexUpdate( nextOutgoingEntry.frameTime, ringBufferIndex );
				}
				break;
			}
			default:
			{
				log.error( "Unknown output command: " + nextOutgoingEntry.command );
				break;
			}
		}
	}

	private void receiveBufferIndexUpdate( long indexUpdateTimestamp, int writeIndex )
	{
		int numReadable = backendRingBuffer.getNumReadableWithWriteIndex( writeIndex );
		
		int spaceAvailable = frontendRingBuffer.getNumWriteable();
		if( spaceAvailable < numReadable )
		{
			int spaceToFree = numReadable - spaceAvailable;
//			log.trace("Moving forward " + spaceToFree + " floats");
			frontendRingBuffer.moveForward( spaceToFree );
		}
		
		int numRead = backendRingBuffer.readToRingWithWriteIndex( writeIndex, frontendRingBuffer, numReadable );
		if( numRead != numReadable )
		{
			log.warn( "Expected " + numReadable + " from mad instance ring but read " + numRead );
			Arrays.fill( frontendRingBuffer.buffer, 0.0f );
			frontendRingBuffer.readPosition = 0;
			frontendRingBuffer.writePosition = frontendRingBuffer.bufferLength - 1;
		}
		else
		{
			// Need to pass new data to the wola and check if there is new data to be displayed here
//			log.trace( "Successfully passed " + numRead + " samples from mad instance to UI ring buffer" );
			int ferbWp = frontendRingBuffer.writePosition;
			
			int readStartOffset = ferbWp - numReadable;
			if( readStartOffset < 0 )
			{
				readStartOffset += frontendRingBuffer.bufferLength;
			}
			
			int numStraightRead;

			if( ferbWp > readStartOffset )
			{
				// Straight read of how many we want
				numStraightRead = numReadable;
			}
			else
			{
				// Some wrapping going on
				numStraightRead = frontendRingBuffer.bufferLength - readStartOffset;
			}
			
			numStraightRead = (numStraightRead > numReadable ? numReadable : numStraightRead );

			int numWrappedRead = numReadable - numStraightRead;
			
			wolaArray[0] = frontendRingBuffer.buffer;

			if( numStraightRead > 0 )
			{
//				log.debug("(1)Pushing " + numStraightRead + " straight frames into wola processor");
				wolaProcessor.write(  wolaArray, readStartOffset, numStraightRead, 1.0, 1.0 );

				if( numWrappedRead > 0 )
				{
//					log.debug("Read start off set is " + readStartOffset );
//					log.debug("(1)Pushing " + numWrappedRead + " wrapped frames into wola processor");
					wolaProcessor.write(  wolaArray, 0, numWrappedRead, 1.0, 1.0 );
				}
			}
			else if( numWrappedRead > 0 )
			{
				log.debug("(2)Pushing " + numWrappedRead + " wrapped frames into wola processor");
				wolaProcessor.write(  wolaArray, 0, numWrappedRead, 1.0, 1.0 );
			}
		}

	}

	public void setSpecDataListener( SpecDataListener specDataListener )
	{
		this.specDataListener = specDataListener;
	}

	public void sendUiActive( boolean showing )
	{
		sendTemporalValueToInstance( SpectralAmpIOQueueBridge.COMMAND_IN_ACTIVE, (showing ? 1 : 0) );
	}

	public FrequencyScaleComputer getDesiredFreqScaleComputer()
	{
		return desiredFreqScaleComputer;
	}

	public AmpScaleComputer getDesiredAmpScaleComputer()
	{
		return desiredAmpScaleComputer;
	}

	public RunningAverageComputer getDesiredRunningAverageComputer()
	{
		return desiredRunningAverageComputer;
	}

	public void setDesiredAmpScaleComputer( AmpScaleComputer desiredAmpScaleComputer )
	{
		this.desiredAmpScaleComputer = desiredAmpScaleComputer;
		reinitialiseFrequencyProcessor();
	}

	public void setDesiredFreqScaleComputer( FrequencyScaleComputer desiredFreqScaleComputer )
	{
		this.desiredFreqScaleComputer = desiredFreqScaleComputer;
		reinitialiseFrequencyProcessor();
	}

	public void setDesiredRunningAverageComputer( RunningAverageComputer desiredRunningAverageComputer )
	{
		this.desiredRunningAverageComputer = desiredRunningAverageComputer;
		reinitialiseFrequencyProcessor();
	}

}
