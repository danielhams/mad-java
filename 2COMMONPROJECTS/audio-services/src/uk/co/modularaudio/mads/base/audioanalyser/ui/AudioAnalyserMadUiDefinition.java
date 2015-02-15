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

package uk.co.modularaudio.mads.base.audioanalyser.ui;

import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.base.audioanalyser.mu.AudioAnalyserMadDefinition;
import uk.co.modularaudio.mads.base.audioanalyser.mu.AudioAnalyserMadInstance;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.audio.gui.mad.MadUIStandardBackgrounds;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class AudioAnalyserMadUiDefinition
extends AbstractNonConfigurableMadUiDefinition<AudioAnalyserMadDefinition, AudioAnalyserMadInstance, AudioAnalyserMadUiInstance>
{
	private static final Span SPAN = new Span(2,4);

	protected static final int SCALE_WIDTH = 40;

	private static final int[] CHAN_INDEXES = new int[] {
		AudioAnalyserMadDefinition.CONSUMER_AUDIO_SIGNAL0
	};

	private static final Point[] CHAN_POSI = new Point[] {
		new Point( 120, 70 )
	};

	private static final String[] CONTROL_NAMES = new String[] {
		"Display",
	};

	private static final ControlType[] CONTROL_TYPES = new ControlType[] {
		ControlType.DISPLAY
	};

	private static final Class<?>[] CONTROL_CLASSES = new Class<?>[] {
		AudioAnalyserDisplayUiJComponent.class,
	};

	private static final Rectangle[] CONTROL_BOUNDS = new Rectangle[] {
		new Rectangle(  6, 26, 544, 280 )
		//		new Rectangle(  6, 26, 1160, 280 )
	};

	private static final Class<AudioAnalyserMadUiInstance> INSTANCE_CLASS = AudioAnalyserMadUiInstance.class;

	public AudioAnalyserMadUiDefinition( final BufferedImageAllocator bia, final AudioAnalyserMadDefinition definition,
			final ComponentImageFactory cif,
			final String imageRoot )
					throws DatastoreException
	{
		super( bia,
				cif,
				imageRoot,
				MadUIStandardBackgrounds.STD_2X4_BLUE,
				definition,
				SPAN,
				INSTANCE_CLASS,
				CHAN_INDEXES,
				CHAN_POSI,
				CONTROL_NAMES,
				CONTROL_TYPES,
				CONTROL_CLASSES,
				CONTROL_BOUNDS );
	}
}
