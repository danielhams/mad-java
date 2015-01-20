package uk.co.modularaudio.util.swing.general;

import java.util.Enumeration;

import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

public class FontResetter
{
	public static void setUIFontFromString( String fontName, int fontStyle, int fontSize )
	{
		FontUIResource f = new FontUIResource( fontName, fontStyle, fontSize );
		setUIFont( f );
	}

	public static void setUIFont( javax.swing.plaf.FontUIResource f )
	{
		Enumeration<Object> keys = UIManager.getDefaults().keys();
		while( keys.hasMoreElements() )
		{
			Object key = keys.nextElement();
			Object value = UIManager.get( key );
			if( value != null && value instanceof javax.swing.plaf.FontUIResource )
			{
				UIManager.put( key,  f );
			}
		}
	}
}
