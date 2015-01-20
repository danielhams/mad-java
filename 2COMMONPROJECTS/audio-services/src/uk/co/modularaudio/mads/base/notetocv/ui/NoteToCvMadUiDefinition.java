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

package uk.co.modularaudio.mads.base.notetocv.ui;

import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.base.notetocv.mu.NoteToCvMadDefinition;
import uk.co.modularaudio.mads.base.notetocv.mu.NoteToCvMadInstance;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class NoteToCvMadUiDefinition
	extends AbstractNonConfigurableMadUiDefinition<NoteToCvMadDefinition, NoteToCvMadInstance, NoteToCvMadUiInstance>
{
	private static final Span span = new Span(2,1);
	
	private static final int[] uiChannelInstanceIndexes = new int[] {
		NoteToCvMadDefinition.CONSUMER_NOTE,
		NoteToCvMadDefinition.PRODUCER_GATE_OUT,
		NoteToCvMadDefinition.PRODUCER_TRIGGER_OUT,
		NoteToCvMadDefinition.PRODUCER_FREQ_OUT,
		NoteToCvMadDefinition.PRODUCER_VELOCITY_OUT,
		NoteToCvMadDefinition.PRODUCER_VEL_AMP_MULT_OUT
		};
	
	private static final Point[] uiChannelPositions = new Point[] {
		new Point( 50, 45 ),
		new Point( 130, 45 ),
		new Point( 150, 45 ),
		new Point( 170, 45 ),
		new Point( 190, 45 ),
		new Point( 210, 45 )
	};
	
	private static final String[] uiControlNames = new String[] {
		"Note On Type",
		"ChannelNum",
		"Frequency Glide"
	};
	
	private static final ControlType[] uiControlTypes = new ControlType[] {
		ControlType.COMBO,
		ControlType.COMBO,
		ControlType.SLIDER
	};
	
	private static final Class<?>[] uiControlClasses = new Class<?>[] {
		NoteOnTypeComboUiJComponent.class,
		NoteChannelComboUiJComponent.class,
		NoteToCvFrequencyGlideSliderUiJComponent.class
	};
	
	private static final Rectangle[] uiControlBounds = new Rectangle[] {
		new Rectangle( 30, 20, 110, 20 ),
		new Rectangle( 30, 60, 110, 20 ),
		new Rectangle( 150, 22, 75, 70 )
	};
	
	private static final Class<NoteToCvMadUiInstance> instanceClass = NoteToCvMadUiInstance.class;

	public NoteToCvMadUiDefinition( BufferedImageAllocator bia, NoteToCvMadDefinition definition, ComponentImageFactory cif, String imageRoot )
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
