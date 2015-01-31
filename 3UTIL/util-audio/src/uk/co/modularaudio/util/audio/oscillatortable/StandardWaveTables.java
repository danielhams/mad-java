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

package uk.co.modularaudio.util.audio.oscillatortable;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.lookuptable.raw.RawLookupTableDefines;

public final class StandardWaveTables
{
	private static Log log = LogFactory.getLog( StandardWaveTables.class.getName() );

	private final String pathToCacheRoot;
	private final HashMap<OscillatorWaveShape, CubicPaddedRawWaveTable> shapeToTableMap = new HashMap<OscillatorWaveShape, CubicPaddedRawWaveTable>();

	private StandardWaveTables( final String pathToCacheRoot ) throws IOException
	{
		this.pathToCacheRoot = pathToCacheRoot;

	}

	public CubicPaddedRawWaveTable getTableForShape( final OscillatorWaveShape shape ) throws NoWaveTableForShapeException
	{
		if( !shapeToTableMap.containsKey( shape ) )
		{
			try
			{
				switch( shape )
				{
					case SINE:
					{
						final SineRawWaveTableGenerator sineWaveTableGenerator = new SineRawWaveTableGenerator();
						final CubicPaddedRawWaveTable sineTable = sineWaveTableGenerator.readFromCacheOrGenerate( pathToCacheRoot,
								RawLookupTableDefines.OSCILLATOR_BUFFER_LENGTH,
								RawLookupTableDefines.OSCILLATOR_NUM_HARMONICS );
						shapeToTableMap.put( OscillatorWaveShape.SINE, sineTable );
						break;
					}
					case SAW:
					{
						final SawRawWaveTableGenerator sawWaveTableGenerator = new SawRawWaveTableGenerator();
						final CubicPaddedRawWaveTable sawTable = sawWaveTableGenerator.readFromCacheOrGenerate( pathToCacheRoot,
								RawLookupTableDefines.OSCILLATOR_BUFFER_LENGTH,
								RawLookupTableDefines.OSCILLATOR_NUM_HARMONICS );
						shapeToTableMap.put( OscillatorWaveShape.SAW, sawTable );
						break;
					}
					case SQUARE:
					{
						final SquareRawWaveTableGenerator squareWaveTableGenerator = new SquareRawWaveTableGenerator();
						final CubicPaddedRawWaveTable squareTable = squareWaveTableGenerator.readFromCacheOrGenerate( pathToCacheRoot,
								RawLookupTableDefines.OSCILLATOR_BUFFER_LENGTH,
								RawLookupTableDefines.OSCILLATOR_NUM_HARMONICS );
						shapeToTableMap.put( OscillatorWaveShape.SQUARE, squareTable );
						break;
					}
					case TEST1:
					{
						final Test1RawWaveTableGenerator test1WaveTableGenerator = new Test1RawWaveTableGenerator();
						final CubicPaddedRawWaveTable test1Table = test1WaveTableGenerator.readFromCacheOrGenerate( pathToCacheRoot,
								RawLookupTableDefines.OSCILLATOR_BUFFER_LENGTH,
								RawLookupTableDefines.OSCILLATOR_NUM_HARMONICS );
						shapeToTableMap.put( OscillatorWaveShape.TEST1, test1Table );
						break;
					}
					case TRIANGLE:
					{
						final TriangleRawWaveTableGenerator triangleWaveTableGenerator = new TriangleRawWaveTableGenerator();
						final CubicPaddedRawWaveTable triangleTable = triangleWaveTableGenerator.readFromCacheOrGenerate( pathToCacheRoot,
								RawLookupTableDefines.OSCILLATOR_BUFFER_LENGTH,
								RawLookupTableDefines.OSCILLATOR_NUM_HARMONICS );
						shapeToTableMap.put( OscillatorWaveShape.TRIANGLE, triangleTable );
						break;
					}
					case JUNO:
					{
						final JunoRawWaveTableGenerator junoWaveTableGenerator = new JunoRawWaveTableGenerator();
						final CubicPaddedRawWaveTable junoTable = junoWaveTableGenerator.readFromCacheOrGenerate( pathToCacheRoot,
								RawLookupTableDefines.OSCILLATOR_BUFFER_LENGTH,
								RawLookupTableDefines.OSCILLATOR_NUM_HARMONICS );
						shapeToTableMap.put( OscillatorWaveShape.JUNO, junoTable );
						break;
					}
					default:
					{
						throw new NoWaveTableForShapeException();
					}
				}
			}
			catch( final IOException ioe )
			{
				throw new NoWaveTableForShapeException( ioe );
			}
		}

		return shapeToTableMap.get( shape );
	}

	private final static Lock INSTANCE_LOCK = new ReentrantLock();
	private final static AtomicReference<StandardWaveTables> PRIVATE_INSTANCE = new AtomicReference<StandardWaveTables>();

	public static StandardWaveTables getInstance( final String pathToCacheRoot ) throws IOException
	{
		StandardWaveTables retVal = null;
		try
		{
			INSTANCE_LOCK.lock();
			retVal = PRIVATE_INSTANCE.get();
			if( retVal == null )
			{
				retVal = new StandardWaveTables( pathToCacheRoot );
				if( !PRIVATE_INSTANCE.compareAndSet( null,  retVal ) )
				{
					log.error( "Failed creating the singleton....");
				}
			}
		}
		finally
		{
			INSTANCE_LOCK.unlock();
		}

		return retVal;
	}

}
