package uk.co.modularaudio.mads.base.interptester.ui;

import java.awt.Component;

import uk.co.modularaudio.mads.base.interptester.mu.InterpTesterMadDefinition;
import uk.co.modularaudio.mads.base.interptester.mu.InterpTesterMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class InterpTesterPerfTableUiJComponent
	implements IMadUiControlInstance<InterpTesterMadDefinition, InterpTesterMadInstance, InterpTesterMadUiInstance>
{
	private final InterpTesterMadUiInstance uiInstance;

	private final PerformanceTable realTable = new PerformanceTable();

	private boolean previouslyShowing;

	public InterpTesterPerfTableUiJComponent( final InterpTesterMadDefinition definition,
			final InterpTesterMadInstance instance,
			final InterpTesterMadUiInstance uiInstance,
			final int controlIndex )
	{
		this.uiInstance = uiInstance;
		uiInstance.setPerfDataReceiver( realTable );
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return true;
	}

	@Override
	public String getControlValue()
	{
		return "";
	}

	@Override
	public void receiveControlValue( final String value )
	{
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime )
	{
		final boolean showing = realTable.isShowing();

		if( previouslyShowing != showing )
		{
			uiInstance.sendUiActive( showing );
			previouslyShowing = showing;
		}
	}

	@Override
	public Component getControl()
	{
		return realTable;
	}

	@Override
	public void destroy()
	{
	}
}
