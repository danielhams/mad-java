package uk.co.modularaudio.mads.base.mixer8.mu;

import java.util.Map;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.mads.base.mixern.mu.MixerNMadInstance;
import uk.co.modularaudio.util.audio.mad.MadChannelConfiguration;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;

public class Mixer8MadInstance extends MixerNMadInstance<Mixer8MadDefinition, Mixer8MadInstance>
{
	public Mixer8MadInstance( final BaseComponentsCreationContext creationContext,
			final String instanceName,
			final Mixer8MadDefinition definition,
			final Map<MadParameterDefinition, String> creationParameterValues,
			final MadChannelConfiguration channelConfiguration )
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );
	}

}
