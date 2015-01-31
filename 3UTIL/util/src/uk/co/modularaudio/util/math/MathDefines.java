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

package uk.co.modularaudio.util.math;

public class MathDefines
{
	public final static double ONE_PI_D = Math.PI;
	public final static double HALF_PI_D = ONE_PI_D / 2.0;
	public final static double TWO_PI_D = Math.PI * 2;
	public final static double FOUR_PI_D = Math.PI * 4;
	public final static float HALF_PI_F = (float)HALF_PI_D;
	public final static float ONE_PI_F = (float)ONE_PI_D;
	public final static float TWO_PI_F = (float)TWO_PI_D;
	public final static float FOUR_PI_F = (float)FOUR_PI_D;

	public final static double SQRT_TWO_D = Math.sqrt( 2.0 );
	public final static float SQRT_TWO_F = (float)SQRT_TWO_D;

	public static final double LOG_2_D = Math.log( 2.0 );
	public static final float LOG_2_F = (float)LOG_2_D;

	public static final float MIN_AUDIO_FLOAT_AMP = Float.MIN_VALUE * 5000;
}
