package uk.co.modularaudio.mads.base.audiotocv4.mu;

import java.util.Map;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.mads.base.audiocvgen.mu.AudioToCvGenMadInstance;
import uk.co.modularaudio.util.audio.mad.MadChannelConfiguration;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;

public class AudioToCv4MadInstance
	extends AudioToCvGenMadInstance<AudioToCv4MadDefinition, AudioToCv4MadInstance>
{
	public AudioToCv4MadInstance( final BaseComponentsCreationContext creationContext,
			final String instanceName,
			final AudioToCv4MadDefinition definition,
			final Map<MadParameterDefinition, String> creationParameterValues,
			final MadChannelConfiguration channelConfiguration )
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );
	}
}
