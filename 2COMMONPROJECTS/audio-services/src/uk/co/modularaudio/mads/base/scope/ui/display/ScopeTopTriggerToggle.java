package uk.co.modularaudio.mads.base.scope.ui.display;

import java.awt.Color;

import uk.co.modularaudio.mads.base.scope.ui.ScopeColours;
import uk.co.modularaudio.util.swing.colouredtoggle.ColouredLabelToggle;
import uk.co.modularaudio.util.swing.colouredtoggle.ToggleReceiver;

public class ScopeTopTriggerToggle extends ColouredLabelToggle
{
	private static final long serialVersionUID = 7011689081245984767L;

//	private static Log log = LogFactory.getLog( ScopeTopTriggerToggle.class.getName() );

	public ScopeTopTriggerToggle( final ToggleReceiver toggleReceiver )
	{
		super( "Trigger",
				"Click to toggle display of the trigger signal",
				ScopeColours.BACKGROUND_COLOR,
				Color.WHITE,
				ScopeWaveDisplay.VIS_COLOURS[0],
				true,
				toggleReceiver,
				0 );
	}

}
