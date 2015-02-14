package uk.co.modularaudio.mads.base.mixer8.ui;

import uk.co.modularaudio.mads.base.mixern.mu.MixerNInstanceConfiguration;
import uk.co.modularaudio.mads.base.mixern.ui.MixerNUiInstanceConfiguration;

public class Mixer8UiInstanceConfiguration extends MixerNUiInstanceConfiguration
{
	public Mixer8UiInstanceConfiguration( final MixerNInstanceConfiguration instanceConfiguration )
	{
		super( instanceConfiguration,
				Mixer8Lane.class,
				Mixer8Master.class );
	}
}
