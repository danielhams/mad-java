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

package uk.co.modularaudio.mads.base.cvtoaudio4.ui;

import uk.co.modularaudio.mads.base.audiocvgen.mu.AudioToCvGenInstanceConfiguration;
import uk.co.modularaudio.mads.base.audiocvgen.ui.AudioToCvGenMadUiDefinition;
import uk.co.modularaudio.mads.base.cvtoaudio4.mu.CvToAudio4MadDefinition;
import uk.co.modularaudio.mads.base.cvtoaudio4.mu.CvToAudio4MadInstance;
import uk.co.modularaudio.util.audio.gui.mad.MadUIStandardBackgrounds;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class CvToAudio4MadUiDefinition extends AudioToCvGenMadUiDefinition<CvToAudio4MadDefinition, CvToAudio4MadInstance, CvToAudio4MadUiInstance>
{
	final static Span SPAN = new Span( 1, 1 );

	public CvToAudio4MadUiDefinition( final CvToAudio4MadDefinition definition )
		throws DatastoreException
	{
		this( definition,
				instanceConfigToUiConfig( CvToAudio4MadDefinition.INSTANCE_CONFIGURATION ) );
	}

	private static CvToAudio4UiInstanceConfiguration instanceConfigToUiConfig(
			final AudioToCvGenInstanceConfiguration instanceConfiguration )
	{
		return new CvToAudio4UiInstanceConfiguration( instanceConfiguration );
	}

	private CvToAudio4MadUiDefinition( final CvToAudio4MadDefinition definition,
			final CvToAudio4UiInstanceConfiguration uiConfiguration ) throws DatastoreException
	{
		super( definition,
				MadUIStandardBackgrounds.STD_1X1_LIGHTGRAY,
				SPAN,
				CvToAudio4MadUiInstance.class,
				uiConfiguration.getChanIndexes(),
				uiConfiguration.getChanPosis(),
				uiConfiguration.getControlNames(),
				uiConfiguration.getControlTypes(),
				uiConfiguration.getControlClasses(),
				uiConfiguration.getControlBounds() );
	}
}
