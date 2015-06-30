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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.spectralamp.mu.SpectralAmpIOQueueBridge;
import uk.co.modularaudio.mads.base.spectralamp.mu.SpectralAmpMadDefinition;
import uk.co.modularaudio.mads.base.spectralamp.mu.SpectralAmpMadInstance;
import uk.co.modularaudio.mads.base.spectralamp.ui.SpectralAmpAmpMappingChoiceUiJComponent.AmpMapping;
import uk.co.modularaudio.mads.base.spectralamp.ui.SpectralAmpAmpMaxChoiceUiJComponent.AmpMax;
import uk.co.modularaudio.mads.base.spectralamp.ui.SpectralAmpAmpMinChoiceUiJComponent.AmpMin;
import uk.co.modularaudio.mads.base.spectralamp.ui.SpectralAmpFreqMappingChoiceUiJComponent.FreqMapping;
import uk.co.modularaudio.mads.base.spectralamp.ui.SpectralAmpRunningAverageChoiceUiJComponent.RunningAverage;
import uk.co.modularaudio.mads.base.spectralamp.util.SpecDataListener;
import uk.co.modularaudio.mads.base.spectralamp.util.SpectralPeakAmpAccumulator;
import uk.co.modularaudio.util.audio.buffer.UnsafeFloatRingBuffer;
import uk.co.modularaudio.util.audio.fft.FftWindow;
import uk.co.modularaudio.util.audio.fft.HannFftWindow;
import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNoNameChangeNonConfigurableMadUiInstance;
import uk.co.modularaudio.util.audio.mad.buffer.BackendToFrontendDataRingBuffer;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEventUiConsumer;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.spectraldisplay.ampscale.AmpScaleComputer;
import uk.co.modularaudio.util.audio.spectraldisplay.ampscale.LinearAmpScaleComputer;
import uk.co.modularaudio.util.audio.spectraldisplay.ampscale.LogarithmicDbAmpScaleComputer;
import uk.co.modularaudio.util.audio.spectraldisplay.ampscale.LogarithmicNaturalAmpScaleComputer;
import uk.co.modularaudio.util.audio.spectraldisplay.freqscale.FrequencyScaleComputer;
import uk.co.modularaudio.util.audio.spectraldisplay.freqscale.LinearFreqScaleComputer;
import uk.co.modularaudio.util.audio.spectraldisplay.freqscale.LogarithmicFreqScaleComputer;
import uk.co.modularaudio.util.audio.spectraldisplay.runav.FallComputer;
import uk.co.modularaudio.util.audio.spectraldisplay.runav.FastFallComputer;
import uk.co.modularaudio.util.audio.spectraldisplay.runav.LongAverageComputer;
import uk.co.modularaudio.util.audio.spectraldisplay.runav.NoAverageComputer;
import uk.co.modularaudio.util.audio.spectraldisplay.runav.PeakHoldComputer;
import uk.co.modularaudio.util.audio.spectraldisplay.runav.RunningAverageComputer;
import uk.co.modularaudio.util.audio.spectraldisplay.runav.ShortAverageComputer;
import uk.co.modularaudio.util.audio.stft.StftParameters;
import uk.co.modularaudio.util.audio.stft.streaming.StreamingWolaProcessor;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;

