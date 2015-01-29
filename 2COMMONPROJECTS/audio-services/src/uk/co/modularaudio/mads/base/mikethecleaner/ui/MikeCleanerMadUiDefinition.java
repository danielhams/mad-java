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

package uk.co.modularaudio.mads.base.mikethecleaner.ui;

import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.base.mikethecleaner.mu.MikeCleanerMadDefinition;
import uk.co.modularaudio.mads.base.mikethecleaner.mu.MikeCleanerMadInstance;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class MikeCleanerMadUiDefinition 
	extends AbstractNonConfigurableMadUiDefinition<MikeCleanerMadDefinition, MikeCleanerMadInstance, MikeCleanerMadUiInstance>
{
	private static final Span SPAN = new Span(2,1);
	
	private static final int[] CHAN_INDEXES = new int[] {
		MikeCleanerMadDefinition.CONSUMER_AUDIO_IN,
		MikeCleanerMadDefinition.PRODUCER_AUDIO_OUT
	};
	
	private static final Point[] CHAN_POSIS = new Point[] {
		new Point( 140, 30 ),
		new Point( 170, 30 )
	};
	
	private static final String[] CONTROL_NAMES = new String[] {
		"Threshold"
	};
	
	private static final ControlType[] CONTROL_TYPES = new ControlType[] {
		ControlType.SLIDER
	};
	
	private static final Class<?>[] CONTROL_CLASSES = new Class<?>[] {
		MikeCleanerThresholdSliderUiJComponent.class
	};
	
	private static final Rectangle[] CONTROL_BOUNDS = new Rectangle[] {
		new Rectangle( 113, 3, 75, 43 )
	};
	
	private static final Class<MikeCleanerMadUiInstance> INSTANCE_CLASS = MikeCleanerMadUiInstance.class;
	
	public MikeCleanerMadUiDefinition( BufferedImageAllocator bia,
			MikeCleanerMadDefinition definition,
			ComponentImageFactory cif,
			String imageRoot )
		throws DatastoreException
	{
		super( bia, definition, cif, imageRoot,
				SPAN,
				INSTANCE_CLASS,
				CHAN_INDEXES,
				CHAN_POSIS,
				CONTROL_NAMES,
				CONTROL_TYPES,
				CONTROL_CLASSES,
				CONTROL_BOUNDS );
	}
}
