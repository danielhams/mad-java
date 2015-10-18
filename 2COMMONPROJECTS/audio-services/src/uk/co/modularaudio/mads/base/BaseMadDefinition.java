package uk.co.modularaudio.mads.base;

import java.util.Map;

import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;

public interface BaseMadDefinition
{
	MadInstance<?, ?> createInstance( Map<MadParameterDefinition, String> parameterValues, String instanceName )
		throws MadProcessingException;
}
