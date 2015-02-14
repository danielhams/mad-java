package uk.co.modularaudio.mads.base.mixer3.ui;

import uk.co.modularaudio.mads.base.mixern.mu.MixerNInstanceConfiguration;
import uk.co.modularaudio.mads.base.mixern.ui.MixerNUiInstanceConfiguration;

public class Mixer3UiInstanceConfiguration extends MixerNUiInstanceConfiguration
{
	public Mixer3UiInstanceConfiguration( final MixerNInstanceConfiguration instanceConfiguration )
	{
		super( instanceConfiguration,
				Mixer3Lane.class,
				Mixer3Master.class );
	}
}
