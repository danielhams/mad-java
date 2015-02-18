package uk.co.modularaudio.mads.base.controlprocessingtester.util;

public interface ControlValueInterpolator
{
	void generateControlValues( float[] output,
			int outputIndex,
			int length );

	void notifyOfNewIncomingAmp( float amp );

	void checkForDenormal();
}
