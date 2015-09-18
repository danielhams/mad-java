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

package uk.co.modularaudio.mads.base.audiotocv4.mu;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.mads.base.audiocvgen.mu.AudioToCvGenInstanceConfiguration;
import uk.co.modularaudio.mads.base.audiocvgen.mu.AudioToCvGenMadDefinition;
import uk.co.modularaudio.service.madclassification.MadClassificationService;
import uk.co.modularaudio.util.audio.mad.MadChannelType;
import uk.co.modularaudio.util.audio.mad.MadClassification;
import uk.co.modularaudio.util.audio.mad.MadClassification.ReleaseState;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public class AudioToCv4MadDefinition
	extends AudioToCvGenMadDefinition<AudioToCv4MadDefinition, AudioToCv4MadInstance>
{
	private static Log log = LogFactory.getLog( AudioToCv4MadDefinition.class.getName() );

	public final static String DEFINITION_ID = "audiotocv4";
	private final static String USER_VISIBLE_NAME = "Audio To CV (Four Channels)";

	private final static String CLASS_GROUP = MadClassificationService.SOUND_ROUTING_GROUP_ID;

	private final static String CLASS_NAME="Audio To CV (Four Channels)";
	private final static String CLASS_DESC="A four channel converter from audio signals to control values.";

	private final static int NUM_CONVERSION_CHANNELS = 4;

	public final static AudioToCvGenInstanceConfiguration INSTANCE_CONFIGURATION = getATCInstanceConfiguration();

	private static AudioToCvGenInstanceConfiguration getATCInstanceConfiguration()
	{
		try
		{
			final AudioToCvGenInstanceConfiguration retVal = new AudioToCvGenInstanceConfiguration(
					MadChannelType.AUDIO,
					NUM_CONVERSION_CHANNELS );
			return retVal;
		}
		catch( final MadProcessingException de )
		{
			if( log.isErrorEnabled() )
			{
				log.error("Exception caught initialising instance configuration: " + de.toString(), de);
			}
			return null;
		}
	}

	public AudioToCv4MadDefinition( final BaseComponentsCreationContext creationContent,
			final MadClassificationService classService )
		throws DatastoreException, RecordNotFoundException
	{
		super( DEFINITION_ID, USER_VISIBLE_NAME,
				new MadClassification( classService.findGroupById( CLASS_GROUP ),
						DEFINITION_ID,
						CLASS_NAME,
						CLASS_DESC,
						ReleaseState.RELEASED ),
				INSTANCE_CONFIGURATION );
	}

}
