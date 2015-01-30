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

package test.uk.co.modularaudio.util.swing.togglegroup;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacToggleGroup;

public class TestToggleGroup extends PacToggleGroup
{
	private static Log log = LogFactory.getLog( TestToggleGroup.class.getName() );
	
	private final TGFrame parentFrame;
	
	public TestToggleGroup( TGFrame parentFrame, String[] optionLabels, int defaultOption )
	{
		super( optionLabels, defaultOption );
		this.parentFrame = parentFrame;
	}

	@Override
	public void receiveUpdateEvent(int previousSelection, int newSelection)
	{
		log.debug("Received update to what's selected from " + previousSelection + " to " + newSelection );
		log.debug("Parent frame size is " + parentFrame.getViewAreaDimensions().toString());
	}

}
