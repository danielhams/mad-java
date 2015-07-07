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

package uk.co.modularaudio.mads.base.crossfader.ui;

import uk.co.modularaudio.mads.base.crossfader.mu.CrossFaderIOQueueBridge;
import uk.co.modularaudio.mads.base.crossfader.mu.CrossFaderMadDefinition;
import uk.co.modularaudio.mads.base.crossfader.mu.CrossFaderMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.helper.NoEventsNoNameChangeNonConfigurableMadUiInstance;
import uk.co.modularaudio.util.audio.lookuptable.powertable.RawCrossfadePowerTable;
import uk.co.modularaudio.util.audio.lookuptable.powertable.StandardCrossfadePowerTables;

public class CrossFaderMadUiInstance extends NoEventsNoNameChangeNonConfigurableMadUiInstance<CrossFaderMadDefinition, CrossFaderMadInstance>
{
//	private static Log log = LogFactory.getLog( CrossFaderMadUiInstance.class.getName() );

	private boolean guiKillA;
	private boolean guiKillB;
	private float guiCrossFaderPosition = 0.0f;

	private RawCrossfadePowerTable powerCurve = StandardCrossfadePowerTables.getAdditivePowerTable();

	public CrossFaderMadUiInstance( final CrossFaderMadInstance instance,
			final CrossFaderMadUiDefinition uiDefinition )
	{
		super( uiDefinition.getCellSpan(), instance, uiDefinition );
	}

	public void recalculateAmps()
	{
		// Varies from -1.0 to +1.0
		// Now use the power table from the instance to calculate the real amps
		float calculatedAmpA = powerCurve.getLeftValueAt( guiCrossFaderPosition );
		float calculatedAmpB = powerCurve.getRightValueAt( guiCrossFaderPosition );

		// Now take into account the kill buttons
		if( guiKillA )
		{
			calculatedAmpA = 0.0f;
		}

		if( guiKillB )
		{
			calculatedAmpB = 0.0f;
		}

		// Now pass these values to the running instance
		sendAmpAAmpBChange( calculatedAmpA, calculatedAmpB );
	}

	public void sendAmpAAmpBChange( final float ampA, final float ampB )
	{
		final int ampAInt = Float.floatToIntBits( ampA );
		final int ampBInt = Float.floatToIntBits( ampB );
		final long combinedValue = ampAInt | ((long)ampBInt << 32 );

		sendTemporalValueToInstance( CrossFaderIOQueueBridge.COMMAND_AMPA_AMPB, combinedValue );
	}

	public void setPowerCurve( final RawCrossfadePowerTable powerCurve )
	{
		this.powerCurve = powerCurve;
	}

	public void setCrossFaderPosition( final float faderPosition )
	{
		this.guiCrossFaderPosition = faderPosition;
	}

	public void setGuiKillA( final boolean selected )
	{
		this.guiKillA = selected;
	}

	public void setGuiKillB( final boolean selected )
	{
		this.guiKillB = selected;
	}
}
