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

package uk.co.modularaudio.mads.base.cdfrequencyfilter.ui;

import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.base.cdfrequencyfilter.mu.CDFrequencyFilterMadDefinition;
import uk.co.modularaudio.mads.base.cdfrequencyfilter.mu.CDFrequencyFilterMadInstance;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.audio.gui.mad.MadUIStandardBackgrounds;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class CDFrequencyFilterMadUiDefinition
	extends AbstractNonConfigurableMadUiDefinition<CDFrequencyFilterMadDefinition, CDFrequencyFilterMadInstance, CDFrequencyFilterMadUiInstance>
{
	private static final Span SPAN = new Span(2,2);

	private static final int[] CHAN_INDEXES = new int[] {
		CDFrequencyFilterMadDefinition.CONSUMER_IN_LEFT,
		CDFrequencyFilterMadDefinition.CONSUMER_IN_RIGHT,
//		FrequencyFilterMadDefinition.CONSUMER_IN_CV_FREQUENCY,
		CDFrequencyFilterMadDefinition.PRODUCER_OUT_LEFT,
		CDFrequencyFilterMadDefinition.PRODUCER_OUT_RIGHT
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
		"Knee"
	};

	private static final ControlType[] CONTROL_TYPES = new ControlType[] {
		ControlType.COMBO,
		ControlType.BUTTON,
		ControlType.SLIDER
	};

	private static final Class<?>[] CONTROL_CLASSES = new Class<?>[] {
		CDFrequencyFilterTypeComboUiJComponent.class,
		CDFrequencyFilterDbToggleUiJComponent.class,
		CDFrequencyFilterFrequencySliderUiJComponent.class
	};

	private static final Rectangle[] CONTROL_BOUNDS = new Rectangle[] {
		new Rectangle( 15, 40, 120, 30 ),		// FilterType
		new Rectangle( 15, 90, 120, 30 ),		// DbToggle
		new Rectangle( 145, 40, 400, 30 )		// Freq
	};

	private static final Class<CDFrequencyFilterMadUiInstance> INSTANCE_CLASS = CDFrequencyFilterMadUiInstance.class;

	protected final static int SLIDER_LABEL_MIN_WIDTH = 40;

	public CDFrequencyFilterMadUiDefinition( final BufferedImageAllocator bia,
			final CDFrequencyFilterMadDefinition definition,
			final ComponentImageFactory cif )
		throws DatastoreException
	{
		super( bia,
				cif,
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
