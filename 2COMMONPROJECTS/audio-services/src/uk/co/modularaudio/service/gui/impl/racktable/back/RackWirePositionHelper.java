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

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.service.gui.impl.racktable.RackTable;
import uk.co.modularaudio.service.gui.plugs.GuiChannelPlug;
import uk.co.modularaudio.service.guicompfactory.AbstractGuiAudioComponent;
import uk.co.modularaudio.util.audio.gui.mad.MadUiChannelInstance;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackDataModel;
import uk.co.modularaudio.util.table.TablePosition;

public class RackWirePositionHelper
{
//	private static Log log = LogFactory.getLog( NewRackWirePositionHelper.class.getName() );

	public static Point calculateCenterForComponentPlug(
			final RackTable table,
			final RackDataModel model,
			final AbstractGuiAudioComponent guiComponent,
			final RackComponent src,
			final GuiChannelPlug plug )
	{
		final Rectangle renderedRectangle = guiComponent.getRenderedRectangle();
		final Point absolutePosition = calculateComponentOrigin( table, model, src );
		final MadUiChannelInstance cd = plug.getUiChannelInstance();
		final Point internalChannelOffset = cd.getCenter();
		absolutePosition.translate( internalChannelOffset.x, internalChannelOffset.y );
		absolutePosition.translate( renderedRectangle.x, renderedRectangle.y );
		return absolutePosition;
	}

	private static Point calculateComponentOrigin( final RackTable table, final RackDataModel dataModel, final RackComponent rc )
	{
		final TablePosition cp = dataModel.getContentsOriginReturnNull( rc );
		final Dimension gridSize = table.getGridSize();
		final int componentStartX = cp.x * gridSize.width;
		final int componentStartY = cp.y * gridSize.height;
		return new Point( componentStartX, componentStartY );
	}

}
