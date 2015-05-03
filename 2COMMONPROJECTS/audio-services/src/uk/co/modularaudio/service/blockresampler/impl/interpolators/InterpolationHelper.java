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

package uk.co.modularaudio.service.blockresampler.impl.interpolators;

public class InterpolationHelper
{

	public static int getNumSourceSamplesForVarispeed( final float curFpOffset,
			final float[] replaySpeeds,
			final int numFramesRequired )
	{
		return 0;
	}

	public static int getNumSourceSamplesForSpeed( final float curFpOffset,
			final float replaySpeed,
			final int numFramesRequired )
	{
		if( replaySpeed > 0.0f )
		{
			return (int)Math.ceil((numFramesRequired - curFpOffset) * replaySpeed);
		}
		else if( replaySpeed < 0.0f )
		{
			return (int)Math.ceil((numFramesRequired - (1.0f - curFpOffset)) * replaySpeed);
		}
		else // if( replaySpeed == 0.0f )
		{
			return 1;
		}
	}

}
