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

package uk.co.modularaudio.mads.base.bandlimitedoscillator.mu;

import java.util.HashMap;
import java.util.Map;

import uk.co.modularaudio.util.audio.oscillatortable.NoWaveTableForShapeException;
import uk.co.modularaudio.util.audio.oscillatortable.Oscillator;
import uk.co.modularaudio.util.audio.oscillatortable.OscillatorFactory;
import uk.co.modularaudio.util.audio.oscillatortable.OscillatorFactoryException;
import uk.co.modularaudio.util.audio.oscillatortable.OscillatorInterpolationType;
import uk.co.modularaudio.util.audio.oscillatortable.OscillatorWaveShape;
import uk.co.modularaudio.util.audio.oscillatortable.OscillatorWaveTableType;

public class BandLimitedOscillatorInstances
{
	private final Map<OscillatorWaveShape, Oscillator> shapeToOscillatorMap = new HashMap<OscillatorWaveShape, Oscillator>();

	public BandLimitedOscillatorInstances( final OscillatorFactory of ) throws NoWaveTableForShapeException, OscillatorFactoryException
	{
		for( final OscillatorWaveShape waveShape : OscillatorWaveShape.values() )
		{
			final Oscillator osc = of.createOscillator( OscillatorWaveTableType.BAND_LIMITED, OscillatorInterpolationType.CUBIC, waveShape );
			shapeToOscillatorMap.put( waveShape, osc );
		}
	}

	public Oscillator getOscillator( final OscillatorWaveShape waveShape )
	{
		return shapeToOscillatorMap.get( waveShape );
	}
}
