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

package uk.co.modularaudio.mads.base.supersawmodule.ui;

import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.base.supersawmodule.mu.SuperSawModuleMadDefinition;
import uk.co.modularaudio.mads.base.supersawmodule.mu.SuperSawModuleMadInstance;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class SuperSawModuleMadUiDefinition
	extends AbstractNonConfigurableMadUiDefinition<SuperSawModuleMadDefinition, SuperSawModuleMadInstance, SuperSawModuleMadUiInstance>
{
	private static final Span span = new Span(2, 4);
	
	private static final int[] uiChannelInstanceIndexes = new int[] {
		SuperSawModuleMadDefinition.CONSUMER_CV_IN,
		SuperSawModuleMadDefinition.CONSUMER_CV_FREQ_IN,
		SuperSawModuleMadDefinition.CONSUMER_CV_MIX_IN,
		SuperSawModuleMadDefinition.PRODUCER_CV_OUT,
		SuperSawModuleMadDefinition.PRODUCER_CV_OSC1_FREQ,
		SuperSawModuleMadDefinition.PRODUCER_CV_OSC2_FREQ,
		SuperSawModuleMadDefinition.PRODUCER_CV_OSC3_FREQ,
		SuperSawModuleMadDefinition.PRODUCER_CV_OSC4_FREQ,
		SuperSawModuleMadDefinition.PRODUCER_CV_OSC5_FREQ,
		SuperSawModuleMadDefinition.PRODUCER_CV_OSC6_FREQ,
		SuperSawModuleMadDefinition.PRODUCER_CV_OSC7_FREQ,
		SuperSawModuleMadDefinition.PRODUCER_CV_OSC1_AMP,
		SuperSawModuleMadDefinition.PRODUCER_CV_OSC2_AMP,
		SuperSawModuleMadDefinition.PRODUCER_CV_OSC3_AMP,
		SuperSawModuleMadDefinition.PRODUCER_CV_OSC4_AMP,
		SuperSawModuleMadDefinition.PRODUCER_CV_OSC5_AMP,
		SuperSawModuleMadDefinition.PRODUCER_CV_OSC6_AMP,
		SuperSawModuleMadDefinition.PRODUCER_CV_OSC7_AMP
	};
	
	private static final Point[] uiChannelPositions = new Point[] {
		new Point( 20, 30 ),
		new Point( 20, 50 ),
		new Point( 20, 70 ),
		new Point( 120, 30 ),

		new Point( 120, 50 ),
		new Point( 120, 70 ),
		new Point( 120, 90 ),
		new Point( 120, 110 ),
		new Point( 120, 130 ),
		new Point( 120, 150 ),
		new Point( 120, 170 ),
		
		new Point( 150, 50 ),
		new Point( 150, 70 ),
		new Point( 150, 90 ),
		new Point( 150, 110 ),
		new Point( 150, 130 ),
		new Point( 150, 150 ),
		new Point( 150, 170 ),
	};
	
	private static final String[] uiControlNames = new String[] {
	};
	
	private static final ControlType[] uiControlTypes = new ControlType[] {
	};
	
	private static final Class<?>[] uiControlClasses = new Class<?>[] {
	};
	
	private static final Rectangle[] uiControlBounds = new Rectangle[] {
	};
	
	private static final Class<SuperSawModuleMadUiInstance> instanceClass = SuperSawModuleMadUiInstance.class;

	public SuperSawModuleMadUiDefinition( BufferedImageAllocator bia, SuperSawModuleMadDefinition definition, ComponentImageFactory cif, String imageRoot )
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
