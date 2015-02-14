package uk.co.modularaudio.mads.base.mixern.ui;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.base.mixern.mu.MixerNMadDefinition;
import uk.co.modularaudio.mads.base.mixern.mu.MixerNMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.image.ImageFactory;
import uk.co.modularaudio.util.table.Span;

public class MixerNMadUiDefinition<D extends MixerNMadDefinition<D,I>,
		I extends MixerNMadInstance<D,I>,
		U extends MixerNMadUiInstance<D,I>>
	extends AbstractNonConfigurableMadUiDefinition<D, I, U>
{

	public static final Color LANE_BG_COLOR = new Color( 57, 63, 63 );
	public static final Color MASTER_BG_COLOR = new Color( 0.6f, 0.6f, 0.6f );

	public MixerNMadUiDefinition( final BufferedImageAllocator bia,
			final D definition,
			final ImageFactory cif,
			final String imageRoot,
			final String imagePrefix,
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
