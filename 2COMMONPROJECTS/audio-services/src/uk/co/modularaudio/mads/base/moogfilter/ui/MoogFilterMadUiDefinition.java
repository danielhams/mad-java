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

package uk.co.modularaudio.mads.base.moogfilter.ui;

import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.base.moogfilter.mu.MoogFilterMadDefinition;
import uk.co.modularaudio.mads.base.moogfilter.mu.MoogFilterMadInstance;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class MoogFilterMadUiDefinition
	extends AbstractNonConfigurableMadUiDefinition<MoogFilterMadDefinition, MoogFilterMadInstance, MoogFilterMadUiInstance>
{
	private static final Span span = new Span(2,2);
	
	private static final int[] uiChannelInstanceIndexes = new int[] {
		MoogFilterMadDefinition.CONSUMER_IN_LEFT,
		MoogFilterMadDefinition.CONSUMER_IN_RIGHT,
		MoogFilterMadDefinition.CONSUMER_IN_CV_FREQUENCY,
		MoogFilterMadDefinition.PRODUCER_OUT_LEFT,
		MoogFilterMadDefinition.PRODUCER_OUT_RIGHT
	};
	
	private static final Point[] uiChannelPositions = new Point[] {
		new Point( 150, 40 ),
		new Point( 170, 40 ),
		new Point( 200, 40 ),
		new Point( 150, 70 ),
		new Point( 170, 70 )
	};
	
	private static final String[] uiControlNames = new String[] {
		"Filter Type",
		"Frequency",
		"Q"
	};
	
	private static final ControlType[] uiControlTypes = new ControlType[] {
		ControlType.COMBO,
		ControlType.SLIDER,
		ControlType.SLIDER
	};
	
	private static final Class<?>[] uiControlClasses = new Class<?>[] {
		MoogFilterTypeComboUiJComponent.class,
		MoogFilterFrequencySliderUiControlInstance.class,
		MoogFilterQSliderUiControlInstance.class
//		OldFrequencyFilterBandwidthSliderUiJComponent.class
	};
	
	private static final Rectangle[] uiControlBounds = new Rectangle[] {
		new Rectangle( 15, 30, 90, 20 ),
		new Rectangle( 113, 3, 280, 43 ),
		new Rectangle( 113, 53, 280, 43 )
	};
	
	private static final Class<MoogFilterMadUiInstance> instanceClass = MoogFilterMadUiInstance.class;
	
	public MoogFilterMadUiDefinition( BufferedImageAllocator bia,
			MoogFilterMadDefinition definition,
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
