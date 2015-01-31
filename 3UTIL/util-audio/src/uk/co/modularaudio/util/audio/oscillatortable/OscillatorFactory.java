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
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.oscillatortable.pulsewidth.ExpCurvePulseWidthMapper;


public final class OscillatorFactory
{
	private static Log log = LogFactory.getLog( OscillatorFactory.class.getName() );

	private final StandardWaveTables standardWaveTables;
	private final StandardBandLimitedWaveTables standardBandLimitedWaveTables;

	private OscillatorFactory( final String pathToCacheRoot ) throws IOException
	{
		standardWaveTables = StandardWaveTables.getInstance( pathToCacheRoot );
		standardBandLimitedWaveTables = StandardBandLimitedWaveTables.getInstance( pathToCacheRoot );
	}

	public Oscillator createOscillator( final OscillatorWaveTableType waveTableType,
			final OscillatorInterpolationType interpolationType,
			final OscillatorWaveShape waveShape )
		throws NoWaveTableForShapeException, OscillatorFactoryException
	{
		WaveTableValueFetcher valueFetcher;
		switch( interpolationType )
		{
			case TRUNCATING:
			{
				valueFetcher = new TruncatingWaveTableValueFetcher();
				break;
			}
			case LINEAR:
			{
				valueFetcher = new LinearInterpolatingWaveTableValueFetcher();
				break;
			}
			case CUBIC:
			{
				valueFetcher = new CubicInterpolatingWaveTableValueFetcher();
				break;
			}
			default:
			{
				final String msg = "Unknown interpolation type: " + interpolationType;
				log.error( msg );
				throw new OscillatorFactoryException( msg );
			}
		}

//		PulseWidthMapper pulseWidthMapper = new HardKneePulseWidthMapper();
		final PulseWidthMapper pulseWidthMapper = new ExpCurvePulseWidthMapper();

		Oscillator retVal;

		if( waveShape == OscillatorWaveShape.SINE )
		{
			// Sine doesn't have harmonics, so always use the single variant.
			final CubicPaddedRawWaveTable waveTable = standardWaveTables.getTableForShape( waveShape );
			retVal = new SingleWaveTableOscillator( waveTable, valueFetcher, pulseWidthMapper );
		}
		else
		{
			switch( waveTableType )
			{
				case SINGLE:
				{
					final CubicPaddedRawWaveTable waveTable = standardWaveTables.getTableForShape( waveShape );
					retVal = new SingleWaveTableOscillator( waveTable, valueFetcher, pulseWidthMapper );
					break;
				}
				case BAND_LIMITED:
				{
					final BandLimitedWaveTableMap waveTableMap = standardBandLimitedWaveTables.getMapForShape( waveShape );
					retVal = new BandLimitedWaveTableOscillator( waveTableMap, valueFetcher, pulseWidthMapper );
					break;
				}
				default:
				{
					final String msg = "Unknown wave table type: " + waveTableType;
					log.error( msg );
					throw new OscillatorFactoryException( msg );
				}
			}
		}

		return retVal;
	}

	private final static Lock INSTANCE_LOCK = new ReentrantLock();
	private final static AtomicReference<OscillatorFactory> PRIVATE_INSTANCE = new AtomicReference<OscillatorFactory>();

	public static OscillatorFactory getInstance( final String pathToCacheRoot ) throws IOException
	{
		OscillatorFactory retVal = PRIVATE_INSTANCE.get();
		if( retVal == null )
		{
			try
			{
				INSTANCE_LOCK.lock();
				retVal = PRIVATE_INSTANCE.get();
				if( retVal == null )
				{
					retVal = new OscillatorFactory( pathToCacheRoot );
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
		}

		return retVal;
	}
}
