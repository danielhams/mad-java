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

package uk.co.modularaudio.util.audio.oscillatortable.pulsewidth;

import uk.co.modularaudio.util.audio.math.AudioMath;
import uk.co.modularaudio.util.audio.oscillatortable.PulseWidthMapper;

public final class HardKneePulseWidthMapper implements PulseWidthMapper
{
	@Override
	public float adjustPwPos( final float iPw, final float iPos)
	{
		float pw = iPw;
		float pos = iPos;
		if( pw == 1.0f )
		{
			// Full pulse, don't adjust
			return iPos;
		}
		else if( pw == 0.0f )
		{
			// Just in case pulsewidth = 0.0f;
			pw = AudioMath.MIN_FLOATING_POINT_24BIT_VAL_F;
		}

		float retVal;
		final float bend = 0.5f * pw;
		if( pos <= bend )
		{
			// normalise it from 0->1
			pos = pos / bend;
			// Map back to 0 -> pw
			retVal = pos * 0.5f;
		}
		else
		{
			final float upperLength = 1.0f - bend;
			// Remove lower bend
			pos = pos - bend;
			// remap back to 0 -> 1
			pos = pos / upperLength;
			// And add back a half
			retVal = (pos * 0.5f) + 0.5f;
		}
		return retVal;
	}

}
