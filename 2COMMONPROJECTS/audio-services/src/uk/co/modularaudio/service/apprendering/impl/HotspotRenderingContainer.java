package uk.co.modularaudio.service.apprendering.impl;

/**
 * <p>A simple container that when passed a rendering plan will
 * loop around with a dedicated thread executing the dsp
 * components in the plan.</p>
 * <p>Not perfect, but will exercise some of the codepaths
 * used by the engine meaning less of an impact when actually
 * run on a realtime thread.</p>
 * @author dan
 */
public interface HotspotRenderingContainer
{
	/**
	 * <p>Launch a thread and begin executing the specified rendering plan.</p>
	 * <p>The sibling stopHotspotLooping method <b>must</b> be called to clean
	 * up the thread and resources used during the looping.</p>
	 */
	void startHotspotLooping();

	/**
	 * <p>Halt the hotspot looping previously begun with the sibling method.</p>
	 */
	void stopHotspotLooping();
}
