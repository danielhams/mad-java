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
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class SingleSamplePlayerMadUiDefinition
	extends AbstractNonConfigurableMadUiDefinition<SingleSamplePlayerMadDefinition, SingleSamplePlayerMadInstance, SingleSamplePlayerMadUiInstance>
{
	private static final Span span = new Span(2,2);
	
	private static final int[] uiChannelInstanceIndexes = new int[] {
		SingleSamplePlayerMadDefinition.CONSUMER_GATE_CV,
		SingleSamplePlayerMadDefinition.CONSUMER_RETRIGGER_CV,
		SingleSamplePlayerMadDefinition.CONSUMER_FREQ_CV,
		SingleSamplePlayerMadDefinition.CONSUMER_AMP_CV,
		SingleSamplePlayerMadDefinition.PRODUCER_AUDIO_OUT_L,
		SingleSamplePlayerMadDefinition.PRODUCER_AUDIO_OUT_R
	};
	
	private static final Point[] uiChannelPositions = new Point[] {
		new Point( 30, 45 ),
		new Point( 50, 45 ),
		new Point( 70, 45 ),
		new Point( 90, 45 ),
		new Point( 150, 45 ),
		new Point( 170, 45 )
	};
	
	private static final String[] uiControlNames = new String[] {
		"StartPosition",
		"RootNoteChoice",
		"FilenameLabel",
		"ChooseFile"
	};
	
	private static final ControlType[] uiControlTypes = new ControlType[] {
		ControlType.SLIDER,
		ControlType.COMBO,
		ControlType.DISPLAY,
		ControlType.BUTTON
	};
	
	private static final Class<?>[] uiControlClasses = new Class<?>[] {
		SingleSamplePlayerStartPosSliderUiJComponent.class,
		SingleSamplePlayerRootNoteComboUiJComponent.class,
		SingleSamplePlayerNameLabelUiJComponent.class,
		SingleSamplePlayerSelectFileUiJComponent.class
	};
	
	private static final Rectangle[] uiControlBounds = new Rectangle[] {
		new Rectangle( 16, 16, 160, 20 ),
		new Rectangle( 190, 16, 50, 20 ),
		new Rectangle( 16, 56, 180, 20 ),
		new Rectangle( 210, 56, 30, 20 )
	};
	
	private static final Class<SingleSamplePlayerMadUiInstance> instanceClass = SingleSamplePlayerMadUiInstance.class;

	public SingleSamplePlayerMadUiDefinition( BufferedImageAllocator bia, SingleSamplePlayerMadDefinition definition, ComponentImageFactory cif, String imageRoot )
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
