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

package uk.co.modularaudio.mads.base.envelope.mu;

public class EnvelopeDefaults
{
	public static final boolean ATTACK_FROM_ZERO = false;
	
	public static final float ATTACK_MILLIS = 2.0f;
	public static final EnvelopeWaveChoice ATTACK_WAVE_CHOICE = EnvelopeWaveChoice.LINEAR;
	
	public static final float DECAY_MILLIS = 4.0f;
	public static final EnvelopeWaveChoice DECAY_WAVE_CHOICE = EnvelopeWaveChoice.LINEAR;
	
	public static final float SUSTAIN_LEVEL = 0.5f;
	
	public static final float RELEASE_MILLIS = 10.0f;
	public static final EnvelopeWaveChoice RELEASE_WAVE_CHOICE = EnvelopeWaveChoice.LINEAR;
	
	public static final float TIMESCALE_MILLIS = 100.0f;
	
	public static final float MAX_TIMESCALE_MILLIS = 5000.0f;
	
}
