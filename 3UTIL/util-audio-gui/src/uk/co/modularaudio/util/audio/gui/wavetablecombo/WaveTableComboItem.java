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

import uk.co.modularaudio.util.audio.lookuptable.LookupTable;
import uk.co.modularaudio.util.mvc.combo.idstringandvalue.IdStringAndValueComboItem;

public class WaveTableComboItem extends IdStringAndValueComboItem<LookupTable>
{
	private final boolean isBipolar;

	public WaveTableComboItem( final String id, final String displayString, final LookupTable value, final boolean isBipolar )
	{
		super( id, displayString, value );
		this.isBipolar = isBipolar;
	}

	public boolean isBipolar()
	{
		return isBipolar;
	}
}
