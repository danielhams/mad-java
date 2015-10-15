package uk.co.modularaudio.util.audio.madnext;

public interface MadNextDefinition
{
	String getId();
	String getName();

	public MadNextChannelDefinition[] getChannelDefinitions();
}
