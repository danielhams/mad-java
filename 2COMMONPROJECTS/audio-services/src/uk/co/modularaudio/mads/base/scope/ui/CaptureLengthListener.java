package uk.co.modularaudio.mads.base.scope.ui;

public interface CaptureLengthListener
{

	void receiveCaptureLengthMillis( float captureMillis );
	void receiveCaptureLengthSamples( int captureSamples );
}
