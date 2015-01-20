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

package uk.co.modularaudio.mads.base.oscilloscope.ui;

import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.base.oscilloscope.mu.OscilloscopeMadDefinition;
import uk.co.modularaudio.mads.base.oscilloscope.mu.OscilloscopeMadInstance;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class OscilloscopeMadUiDefinition 
	extends AbstractNonConfigurableMadUiDefinition<OscilloscopeMadDefinition, OscilloscopeMadInstance, OscilloscopeMadUiInstance>
{
	private static final Span span = new Span(2,2);
	
	private static final int[] uiChannelInstanceIndexes = new int[] {
		OscilloscopeMadDefinition.CONSUMER_CV_TRIGGER,
		OscilloscopeMadDefinition.CONSUMER_AUDIO_SIGNAL0,
		OscilloscopeMadDefinition.CONSUMER_CV_SIGNAL0,
		OscilloscopeMadDefinition.CONSUMER_AUDIO_SIGNAL1,
		OscilloscopeMadDefinition.CONSUMER_CV_SIGNAL1
	};
	
	private static final Point[] uiChannelPositions = new Point[] {
		new Point( 60, 90 ),
		new Point( 120, 70 ),
		new Point( 150, 70 ),
		new Point( 120, 110 ),
		new Point( 150, 110 )
	};
	
	private static final String[] uiControlNames = new String[] {
		"TriggerType",
		"Repetitions",
		"Recapture",
		"CaptureTime",
		"Display"
	};
	
	private static final ControlType[] uiControlTypes = new ControlType[] {
		ControlType.COMBO,
		ControlType.COMBO,
		ControlType.BUTTON,
		ControlType.SLIDER,
		ControlType.DISPLAY
	};
	
	private static final Class<?>[] uiControlClasses = new Class<?>[] {
		OscilloscopeTriggerComboUiJComponent.class,
		OscilloscopeRepetitionsComboUiJComponent.class,
		OscilloscopeRecaptureButtonUiJComponent.class,
		OscilloscopeCaptureTimeUiJComponent.class,
		OscilloscopeDisplayUiJComponent.class
	};
	
	private static final Rectangle[] uiControlBounds = new Rectangle[] {
		new Rectangle( 6, 32, 120, 20 ),
		new Rectangle( 130, 32, 120, 20 ),
		new Rectangle( 254, 32, 50, 20 ),
		new Rectangle( 310, 22, 100, 30 ),
		new Rectangle(  6, 55, 394, 133 )
	};
	
	private static final Class<OscilloscopeMadUiInstance> instanceClass = OscilloscopeMadUiInstance.class;
	
	public OscilloscopeMadUiDefinition( BufferedImageAllocator bia, OscilloscopeMadDefinition definition, ComponentImageFactory cif, String imageRoot )
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
