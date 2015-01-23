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


public class MadChannelPeriodData
{
//	private static Log log = LogFactory.getLog( MadChannelPeriodData.class.getName() );

	private long periodStartFrameTimes;
	private int numFramesThisPeriod;

	public MadChannelPeriodData()
	{
	}

	public void reset( final long periodStartFrameTime, final int numFramesThisPeriod )
	{
		this.periodStartFrameTimes = periodStartFrameTime;
		this.numFramesThisPeriod = numFramesThisPeriod;
	}

	public long getPeriodStartFrameTimes()
	{
		return periodStartFrameTimes;
	}

	public int getNumFramesThisPeriod()
	{
		return numFramesThisPeriod;
	}
}
