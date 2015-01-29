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

package uk.co.modularaudio.service.madclassification.impl;

import java.util.HashMap;
import java.util.Map;

import uk.co.modularaudio.service.madclassification.MadClassificationService;
import uk.co.modularaudio.util.audio.mad.MadClassificationGroup;
import uk.co.modularaudio.util.audio.mad.MadClassificationGroup.Visibility;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public class MadClassificationServiceImpl
	implements ComponentWithLifecycle, MadClassificationService
{
	private final static Map<String, MadClassificationGroup> ID_TO_GROUP_MAP = new HashMap<String, MadClassificationGroup>();

	@Override
	public void init() throws ComponentConfigurationException
	{
		final MadClassificationGroup internalGroup = new MadClassificationGroup(  Visibility.CODE, "Internal Units" );
		ID_TO_GROUP_MAP.put( MadClassificationService.INTERNAL_GROUP_ID, internalGroup );
		final MadClassificationGroup soundSourceGroup = new MadClassificationGroup( Visibility.PUBLIC, "Sound Sources");
		ID_TO_GROUP_MAP.put( MadClassificationService.SOUND_SOURCE_GROUP_ID, soundSourceGroup );
		final MadClassificationGroup soundRoutingGroup = new MadClassificationGroup( Visibility.PUBLIC, "Sound Routing");
		ID_TO_GROUP_MAP.put( MadClassificationService.SOUND_ROUTING_GROUP_ID, soundRoutingGroup );
		final MadClassificationGroup controlProcessingGroup = new MadClassificationGroup( Visibility.PUBLIC, "Control Processing");
		ID_TO_GROUP_MAP.put( MadClassificationService.CONTROL_PROCESSING_GROUP_ID, controlProcessingGroup );
		final MadClassificationGroup userRackGroup = new MadClassificationGroup( Visibility.PUBLIC, "User Rack");
		ID_TO_GROUP_MAP.put( MadClassificationService.USER_RACK_GROUP_ID, userRackGroup );
		final MadClassificationGroup soundAnalysisGroup = new MadClassificationGroup( Visibility.PUBLIC, "Sound Analysis");
		ID_TO_GROUP_MAP.put( MadClassificationService.SOUND_ANALYSIS_GROUP_ID, soundAnalysisGroup );
		final MadClassificationGroup soundProcessingGroup = new MadClassificationGroup( Visibility.PUBLIC, "Sound Processing" );
		ID_TO_GROUP_MAP.put( MadClassificationService.SOUND_PROCESSING_GROUP_ID, soundProcessingGroup );
	}

	@Override
	public void destroy()
	{
	}

	@Override
	public MadClassificationGroup findGroupById( final String groupId )
			throws DatastoreException, RecordNotFoundException
	{
		final MadClassificationGroup madClassificationGroup = ID_TO_GROUP_MAP.get( groupId );
		if( madClassificationGroup != null )
		{
			return madClassificationGroup;
		}
		else
		{
			throw new RecordNotFoundException( "Unable to find classification group: " + groupId );
		}
	}

}
