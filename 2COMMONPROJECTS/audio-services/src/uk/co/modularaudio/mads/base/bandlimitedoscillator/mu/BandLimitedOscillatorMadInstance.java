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

package uk.co.modularaudio.mads.base.bandlimitedoscillator.mu;

import java.util.Map;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.util.audio.controlinterpolation.SpringAndDamperDouble24Interpolator;
import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.mad.MadChannelBuffer;
import uk.co.modularaudio.util.audio.mad.MadChannelConfiguration;
import uk.co.modularaudio.util.audio.mad.MadChannelConnectedFlags;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.mvc.displayslider.models.OscillatorFrequencySliderModel;
import uk.co.modularaudio.util.audio.mvc.displayslider.models.OscillatorPulseWidthSliderModel;
import uk.co.modularaudio.util.audio.oscillatortable.NoWaveTableForShapeException;
import uk.co.modularaudio.util.audio.oscillatortable.Oscillator;
import uk.co.modularaudio.util.audio.oscillatortable.OscillatorFactoryException;
import uk.co.modularaudio.util.audio.oscillatortable.OscillatorWaveShape;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class BandLimitedOscillatorMadInstance extends MadInstance<BandLimitedOscillatorMadDefinition,BandLimitedOscillatorMadInstance>
{
//	private static Log log = LogFactory.getLog( BandLimitedOscillatorMadInstance.class.getName() );

	private int sampleRate = DataRate.CD_QUALITY.getValue();

	private final BandLimitedOscillatorInstances oscillatorInstances;

	protected OscillatorWaveShape desiredWaveShape = OscillatorWaveShape.SAW;

	private OscillatorWaveShape usedWaveShape = OscillatorWaveShape.SAW;
	private Oscillator oscillator;

	private final SpringAndDamperDouble24Interpolator freqSad = new SpringAndDamperDouble24Interpolator();
	private final SpringAndDamperDouble24Interpolator pwSad = new SpringAndDamperDouble24Interpolator();

	public BandLimitedOscillatorMadInstance( final BaseComponentsCreationContext creationContext,
			final String instanceName,
			final BandLimitedOscillatorMadDefinition definition,
			final Map<MadParameterDefinition, String> creationParameterValues,
			final MadChannelConfiguration channelConfiguration )
		throws NoWaveTableForShapeException, OscillatorFactoryException
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );

		oscillatorInstances = new BandLimitedOscillatorInstances( creationContext.getOscillatorFactory() );

		freqSad.resetLowerUpperBounds( 0.0f, sampleRate / 2.0f );
		freqSad.hardSetValue( OscillatorFrequencySliderModel.DEFAULT_OSCILLATOR_FREQUENCY );
		pwSad.resetLowerUpperBounds( 0.01f, 1.0f );
		pwSad.hardSetValue( OscillatorPulseWidthSliderModel.DEFAULT_PULSE_WIDTH );
	}

	@Override
	public void start( final HardwareIOChannelSettings hardwareChannelSettings, final MadTimingParameters timingParameters, final MadFrameTimeFactory frameTimeFactory )
			throws MadProcessingException
	{
		try
		{
			sampleRate = hardwareChannelSettings.getAudioChannelSetting().getDataRate().getValue();

			oscillator = oscillatorInstances.getOscillator( usedWaveShape );

			freqSad.resetLowerUpperBounds( 0.0f, sampleRate / 2.0f );
		}
		catch (final Exception e)
		{
			throw new MadProcessingException( e );
		}
	}

	@Override
	public void stop() throws MadProcessingException
	{
	}

	@Override
	public RealtimeMethodReturnCodeEnum process( final ThreadSpecificTemporaryEventStorage tempQueueEntryStorage,
			final MadTimingParameters timingParameters,
			final int U_periodStartFrameTime,
			final MadChannelConnectedFlags channelConnectedFlags,
			final MadChannelBuffer[] channelBuffers,
			final int frameOffset,
			final int numFrames  )
	{
		final float[] tmpFloats = tempQueueEntryStorage.temporaryFloatArray;

		final boolean cvFreqConnected = channelConnectedFlags.get( BandLimitedOscillatorMadDefinition.CONSUMER_CV_FREQ );
		final MadChannelBuffer cvFreqBuf = channelBuffers[ BandLimitedOscillatorMadDefinition.CONSUMER_CV_FREQ ];
		float[] cvFreqFloats = cvFreqBuf.floatBuffer;

//		final boolean phaseConnected = channelConnectedFlags.get( BandLimitedOscillatorMadDefinition.CONSUMER_CV_PHASE );
//		final MadChannelBuffer phaseBuf = channelBuffers[ BandLimitedOscillatorMadDefinition.CONSUMER_CV_PHASE ];
//		final float[] phaseSamples = phaseBuf.floatBuffer;
//
//		final boolean triggerConnected = channelConnectedFlags.get(  BandLimitedOscillatorMadDefinition.CONSUMER_CV_TRIGGER );
//		final MadChannelBuffer triggerBuf = channelBuffers[ BandLimitedOscillatorMadDefinition.CONSUMER_CV_TRIGGER ];
//		final float[] triggerSamples = triggerBuf.floatBuffer;

		final boolean pwConnected = channelConnectedFlags.get( BandLimitedOscillatorMadDefinition.CONSUMER_CV_PULSEWIDTH );
		final MadChannelBuffer pwBuf = channelBuffers[ BandLimitedOscillatorMadDefinition.CONSUMER_CV_PULSEWIDTH ];
		float[] pwFloats = pwBuf.floatBuffer;

		final boolean audioOutConnected = channelConnectedFlags.get( BandLimitedOscillatorMadDefinition.PRODUCER_AUDIO_OUT );
		final MadChannelBuffer audioOutBuf = channelBuffers[ BandLimitedOscillatorMadDefinition.PRODUCER_AUDIO_OUT ];
		final float[] audioOutFloats = audioOutBuf.floatBuffer;

		final boolean cvOutConnected = channelConnectedFlags.get( BandLimitedOscillatorMadDefinition.PRODUCER_CV_OUT );
		final MadChannelBuffer cvOutBuf = channelBuffers[ BandLimitedOscillatorMadDefinition.PRODUCER_CV_OUT ];
		final float[] cvOutFloats = cvOutBuf.floatBuffer;

		int freqIndex = 0;
		boolean constantFreq = freqSad.checkForDenormal();
		freqSad.generateControlValues( tmpFloats, freqIndex, numFrames );
		int pwIndex = numFrames;
		boolean constantPw = pwSad.checkForDenormal();
		pwSad.generateControlValues( tmpFloats, pwIndex, numFrames );

		if( !audioOutConnected && !cvOutConnected )
		{
			// Do nothing, we have no output anyway
		}
		else
		{
			if( cvFreqConnected )
			{
				cvFreqFloats = cvFreqBuf.floatBuffer;
				freqIndex = frameOffset;
				constantFreq = false;
			}
			else
			{
				cvFreqFloats = tmpFloats;
			}

			if( pwConnected )
			{
				pwFloats = pwBuf.floatBuffer;
				pwIndex = frameOffset;
				constantPw = false;
			}
			else
			{
				pwFloats = tmpFloats;
			}

			// Need one of the buffers to render into
			final float[] genFloats = (audioOutConnected ? audioOutFloats : cvOutFloats );

			if( usedWaveShape != desiredWaveShape )
			{
				oscillator = oscillatorInstances.getOscillator( desiredWaveShape );
				usedWaveShape = desiredWaveShape;
			}

			// TODO: Fix trigger aspects

			if( !constantFreq )
			{
				if( !constantPw )
				{
					oscillator.oscillate( cvFreqFloats, freqIndex, 0.0f, pwFloats, pwIndex, genFloats, frameOffset, numFrames, sampleRate );
				}
				else
				{
					oscillator.oscillate( cvFreqFloats, freqIndex, 0.0f, pwFloats[pwIndex], genFloats, frameOffset, numFrames, sampleRate );
				}
			}
			else
			{
				if( !constantPw )
				{
					oscillator.oscillate( cvFreqFloats[freqIndex], 0.0f, pwFloats, pwIndex, genFloats, frameOffset, numFrames, sampleRate );
				}
				else
				{
					oscillator.oscillate( cvFreqFloats[freqIndex], 0.0f, pwFloats[pwIndex], genFloats, frameOffset, numFrames, sampleRate );
				}
			}

			if( audioOutConnected && cvOutConnected )
			{
				// We rendered into audio out, copy it over into the cv out
				System.arraycopy( audioOutFloats, frameOffset, cvOutFloats, frameOffset, numFrames );
			}
		}

		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	public void setDesiredFrequency( final float freq )
	{
		freqSad.notifyOfNewValue( freq );
	}

	public void setDesiredFrequencyImmediate( final float freq )
	{
		freqSad.hardSetValue( freq );
	}

	public void setDesiredPulseWidth( final float pw )
	{
		pwSad.notifyOfNewValue( pw );
	}
}
