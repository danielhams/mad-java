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

package uk.co.modularaudio.util.xml;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * @author dan
 *
 */
public class XmlCharacterEncoder
{
	/**
	 * The size our array needs to be (chars go from 0 -> 65535)
	 */
	private static int HASH_SIZE = 65536;

	/**
	 * The array of chars -> Encoding Strings
	 */
	private static String[] entityHashMap;

	/**
	 * Encodes 's' by converting each character to its equivalent XML mapping,
	 * where one exists.
	 * @param s the string to convert.
	 * @return a string with entities encoded.
	 */
	public final static String encodeForCData(String s)
	{
		return s.replaceAll( "]]>", "]]]]><![CDATA[>");
	}
	
	/**
	 * Encodes 's' by converting each character to its equivalent XML mapping,
	 * where one exists.
	 * @param s the string to convert.
	 * @return a string with entities encoded.
	 */
	public final static String encode(String s)
	{
		StringBuilder sb = new StringBuilder();
		char[] chars = s.toCharArray();
		for (int i = 0; i < chars.length; i++)
		{
			char c = chars[i];
			String hashString = entityHashMap[(int) c];
			if (hashString == null)
			{
				sb.append(String.valueOf(c));
			}
			else
			{
				sb.append(hashString);
			}
		}
		return sb.toString();
	}

	/**
	 * Converts a character into its entity equivalent. If no entity
	 * corresponding to 'c' exist, this method returns a string containing 'c'.
	 * This means that 'getEntity()' is always guaranteed to return
	 * a valid string representing 'c'.
	 * @param c the character to convert.
	 * @return a string that contains either an entity representing 'c'
	 * or 'c' itself.
	 */
	public final static String getEntity(char c)
	{
		String s = entityHashMap[(int) c];
		if (s == null)
		{
			return (String.valueOf(s));
		}
		else
		{
			return (s);
		}
	}

	/**
	 * Initialisation of the internal (int)char -> String tables.
	 */
	static
	{

		entityHashMap = new String[HASH_SIZE];
		for (int i = 0; i < HASH_SIZE; i++)
		{
			entityHashMap[i] = null;
		}
		// Greater than, less than, single apostraphe, double quotes and ampersand
		entityHashMap[(int) '\u0022'] = "&quot;"; //quotation mark = APL quote.
		entityHashMap[(int) '\u0026'] = "&amp;"; //ampersand.
		entityHashMap[(int) '\u003C'] = "&lt;"; //less-than sign.
		entityHashMap[(int) '\u003E'] = "&gt;"; //greater-than sign.
		entityHashMap[(int) '\'']     = "&apos;"; // Apostraphe
	}

	/**
	 * Creates a Writer that, when written to, writes entity
	 * equivalents of each character to 'out'; where entity equivalents do not
	 * exist, the original character is written directly to 'out'.
	 * @param out the writer to which to write the output.
	 */
	public final static Writer createWriter(final Writer out)
	{
		return new FilterWriter(out)
		{
			public void write(char[] cbuf, int off, int len) throws IOException
			{
				for (int i = off; i < off + len; i++)
				{
					this.out.write(XmlCharacterEncoder.getEntity(cbuf[i]));
				}
			}
			// Not sure whether I need to override these or not, so will do to
			// play safe. The Java documentation doesn't bother to say what
			// the implementation of these methods in FilterWriter actually do.
			public void close() throws IOException
			{
				this.out.close();
			}
			public void flush() throws IOException
			{
				this.out.flush();
			}
			public void write(int c) throws IOException
			{
				this.out.write(XmlCharacterEncoder.getEntity((char) c));
			}
			public void write(String str, int off, int len) throws IOException
			{
				this.write(str.toCharArray(), off, len);
			}
		};
	}
}
