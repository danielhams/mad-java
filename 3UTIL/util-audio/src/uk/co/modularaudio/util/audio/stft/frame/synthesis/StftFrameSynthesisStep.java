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

package uk.co.modularaudio.util.audio.stft.frame.synthesis;

public class StftFrameSynthesisStep
{
	private double synthesisStepSize = 0.0;
	private int synthesisRoundedStepSize = -1;
	private double synthesisFrac = 0.0;
	private double internalSpeedRatio = 0.0;
	private double internalPitchRatio = 0.0;
	private double desiredSpeed = 0.0;
	private double desiredPitch = 0.0;
	
	public StftFrameSynthesisStep()
	{
	}
	
	public void calculate( double speed, double pitch, int analysisStepSize )
	{
		desiredSpeed = speed;
		desiredPitch = pitch;
		
		internalSpeedRatio = desiredSpeed / desiredPitch;
		internalPitchRatio = (desiredSpeed / internalSpeedRatio );
		
		// Calculate the ratio from analysis to synthesis
//		double synthesisStepSize = (analysisStepSize + synthesisFrac) / internalSpeedRatio;
//		synthesisRoundedStepSize = (int)synthesisStepSize;
//		synthesisStepSize = (((double)analysisStepSize) / internalSpeedRatio) + synthesisFrac;
		synthesisStepSize = (((double)analysisStepSize) / internalSpeedRatio);
		synthesisRoundedStepSize = (int)synthesisStepSize;

		// Hold on to the synthesis frac and add it back it when necessary (samples are integer spaces apart,
		// but our output speed is a floating point number
		synthesisFrac = synthesisStepSize - synthesisRoundedStepSize;
		
	}

	public double getSynthesisStepSize()
	{
		return synthesisStepSize;
	}
	
	public int getRoundedStepSize()
	{
		return synthesisRoundedStepSize;
	}

	public double getFrac()
	{
		return synthesisFrac;
	}
	
	public String toString()
	{
		return( "SynthStep( speed=" + desiredSpeed + " pitch=" + desiredPitch + ")  =  ( internalSpeed=" + internalSpeedRatio + " internalPitch=" + internalPitchRatio + ")");
	}

	public double getInternalSpeedRatio()
	{
		return internalSpeedRatio;
	}

	public double getInternalPitchRatio()
	{
		return internalPitchRatio;
	}
}
