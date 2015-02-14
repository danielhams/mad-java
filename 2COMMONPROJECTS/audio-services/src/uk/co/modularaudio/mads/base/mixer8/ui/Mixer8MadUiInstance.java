package uk.co.modularaudio.mads.base.mixer8.ui;

import uk.co.modularaudio.mads.base.mixer8.mu.Mixer8MadDefinition;
import uk.co.modularaudio.mads.base.mixer8.mu.Mixer8MadInstance;
import uk.co.modularaudio.mads.base.mixern.ui.MixerNMadUiInstance;

public class Mixer8MadUiInstance extends MixerNMadUiInstance<Mixer8MadDefinition, Mixer8MadInstance>
{

	public Mixer8MadUiInstance( final Mixer8MadInstance instance,
			final Mixer8MadUiDefinition componentUiDefinition )
	{
		super( Mixer8MadUiDefinition.SPAN, instance, componentUiDefinition );
	}

}
