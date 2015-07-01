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

package uk.co.modularaudio.util.audio.spectraldisplay.freqscale;

import uk.co.modularaudio.util.audio.format.DataRate;


public class LinearFreqScaleComputer implements FrequencyScaleComputer
{
	@Override
	public int displayBinToSpectraBin(final int numBins, final int numDisplayPoints,
			final int currentDisplayPoint)
	{
		final float fIndex = ((float)numBins / numDisplayPoints );
		return (int)(fIndex * currentDisplayPoint );
	}
	@Override
	public int spectraBinToDisplayBin( final int numBins, final int numDisplayPoints, final int currentSpectralPoint )
	{
		final float fIndex = numDisplayPoints / numBins;
		return( (int)(currentSpectralPoint * fIndex) );
	}

	private float minFrequency = 0.0f;
	private float maxFrequency = DataRate.CD_QUALITY.getValue() / 2.0f;
	private float frequencyRange = maxFrequency - minFrequency;

	@Override
	public void setMinMaxFrequency( final float minFreq, final float maxFreq )
	{
		this.minFrequency = minFreq;
		this.maxFrequency = maxFreq;
		frequencyRange = maxFreq - minFreq;
	}

	@Override
	public float getMinFrequency()
	{
		return minFrequency;
	}

	@Override
	public float getMaxFrequency()
	{
		return maxFrequency;
	}

	@Override
	public int rawToMappedBucketMinMax( final int numBuckets, final float rawValue )
	{
		float normalisedValue = (rawValue - minFrequency) / frequencyRange;
		normalisedValue = (normalisedValue < 0.0f ? 0.0f : (normalisedValue > 1.0f ? 1.0f : normalisedValue ));
		return Math.round( (numBuckets-1) * normalisedValue );
	}

	@Override
	public float mappedBucketToRawMinMax( final int numBuckets, final int bucket )
	{
		final float normalisedValue = (bucket / (float)(numBuckets-1));
		return (normalisedValue * frequencyRange) + minFrequency;
	}
}
