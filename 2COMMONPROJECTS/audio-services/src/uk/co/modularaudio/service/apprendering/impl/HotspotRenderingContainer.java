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
