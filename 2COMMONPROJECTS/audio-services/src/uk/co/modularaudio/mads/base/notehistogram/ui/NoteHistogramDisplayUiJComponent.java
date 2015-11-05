package uk.co.modularaudio.mads.base.notehistogram.ui;

import java.awt.Component;

import uk.co.modularaudio.mads.base.notehistogram.mu.NoteHistogramMadDefinition;
import uk.co.modularaudio.mads.base.notehistogram.mu.NoteHistogramMadInstance;
import uk.co.modularaudio.mads.base.notehistogram.util.NoteHistogramDisplay;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class NoteHistogramDisplayUiJComponent
	implements IMadUiControlInstance<NoteHistogramMadDefinition, NoteHistogramMadInstance, NoteHistogramMadUiInstance>
{
	private final NoteHistogramDisplay display;

	public NoteHistogramDisplayUiJComponent(
			final NoteHistogramMadDefinition definition,
			final NoteHistogramMadInstance instance,
			final NoteHistogramMadUiInstance uiInstance,
			final int controlIndex )
	{
		display = new NoteHistogramDisplay( uiInstance );
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
			final MadTimingParameters timingParameters, final long currentGuiTime )
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
