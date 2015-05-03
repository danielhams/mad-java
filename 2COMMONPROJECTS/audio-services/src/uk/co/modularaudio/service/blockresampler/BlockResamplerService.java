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

package uk.co.modularaudio.service.blockresampler;

import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;

import uk.co.modularaudio.service.samplecaching.SampleCacheClient;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public interface BlockResamplerService
{
	public final static float MAGIC_FLOAT = 9999f;
	public final static float EXCESSIVE_FLOAT = 10.0f;

	BlockResamplingClient createResamplingClient( String pathToFile,
			BlockResamplingMethod resamplingMethod )
		throws DatastoreException, IOException, UnsupportedAudioFileException;

	BlockResamplingClient promoteSampleCacheClientToResamplingClient( SampleCacheClient sampleCacheClient,
			BlockResamplingMethod cubic );

	void destroyResamplingClient( BlockResamplingClient resamplingClient )
		throws DatastoreException, RecordNotFoundException;


	RealtimeMethodReturnCodeEnum fetchAndResample( BlockResamplingClient resamplingClient,
			int outputSampleRate,
			float playbackSpeed,
			float[] outputLeftFloats, int outputLeftOffset,
			float[] outputRightFloats, int outputRightOffset,
			int numFramesRequired,
			float[] tmpBuffer,
			int tmpBufferOffset );

	RealtimeMethodReturnCodeEnum fetchAndResampleVarispeed( BlockResamplingClient resamplingClient,
			int outputSampleRate,
			float[] playbackSpeeds, int playbackOffset,
			float[] outputLeftFloats, int outputLeftOffset,
			float[] outputRightFloats, int outputRightOffset,
			int numFramesRequired,
			float[] tmpBuffer,
			int tmpBufferOffset );

}
