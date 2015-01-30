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

package uk.co.modularaudio.service.madclassification;

import uk.co.modularaudio.util.audio.mad.MadClassificationGroup;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public interface MadClassificationService
{

	public final static String INTERNAL_GROUP_ID = "internal";
	public final static String SOUND_SOURCE_GROUP_ID = "sound_source";
	public final static String SOUND_ROUTING_GROUP_ID = "sound_routing";
	public final static String CONTROL_PROCESSING_GROUP_ID = "control_processing";
	public final static String USER_RACK_GROUP_ID = "user_rack";
	public final static String SOUND_PROCESSING_GROUP_ID = "sound_processing";
	public final static String SOUND_ANALYSIS_GROUP_ID = "sound_analysis";

	MadClassificationGroup findGroupById( String groupId ) throws DatastoreException, RecordNotFoundException;

}
