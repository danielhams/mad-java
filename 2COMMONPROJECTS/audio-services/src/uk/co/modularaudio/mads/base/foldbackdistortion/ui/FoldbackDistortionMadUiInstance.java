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

package uk.co.modularaudio.mads.base.foldbackdistortion.ui;

import uk.co.modularaudio.mads.base.foldbackdistortion.mu.FoldbackDistortionMadDefinition;
import uk.co.modularaudio.mads.base.foldbackdistortion.mu.FoldbackDistortionMadInstance;
import uk.co.modularaudio.mads.base.foldbackdistortion.mu.FoldbackDistortionIOQueueBridge;
import uk.co.modularaudio.util.audio.gui.mad.helper.NoEventsNoNameChangeNonConfigurableMadUiInstance;

public class FoldbackDistortionMadUiInstance extends NoEventsNoNameChangeNonConfigurableMadUiInstance<FoldbackDistortionMadDefinition, FoldbackDistortionMadInstance>
{
//	private static Log log = LogFactory.getLog( FoldbackDistortionMadUiInstance.class.getName() );

	public FoldbackDistortionMadUiInstance( final FoldbackDistortionMadInstance instance,
			final FoldbackDistortionMadUiDefinition uiDefinition )
	{
		super( uiDefinition.getCellSpan(), instance, uiDefinition );
	}

	public void sendMaxFoldoversChange( final int desiredMaxFoldovers )
	{
		sendTemporalValueToInstance( FoldbackDistortionIOQueueBridge.COMMAND_MAX_FOLDOVERS, desiredMaxFoldovers );
	}

	public void sendThresholdChange( final float desiredThreshold )
	{
		sendTemporalValueToInstance( FoldbackDistortionIOQueueBridge.COMMAND_THRESHOLD, Float.floatToIntBits( desiredThreshold ) );
	}
}
