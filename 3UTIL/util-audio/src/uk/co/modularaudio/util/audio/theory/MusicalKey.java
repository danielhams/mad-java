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

import uk.co.modularaudio.util.tuple.TwoTuple;

public class MusicalKey
{
//	private static Log log = LogFactory.getLog( MusicalKey.class.getName() );
	
	public Root root;
	public Scale scale;
	
	protected MusicalKey( Root root, Scale scale )
	{
		this.root = root;
		this.scale = scale;
	}
	
	public String toString()
	{
		return root.toString() + " " + scale.toString();
	}
	
	public enum AllKeysEnum
	{
		GSHARP_AFLAT_MINOR,	// 01A
		B_CFLAT_MAJOR,					// 01B
		DSHARP_EFLAT_MINOR,		// 02A
		FSHARP_GFLAT_MAJOR,	// 02B
		ASHARP_BFLAT_MINOR,		// 03A
		CSHARP_DFLAT_MAJOR,	// 03B
		ESHARP_F_MINOR,				// 04A
		GSHARP_AFLAT_MAJOR,	// 04B
		C_MINOR,									// 05A
		DSHARP_EFLAT_MAJOR,	// 05B
		G_MINOR,								// 06A
		ASHARP_BFLAT_MAJOR,	// 06B
		D_MINOR,								// 07A
		ESHARP_F_MAJOR,				// 07B
		A_MINOR,									// 08A
		C_MAJOR,								// 08B
		E_MINOR,									// 09A
		G_MAJOR,								// 09B
		B_CFLAT_MINOR,					// 10A
		D_MAJOR,								// 10B
		FSHARP_GFLAT_MINOR,		// 11A
		A_MAJOR,								// 11B
		CSHARP_DFLAT_MINOR,	// 12A
		E_MAJOR									// 12B
	};
	
	public static Map<AllKeysEnum,MusicalKey> keyEnumToMusicalKeyMap = new HashMap<AllKeysEnum,MusicalKey>();
	public static Map<MusicalKey, AllKeysEnum> musicalKeyToKeyEnumMap = new HashMap<MusicalKey, AllKeysEnum>();
	public static Map<Root, TwoTuple<MusicalKey, MusicalKey>> rootToMajorMinorMap = new HashMap<Root, TwoTuple<MusicalKey, MusicalKey>>();
	
