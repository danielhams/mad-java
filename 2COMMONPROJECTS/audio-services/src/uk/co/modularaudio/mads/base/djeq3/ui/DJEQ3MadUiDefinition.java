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

package uk.co.modularaudio.mads.base.djeq3.ui;

import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.base.djeq3.mu.DJEQ3MadDefinition;
import uk.co.modularaudio.mads.base.djeq3.mu.DJEQ3MadInstance;
import uk.co.modularaudio.util.audio.gui.mad.MadUIStandardBackgrounds;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class DJEQ3MadUiDefinition
	extends AbstractNonConfigurableMadUiDefinition<DJEQ3MadDefinition, DJEQ3MadInstance, DJEQ3MadUiInstance>
{
	private static final Span SPAN = new Span(1, 4);

	private static final int[] CHAN_INDEXES = new int[] {
		DJEQ3MadDefinition.CONSUMER_WAVE_LEFT,
		DJEQ3MadDefinition.CONSUMER_WAVE_RIGHT,
		DJEQ3MadDefinition.PRODUCER_WAVE_LEFT,
		DJEQ3MadDefinition.PRODUCER_WAVE_RIGHT,
		DJEQ3MadDefinition.PRODUCER_HIGH_LEFT,
		DJEQ3MadDefinition.PRODUCER_HIGH_RIGHT,
		DJEQ3MadDefinition.PRODUCER_MID_LEFT,
		DJEQ3MadDefinition.PRODUCER_MID_RIGHT,
		DJEQ3MadDefinition.PRODUCER_LOW_LEFT,
		DJEQ3MadDefinition.PRODUCER_LOW_RIGHT,
	};

	private static final Point[] CHAN_POSIS = new Point[] {
		new Point( 50, 45 ),
		new Point( 70, 45 ),
		new Point( 170, 45 ),
		new Point( 190, 45 ),

		new Point( 170, 115 ),
		new Point( 190, 115 ),
		new Point( 170, 145 ),
		new Point( 190, 145 ),
		new Point( 170, 175 ),
		new Point( 190, 175 ),
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
		DJEQ3HighEQLane.class,
		DJEQ3MidEQLane.class,
		DJEQ3LowEQLane.class,
		DJEQ3FaderMarksMeter.class
	};

	private static final Rectangle[] CONTROL_BOUNDS = new Rectangle[] {
		new Rectangle(   6,  26, 126,  90 ),
		new Rectangle(   6, 116, 126,  90 ),
		new Rectangle(   6, 206, 126,  90 ),
		new Rectangle( 138,  26, 104, 270 )
	};

	private static final Class<DJEQ3MadUiInstance> INSTANCE_CLASS = DJEQ3MadUiInstance.class;

	private final BufferedImageAllocator bufferedImageAllocator;

	public DJEQ3MadUiDefinition( final BufferedImageAllocator bufferedImageAllocator,
			final DJEQ3MadDefinition definition )
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
