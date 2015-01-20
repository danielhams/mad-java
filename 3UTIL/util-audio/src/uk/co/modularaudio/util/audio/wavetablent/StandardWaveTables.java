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

package uk.co.modularaudio.util.audio.wavetablent;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.wavetable.raw.RawWaveTableDefines;

public class StandardWaveTables
{
	private static Log log = LogFactory.getLog( StandardWaveTables.class.getName() );
	
	private String pathToCacheRoot;
	private HashMap<OscillatorWaveShape, CubicPaddedRawWaveTable> shapeToTableMap = new HashMap<OscillatorWaveShape, CubicPaddedRawWaveTable>();
	
	private StandardWaveTables( String pathToCacheRoot ) throws IOException
	{
		this.pathToCacheRoot = pathToCacheRoot;

	}
	
	public CubicPaddedRawWaveTable getTableForShape( OscillatorWaveShape shape ) throws NoWaveTableForShapeException
	{
		if( !shapeToTableMap.containsKey( shape ) )
		{
			try
			{
				switch( shape )
				{
					case SINE:
					{
						SineRawWaveTableGenerator sineWaveTableGenerator = new SineRawWaveTableGenerator();
						CubicPaddedRawWaveTable sineTable = sineWaveTableGenerator.readFromCacheOrGenerate( pathToCacheRoot,
								RawWaveTableDefines.OSCILLATOR_BUFFER_LENGTH,
								RawWaveTableDefines.OSCILLATOR_NUM_HARMONICS );
						shapeToTableMap.put( OscillatorWaveShape.SINE, sineTable );
						break;
					}
					case SAW:
					{
						SawRawWaveTableGenerator sawWaveTableGenerator = new SawRawWaveTableGenerator();
						CubicPaddedRawWaveTable sawTable = sawWaveTableGenerator.readFromCacheOrGenerate( pathToCacheRoot,
								RawWaveTableDefines.OSCILLATOR_BUFFER_LENGTH,
								RawWaveTableDefines.OSCILLATOR_NUM_HARMONICS );
						shapeToTableMap.put( OscillatorWaveShape.SAW, sawTable );
						break;
					}
					case SQUARE:
					{
						SquareRawWaveTableGenerator squareWaveTableGenerator = new SquareRawWaveTableGenerator();
						CubicPaddedRawWaveTable squareTable = squareWaveTableGenerator.readFromCacheOrGenerate( pathToCacheRoot,
								RawWaveTableDefines.OSCILLATOR_BUFFER_LENGTH,
								RawWaveTableDefines.OSCILLATOR_NUM_HARMONICS );
						shapeToTableMap.put( OscillatorWaveShape.SQUARE, squareTable );
						break;
					}
					case TEST1:
					{
						Test1RawWaveTableGenerator test1WaveTableGenerator = new Test1RawWaveTableGenerator();
						CubicPaddedRawWaveTable test1Table = test1WaveTableGenerator.readFromCacheOrGenerate( pathToCacheRoot,
								RawWaveTableDefines.OSCILLATOR_BUFFER_LENGTH,
								RawWaveTableDefines.OSCILLATOR_NUM_HARMONICS );
						shapeToTableMap.put( OscillatorWaveShape.TEST1, test1Table );
						break;
					}
					case TRIANGLE:
					{
						TriangleRawWaveTableGenerator triangleWaveTableGenerator = new TriangleRawWaveTableGenerator();
						CubicPaddedRawWaveTable triangleTable = triangleWaveTableGenerator.readFromCacheOrGenerate( pathToCacheRoot,
								RawWaveTableDefines.OSCILLATOR_BUFFER_LENGTH,
								RawWaveTableDefines.OSCILLATOR_NUM_HARMONICS );
						shapeToTableMap.put( OscillatorWaveShape.TRIANGLE, triangleTable );
						break;
					}
					case JUNO:
					{
						JunoRawWaveTableGenerator junoWaveTableGenerator = new JunoRawWaveTableGenerator();
						CubicPaddedRawWaveTable junoTable = junoWaveTableGenerator.readFromCacheOrGenerate( pathToCacheRoot,
								RawWaveTableDefines.OSCILLATOR_BUFFER_LENGTH,
								RawWaveTableDefines.OSCILLATOR_NUM_HARMONICS );
						shapeToTableMap.put( OscillatorWaveShape.JUNO, junoTable );
						break;
					}
					default:
					{
						throw new NoWaveTableForShapeException();
					}
				}
			}
			catch( IOException ioe )
			{
				throw new NoWaveTableForShapeException( ioe );
			}
		}

		return shapeToTableMap.get( shape );
	}

	private static Lock instanceLock = new ReentrantLock();
	private static AtomicReference<StandardWaveTables> privateInstance = new AtomicReference<StandardWaveTables>();
	
	public static StandardWaveTables getInstance( String pathToCacheRoot ) throws IOException
	{
		StandardWaveTables retVal = null;
		try
		{
			instanceLock.lock();
			retVal = privateInstance.get();
			if( retVal == null )
			{
				retVal = new StandardWaveTables( pathToCacheRoot );
				if( !privateInstance.compareAndSet( null,  retVal ) )
				{
					log.error( "Failed creating the singleton....");
				}
			}
		}
		finally
		{
			instanceLock.unlock();
		}

		return retVal;
	}
	
}
