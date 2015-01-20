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

package test.uk.co.modularaudio.util.audio.mad.factory;

import java.util.Map;

import test.uk.co.modularaudio.util.audio.mad.units.singlechanvolume.SingleChannelVolumeMadDefinition;
import test.uk.co.modularaudio.util.audio.mad.units.singlechanvolume.SingleChannelVolumeMadInstance;
import test.uk.co.modularaudio.util.audio.mad.units.stereotee.StereoTeeMadDefinition;
import test.uk.co.modularaudio.util.audio.mad.units.stereotee.StereoTeeMadInstance;
import uk.co.modularaudio.util.audio.mad.MadChannelConfiguration;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.factory.IMadInstanceFactory;
import uk.co.modularaudio.util.exception.DatastoreException;

public class TestMadInstanceFactory implements IMadInstanceFactory
{

	@Override
	public MadInstance<?,?> createInstanceForDefinition(
			MadDefinition<?,?> definition,
			Map<MadParameterDefinition, String> parameterValues,
			String instanceName )
			throws DatastoreException
	{
		MadInstance<?,?> retVal = null;
		if( definition instanceof StereoTeeMadDefinition )
		{
			StereoTeeMadDefinition realDef = (StereoTeeMadDefinition)definition;
			
			// No parameters, and a default channel configuration
			MadChannelConfiguration channelConfiguration = realDef.getChannelConfigurationForParameters( parameterValues );

			retVal = new StereoTeeMadInstance( instanceName, realDef, parameterValues, channelConfiguration );
		}
		else if( definition instanceof SingleChannelVolumeMadDefinition )
		{
			SingleChannelVolumeMadDefinition realDef = (SingleChannelVolumeMadDefinition)definition;

			MadChannelConfiguration channelConfiguration = realDef.getChannelConfigurationForParameters( parameterValues );
			
			retVal = new SingleChannelVolumeMadInstance( instanceName, realDef, parameterValues, channelConfiguration );
		}
		else
		{
			String msg = "Factory cannot create instance of unknown definition: " + definition.getName();
			throw new DatastoreException(  msg  );
		}

		return retVal;
	}

	@Override
	public void destroyInstance( MadInstance<?,?> instanceToDestroy )
			throws DatastoreException
	{
	}

}
