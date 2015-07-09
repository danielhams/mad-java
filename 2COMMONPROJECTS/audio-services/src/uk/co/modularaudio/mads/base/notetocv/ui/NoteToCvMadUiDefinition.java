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
import uk.co.modularaudio.util.audio.gui.mad.MadUIStandardBackgrounds;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class NoteToCvMadUiDefinition
	extends AbstractNonConfigurableMadUiDefinition<NoteToCvMadDefinition, NoteToCvMadInstance, NoteToCvMadUiInstance>
{
	private static final Span SPAN = new Span(2,1);

	private static final int[] CHAN_INDEXES = new int[] {
		NoteToCvMadDefinition.CONSUMER_NOTE,
		NoteToCvMadDefinition.PRODUCER_GATE_OUT,
		NoteToCvMadDefinition.PRODUCER_TRIGGER_OUT,
		NoteToCvMadDefinition.PRODUCER_FREQ_OUT,
		NoteToCvMadDefinition.PRODUCER_VELOCITY_OUT,
		NoteToCvMadDefinition.PRODUCER_VEL_AMP_MULT_OUT
		};

	private static final Point[] CHAN_POSIS = new Point[] {
		new Point( 150, 40 ),
		new Point( 230, 40 ),
		new Point( 250, 40 ),
		new Point( 270, 40 ),
		new Point( 290, 40 ),
		new Point( 310, 40 )
	};

	private static final String[] CONTROL_NAMES = new String[] {
		"Note On Type",
		"ChannelNum",
		"Frequency Glide",
		"Learn"
	};

	private static final ControlType[] CONTROL_TYPES = new ControlType[] {
		ControlType.COMBO,
		ControlType.COMBO,
		ControlType.SLIDER,
		ControlType.BUTTON
	};

	private static final Class<?>[] CONTROL_CLASSES = new Class<?>[] {
		NoteOnTypeChoiceUiJComponent.class,
		NoteChannelChoiceUiJComponent.class,
		NoteToCvGlideLengthSliderUiJComponent.class,
		NoteToCvLearnButtonUiJComponent.class
	};

	private static final Rectangle[] CONTROL_BOUNDS = new Rectangle[] {
		new Rectangle(  30,  30, 110,  30 ),
		new Rectangle( 150,  30, 120,  30 ),
		new Rectangle( 280,  30, 140,  30 ),
		new Rectangle( 430,  30,  90,  30 ),
	};

	private static final Class<NoteToCvMadUiInstance> INSTANCE_CLASS = NoteToCvMadUiInstance.class;

	public NoteToCvMadUiDefinition( final BufferedImageAllocator bia, final NoteToCvMadDefinition definition, final ComponentImageFactory cif, final String imageRoot )
			throws DatastoreException
		{
			super( bia,
					cif,
					imageRoot,
					MadUIStandardBackgrounds.STD_2X1_DARKGRAY,
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
