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

package uk.co.modularaudio.service.guicompfactory.impl.cache.bia;

import uk.co.modularaudio.service.bufferedimageallocation.BufferedImageAllocationService;
import uk.co.modularaudio.service.guicompfactory.impl.cache.GuiComponentPainter;
import uk.co.modularaudio.service.guicompfactory.impl.cache.painters.GuiBackComponentPainter;

public class ComponentBackImageACache extends AbstractComponentImageACache
{
	private static final String CACHE_ALLOCATION_NAME = ComponentBackImageACache.class.getSimpleName();

	private static final GuiComponentPainter PAINTER = new GuiBackComponentPainter();

	public ComponentBackImageACache( final BufferedImageAllocationService imageAllocationService, final boolean useCustomImages  )
	{
		super( CACHE_ALLOCATION_NAME, imageAllocationService, PAINTER, useCustomImages );
	}
}
