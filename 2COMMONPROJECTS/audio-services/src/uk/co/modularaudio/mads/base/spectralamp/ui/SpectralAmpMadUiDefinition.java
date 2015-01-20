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
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class SpectralAmpMadUiDefinition
	extends AbstractNonConfigurableMadUiDefinition<SpectralAmpMadDefinition, SpectralAmpMadInstance, SpectralAmpMadUiInstance>
{
	private static final Span span = new Span(2,4);
	
	private static final int[] uiChannelInstanceIndexes = new int[] {
		SpectralAmpMadDefinition.CONSUMER_IN
	};
	
	private static final Point[] uiChannelPositions = new Point[] {
		new Point( 150, 40 )
	};
	
	private static final String[] uiControlNames = new String[] {
		"Resolution",
		"Frequency Scale",
		"Amp Scale",
		"Running Average",
		"Display"
	};
	
	private static final ControlType[] uiControlTypes = new ControlType[] {
		ControlType.COMBO,
		ControlType.COMBO,
		ControlType.COMBO,
		ControlType.COMBO,
		ControlType.DISPLAY
	};
	
	private static final Class<?>[] uiControlClasses = new Class<?>[] {
		SpectralAmpResolutionComboUiJComponent.class,
		SpectralAmpFrequencyScaleComboUiJComponent.class,
		SpectralAmpAmpScaleComboUiJComponent.class,
		SpectralAmpRunningAverageComboUiJComponent.class,
		SpectralAmpDisplayUiJComponent.class
	};
	
	private static final Rectangle[] uiControlBounds = new Rectangle[] {
//		new Rectangle( 457,  10,  96,  20 ),
//		new Rectangle( 457,  52,  96,  20 ),
//		new Rectangle( 457,  90,  96,  20 ),
//		new Rectangle( 457, 128,  96,  20 ),
//		new Rectangle(   6,  22, 446, 168 )
		new Rectangle(  70, 274,  96,  30 ),		// Resolution
		new Rectangle( 180, 274,  96,  30 ),		// Freq
		new Rectangle( 290, 274,  96,  30 ),		// Amp
		new Rectangle( 400, 274,  96,  30 ),		// Runn Av
		new Rectangle(   6,  34, 544, 230 )			// Display
	};
	
	private static final Class<SpectralAmpMadUiInstance> instanceClass = SpectralAmpMadUiInstance.class;
	
	public SpectralAmpMadUiDefinition( BufferedImageAllocator bia,
			SpectralAmpMadDefinition definition,
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
