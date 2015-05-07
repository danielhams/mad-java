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

package uk.co.modularaudio.mads.base.oscilloscope.ui;

import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.base.oscilloscope.mu.OscilloscopeMadDefinition;
import uk.co.modularaudio.mads.base.oscilloscope.mu.OscilloscopeMadInstance;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.audio.gui.mad.MadUIStandardBackgrounds;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class OscilloscopeMadUiDefinition
	extends AbstractNonConfigurableMadUiDefinition<OscilloscopeMadDefinition, OscilloscopeMadInstance, OscilloscopeMadUiInstance>
{
	private static final Span SPAN = new Span(2,2);

	private static final int[] CHAN_INDEXES = new int[] {
		OscilloscopeMadDefinition.CONSUMER_CV_TRIGGER,
		OscilloscopeMadDefinition.CONSUMER_AUDIO_SIGNAL0,
		OscilloscopeMadDefinition.CONSUMER_CV_SIGNAL0,
		OscilloscopeMadDefinition.CONSUMER_AUDIO_SIGNAL1,
		OscilloscopeMadDefinition.CONSUMER_CV_SIGNAL1
	};

	private static final Point[] CHAN_POSIS = new Point[] {
		new Point( 60, 80 ),
		new Point( 120, 60 ),
		new Point( 150, 60 ),
		new Point( 120, 100 ),
		new Point( 150, 100 )
	};

	private static final String[] CONTROL_NAMES = new String[] {
		"CaptureTime",
		"Display",
		"TriggerType",
		"Repetitions",
		"Recapture"
	};

	private static final ControlType[] CONTROL_TYPES = new ControlType[] {
		ControlType.SLIDER,
		ControlType.DISPLAY,
		ControlType.COMBO,
		ControlType.COMBO,
		ControlType.BUTTON
	};

	private static final Class<?>[] CONTROL_CLASSES = new Class<?>[] {
		OscilloscopeCaptureLengthSliderUiJComponent.class,
		OscilloscopeDisplayUiJComponent.class,
		OscilloscopeTriggerComboUiJComponent.class,
		OscilloscopeRepetitionsComboUiJComponent.class,
		OscilloscopeRecaptureButtonUiJComponent.class
	};

	private static final Rectangle[] COUNTROL_BOUNDS = new Rectangle[] {
		new Rectangle( 116,   3, 434,  30 ),		// Capture Time
		new Rectangle(   6,  36, 418, 110 ),		// Display
		new Rectangle( 430,  36, 120,  30 ),		// Trigger Combo
		new Rectangle( 430,  76, 120,  30 ),		// Repetitions
		new Rectangle( 430, 116, 120,  30 )			// Recapture
	};

	private static final Class<OscilloscopeMadUiInstance> INSTANCE_CLASS = OscilloscopeMadUiInstance.class;

	public OscilloscopeMadUiDefinition( final BufferedImageAllocator bia, final OscilloscopeMadDefinition definition, final ComponentImageFactory cif, final String imageRoot )
		throws DatastoreException
	{
		super( bia,
				cif,
				imageRoot,
				MadUIStandardBackgrounds.STD_2X2_BLUE,
				definition,
				SPAN,
				INSTANCE_CLASS,
				CHAN_INDEXES,
				CHAN_POSIS,
				CONTROL_NAMES,
				CONTROL_TYPES,
				CONTROL_CLASSES,
				COUNTROL_BOUNDS );
	}
}
