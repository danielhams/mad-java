/**
 *
 * Copyright (C) 2015 - Daniel Hams, Modular Audio Limited
 *                      daniel.hams@gmail.com
 *
 * Mad is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Mad is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Mad.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.co.modularaudio.mads.base.imixern.ui;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.base.imixern.mu.MixerNMadDefinition;
import uk.co.modularaudio.mads.base.imixern.mu.MixerNMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.image.ImageFactory;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.table.Span;

public class MixerNMadUiDefinition<D extends MixerNMadDefinition<D,I>,
		I extends MixerNMadInstance<D,I>,
		U extends MixerNMadUiInstance<D,I>>
	extends AbstractNonConfigurableMadUiDefinition<D, I, U>
{

	public static final Color LANE_BG_COLOR = new Color( 57, 63, 63 );
	public static final Color LANE_FG_COLOR = LWTCControlConstants.CONTROL_LABEL_FOREGROUND;
	public static final Color LANE_INDICATOR_COLOR = Color.decode( "#00ff00" );
	public static final Color MASTER_BG_COLOR = new Color( 0.6f, 0.6f, 0.6f );
	public static final Color MASTER_FG_COLOR = Color.black;
	public static final Color MASTER_INDICATOR_COLOR = Color.decode( "#00ff00" );

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
