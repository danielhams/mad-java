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

package uk.co.modularaudio.service.madcomponentui;

import uk.co.modularaudio.util.audio.gui.mad.IMadUiInstance;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.table.Span;

public interface MadComponentUiService
{
	// How we can dynamically add plugins
	public void registerComponentUiFactory( MadComponentUiFactory componentUiFactory )
			throws DatastoreException;
	public void unregisterComponentUiFactory( MadComponentUiFactory componentUiFactory )
			throws DatastoreException;

	// Creating the UiComponentInstance for a particular mad instance
	public IMadUiInstance<?,?> createUiInstanceForInstance( MadInstance<?,?> instance )
			throws DatastoreException, RecordNotFoundException;
	public void destroyUiInstance( IMadUiInstance<?,?> uiInstanceToDestroy ) throws DatastoreException, RecordNotFoundException;

	// Allow the starting painting to know how big a particular component takes up
	// for it's caching
	public Span getUiSpanForDefinition( MadDefinition<?, ?> definition )
		throws DatastoreException, RecordNotFoundException;
}
