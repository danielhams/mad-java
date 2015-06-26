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

package uk.co.modularaudio.mads.base.dctrap.ui;

import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.base.dctrap.mu.DCTrapMadDefinition;
import uk.co.modularaudio.mads.base.dctrap.mu.DCTrapMadInstance;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.audio.gui.mad.MadUIStandardBackgrounds;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class DCTrapMadUiDefinition
	extends AbstractNonConfigurableMadUiDefinition<DCTrapMadDefinition, DCTrapMadInstance, DCTrapMadUiInstance>
{
	private static final Span SPAN = new Span(1,1);

	private static final int[] UI_CHANNEL_INDEXES = new int[] {
		DCTrapMadDefinition.CONSUMER_IN_WAVE_LEFT,
		DCTrapMadDefinition.CONSUMER_IN_WAVE_RIGHT,
		DCTrapMadDefinition.PRODUCER_OUT_WAVE_LEFT,
		DCTrapMadDefinition.PRODUCER_OUT_WAVE_RIGHT,
	};

	private static final Point[] UI_CHANNEL_POSI = new Point[] {
		new Point( 60, 40 ),
		new Point( 90, 40 ),
		new Point( 150, 40 ),
		new Point( 180, 40 )
	};

	private static final String[] UI_CONTROL_NAMES = new String[] {
	};

	private static final ControlType[] UI_CONTROL_TYPES = new ControlType[] {
	};

	private static final Class<?>[] UI_CONTROL_CLASSES = new Class<?>[] {
	};

	private static final Rectangle[] UI_CONTROL_BOUNDS = new Rectangle[] {
	};

	private static final Class<DCTrapMadUiInstance> INSTANCE_CLASS = DCTrapMadUiInstance.class;

	public DCTrapMadUiDefinition( final BufferedImageAllocator bia,
			final DCTrapMadDefinition definition,
			final ComponentImageFactory cif,
			final String imageRoot )
		throws DatastoreException
	{
		super( bia,
				cif,
				imageRoot,
				MadUIStandardBackgrounds.STD_1X1_LIGHTGRAY,
				definition,
				SPAN,
				INSTANCE_CLASS,
				UI_CHANNEL_INDEXES,
				UI_CHANNEL_POSI,
				UI_CONTROL_NAMES,
				UI_CONTROL_TYPES,
				UI_CONTROL_CLASSES,
				UI_CONTROL_BOUNDS );
	}
}
