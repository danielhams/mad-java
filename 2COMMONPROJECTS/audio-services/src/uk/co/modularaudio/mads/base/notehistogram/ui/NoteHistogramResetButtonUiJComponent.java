package uk.co.modularaudio.mads.base.notehistogram.ui;

import java.awt.Component;

import uk.co.modularaudio.mads.base.notehistogram.mu.NoteHistogramMadDefinition;
import uk.co.modularaudio.mads.base.notehistogram.mu.NoteHistogramMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.swing.lwtc.LWTCButton;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;

public class NoteHistogramResetButtonUiJComponent
	implements IMadUiControlInstance<NoteHistogramMadDefinition, NoteHistogramMadInstance, NoteHistogramMadUiInstance>
{
	private final LWTCButton resetButton;

	public NoteHistogramResetButtonUiJComponent(
			final NoteHistogramMadDefinition definition,
			final NoteHistogramMadInstance instance,
			final NoteHistogramMadUiInstance uiInstance,
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
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters, final long currentGuiTime )
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
