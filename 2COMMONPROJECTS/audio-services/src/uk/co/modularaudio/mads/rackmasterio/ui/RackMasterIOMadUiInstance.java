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

package uk.co.modularaudio.mads.rackmasterio.ui;

import uk.co.modularaudio.mads.rackmasterio.mu.RackMasterIOMadDefinition;
import uk.co.modularaudio.mads.rackmasterio.mu.RackMasterIOMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.MadUiInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.table.Span;

public class RackMasterIOMadUiInstance extends MadUiInstance<RackMasterIOMadDefinition, RackMasterIOMadInstance>
{
	
	private RackMasterIOMadUiDefinition rmUiDefinition = null;

	public RackMasterIOMadUiInstance( RackMasterIOMadInstance instance,
			RackMasterIOMadUiDefinition uiDefinition )
	{
		super( instance, uiDefinition );
		rmUiDefinition = uiDefinition;
	}

	@Override
	public Span getCellSpan()
	{
		return rmUiDefinition.getCellSpan();
	}

	@Override
	public void consumeQueueEntry( RackMasterIOMadInstance instance, IOQueueEvent nextOutgoingEntry)
	{
	}
}
