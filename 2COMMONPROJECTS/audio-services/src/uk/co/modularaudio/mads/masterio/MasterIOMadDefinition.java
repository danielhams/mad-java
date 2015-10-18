package uk.co.modularaudio.mads.masterio;

import java.util.Map;

import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;

public interface MasterIOMadDefinition
{
	MadInstance<?, ?> createInstance( Map<MadParameterDefinition, String> parameterValues, String instanceName );
}
