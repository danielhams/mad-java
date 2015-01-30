package uk.co.modularaudio.util.swing.general;

import java.util.Enumeration;

import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

public class FontResetter
{
	public static void setUIFontFromString( final String fontName, final int fontStyle, final int fontSize )
	{
		final FontUIResource f = new FontUIResource( fontName, fontStyle, fontSize );
		setUIFont( f );
	}

	public static void setUIFont( final javax.swing.plaf.FontUIResource f )
	{
		final Enumeration<Object> keys = UIManager.getDefaults().keys();
		while( keys.hasMoreElements() )
		{
			final Object key = keys.nextElement();
			final Object value = UIManager.get( key );
			if( value != null && value instanceof javax.swing.plaf.FontUIResource )
			{
				UIManager.put( key,  f );
			}
		}
	}
}
