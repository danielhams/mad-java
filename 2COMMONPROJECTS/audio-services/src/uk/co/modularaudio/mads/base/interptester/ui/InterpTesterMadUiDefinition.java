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

package uk.co.modularaudio.mads.base.interptester.ui;

import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.base.interptester.mu.InterpTesterMadDefinition;
import uk.co.modularaudio.mads.base.interptester.mu.InterpTesterMadInstance;
import uk.co.modularaudio.mads.base.interptester.mu.InterpolatorType;
import uk.co.modularaudio.util.audio.gui.mad.MadUIStandardBackgrounds;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiDefinition;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class InterpTesterMadUiDefinition
	extends AbstractNonConfigurableMadUiDefinition<InterpTesterMadDefinition, InterpTesterMadInstance, InterpTesterMadUiInstance>
{
	private static final Span SPAN = new Span(2,4);

	private final static int PLUG_START_X = 50;
	private final static int PLUG_DELTA_X = 30;
	private final static int PLUG_NOTS_Y = 120;
	private final static int PLUG_TS_Y = 160;

	private static final int[] CHAN_INDEXES;
	private static final Point[] CHAN_POSITIONS;

	static
	{
		CHAN_INDEXES = new int[InterpTesterMadDefinition.NUM_CHANNELS];
		CHAN_POSITIONS = new Point[InterpTesterMadDefinition.NUM_CHANNELS];

		for( int i = 0 ; i < InterpTesterMadDefinition.NUM_CHANNELS ; ++i )
		{
			CHAN_INDEXES[i] = i;
		}
		final int numInterpolators = InterpolatorType.values().length;

		for( int i = 0 ; i < numInterpolators ; ++i )
		{
			CHAN_POSITIONS[i] = new Point( PLUG_START_X + (i*PLUG_DELTA_X), PLUG_NOTS_Y );
			CHAN_POSITIONS[numInterpolators+i] = new Point( PLUG_START_X + (i*PLUG_DELTA_X), PLUG_TS_Y );
		}
	}

	private static final String[] CONTROL_NAMES = new String[] {
		"Model Choice",
		"Impulse",
		"ValueChase",
		"Value",
		"Perf Table"
	};

	private static final ControlType[] CONTROL_TYPES = new ControlType[] {
		ControlType.COMBO,
		ControlType.BUTTON,
		ControlType.SLIDER,
		ControlType.SLIDER,
		ControlType.CUSTOM
	};

	private static final Class<?>[] CONTROL_CLASSES = new Class<?>[] {
		InterpTesterModelChoiceUiJComponent.class,
		InterpTesterImpulseUiJComponent.class,
		InterpTesterValueChaseMillisSliderUiJComponent.class,
		InterpTesterValueSliderUiJComponent.class,
		InterpTesterPerfTableUiJComponent.class
	};

	private static final Rectangle[] CONTROL_BOUNDS = new Rectangle[] {
		new Rectangle(  16,  30, 230,  30 ),		// Model Choice
		new Rectangle( 250,  30, 130,  30 ),		// Impulse Button
		new Rectangle(  16,  66,  80, 220 ),		// Value Chase
		new Rectangle( 106,  66, 120, 220 ),		// Value
		new Rectangle( 230,  60, 300, 256 )			// Perf table
	};

	private static final Class<InterpTesterMadUiInstance> INSTANCE_CLASS = InterpTesterMadUiInstance.class;

	public InterpTesterMadUiDefinition( final InterpTesterMadDefinition definition )
		throws DatastoreException
	{
		super( MadUIStandardBackgrounds.STD_2X4_ORANGE,
				definition,
				SPAN,
				INSTANCE_CLASS,
				CHAN_INDEXES,
				CHAN_POSITIONS,
				CONTROL_NAMES,
				CONTROL_TYPES,
				CONTROL_CLASSES,
				CONTROL_BOUNDS );
	}
}
