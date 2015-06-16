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

package uk.co.modularaudio.service.apprendering.util.structure;

public class ParsedJobData
{
	private final long jobOffsetFromStart;
	private final long jobLength;
	private final int jobThreadNum;
	private final String jobName;

	public ParsedJobData(
			final long offsetFromStart,
			final long length,
			final int threadNum,
			final String name )
	{
		this.jobOffsetFromStart = offsetFromStart;
		this.jobLength = length;
		this.jobThreadNum = threadNum;
		this.jobName = name;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		final String jobStr = String.format(
				"JobStart(%8d) JobLength(%8d) JobThreadNum(%d) JobName(%s)",
				jobOffsetFromStart,
				jobLength,
				jobThreadNum,
				jobName );
		sb.append( jobStr );

		return sb.toString();
	}

	public long getJobOffsetFromStart()
	{
		return jobOffsetFromStart;
	}

	public long getJobLength()
	{
		return jobLength;
	}

	public int getJobThreadNum()
	{
		return jobThreadNum;
	}

	public String getJobName()
	{
		return jobName;
	}
}
