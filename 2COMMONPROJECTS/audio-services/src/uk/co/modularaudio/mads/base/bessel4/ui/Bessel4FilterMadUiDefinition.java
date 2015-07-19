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

package uk.co.modularaudio.mads.base.bessel4.ui;

import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.base.bessel4.mu.Bessel4FilterMadDefinition;
import uk.co.modularaudio.mads.base.bessel4.mu.Bessel4FilterMadInstance;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.audio.gui.mad.MadUIStandardBackgrounds;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class Bessel4FilterMadUiDefinition
	extends AbstractNonConfigurableMadUiDefinition<Bessel4FilterMadDefinition, Bessel4FilterMadInstance, Bessel4FilterMadUiInstance>
{
	private static final Span SPAN = new Span(2,2);

	private static final int[] CHAN_INDEXES = new int[] {
		Bessel4FilterMadDefinition.CONSUMER_IN_LEFT,
		Bessel4FilterMadDefinition.CONSUMER_IN_RIGHT,
		Bessel4FilterMadDefinition.PRODUCER_OUT_LEFT,
		Bessel4FilterMadDefinition.PRODUCER_OUT_RIGHT
	};

	private static final Point[] CHAN_POSIS = new Point[] {
		new Point( 150, 40 ),
		new Point( 170, 40 ),
		new Point( 150, 70 ),
		new Point( 170, 70 )
	};

	private static final String[] CONTROL_NAMES = new String[] {
		"Filter Type"
	};

	private static final ControlType[] CONTROL_TYPES = new ControlType[] {
		ControlType.COMBO
	};

	private static final Class<?>[] CONTROL_CLASSES = new Class<?>[] {
		Bessel4FilterTypeComboUiJComponent.class
	};

	private static final Rectangle[] CONTROL_BOUNDS = new Rectangle[] {
		new Rectangle(  15, 40, 120, 30 )		// FilterType
	};

	private static final Class<Bessel4FilterMadUiInstance> INSTANCE_CLASS = Bessel4FilterMadUiInstance.class;

	protected final static int SLIDER_LABEL_MIN_WIDTH = 40;

	public Bessel4FilterMadUiDefinition( final BufferedImageAllocator bia,
			final Bessel4FilterMadDefinition definition,
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
