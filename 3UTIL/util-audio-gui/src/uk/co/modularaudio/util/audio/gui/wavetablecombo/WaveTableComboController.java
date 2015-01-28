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

package uk.co.modularaudio.util.audio.gui.wavetablecombo;

import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.mvc.combo.ComboController;

public class WaveTableComboController implements
		ComboController<WaveTableComboItem>
{
//	private static Log log = LogFactory.getLog( WaveTableComboController.class.getName() );

	protected final WaveTableComboModel model;

	public WaveTableComboController( final WaveTableComboModel model )
	{
		this.model = model;
	}

	@Override
	public void setSelectedElement( final WaveTableComboItem selectedElement )
			throws RecordNotFoundException
	{
//		log.debug("SetSelectedElement with " + selectedElement.getDisplayString() );
//		log.debug("Model says it is " + model.getSelectedElement() );
		final int index = model.getItemIndex( selectedElement );
		model.setSelectedItemByIndex( index );
	}

	@Override
	public void setSelectedElementById( final String selectedElementId )
			throws RecordNotFoundException
	{
//		log.debug("SetSelectedElementById with " + selectedElementId );
		final WaveTableComboItem ci = model.getElementById( selectedElementId );
		final int ciIndex = model.getItemIndex( ci );
		model.setSelectedItemByIndex( ciIndex );
	}

}
