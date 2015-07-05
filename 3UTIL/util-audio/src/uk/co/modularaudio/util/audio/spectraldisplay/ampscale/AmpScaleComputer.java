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

package uk.co.modularaudio.util.audio.spectraldisplay.ampscale;

public interface AmpScaleComputer
{
	public final static float APPROX_POLAR_AMP_SCALE_FACTOR = 510.5857f;

	float scaleIt(float valForBin);

	// Latest methods
	void setMinMaxDb( float minValueDb, float maxValueDb );
	int rawToMappedBucketMinMax( int numBuckets, float rawValue );
	float mappedBucketToRawMinMax( int numBuckets, int bucket );

	// Optimised
	void setParameters( int numBuckets, float minValueDb, float maxValueDb );
	int rawToMappedBucket( float rawValue );
	float mappedBucketToRaw( int bucket );
}
