package uk.co.modularaudio.mads.base.cvtoaudio4.mu;

import java.util.Map;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.mads.base.audiocvgen.mu.AudioToCvGenMadInstance;
import uk.co.modularaudio.util.audio.mad.MadChannelConfiguration;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;

public class CvToAudio4MadInstance
	extends AudioToCvGenMadInstance<CvToAudio4MadDefinition, CvToAudio4MadInstance>
{
	public CvToAudio4MadInstance( final BaseComponentsCreationContext creationContext,
			final String instanceName,
			final CvToAudio4MadDefinition definition,
			final Map<MadParameterDefinition, String> creationParameterValues,
			final MadChannelConfiguration channelConfiguration )
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );
	}
}
