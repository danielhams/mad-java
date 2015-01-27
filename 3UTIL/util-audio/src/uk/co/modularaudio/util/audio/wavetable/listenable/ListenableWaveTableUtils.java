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

package uk.co.modularaudio.util.audio.wavetable.listenable;

public class ListenableWaveTableUtils
{
	public static void normalise_table( final ListenableWaveTable table, final float scale )
	{
		int n;
		float max = 0.f;
		for(n=0; n < table.getLength(); n++)
		{
			final float tableN = table.getValueAt( n );
			max = tableN > max ? tableN : max;
		}

		for(n=0; n < table.getLength(); n++)
		{
			final float curVal = table.getValueAt( n );
			table.setValueAt( n, curVal / max  );
		}
	}
}
