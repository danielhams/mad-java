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

package uk.co.modularaudio.mads.base.sampleplayer.ui;

import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.base.sampleplayer.mu.SingleSamplePlayerMadDefinition;
import uk.co.modularaudio.mads.base.sampleplayer.mu.SingleSamplePlayerMadInstance;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.audio.gui.mad.MadUIStandardBackgrounds;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class SingleSamplePlayerMadUiDefinition
	extends AbstractNonConfigurableMadUiDefinition<SingleSamplePlayerMadDefinition, SingleSamplePlayerMadInstance, SingleSamplePlayerMadUiInstance>
{
	private static final Span SPAN = new Span(2,2);

	private static final int[] CHAN_INDEXES = new int[] {
		SingleSamplePlayerMadDefinition.CONSUMER_GATE_CV,
		SingleSamplePlayerMadDefinition.CONSUMER_RETRIGGER_CV,
		SingleSamplePlayerMadDefinition.CONSUMER_FREQ_CV,
		SingleSamplePlayerMadDefinition.CONSUMER_AMP_CV,
		SingleSamplePlayerMadDefinition.PRODUCER_AUDIO_OUT_L,
		SingleSamplePlayerMadDefinition.PRODUCER_AUDIO_OUT_R
	};

	private static final Point[] CHAN_POSIS = new Point[] {
		new Point( 30, 45 ),
		new Point( 50, 45 ),
		new Point( 70, 45 ),
		new Point( 90, 45 ),
		new Point( 150, 45 ),
		new Point( 170, 45 )
	};

	private static final String[] CONTROL_NAMES = new String[] {
		"StartPosition",
		"RootNoteChoice",
		"FilenameLabel",
		"ChooseFile"
	};

	private static final ControlType[] CONTROL_TYPES = new ControlType[] {
		ControlType.SLIDER,
		ControlType.COMBO,
		ControlType.DISPLAY,
		ControlType.BUTTON
	};

	private static final Class<?>[] CONTROL_CLASSES = new Class<?>[] {
		SingleSamplePlayerStartPosSliderUiJComponent.class,
		SingleSamplePlayerRootNoteComboUiJComponent.class,
		SingleSamplePlayerNameLabelUiJComponent.class,
		SingleSamplePlayerSelectFileUiJComponent.class
	};

	private static final Rectangle[] CONTROL_BOUNDS = new Rectangle[] {
		new Rectangle( 16, 16, 160, 20 ),
		new Rectangle( 190, 16, 50, 20 ),
		new Rectangle( 16, 56, 180, 20 ),
		new Rectangle( 210, 56, 30, 20 )
	};

	private static final Class<SingleSamplePlayerMadUiInstance> INSTANCE_CLASS = SingleSamplePlayerMadUiInstance.class;

	public SingleSamplePlayerMadUiDefinition( final BufferedImageAllocator bia, final SingleSamplePlayerMadDefinition definition, final ComponentImageFactory cif, final String imageRoot )
			throws DatastoreException
		{
			super( bia,
					cif,
					imageRoot,
					MadUIStandardBackgrounds.STD_2X2_DARKGRAY,
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
