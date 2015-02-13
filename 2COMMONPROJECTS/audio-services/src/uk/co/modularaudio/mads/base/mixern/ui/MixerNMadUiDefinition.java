package uk.co.modularaudio.mads.base.mixern.ui;

import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.base.mixern.mu.MixerNDefinition;
import uk.co.modularaudio.mads.base.mixern.mu.MixerNInstance;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.image.ImageFactory;
import uk.co.modularaudio.util.table.Span;

public class MixerNMadUiDefinition<D extends MixerNDefinition<D,I>,
		I extends MixerNInstance<D,I>,
		U extends MixerNMadUiInstance<D,I>>
	extends AbstractNonConfigurableMadUiDefinition<D, I, U>
{

	public MixerNMadUiDefinition( final BufferedImageAllocator bia,
			final ImageFactory cif,
			final String imageRoot,
			final String imagePrefix,
			final D definition,
			final Span span,
			final Class<U> instanceClass,
			final int[] uiChannelInstanceIndexes,
			final Point[] uiChannelPositions,
			final String[] uiControlNames,
			final ControlType[] uiControlTypes,
			final Class<?>[] uiControlClasses,
			final Rectangle[] uiControlBounds )
		throws DatastoreException
	{
		super( bia,
				cif,
				imageRoot,
				imagePrefix,
				definition,
				span,
				instanceClass,
				uiChannelInstanceIndexes,
				uiChannelPositions,
				uiControlNames,
				uiControlTypes,
				uiControlClasses,
				uiControlBounds );
	}

}
