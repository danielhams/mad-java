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

/**
 * <P>Represents a single tag replacement in a template line.</P>
 * <P>Contains the position of the tag, how long the tag is, and what the tag
 * was.</P>
 * @author D Hams
 * @version 1.0
 * @see uk.co.modularaudio.util.template.Template
 * @see uk.co.modularaudio.util.template.TemplateLine
 * @see uk.co.modularaudio.util.template.TemplateReplacements
 * @see uk.co.modularaudio.util.template.TemplateManager
 * @see uk.co.modularaudio.util.template.TemplateFactory
 */
class TagPos
{
	public TagPos(final int p, final int l, final String v)
	{
		position = p;
		length = l;
		value = v;
	}

	protected int position;
	protected int length;
	protected String value;
}
