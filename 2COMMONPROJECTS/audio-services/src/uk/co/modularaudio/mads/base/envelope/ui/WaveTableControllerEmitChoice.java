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

package uk.co.modularaudio.mads.base.envelope.ui;

import uk.co.modularaudio.util.audio.gui.wavetablecombo.WaveTableComboController;
import uk.co.modularaudio.util.audio.gui.wavetablecombo.WaveTableComboItem;
import uk.co.modularaudio.util.audio.gui.wavetablecombo.WaveTableComboModel;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public class WaveTableControllerEmitChoice extends WaveTableComboController
{
	private WaveTableChoiceChangeReceiver changeReceiver = null;

	public WaveTableControllerEmitChoice( final WaveTableComboModel model, final WaveTableChoiceChangeReceiver changeReceiver )
	{
		super( model );
		this.changeReceiver = changeReceiver;
	}

	@Override
	public void setSelectedElement( final WaveTableComboItem selectedElement )
			throws RecordNotFoundException
	{
		super.setSelectedElement( selectedElement );
		changeReceiver.receiveChangedWaveTable( selectedElement.getValue() );
	}

	@Override
	public void setSelectedElementById( final String selectedElementId )
			throws RecordNotFoundException
	{
		super.setSelectedElementById( selectedElementId );
		final WaveTableComboItem ci = model.getSelectedElement();
		changeReceiver.receiveChangedWaveTable( ci.getValue() );
	}

}
