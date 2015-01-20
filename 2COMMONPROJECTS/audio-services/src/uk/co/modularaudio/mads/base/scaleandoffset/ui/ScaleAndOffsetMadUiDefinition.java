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

package uk.co.modularaudio.mads.base.scaleandoffset.ui;

import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.base.scaleandoffset.mu.ScaleAndOffsetMadDefinition;
import uk.co.modularaudio.mads.base.scaleandoffset.mu.ScaleAndOffsetMadInstance;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class ScaleAndOffsetMadUiDefinition
	extends AbstractNonConfigurableMadUiDefinition<ScaleAndOffsetMadDefinition, ScaleAndOffsetMadInstance, ScaleAndOffsetMadUiInstance>
{
	
	private static final Span span = new Span(2,1);
	
	private static final int[] uiChannelInstanceIndexes = new int[] {
		ScaleAndOffsetMadDefinition.CONSUMER_CV_IN_IDX,
		ScaleAndOffsetMadDefinition.CONSUMER_CV_SCALE_IDX,
		ScaleAndOffsetMadDefinition.CONSUMER_CV_OFFSET_IDX,
		ScaleAndOffsetMadDefinition.PRODUCER_CV_OUT_IDX
	};
	
	private static final Point[] uiChannelPositions = new Point[] {
		new Point( 40, 30 ),
		new Point( 70, 30 ),
		new Point( 100, 30 ),
		new Point( 140, 30 )
	};
	
	private static final String[] uiControlNames = new String[] {
		"Scale",
		"Offset"
	};
	
	private static final ControlType[] uiControlTypes = new ControlType[] {
		ControlType.SLIDER,
		ControlType.SLIDER
	};
	
	private static final Class<?>[] uiControlClasses = new Class<?>[] {
		ScaleAndOffsetScaleUiJComponent.class,
		ScaleAndOffsetOffsetUiJComponent.class
	};
	
	private static final Rectangle[] uiControlBounds = new Rectangle[] {
		new Rectangle(20, 15, 100, 30 ),
		new Rectangle(130, 15, 100, 30 )
	};
	
	private static final Class<ScaleAndOffsetMadUiInstance> instanceClass = ScaleAndOffsetMadUiInstance.class;
	
	public ScaleAndOffsetMadUiDefinition( BufferedImageAllocator bia, ScaleAndOffsetMadDefinition definition, ComponentImageFactory cif, String imageRoot )
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
