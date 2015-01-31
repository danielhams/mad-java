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

package uk.co.modularaudio.mads.base.oscillator.mu;

import java.util.HashMap;
import java.util.Map;

import uk.co.modularaudio.util.audio.oscillatortable.NoWaveTableForShapeException;
import uk.co.modularaudio.util.audio.oscillatortable.Oscillator;
import uk.co.modularaudio.util.audio.oscillatortable.OscillatorFactory;
import uk.co.modularaudio.util.audio.oscillatortable.OscillatorFactoryException;
import uk.co.modularaudio.util.audio.oscillatortable.OscillatorInterpolationType;
import uk.co.modularaudio.util.audio.oscillatortable.OscillatorWaveShape;
import uk.co.modularaudio.util.audio.oscillatortable.OscillatorWaveTableType;

public class OscillatorInstances
{
//	private static Log log = LogFactory.getLog( OscillatorInstances.class.getName() );

	public enum WaveType
	{
		SINE,
		SQUARE,
		SAW,
		TRIANGLE,
		TEST1
	}

	private final Map<OscillatorWaveShape, Oscillator> typeToTableMap = new HashMap<OscillatorWaveShape, Oscillator>();

	public OscillatorInstances( final OscillatorFactory oscillatorFactory ) throws NoWaveTableForShapeException, OscillatorFactoryException
	{
		for( final OscillatorWaveShape waveShape : OscillatorWaveShape.values() )
		{
			typeToTableMap.put( waveShape,
					oscillatorFactory.createOscillator( OscillatorWaveTableType.SINGLE, OscillatorInterpolationType.LINEAR, waveShape ) );
		}
	}

	public Oscillator getOscillator( final OscillatorWaveShape waveShape )
	{
		return typeToTableMap.get( waveShape );
	}
}
