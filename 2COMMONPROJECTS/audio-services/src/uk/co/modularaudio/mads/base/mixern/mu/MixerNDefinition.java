package uk.co.modularaudio.mads.base.mixern.mu;

import uk.co.modularaudio.util.audio.mad.MadClassification;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.helper.AbstractNonConfigurableMadDefinition;

public class MixerNDefinition<D extends MadDefinition<D,I>, I extends MixerNInstance<D,I>>
	extends AbstractNonConfigurableMadDefinition<D,I>
{
	public MixerNDefinition( final String definitionId,
			final String userVisibleName,
			final MadClassification classification,
			final MixerNInstanceConfiguration instanceConfiguration )
	{
		super( definitionId,
				userVisibleName,
				classification,
				new MixerNIOQueueBridge<I>(),
				instanceConfiguration.getNumTotalChannels(),
				instanceConfiguration.getChannelNames(),
				instanceConfiguration.getChannelTypes(),
				instanceConfiguration.getChannelDirections(),
				instanceConfiguration.getChannelPositions() );
	}
}
