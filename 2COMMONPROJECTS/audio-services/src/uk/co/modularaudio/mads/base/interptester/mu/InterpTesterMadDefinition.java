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

package uk.co.modularaudio.mads.base.interptester.mu;

import java.util.Map;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.mads.base.BaseMadDefinition;
import uk.co.modularaudio.service.madclassification.MadClassificationService;
import uk.co.modularaudio.util.audio.mad.MadChannelDefinition.MadChannelDirection;
import uk.co.modularaudio.util.audio.mad.MadChannelDefinition.MadChannelPosition;
import uk.co.modularaudio.util.audio.mad.MadChannelDefinition.MadChannelType;
import uk.co.modularaudio.util.audio.mad.MadClassification;
import uk.co.modularaudio.util.audio.mad.MadClassification.ReleaseState;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.helper.AbstractNonConfigurableMadDefinition;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public class InterpTesterMadDefinition
	extends AbstractNonConfigurableMadDefinition<InterpTesterMadDefinition, InterpTesterMadInstance>
	implements BaseMadDefinition
{
	public static final String DEFINITION_ID = "interptester";

	private final static String USER_VISIBLE_NAME = "Control Interpolation Tester";

	private final static String CLASS_GROUP = MadClassificationService.SOUND_ROUTING_GROUP_ID;
	private final static String CLASS_NAME = "Control Interpolation Tester";
	private final static String CLASS_DESC = "A tester of control interpolation";

	public final static int NUM_CHANNELS = InterpolatorType.values().length * 2;
	private final static String[] CHAN_NAMES;
	private final static MadChannelType[] CHAN_TYPES;
	private final static MadChannelDirection[] CHAN_DIRS;
	private final static MadChannelPosition[] CHAN_POSI;

	static
	{
		CHAN_NAMES = new String[NUM_CHANNELS];
		CHAN_TYPES = new MadChannelType[NUM_CHANNELS];
		CHAN_DIRS = new MadChannelDirection[NUM_CHANNELS];
		CHAN_POSI = new MadChannelPosition[NUM_CHANNELS];

		int chanNum = 0;
		for( final InterpolatorType it : InterpolatorType.values() )
		{
			CHAN_NAMES[chanNum] = it.getChannelPrefix() + " NoTS CV Out";
			CHAN_TYPES[chanNum] = MadChannelType.CV;
			CHAN_DIRS[chanNum] = MadChannelDirection.PRODUCER;
			CHAN_POSI[chanNum] = MadChannelPosition.MONO;
			chanNum++;
		}
		for( final InterpolatorType it : InterpolatorType.values() )
		{
			CHAN_NAMES[chanNum] = it.getChannelPrefix() + " CV Out";
			CHAN_TYPES[chanNum] = MadChannelType.CV;
			CHAN_DIRS[chanNum] = MadChannelDirection.PRODUCER;
			CHAN_POSI[chanNum] = MadChannelPosition.MONO;
			chanNum++;
		}
	}

	private final BaseComponentsCreationContext creationContext;

	public InterpTesterMadDefinition( final BaseComponentsCreationContext creationContext,
			final MadClassificationService classService ) throws RecordNotFoundException, DatastoreException
	{
		super( DEFINITION_ID, USER_VISIBLE_NAME,
				new MadClassification( classService.findGroupById( CLASS_GROUP ),
						DEFINITION_ID,
						CLASS_NAME,
						CLASS_DESC,
						ReleaseState.BETA ),
				new InterpTesterIOQueueBridge(),
				NUM_CHANNELS,
				CHAN_NAMES,
				CHAN_TYPES,
				CHAN_DIRS,
				CHAN_POSI );
		this.creationContext = creationContext;
	}

	@Override
	public MadInstance<?, ?> createInstance( final Map<MadParameterDefinition, String> parameterValues, final String instanceName )
			throws MadProcessingException
	{
		return new InterpTesterMadInstance(
				creationContext,
				instanceName,
				this,
				parameterValues,
				getChannelConfigurationForParameters( parameterValues ) );
	}
}
