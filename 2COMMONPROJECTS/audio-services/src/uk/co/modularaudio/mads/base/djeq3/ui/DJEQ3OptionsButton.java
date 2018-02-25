package uk.co.modularaudio.mads.base.djeq3.ui;

import java.awt.Component;

import uk.co.modularaudio.mads.base.djeq3.mu.DJEQ3MadDefinition;
import uk.co.modularaudio.mads.base.djeq3.mu.DJEQ3MadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.swing.imagebutton.LWTCOptionsButton;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;

public class DJEQ3OptionsButton extends LWTCOptionsButton
implements IMadUiControlInstance<DJEQ3MadDefinition,DJEQ3MadInstance,DJEQ3MadUiInstance>
{
	private static final long serialVersionUID = -3538983202783891265L;

	public DJEQ3OptionsButton( final DJEQ3MadDefinition definition,
			final DJEQ3MadInstance instance,
			final DJEQ3MadUiInstance uiInstance,
			final int controlIndex )
	{
		super( LWTCControlConstants.STD_BUTTON_COLOURS, true );
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return false;
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters, final int U_currentGuiTime, final int framesSinceLastTick )
	{
	}

	@Override
	public Component getControl()
	{
		return this;
	}

	@Override
	public void destroy()
	{
	}
}
