package uk.co.modularaudio.util.audio.madnext;

public interface MadNextInstance
{
	MadNextDefinition getDefinition();

	// Lifecycle
	void start() throws MadNextException;
	void stop() throws MadNextException;

	// Realtime methods
	int backEndProcess();

	// Non realtime methods
	int frontEndProcess();
}
