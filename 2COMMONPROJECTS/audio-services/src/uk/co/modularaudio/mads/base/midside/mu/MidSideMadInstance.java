package uk.co.modularaudio.mads.base.midside.mu;

import java.util.Map;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.util.audio.dsp.MidSideProcessor;
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

public class MidSideMadInstance extends MadInstance<MidSideMadDefinition, MidSideMadInstance>
{
	private boolean isLrToMs = true;

	public MidSideMadInstance( final BaseComponentsCreationContext creationContext,
			final String instanceName,
			final MidSideMadDefinition definition,
			final Map<MadParameterDefinition, String> creationParameterValues,
			final MadChannelConfiguration channelConfiguration )
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );
	}

	@Override
	public void startup( final HardwareIOChannelSettings hardwareChannelSettings, final MadTimingParameters timingParameters,
			final MadFrameTimeFactory frameTimeFactory ) throws MadProcessingException
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
		final MadChannelBuffer in1mcb = channelBuffers[ MidSideMadDefinition.CONSUMER_AUDIO_IN_1 ];
		final float[] in1Floats = in1mcb.floatBuffer;
		final MadChannelBuffer in2mcb = channelBuffers[ MidSideMadDefinition.CONSUMER_AUDIO_IN_2 ];
		final float[] in2Floats = in2mcb.floatBuffer;
		final MadChannelBuffer out1mcb = channelBuffers[ MidSideMadDefinition.PRODUCER_AUDIO_OUT_1 ];
		final float[] out1Floats = out1mcb.floatBuffer;
		final MadChannelBuffer out2mcb = channelBuffers[ MidSideMadDefinition.PRODUCER_AUDIO_OUT_2 ];
		final float[] out2Floats = out2mcb.floatBuffer;

		if( isLrToMs )
		{
			MidSideProcessor.lrToMs( in1Floats, in2Floats, out1Floats, out2Floats, frameOffset, numFrames );
		}
		else
		{
			MidSideProcessor.msToLr( in1Floats, in2Floats, out1Floats, out2Floats, frameOffset, numFrames );
		}
		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	@Override
	public void stop() throws MadProcessingException
	{
	}

	public void setMidSideType( final boolean isLrToMsIn )
	{
		this.isLrToMs = isLrToMsIn;
	}
}
