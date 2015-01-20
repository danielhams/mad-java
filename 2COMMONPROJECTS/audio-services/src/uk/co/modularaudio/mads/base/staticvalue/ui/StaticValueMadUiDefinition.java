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

package uk.co.modularaudio.mads.base.staticvalue.ui;

import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.base.staticvalue.mu.StaticValueMadDefinition;
import uk.co.modularaudio.mads.base.staticvalue.mu.StaticValueMadInstance;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class StaticValueMadUiDefinition
	extends AbstractNonConfigurableMadUiDefinition<StaticValueMadDefinition, StaticValueMadInstance, StaticValueMadUiInstance>
{
	private static final Span span = new Span(2,1);
	
	private static final int[] uiChannelInstanceIndexes = new int[] {
		StaticValueMadDefinition.PRODUCER_CV_OUT_IDX
	};
	
	private static final Point[] uiChannelPositions = new Point[] {
		new Point( 140, 30 )
	};
	
	private static final String[] uiControlNames = new String[] {
		"Value"
	};
	
	private static final ControlType[] uiControlTypes = new ControlType[] {
		ControlType.SLIDER
	};
	
	private static final Class<?>[] uiControlClasses = new Class<?>[] {
		StaticValueSliderUiJComponent.class
	};
	
	private static final Rectangle[] uiControlBounds = new Rectangle[] {
		new Rectangle( 6, 18, 250, 25 )
	};
	
	private static final Class<StaticValueMadUiInstance> instanceClass = StaticValueMadUiInstance.class;
	
	public StaticValueMadUiDefinition( BufferedImageAllocator bia, StaticValueMadDefinition definition, ComponentImageFactory cif, String imageRoot )
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
