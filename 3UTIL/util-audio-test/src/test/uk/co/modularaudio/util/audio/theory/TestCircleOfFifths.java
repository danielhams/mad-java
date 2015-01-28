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

package test.uk.co.modularaudio.util.audio.theory;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.theory.CircleOfFifths;
import uk.co.modularaudio.util.audio.theory.MusicalKey;
import uk.co.modularaudio.util.audio.theory.CircleOfFifths.Segment;

public class TestCircleOfFifths extends TestCase
{
	private static Log log = LogFactory.getLog( TestCircleOfFifths.class.getName() );
	
	public void testKeyLookup()
		throws Exception
	{
		String[] testKeys = new String[] {
				"AbMINOR",
				"G#MINOR",
				"BMAJOR",
				"CbMAJOR",
				"D#MINOR",
				"EbMINOR",
				"F#MAJOR",
				"GbMAJOR",
				"A#MINOR",
				"BbMINOR",
				"C#MAJOR",
				"DbMAJOR",
				"E#MINOR",
				"FMINOR",
				"G#MAJOR",
				"AbMAJOR",
				"CMINOR",
				"D#MAJOR",
				"EbMAJOR",
				"GMINOR",
				"A#MAJOR",
				"BbMAJOR",
				"DMINOR",
				"E#MAJOR",
				"FMAJOR",
				"AMINOR",
				"CMAJOR",
				"EMINOR",
				"GMAJOR",
				"BMINOR",
				"CbMINOR",
				"DMAJOR",
				"F#MINOR",
				"GbMINOR",
				"AMAJOR",
				"C#MINOR",
				"DbMINOR",
				"EMAJOR"
		};
		for( int i = 0 ; i < testKeys.length ; i++ )
		{
			String testMusicalKeyStr = testKeys[i];
			MusicalKey musicalKey = MusicalKey.lookupKeyFromString( testMusicalKeyStr );
			Segment cofSeg = CircleOfFifths.KEY_TO_SEG_MAP.get( musicalKey );
			log.debug( "MK: " + musicalKey.toString() + " -> " + cofSeg.toString()  + "\t\t\t" + testMusicalKeyStr);
		}
	}
}
