package uk.co.modularaudio.mads.base.mixer3.ui;

import uk.co.modularaudio.mads.base.mixer3.mu.Mixer3MadDefinition;
import uk.co.modularaudio.mads.base.mixer3.mu.Mixer3MadInstance;
import uk.co.modularaudio.mads.base.mixern.ui.master.MasterMixerPanelUiInstance;

public class Mixer3Master extends MasterMixerPanelUiInstance<Mixer3MadDefinition, Mixer3MadInstance, Mixer3MadUiInstance>
{
	private static final long serialVersionUID = 7431791158964357287L;

	public Mixer3Master( final Mixer3MadDefinition definition,
			final Mixer3MadInstance instance,
			final Mixer3MadUiInstance uiInstance,
			final int controlIndex )
	{
		super( definition, instance, uiInstance, controlIndex );
	}

}
