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

package uk.co.modularaudio.util.template;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * <P>Allows the creation of a string (of HTML output normally) from a template file along with a list of replaceables.</P>
 * <P>After creation, the replacement positions are cached, it very performant to pass a hash of replacements and get the output.</P>
 * @author D Hams
 * @version 1.0
 * @see uk.co.modularaudio.util.template.TemplateManager
 * @see uk.co.modularaudio.util.template.TemplateFactory
 */
public class Template
{
	public Template(final InputStream is) throws IOException
	{
		this(is, null);
	}

	public Template(final InputStream is, final String[] tags) throws IOException
	{
		this( new BufferedReader( new InputStreamReader( is ) ), tags );
	}

	public Template( final BufferedReader br, final String[] tags) throws IOException
	{
		lines = new ArrayList<TemplateLine>();

		String line;
		while ((line = br.readLine()) != null)
		{
			lines.add(new TemplateLine(line, tags));
		}
	}

	public StringBuilder replaceTags()
	{
		return (replaceTags(null));
	}

	public StringBuilder replaceTags(final TemplateReplacements replacements)
	{
		final StringBuilder retVal = new StringBuilder();
		final Iterator<TemplateLine> iter = lines.iterator();
		while (iter.hasNext())
		{
			final TemplateLine tl = iter.next();
			retVal.append(tl.replaceTags(replacements));
			if( lines.size() > 1 )
			{
				retVal.append('\n');
			}
		}
		return (retVal);
	}

	private final List<TemplateLine> lines;
}
