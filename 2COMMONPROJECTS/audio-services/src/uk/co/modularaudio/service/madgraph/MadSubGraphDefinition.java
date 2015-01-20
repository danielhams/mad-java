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

package uk.co.modularaudio.service.madgraph;

import java.util.ArrayList;

import uk.co.modularaudio.util.audio.mad.MadClassification;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.graph.MadGraphDefinition;
import uk.co.modularaudio.util.audio.mad.graph.MadGraphInstance;
import uk.co.modularaudio.util.audio.mad.graph.MadGraphQueueBridge;

public class MadSubGraphDefinition<AUGD extends MadGraphDefinition<AUGD,AUGI>,
	AUGI extends MadGraphInstance<AUGD,AUGI> >
	extends MadGraphDefinition<AUGD, AUGI>
{
	public MadSubGraphDefinition( String id, String name, MadClassification classification, MadGraphQueueBridge<AUGI> ioQueueBridge  )
	{
		super( id, name, false, classification, new ArrayList<MadParameterDefinition>(), ioQueueBridge );
	}
}
