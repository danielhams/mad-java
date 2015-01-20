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

package uk.co.modularaudio.mads.base.pattern_sequencer.ui;

import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.base.pattern_sequencer.mu.PatternSequencerMadDefinition;
import uk.co.modularaudio.mads.base.pattern_sequencer.mu.PatternSequencerMadInstance;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class PatternSequencerMadUiDefinition
	extends AbstractNonConfigurableMadUiDefinition<PatternSequencerMadDefinition, PatternSequencerMadInstance, PatternSequencerMadUiInstance>
{
	private static final Span span = new Span(4,4);
	
	private static final int[] uiChannelInstanceIndexes = new int[] {
		PatternSequencerMadDefinition.PRODUCER_NOTE_OUT,
		PatternSequencerMadDefinition.PRODUCER_CV_OUT
	};
	
	private static final Point[] uiChannelPositions = new Point[] {
		new Point( 50, 45 ),
		new Point( 70, 45 )
	};
	
	private static final String[] uiControlNames = new String[] {
		"Clear",
		"Octave Up",
		"Octave Down",
		"Semitone Up",
		"Semitone Down",
		"Run",
		"PatternSequencer",
		"Bpm"
	};
	
	private static final ControlType[] uiControlTypes = new ControlType[] {
		ControlType.BUTTON,
		ControlType.BUTTON,
		ControlType.BUTTON,
		ControlType.BUTTON,
		ControlType.BUTTON,
		ControlType.BUTTON,
		ControlType.CUSTOM,
		ControlType.CUSTOM
	};
	
	private static final Class<?>[] uiControlClasses = new Class<?>[] {
		PatternSequencerClearUiJComponent.class,
		PatternSequencerOctaveUpUiJComponent.class,
		PatternSequencerOctaveDownUiJComponent.class,
		PatternSequencerSemitoneUpUiJComponent.class,
		PatternSequencerSemitoneDownUiJComponent.class,
		PatternSequencerRunToggleUiJComponent.class,
		PatternSequencerDisplayUiJComponent.class,
		PatternSequencerBpmInputValueUiJComponent.class
	};
	
	private static final Rectangle[] uiControlBounds = new Rectangle[] {
		new Rectangle( 20, 80, 90, 20 ),
		new Rectangle( 20, 110, 90, 20 ),
		new Rectangle( 20, 130, 90, 20 ),
		new Rectangle( 20, 160, 90, 20 ),
		new Rectangle( 20, 180, 90, 20 ),
		new Rectangle( 20, 210, 90, 20 ),
		new Rectangle( 200, 30, 310, 200 ),
		new Rectangle( 20, 30, 120, 43 )
	};
	
	private static final Class<PatternSequencerMadUiInstance> instanceClass = PatternSequencerMadUiInstance.class;

	public PatternSequencerMadUiDefinition( BufferedImageAllocator bia, PatternSequencerMadDefinition definition, ComponentImageFactory cif, String imageRoot )
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
