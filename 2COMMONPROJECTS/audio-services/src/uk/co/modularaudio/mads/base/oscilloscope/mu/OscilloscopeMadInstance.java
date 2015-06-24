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

package uk.co.modularaudio.mads.base.oscilloscope.mu;

import java.util.ArrayList;
import java.util.Map;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
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
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class OscilloscopeMadInstance extends MadInstance<OscilloscopeMadDefinition,OscilloscopeMadInstance>
{
//	private static Log log = LogFactory.getLog( OscilloscopeMadInstance.class.getName() );

	protected int sampleRate;

	protected OscilloscopeWriteableScopeData scopeData;

	protected boolean active;

	protected ArrayList<OscilloscopeWriteableScopeData> bufferedScopeData = new ArrayList<OscilloscopeWriteableScopeData>();

	protected OscilloscopeCaptureTriggerEnum captureTrigger = OscilloscopeCaptureTriggerEnum.NONE;
	protected OscilloscopeCaptureRepetitionsEnum captureRepetitions = OscilloscopeCaptureRepetitionsEnum.CONTINOUS;

	protected OscilloscopeProcessor oscProc;

	public OscilloscopeMadInstance( final BaseComponentsCreationContext creationContext,
			final String instanceName,
			final OscilloscopeMadDefinition definition,
			final Map<MadParameterDefinition, String> creationParameterValues,
			final MadChannelConfiguration channelConfiguration )
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );
	}

	@Override
	public void startup( final HardwareIOChannelSettings hardwareChannelSettings, final MadTimingParameters timingParameters, final MadFrameTimeFactory frameTimeFactory )
			throws MadProcessingException
	{
		sampleRate = hardwareChannelSettings.getAudioChannelSetting().getDataRate().getValue();
		scopeData = null;
		oscProc = new OscilloscopeProcessor( this, bufferedScopeData );
//		log.warn("Fix me or delete me!");
	}

	@Override
	public void stop() throws MadProcessingException
	{
	}

	@Override
	public RealtimeMethodReturnCodeEnum process( final ThreadSpecificTemporaryEventStorage tempQueueEntryStorage,
			final MadTimingParameters timingParameters,
			final long periodStartFrameTime,
			final MadChannelConnectedFlags channelConnectedFlags,
			final MadChannelBuffer[] channelBuffers,
			final int frameOffset,
			final int numFrames )
	{
		final boolean inTriggerConnected = channelConnectedFlags.get( OscilloscopeMadDefinition.CONSUMER_CV_TRIGGER );
		final MadChannelBuffer inTriggerChannelBuffer = channelBuffers[ OscilloscopeMadDefinition.CONSUMER_CV_TRIGGER ];
		final float[] inTriggerFloats = (inTriggerConnected ? inTriggerChannelBuffer.floatBuffer : null );

		final boolean in0Connected = channelConnectedFlags.get( OscilloscopeMadDefinition.CONSUMER_AUDIO_SIGNAL0 );
		final MadChannelBuffer in0ChannelBuffer = channelBuffers[ OscilloscopeMadDefinition.CONSUMER_AUDIO_SIGNAL0 ];
		final float[] in0Floats = (in0Connected ? in0ChannelBuffer.floatBuffer : null );
		final boolean in0CvConnected = channelConnectedFlags.get( OscilloscopeMadDefinition.CONSUMER_CV_SIGNAL0 );
		final MadChannelBuffer in0CvChannelBuffer = channelBuffers[ OscilloscopeMadDefinition.CONSUMER_CV_SIGNAL0 ];
		final float[] in0CvFloats = (in0CvConnected ? in0CvChannelBuffer.floatBuffer : null );

		final boolean in1Connected = channelConnectedFlags.get( OscilloscopeMadDefinition.CONSUMER_AUDIO_SIGNAL1 );
		final MadChannelBuffer in1ChannelBuffer = channelBuffers[ OscilloscopeMadDefinition.CONSUMER_AUDIO_SIGNAL1 ];
		final float[] in1Floats = (in1Connected ? in1ChannelBuffer.floatBuffer : null );
		final boolean in1CvConnected = channelConnectedFlags.get( OscilloscopeMadDefinition.CONSUMER_CV_SIGNAL1 );
		final MadChannelBuffer in1CvChannelBuffer = channelBuffers[ OscilloscopeMadDefinition.CONSUMER_CV_SIGNAL1 ];
		final float[] in1CvFloats = (in1CvConnected ? in1CvChannelBuffer.floatBuffer : null );

		if( active )
		{
			oscProc.processPeriod( captureTrigger,
					frameOffset,
					numFrames,
					tempQueueEntryStorage,
					periodStartFrameTime,
					inTriggerConnected, inTriggerFloats,
					in0Connected, in0Floats,
					in0CvConnected, in0CvFloats,
					in1Connected, in1Floats,
					in1CvConnected, in1CvFloats );
		}
		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	protected void emitScopeDataToUi( final ThreadSpecificTemporaryEventStorage tstes, final long frameTime, final OscilloscopeWriteableScopeData scopeData )
	{
		localBridge.queueTemporalEventToUi( tstes, frameTime, OscilloscopeIOQueueBridge.COMMAND_OUT_SCOPE_DATA, -1, scopeData );
	}
}
