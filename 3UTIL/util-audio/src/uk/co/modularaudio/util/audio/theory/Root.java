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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum Root
{
	C,
	CSHARP_DFLAT,
	D,
	DSHARP_EFLAT,
	E,
	ESHARP_F,
	FSHARP_GFLAT,
	G,
	GSHARP_AFLAT,
	A,
	ASHARP_BFLAT,
	B_CFLAT;
	
	public static Map<Root,List<String>> rootToStringMap = new HashMap<Root,List<String>>();
	public static Map<String, Root> stringToRootMap = new HashMap<String,Root>();

	static
	{
		List<String> tmpList = new ArrayList<String>();
		tmpList.add( "C" );
		rootToStringMap.put( C, tmpList );
		stringToRootMap.put( "C", C );
		
		tmpList = new ArrayList<String>();
		tmpList.add( "C#" );
		tmpList.add( "Db" );
		rootToStringMap.put( CSHARP_DFLAT, tmpList);
		stringToRootMap.put( "C#", CSHARP_DFLAT );
		stringToRootMap.put( "Db", CSHARP_DFLAT );
		
		tmpList = new ArrayList<String>();
		tmpList.add( "D" );
		rootToStringMap.put( D, tmpList );
		stringToRootMap.put( "D", D );
		
		tmpList = new ArrayList<String>();
		tmpList.add( "D#" );
		tmpList.add( "Eb" );
		rootToStringMap.put( DSHARP_EFLAT, tmpList );
		stringToRootMap.put( "D#", DSHARP_EFLAT );
		stringToRootMap.put( "Eb", DSHARP_EFLAT );
		
		tmpList = new ArrayList<String>();
		tmpList.add( "E" );
		rootToStringMap.put( E, tmpList );
		stringToRootMap.put( "E", E );
		
		tmpList = new ArrayList<String>();
		tmpList.add( "F" );
		tmpList.add( "E#" );
		rootToStringMap.put( ESHARP_F, tmpList );
		stringToRootMap.put( "F", ESHARP_F );
		stringToRootMap.put( "E#", ESHARP_F );
		
		tmpList = new ArrayList<String>();
		tmpList.add( "F#" );
		tmpList.add( "Gb" );
		rootToStringMap.put( FSHARP_GFLAT, tmpList );
		stringToRootMap.put( "F#", FSHARP_GFLAT );
		stringToRootMap.put( "Gb", FSHARP_GFLAT );
		
		tmpList = new ArrayList<String>();
		tmpList.add( "G" );
		rootToStringMap.put( G, tmpList );
		stringToRootMap.put( "G", G );
		
		tmpList = new ArrayList<String>();
		tmpList.add( "G#" );
		tmpList.add( "Ab" );
		rootToStringMap.put( GSHARP_AFLAT, tmpList );
		stringToRootMap.put( "G#", GSHARP_AFLAT );
		stringToRootMap.put( "Ab", GSHARP_AFLAT );
		
		tmpList = new ArrayList<String>();
		tmpList.add( "A" );
		rootToStringMap.put( A, tmpList );
		stringToRootMap.put( "A", A );
		
		tmpList = new ArrayList<String>();
		tmpList.add( "A#" );
		tmpList.add( "Bb" );
		rootToStringMap.put( ASHARP_BFLAT, tmpList );
		stringToRootMap.put( "A#", ASHARP_BFLAT );
		stringToRootMap.put( "Bb", ASHARP_BFLAT );
		
		tmpList = new ArrayList<String>();
		tmpList.add( "B" );
		tmpList.add( "Cb" );
		rootToStringMap.put( B_CFLAT, tmpList );
		stringToRootMap.put( "B", B_CFLAT );
		stringToRootMap.put( "Cb", B_CFLAT );
	}
}
