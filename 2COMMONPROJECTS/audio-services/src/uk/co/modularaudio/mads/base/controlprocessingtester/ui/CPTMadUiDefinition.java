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

package uk.co.modularaudio.mads.base.controlprocessingtester.ui;

import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.base.controlprocessingtester.mu.CPTMadDefinition;
import uk.co.modularaudio.mads.base.controlprocessingtester.mu.CPTMadInstance;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.audio.gui.mad.MadUIStandardBackgrounds;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class CPTMadUiDefinition
	extends AbstractNonConfigurableMadUiDefinition<CPTMadDefinition, CPTMadInstance, CPTMadUiInstance>
{
	private static final Span SPAN = new Span(2,1);

	private static final int[] CHAN_INDEXES = new int[] {
		CPTMadDefinition.CONSUMER_CHAN1_LEFT,
		CPTMadDefinition.CONSUMER_CHAN1_RIGHT,
		CPTMadDefinition.PRODUCER_OUT_LEFT,
		CPTMadDefinition.PRODUCER_OUT_RIGHT
	};

	private static final Point[] CHAN_POSITIONS = new Point[] {
		new Point( 150, 40 ),
		new Point( 170, 40 ),
		new Point( 300, 40 ),
		new Point( 320, 40 )
	};

	private static final String[] CONTROL_NAMES = new String[] {
		"AmpAKill",
		"Slider",
		"PowerCurve",
		"Interpolator",
		"ValueChase"
	};

	private static final ControlType[] CONTROL_TYPES = new ControlType[] {
		ControlType.BUTTON,
		ControlType.SLIDER,
		ControlType.COMBO,
		ControlType.COMBO,
		ControlType.SLIDER
	};

	private static final Class<?>[] CONTROL_CLASSES = new Class<?>[] {
		CPTAmpKillUiJComponent.class,
		CPTSliderUiJComponent.class,
		CPTPowerCurveUiJComponent.class,
		CPTCVInterpolatorUiJComponent.class,
		CPTValueChaseMillisSliderUiJComponent.class
	};

	private static final Rectangle[] CONTROL_BOUNDS = new Rectangle[] {
		new Rectangle(  16, 30,  75, 30 ),		// Amp Kill
		new Rectangle( 116, 30, 418, 30 ),		// Fader Slider
//		new Rectangle( 160,  3, 130, 30 ),		// Power Curve
//		new Rectangle( 350,  3, 130, 30 )		// Interpolator
		new Rectangle( 116,  3, 110, 30 ),		// Power Curve
		new Rectangle( 236,  3, 110, 30 ),		// Interpolator
		new Rectangle( 356,  3, 190, 30 )		// Value Chase
	};

	private static final Class<CPTMadUiInstance> INSTANCE_CLASS = CPTMadUiInstance.class;

	public CPTMadUiDefinition( final BufferedImageAllocator bia,
			final CPTMadDefinition definition,
			final ComponentImageFactory cif,
			final String imageRoot )
		throws DatastoreException
	{
		super( bia,
				cif,
				imageRoot,
				MadUIStandardBackgrounds.STD_2X1_DARKGRAY,
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
