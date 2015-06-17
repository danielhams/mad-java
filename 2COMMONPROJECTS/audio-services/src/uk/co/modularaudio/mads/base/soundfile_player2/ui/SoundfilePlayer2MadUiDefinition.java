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

package uk.co.modularaudio.mads.base.soundfile_player2.ui;

import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.base.soundfile_player2.mu.SoundfilePlayer2MadDefinition;
import uk.co.modularaudio.mads.base.soundfile_player2.mu.SoundfilePlayer2MadInstance;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.audio.gui.mad.MadUIStandardBackgrounds;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class SoundfilePlayer2MadUiDefinition extends
		AbstractNonConfigurableMadUiDefinition<SoundfilePlayer2MadDefinition, SoundfilePlayer2MadInstance, SoundfilePlayer2MadUiInstance>
{
	private static final Span SPAN = new Span( 2, 4 );

	private static final int[] CHAN_INDEXES = new int[] {
		SoundfilePlayer2MadDefinition.PRODUCER_LEFT,
		SoundfilePlayer2MadDefinition.PRODUCER_RIGHT
	};

	private static final Point[] CHAN_POSITIONS = new Point[] {
		new Point( 120, 70 ),
		new Point( 140, 70 )
	};

	private static final String[] CONTROL_NAMES = new String[] {
		"Gain",
		"Song BPM",
		"Desired BPM",
		"FileInfo",
		"SelectFile",
		"ZoomToggleGroup",
		"WaveDisplay",
		"WaveOverview",
		"SpeedSlider",
		"Rewind",
		"PlayStop",
		"FastForward"
	};

	private static final ControlType[] CONTROL_TYPES = new ControlType[] {
		ControlType.ROTARY_BIPOLAR,
		ControlType.ROTARY_BIPOLAR,
		ControlType.ROTARY_BIPOLAR,
		ControlType.DISPLAY,
		ControlType.BUTTON,
		ControlType.CUSTOM,
		ControlType.DISPLAY,
		ControlType.CUSTOM,
		ControlType.SLIDER,
		ControlType.BUTTON,
		ControlType.BUTTON,
		ControlType.BUTTON
	};

	private static final Class<?>[] COTNROL_CLASSES = new Class<?>[] {
		SoundfilePlayer2GainDialUiJComponent.class,
		SoundfilePlayer2SongBpmDialUiJComponent.class,
		SoundfilePlayer2DesiredBpmDialUiJComponent.class,
		SoundfilePlayer2FileInfoUiJComponent.class,
		SoundfilePlayer2SelectFileUiJComponent.class,
		SoundfilePlayer2ZoomToggleGroupUiJComponent.class,
		SoundfilePlayer2WaveDisplayUiJComponent.class,
		SoundfilePlayer2WaveOverviewUiJComponent.class,
		SoundfilePlayer2SpeedSliderUiJComponent.class,
		SoundfilePlayer2RWDUiJComponent.class,
		SoundfilePlayer2PlayStopUiJComponent.class,
		SoundfilePlayer2FFUiJComponent.class
	};

	private static final Rectangle[] CONTROL_BOUNDS = new Rectangle[] {
//		new Rectangle( 190,   6, 130,  30 ),		// Gain
		new Rectangle( 110,   6, 130,  30 ),		// Gain
		new Rectangle( 250,   6, 150,  30 ),		// Song BPM
		new Rectangle( 410,   6, 150,  30 ),		// Desired BPM
		new Rectangle(  47,  38, 411,  30 ),		// FileInfo
		new Rectangle( 458,  38,  30,  30 ),		// SelectFile
		new Rectangle(   6, 113,  41,  90 ),		// ZoomToggle
		new Rectangle(  47,  68,
				SoundfilePlayer2WaveDisplayUiJComponent.REQUIRED_WIDTH,
				SoundfilePlayer2WaveDisplayUiJComponent.REQUIRED_HEIGHT ),		// WaveDisplay
		new Rectangle(  47, 213, 441,  37 ),		// WaveOverview
		new Rectangle( 500,  38,  50, 213 ),		// SpeedSlider
		new Rectangle(  47, 250,  45,  30 ),		// Rewind
		new Rectangle(  92, 250,  91,  30 ),		// PlayStop
		new Rectangle( 183, 250,  45,  30 )			// FastForward
	};

	private static final Class<SoundfilePlayer2MadUiInstance> INSTANCE_CLASS = SoundfilePlayer2MadUiInstance.class;

	public SoundfilePlayer2MadUiDefinition( final BufferedImageAllocator bia,
			final SoundfilePlayer2MadDefinition definition,
			final ComponentImageFactory cif,
			final String imageRoot )
		throws DatastoreException
	{
		super( bia,
				cif,
				imageRoot,
				MadUIStandardBackgrounds.STD_2X4_RACINGGREEN,
				definition,
				SPAN,
				INSTANCE_CLASS,
				CHAN_INDEXES,
				CHAN_POSITIONS,
				CONTROL_NAMES,
				CONTROL_TYPES,
				COTNROL_CLASSES,
				CONTROL_BOUNDS );
	}
}
