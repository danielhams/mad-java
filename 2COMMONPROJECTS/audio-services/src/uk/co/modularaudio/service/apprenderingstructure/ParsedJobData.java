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

package uk.co.modularaudio.service.apprenderingstructure;

public class ParsedJobData
{
	private final long jobOffsetFromStart;
	private final long jobLength;
	private final int jobThreadNum;
	private final String jobName;

	public ParsedJobData( final long jst,
			final long jet,
			final long jofs,
			final long jl,
			final int jn,
			final String jobName )
	{
//		this.jobStartTimestamp = jst;
//		this.jobEndTimestamp = jet;
		this.jobOffsetFromStart = jofs;
		this.jobLength = jl;
		this.jobThreadNum = jn;
		this.jobName = jobName;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		final String jobStr = String.format( "JobLength(%8d) JobThreadNum(%d) JobName(%s)", jobLength, jobThreadNum, jobName );
		sb.append( jobStr );
//		sb.append("JobOffsetFromStart(" );
//		sb.append( jobOffsetFromStart );
//		sb.append( ") JobLength(" );
//		sb.append( jobLength );
//		sb.append( ") JobThreadNum(" );
//		sb.append( jobThreadNum );
//		sb.append( ") JobName(" );
//		sb.append( jobName );
//		sb.append( ")" );
		return sb.toString();
	}

	public long getJobOffsetFromStart()
	{
		return jobOffsetFromStart;
	}

}
