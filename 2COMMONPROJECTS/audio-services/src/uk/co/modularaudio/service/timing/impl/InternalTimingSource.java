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

package uk.co.modularaudio.service.timing.impl;

import uk.co.modularaudio.util.audio.mad.timing.MadChannelPeriodData;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingSource;

public class InternalTimingSource implements MadTimingSource
{
	private final MadChannelPeriodData periodData = new MadChannelPeriodData();
	private final MadTimingParameters timingParameters = new MadTimingParameters( 0, 0, 0, 0, 0 );

	public InternalTimingSource()
	{
	}

	@Override
	public MadChannelPeriodData getTimingPeriodData()
	{
		return periodData;
	}

	@Override
	public MadTimingParameters getTimingParameters()
	{
		return timingParameters;
	}
}
