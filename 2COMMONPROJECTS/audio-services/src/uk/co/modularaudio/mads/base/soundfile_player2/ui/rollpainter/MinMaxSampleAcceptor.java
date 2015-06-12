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

package uk.co.modularaudio.mads.base.soundfile_player2.ui.rollpainter;

import uk.co.modularaudio.service.samplecaching.SampleAcceptor;
import uk.co.modularaudio.util.math.MinMaxComputer;

public class MinMaxSampleAcceptor implements SampleAcceptor
{
//	private static Log log = LogFactory.getLog( MinMaxSampleAcceptor.class.getName());

//	public float minValue;
//	public float maxValue;
	public float[] minMaxValues = new float[2];

	public MinMaxSampleAcceptor()
	{
		reset();
	}

	public final void reset()
	{
		minMaxValues[0] = Float.MAX_VALUE;
		minMaxValues[1] = -minMaxValues[0];
	}

	@Override
	public void acceptEmptySamples( final long framePosition, final int numChannelsPerFrame, final int numFramesOfZeros )
	{
//		log.debug("Received empty samples at frame position " + framePosition + " of " + numFramesOfZeros );
		minMaxValues[0] = 0.0f;
		minMaxValues[1] = 0.0f;
	}

	@Override
	public void acceptSamples(
			final long framePosition,
			final int numChannelsPerFrame,
			final int numFramesToAccept,
			final float[] blockBuffer,
			final int blockFloatsOffset)
	{
//		log.debug("Received samples at frame position " + framePosition + " of " + numFramesToAccept  + " from " + blockFloatsOffset );

//		int numFloatsToCheck = numFramesToAccept * numChannels;
//		for( int i = 0 ; i < numFloatsToCheck ; i=i+numChannels )
//		{
//			float value = blockBuffer[ i + blockFloatsOffset ];
//			if( value < minValue )
//			{
//				minValue = value;
//			}
//			if( value > maxValue )
//			{
//				maxValue = value;
//			}
//		}
		MinMaxComputer.calcMinMaxForFloatsWithStride(blockBuffer, blockFloatsOffset, numFramesToAccept, numChannelsPerFrame, minMaxValues );
	}
}
