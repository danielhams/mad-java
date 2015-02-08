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

package uk.co.modularaudio.service.gui.impl.racktable.back;

import uk.co.modularaudio.service.gui.impl.racktable.RackTableGuiFactory;
import uk.co.modularaudio.service.guicompfactory.AbstractGuiAudioComponent;
import uk.co.modularaudio.service.guicompfactory.GuiComponentFactoryService;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;

public class BackRackTableGuiFactory implements RackTableGuiFactory
{
	private final GuiComponentFactoryService guiComponentFactoryService;

	public BackRackTableGuiFactory( final GuiComponentFactoryService guiComponentFactoryService )
	{
		this.guiComponentFactoryService = guiComponentFactoryService;
	}

	@Override
	public AbstractGuiAudioComponent generateSwingComponent( final RackComponent inComponent )
	{
		return guiComponentFactoryService.createBackGuiComponent( inComponent );
	}
}
