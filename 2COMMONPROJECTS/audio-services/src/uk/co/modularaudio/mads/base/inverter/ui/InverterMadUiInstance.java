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

package uk.co.modularaudio.mads.base.inverter.ui;

import uk.co.modularaudio.mads.base.inverter.mu.InverterMadDefinition;
import uk.co.modularaudio.mads.base.inverter.mu.InverterMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableNoEventsMadUiInstance;

public class InverterMadUiInstance extends AbstractNonConfigurableNoEventsMadUiInstance<InverterMadDefinition, InverterMadInstance>
{

	public InverterMadUiInstance( InverterMadInstance instance, InverterMadUiDefinition uiDefinition )
	{
		super( uiDefinition.getCellSpan(), instance, uiDefinition );
	}
}
