package uk.co.modularaudio.mads.base.interptester.ui;

public interface PerfDataReceiver
{

	void setNoneNanos( long value );

	void setLNanos( long value );

	void setHHNanos( long value );

	void setSDNanos( long value );

	void setLPNanos( long value );

	void setSDDNanos( long value );

}
