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

package uk.co.modularaudio.service.renderingplan;

import java.util.Set;

import uk.co.modularaudio.service.renderingplan.profiling.RenderingPlanProfileResults;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public interface RenderingPlan
{
	HardwareIOChannelSettings getPlanChannelSettings();
	MadTimingParameters getPlanTimingParameters();
	MadFrameTimeFactory getPlanFrameTimeFactory();

	RenderingJob[] getAllJobs();

	// Used to initially kick start audio rendering
	RenderingJob[] getInitialJobs();

	// Useful when dumping out a rendering plan so we don't iterate forever
	int getTotalNumJobs();

	// For performance of determining what to start/stop this needs to
	// be a set that has quick iteration and a contains method
	Set<MadInstance<?,?>> getAllInstances();

	boolean getPlanUsed();

	void resetPlanExecution();
	boolean wasPlanExecuted();

	void fillProfilingIfNotFilled(
			int numRenderingThreads,
			long clockCallbackStart,
			long clockCallbackPostProducer,
			long clockCallbackPostRpFetch,
			long clockCallbackPostLoop );

	boolean getProfileResultsIfFilled( RenderingPlanProfileResults destinationResults );

}
