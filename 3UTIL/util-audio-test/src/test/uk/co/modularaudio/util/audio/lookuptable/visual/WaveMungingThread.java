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

package test.uk.co.modularaudio.util.audio.lookuptable.visual;

import uk.co.modularaudio.util.audio.lookuptable.listenable.ListenableWaveTable;
import uk.co.modularaudio.util.audio.lookuptable.listenable.ListenableWaveTableFourierGenerator;

public class WaveMungingThread extends Thread
{
	private final WaveSurface harmonicsSurface;
	private final WaveSurface waveSurface;

	private static final int TABLE_LENGTH = 512;

	public WaveMungingThread( final WaveSurface harmonicsSurface, final WaveSurface waveSurface )
	{
		this.harmonicsSurface = harmonicsSurface;
		this.waveSurface = waveSurface;
	}

	@Override
	public void run()
	{
		final int numHarms = 336;
		final boolean useAmps = true;
//		generateSaw( numHarms, useAmps );
		generateSquare( numHarms, useAmps );
//		generateTriangle( numHarms, useAmps );
	}

	public void generateSaw( final int numHarms, final boolean useAmps )
	{
		final ListenableWaveTable lwt = new ListenableWaveTable( TABLE_LENGTH + 2, true, true );
		waveSurface.setWaveTable( lwt );

		final ListenableWaveTable harmonics = new ListenableWaveTable( numHarms, true, true );
		harmonicsSurface.setWaveTable( harmonics );
		for( int i = 0 ; i < numHarms ; i++ )
		{
			harmonics.setValueAt( i, 1.0f / (i + 1 ) );
		}

		if( useAmps )
		{
			ListenableWaveTableFourierGenerator.fillTable( lwt, numHarms, harmonics, -0.25f);
		}
		else
		{
			ListenableWaveTableFourierGenerator.fillTable( lwt, numHarms, null, -0.25f);
		}
	}

	public void generateSquare( final int numHarms, final boolean useAmps )
	{
		final ListenableWaveTable lwt = new ListenableWaveTable( TABLE_LENGTH + 2, true, true );
		waveSurface.setWaveTable( lwt );

		final ListenableWaveTable harmonics = new ListenableWaveTable( numHarms, true, true );
		harmonicsSurface.setWaveTable( harmonics );
		for( int i = 0 ; i < numHarms ; i+=2 )
		{
			harmonics.setValueAt( i, 1.0f / (i + 1) );
		}

		if( useAmps )
		{
			ListenableWaveTableFourierGenerator.fillTable( lwt, numHarms, harmonics, -0.25f);
		}
		else
		{
			ListenableWaveTableFourierGenerator.fillTable( lwt, numHarms, null, -0.25f);
		}
	}

	public void generateTriangle( final int numHarms, final boolean useAmps )
	{
		final ListenableWaveTable lwt = new ListenableWaveTable( TABLE_LENGTH + 2, true, true );
		waveSurface.setWaveTable( lwt );

		final ListenableWaveTable harmonicAmps = new ListenableWaveTable( numHarms, true, true );
		harmonicsSurface.setWaveTable( harmonicAmps );
		for( int i = 0 ; i < numHarms ; i+=2 )
		{
			harmonicAmps.setValueAt( i, 1.0f / ( (i + 1) * (i + 1)) );
		}

		if( useAmps )
		{
			ListenableWaveTableFourierGenerator.fillTable( lwt, numHarms, harmonicAmps, 0.0f);
		}
		else
		{
			ListenableWaveTableFourierGenerator.fillTable( lwt, numHarms, null, 0.0f);
		}
	}
}
