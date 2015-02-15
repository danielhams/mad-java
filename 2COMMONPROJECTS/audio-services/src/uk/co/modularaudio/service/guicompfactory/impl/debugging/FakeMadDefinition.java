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

package uk.co.modularaudio.service.guicompfactory.impl.debugging;

import java.util.ArrayList;
import java.util.Map;

import uk.co.modularaudio.util.audio.mad.MadChannelConfiguration;
import uk.co.modularaudio.util.audio.mad.MadChannelDefinition;
import uk.co.modularaudio.util.audio.mad.MadClassification;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.ioqueue.MadNullLocklessQueueBridge;

class FakeMadDefinition extends MadDefinition<FakeMadDefinition, FakeMadInstance>
{

	public FakeMadDefinition( final MadClassification classification )
	{
		super( FakeRackComponent.FAKE_STR,
				FakeRackComponent.FAKE_STR,
				false,
				classification,
				new ArrayList<MadParameterDefinition>( 0 ),
				new MadNullLocklessQueueBridge<FakeMadInstance>() );
	}

	@Override
	public MadChannelConfiguration getChannelConfigurationForParameters(
			final Map<MadParameterDefinition, String> parameterValues ) throws MadProcessingException
	{
		return new MadChannelConfiguration( new MadChannelDefinition[0] );
	}
}
