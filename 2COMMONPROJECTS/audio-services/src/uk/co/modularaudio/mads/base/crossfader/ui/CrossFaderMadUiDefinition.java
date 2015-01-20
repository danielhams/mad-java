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

package uk.co.modularaudio.mads.base.crossfader.ui;

import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.base.crossfader.mu.CrossFaderMadDefinition;
import uk.co.modularaudio.mads.base.crossfader.mu.CrossFaderMadInstance;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class CrossFaderMadUiDefinition
	extends AbstractNonConfigurableMadUiDefinition<CrossFaderMadDefinition, CrossFaderMadInstance, CrossFaderMadUiInstance>
{
	private static final Span span = new Span(2,1);

	private static final int[] uiChannelInstanceIndexes = new int[] {
		CrossFaderMadDefinition.CONSUMER_CHAN1_LEFT,
		CrossFaderMadDefinition.CONSUMER_CHAN1_RIGHT,
		CrossFaderMadDefinition.CONSUMER_CHAN2_LEFT,
		CrossFaderMadDefinition.CONSUMER_CHAN2_RIGHT,
		CrossFaderMadDefinition.PRODUCER_OUT_LEFT,
		CrossFaderMadDefinition.PRODUCER_OUT_RIGHT
	};

	private static final Point[] uiChannelPositions = new Point[] {
		new Point( 150, 40 ),
		new Point( 170, 40 ),
		new Point( 200, 40 ),
		new Point( 220, 40 ),
		new Point( 300, 40 ),
		new Point( 320, 40 )
	};

	private static final String[] uiControlNames = new String[] {
		"AmpAKill",
		"AmpBKill",
		"Slider",
		"PowerCurve"
	};

	private static final ControlType[] uiControlTypes = new ControlType[] {
		ControlType.BUTTON,
		ControlType.BUTTON,
		ControlType.SLIDER,
		ControlType.COMBO
	};

	private static final Class<?>[] uiControlClasses = new Class<?>[] {
		CrossFaderAmpAKillUiJComponent.class,
		CrossFaderAmpBKillUiJComponent.class,
		CrossFaderSliderUiJComponent.class,
		CrossFaderPowerCurveUiJComponent.class
	};

	private static final Rectangle[] uiControlBounds = new Rectangle[] {
		new Rectangle(  16, 30,  75, 30 ),		// AmpA Kill
		new Rectangle( 468, 30,  75, 30 ),		// AmpB Kill
		new Rectangle( 116, 30, 328, 30 ),		// Fader Slider
		new Rectangle( 240,  3, 130, 30 )		// Power Curve
	};

	private static final Class<CrossFaderMadUiInstance> instanceClass = CrossFaderMadUiInstance.class;

	public CrossFaderMadUiDefinition( BufferedImageAllocator bia,
			CrossFaderMadDefinition definition,
			ComponentImageFactory cif,
			String imageRoot )
		throws DatastoreException
	{
		super( bia, definition, cif, imageRoot,
				span,
				instanceClass,
				uiChannelInstanceIndexes,
				uiChannelPositions,
				uiControlNames,
				uiControlTypes,
				uiControlClasses,
				uiControlBounds );
	}
}
