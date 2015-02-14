package uk.co.modularaudio.mads.base.mixer3.ui;

import uk.co.modularaudio.mads.base.mixer3.mu.Mixer3MadDefinition;
import uk.co.modularaudio.mads.base.mixer3.mu.Mixer3MadInstance;
import uk.co.modularaudio.mads.base.mixern.ui.MixerNMadUiInstance;

public class Mixer3MadUiInstance extends MixerNMadUiInstance<Mixer3MadDefinition, Mixer3MadInstance>
{

	public Mixer3MadUiInstance( final Mixer3MadInstance instance,
			final Mixer3MadUiDefinition componentUiDefinition )
	{
		super( Mixer3MadUiDefinition.SPAN, instance, componentUiDefinition );
	}

}
