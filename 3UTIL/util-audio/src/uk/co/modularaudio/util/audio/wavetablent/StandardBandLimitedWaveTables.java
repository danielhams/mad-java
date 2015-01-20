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

public class StandardBandLimitedWaveTables
{
	private static Log log = LogFactory.getLog( StandardBandLimitedWaveTables.class.getName() );
	
	private String pathToCacheRoot;
	private HashMap<OscillatorWaveShape, BandLimitedWaveTableMap> shapeToTableMap = new HashMap<OscillatorWaveShape, BandLimitedWaveTableMap>();

	private StandardBandLimitedWaveTables( String pathToCacheRoot ) throws IOException
	{
		this.pathToCacheRoot = pathToCacheRoot;
	}

	private static Lock instanceLock = new ReentrantLock();
	private static AtomicReference<StandardBandLimitedWaveTables> privateInstance = new AtomicReference<StandardBandLimitedWaveTables>();
	
	public static StandardBandLimitedWaveTables getInstance( String pathToCacheRoot ) throws IOException
	{
		StandardBandLimitedWaveTables retVal = null;
		try
		{
			instanceLock.lock();
			retVal = privateInstance.get();
			if( retVal == null )
			{
				retVal = new StandardBandLimitedWaveTables( pathToCacheRoot );
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

	public BandLimitedWaveTableMap getMapForShape( OscillatorWaveShape waveShape ) throws NoWaveTableForShapeException
	{
		if( !shapeToTableMap.containsKey( waveShape ) )
		{
			try
			{
				switch( waveShape )
				{
					case SAW:
					{
						SawRawWaveTableGenerator sawWaveTableGenerator = new SawRawWaveTableGenerator();
						BandLimitedWaveTableMap sawWaveTableMap = new BandLimitedWaveTableMap( pathToCacheRoot,
								sawWaveTableGenerator,
								RawWaveTableDefines.OSCILLATOR_BUFFER_LENGTH );
						shapeToTableMap.put( OscillatorWaveShape.SAW, sawWaveTableMap );
						break;
					}
					case SQUARE:
					{
						SquareRawWaveTableGenerator squareWaveTableGenerator = new SquareRawWaveTableGenerator();
						BandLimitedWaveTableMap squareWaveTableMap = new BandLimitedWaveTableMap( pathToCacheRoot,
								squareWaveTableGenerator,
								RawWaveTableDefines.OSCILLATOR_BUFFER_LENGTH );
						shapeToTableMap.put( OscillatorWaveShape.SQUARE, squareWaveTableMap );
						break;
					}
					case TEST1:
					{
						Test1RawWaveTableGenerator test1WaveTableGenerator = new Test1RawWaveTableGenerator();
						BandLimitedWaveTableMap test1WaveTableMap = new BandLimitedWaveTableMap( pathToCacheRoot,
								test1WaveTableGenerator,
								RawWaveTableDefines.OSCILLATOR_BUFFER_LENGTH );
						shapeToTableMap.put( OscillatorWaveShape.TEST1, test1WaveTableMap );
						break;
					}
					case TRIANGLE:
					{
						TriangleRawWaveTableGenerator triangleWaveTableGenerator = new TriangleRawWaveTableGenerator();
						BandLimitedWaveTableMap triangleWaveTableMap = new BandLimitedWaveTableMap( pathToCacheRoot,
								triangleWaveTableGenerator,
								RawWaveTableDefines.OSCILLATOR_BUFFER_LENGTH );
						shapeToTableMap.put( OscillatorWaveShape.TRIANGLE, triangleWaveTableMap );
						break;
					}
					case JUNO:
					{
						JunoRawWaveTableGenerator junoWaveTableGenerator = new JunoRawWaveTableGenerator();
						BandLimitedWaveTableMap junoWaveTableMap = new BandLimitedWaveTableMap( pathToCacheRoot,
								junoWaveTableGenerator,
								RawWaveTableDefines.OSCILLATOR_BUFFER_LENGTH );
						shapeToTableMap.put( OscillatorWaveShape.JUNO, junoWaveTableMap );
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

		return shapeToTableMap.get( waveShape );
	}
}
