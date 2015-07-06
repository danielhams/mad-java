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

package uk.co.modularaudio.mads.base.midside.ui;

import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.base.midside.mu.MidSideMadDefinition;
import uk.co.modularaudio.mads.base.midside.mu.MidSideMadInstance;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.audio.gui.mad.MadUIStandardBackgrounds;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class MidSideMadUiDefinition extends
		AbstractNonConfigurableMadUiDefinition<MidSideMadDefinition, MidSideMadInstance, MidSideMadUiInstance>
{
	private static final Span SPAN = new Span(2,1);

	private static final int[] CHAN_INDEXES = new int[] {
		MidSideMadDefinition.CONSUMER_AUDIO_IN_1,
		MidSideMadDefinition.CONSUMER_AUDIO_IN_2,
		MidSideMadDefinition.PRODUCER_AUDIO_OUT_1,
		MidSideMadDefinition.PRODUCER_AUDIO_OUT_2
	};

	private static final Point[] CHAN_POSIS = new Point[] {
		new Point( 150, 40 ),
		new Point( 170, 40 ),
		new Point( 300, 40 ),
		new Point( 320, 40 )
	};

	private static final String[] CONTROL_NAMES = new String[] {
		"MidSideType"
	};

	private static final ControlType[] CONTROL_TYPES = new ControlType[] {
		ControlType.COMBO
	};

	private static final Class<?>[] CONTROL_CLASSES = new Class<?>[] {
		MidSideTypeUiJComponent.class
	};

	private static final Rectangle[] CONTROL_BOUNDS = new Rectangle[] {
		new Rectangle( 180,  22, 190,  30 )		// Mid Side Type
	};

	private static final Class<MidSideMadUiInstance> INSTANCE_CLASS = MidSideMadUiInstance.class;

	public MidSideMadUiDefinition( final BufferedImageAllocator bia,
			final MidSideMadDefinition definition,
			final ComponentImageFactory cif,
			final String imageRoot )
			throws DatastoreException
		{
			super( bia,
					cif,
					imageRoot,
					MadUIStandardBackgrounds.STD_2X1_METALLIC,
					definition,
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
