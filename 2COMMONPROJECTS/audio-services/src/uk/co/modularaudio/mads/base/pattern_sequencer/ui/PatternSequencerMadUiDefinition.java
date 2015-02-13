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
	private static final Span SPAN = new Span(4,4);

	private static final int[] CHAN_INDEXES = new int[] {
		PatternSequencerMadDefinition.PRODUCER_NOTE_OUT,
		PatternSequencerMadDefinition.PRODUCER_CV_OUT
	};

	private static final Point[] CHAN_POSIS = new Point[] {
		new Point( 50, 45 ),
		new Point( 70, 45 )
	};

	private static final String[] CONTROL_NAMES = new String[] {
		"Bpm",
		"Run",
		"Clear",
		"Octave Up",
		"Octave Down",
		"Semitone Up",
		"Semitone Down",
		"PatternSequencer"
	};

	private static final ControlType[] CONTROL_TYPES = new ControlType[] {
		ControlType.CUSTOM,
		ControlType.BUTTON,
		ControlType.BUTTON,
		ControlType.BUTTON,
		ControlType.BUTTON,
		ControlType.BUTTON,
		ControlType.BUTTON,
		ControlType.CUSTOM
	};

	private static final Class<?>[] CONTROL_CLASSES = new Class<?>[] {
		PatternSequencerBpmUiJComponent.class,
		PatternSequencerRunToggleUiJComponent.class,
		PatternSequencerClearUiJComponent.class,
		PatternSequencerOctaveUpUiJComponent.class,
		PatternSequencerOctaveDownUiJComponent.class,
		PatternSequencerSemitoneUpUiJComponent.class,
		PatternSequencerSemitoneDownUiJComponent.class,
		PatternSequencerDisplayUiJComponent.class
	};

	private static final Rectangle[] CONTROL_BOUNDS = new Rectangle[] {
		new Rectangle(  116,   3,  920,  48 ),		// BPM
		new Rectangle( 1050,  12,   90,  30 ),		// Run
		new Rectangle(   14,  50,   90,  30 ),		// Clear
		new Rectangle(   14, 120,   90,  30 ),		// OUP
		new Rectangle(   14, 160,   90,  30 ),		// ODOWN
		new Rectangle(   14, 230,   90,  30 ),		// SUP
		new Rectangle(   14, 270,   90,  30 ),		// SDOWN
		new Rectangle(  116,  50, 1024, 250 )		// Display
	};

	private static final Class<PatternSequencerMadUiInstance> INSTANCE_CLASS = PatternSequencerMadUiInstance.class;

	public PatternSequencerMadUiDefinition( final BufferedImageAllocator bia, final PatternSequencerMadDefinition definition, final ComponentImageFactory cif, final String imageRoot )
			throws DatastoreException
		{
			super( bia,
					cif,
					imageRoot,
					definition.getId(),
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
