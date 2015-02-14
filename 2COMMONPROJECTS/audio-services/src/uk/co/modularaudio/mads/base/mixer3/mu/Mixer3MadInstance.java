package uk.co.modularaudio.mads.base.mixer3.mu;

import java.util.Map;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.mads.base.mixern.mu.MixerNMadInstance;
import uk.co.modularaudio.util.audio.mad.MadChannelConfiguration;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;

public class Mixer3MadInstance extends MixerNMadInstance<Mixer3MadDefinition, Mixer3MadInstance>
{
	public Mixer3MadInstance( final BaseComponentsCreationContext creationContext,
			final String instanceName,
			final Mixer3MadDefinition definition,
			final Map<MadParameterDefinition, String> creationParameterValues,
			final MadChannelConfiguration channelConfiguration )
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );
	}

}
