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

package uk.co.modularaudio.mads.base.scopegen.mu;

import uk.co.modularaudio.util.audio.mad.MadChannelDirection;
import uk.co.modularaudio.util.audio.mad.MadChannelPosition;
import uk.co.modularaudio.util.audio.mad.MadChannelType;
import uk.co.modularaudio.util.audio.mad.MadClassification;
import uk.co.modularaudio.util.audio.mad.helper.AbstractNonConfigurableMadDefinition;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public class ScopeGenMadDefinition<D extends ScopeGenMadDefinition<D,I>,
	I extends ScopeGenMadInstance<D,I>>
	extends	AbstractNonConfigurableMadDefinition<D, I>
{
	// Indexes into the channels
	public final static int SCOPE_TRIGGER = 0;
	public final static int SCOPE_INPUT_0 = 1;
	public final static int SCOPE_INPUT_1 = 2;
	public final static int SCOPE_INPUT_2 = 3;
	public final static int SCOPE_INPUT_3 = 4;
	private final static int NUM_CHANNELS = 5;
	public final static int NUM_VIS_CHANNELS = NUM_CHANNELS;

	// These must match the channel indexes given above
	private final static String[] CHAN_NAMES = new String[] {
		"Input Trigger",
		"Input Signal 1",
		"Input Signal 2",
		"Input Signal 3",
		"Input Signal 4"
	};

	private final static MadChannelType[] CHAN_TYPES = new MadChannelType[] {
		MadChannelType.CV,
		MadChannelType.CV,
		MadChannelType.CV,
		MadChannelType.CV,
		MadChannelType.CV
	};

	private final static MadChannelDirection[] CHAN_DIRS = new MadChannelDirection[] {
		MadChannelDirection.CONSUMER,
		MadChannelDirection.CONSUMER,
		MadChannelDirection.CONSUMER,
		MadChannelDirection.CONSUMER,
		MadChannelDirection.CONSUMER
	};

	private final static MadChannelPosition[] CHAN_POSI = new MadChannelPosition[] {
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO
	};

	public ScopeGenMadDefinition(
			final String definitionId,
			final String userVisibleName,
			final MadClassification classification )
		throws RecordNotFoundException, DatastoreException
	{
		super( definitionId, userVisibleName,
				classification,
				new ScopeGenIOQueueBridge<I>(),
				NUM_CHANNELS,
				CHAN_NAMES,
				CHAN_TYPES,
				CHAN_DIRS,
				CHAN_POSI );

	}
}
