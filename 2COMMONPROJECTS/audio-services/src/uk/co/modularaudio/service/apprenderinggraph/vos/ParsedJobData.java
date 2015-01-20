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

package uk.co.modularaudio.service.apprenderinggraph.vos;

public class ParsedJobData
{
//	private long jobStartTimestamp = -1;
//	private long jobEndTimestamp = -1;
	private long jobOffsetFromStart = -1;
	private long jobLength = -1;
	private int jobThreadNum = -1;
	private String jobName = "";
	
	public ParsedJobData( long jst,
			long jet,
			long jofs,
			long jl,
			int jn,
			String jobName )
	{
//		this.jobStartTimestamp = jst;
//		this.jobEndTimestamp = jet;
		this.jobOffsetFromStart = jofs;
		this.jobLength = jl;
		this.jobThreadNum = jn;
		this.jobName = jobName;
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		String jobStr = String.format( "JobLength(%8d) JobThreadNum(%d) JobName(%s)", jobLength, jobThreadNum, jobName );
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
