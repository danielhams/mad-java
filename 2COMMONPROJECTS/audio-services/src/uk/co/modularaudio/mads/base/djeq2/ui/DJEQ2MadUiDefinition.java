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

package uk.co.modularaudio.mads.base.djeq2.ui;

import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.base.djeq2.mu.DJEQ2MadDefinition;
import uk.co.modularaudio.mads.base.djeq2.mu.DJEQ2MadInstance;
import uk.co.modularaudio.util.audio.gui.mad.MadUIStandardBackgrounds;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class DJEQ2MadUiDefinition
	extends AbstractNonConfigurableMadUiDefinition<DJEQ2MadDefinition, DJEQ2MadInstance, DJEQ2MadUiInstance>
{
	private static final Span SPAN = new Span(1, 4);

	private static final int[] CHAN_INDEXES = new int[] {
		DJEQ2MadDefinition.CONSUMER_WAVE_LEFT,
		DJEQ2MadDefinition.CONSUMER_WAVE_RIGHT,
		DJEQ2MadDefinition.PRODUCER_WAVE_LEFT,
		DJEQ2MadDefinition.PRODUCER_WAVE_RIGHT,
		DJEQ2MadDefinition.PRODUCER_HIGH_LEFT,
		DJEQ2MadDefinition.PRODUCER_HIGH_RIGHT,
		DJEQ2MadDefinition.PRODUCER_LOW_LEFT,
		DJEQ2MadDefinition.PRODUCER_LOW_RIGHT,
	};

	private static final Point[] CHAN_POSIS = new Point[] {
		new Point( 50, 45 ),
		new Point( 70, 45 ),
		new Point( 170, 45 ),
		new Point( 190, 45 ),

		new Point( 170, 115 ),
		new Point( 190, 115 ),
		new Point( 170, 175 ),
		new Point( 190, 175 ),
	};

	private static final String[] CONTROL_NAMES = new String[] {
		"HighLane",
		"LowLane",
		"Fader"
	};

	private static final ControlType[] CONTROL_TYPES = new ControlType[] {
		ControlType.CUSTOM,
		ControlType.CUSTOM,
		ControlType.CUSTOM
	};

	private static final Class<?>[] CONTROL_CLASSES = new Class<?>[] {
		DJEQ2HighEQLane.class,
		DJEQ2LowEQLane.class,
		DJEQ2FaderMarksMeter.class
	};

	private static final Rectangle[] CONTROL_BOUNDS = new Rectangle[] {
		new Rectangle(   6,  56, 126,  90 ),
		new Rectangle(   6, 156, 126,  90 ),
		new Rectangle( 138,  26, 104, 270 )
	};

	private static final Class<DJEQ2MadUiInstance> INSTANCE_CLASS = DJEQ2MadUiInstance.class;

	private final BufferedImageAllocator bufferedImageAllocator;

	public DJEQ2MadUiDefinition( final BufferedImageAllocator bufferedImageAllocator,
			final DJEQ2MadDefinition definition )
		throws DatastoreException
	{
		super( MadUIStandardBackgrounds.STD_1X4_DARKGRAY,
				definition,
				SPAN,
				INSTANCE_CLASS,
				CHAN_INDEXES,
				CHAN_POSIS,
				CONTROL_NAMES,
				CONTROL_TYPES,
				CONTROL_CLASSES,
				CONTROL_BOUNDS );
		this.bufferedImageAllocator = bufferedImageAllocator;
	}

	public BufferedImageAllocator getBufferedImageAllocator()
	{
		return bufferedImageAllocator;
	}
}
