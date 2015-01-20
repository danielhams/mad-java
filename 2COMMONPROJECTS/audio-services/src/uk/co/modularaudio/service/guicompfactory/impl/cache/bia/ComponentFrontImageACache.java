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
import uk.co.modularaudio.service.guicompfactory.impl.cache.painters.GuiFrontComponentPainter;

public class ComponentFrontImageACache extends AbstractComponentImageACache
{
	private static final String GUI_CACHE_ALLOCATION_NAME = ComponentFrontImageACache.class.getSimpleName();

	private static final GuiComponentPainter painter = new GuiFrontComponentPainter();

	public ComponentFrontImageACache( BufferedImageAllocationService bufferedImageAllocationService )
	{
		super( GUI_CACHE_ALLOCATION_NAME, bufferedImageAllocationService, painter );
	}
}
