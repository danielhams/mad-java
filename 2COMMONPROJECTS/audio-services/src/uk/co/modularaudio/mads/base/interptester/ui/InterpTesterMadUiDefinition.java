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
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.audio.gui.mad.MadUIStandardBackgrounds;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class InterpTesterMadUiDefinition
	extends AbstractNonConfigurableMadUiDefinition<InterpTesterMadDefinition, InterpTesterMadInstance, InterpTesterMadUiInstance>
{
	private static final Span SPAN = new Span(2,4);

	private static final int[] CHAN_INDEXES = new int[] {
		InterpTesterMadDefinition.CONSUMER_CHAN1_LEFT,
		InterpTesterMadDefinition.CONSUMER_CHAN1_RIGHT,
		InterpTesterMadDefinition.PRODUCER_OUT_LEFT,
		InterpTesterMadDefinition.PRODUCER_OUT_RIGHT
	};

	private static final Point[] CHAN_POSITIONS = new Point[] {
		new Point( 150, 40 ),
		new Point( 170, 40 ),
		new Point( 300, 40 ),
		new Point( 320, 40 )
	};

	private static final String[] CONTROL_NAMES = new String[] {
		"Slider",
		"Interpolator",
		"ValueChase"
	};

	private static final ControlType[] CONTROL_TYPES = new ControlType[] {
		ControlType.SLIDER,
		ControlType.COMBO,
		ControlType.SLIDER
	};

	private static final Class<?>[] CONTROL_CLASSES = new Class<?>[] {
		InterpTesterSliderUiJComponent.class,
		InterpTesterCVInterpolatorUiJComponent.class,
		InterpTesterValueChaseMillisSliderUiJComponent.class
	};

	private static final Rectangle[] CONTROL_BOUNDS = new Rectangle[] {
		new Rectangle( 116, 30, 418, 30 ),		// Fader Slider
		new Rectangle( 236,  3, 110, 30 ),		// Interpolator
		new Rectangle( 356,  3, 190, 30 )		// Value Chase
	};

	private static final Class<InterpTesterMadUiInstance> INSTANCE_CLASS = InterpTesterMadUiInstance.class;

	public InterpTesterMadUiDefinition( final BufferedImageAllocator bia,
			final InterpTesterMadDefinition definition,
			final ComponentImageFactory cif,
			final String imageRoot )
		throws DatastoreException
	{
		super( bia,
				cif,
				imageRoot,
				MadUIStandardBackgrounds.STD_2X4_ORANGE,
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
