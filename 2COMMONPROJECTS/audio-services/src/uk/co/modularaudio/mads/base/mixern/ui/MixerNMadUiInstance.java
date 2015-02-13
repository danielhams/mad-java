package uk.co.modularaudio.mads.base.mixern.ui;

import uk.co.modularaudio.mads.base.mixern.mu.MixerNDefinition;
import uk.co.modularaudio.mads.base.mixern.mu.MixerNInstance;
import uk.co.modularaudio.util.audio.gui.mad.MadUiDefinition;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNoNameChangeNonConfigurableMadUiInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.table.Span;

public class MixerNMadUiInstance<D extends MixerNDefinition<D,I>, I extends MixerNInstance<D,I>>
	extends AbstractNoNameChangeNonConfigurableMadUiInstance<D, I>
{

	public MixerNMadUiInstance( final Span span,
			final I instance,
			final MadUiDefinition<D, I> componentUiDefinition )
	{
		super( span, instance, componentUiDefinition );
	}

	@Override
	public void consumeQueueEntry( final I instance, final IOQueueEvent nextOutgoingEntry )
	{
		// Do nothing for now.
	}

}
