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

package uk.co.modularaudio.mads.base.controlprocessingtester.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.controlprocessingtester.mu.CPTIOQueueBridge;
import uk.co.modularaudio.mads.base.controlprocessingtester.mu.CPTMadDefinition;
import uk.co.modularaudio.mads.base.controlprocessingtester.mu.CPTMadInstance;
import uk.co.modularaudio.mads.base.crossfader.ui.CrossFaderMadUiInstance;
import uk.co.modularaudio.util.audio.gui.mad.helper.NoEventsNoNameChangeNonConfigurableMadUiInstance;
import uk.co.modularaudio.util.audio.lookuptable.powertable.RawCrossfadePowerTable;
import uk.co.modularaudio.util.audio.lookuptable.powertable.StandardCrossfadePowerTables;

public class CPTMadUiInstance extends NoEventsNoNameChangeNonConfigurableMadUiInstance<CPTMadDefinition, CPTMadInstance>
{
	private static Log log = LogFactory.getLog( CrossFaderMadUiInstance.class.getName() );

	private boolean guiKillA = false;
	private float guiCrossFaderPosition = 0.0f;

	private RawCrossfadePowerTable powerCurve = StandardCrossfadePowerTables.getAdditivePowerTable();

	public CPTMadUiInstance( final CPTMadInstance instance,
			final CPTMadUiDefinition uiDefinition )
	{
		super( uiDefinition.getCellSpan(), instance, uiDefinition );
	}

	public void recalculateAmps()
	{
		// Varies from -1.0 to +1.0
		// Now use the power table from the instance to calculate the real amps
		float calculatedAmpA = powerCurve.getLeftValueAt( guiCrossFaderPosition );

		// Now take into account the kill buttons
		if( guiKillA )
		{
			calculatedAmpA = 0.0f;
		}

		// Now pass these values to the running instance
		sendAmpChange( calculatedAmpA );
	}

	public void sendAmpChange( final float amp )
	{
		final int ampInt = Float.floatToIntBits( amp );
		final long combinedValue = ampInt;

		sendTemporalValueToInstance( CPTIOQueueBridge.COMMAND_AMP, combinedValue );
	}

	public void setPowerCurve( final RawCrossfadePowerTable powerCurve )
	{
		this.powerCurve = powerCurve;
	}

	public void setCrossFaderPosition( final float faderPosition )
	{
		this.guiCrossFaderPosition = faderPosition;
	}

	public void setGuiKill( final boolean selected )
	{
		this.guiKillA = selected;
	}

	public void setInterpolator( final int interpolatorIndex )
	{
		sendTemporalValueToInstance( CPTIOQueueBridge.COMMAND_INTERPOLATOR, interpolatorIndex );
		log.debug("Sent change to interpolator " + interpolatorIndex );

	}
}
