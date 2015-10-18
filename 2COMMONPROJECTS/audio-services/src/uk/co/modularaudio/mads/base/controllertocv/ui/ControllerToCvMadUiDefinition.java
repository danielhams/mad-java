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

package uk.co.modularaudio.mads.base.controllertocv.ui;

import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.base.controllertocv.mu.ControllerToCvMadDefinition;
import uk.co.modularaudio.mads.base.controllertocv.mu.ControllerToCvMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.MadUIStandardBackgrounds;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class ControllerToCvMadUiDefinition
	extends AbstractNonConfigurableMadUiDefinition<ControllerToCvMadDefinition, ControllerToCvMadInstance, ControllerToCvMadUiInstance>
{
	private static final Span SPAN = new Span(2, 1);

	private static final int[] CHAN_INDEXES = new int[] {
		ControllerToCvMadDefinition.CONSUMER_NOTE,
		ControllerToCvMadDefinition.PRODUCER_CV_OUT };

	private static final Point[] CHAN_POSIS = new Point[] {
		new Point( 150, 40 ),
		new Point( 250, 40 )
	};

	private static final String[] CONTROL_NAMES = new String[] {
		"RespectTimestamps",
		"ChannelSelection",
		"ControllerSelection",
		"Learn",
		"MappingCurve",
		"InterpolationChoice"
	};

	private static final ControlType[] CONTROL_TYPES = new ControlType[] {
		ControlType.CHECKBOX,
		ControlType.COMBO,
		ControlType.COMBO,
		ControlType.BUTTON,
		ControlType.COMBO,
		ControlType.COMBO
	};

	private static final Class<?>[] CONTROL_CLASSES = new Class<?>[] {
		ControllerToCvUseTimestampingUiJComponent.class,
		ControllerToCvChannelChoiceUiJComponent.class,
		ControllerToCvControlChoiceUiJComponent.class,
		ControllerToCvLearnButtonUiJComponent.class,
		ControllerToCvMappingChoiceUiJComponent.class,
		ControllerToCvInterpolationChoiceUiJComponent.class
	};

	private static final Rectangle[] CONTROL_BOUNDS = new Rectangle[] {
		new Rectangle( 200,   6, 156,  18 ),		// Use timestamps
		new Rectangle(  10,  30, 100,  30 ),		// Channel
		new Rectangle( 120,  30, 100,  30 ),		// Controller
 		new Rectangle( 230,  30,  90,  30 ),		// Learn
		new Rectangle( 340,  30, 100,  30 ),		// Mapping
		new Rectangle( 450,  30, 100,  30 )			// Interpolation
	};

	private static final Class<ControllerToCvMadUiInstance> INSTANCE_CLASS = ControllerToCvMadUiInstance.class;

	public ControllerToCvMadUiDefinition( final BufferedImageAllocator bia,
			final ControllerToCvMadDefinition definition )
			throws DatastoreException
		{
			super( MadUIStandardBackgrounds.STD_2X1_DARKGRAY,
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
