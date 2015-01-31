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

package uk.co.modularaudio.mads.base;

import uk.co.modularaudio.controller.advancedcomponents.AdvancedComponentsFrontController;
import uk.co.modularaudio.util.audio.mad.MadCreationContext;
import uk.co.modularaudio.util.audio.oscillatortable.OscillatorFactory;

public class BaseComponentsCreationContext extends MadCreationContext
{
	private final AdvancedComponentsFrontController advancedComponentsFrontController;
	private final OscillatorFactory oscillatorFactory;

	public BaseComponentsCreationContext( AdvancedComponentsFrontController advancedComponentsFrontController,
			OscillatorFactory oscillatorFactory )
	{
		this.advancedComponentsFrontController = advancedComponentsFrontController;
		this.oscillatorFactory = oscillatorFactory;
	}
	
	public AdvancedComponentsFrontController getAdvancedComponentsFrontController()
	{
		return advancedComponentsFrontController;
	}

	public OscillatorFactory getOscillatorFactory()
	{
		return oscillatorFactory;
	}
}
