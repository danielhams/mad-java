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

package uk.co.modularaudio.util.audio.mad.timing;

import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;

public class MadTimingParameters
{
	protected long nanosPerBackEndPeriod;
	protected long nanosPerBackEndSample;
	protected long nanosPerFrontEndPeriod;
	protected int sampleFramesPerFrontEndPeriod;
	protected int sampleFramesPerBackEndPeriod;

	public MadTimingParameters( final DataRate dataRate,
			final int sampleFramesPerBackEndPeriod,
			final int frontEndFps )
	{
		final int sampleRate = dataRate.getValue();
		this.nanosPerBackEndPeriod = AudioTimingUtils.getNumNanosecondsForBufferLength( sampleRate, sampleFramesPerBackEndPeriod );
		this.nanosPerBackEndSample = nanosPerBackEndPeriod / sampleFramesPerBackEndPeriod;
		final double secondsPerFrontEndPeriod = 1.0f / frontEndFps;
		this.nanosPerFrontEndPeriod = (long)(secondsPerFrontEndPeriod * 1000000000L);
		this.sampleFramesPerFrontEndPeriod = AudioTimingUtils.getNumSamplesForNanosAtSampleRate(sampleRate, nanosPerFrontEndPeriod );
		this.sampleFramesPerBackEndPeriod = sampleFramesPerBackEndPeriod;
	}

	public MadTimingParameters( final long nanosPerBackEndPeriod,
			final long nanosPerBackEndSample,
			final long nanosPerFrontEndPeriod,
			final int sampleFramesPerFrontEndPeriod,
			final int sampleFramesPerBackEndPeriod )
	{
		this.nanosPerBackEndPeriod = nanosPerBackEndPeriod;
		this.nanosPerBackEndSample = nanosPerBackEndSample;
		this.nanosPerFrontEndPeriod = nanosPerFrontEndPeriod;
		this.sampleFramesPerFrontEndPeriod = sampleFramesPerFrontEndPeriod;
		this.sampleFramesPerBackEndPeriod = sampleFramesPerBackEndPeriod;
	}

	public final void reset( final MadTimingParameters from )
	{
		this.nanosPerBackEndPeriod = from.nanosPerBackEndPeriod;
		this.nanosPerBackEndSample = from.nanosPerBackEndSample;
		this.nanosPerFrontEndPeriod = from.nanosPerFrontEndPeriod;
		this.sampleFramesPerFrontEndPeriod = from.sampleFramesPerFrontEndPeriod;
		this.sampleFramesPerBackEndPeriod = from.sampleFramesPerBackEndPeriod;
	}

	public long getNanosPerBackEndPeriod()
	{
		return nanosPerBackEndPeriod;
	}

	public long getNanosPerBackEndSample()
	{
		return nanosPerBackEndSample;
	}

	public long getNanosPerFrontEndPeriod()
	{
		return nanosPerFrontEndPeriod;
	}

	public int getSampleFramesPerFrontEndPeriod()
	{
		return sampleFramesPerFrontEndPeriod;
	}

	public int getSampleFramesPerBackEndPeriod()
	{
		return sampleFramesPerBackEndPeriod;
	}

	public void reset( final long nanosPerBackEndPeriod,
			final long nanosPerBackEndSample,
			final long nanosPerFrontEndPeriod,
			final int sampleFramesPerFrontEndPeriod,
			final int sampleFramesPerBackEndPeriod )
	{
		this.nanosPerBackEndPeriod = nanosPerBackEndPeriod;
		this.nanosPerBackEndSample = nanosPerBackEndSample;
		this.nanosPerFrontEndPeriod = nanosPerFrontEndPeriod;
		this.sampleFramesPerFrontEndPeriod = sampleFramesPerFrontEndPeriod;
		this.sampleFramesPerBackEndPeriod = sampleFramesPerBackEndPeriod;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("NanosBEP: ");
		sb.append( nanosPerBackEndPeriod );
		sb.append(" NanosBES: ");
		sb.append( nanosPerBackEndSample );
		sb.append(" NanosFEP: ");
		sb.append( nanosPerFrontEndPeriod );
		sb.append(" SamplesFEP: ");
		sb.append( sampleFramesPerFrontEndPeriod );
		sb.append(" SamplesBEP: ");
		sb.append( sampleFramesPerBackEndPeriod );

		return sb.toString();
	}


}