public class SpectralAmpMadUiInstance extends
		AbstractNoNameChangeNonConfigurableMadUiInstance<SpectralAmpMadDefinition, SpectralAmpMadInstance>
		implements IOQueueEventUiConsumer<SpectralAmpMadInstance>
{
	private static Log log = LogFactory.getLog( SpectralAmpMadUiInstance.class.getName() );

	public static final float MAX_CAPTURE_MILLIS = 5000.0f;

	private DataRate dataRate = DataRate.SR_44100;
	private int maxCaptureBufferLength;
	private UnsafeFloatRingBuffer frontendRingBuffer;
	private BackendToFrontendDataRingBuffer backendRingBuffer;

	// Things the UI sets
	private int desiredFftSize = SpectralAmpFFTResolutionChoiceUiJComponent.DEFAULT_RESOLUTION;

	// How the frequency scale is computed
	private final FrequencyScaleComputer linearFreqScaleComputer = new LinearFreqScaleComputer();
	private final FrequencyScaleComputer logFreqScaleComputer = new LogarithmicFreqScaleComputer();

	private FrequencyScaleComputer desiredFreqScaleComputer = logFreqScaleComputer;

	// How the amplitude scale is computed
	private final AmpScaleComputer linearAmpScaleComputer = new LinearAmpScaleComputer();
	private final AmpScaleComputer logAmpScaleComputer = new LogarithmicNaturalAmpScaleComputer();
	private final AmpScaleComputer logDbAmpScaleComputer = new LogarithmicDbAmpScaleComputer();

	private AmpScaleComputer desiredAmpScaleComputer = logAmpScaleComputer;

	// How the running average is computed
	private final RunningAverageComputer noAverageComputer = new NoAverageComputer();
	private final RunningAverageComputer shortAverageComputer = new ShortAverageComputer();
	private final RunningAverageComputer longAverageComputer = new LongAverageComputer();
	private final RunningAverageComputer fallComputer = new FallComputer();
	private final RunningAverageComputer fastFallComputer = new FastFallComputer();
	private final PeakHoldComputer peakHoldComputer = new PeakHoldComputer();

	private RunningAverageComputer desiredRunningAverageComputer = fastFallComputer;

	// The FFT processor and bits used to pull out the amplitudes
	private StreamingWolaProcessor wolaProcessor;
	private SpecDataListener specDataListener;
	private final float[][] wolaArray = new float[1][];
	private SpectralPeakAmpAccumulator peakAmpAccumulator;
	private int currentNumBins = 0;

	private float desiredAmpMinDb = SpectralAmpAmpMinChoiceUiJComponent.DEFAULT_AMP_MIN.getDb();
	private float desiredAmpMaxDb = SpectralAmpAmpMaxChoiceUiJComponent.DEFAULT_AMP_MAX.getDb();
	private final List<AmpAxisChangeListener> ampAxisChangeListeners = new ArrayList<>();
	private final List<FreqAxisChangeListener> freqAxisChangeListeners = new ArrayList<>();
	private final List<RunningAvChangeListener> runAvChangeListeners = new ArrayList<>();

	public SpectralAmpMadUiInstance( final SpectralAmpMadInstance instance,
			final SpectralAmpMadUiDefinition uiDefinition )
	{
		super( uiDefinition.getCellSpan(), instance, uiDefinition );
		initialiseBuffers();

		reinitialiseFrequencyProcessor();
	}

	@Override
	public void receiveStartup( final HardwareIOChannelSettings ratesAndLatency,
			final MadTimingParameters timingParameters,
			final MadFrameTimeFactory frameTimeFactory )
	{
		super.receiveStartup( ratesAndLatency, timingParameters, frameTimeFactory );
		dataRate = ratesAndLatency.getAudioChannelSetting().getDataRate();
		initialiseBuffers();

		// Notify the frequency axis listeners we've started since the
		// frequency scale depends on the sample rate we're running at
		for( final FreqAxisChangeListener facl : freqAxisChangeListeners )
		{
			facl.receiveDataRateChange( dataRate );
		}
	}

	private void initialiseBuffers()
	{
		maxCaptureBufferLength = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( dataRate.getValue(),
				MAX_CAPTURE_MILLIS + 100 );
		frontendRingBuffer = new UnsafeFloatRingBuffer( maxCaptureBufferLength, true );
		backendRingBuffer = instance.getDataRingBuffer();
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters, final long currentGuiTick )
	{
		localQueueBridge.receiveQueuedEventsToUi( tempEventStorage, instance, this );

		super.doDisplayProcessing( tempEventStorage, timingParameters, currentGuiTick );

		if( peakAmpAccumulator.hasNewAmps() )
		{
			final float[][] computedAmps = peakAmpAccumulator.getComputedAmpsMarkTaken();
			specDataListener.processScopeData( computedAmps[0] );
			// log.trace("Got new amps - first value is " + computedAmps[0][0]
			// );
		}
	}

	public final void reinitialiseFrequencyProcessor()
	{
		try
		{
			final int fftSize = desiredFftSize;
			final int windowLength = (fftSize >= SpectralAmpMadDefinition.MAX_WINDOW_LENGTH ? SpectralAmpMadDefinition.MAX_WINDOW_LENGTH
					: fftSize);
			final FftWindow hannWindow = new HannFftWindow( windowLength );

			final StftParameters params = new StftParameters( dataRate, 1, windowLength,
					SpectralAmpMadDefinition.NUM_OVERLAPS, fftSize, hannWindow );

			currentNumBins = params.getNumBins();

			peakAmpAccumulator = new SpectralPeakAmpAccumulator();
			wolaProcessor = new StreamingWolaProcessor( params, peakAmpAccumulator );
		}
		catch (final Exception e)
		{
			final String msg = "Exception caught reinitialising frequency processor" + e.toString();
			log.error( msg, e );
		}
	}

	public void setDesiredFftSize( final int resolution )
	{
		this.desiredFftSize = resolution;
		reinitialiseFrequencyProcessor();
		specDataListener.setNumBins( currentNumBins );
	}

	@Override
	public void consumeQueueEntry( final SpectralAmpMadInstance instance, final IOQueueEvent nextOutgoingEntry )
	{
		switch (nextOutgoingEntry.command)
		{
			case SpectralAmpIOQueueBridge.COMMAND_OUT_RINGBUFFER_WRITE_INDEX:
			{
				final long value = nextOutgoingEntry.value;
				final int bufferNum = (int) ((value) & 0xFFFFFFFF);
				final int ringBufferIndex = (int) ((value >> 32) & 0xFFFFFFFF);

				if (bufferNum == 0)
				{
					receiveBufferIndexUpdate( nextOutgoingEntry.frameTime, ringBufferIndex );
				}
				break;
			}
			default:
			{
				if( log.isErrorEnabled() )
				{
					log.error( "Unknown output command: " + nextOutgoingEntry.command );
				}
				break;
			}
		}
	}

	private void receiveBufferIndexUpdate( final long indexUpdateTimestamp, final int writeIndex )
	{
		final int numReadable = backendRingBuffer.getNumReadableWithWriteIndex( writeIndex );

		final int spaceAvailable = frontendRingBuffer.getNumWriteable();
		if( spaceAvailable < numReadable )
		{
			final int spaceToFree = numReadable - spaceAvailable;
//			log.trace("Moving forward " + spaceToFree + " floats");
			frontendRingBuffer.moveForward( spaceToFree );
		}

		final int numRead = backendRingBuffer.readToRingWithWriteIndex( writeIndex, frontendRingBuffer, numReadable );
		if( numRead != numReadable )
		{
			if( log.isWarnEnabled() )
			{
				log.warn( "Expected " + numReadable + " from mad instance ring but read " + numRead );
			}
			Arrays.fill( frontendRingBuffer.buffer, 0.0f );
			frontendRingBuffer.readPosition = 0;
			frontendRingBuffer.writePosition = frontendRingBuffer.bufferLength - 1;
		}
		else
		{
			// Need to pass new data to the wola and check if there is new data to be displayed here
//			log.trace( "Successfully passed " + numRead + " samples from mad instance to UI ring buffer" );
			final int ferbWp = frontendRingBuffer.writePosition;

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

			final int numWrappedRead = numReadable - numStraightRead;

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
//				if( log.isDebugEnabled() )
//				{
//					log.debug("(2)Pushing " + numWrappedRead + " wrapped frames into wola processor");
//				}
				wolaProcessor.write(  wolaArray, 0, numWrappedRead, 1.0, 1.0 );
			}
		}

	}

	public void setSpecDataListener( final SpecDataListener specDataListener )
	{
		this.specDataListener = specDataListener;
		specDataListener.setNumBins( currentNumBins );
	}

	public void sendUiActive( final boolean showing )
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

	public void setDesiredRunningAverageComputer( final RunningAverageComputer desiredRunningAverageComputer )
	{
		this.desiredRunningAverageComputer = desiredRunningAverageComputer;
		reinitialiseFrequencyProcessor();
		for( final RunningAvChangeListener racl : runAvChangeListeners )
		{
			racl.receiveRunAvComputer( desiredRunningAverageComputer );
		}
	}

	public void resetPeakHoldComputer()
	{
		peakHoldComputer.reset();
	}

	public void addAmpAxisChangeListener( final AmpAxisChangeListener cl )
	{
		ampAxisChangeListeners.add( cl );
		cl.receiveAmpScaleChange();
	}

	public void setDesiredAmpMax( final AmpMax al )
	{
		this.desiredAmpMaxDb = al.getDb();
		this.desiredAmpScaleComputer.setMinMaxDb( desiredAmpMinDb, desiredAmpMaxDb );
		for( final AmpAxisChangeListener cl : ampAxisChangeListeners )
		{
			cl.receiveAmpScaleChange();
		}
	}

	public void setDesiredAmpMin( final AmpMin am )
	{
		this.desiredAmpMinDb = am.getDb();
		this.desiredAmpScaleComputer.setMinMaxDb( desiredAmpMinDb, desiredAmpMaxDb );
		for( final AmpAxisChangeListener cl : ampAxisChangeListeners )
		{
			cl.receiveAmpScaleChange();
		}
	}

	public void addFreqAxisChangeListener( final FreqAxisChangeListener cl )
	{
		freqAxisChangeListeners.add( cl );
		cl.receiveFreqScaleComputer( desiredFreqScaleComputer );
		cl.receiveDataRateChange( dataRate );
	}

	public void addRunAvChangeListener( final RunningAvChangeListener racl )
	{
		runAvChangeListeners.add( racl );
		racl.receiveRunAvComputer( desiredRunningAverageComputer );
	}

	public void setDesiredAmpMapping( final AmpMapping mapping )
	{
		switch( mapping )
		{
			case LINEAR:
			{
				desiredAmpScaleComputer = linearAmpScaleComputer;
				break;
			}
			case LOG:
			default:
			{
				desiredAmpScaleComputer = logAmpScaleComputer;
				break;
			}
			case LOG_DB:
			{
				desiredAmpScaleComputer = logDbAmpScaleComputer;
				break;
			}
		}
		desiredAmpScaleComputer.setMinMaxDb( desiredAmpMinDb, desiredAmpMaxDb );

		for( final AmpAxisChangeListener cl : ampAxisChangeListeners )
		{
			cl.receiveAmpScaleChange();
		}
	}

	public void setDesiredFreqMapping( final FreqMapping mapping )
	{
		switch( mapping )
		{
			case LINEAR:
			{
				desiredFreqScaleComputer = linearFreqScaleComputer;
				break;
			}
			case LOG:
			default:
			{
				desiredFreqScaleComputer = logFreqScaleComputer;
				break;
			}
		}

		for( final FreqAxisChangeListener fl : freqAxisChangeListeners )
		{
			fl.receiveFreqScaleComputer( desiredFreqScaleComputer );
		}
	}

	public void setDesiredRunningAverage( final RunningAverage runningAverage )
	{
		switch( runningAverage )
		{
			case OFF:
			{
				desiredRunningAverageComputer = noAverageComputer;
				break;
			}
			case SHORT:
			{
				desiredRunningAverageComputer = shortAverageComputer;
				break;
			}
			case LONG:
			{
				desiredRunningAverageComputer = longAverageComputer;
				break;
			}
			case FALL:
			{
				desiredRunningAverageComputer = fallComputer;
				break;
			}
			case FAST_FALL:
			default:
			{
				desiredRunningAverageComputer = fastFallComputer;
				break;
			}
			case PEAK_HOLD:
			{
				desiredRunningAverageComputer = peakHoldComputer;
				break;
			}
		}

		for( final RunningAvChangeListener rcl : runAvChangeListeners )
		{
			rcl.receiveRunAvComputer( desiredRunningAverageComputer );
		}

	}
}
