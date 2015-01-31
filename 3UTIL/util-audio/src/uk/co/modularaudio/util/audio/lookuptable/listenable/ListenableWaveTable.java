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

package uk.co.modularaudio.util.audio.lookuptable.listenable;

import java.util.ArrayList;
import java.util.List;


public class ListenableWaveTable
{
	private static final long DEBUG_THREAD_SLEEP_MILLIS = 1;
	public final float[] floatBuffer;
	public final int length;

	private boolean debug = false;

	private final List<WaveTableListener> listeners = new ArrayList<WaveTableListener>();

	public ListenableWaveTable( final int capacity, final boolean initialiseToZero, final boolean debug )
	{
		this.length = capacity;
		this.debug = debug;
		floatBuffer = new float[ length ];
		fireTableChanged();
		if( initialiseToZero )
		{
			for( int i = 0 ; i < length ; i++ )
			{
				floatBuffer[i] = 0.0f;
			}
		}
		fireTableChanged();
	}

	public float getValueAt( final int position )
	{
		if( position >= length )
		{
			return floatBuffer[length - 1];
		}
		else
		{
			return floatBuffer[position];
		}
	}

	public void setValueAt( final int position, final float value )
	{
		floatBuffer[position] = value;
		fireTableChanged();
	}

	private void fireTableChanged()
	{
		for( final WaveTableListener l : listeners )
		{
			l.receiveTableChanged();
		}
		if( debug )
		{
			try
			{
				Thread.sleep( DEBUG_THREAD_SLEEP_MILLIS );
			}
			catch (final InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	public void addListener( final WaveTableListener l )
	{
		listeners.add( l );
	}

	public void removeListener( final WaveTableListener l )
	{
		listeners.remove( l );
	}
}
