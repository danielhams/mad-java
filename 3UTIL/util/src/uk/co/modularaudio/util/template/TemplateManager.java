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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * <P>Provides a lazy evaluation cache for required templates for a servlet.</P>
 * <P>Used by the StateProcessingServlet class to fill into the StateProcessingContext, and make the templates available to all StateProcessors</P>
 * @author D Hams
 * @version 1.0
 * @see uk.co.modularaudio.util.template.Template
 * @see uk.co.modularaudio.util.template.TemplateFactory
 */
public class TemplateManager
{
	public TemplateManager(final TemplateFactory tFactory)
	{
		precachedTemplates = new HashMap<String,Template>();
		templateFactory = tFactory;
	}

	public Template getTemplate(final String tname)
		throws IOException, FileNotFoundException
	{
		// Check to see if we already have that template cached,
		// if not, create a new one, add it into the cache, and
		// return it.
		Template retTemplate = precachedTemplates.get(tname);

		if (retTemplate == null)
		{
			retTemplate = templateFactory.createTemplate(tname);
			precachedTemplates.put(tname, retTemplate);
		}

		return (retTemplate);
	}

	private final Map<String,Template> precachedTemplates;

	private final TemplateFactory templateFactory;
}
