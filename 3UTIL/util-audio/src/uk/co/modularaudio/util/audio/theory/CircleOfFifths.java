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
	
	public static Map<MusicalKey, CircleOfFifths.Segment> keyToSegmentMap = new HashMap<MusicalKey, CircleOfFifths.Segment>();
	public static Map<CircleOfFifths.Segment, MusicalKey> segmentToKeyMap = new HashMap<CircleOfFifths.Segment, MusicalKey>();;
	
	public static Map<Segment, String> segmentToUserStringMap = new HashMap<Segment,String>();
	
	static
	{
		keyToSegmentMap.put( MusicalKey.keyEnumToMusicalKeyMap.get( AllKeysEnum.GSHARP_AFLAT_MINOR), CircleOfFifths.Segment.V01A );
		keyToSegmentMap.put( MusicalKey.keyEnumToMusicalKeyMap.get( AllKeysEnum.B_CFLAT_MAJOR), CircleOfFifths.Segment.V01B );
		keyToSegmentMap.put( MusicalKey.keyEnumToMusicalKeyMap.get( AllKeysEnum.DSHARP_EFLAT_MINOR), CircleOfFifths.Segment.V02A );
		keyToSegmentMap.put( MusicalKey.keyEnumToMusicalKeyMap.get( AllKeysEnum.FSHARP_GFLAT_MAJOR), CircleOfFifths.Segment.V02B );
		keyToSegmentMap.put( MusicalKey.keyEnumToMusicalKeyMap.get( AllKeysEnum.ASHARP_BFLAT_MINOR), CircleOfFifths.Segment.V03A );
		keyToSegmentMap.put( MusicalKey.keyEnumToMusicalKeyMap.get( AllKeysEnum.CSHARP_DFLAT_MAJOR), CircleOfFifths.Segment.V03B );
		keyToSegmentMap.put( MusicalKey.keyEnumToMusicalKeyMap.get( AllKeysEnum.ESHARP_F_MINOR), CircleOfFifths.Segment.V04A );
		keyToSegmentMap.put( MusicalKey.keyEnumToMusicalKeyMap.get( AllKeysEnum.GSHARP_AFLAT_MAJOR), CircleOfFifths.Segment.V04B );
		keyToSegmentMap.put( MusicalKey.keyEnumToMusicalKeyMap.get( AllKeysEnum.C_MINOR), CircleOfFifths.Segment.V05A );
		keyToSegmentMap.put( MusicalKey.keyEnumToMusicalKeyMap.get( AllKeysEnum.DSHARP_EFLAT_MAJOR), CircleOfFifths.Segment.V05B );
		keyToSegmentMap.put( MusicalKey.keyEnumToMusicalKeyMap.get( AllKeysEnum.G_MINOR), CircleOfFifths.Segment.V06A );
		keyToSegmentMap.put( MusicalKey.keyEnumToMusicalKeyMap.get( AllKeysEnum.ASHARP_BFLAT_MAJOR), CircleOfFifths.Segment.V06B );
		keyToSegmentMap.put( MusicalKey.keyEnumToMusicalKeyMap.get( AllKeysEnum.D_MINOR), CircleOfFifths.Segment.V07A );
		keyToSegmentMap.put( MusicalKey.keyEnumToMusicalKeyMap.get( AllKeysEnum.ESHARP_F_MAJOR), CircleOfFifths.Segment.V07B );
		keyToSegmentMap.put( MusicalKey.keyEnumToMusicalKeyMap.get( AllKeysEnum.A_MINOR), CircleOfFifths.Segment.V08A );
		keyToSegmentMap.put( MusicalKey.keyEnumToMusicalKeyMap.get( AllKeysEnum.C_MAJOR), CircleOfFifths.Segment.V08B );
		keyToSegmentMap.put( MusicalKey.keyEnumToMusicalKeyMap.get( AllKeysEnum.E_MINOR), CircleOfFifths.Segment.V09A );
		keyToSegmentMap.put( MusicalKey.keyEnumToMusicalKeyMap.get( AllKeysEnum.G_MAJOR), CircleOfFifths.Segment.V09B );
		keyToSegmentMap.put( MusicalKey.keyEnumToMusicalKeyMap.get( AllKeysEnum.B_CFLAT_MINOR), CircleOfFifths.Segment.V10A );
		keyToSegmentMap.put( MusicalKey.keyEnumToMusicalKeyMap.get( AllKeysEnum.D_MAJOR), CircleOfFifths.Segment.V10B );
		keyToSegmentMap.put( MusicalKey.keyEnumToMusicalKeyMap.get( AllKeysEnum.FSHARP_GFLAT_MINOR), CircleOfFifths.Segment.V11A );
		keyToSegmentMap.put( MusicalKey.keyEnumToMusicalKeyMap.get( AllKeysEnum.A_MAJOR), CircleOfFifths.Segment.V11B );
		keyToSegmentMap.put( MusicalKey.keyEnumToMusicalKeyMap.get( AllKeysEnum.CSHARP_DFLAT_MINOR), CircleOfFifths.Segment.V12A );
		keyToSegmentMap.put( MusicalKey.keyEnumToMusicalKeyMap.get( AllKeysEnum.E_MAJOR), CircleOfFifths.Segment.V12B );
		
		Set<MusicalKey> keys = keyToSegmentMap.keySet();
		for( MusicalKey key : keys )
		{
			segmentToKeyMap.put( keyToSegmentMap.get( key ), key );
		}

		segmentToUserStringMap.put( Segment.V01A, "01A" );
		segmentToUserStringMap.put( Segment.V01B, "01B" );
		segmentToUserStringMap.put( Segment.V02A, "02A" );
		segmentToUserStringMap.put( Segment.V02B, "02B" );
		segmentToUserStringMap.put( Segment.V03A, "03A" );
		segmentToUserStringMap.put( Segment.V03B, "03B" );
		segmentToUserStringMap.put( Segment.V04A, "04A" );
		segmentToUserStringMap.put( Segment.V04B, "04B" );
		segmentToUserStringMap.put( Segment.V05A, "05A" );
		segmentToUserStringMap.put( Segment.V05B, "05B" );
		segmentToUserStringMap.put( Segment.V06A, "06A" );
		segmentToUserStringMap.put( Segment.V06B, "06B" );
		segmentToUserStringMap.put( Segment.V07A, "07A" );
		segmentToUserStringMap.put( Segment.V07B, "07B" );
		segmentToUserStringMap.put( Segment.V08A, "08A" );
		segmentToUserStringMap.put( Segment.V08B, "08B" );
		segmentToUserStringMap.put( Segment.V09A, "09A" );
		segmentToUserStringMap.put( Segment.V09B, "09B" );
		segmentToUserStringMap.put( Segment.V10A, "10A" );
		segmentToUserStringMap.put( Segment.V10B, "10B" );
		segmentToUserStringMap.put( Segment.V11A, "11A" );
		segmentToUserStringMap.put( Segment.V11B, "11B" );
		segmentToUserStringMap.put( Segment.V12A, "12A" );
		segmentToUserStringMap.put( Segment.V12B, "12B" );
	}
}
