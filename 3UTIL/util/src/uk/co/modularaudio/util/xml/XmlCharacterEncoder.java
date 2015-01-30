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
	private final static int HASH_SIZE = 65536;

	/**
	 * The array of chars -> Encoding Strings
	 */
	private final static String[] ENTITY_HASH_MAP;

	/**
	 * Encodes 's' by converting each character to its equivalent XML mapping,
	 * where one exists.
	 * @param s the string to convert.
	 * @return a string with entities encoded.
	 */
	public final static String encodeForCData(final String s)
	{
		return s.replaceAll( "]]>", "]]]]><![CDATA[>");
	}

	/**
	 * Encodes 's' by converting each character to its equivalent XML mapping,
	 * where one exists.
	 * @param s the string to convert.
	 * @return a string with entities encoded.
	 */
	public final static String encode(final String s)
	{
		final StringBuilder sb = new StringBuilder();
		final char[] chars = s.toCharArray();
		for (int i = 0; i < chars.length; i++)
		{
			final char c = chars[i];
			final String hashString = ENTITY_HASH_MAP[c];
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
	public final static String getEntity(final char c)
	{
		final String s = ENTITY_HASH_MAP[c];
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

		ENTITY_HASH_MAP = new String[HASH_SIZE];
		for (int i = 0; i < HASH_SIZE; i++)
		{
			ENTITY_HASH_MAP[i] = null;
		}
		// Greater than, less than, single apostraphe, double quotes and ampersand
		ENTITY_HASH_MAP['\u0022'] = "&quot;"; //quotation mark = APL quote.
		ENTITY_HASH_MAP['\u0026'] = "&amp;"; //ampersand.
		ENTITY_HASH_MAP['\u003C'] = "&lt;"; //less-than sign.
		ENTITY_HASH_MAP['\u003E'] = "&gt;"; //greater-than sign.
		ENTITY_HASH_MAP['\'']     = "&apos;"; // Apostraphe
	}

	/**
	 * Creates a Writer that, when written to, writes entity
	 * equivalents of each character to 'out'; where entity equivalents do not
	 * exist, the original character is written directly to 'out'.
	 * @param outputWriter the writer to which to write the output.
	 */
	public final static Writer createWriter(final Writer outputWriter)
	{
		return new FilterWriter(outputWriter)
		{
			@Override
			public void write(final char[] cbuf, final int off, final int len) throws IOException
			{
				for (int i = off; i < off + len; i++)
				{
					this.out.write(XmlCharacterEncoder.getEntity(cbuf[i]));
				}
			}
			// Not sure whether I need to override these or not, so will do to
			// play safe. The Java documentation doesn't bother to say what
			// the implementation of these methods in FilterWriter actually do.
			@Override
			public void close() throws IOException
			{
				this.out.close();
			}
			@Override
			public void flush() throws IOException
			{
				this.out.flush();
			}
			@Override
			public void write(final int c) throws IOException
			{
				this.out.write(XmlCharacterEncoder.getEntity((char) c));
			}
			@Override
			public void write(final String str, final int off, final int len) throws IOException
			{
				this.write(str.toCharArray(), off, len);
			}
		};
	}
}
