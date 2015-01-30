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

package uk.co.modularaudio.mads.base.waveshaper.ui;

import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.base.waveshaper.mu.WaveShaperMadDefinition;
import uk.co.modularaudio.mads.base.waveshaper.mu.WaveShaperMadInstance;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class WaveShaperMadUiDefinition
	extends AbstractNonConfigurableMadUiDefinition<WaveShaperMadDefinition, WaveShaperMadInstance, WaveShaperMadUiInstance>
{
	private static final Span SPAN = new Span(1,1);
	
	private static final int[] CHAN_INDEXES = new int[] {
		WaveShaperMadDefinition.CONSUMER_AUDIO_IN,
		WaveShaperMadDefinition.PRODUCER_AUDIO_OUT,
	};
	
	private static final Point[] CHAN_POSIS = new Point[] {
		new Point( 50, 30 ),
		new Point( 70, 30 ) 
	};

	private static final String[] CONTROL_NAMES = new String[] {
		"Shape"
	};
	
	private static final ControlType[] CONTROL_TYPES = new ControlType[] {
		ControlType.COMBO
	};
	
	private static final Class<?>[] CONTROL_CLASSES = new Class<?>[] {
		WaveShaperShapeComboUiJComponent.class
	};
	
	private static final Rectangle[] CONTROL_BOUNDS = new Rectangle[] {
		new Rectangle( 190, 15, 65, 20 )
	};
	
	private static final Class<WaveShaperMadUiInstance> INSTANCE_CLASS = WaveShaperMadUiInstance.class;

	public WaveShaperMadUiDefinition( BufferedImageAllocator bia, WaveShaperMadDefinition definition, ComponentImageFactory cif, String imageRoot )
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
