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

package uk.co.modularaudio.mads.base.spectralroll.mu;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.service.madclassification.MadClassificationService;
import uk.co.modularaudio.util.audio.mad.MadChannelDirection;
import uk.co.modularaudio.util.audio.mad.MadChannelPosition;
import uk.co.modularaudio.util.audio.mad.MadChannelType;
import uk.co.modularaudio.util.audio.mad.MadClassification;
import uk.co.modularaudio.util.audio.mad.MadClassification.ReleaseState;
import uk.co.modularaudio.util.audio.mad.helper.AbstractNonConfigurableMadDefinition;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public class SpectralRollMadDefinition extends AbstractNonConfigurableMadDefinition<SpectralRollMadDefinition,SpectralRollMadInstance>
{
	// Indexes into the channels
	public final static int CONSUMER_IN = 0;
	public final static int NUM_CHANNELS = 1;

	public static final String DEFINITION_ID = "spectral_roll";

	private final static String USER_VISIBLE_NAME = "Spectral Roll";

	private final static String CLASS_GROUP = MadClassificationService.SOUND_ANALYSIS_GROUP_ID;
	private final static String CLASS_NAME = "Spectral Roll";
	private final static String CLASS_DESC = "A spectral display like a piano roll";

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
	public static final int DEFAULT_FFT_SIZE = 4096;
	public static final int MAX_WINDOW_LENGTH = 2048;

	public static final int SAMPLES_PER_FRAME = 2048;
	public static final int MIN_SAMPLES_FOR_WOLA = SAMPLES_PER_FRAME / NUM_OVERLAPS;

	public SpectralRollMadDefinition( final BaseComponentsCreationContext creationContext,
			final MadClassificationService classificationService ) throws RecordNotFoundException, DatastoreException
	{
		super( DEFINITION_ID, USER_VISIBLE_NAME,
				new MadClassification( classificationService.findGroupById( CLASS_GROUP ),
						DEFINITION_ID,
						CLASS_NAME,
						CLASS_DESC,
						ReleaseState.ALPHA ),
				new SpectralRollIOQueueBridge(),
				NUM_CHANNELS,
				CHAN_NAMES,
				CHAN_TYPES,
				CHAN_DIRS,
				CHAN_POSIS );

	}
}
