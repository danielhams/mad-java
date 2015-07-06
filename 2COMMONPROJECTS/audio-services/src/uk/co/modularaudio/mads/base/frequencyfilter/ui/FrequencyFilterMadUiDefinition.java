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

package uk.co.modularaudio.mads.base.frequencyfilter.ui;

import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.base.frequencyfilter.mu.FrequencyFilterMadDefinition;
import uk.co.modularaudio.mads.base.frequencyfilter.mu.FrequencyFilterMadInstance;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.audio.gui.mad.MadUIStandardBackgrounds;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class FrequencyFilterMadUiDefinition
	extends AbstractNonConfigurableMadUiDefinition<FrequencyFilterMadDefinition, FrequencyFilterMadInstance, FrequencyFilterMadUiInstance>
{
	private static final Span SPAN = new Span(2,2);

	private static final int[] CHAN_INDEXES = new int[] {
		FrequencyFilterMadDefinition.CONSUMER_IN_LEFT,
		FrequencyFilterMadDefinition.CONSUMER_IN_RIGHT,
//		FrequencyFilterMadDefinition.CONSUMER_IN_CV_FREQUENCY,
		FrequencyFilterMadDefinition.PRODUCER_OUT_LEFT,
		FrequencyFilterMadDefinition.PRODUCER_OUT_RIGHT
	};

	private static final Point[] CHAN_POSIS = new Point[] {
		new Point( 200, 60 ),
		new Point( 220, 60 ),
//		new Point( 250, 60 ),
		new Point( 200, 100 ),
		new Point( 220, 100 )
	};

	private static final String[] CONTROL_NAMES = new String[] {
		"Filter Type",
		"Toggle 24 dB",
		"Knee",
		"Bandwidth"
	};

	private static final ControlType[] CONTROL_TYPES = new ControlType[] {
		ControlType.COMBO,
		ControlType.BUTTON,
		ControlType.SLIDER,
		ControlType.SLIDER
	};

	private static final Class<?>[] CONTROL_CLASSES = new Class<?>[] {
		FrequencyFilterTypeComboUiJComponent.class,
		FrequencyFilterDbToggleUiJComponent.class,
		FrequencyFilterFrequencySliderUiJComponent.class,
		FrequencyFilterBWSliderUiJComponent.class
	};

	private static final Rectangle[] CONTROL_BOUNDS = new Rectangle[] {
		new Rectangle( 15, 40, 120, 30 ),		// FilterType
		new Rectangle( 15, 90, 120, 30 ),		// DbToggle
		new Rectangle( 145, 40, 400, 30 ),		// Freq
		new Rectangle( 145, 90, 400, 30 )		// BW
	};

	private static final Class<FrequencyFilterMadUiInstance> INSTANCE_CLASS = FrequencyFilterMadUiInstance.class;

	protected final static int SLIDER_LABEL_MIN_WIDTH = 40;

	public FrequencyFilterMadUiDefinition( final BufferedImageAllocator bia,
			final FrequencyFilterMadDefinition definition,
			final ComponentImageFactory cif,
			final String imageRoot )
		throws DatastoreException
	{
		super( bia,
				cif,
				imageRoot,
				MadUIStandardBackgrounds.STD_2X2_DARKGRAY,
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
