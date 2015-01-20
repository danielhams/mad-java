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

package uk.co.modularaudio.util.audio.mad.ioqueue;

public class ThreadSpecificTemporaryEventStorage
{
	public int numCommandEventsToInstance = 0;
	public IOQueueEvent[] commandEventsToInstance;
	public int numTemporalEventsToInstance = 0;
	public IOQueueEvent[] temporalEventsToInstance;
	public int numCommandEventsToUi = 0;
	public IOQueueEvent[] commandEventsToUi;
	public int numTemporalEventsToUi = 0;
	public IOQueueEvent[] temporalEventsToUi;
	
	// 512K of floats
	public final static int TEMP_FLOAT_ARRAY_LENGTH = 512 * 1024;
	
	public float[] temporaryFloatArray;
	
	public ThreadSpecificTemporaryEventStorage( int storageSizePerArray )
	{
		commandEventsToInstance = new IOQueueEvent[ storageSizePerArray ];
		allocate( commandEventsToInstance );
		temporalEventsToInstance = new IOQueueEvent[ storageSizePerArray ];
		allocate( temporalEventsToInstance );
		commandEventsToUi = new IOQueueEvent[ storageSizePerArray ];
		allocate( commandEventsToUi );
		temporalEventsToUi = new IOQueueEvent[ storageSizePerArray ];
		allocate( temporalEventsToUi );
		
		temporaryFloatArray = new float[ TEMP_FLOAT_ARRAY_LENGTH ];
	}

	private void allocate( IOQueueEvent[] eventsToAllocate )
	{
		for( int i = 0 ; i < eventsToAllocate.length ; i++) 
		{
			eventsToAllocate[ i ] = new IOQueueEvent();
		}
	}
	
	public void resetEventsToInstance()
	{
		numCommandEventsToInstance = 0;
		numTemporalEventsToInstance = 0;
	}
	
	public void resetEventsToUi()
	{
		numCommandEventsToUi = 0;
		numTemporalEventsToUi = 0;
	}
}
