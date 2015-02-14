package uk.co.modularaudio.mads.base.mixern.mu;

import uk.co.modularaudio.util.audio.mad.MadClassification;
import uk.co.modularaudio.util.audio.mad.helper.AbstractNonConfigurableMadDefinition;

public class MixerNMadDefinition<D extends MixerNMadDefinition<D,I>, I extends MixerNMadInstance<D,I>>
	extends AbstractNonConfigurableMadDefinition<D,I>
{
	private final MixerNInstanceConfiguration instanceConfiguration;

	public MixerNMadDefinition( final String definitionId,
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
		this.instanceConfiguration = instanceConfiguration;
	}

	public MixerNInstanceConfiguration getMixerInstanceConfiguration()
	{
		return instanceConfiguration;
	}
}
