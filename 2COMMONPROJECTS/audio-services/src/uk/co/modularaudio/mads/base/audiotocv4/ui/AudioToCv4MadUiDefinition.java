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

package uk.co.modularaudio.mads.base.audiotocv4.ui;

import uk.co.modularaudio.mads.base.audiocvgen.mu.AudioToCvGenInstanceConfiguration;
import uk.co.modularaudio.mads.base.audiocvgen.ui.AudioToCvGenMadUiDefinition;
import uk.co.modularaudio.mads.base.audiotocv4.mu.AudioToCv4MadDefinition;
import uk.co.modularaudio.mads.base.audiotocv4.mu.AudioToCv4MadInstance;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.audio.gui.mad.MadUIStandardBackgrounds;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.image.ImageFactory;
import uk.co.modularaudio.util.table.Span;

public class AudioToCv4MadUiDefinition extends AudioToCvGenMadUiDefinition<AudioToCv4MadDefinition, AudioToCv4MadInstance, AudioToCv4MadUiInstance>
{
	final static Span SPAN = new Span( 1, 1 );

	public AudioToCv4MadUiDefinition( final BufferedImageAllocator bia,
			final AudioToCv4MadDefinition definition,
			final ComponentImageFactory cif )
		throws DatastoreException
	{
		this( bia,
				definition,
				cif,
				instanceConfigToUiConfig( AudioToCv4MadDefinition.INSTANCE_CONFIGURATION ) );
	}

	private static AudioToCv4UiInstanceConfiguration instanceConfigToUiConfig(
			final AudioToCvGenInstanceConfiguration instanceConfiguration )
	{
		return new AudioToCv4UiInstanceConfiguration( instanceConfiguration );
	}

	private AudioToCv4MadUiDefinition( final BufferedImageAllocator bia,
			final AudioToCv4MadDefinition definition,
			final ImageFactory cif,
			final AudioToCv4UiInstanceConfiguration uiConfiguration ) throws DatastoreException
	{
		super( bia,
				definition,
				cif,
				MadUIStandardBackgrounds.STD_1X1_LIGHTGRAY,
				SPAN,
				AudioToCv4MadUiInstance.class,
				uiConfiguration.getChanIndexes(),
				uiConfiguration.getChanPosis(),
				uiConfiguration.getControlNames(),
				uiConfiguration.getControlTypes(),
				uiConfiguration.getControlClasses(),
				uiConfiguration.getControlBounds() );
	}
}
