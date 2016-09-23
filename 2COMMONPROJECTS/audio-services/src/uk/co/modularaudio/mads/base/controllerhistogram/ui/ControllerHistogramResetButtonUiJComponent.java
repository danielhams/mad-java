package uk.co.modularaudio.mads.base.controllerhistogram.ui;

import java.awt.Component;

import uk.co.modularaudio.mads.base.controllerhistogram.mu.ControllerHistogramMadDefinition;
import uk.co.modularaudio.mads.base.controllerhistogram.mu.ControllerHistogramMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.swing.lwtc.LWTCButton;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;

public class ControllerHistogramResetButtonUiJComponent
	implements IMadUiControlInstance<ControllerHistogramMadDefinition, ControllerHistogramMadInstance, ControllerHistogramMadUiInstance>
{
	private final LWTCButton resetButton;

	public ControllerHistogramResetButtonUiJComponent(
			final ControllerHistogramMadDefinition definition,
			final ControllerHistogramMadInstance instance,
			final ControllerHistogramMadUiInstance uiInstance,
			final int controlIndex )
	{
		resetButton = new LWTCButton( LWTCControlConstants.STD_BUTTON_COLOURS,
				"Reset Histogram",
				false )
		{
			private static final long serialVersionUID = 123532476483L;

			@Override
			public void receiveClick()
			{
				uiInstance.resetHistogram();
			}
		};
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return false;
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
	}

	@Override
	public Component getControl()
	{
		return resetButton;
	}

	@Override
	public void destroy()
	{
	}
}
