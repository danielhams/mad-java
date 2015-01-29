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

package uk.co.modularaudio.mads.base.scaleandoffset.ui;

import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.base.scaleandoffset.mu.ScaleAndOffsetMadDefinition;
import uk.co.modularaudio.mads.base.scaleandoffset.mu.ScaleAndOffsetMadInstance;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class ScaleAndOffsetMadUiDefinition
	extends AbstractNonConfigurableMadUiDefinition<ScaleAndOffsetMadDefinition, ScaleAndOffsetMadInstance, ScaleAndOffsetMadUiInstance>
{

	private static final Span SPAN = new Span(2,1);

	private static final int[] CHAN_INDEXES = new int[] {
		ScaleAndOffsetMadDefinition.CONSUMER_CV_IN_IDX,
		ScaleAndOffsetMadDefinition.CONSUMER_CV_SCALE_IDX,
		ScaleAndOffsetMadDefinition.CONSUMER_CV_OFFSET_IDX,
		ScaleAndOffsetMadDefinition.PRODUCER_CV_OUT_IDX
	};

	private static final Point[] CHAN_POSIS = new Point[] {
		new Point(  40, 40 ),
		new Point( 140, 40 ),
		new Point( 400, 40 ),
		new Point( 490, 40 )
	};

	private static final String[] CONTROL_NAMES = new String[] {
		"Scale",
		"Offset"
	};

	private static final ControlType[] CONTROL_TYPES = new ControlType[] {
		ControlType.SLIDER,
		ControlType.SLIDER
	};

	private static final Class<?>[] CONTROL_CLASSES = new Class<?>[] {
		ScaleAndOffsetScaleUiJComponent.class,
		ScaleAndOffsetOffsetUiJComponent.class
	};

	private static final Rectangle[] CONTROL_BOUNDS = new Rectangle[] {
		new Rectangle(  20,  15, 250,  48 ),		// Scale
		new Rectangle( 290,  15, 250,  48 )			// Offset
	};

	private static final Class<ScaleAndOffsetMadUiInstance> INSTANCE_CLASS = ScaleAndOffsetMadUiInstance.class;

	public ScaleAndOffsetMadUiDefinition( final BufferedImageAllocator bia, final ScaleAndOffsetMadDefinition definition, final ComponentImageFactory cif, final String imageRoot )
		throws DatastoreException
	{
		super( bia, definition, cif, imageRoot,
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
