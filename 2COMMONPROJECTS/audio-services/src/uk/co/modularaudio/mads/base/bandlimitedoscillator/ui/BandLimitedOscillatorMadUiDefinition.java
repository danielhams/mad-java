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

package uk.co.modularaudio.mads.base.bandlimitedoscillator.ui;

import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.base.bandlimitedoscillator.mu.BandLimitedOscillatorMadDefinition;
import uk.co.modularaudio.mads.base.bandlimitedoscillator.mu.BandLimitedOscillatorMadInstance;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.audio.gui.mad.MadUIStandardBackgrounds;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class BandLimitedOscillatorMadUiDefinition
	extends AbstractNonConfigurableMadUiDefinition<BandLimitedOscillatorMadDefinition, BandLimitedOscillatorMadInstance, BandLimitedOscillatorMadUiInstance>
{
	private static final Span SPAN = new Span(2,2);

	private static final int[] CHAN_INDEXES = new int[] {
		BandLimitedOscillatorMadDefinition.CONSUMER_CV_FREQ,
		BandLimitedOscillatorMadDefinition.CONSUMER_CV_TRIGGER,
		BandLimitedOscillatorMadDefinition.CONSUMER_CV_PHASE,
		BandLimitedOscillatorMadDefinition.CONSUMER_CV_PULSEWIDTH,
		BandLimitedOscillatorMadDefinition.PRODUCER_AUDIO_OUT,
		BandLimitedOscillatorMadDefinition.PRODUCER_CV_OUT,
		BandLimitedOscillatorMadDefinition.PRODUCER_PHASE_OUT
	};

	private static final Point[] CHAN_POSI = new Point[] {
		new Point( 80, 30 ),
		new Point( 60, 50 ),
		new Point( 100, 50 ),
		new Point( 80, 70 ),
		new Point( 200, 30 ),
		new Point( 200, 50 ),
		new Point( 200, 70 )
	};

	private static final String[] CONTROL_NAMES = new String[] {
		"Frequency",
		"Shape",
		"Pulsewidth"
	};

	private static final ControlType[] CONTROL_TYPES = new ControlType[] {
		ControlType.SLIDER,
		ControlType.COMBO,
		ControlType.SLIDER
	};

	private static final Class<?>[] CONTROL_CLASSES = new Class<?>[] {
		BandLimitedOscillatorHertzSliderUiJComponent.class,
		BandLimitedOscillatorShapeComboUiJComponent.class,
		BandLimitedOscillatorPulseWidthUiJComponent.class
	};

	private static final Rectangle[] CONTROL_BOUNDS = new Rectangle[] {
		new Rectangle( 113, 3, 75, 43 ),
		new Rectangle( 190, 15, 65, 20 ),
		new Rectangle( 113, 50, 75, 42 )
	};

	private static final Class<BandLimitedOscillatorMadUiInstance> INSTANCE_CLASS = BandLimitedOscillatorMadUiInstance.class;

	public BandLimitedOscillatorMadUiDefinition( final BufferedImageAllocator bia,
			final BandLimitedOscillatorMadDefinition definition, final ComponentImageFactory cif, final String imageRoot )
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
				CHAN_POSI,
				CONTROL_NAMES,
				CONTROL_TYPES,
				CONTROL_CLASSES,
				CONTROL_BOUNDS );
	}
}
