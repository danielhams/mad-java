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

import uk.co.modularaudio.util.audio.oscillatortable.PulseWidthMapper;

public final class ExpCurvePulseWidthMapper implements PulseWidthMapper
{
	@Override
	public float adjustPwPos( final float pulseWidth, final float pos )
	{
		// Simplified version of whats in the normalised values mapper for curve 4
		final float curvePos = (float)Math.sqrt( (pos * 2) - (pos * pos) );
		final float curveInfluence = (1.0f - pulseWidth) * 0.7f;
		final float posInfluence = 1.0f - curveInfluence;
		return (curveInfluence * curvePos) + (pos * posInfluence);
	}

}
