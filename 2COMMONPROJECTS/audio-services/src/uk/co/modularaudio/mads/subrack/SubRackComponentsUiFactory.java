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

package uk.co.modularaudio.mads.subrack;

import java.util.Collection;

import uk.co.modularaudio.mads.subrack.mu.SubRackMadDefinition;
import uk.co.modularaudio.mads.subrack.ui.SubRackMadUiDefinition;
import uk.co.modularaudio.mads.subrack.ui.SubRackMadUiInstance;
import uk.co.modularaudio.service.madcomponentui.AbstractMadComponentUiFactory;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiInstance;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public class SubRackComponentsUiFactory extends AbstractMadComponentUiFactory
{
	private SubRackComponentsFactory subRackComponentsFactory;

	@Override
	public void setupTypeToDefinitionClasses() throws DatastoreException
	{
		final Collection<MadDefinition<?,?>> auds = subRackComponentsFactory.listDefinitions();
		for( final MadDefinition<?,?> aud : auds )
		{
			if( aud instanceof SubRackMadDefinition )
			{
				final SubRackMadDefinition fi = (SubRackMadDefinition)aud;
				final SubRackMadUiDefinition fiud = new SubRackMadUiDefinition( bufferedImageAllocationService, fi, componentImageFactory, imageRoot );
				componentDefinitionToUiDefinitionMap.put( aud, fiud );
			}
		}
	}

	public void setSubRackComponentsFactory( final SubRackComponentsFactory subRackComponentsFactory )
	{
		this.subRackComponentsFactory = subRackComponentsFactory;
	}

	@Override
	public void destroyUiInstance( final IMadUiInstance<?, ?> instanceToDestroy )
			throws DatastoreException, RecordNotFoundException
	{
		// Clean it up before we actually destroy it
		final SubRackMadUiInstance uiInstance = (SubRackMadUiInstance)instanceToDestroy;
		uiInstance.cleanup();

		super.destroyUiInstance( instanceToDestroy );
	}

}
