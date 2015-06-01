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

public class SpringAndDamperDoubleInterpolator implements ControlValueInterpolator
{
	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog( SpringAndDamperDoubleInterpolator.class.getName() );

	// Kinda happy (but issues with settling)
	public static final double FORCE_SCALE = 0.025;
	public static final double DAMPING_FACTOR = 0.25;
	public static final double INTEGRATION_TIMESTEP_FOR_48K = 0.03;

	public static final double MIN_VALUE_DELTA_DB = -120.0;
	public static final double MIN_VALUE_DELTA = AudioMath.dbToLevel( MIN_VALUE_DELTA_DB );
//	public static final double MIN_VALUE_DELTA = AudioMath.MIN_FLOATING_POINT_24BIT_VAL_D;
	public static final double MIN_VELOCITY = 0.00001;

	private class State
	{
		double x;
		double v;
	};

	private class Derivative
	{
		double dx;
		double dv;
	};

	private double deltaTimestep;

	private final State curState = new State();
	private final State evaluateState = new State();

	private final Derivative a = new Derivative();
	private final Derivative b = new Derivative();
	private final Derivative c = new Derivative();
	private final Derivative d = new Derivative();

	private final Derivative integrationDerivative = new Derivative();

	private float lowerBound;
	private float upperBound;

	private double desPos = 0.0f;

	public SpringAndDamperDoubleInterpolator( final float lowerBound, final float upperBound )
	{
		curState.x = 0.0;
		curState.v = 0.0;
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
			final float curStateFloat = (float)curState.x;
			Arrays.fill( output, outputIndex, lastIndex, curStateFloat );
		}
		else
		{
//			final float delta = desPos - curState.x;
//			log.debug("Performing integration desVal(" + desPos + ") delta(" + delta + ") - " + curState.x + " - " + curState.v );
			for( int curIndex = outputIndex ; curIndex < lastIndex ; ++curIndex )
			{
				integrate( curState, 0, deltaTimestep );
				final float curStateFloat = (float)curState.x;
				if( curStateFloat > upperBound )
				{
					output[ curIndex ] = upperBound;
				}
				else if( curStateFloat < lowerBound )
				{
					output[ curIndex ] = lowerBound;
				}
				else
				{
					output[ curIndex ] = curStateFloat;
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
		final double delta = desPos - curState.x;
		final double absX = (delta < 0.0 ? -delta : delta );
		final double absV = (curState.v < 0.0 ? -curState.v : curState.v );

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
			curState.x = curState.x + sigNum * AudioMath.MIN_FLOATING_POINT_24BIT_VAL_D;
		}

		if( absX <= MIN_VALUE_DELTA &&
				absV <= MIN_VELOCITY )
		{
			curState.x = desPos;
			curState.v = 0.0;
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
		curState.v = 0.0;
		desPos = value;
	}

	private final void evaluate( final State initial,
			final double t,
			final double dt,
			final Derivative d,
			final Derivative o )
	{
		evaluateState.x = initial.x + d.dx*dt;
		evaluateState.v = initial.v + d.dv*dt;

		o.dx = evaluateState.v;
		o.dv = acceleration( evaluateState, t+dt );
//		log.debug("Acceleration is " + output.dv );
	}

	private final double acceleration( final State state,
			final double t )
	{
		final double k = FORCE_SCALE;
		final double b = DAMPING_FACTOR;
		final double posDiff = state.x - desPos;
//		log.debug("PosDiff is " + posDiff );
		return -k * posDiff - b*state.v;
	}

	private final void integrate( final State state,
			final double t,
			final double dt )
	{
		evaluate( state, t, 0.0, integrationDerivative, a );
		evaluate( state, t, dt*0.5, a, b );
		evaluate( state, t, dt*0.5, b, c );
		evaluate( state, t, dt, c, d );

		final double dxdt = 1.0 / 6.0 *
				(a.dx + 2.0*(b.dx + c.dx) + d.dx );
		final double dvdt = 1.0 / 6.0 *
				(a.dv + 2.0*(b.dv + c.dv) + d.dv );

		state.x = (state.x + dxdt * dt);
		state.v = (state.v + dvdt * dt);
	}

	public final void reset( final int sampleRate )
	{
		deltaTimestep = (DataRate.SR_48000.getValue() * INTEGRATION_TIMESTEP_FOR_48K) / sampleRate;
	}
}
