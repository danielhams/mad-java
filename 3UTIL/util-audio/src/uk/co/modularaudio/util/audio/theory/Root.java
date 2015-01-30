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

	public final static Map<Root,List<String>> ROOT_TO_STR_MAP = new HashMap<Root,List<String>>();
	public final static Map<String, Root> STR_TO_ROOT_MAP = new HashMap<String,Root>();

	static
	{
		List<String> tmpList = new ArrayList<String>();
		tmpList.add( "C" );
		ROOT_TO_STR_MAP.put( C, tmpList );
		STR_TO_ROOT_MAP.put( "C", C );

		tmpList = new ArrayList<String>();
		tmpList.add( "C#" );
		tmpList.add( "Db" );
		ROOT_TO_STR_MAP.put( CSHARP_DFLAT, tmpList);
		STR_TO_ROOT_MAP.put( "C#", CSHARP_DFLAT );
		STR_TO_ROOT_MAP.put( "Db", CSHARP_DFLAT );

		tmpList = new ArrayList<String>();
		tmpList.add( "D" );
		ROOT_TO_STR_MAP.put( D, tmpList );
		STR_TO_ROOT_MAP.put( "D", D );

		tmpList = new ArrayList<String>();
		tmpList.add( "D#" );
		tmpList.add( "Eb" );
		ROOT_TO_STR_MAP.put( DSHARP_EFLAT, tmpList );
		STR_TO_ROOT_MAP.put( "D#", DSHARP_EFLAT );
		STR_TO_ROOT_MAP.put( "Eb", DSHARP_EFLAT );

		tmpList = new ArrayList<String>();
		tmpList.add( "E" );
		ROOT_TO_STR_MAP.put( E, tmpList );
		STR_TO_ROOT_MAP.put( "E", E );

		tmpList = new ArrayList<String>();
		tmpList.add( "F" );
		tmpList.add( "E#" );
		ROOT_TO_STR_MAP.put( ESHARP_F, tmpList );
		STR_TO_ROOT_MAP.put( "F", ESHARP_F );
		STR_TO_ROOT_MAP.put( "E#", ESHARP_F );

		tmpList = new ArrayList<String>();
		tmpList.add( "F#" );
		tmpList.add( "Gb" );
		ROOT_TO_STR_MAP.put( FSHARP_GFLAT, tmpList );
		STR_TO_ROOT_MAP.put( "F#", FSHARP_GFLAT );
		STR_TO_ROOT_MAP.put( "Gb", FSHARP_GFLAT );

		tmpList = new ArrayList<String>();
		tmpList.add( "G" );
		ROOT_TO_STR_MAP.put( G, tmpList );
		STR_TO_ROOT_MAP.put( "G", G );

		tmpList = new ArrayList<String>();
		tmpList.add( "G#" );
		tmpList.add( "Ab" );
		ROOT_TO_STR_MAP.put( GSHARP_AFLAT, tmpList );
		STR_TO_ROOT_MAP.put( "G#", GSHARP_AFLAT );
		STR_TO_ROOT_MAP.put( "Ab", GSHARP_AFLAT );

		tmpList = new ArrayList<String>();
		tmpList.add( "A" );
		ROOT_TO_STR_MAP.put( A, tmpList );
		STR_TO_ROOT_MAP.put( "A", A );

		tmpList = new ArrayList<String>();
		tmpList.add( "A#" );
		tmpList.add( "Bb" );
		ROOT_TO_STR_MAP.put( ASHARP_BFLAT, tmpList );
		STR_TO_ROOT_MAP.put( "A#", ASHARP_BFLAT );
		STR_TO_ROOT_MAP.put( "Bb", ASHARP_BFLAT );

		tmpList = new ArrayList<String>();
		tmpList.add( "B" );
		tmpList.add( "Cb" );
		ROOT_TO_STR_MAP.put( B_CFLAT, tmpList );
		STR_TO_ROOT_MAP.put( "B", B_CFLAT );
		STR_TO_ROOT_MAP.put( "Cb", B_CFLAT );
	}
}
