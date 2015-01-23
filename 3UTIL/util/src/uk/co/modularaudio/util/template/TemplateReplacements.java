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

import java.util.HashMap;

/**
 * <P>A hash of tag-&gt;replacement</P>
 * <P>This class also provides a convenience method for filling in some standard tags into the replacements from the StateProcessingContext object.</P>
 * @author D Hams
 * @version 1.0
 * @see uk.co.modularaudio.util.template.Template*/
public class TemplateReplacements extends HashMap<String,String>
{
	private static final long serialVersionUID = -8545659296762439335L;

	public TemplateReplacements()
	{
	}

	public String get(final String key)
	{
		String retVal = super.get(key);
		if (retVal == null)
		{
			retVal = "";
		}
		return (retVal);
	}

}
