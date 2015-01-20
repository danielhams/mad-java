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

package uk.co.modularaudio.mads.base.spectralroll.ui;

import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.base.spectralroll.mu.SpectralRollMadDefinition;
import uk.co.modularaudio.mads.base.spectralroll.mu.SpectralRollMadInstance;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class SpectralRollMadUiDefinition
	extends AbstractNonConfigurableMadUiDefinition<SpectralRollMadDefinition, SpectralRollMadInstance, SpectralRollMadUiInstance>
{
	private static final Span span = new Span(2,4);
	
	private static final int[] uiChannelInstanceIndexes = new int[] {
		SpectralRollMadDefinition.CONSUMER_IN
	};

	private static final Point[] uiChannelPositions = new Point[] {
		new Point( 140, 45 )
	};

	private static final String[] uiControlNames = new String[] {
		"Display",
		"Amp Scale",
		"Freq Scale",
		"Resolution"
	};

	private static final ControlType[] uiControlTypes = new ControlType[] {
		ControlType.DISPLAY,
		ControlType.COMBO,
		ControlType.COMBO,
		ControlType.COMBO		
	};

	private static final Class<?>[] uiControlClasses = new Class<?>[] {
		SpectralRollDisplayUiJComponent.class,
		SpectralRollResolutionComboUiJComponent.class,
		SpectralRollFrequencyScaleComboUiJComponent.class,
		SpectralRollAmpScaleComboUiJComponent.class
	};

	private static final Rectangle[] uiControlBounds = new Rectangle[] {
//		new Rectangle(   6, 22, 292, 168 ),
//		new Rectangle( 304, 56,  96,  20 ),
//		new Rectangle( 304, 98,  96,  20 ),
//		new Rectangle( 304, 136, 96,  20 )
		new Rectangle(   6,   34, 544, 230 ),		// Display
		new Rectangle( 116,  274,  96,  30 ),		// FFT Resolution
		new Rectangle( 230,  274,  96,  30 ),		// Freq
		new Rectangle( 350,  274,  96,  30 )		// Amp
	};
	
	private static final Class<SpectralRollMadUiInstance> instanceClass = SpectralRollMadUiInstance.class;
	
	public SpectralRollMadUiDefinition( BufferedImageAllocator bia, SpectralRollMadDefinition definition, ComponentImageFactory cif, String imageRoot )
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
