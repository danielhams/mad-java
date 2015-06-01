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

package uk.co.modularaudio.util.audio.controlinterpolation;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.math.AudioMath;

public class SpringAndDamperInterpolator implements ControlValueInterpolator
{
	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog( SpringAndDamperInterpolator.class.getName() );

	// initial values
	public static final float FORCE_SCALE = 0.025f;
	public static final float DAMPING_FACTOR = 0.25f;
	public static final float INTEGRATION_TIMESTEP_FOR_48K = 0.03f;

	public static final float MIN_VALUE_DELTA_DB = -120.0f;
	public static final float MIN_VALUE_DELTA = 2 * AudioMath.MIN_FLOATING_POINT_24BIT_VAL_F;
	public static final float MIN_VELOCITY = 0.00001f;

	private class State
	{
		float x;
		float v;
	};

	private class Derivative
	{
		float dx;
		float dv;
	};

	private float deltaTimestep;

	private final State curState = new State();
	private final State evaluateState = new State();

	private final Derivative a = new Derivative();
	private final Derivative b = new Derivative();
	private final Derivative c = new Derivative();
	private final Derivative d = new Derivative();

	private final Derivative integrationDerivative = new Derivative();

	private float lowerBound;
	private float upperBound;

	private float desPos = 0.0f;

	public SpringAndDamperInterpolator( final float lowerBound, final float upperBound )
	{
		curState.x = 0.0f;
		curState.v = 0.0f;
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
		deltaTimestep = INTEGRATION_TIMESTEP_FOR_48K;
	}

	@Override
	public void resetLowerUpperBounds( final float lowerBound, final float upperBound )
	{
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}

	@Override
	public final void generateControlValues( final float[] output, final int outputIndex, final int length )
	{
		final int lastIndex = outputIndex + length;

		if( curState.v == 0.0 && curState.x == desPos )
		{
//			log.debug("Filling as steady state");
			Arrays.fill( output, outputIndex, lastIndex, curState.x );
		}
		else
		{
//			final float delta = desPos - curState.x;
//			log.debug("Performing integration desVal(" + desPos + ") delta(" + delta + ") - " + curState.x + " - " + curState.v );
			for( int curIndex = outputIndex ; curIndex < lastIndex ; ++curIndex )
			{
				integrate( curState, 0, deltaTimestep );
				if( curState.x > upperBound )
				{
					output[ curIndex ] = upperBound;
				}
				else if( curState.x < lowerBound )
				{
					output[ curIndex ] = lowerBound;
				}
				else
				{
					output[ curIndex ] = curState.x;
				}
			}
		}
	}

	@Override
	public final void notifyOfNewValue( final float value )
	{
		desPos = value;
	}

	@Override
	public final boolean checkForDenormal()
	{
		final float delta = desPos - curState.x;
		final float absX = (delta < 0.0f ? -delta : delta );
		final float absV = (curState.v < 0.0f ? -curState.v : curState.v );

		if( curState.x != desPos )
		{
//			final float deltaInDb = AudioMath.levelToDbF( absX );
//			log.debug("Not yet damped - pos(" + MathFormatter.slowFloatPrint( curState.x, 8, true ) +
//					") desPos(" + MathFormatter.slowFloatPrint( desPos, 8, true ) +
//					") delta(" + MathFormatter.slowFloatPrint( delta, 8, true ) +
//					") absV(" + MathFormatter.slowFloatPrint( absV, 8, true ) +
//					") deltaDb(" + MathFormatter.slowFloatPrint( deltaInDb, 8, true ) + ")");

			// Nudge by two bits towards desired value
			final int sigNum = (delta < 0 ? -2 : 2 );
			curState.x = curState.x + sigNum * AudioMath.MIN_FLOATING_POINT_24BIT_VAL_F;
		}

		if( absX <= MIN_VALUE_DELTA &&
				absV <= MIN_VELOCITY )
		{
			curState.x = desPos;
			curState.v = 0.0f;
//			log.debug("Damped to pos(" + MathFormatter.slowFloatPrint( desPos, 8, true ) +
//					") v=0");
			return true;
		}
		else
		{
			return false;
		}
//		else
//		{
//			if( absX > MIN_VALUE_DELTA )
//			{
//				log.debug( "Not damping due to delta" );
//			}
//			if( absV > MIN_VELOCITY )
//			{
//				log.debug( "Not damping due to vel" );
//			}
//		}

	}

	@Override
	public final void hardSetValue( final float value )
	{
		curState.x = value;
		curState.v = 0.0f;
	}

	private final void evaluate( final State initial,
			final float t,
			final float dt,
			final Derivative d,
			final Derivative o )
	{
		evaluateState.x = initial.x + d.dx*dt;
		evaluateState.v = initial.v + d.dv*dt;

		o.dx = evaluateState.v;
		o.dv = acceleration( evaluateState, t+dt );
//		log.debug("Acceleration is " + output.dv );
	}

	private final float acceleration( final State state,
			final float t )
	{
		final float k = FORCE_SCALE;
		final float b = DAMPING_FACTOR;
		final float posDiff = state.x - desPos;
//		log.debug("PosDiff is " + posDiff );
		return -k * posDiff - b*state.v;
	}

	private final void integrate( final State state,
			final float t,
			final float dt )
	{
		evaluate( state, t, 0.0f, integrationDerivative, a );
		evaluate( state, t, dt*0.5f, a, b );
		evaluate( state, t, dt*0.5f, b, c );
		evaluate( state, t, dt, c, d );

		final float dxdt = 1.0f / 6.0f *
				(a.dx + 2.0f*(b.dx + c.dx) + d.dx );
		final float dvdt = 1.0f / 6.0f *
				(a.dv + 2.0f*(b.dv + c.dv) + d.dv );

		state.x = (state.x + dxdt * dt);
		state.v = (state.v + dvdt * dt);
	}

	public final void reset( final int sampleRate )
	{
		deltaTimestep = (DataRate.SR_48000.getValue() * INTEGRATION_TIMESTEP_FOR_48K) / sampleRate;
	}
}
