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

package uk.co.modularaudio.util.audio.spectraldisplay.runav;

import java.util.Arrays;

public class PeakGrabComputer implements RunningAverageComputer
{
	private final float[] binValues = new float[ 16384 ];
	private boolean peaksGrabbed;

	public PeakGrabComputer()
	{
		reset();
	}

	@Override
	public void computeNewRunningAverages( final int currentNumBins, final float[] valuesToAdd, final float[] runningValues )
	{
		if( !peaksGrabbed )
		{
			System.arraycopy( valuesToAdd, 0, binValues, 0, currentNumBins );
			peaksGrabbed = true;
		}
		System.arraycopy( binValues, 0, runningValues, 0, currentNumBins );
	}

	public final void reset()
	{
		Arrays.fill( binValues, 0.0f );
		peaksGrabbed = false;
	}
}
