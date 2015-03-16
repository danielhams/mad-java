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

package uk.co.modularaudio.mads.internal.paramfade.mu;

import uk.co.modularaudio.util.audio.fft.HannFftWindow;
import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.lookuptable.raw.RawLookupTable;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;

public class PFadeInWaveTable extends RawLookupTable
{
	public PFadeInWaveTable( final DataRate dataRate, final int millisForFadeIn)
	{
		super( calculateHalfWindowLength( dataRate.getValue(), millisForFadeIn ), false);

		final HannFftWindow fullHannWindow = new HannFftWindow( capacity * 2 );
		final float[] hwAmps = fullHannWindow.getAmps();

		System.arraycopy( hwAmps, 0, floatBuffer, 0, capacity );
	}

	private final static int calculateHalfWindowLength( final int sampleRate, final float windowLengthMillis )
	{
		return AudioTimingUtils.getNumSamplesForMillisAtSampleRate( sampleRate, windowLengthMillis );
	}
}
