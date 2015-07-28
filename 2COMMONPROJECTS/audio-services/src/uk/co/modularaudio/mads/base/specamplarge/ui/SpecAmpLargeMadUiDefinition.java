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

package uk.co.modularaudio.mads.base.specamplarge.ui;

import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.base.specampgen.ui.SpectralAmpGenMadUiDefinition;
import uk.co.modularaudio.mads.base.specamplarge.mu.SpecAmpLargeMadDefinition;
import uk.co.modularaudio.mads.base.specamplarge.mu.SpecAmpLargeMadInstance;
import uk.co.modularaudio.mads.base.specampsmall.mu.SpecAmpSmallMadDefinition;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.audio.gui.mad.MadUIStandardBackgrounds;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class SpecAmpLargeMadUiDefinition
	extends SpectralAmpGenMadUiDefinition<SpecAmpLargeMadDefinition, SpecAmpLargeMadInstance, SpecAmpLargeMadUiInstance>
{
	private static final Span SPAN = new Span(4,8);

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
		SpecAmpLargeAmpMaxChoiceUiJComponent.class,
		SpecAmpLargeAmpMinChoiceUiJComponent.class,
		SpecAmpLargeAmpMappingChoiceUiJComponent.class,
		SpecAmpLargeFreqMinDialUiJComponent.class,
		SpecAmpLargeFreqMaxDialUiJComponent.class,
		SpecAmpLargeFreqMappingChoiceUiJComponent.class,
		SpecAmpLargeFFTResolutionChoiceUiJComponent.class,
		SpecAmpLargeWindowChoiceUiJComponent.class,
		SpecAmpLargeRunAvChoiceUiJComponent.class,
		SpecAmpLargePeakResetUiJComponent.class,
		SpecAmpLargeDisplayUiJComponent.class
	};

	private static final Rectangle[] CONTROL_BOUNDS = new Rectangle[] {
		new Rectangle( 108,   5, 150,  30 ),		// Amp Ceil Choice
		new Rectangle( 296,   5, 118,  30 ),		// Amp Floor Choice
		new Rectangle( 450,   5, 100,  30 ),		// Amp Scale
		new Rectangle(  94,  41, 182,  30 ),		// Freq Min
		new Rectangle( 302,  41, 122,  30 ),		// Freq Max
		new Rectangle( 450,  41, 100,  30 ),		// Freq Scale
		new Rectangle(   8,  77, 105,  30 ),		// FFT Resolution
		new Rectangle( 124,  77, 130,  30 ),		// Window choice
		new Rectangle( 268,  77, 166,  30 ),		// Runn Average Type
		new Rectangle( 450,  77,  96,  30 ),		// Peak Reset Button
		new Rectangle(   6, 114, 1144, 408 )			// Display
	};

	private static final Class<SpecAmpLargeMadUiInstance> INSTANCE_CLASS = SpecAmpLargeMadUiInstance.class;

	public SpecAmpLargeMadUiDefinition( final BufferedImageAllocator bia,
			final SpecAmpLargeMadDefinition definition,
			final ComponentImageFactory cif,
			final String imageRoot )
		throws DatastoreException
	{
		super( bia,
				definition,
				cif,
				imageRoot,
				MadUIStandardBackgrounds.STD_4X8_ORANGE,
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
