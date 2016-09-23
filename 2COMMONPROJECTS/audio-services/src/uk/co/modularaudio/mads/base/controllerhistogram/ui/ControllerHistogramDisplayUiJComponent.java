package uk.co.modularaudio.mads.base.controllerhistogram.ui;

import java.awt.Component;

import uk.co.modularaudio.mads.base.controllerhistogram.mu.ControllerHistogramMadDefinition;
import uk.co.modularaudio.mads.base.controllerhistogram.mu.ControllerHistogramMadInstance;
import uk.co.modularaudio.mads.base.controllerhistogram.util.HistogramDisplay;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class ControllerHistogramDisplayUiJComponent
	implements IMadUiControlInstance<ControllerHistogramMadDefinition, ControllerHistogramMadInstance, ControllerHistogramMadUiInstance>
{
	private final HistogramDisplay display;

	public ControllerHistogramDisplayUiJComponent(
			final ControllerHistogramMadDefinition definition,
			final ControllerHistogramMadInstance instance,
			final ControllerHistogramMadUiInstance uiInstance,
			final int controlIndex )
	{
		display = new HistogramDisplay( uiInstance );
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
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage ,
			final MadTimingParameters timingParameters ,
			final int U_currentGuiTime , int framesSinceLastTick  )
	{
		display.doDisplayProcessing();
	}

	@Override
	public Component getControl()
	{
		return display;
	}

	@Override
	public void destroy()
	{
	}
}
