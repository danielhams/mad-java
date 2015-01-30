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

package uk.co.modularaudio.mads.base.cvsurface.ui;

import uk.co.modularaudio.mads.base.cvsurface.mu.CvSurfaceMadDefinition;
import uk.co.modularaudio.mads.base.cvsurface.mu.CvSurfaceMadInstance;
import uk.co.modularaudio.mads.base.cvsurface.mu.CvSurfaceIOQueueBridge;
import uk.co.modularaudio.util.audio.gui.mad.helper.NoEventsNoNameChangeNonConfigurableMadUiInstance;

public class CvSurfaceMadUiInstance extends NoEventsNoNameChangeNonConfigurableMadUiInstance<CvSurfaceMadDefinition, CvSurfaceMadInstance>
{
//	private static Log log = LogFactory.getLog( CvSurfaceMadUiInstance.class.getName() );

	public float guiDesiredX;
	public float guiDesiredY;

	public CvSurfaceMadUiInstance( final CvSurfaceMadInstance instance,
			final CvSurfaceMadUiDefinition uiDefinition )
	{
		super( uiDefinition.getCellSpan(), instance, uiDefinition );
	}

	public void sendPositionChange( final float newX, final float newY)
	{
//		log.debug("Sending position change at " + System.nanoTime() / (1000 * 1000 ) );
		sendTemporalValueToInstance( CvSurfaceIOQueueBridge.COMMAND_NEWX, (Float.floatToIntBits( newX ) ) );

		sendTemporalValueToInstance( CvSurfaceIOQueueBridge.COMMAND_NEWY, (Float.floatToIntBits( newY ) ) );
	}
}
