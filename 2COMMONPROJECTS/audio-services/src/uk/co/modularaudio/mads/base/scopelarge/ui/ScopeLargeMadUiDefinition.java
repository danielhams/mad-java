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

package uk.co.modularaudio.mads.base.scopelarge.ui;

import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.base.scopelarge.mu.ScopeLargeMadDefinition;
import uk.co.modularaudio.mads.base.scopelarge.mu.ScopeLargeMadInstance;
import uk.co.modularaudio.mads.base.scopen.mu.ScopeNMadDefinition;
import uk.co.modularaudio.mads.base.scopen.ui.ScopeNMadUiDefinition;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.audio.gui.mad.MadUIStandardBackgrounds;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class ScopeLargeMadUiDefinition
	extends	ScopeNMadUiDefinition<ScopeLargeMadDefinition, ScopeLargeMadInstance, ScopeLargeMadUiInstance>
{
	private static final Span SPAN = new Span( 4, 6 );

	private static final int[] CHAN_INDEXES = new int[] {
		ScopeNMadDefinition.SCOPE_TRIGGER,
		ScopeNMadDefinition.SCOPE_INPUT_0,
		ScopeNMadDefinition.SCOPE_INPUT_1,
		ScopeNMadDefinition.SCOPE_INPUT_2,
		ScopeNMadDefinition.SCOPE_INPUT_3
	};

	private static final Point[] CHAN_POSITIONS = new Point[] {
		new Point( 120,  70 ),
		new Point( 140,  70 ),
		new Point( 140,  90 ),
		new Point( 140, 110 ),
		new Point( 140, 130 ),
	};

	private static final String[] CONTROL_NAMES = new String[] {
		"CaptureLength",
		"TriggerChoice",
		"RepetitionChoice",
		"Recapture",
		"SaveImage",
		"WaveDisplay"
	};

	private static final ControlType[] CONTROL_TYPES = new ControlType[] {
		ControlType.SLIDER,
		ControlType.COMBO,
		ControlType.COMBO,
		ControlType.BUTTON,
		ControlType.CUSTOM,
		ControlType.CUSTOM
	};

	private static final Class<?>[] CONTROL_CLASSES = new Class<?>[] {
		ScopeLargeCaptureLengthSliderUiJComponent.class,
		ScopeLargeTriggerChoiceUiJComponent.class,
		ScopeLargeRepetitionsChoiceUiJComponent.class,
		ScopeLargeRecaptureButtonUiJComponent.class,
		ScopeLargeSaveImageButtonUiJComponent.class,
		ScopeLargeDisplayUiJComponent.class
	};

	private static final Rectangle[] CONTROL_BOUNDS = new Rectangle[] {
		new Rectangle( 116,   5, 434,  30 ),		// Capture Time
		new Rectangle( 576,   5, 170,  30 ),		// Trigger Choice
		new Rectangle( 770,   5, 165,  30 ),		// Repetition Choice
		new Rectangle( 970,   5, 120,  30 ),		// Recapture
		new Rectangle( 1104,  5,  35,  30 ),		// Save Image
		new Rectangle(   6,  40,1145, 428 )			// Scope Display
	};

	private static final Class<ScopeLargeMadUiInstance> INSTANCE_CLASS = ScopeLargeMadUiInstance.class;

	protected static final int NUM_AMP_MARKS = 21;
	protected static final int NUM_TIME_MARKS = 21;
	protected static final int NUM_AMP_DECIMAL_PLACES = 2;

	public ScopeLargeMadUiDefinition( final BufferedImageAllocator bia,
			final ScopeLargeMadDefinition definition,
			final ComponentImageFactory cif )
		throws DatastoreException
	{
		super( bia,
				definition,
				cif,
				MadUIStandardBackgrounds.STD_4X6_BLUE,
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
