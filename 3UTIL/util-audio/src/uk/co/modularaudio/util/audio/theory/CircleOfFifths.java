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

package uk.co.modularaudio.util.audio.theory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import uk.co.modularaudio.util.audio.theory.MusicalKey.AllKeysEnum;

public class CircleOfFifths
{
	public enum Segment
	{
		V01A,
		V01B,
		V02A,
		V02B,
		V03A,
		V03B,
		V04A,
		V04B,
		V05A,
		V05B,
		V06A,
		V06B,
		V07A,
		V07B,
		V08A,
		V08B,
		V09A,
		V09B,
		V10A,
		V10B,
		V11A,
		V11B,
		V12A,
		V12B
	};

	public final static Map<MusicalKey, CircleOfFifths.Segment> KEY_TO_SEG_MAP = new HashMap<MusicalKey, CircleOfFifths.Segment>();
	public final static Map<CircleOfFifths.Segment, MusicalKey> SEG_TO_KEY_MAP = new HashMap<CircleOfFifths.Segment, MusicalKey>();;

	public final static Map<Segment, String> SEG_TO_STR_MAP = new HashMap<Segment,String>();

	static
	{
		KEY_TO_SEG_MAP.put( MusicalKey.KEY_TO_MUSICAL_KEY_MAP.get( AllKeysEnum.GSHARP_AFLAT_MINOR), CircleOfFifths.Segment.V01A );
		KEY_TO_SEG_MAP.put( MusicalKey.KEY_TO_MUSICAL_KEY_MAP.get( AllKeysEnum.B_CFLAT_MAJOR), CircleOfFifths.Segment.V01B );
		KEY_TO_SEG_MAP.put( MusicalKey.KEY_TO_MUSICAL_KEY_MAP.get( AllKeysEnum.DSHARP_EFLAT_MINOR), CircleOfFifths.Segment.V02A );
		KEY_TO_SEG_MAP.put( MusicalKey.KEY_TO_MUSICAL_KEY_MAP.get( AllKeysEnum.FSHARP_GFLAT_MAJOR), CircleOfFifths.Segment.V02B );
		KEY_TO_SEG_MAP.put( MusicalKey.KEY_TO_MUSICAL_KEY_MAP.get( AllKeysEnum.ASHARP_BFLAT_MINOR), CircleOfFifths.Segment.V03A );
		KEY_TO_SEG_MAP.put( MusicalKey.KEY_TO_MUSICAL_KEY_MAP.get( AllKeysEnum.CSHARP_DFLAT_MAJOR), CircleOfFifths.Segment.V03B );
		KEY_TO_SEG_MAP.put( MusicalKey.KEY_TO_MUSICAL_KEY_MAP.get( AllKeysEnum.ESHARP_F_MINOR), CircleOfFifths.Segment.V04A );
		KEY_TO_SEG_MAP.put( MusicalKey.KEY_TO_MUSICAL_KEY_MAP.get( AllKeysEnum.GSHARP_AFLAT_MAJOR), CircleOfFifths.Segment.V04B );
		KEY_TO_SEG_MAP.put( MusicalKey.KEY_TO_MUSICAL_KEY_MAP.get( AllKeysEnum.C_MINOR), CircleOfFifths.Segment.V05A );
		KEY_TO_SEG_MAP.put( MusicalKey.KEY_TO_MUSICAL_KEY_MAP.get( AllKeysEnum.DSHARP_EFLAT_MAJOR), CircleOfFifths.Segment.V05B );
		KEY_TO_SEG_MAP.put( MusicalKey.KEY_TO_MUSICAL_KEY_MAP.get( AllKeysEnum.G_MINOR), CircleOfFifths.Segment.V06A );
		KEY_TO_SEG_MAP.put( MusicalKey.KEY_TO_MUSICAL_KEY_MAP.get( AllKeysEnum.ASHARP_BFLAT_MAJOR), CircleOfFifths.Segment.V06B );
		KEY_TO_SEG_MAP.put( MusicalKey.KEY_TO_MUSICAL_KEY_MAP.get( AllKeysEnum.D_MINOR), CircleOfFifths.Segment.V07A );
		KEY_TO_SEG_MAP.put( MusicalKey.KEY_TO_MUSICAL_KEY_MAP.get( AllKeysEnum.ESHARP_F_MAJOR), CircleOfFifths.Segment.V07B );
		KEY_TO_SEG_MAP.put( MusicalKey.KEY_TO_MUSICAL_KEY_MAP.get( AllKeysEnum.A_MINOR), CircleOfFifths.Segment.V08A );
		KEY_TO_SEG_MAP.put( MusicalKey.KEY_TO_MUSICAL_KEY_MAP.get( AllKeysEnum.C_MAJOR), CircleOfFifths.Segment.V08B );
		KEY_TO_SEG_MAP.put( MusicalKey.KEY_TO_MUSICAL_KEY_MAP.get( AllKeysEnum.E_MINOR), CircleOfFifths.Segment.V09A );
		KEY_TO_SEG_MAP.put( MusicalKey.KEY_TO_MUSICAL_KEY_MAP.get( AllKeysEnum.G_MAJOR), CircleOfFifths.Segment.V09B );
		KEY_TO_SEG_MAP.put( MusicalKey.KEY_TO_MUSICAL_KEY_MAP.get( AllKeysEnum.B_CFLAT_MINOR), CircleOfFifths.Segment.V10A );
		KEY_TO_SEG_MAP.put( MusicalKey.KEY_TO_MUSICAL_KEY_MAP.get( AllKeysEnum.D_MAJOR), CircleOfFifths.Segment.V10B );
		KEY_TO_SEG_MAP.put( MusicalKey.KEY_TO_MUSICAL_KEY_MAP.get( AllKeysEnum.FSHARP_GFLAT_MINOR), CircleOfFifths.Segment.V11A );
		KEY_TO_SEG_MAP.put( MusicalKey.KEY_TO_MUSICAL_KEY_MAP.get( AllKeysEnum.A_MAJOR), CircleOfFifths.Segment.V11B );
		KEY_TO_SEG_MAP.put( MusicalKey.KEY_TO_MUSICAL_KEY_MAP.get( AllKeysEnum.CSHARP_DFLAT_MINOR), CircleOfFifths.Segment.V12A );
		KEY_TO_SEG_MAP.put( MusicalKey.KEY_TO_MUSICAL_KEY_MAP.get( AllKeysEnum.E_MAJOR), CircleOfFifths.Segment.V12B );

		final Set<MusicalKey> keys = KEY_TO_SEG_MAP.keySet();
		for( final MusicalKey key : keys )
		{
			SEG_TO_KEY_MAP.put( KEY_TO_SEG_MAP.get( key ), key );
		}

		SEG_TO_STR_MAP.put( Segment.V01A, "01A" );
		SEG_TO_STR_MAP.put( Segment.V01B, "01B" );
		SEG_TO_STR_MAP.put( Segment.V02A, "02A" );
		SEG_TO_STR_MAP.put( Segment.V02B, "02B" );
		SEG_TO_STR_MAP.put( Segment.V03A, "03A" );
		SEG_TO_STR_MAP.put( Segment.V03B, "03B" );
		SEG_TO_STR_MAP.put( Segment.V04A, "04A" );
		SEG_TO_STR_MAP.put( Segment.V04B, "04B" );
		SEG_TO_STR_MAP.put( Segment.V05A, "05A" );
		SEG_TO_STR_MAP.put( Segment.V05B, "05B" );
		SEG_TO_STR_MAP.put( Segment.V06A, "06A" );
		SEG_TO_STR_MAP.put( Segment.V06B, "06B" );
		SEG_TO_STR_MAP.put( Segment.V07A, "07A" );
		SEG_TO_STR_MAP.put( Segment.V07B, "07B" );
		SEG_TO_STR_MAP.put( Segment.V08A, "08A" );
		SEG_TO_STR_MAP.put( Segment.V08B, "08B" );
		SEG_TO_STR_MAP.put( Segment.V09A, "09A" );
		SEG_TO_STR_MAP.put( Segment.V09B, "09B" );
		SEG_TO_STR_MAP.put( Segment.V10A, "10A" );
		SEG_TO_STR_MAP.put( Segment.V10B, "10B" );
		SEG_TO_STR_MAP.put( Segment.V11A, "11A" );
		SEG_TO_STR_MAP.put( Segment.V11B, "11B" );
		SEG_TO_STR_MAP.put( Segment.V12A, "12A" );
		SEG_TO_STR_MAP.put( Segment.V12B, "12B" );
	}
}
