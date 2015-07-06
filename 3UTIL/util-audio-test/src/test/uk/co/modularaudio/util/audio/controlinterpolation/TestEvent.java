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

package test.uk.co.modularaudio.util.audio.controlinterpolation;

import uk.co.modularaudio.util.math.MathFormatter;

public class TestEvent
{
	private final int offsetInSamples;
	private float eventValue;
	public TestEvent( final int offsetInSamples, final float eventValue )
	{
		this.offsetInSamples = offsetInSamples;
		this.eventValue = eventValue;
	}
	public int getOffsetInSamples()
	{
		return offsetInSamples;
	}
	public float getEventValue()
	{
		return eventValue;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder("Offset(");
		sb.append( offsetInSamples );
		sb.append( ") Value(" );
		sb.append( MathFormatter.fastFloatPrint( eventValue, 4, false ) );
		sb.append( ")" );
		return sb.toString();
	}

	public void setEventValue( final float newValue )
	{
		eventValue = newValue;
	}
}
