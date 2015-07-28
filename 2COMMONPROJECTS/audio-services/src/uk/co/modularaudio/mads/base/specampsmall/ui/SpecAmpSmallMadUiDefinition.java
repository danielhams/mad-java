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

package uk.co.modularaudio.mads.base.specampsmall.ui;

import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.base.specampgen.ui.SpectralAmpGenMadUiDefinition;
import uk.co.modularaudio.mads.base.specampsmall.mu.SpecAmpSmallMadDefinition;
import uk.co.modularaudio.mads.base.specampsmall.mu.SpecAmpSmallMadInstance;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.audio.gui.mad.MadUIStandardBackgrounds;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class SpecAmpSmallMadUiDefinition
	extends SpectralAmpGenMadUiDefinition<SpecAmpSmallMadDefinition, SpecAmpSmallMadInstance, SpecAmpSmallMadUiInstance>
{
	private static final Span SPAN = new Span(2,4);

	private static final int[] CHAN_INDEXES = new int[] {
		SpecAmpSmallMadDefinition.CONSUMER_IN
	};

	private static final Point[] CHAN_POSIS = new Point[] {
		new Point( 150, 40 )
	};

	private static final String[] CONTROL_NAMES = new String[] {
		"Amp Ceil",
		"Amp Floor",
		"Amp Scale",
		"Frequency Min",
		"Frequency Max",
		"Frequency Scale",
		"Resolution",
		"Window",
		"Running Average",
		"Peak Reset",
		"Display"
	};

	private static final ControlType[] CONTROL_TYPES = new ControlType[] {
		ControlType.COMBO,
		ControlType.COMBO,
		ControlType.COMBO,
		ControlType.SLIDER,
		ControlType.SLIDER,
		ControlType.COMBO,
		ControlType.COMBO,
		ControlType.COMBO,
		ControlType.COMBO,
		ControlType.BUTTON,
		ControlType.DISPLAY
	};

	private static final Class<?>[] CONTROL_CLASSES = new Class<?>[] {
		SpecAmpSmallAmpMaxChoiceUiJComponent.class,
		SpecAmpSmallAmpMinChoiceUiJComponent.class,
		SpecAmpSmallAmpMappingChoiceUiJComponent.class,
		SpecAmpSmallFreqMinDialUiJComponent.class,
		SpecAmpSmallFreqMaxDialUiJComponent.class,
		SpecAmpSmallFreqMappingChoiceUiJComponent.class,
		SpecAmpSmallFFTResolutionChoiceUiJComponent.class,
		SpecAmpSmallWindowChoiceUiJComponent.class,
		SpecAmpSmallRunAvChoiceUiJComponent.class,
		SpecAmpSmallPeakResetUiJComponent.class,
		SpecAmpSmallDisplayUiJComponent.class
	};

	private static final Rectangle[] CONTROL_BOUNDS = new Rectangle[] {
		new Rectangle( 108,   5, 150,  30 ),		// Amp Ceil Choice
		new Rectangle( 296,   5, 118,  30 ),		// Amp Floor Choice
		new Rectangle( 450,   5, 100,  30 ),		// Amp Scale
		new Rectangle(  94,  41, 182,  30 ),		// Freq Min
		new Rectangle( 302,  41, 122,  30 ),		// Freq Max
		new Rectangle( 450,  41, 100,  30 ),		// Freq Scale
		new Rectangle(   8, 276, 105,  30 ),		// FFT Resolution
		new Rectangle( 124, 276, 130,  30 ),		// Window choice
		new Rectangle( 268, 276, 166,  30 ),		// Runn Average Type
		new Rectangle( 450, 276,  96,  30 ),		// Peak Reset Button
		new Rectangle(   6,  77, 544, 192 )			// Display
	};

	private static final Class<SpecAmpSmallMadUiInstance> INSTANCE_CLASS = SpecAmpSmallMadUiInstance.class;

	public SpecAmpSmallMadUiDefinition( final BufferedImageAllocator bia,
			final SpecAmpSmallMadDefinition definition,
			final ComponentImageFactory cif,
			final String imageRoot )
		throws DatastoreException
	{
		super( bia,
				definition,
				cif,
				imageRoot,
				MadUIStandardBackgrounds.STD_2X4_ORANGE,
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
