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

package uk.co.modularaudio.mads.base.feedbackdelay.ui;

import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.base.feedbackdelay.mu.FeedbackDelayMadDefinition;
import uk.co.modularaudio.mads.base.feedbackdelay.mu.FeedbackDelayMadInstance;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class FeedbackDelayMadUiDefinition
	extends AbstractNonConfigurableMadUiDefinition<FeedbackDelayMadDefinition, FeedbackDelayMadInstance, FeedbackDelayMadUiInstance>
{
	private static final Span span = new Span(2,1);
	
	private static final int[] uiChannelInstanceIndexes = new int[] {
		FeedbackDelayMadDefinition.CONSUMER_IN_LEFT,
		FeedbackDelayMadDefinition.CONSUMER_IN_RIGHT,
		FeedbackDelayMadDefinition.PRODUCER_OUT_LEFT,
		FeedbackDelayMadDefinition.PRODUCER_OUT_RIGHT
	};
	
	private static final Point[] uiChannelPositions = new Point[] {
		new Point( 150, 40 ),
		new Point( 170, 40 ),
		new Point( 150, 70 ),
		new Point( 170, 70 )
	};
	
	private static final String[] uiControlNames = new String[] {
		"Delay",
		"Feedback"
	};
	
	private static final ControlType[] uiControlTypes = new ControlType[] {
		ControlType.SLIDER,
		ControlType.SLIDER
	};
	
	private static final Class<?>[] uiControlClasses = new Class<?>[] {
		FeedbackDelayDelaySliderUiJComponent.class,
		FeedbackDelayFeedbackSliderUiJComponent.class
	};
	
	private static final Rectangle[] uiControlBounds = new Rectangle[] {
		new Rectangle( 113, 3, 75, 43 ),
		new Rectangle( 113, 53, 75, 43 )
	};
	
	private static final Class<FeedbackDelayMadUiInstance> instanceClass = FeedbackDelayMadUiInstance.class;
	
	public FeedbackDelayMadUiDefinition( BufferedImageAllocator bia, FeedbackDelayMadDefinition definition, ComponentImageFactory cif, String imageRoot )
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
