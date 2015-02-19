package test.uk.co.modularaudio.util.audio.controlinterpolation;

import uk.co.modularaudio.util.math.MathFormatter;

public class TestEvent
{
	private final int offsetInSamples;
	private final float eventValue;
	public TestEvent( final int offsetInSamples, final float eventValue )
	{
		this.offsetInSamples = offsetInSamples;
		this.eventValue = eventValue;
	}
	public int getOffsetInSamples()
	{
		return offsetInSamples;
	}
	public float getEventValue()
	{
		return eventValue;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder("Offset(");
		sb.append( offsetInSamples );
		sb.append( ") Value(" );
		sb.append( MathFormatter.slowFloatPrint( eventValue, 4, false ) );
		sb.append( ")" );
		return sb.toString();
	}
}
