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

package uk.co.modularaudio.mads.base.imixer3.mu;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.mads.base.imixern.mu.MixerNMadDefinition;
import uk.co.modularaudio.mads.base.imixern.mu.MixerNInstanceConfiguration;
import uk.co.modularaudio.service.madclassification.MadClassificationService;
import uk.co.modularaudio.util.audio.mad.MadClassification;
import uk.co.modularaudio.util.audio.mad.MadClassification.ReleaseState;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public class IMixer3MadDefinition extends MixerNMadDefinition<IMixer3MadDefinition, IMixer3MadInstance>
{
	private static Log log = LogFactory.getLog( IMixer3MadDefinition.class.getName() );

	public final static String DEFINITION_ID = "imixer3";
	private final static String USER_VISIBLE_NAME = "Mixer (Three Stereo Lanes)";

	private final static String CLASS_GROUP = MadClassificationService.SOUND_PROCESSING_GROUP_ID;

	private final static String CLASS_NAME="Mixer (Three Stereo Lanes)";
	private final static String CLASS_DESC="A three lane stereo mixer";

	private final static int NUM_MIXER_LANES = 3;

	public final static MixerNInstanceConfiguration INSTANCE_CONFIGURATION = getMixer3InstanceConfiguration();

	private static MixerNInstanceConfiguration getMixer3InstanceConfiguration()
	{
		try
		{
			final MixerNInstanceConfiguration retVal = new MixerNInstanceConfiguration( NUM_MIXER_LANES );
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

	public IMixer3MadDefinition( final BaseComponentsCreationContext creationContent,
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
