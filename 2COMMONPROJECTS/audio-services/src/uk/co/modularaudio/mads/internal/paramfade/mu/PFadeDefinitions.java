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

package uk.co.modularaudio.mads.internal.paramfade.mu;

import java.util.HashSet;
import java.util.Set;

import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;

public class PFadeDefinitions
{
	public final static long FADE_200_MILLIS = 200;
	public final static int FADE_100_MILLIS = 100;
	public final static int FADE_10_MILLIS = 10;
	public final static int FADE_5_MILLIS = 5;
	public final static int FADE_2_MILLIS = 2;
	public final static int FADE_1_MILLIS = 1;

	public final static int FADE_MILLIS = FADE_10_MILLIS;

	protected static final Set<MadParameterDefinition> PARAM_DEFS;
	public static final MadParameterDefinition NUM_CHANNELS_PARAMETER = new MadParameterDefinition("numchannels", "Num Channels");

	static
	{
		PARAM_DEFS = new HashSet<MadParameterDefinition>();
		PARAM_DEFS.add( NUM_CHANNELS_PARAMETER );
	}
}
