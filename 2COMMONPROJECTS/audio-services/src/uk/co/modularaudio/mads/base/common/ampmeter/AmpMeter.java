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

package uk.co.modularaudio.mads.base.common.ampmeter;

public interface AmpMeter
{
	public static final int PREFERRED_WIDTH = 10;
	public static final int PREFERRED_METER_WIDTH = PREFERRED_WIDTH - 2;

	public static final float GREEN_THRESHOLD_DB = -6.0f;
	public static final float ORANGE_THRESHOLD_DB = -3.0f;

	void receiveMeterReadingInDb( final long currentTimestamp, final float meterReadingDb );

	void destroy();

	void receiveDisplayTick( long currentGuiTime );

	void setFramesBetweenPeakReset( int framesBetweenPeakReset );
}
