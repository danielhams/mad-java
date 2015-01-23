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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
/**
 * <P>Represents a single line for replacement in a template.</P>
 * <P>It contains references to positions of tags (TagPos classes), along with
 * the string of the line itself.</P>
 * @author D Hams
 * @version 1.0
 * @see uk.co.modularaudio.util.template.Template
 * @see uk.co.modularaudio.util.template.TemplateReplacements
 * @see uk.co.modularaudio.util.template.TemplateManager
 * @see uk.co.modularaudio.util.template.TemplateFactory
 * @see uk.co.modularaudio.util.template.TagPos
 */
public class TemplateLine
{
	public TemplateLine(final String line, final String[] tags)
	{
		tagPositions = new ArrayList<TagPos>();
		theLine = line;
		// Cycle through the line, looking for the tag that matches earliest
		// since the end of the last tag, and then create a new
		// TagPos object for it, and add it into the list.

		// The predicate for the loop
		int lastMatchedTagPos = 0;

		while (tags != null && lastMatchedTagPos < line.length())
		{
			int earliestMatchedTagNum = -1;
			int earliestMatchedTagPos = line.length();
			for (int checkTag = 0; checkTag < tags.length; checkTag++)
			{
				int currentMatchedTagPos = -1;

				if ((currentMatchedTagPos =
					line.indexOf(tags[checkTag], lastMatchedTagPos))
					!= -1)
				{
					if (currentMatchedTagPos < earliestMatchedTagPos)
					{
						earliestMatchedTagPos = currentMatchedTagPos;
						earliestMatchedTagNum = checkTag;
					}
				}
			}
			// We checked all the tags, if earliestMatchedTagNum
			// is not -1, we found a new TagPos object
			if (earliestMatchedTagNum != -1)
			{
				final String whichTag = tags[earliestMatchedTagNum];

				final TagPos tp =
					new TagPos(
						earliestMatchedTagPos,
						whichTag.length(),
						whichTag);

				tagPositions.add(tp);

				lastMatchedTagPos = earliestMatchedTagPos + whichTag.length();
			}
			else
			{
				lastMatchedTagPos = line.length();
			}
		}
	}

	public StringBuilder replaceTags(final TemplateReplacements reps)
	{
		final StringBuilder retVal = new StringBuilder();

		// Performance hack - if no tags in list, output the whole line
		if (tagPositions.size() == 0)
		{
			retVal.append(theLine);
		}
		else
		{
			// Starting from the start of the string, go up to the next tag
			// (if one exists), and output
			int lastPos = 0;

			final Iterator<TagPos> iter = tagPositions.iterator();

			while (iter.hasNext())
			{
				final TagPos tp = iter.next();
				final int tagpos = tp.position;
				final int taglen = tp.length;
				final String tag = tp.value;

				// Output the line from lastPos up to the startpos
				// of the replacement only if there is some to output
				retVal.append(theLine.substring(lastPos, tagpos));

				// Now add in the replacement
				final String rep = reps.get(tag);
				retVal.append(rep);

				// Now move lastPos past this tag
				lastPos = (tagpos + taglen);
			}
			// Now output the rest of the line (if any)
			retVal.append(theLine.substring(lastPos));
		}
		return (retVal);
	}

	private final List<TagPos> tagPositions;

	private final String theLine;
}
