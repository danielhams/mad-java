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
	protected long nanosPerBackEndPeriod = -1;
	protected long nanosPerBackEndSample = -1;
	protected long nanosPerFrontEndPeriod = -1;
	protected int sampleFramesPerFrontEndPeriod = -1;
	protected long nanosOutputLatency = -1;
	
	public MadTimingParameters( DataRate dataRate,
			int sampleFramesPerBackEndPeriod,
			int frontEndFps,
			long nanosOutputLatency )
	{
		int sampleRate = dataRate.getValue();
		this.nanosPerBackEndPeriod = AudioTimingUtils.getNumNanosecondsForBufferLength( sampleRate, sampleFramesPerBackEndPeriod );
		this.nanosPerBackEndSample = nanosPerBackEndPeriod / sampleFramesPerBackEndPeriod;
		double secondsPerFrontEndPeriod = 1.0f / frontEndFps;
		this.nanosPerFrontEndPeriod = (long)(secondsPerFrontEndPeriod * 1000000000L);
		this.sampleFramesPerFrontEndPeriod = AudioTimingUtils.getNumSamplesForNanosAtSampleRate(sampleRate, nanosPerFrontEndPeriod );
		this.nanosOutputLatency = nanosOutputLatency;
	}

	public MadTimingParameters( long nanosPerBackEndPeriod,
			long nanosPerBackEndSample,
			long nanosPerFrontEndPeriod,
			int sampleFramesPerFrontEndPeriod,
			long nanosOutputLatency )
	{
		this.nanosPerBackEndPeriod = nanosPerBackEndPeriod;
		this.nanosPerBackEndSample = nanosPerBackEndSample;
		this.nanosPerFrontEndPeriod = nanosPerFrontEndPeriod;
		this.sampleFramesPerFrontEndPeriod = sampleFramesPerFrontEndPeriod;
		this.nanosOutputLatency = nanosOutputLatency;
	}
	
	public void reset( MadTimingParameters from )
	{
		this.nanosPerBackEndPeriod = from.nanosPerBackEndPeriod;
		this.nanosPerBackEndSample = from.nanosPerBackEndSample;
		this.nanosPerFrontEndPeriod = from.nanosPerFrontEndPeriod;
		this.sampleFramesPerFrontEndPeriod = from.sampleFramesPerFrontEndPeriod;
		this.nanosOutputLatency = from.nanosOutputLatency;
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

	public long getNanosOutputLatency()
	{
		return nanosOutputLatency;
	}
	
	public void reset( long nanosPerBackEndPeriod,
			long nanosPerBackEndSample,
			long nanosPerFrontEndPeriod,
			int sampleFramesPerFrontEndPeriod,
			long nanosOutputLatency )
	{
		this.nanosPerBackEndPeriod = nanosPerBackEndPeriod;
		this.nanosPerBackEndSample = nanosPerBackEndSample;
		this.nanosPerFrontEndPeriod = nanosPerFrontEndPeriod;
		this.sampleFramesPerFrontEndPeriod = sampleFramesPerFrontEndPeriod;
		this.nanosOutputLatency = nanosOutputLatency;
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("NanosBEP: ");
		sb.append( nanosPerBackEndPeriod );
		sb.append(" NanosBES: ");
		sb.append( nanosPerBackEndSample );
		sb.append(" NanosFEP: ");
		sb.append( nanosPerFrontEndPeriod );
		sb.append(" SamplesFEP: ");
		sb.append( sampleFramesPerFrontEndPeriod );
		sb.append(" NanosOL: ");
		sb.append( nanosOutputLatency );
		
		return sb.toString();
	}

}
