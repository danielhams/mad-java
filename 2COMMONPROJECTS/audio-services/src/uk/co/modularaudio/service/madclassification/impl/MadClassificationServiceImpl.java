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
	private Map<String, MadClassificationGroup> idToGroupMap = new HashMap<String, MadClassificationGroup>();

	@Override
	public void init() throws ComponentConfigurationException
	{
		MadClassificationGroup internalGroup = new MadClassificationGroup(  Visibility.CODE, "Internal Units" );
		idToGroupMap.put( MadClassificationService.INTERNAL_GROUP_ID, internalGroup );
		MadClassificationGroup soundSourceGroup = new MadClassificationGroup( Visibility.PUBLIC, "Sound Sources");
		idToGroupMap.put( MadClassificationService.SOUND_SOURCE_GROUP_ID, soundSourceGroup );
		MadClassificationGroup soundRoutingGroup = new MadClassificationGroup( Visibility.PUBLIC, "Sound Routing");
		idToGroupMap.put( MadClassificationService.SOUND_ROUTING_GROUP_ID, soundRoutingGroup );
		MadClassificationGroup controlProcessingGroup = new MadClassificationGroup( Visibility.PUBLIC, "Control Processing");
		idToGroupMap.put( MadClassificationService.CONTROL_PROCESSING_GROUP_ID, controlProcessingGroup );
		MadClassificationGroup userRackGroup = new MadClassificationGroup( Visibility.PUBLIC, "User Rack");
		idToGroupMap.put( MadClassificationService.USER_RACK_GROUP_ID, userRackGroup );
		MadClassificationGroup soundAnalysisGroup = new MadClassificationGroup( Visibility.PUBLIC, "Sound Analysis");
		idToGroupMap.put( MadClassificationService.SOUND_ANALYSIS_GROUP_ID, soundAnalysisGroup );
		MadClassificationGroup soundProcessingGroup = new MadClassificationGroup( Visibility.PUBLIC, "Sound Processing" );
		idToGroupMap.put( MadClassificationService.SOUND_PROCESSING_GROUP_ID, soundProcessingGroup );
	}

	@Override
	public void destroy()
	{
	}

	@Override
	public MadClassificationGroup findGroupById( String groupId )
			throws DatastoreException, RecordNotFoundException
	{
		MadClassificationGroup nadClassificationGroup = idToGroupMap.get( groupId );
		if( nadClassificationGroup != null )
		{
			return nadClassificationGroup;
		}
		else
		{
			throw new RecordNotFoundException( "Unable to find classification group: " + groupId );
		}
	}

}
