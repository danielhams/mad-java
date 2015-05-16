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
		InterpTesterMadDefinition.CONSUMER_AUDIO,
		InterpTesterMadDefinition.CONSUMER_CV,
		InterpTesterMadDefinition.PRODUCER_CV_RAW_NOTS,
		InterpTesterMadDefinition.PRODUCER_CV_HALFHANN_NOTS,
		InterpTesterMadDefinition.PRODUCER_CV_RAW,
		InterpTesterMadDefinition.PRODUCER_CV_LINEAR,
		InterpTesterMadDefinition.PRODUCER_CV_HALFHANN,
		InterpTesterMadDefinition.PRODUCER_CV_SPRINGDAMPER,
		InterpTesterMadDefinition.PRODUCER_CV_LOWPASS,
		InterpTesterMadDefinition.PRODUCER_CV_SPRINGDAMPER_DOUBLE,
		InterpTesterMadDefinition.PRODUCER_AUDIO,
		InterpTesterMadDefinition.PRODUCER_CV
	};

	private static final Point[] CHAN_POSITIONS = new Point[] {
		new Point( 150, 80 ),
		new Point( 190, 80 ),

		new Point( 150, 120 ),
		new Point( 190, 120 ),

		new Point( 150, 160 ),
		new Point( 190, 160 ),
		new Point( 230, 160 ),
		new Point( 270, 160 ),
		new Point( 310, 160 ),
		new Point( 350, 160 ),


		new Point( 150, 200 ),
		new Point( 190, 200 )
	};

	private static final String[] CONTROL_NAMES = new String[] {
		"Model Choice",
		"ValueChase",
		"Value",
		"Perf Table"
	};

	private static final ControlType[] CONTROL_TYPES = new ControlType[] {
		ControlType.COMBO,
		ControlType.SLIDER,
		ControlType.SLIDER,
		ControlType.CUSTOM
	};

	private static final Class<?>[] CONTROL_CLASSES = new Class<?>[] {
		InterpTesterModelChoiceUiJComponent.class,
		InterpTesterValueChaseMillisSliderUiJComponent.class,
		InterpTesterValueSliderUiJComponent.class,
		InterpTesterPerfTableUiJComponent.class
	};

	private static final Rectangle[] CONTROL_BOUNDS = new Rectangle[] {
		new Rectangle(  16, 30, 230,  30 ),		// Model Choice
		new Rectangle(  16, 66,  80, 220 ),		// Value Chase
//		new Rectangle( 106, 66,  80, 220 ),		// Value
		new Rectangle( 106, 66, 120, 220 ),		// Value
		new Rectangle( 230, 30, 300, 256 )		// Perf table
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
