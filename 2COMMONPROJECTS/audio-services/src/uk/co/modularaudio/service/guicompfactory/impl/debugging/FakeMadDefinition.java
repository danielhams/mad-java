package uk.co.modularaudio.service.guicompfactory.impl.debugging;

import java.util.ArrayList;
import java.util.Map;

import uk.co.modularaudio.util.audio.mad.MadChannelConfiguration;
import uk.co.modularaudio.util.audio.mad.MadChannelDefinition;
import uk.co.modularaudio.util.audio.mad.MadClassification;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.ioqueue.MadNullLocklessQueueBridge;

class FakeMadDefinition extends MadDefinition<FakeMadDefinition, FakeMadInstance>
{

	public FakeMadDefinition( final MadClassification classification )
	{
		super( FakeRackComponent.fakeStr,
				FakeRackComponent.fakeStr,
				false,
				classification,
				new ArrayList<MadParameterDefinition>( 0 ),
				new MadNullLocklessQueueBridge<FakeMadInstance>() );
	}

	@Override
	public MadChannelConfiguration getChannelConfigurationForParameters(
			final Map<MadParameterDefinition, String> parameterValues ) throws MadProcessingException
	{
		return new MadChannelConfiguration( new MadChannelDefinition[0] );
	}
}