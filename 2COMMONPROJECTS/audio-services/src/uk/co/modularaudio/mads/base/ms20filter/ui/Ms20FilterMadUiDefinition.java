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

package uk.co.modularaudio.mads.base.ms20filter.ui;

import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.base.ms20filter.mu.Ms20FilterMadDefinition;
import uk.co.modularaudio.mads.base.ms20filter.mu.Ms20FilterMadInstance;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class Ms20FilterMadUiDefinition
	extends AbstractNonConfigurableMadUiDefinition<Ms20FilterMadDefinition, Ms20FilterMadInstance, Ms20FilterMadUiInstance>
{
	private static final Span SPAN = new Span(2,2);

	private static final int[] CHAN_INDEXES = new int[] {
		Ms20FilterMadDefinition.CONSUMER_IN_CV_FREQUENCY,
		Ms20FilterMadDefinition.CONSUMER_IN_CV_RESONANCE,
		Ms20FilterMadDefinition.CONSUMER_IN_CV_THRESHOLD,
		Ms20FilterMadDefinition.CONSUMER_IN_LEFT,
		Ms20FilterMadDefinition.CONSUMER_IN_RIGHT,
		Ms20FilterMadDefinition.PRODUCER_OUT_LEFT,
		Ms20FilterMadDefinition.PRODUCER_OUT_RIGHT
	};

	private static final Point[] CHAN_POSITIONS = new Point[] {
		new Point( 50, 40 ),
		new Point( 50, 70 ),
		new Point( 50, 100 ),
		new Point( 150, 40 ),
		new Point( 170, 40 ),
		new Point( 150, 100 ),
		new Point( 170, 100 )
	};

	private static final String[] CONTROL_NAMES = new String[] {
		"Filter Type",
		"Frequency",
		"Resonance",
		"Threshold"
	};

	private static final ControlType[] CONTROL_TYPES = new ControlType[] {
		ControlType.COMBO,
		ControlType.SLIDER,
		ControlType.SLIDER,
		ControlType.SLIDER
	};

	private static final Class<?>[] CONTROL_CLASSES = new Class<?>[] {
		Ms20FilterTypeComboUiJComponent.class,
		Ms20FilterFrequencySliderUiControlInstance.class,
		Ms20FilterResonanceSliderUiJComponent.class,
		Ms20FilterThresholdSliderUiJComponent.class,
	};

	private static final Rectangle[] CONTROL_BOUNDS = new Rectangle[] {
		new Rectangle( 15, 30, 90, 20 ),
		new Rectangle( 113, 3, 130, 43 ),
		new Rectangle( 113, 48, 130, 43 ),
		new Rectangle( 113, 93, 130, 43 )
	};

	private static final Class<Ms20FilterMadUiInstance> INSTANCE_CLASS = Ms20FilterMadUiInstance.class;

	public Ms20FilterMadUiDefinition( final BufferedImageAllocator bia, final Ms20FilterMadDefinition definition, final ComponentImageFactory cif, final String imageRoot )
		throws DatastoreException
	{
		super( bia,
				cif,
				imageRoot,
				definition.getId(),
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
