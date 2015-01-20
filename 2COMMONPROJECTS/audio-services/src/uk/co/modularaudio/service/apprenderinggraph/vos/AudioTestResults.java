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

import org.apache.commons.logging.Log;

public class AudioTestResults
{
//	private static Log log = LogFactory.getLog( AudioTestResults.class.getName() );
	
	public long numSoftOverflows = 0;
	public long numSoftUnderflows = 0;
	public long numHardOverflows = 0;
	public long numHardUnderflows = 0;
	public boolean fatalException = false;
	public long numPeriodsRecorded = 0;

	public AudioTestResults()
	{
	}

	public void fillIn( long numSoftOverflows,
			long numSoftUnderflows,
			long numHardOverflows,
			long numHardUnderflows,
			boolean fatalException,
			long numPeriodsRecorded )
	{
		this.numSoftOverflows = numSoftOverflows;
		this.numSoftUnderflows = numSoftUnderflows;
		this.numHardOverflows = numHardOverflows;
		this.numHardUnderflows = numHardUnderflows;
		this.fatalException = fatalException;
		this.numPeriodsRecorded  = numPeriodsRecorded;
	}

	public void logResults( Log log )
	{
		// Only interested in the hardware underflows / overflows
		long totalErrors = numHardOverflows + numHardUnderflows;
		float percentageErrors = 0.0f;
		if( numPeriodsRecorded > 0 )
		{
			percentageErrors = (float)totalErrors / (float)numPeriodsRecorded;
		}
		log.info( "Test had " +
				numSoftOverflows + " soft overflows and " +
				numSoftUnderflows + " soft underflows for " +
				numHardOverflows + " hard overflows and " +
				numHardUnderflows + " hard underflows for " +
				numPeriodsRecorded + " periods recorded");
		log.info("This is " + percentageErrors + " percent hard errors");
	}

	public boolean isSuccessfull()
	{
		boolean success = true;

		// Only interested in the hardware underflows / overflows
		long totalErrors = numHardOverflows + numHardUnderflows;
		float percentageErrors = 0.0f;
		if( numPeriodsRecorded == 0 )
		{
			fatalException = true;
		}
		else
		{
			percentageErrors = (float)totalErrors / (float)numPeriodsRecorded;
		}
		if( fatalException || percentageErrors > 0.4f )
		{
			success = false;
		}

		return success;
	}
}
