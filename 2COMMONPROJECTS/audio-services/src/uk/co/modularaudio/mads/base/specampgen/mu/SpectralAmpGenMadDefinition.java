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

package uk.co.modularaudio.mads.base.specampgen.mu;

import uk.co.modularaudio.util.audio.mad.MadChannelDirection;
import uk.co.modularaudio.util.audio.mad.MadChannelPosition;
import uk.co.modularaudio.util.audio.mad.MadChannelType;
import uk.co.modularaudio.util.audio.mad.MadClassification;
import uk.co.modularaudio.util.audio.mad.helper.AbstractNonConfigurableMadDefinition;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public class SpectralAmpGenMadDefinition<D extends SpectralAmpGenMadDefinition<D,I>,
	I extends SpectralAmpGenMadInstance<D,I>>
	extends AbstractNonConfigurableMadDefinition<D,I>
{
	// Indexes into the channels
	public final static int CONSUMER_IN = 0;
	public final static int NUM_CHANNELS = 1;

	// These must match the channel indexes given above
	private final static String[] CHAN_NAMES = new String[] { "Input Wave" };

	private final static MadChannelType[] CHAN_TYPES = new MadChannelType[] { MadChannelType.AUDIO };

	private final static MadChannelDirection[] CHAN_DIRS = new MadChannelDirection[] { MadChannelDirection.CONSUMER };

	private final static MadChannelPosition[] CHAN_POSIS = new MadChannelPosition[] { MadChannelPosition.MONO };


	// Definitions for the FFT
	public final static int MAX_SAMPLES_PER_STFT = 16384;
	public final static int MAX_NUM_FFT_BINS = (MAX_SAMPLES_PER_STFT / 2) + 1;

	// STFT parameters
	public static final int NUM_OVERLAPS = 4;
	public static final int DEFAULT_FFT_SIZE = 512;
	public static final int MAX_WINDOW_LENGTH = 2048;

	public static final int SAMPLES_PER_FRAME = 2048;
	public static final int MIN_SAMPLES_FOR_WOLA = SAMPLES_PER_FRAME / NUM_OVERLAPS;

	public SpectralAmpGenMadDefinition(
			final String definitionId,
			final String userVisibleName,
			final MadClassification classification
			)
		throws RecordNotFoundException, DatastoreException
	{
		super( definitionId, userVisibleName,
				classification,
				new SpectralAmpGenIOQueueBridge<I>(),
				NUM_CHANNELS,
				CHAN_NAMES,
				CHAN_TYPES,
				CHAN_DIRS,
				CHAN_POSIS );

	}
}
