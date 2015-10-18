package uk.co.modularaudio.mads.internal;

import java.util.Map;

import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;

public interface InternalMadDefinition
{
	MadInstance<?, ?> createInstance( Map<MadParameterDefinition, String> parameterValues, String instanceName );
}
