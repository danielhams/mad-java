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

package uk.co.modularaudio.mads.base.spectralamp.ui;

import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.base.spectralamp.mu.SpectralAmpMadDefinition;
import uk.co.modularaudio.mads.base.spectralamp.mu.SpectralAmpMadInstance;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.audio.gui.mad.MadUIStandardBackgrounds;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class SpectralAmpMadUiDefinition
	extends AbstractNonConfigurableMadUiDefinition<SpectralAmpMadDefinition, SpectralAmpMadInstance, SpectralAmpMadUiInstance>
{
	private static final Span SPAN = new Span(2,4);

	private static final int[] CHAN_INDEXES = new int[] {
		SpectralAmpMadDefinition.CONSUMER_IN
	};

	private static final Point[] CHAN_POSIS = new Point[] {
		new Point( 150, 40 )
	};

	private static final String[] CONTROL_NAMES = new String[] {
		"ScaleChoice",
		"Resolution",
		"Frequency Scale",
		"Amp Scale",
		"Running Average",
		"Peak Reset",
		"Amp Scale Axis",
		"Display",
		"Freq Scale Axis"
	};

	private static final ControlType[] CONTROL_TYPES = new ControlType[] {
		ControlType.COMBO,
		ControlType.COMBO,
		ControlType.COMBO,
		ControlType.COMBO,
		ControlType.COMBO,
		ControlType.BUTTON,
		ControlType.DISPLAY,
		ControlType.DISPLAY,
		ControlType.DISPLAY
	};

	private static final Class<?>[] CONTROL_CLASSES = new Class<?>[] {
		SpectralAmpAmpMaxChoiceUiJComponent.class,
		SpectralAmpFFTResolutionChoiceUiJComponent.class,
		SpectralAmpFreqScaleChoiceUiJComponent.class,
		SpectralAmpAmpScaleChoiceUiJComponent.class,
		SpectralAmpRunningAverageChoiceUiJComponent.class,
		SpectralAmpPeakResetUiJComponent.class,
		SpectralAmpAmpAxisDisplay.class,
//		SpectralAmpDisplayUiJComponent.class,
		NonBiBackedPeakDisplay.class,
		SpectralAmpFreqAxisDisplay.class
	};

	public final static int SCALES_HEIGHT_OFFSET = 10;
	public final static int SCALES_WIDTH_OFFSET = 30;
	public final static int FREQ_AXIS_COMPONENT_HEIGHT = 25;

	private static final Rectangle[] CONTROL_BOUNDS = new Rectangle[] {
		new Rectangle( 438,   3, 112,  30 ),		// Amp Limit Choice
		new Rectangle(   8, 274,  80,  30 ),		// FFT Resolution
		new Rectangle( 108, 274,  66,  30 ),		// Freq Scale
		new Rectangle( 194, 274,  90,  30 ),		// Amp Scale
		new Rectangle( 304, 274, 126,  30 ),		// Runn Average Type
		new Rectangle( 450, 274,  96,  30 ),		// Peak Reset Button
		new Rectangle(   6,  36,  45, 228 ),		// Amp Axis Display
		new Rectangle(  51,  36, 499, 203 ),		// Spectral Display
		new Rectangle(  51, 239, 499, FREQ_AXIS_COMPONENT_HEIGHT )			// Freq Axis Display
	};

	private static final Class<SpectralAmpMadUiInstance> INSTANCE_CLASS = SpectralAmpMadUiInstance.class;

	public SpectralAmpMadUiDefinition( final BufferedImageAllocator bia,
			final SpectralAmpMadDefinition definition,
			final ComponentImageFactory cif,
			final String imageRoot )
		throws DatastoreException
	{
		super( bia,
				cif,
				imageRoot,
				MadUIStandardBackgrounds.STD_2X4_ORANGE,
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
