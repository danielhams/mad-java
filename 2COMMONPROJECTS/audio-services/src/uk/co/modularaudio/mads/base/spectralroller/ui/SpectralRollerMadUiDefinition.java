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

package uk.co.modularaudio.mads.base.spectralroller.ui;

import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.base.spectralroller.mu.SpectralRollerMadDefinition;
import uk.co.modularaudio.mads.base.spectralroller.mu.SpectralRollerMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.MadUIStandardBackgrounds;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class SpectralRollerMadUiDefinition
	extends AbstractNonConfigurableMadUiDefinition<SpectralRollerMadDefinition, SpectralRollerMadInstance, SpectralRollerMadUiInstance>
{
	private static final Span SPAN = new Span(2,4);

	protected static final int SCALE_WIDTH = 40;

	private static final int[] CHAN_INDEXES = new int[] {
		SpectralRollerMadDefinition.CONSUMER_AUDIO_SIGNAL0
	};

	private static final Point[] CHAN_POSIS = new Point[] {
		new Point( 120, 70 )
	};

	private static final String[] CONTROL_NAMES = new String[] {
		"CaptureTime",
		"ScaleChoice",
		"Left Scale",
		"Display",
		"Right Scale"
	};

	private static final ControlType[] CONTROL_TYPES = new ControlType[] {
		ControlType.SLIDER,
		ControlType.COMBO,
		ControlType.DISPLAY,
		ControlType.DISPLAY,
		ControlType.DISPLAY
	};

	private static final Class<?>[] CONTROL_CLASSES = new Class<?>[] {
		SpectralRollerCaptureLengthSliderUiJComponent.class,
		SpectralRollerScaleLimitComboUiJComponent.class,
		SpectralRollerScaleDisplay.class,
		SpectralRollerDisplayUiJComponent.class,
		SpectralRollerScaleDisplay.class,
	};

	private static final Rectangle[] CONTROL_BOUNDS = new Rectangle[] {
		new Rectangle( 116,  3, 294,  30 ),		// Capture Time
		new Rectangle( 438,  3, 112,  30 ),		// Wave Scale Choice
//		new Rectangle(   6, 36,  45, 110 ),		// Scale Display
//		new Rectangle(  51, 36, 454, 110 ),		// Display
//		new Rectangle( 505, 36,  45, 110 ),		// Scale Display
		new Rectangle(   6, 36,  45, 271 ),		// Scale Display
		new Rectangle(  51, 36, 454, 271 ),		// Display
		new Rectangle( 505, 36,  45, 271 ),		// Scale Display
	};
	// 307
	// 307 - 36 = 271

	private static final Class<SpectralRollerMadUiInstance> INSTANCE_CLASS = SpectralRollerMadUiInstance.class;

	private final BufferedImageAllocator bufferedImageAllocator;

	public SpectralRollerMadUiDefinition( final BufferedImageAllocator bufferedImageAllocator,
			final SpectralRollerMadDefinition definition )
		throws DatastoreException
	{
		super( MadUIStandardBackgrounds.STD_2X4_BLUE,
				definition,
				SPAN,
				INSTANCE_CLASS,
				CHAN_INDEXES,
				CHAN_POSIS,
				CONTROL_NAMES,
				CONTROL_TYPES,
				CONTROL_CLASSES,
				CONTROL_BOUNDS );
		this.bufferedImageAllocator = bufferedImageAllocator;
	}

	public BufferedImageAllocator getBufferedImageAllocator()
	{
		return bufferedImageAllocator;
	}
}
