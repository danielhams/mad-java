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

package uk.co.modularaudio.mads.base.djeq.ui;

import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.base.djeq.mu.DJEQMadDefinition;
import uk.co.modularaudio.mads.base.djeq.mu.DJEQMadInstance;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.audio.gui.mad.MadUIStandardBackgrounds;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class DJEQMadUiDefinition
	extends AbstractNonConfigurableMadUiDefinition<DJEQMadDefinition, DJEQMadInstance, DJEQMadUiInstance>
{
	private static final Span SPAN = new Span(1, 4);

	private static final int[] CHAN_INDEXES = new int[] {
		DJEQMadDefinition.CONSUMER_WAVE_LEFT,
		DJEQMadDefinition.CONSUMER_WAVE_RIGHT,
		DJEQMadDefinition.PRODUCER_WAVE_LEFT,
		DJEQMadDefinition.PRODUCER_WAVE_RIGHT
	};

	private static final Point[] CHAN_POSIS = new Point[] {
		new Point( 50, 45 ),
		new Point( 70, 45 ),
		new Point( 130, 45 ),
		new Point( 150, 45 ),
	};

	private static final String[] CONTROL_NAMES = new String[] {
		"HighLane",
		"MidLane",
		"LowLane",
		"Fader"
	};

	private static final ControlType[] CONTROL_TYPES = new ControlType[] {
		ControlType.CUSTOM,
		ControlType.CUSTOM,
		ControlType.CUSTOM,
		ControlType.CUSTOM
	};

	private static final Class<?>[] CONTROL_CLASSES = new Class<?>[] {
		DJEQHighEQLane.class,
		DJEQMidEQLane.class,
		DJEQLowEQLane.class,
		DJEQFaderMarksMeter.class
	};

	private static final Rectangle[] CONTROL_BOUNDS = new Rectangle[] {
		new Rectangle(   6,  26, 126,  90 ),
		new Rectangle(   6, 116, 126,  90 ),
		new Rectangle(   6, 206, 126,  90 ),
		new Rectangle( 138,  26, 104, 270 )
	};

	private static final Class<DJEQMadUiInstance> INSTANCE_CLASS = DJEQMadUiInstance.class;

	public DJEQMadUiDefinition( final BufferedImageAllocator bia, final DJEQMadDefinition definition, final ComponentImageFactory cif, final String imageRoot )
			throws DatastoreException
		{
			super( bia,
					cif,
					imageRoot,
					MadUIStandardBackgrounds.STD_1X4_DARKGRAY,
					definition,
					SPAN,
					INSTANCE_CLASS,
					CHAN_INDEXES,
					CHAN_POSIS,
					CONTROL_NAMES,
					CONTROL_TYPES,
					CONTROL_CLASSES,
					CONTROL_BOUNDS );
		}
}
