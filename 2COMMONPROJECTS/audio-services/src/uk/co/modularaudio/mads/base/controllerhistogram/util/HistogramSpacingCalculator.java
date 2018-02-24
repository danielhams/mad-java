/**
 *
 * Copyright (C) 2015 -> 2018 - Daniel Hams, Modular Audio Limited
 *                              daniel.hams@gmail.com
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

package uk.co.modularaudio.mads.base.controllerhistogram.util;

public class HistogramSpacingCalculator
{
	public static int calculateEventMarkerSpacing( final int height )
	{
		// Work out rounded num pixels per marker so we have integer number
		// and don't exceed the specified height.
		final int roundedNum = (int)Math.floor(
				(height-HistogramDisplay.MARKER_PADDING) /
				(HistogramDisplay.NUM_EVENT_MARKERS-1) );

		return roundedNum;
	}

	public static int calculateBucketMarkerSpacing( final int width )
	{
		final int roundedNum = (int)Math.floor(
				(width-HistogramDisplay.MARKER_PADDING) /
				(HistogramDisplay.NUM_BUCKET_MARKERS-1) );
		return roundedNum;
	}
}
