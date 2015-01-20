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

package uk.co.modularaudio.mads.internal.paramfade.mu;

import java.util.Map;

import uk.co.modularaudio.mads.internal.InternalComponentsCreationContext;
import uk.co.modularaudio.service.madclassification.MadClassificationService;
import uk.co.modularaudio.util.audio.mad.MadChannelConfiguration;
import uk.co.modularaudio.util.audio.mad.MadClassification;
import uk.co.modularaudio.util.audio.mad.MadClassification.ReleaseState;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public class PFadeInMadDefinition extends MadDefinition<PFadeInMadDefinition,PFadeInMadInstance>
{
	public final static String DEFINITION_ID = "pfade_in";

	private final static String userVisibleName = "Parametric Fade In";

	private final static String classificationGroup = MadClassificationService.INTERNAL_GROUP_ID;
	private final static String classificationName = "Parametric Fade In";
	private final static String classificationDescription = "Fade in a source with a configurable number of channels";

	public PFadeInMadDefinition( InternalComponentsCreationContext creationContext,
			MadClassificationService classificationService ) throws RecordNotFoundException, DatastoreException
	{
		super( DEFINITION_ID, userVisibleName, true,
				new MadClassification( classificationService.findGroupById( classificationGroup ),
						DEFINITION_ID,
						classificationName,
						classificationDescription,
						ReleaseState.RELEASED ),
				PFadeDefinitions.parameterDefinitions,
				new PFadeInIOQueueBridge() );
	}

	@Override
	public MadChannelConfiguration getChannelConfigurationForParameters( Map<MadParameterDefinition, String> parameterValues ) throws MadProcessingException
	{
		PFadeConfiguration ic = new PFadeConfiguration( parameterValues );
		return ic.getChannelConfiguration();
	}
}
