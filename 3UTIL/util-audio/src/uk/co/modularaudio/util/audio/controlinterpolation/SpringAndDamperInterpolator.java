package uk.co.modularaudio.util.audio.controlinterpolation;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.format.DataRate;

public class SpringAndDamperInterpolator implements ControlValueInterpolator
{
	private static Log log = LogFactory.getLog( SpringAndDamperInterpolator.class.getName() );

	// initial values
//	private static final float FORCE_SCALE = 10.0f;
//	private static final float DAMPING_FACTOR = 1.0f;
//	private static final float INTEGRATION_TIMESTEP = 0.1f;

	// Kinda happy (but issues with settling)
	private static final float FORCE_SCALE = 0.1f;
	private static final float DAMPING_FACTOR = 0.5f;
	private static final float INTEGRATION_TIMESTEP_FOR_48K = 0.03f;

	private static final float MIN_VALUE_DELTA = 0.00001f;
	private static final float MIN_VELOCITY = 0.000001f;

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

	private final Derivative integrateDerivative = new Derivative();

	private float desPos = 0.0f;

	public SpringAndDamperInterpolator()
	{
		curState.x = 0.0f;
		curState.v = 0.0f;
		deltaTimestep = INTEGRATION_TIMESTEP_FOR_48K;
	}

	@Override
	public final void generateControlValues( final float[] output, final int outputIndex, final int length )
	{
		final int lastIndex = outputIndex + length;

		if( curState.v == 0.0f && curState.x == desPos )
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
				if( curState.x > 1.0f )
				{
					output[ curIndex ] = 1.0f;
				}
				else if( curState.x < -1.0f )
				{
					output[ curIndex ] = -1.0f;
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
	public final void checkForDenormal()
	{
		final float delta = desPos - curState.x;
		final float absX = (delta < 0.0f ? -delta : delta );
		final float absV = (curState.v < 0.0f ? -curState.v : curState.v );
//		if( absX < AudioMath.MIN_FLOATING_POINT_24BIT_VAL_F &&
//				absV < AudioMath.MIN_FLOATING_POINT_24BIT_VAL_F )
		if( absX < MIN_VALUE_DELTA &&
				absV < MIN_VELOCITY )
		{
			curState.x = desPos;
			curState.v = 0.0f;
//			log.debug("Damped to desired value due to no velocity and close to done");
		}
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
		evaluate( state, t, 0.0f, integrateDerivative, a );
		evaluate( state, t, dt*0.5f, a, b );
		evaluate( state, t, dt*0.5f, b, c );
		evaluate( state, t, dt, c, d );

		final float dxdt = 1.0f / 6.0f *
				(a.dx + 2.0f*(b.dx + c.dx) + d.dx );
		final float dvdt = 1.0f / 6.0f *
				(a.dv + 2.0f*(b.dv + c.dv) + d.dv );

		state.x = state.x + dxdt * dt;
		state.v = state.v + dvdt * dt;
	}

	public final void reset( final int sampleRate, final float valueChaseMillis )
	{
		deltaTimestep = (DataRate.SR_48000.getValue() * INTEGRATION_TIMESTEP_FOR_48K) / sampleRate;
	}
}
