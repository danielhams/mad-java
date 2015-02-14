package uk.co.modularaudio.mads.base.mixer8.ui;

import uk.co.modularaudio.mads.base.mixer8.mu.Mixer8MadDefinition;
import uk.co.modularaudio.mads.base.mixer8.mu.Mixer8MadInstance;
import uk.co.modularaudio.mads.base.mixern.ui.lane.LaneMixerPanelUiInstance;

public class Mixer8Lane extends LaneMixerPanelUiInstance<Mixer8MadDefinition, Mixer8MadInstance, Mixer8MadUiInstance>
{
	private static final long serialVersionUID = 7431791158964357287L;

	public Mixer8Lane( final Mixer8MadDefinition definition,
			final Mixer8MadInstance instance,
			final Mixer8MadUiInstance uiInstance,
			final int controlIndex )
	{
		super( definition, instance, uiInstance, controlIndex );
	}

}
