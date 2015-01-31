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

public final class StandardBandLimitedWaveTables
{
	private static Log log = LogFactory.getLog( StandardBandLimitedWaveTables.class.getName() );

	private final String pathToCacheRoot;
	private final HashMap<OscillatorWaveShape, BandLimitedWaveTableMap> shapeToTableMap = new HashMap<OscillatorWaveShape, BandLimitedWaveTableMap>();

	private StandardBandLimitedWaveTables( final String pathToCacheRoot ) throws IOException
	{
		this.pathToCacheRoot = pathToCacheRoot;
	}

	private final static Lock INSTANCE_LOCK = new ReentrantLock();
	private final static AtomicReference<StandardBandLimitedWaveTables> PRIVATE_INSTANCE = new AtomicReference<StandardBandLimitedWaveTables>();

	public static StandardBandLimitedWaveTables getInstance( final String pathToCacheRoot ) throws IOException
	{
		StandardBandLimitedWaveTables retVal = null;
		try
		{
			INSTANCE_LOCK.lock();
			retVal = PRIVATE_INSTANCE.get();
			if( retVal == null )
			{
				retVal = new StandardBandLimitedWaveTables( pathToCacheRoot );
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

	public BandLimitedWaveTableMap getMapForShape( final OscillatorWaveShape waveShape ) throws NoWaveTableForShapeException
	{
		if( !shapeToTableMap.containsKey( waveShape ) )
		{
			try
			{
				switch( waveShape )
				{
					case SAW:
					{
						final SawRawWaveTableGenerator sawWaveTableGenerator = new SawRawWaveTableGenerator();
						final BandLimitedWaveTableMap sawWaveTableMap = new BandLimitedWaveTableMap( pathToCacheRoot,
								sawWaveTableGenerator,
								RawLookupTableDefines.OSCILLATOR_BUFFER_LENGTH );
						shapeToTableMap.put( OscillatorWaveShape.SAW, sawWaveTableMap );
						break;
					}
					case SQUARE:
					{
						final SquareRawWaveTableGenerator squareWaveTableGenerator = new SquareRawWaveTableGenerator();
						final BandLimitedWaveTableMap squareWaveTableMap = new BandLimitedWaveTableMap( pathToCacheRoot,
								squareWaveTableGenerator,
								RawLookupTableDefines.OSCILLATOR_BUFFER_LENGTH );
						shapeToTableMap.put( OscillatorWaveShape.SQUARE, squareWaveTableMap );
						break;
					}
					case TEST1:
					{
						final Test1RawWaveTableGenerator test1WaveTableGenerator = new Test1RawWaveTableGenerator();
						final BandLimitedWaveTableMap test1WaveTableMap = new BandLimitedWaveTableMap( pathToCacheRoot,
								test1WaveTableGenerator,
								RawLookupTableDefines.OSCILLATOR_BUFFER_LENGTH );
						shapeToTableMap.put( OscillatorWaveShape.TEST1, test1WaveTableMap );
						break;
					}
					case TRIANGLE:
					{
						final TriangleRawWaveTableGenerator triangleWaveTableGenerator = new TriangleRawWaveTableGenerator();
						final BandLimitedWaveTableMap triangleWaveTableMap = new BandLimitedWaveTableMap( pathToCacheRoot,
								triangleWaveTableGenerator,
								RawLookupTableDefines.OSCILLATOR_BUFFER_LENGTH );
						shapeToTableMap.put( OscillatorWaveShape.TRIANGLE, triangleWaveTableMap );
						break;
					}
					case JUNO:
					{
						final JunoRawWaveTableGenerator junoWaveTableGenerator = new JunoRawWaveTableGenerator();
						final BandLimitedWaveTableMap junoWaveTableMap = new BandLimitedWaveTableMap( pathToCacheRoot,
								junoWaveTableGenerator,
								RawLookupTableDefines.OSCILLATOR_BUFFER_LENGTH );
						shapeToTableMap.put( OscillatorWaveShape.JUNO, junoWaveTableMap );
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

		return shapeToTableMap.get( waveShape );
	}
}
