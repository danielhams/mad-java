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

package uk.co.modularaudio.util.audio.pvoc.frame.processors.scaledlocking;

import uk.co.modularaudio.util.audio.pvoc.PvocParameters;

public class PvocPeaksWorkingBuffer
{
	public int[] outputRunningPeaks;
	public int numRunningPeaks = 0;
	
	public int[] outputStarterPeaks;
	public int numStarterPeaks = 0;
	
	public int[] outputBinCrossingPeaks;
	public int numBinCrossingPeaks = 0;
	
	public PvocPeaksWorkingBuffer( PvocParameters params )
	{
		int numBins = params.getNumBins();
		
		// A list of bin numbers ending with - 1 of continuous peaks
		outputRunningPeaks = new int[ numBins ];
		
		// Just a list of bin numbers ending with -1 of peaks just beginning
		outputStarterPeaks = new int[ numBins ];
	
		// A tuple of curPeakNum and oldPeakNum, ends with -1 in the curPeakNum
		outputBinCrossingPeaks = new int[ numBins * 2 ];
	}

}
