package uk.co.modularaudio.service.guicompfactory.impl.memreduce.fakemad;

import java.util.HashMap;

import uk.co.modularaudio.util.audio.mad.MadChannelBuffer;
import uk.co.modularaudio.util.audio.mad.MadChannelConnectedFlags;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

class FakeMadInstance extends MadInstance<FakeMadDefinition, FakeMadInstance>
{

	public FakeMadInstance( final FakeMadDefinition definition ) throws MadProcessingException
	{
		super( FakeRackComponent.fakeStr, definition, new HashMap<MadParameterDefinition,String>(),
				definition.getChannelConfigurationForParameters( null ) );
	}

	@Override
	public void startup( final HardwareIOChannelSettings hardwareChannelSettings, final MadTimingParameters timingParameters,
			final MadFrameTimeFactory frameTimeFactory ) throws MadProcessingException
	{
	}

	@Override
	public RealtimeMethodReturnCodeEnum process( final ThreadSpecificTemporaryEventStorage tempQueueEntryStorage,
			final MadTimingParameters timingParameters, final long periodStartFrameTime,
			final MadChannelConnectedFlags channelConnectedFlags, final MadChannelBuffer[] channelBuffers, final int numFrames )
	{
		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	@Override
	public void stop() throws MadProcessingException
	{
	}
}