	static
	{
		keyEnumToMusicalKeyMap.put( AllKeysEnum.GSHARP_AFLAT_MINOR, new MusicalKey( Root.GSHARP_AFLAT, Scale.MINOR ) );			// 01A
		keyEnumToMusicalKeyMap.put( AllKeysEnum.B_CFLAT_MAJOR, new MusicalKey( Root.B_CFLAT, Scale.MAJOR ) );									// 01B
		keyEnumToMusicalKeyMap.put( AllKeysEnum.DSHARP_EFLAT_MINOR, new MusicalKey( Root.DSHARP_EFLAT, Scale.MINOR ) );			// 02A
		keyEnumToMusicalKeyMap.put( AllKeysEnum.FSHARP_GFLAT_MAJOR, new MusicalKey( Root.FSHARP_GFLAT, Scale.MAJOR ) );			// 02B
		keyEnumToMusicalKeyMap.put( AllKeysEnum.ASHARP_BFLAT_MINOR, new MusicalKey( Root.ASHARP_BFLAT, Scale.MINOR ) );			// 03A
		keyEnumToMusicalKeyMap.put( AllKeysEnum.CSHARP_DFLAT_MAJOR, new MusicalKey( Root.CSHARP_DFLAT, Scale.MAJOR ) );			// 03B
		keyEnumToMusicalKeyMap.put( AllKeysEnum.ESHARP_F_MINOR, new MusicalKey( Root.ESHARP_F, Scale.MINOR ) );								// 04A
		keyEnumToMusicalKeyMap.put( AllKeysEnum.GSHARP_AFLAT_MAJOR, new MusicalKey( Root.GSHARP_AFLAT, Scale.MAJOR ) );			// 04B
		keyEnumToMusicalKeyMap.put( AllKeysEnum.C_MINOR, new MusicalKey( Root.C, Scale.MINOR ) );																	// 05A
		keyEnumToMusicalKeyMap.put( AllKeysEnum.DSHARP_EFLAT_MAJOR, new MusicalKey( Root.DSHARP_EFLAT, Scale.MAJOR ) );			// 05B
		keyEnumToMusicalKeyMap.put( AllKeysEnum.G_MINOR, new MusicalKey( Root.G, Scale.MINOR ) );																	// 06A
		keyEnumToMusicalKeyMap.put( AllKeysEnum.ASHARP_BFLAT_MAJOR, new MusicalKey( Root.ASHARP_BFLAT, Scale.MAJOR ) );			// 06B
		keyEnumToMusicalKeyMap.put( AllKeysEnum.D_MINOR, new MusicalKey( Root.D, Scale.MINOR ) );																	// 07A
		keyEnumToMusicalKeyMap.put( AllKeysEnum.ESHARP_F_MAJOR, new MusicalKey( Root.ESHARP_F, Scale.MAJOR ) );								// 07B
		keyEnumToMusicalKeyMap.put( AllKeysEnum.A_MINOR, new MusicalKey( Root.A, Scale.MINOR ) );																	// 08A
		keyEnumToMusicalKeyMap.put( AllKeysEnum.C_MAJOR, new MusicalKey( Root.C, Scale.MAJOR ) );																	// 08B
		keyEnumToMusicalKeyMap.put( AllKeysEnum.E_MINOR, new MusicalKey( Root.E, Scale.MINOR ) );																	// 09A
		keyEnumToMusicalKeyMap.put( AllKeysEnum.G_MAJOR, new MusicalKey( Root.G, Scale.MAJOR ) );																	// 09B
		keyEnumToMusicalKeyMap.put( AllKeysEnum.B_CFLAT_MINOR, new MusicalKey( Root.B_CFLAT, Scale.MINOR ) );										// 10A
		keyEnumToMusicalKeyMap.put( AllKeysEnum.D_MAJOR, new MusicalKey( Root.D, Scale.MAJOR ) );																	// 10B
		keyEnumToMusicalKeyMap.put( AllKeysEnum.FSHARP_GFLAT_MINOR, new MusicalKey( Root.FSHARP_GFLAT, Scale.MINOR ) );			// 11A
		keyEnumToMusicalKeyMap.put( AllKeysEnum.A_MAJOR, new MusicalKey( Root.A, Scale.MAJOR ) );																	// 11B
		keyEnumToMusicalKeyMap.put( AllKeysEnum.CSHARP_DFLAT_MINOR, new MusicalKey( Root.CSHARP_DFLAT, Scale.MINOR ) );			// 12A
		keyEnumToMusicalKeyMap.put( AllKeysEnum.E_MAJOR, new MusicalKey( Root.E, Scale.MAJOR ) );																	// 12B
		
		Set<AllKeysEnum> allKeysSet = keyEnumToMusicalKeyMap.keySet();
		for( AllKeysEnum keyEnum : allKeysSet )
		{
			MusicalKey mk = keyEnumToMusicalKeyMap.get( keyEnum );
			musicalKeyToKeyEnumMap.put( mk, keyEnum );
		}
		
		// And fill in the rootToMajorMinor map
		for( AllKeysEnum keyEnum : allKeysSet )
		{
			MusicalKey mk = keyEnumToMusicalKeyMap.get( keyEnum );
			TwoTuple<MusicalKey,MusicalKey> majorMinorEntry = rootToMajorMinorMap.get( mk.root );
			if( majorMinorEntry == null )
			{
				majorMinorEntry = new TwoTuple<MusicalKey, MusicalKey>(null, null);
				rootToMajorMinorMap.put( mk.root, majorMinorEntry );
			}
			if( mk.scale == Scale.MAJOR )
			{
				majorMinorEntry.setHead( mk );
			}
			else
			{
				majorMinorEntry.setTail( mk );
			}
		}
	};
	
	public static MusicalKey lookupKeyFromString( String inKey )
		throws UnknownMusicalKeyException
	{
		MusicalKey retVal = null;
		boolean parsingError = false;
		
		if( inKey.length() < 6 )
		{
			parsingError = true;
		}
		else
		{
			String rootKeyStr = inKey.substring( 0, inKey.length() - 5 );
			String scaleStr = inKey.substring( inKey.length() - 5, inKey.length() );
//			log.debug( "Got rootkeyStr " + rootKeyStr + " and scaleStr " + scaleStr );
			
			// Now lookup the actual root key and scale
			Root rootKey = Root.stringToRootMap.get( rootKeyStr );
			Scale scale = Scale.stringToScaleMap.get( scaleStr );
			if( rootKey == null ||
					scale == null )
			{
				parsingError = true;
			}
			else
			{
				TwoTuple<MusicalKey, MusicalKey> majorMinorEntry = rootToMajorMinorMap.get( rootKey );

				switch( scale )
				{
				case MAJOR:
					retVal = majorMinorEntry.getHead();
					break;
				case MINOR:
					retVal = majorMinorEntry.getTail();
					break;
				}
			}
		}
		
		if( parsingError )
		{
			String msg = "Unable to parse supplied musical key string: " + inKey + " - it should be of the form C#MAJOR or AbMINOR";
			throw new UnknownMusicalKeyException( msg );
		}
		
		return retVal;
	}
}
