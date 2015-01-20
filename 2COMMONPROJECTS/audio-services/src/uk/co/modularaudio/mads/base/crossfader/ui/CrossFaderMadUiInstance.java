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

import uk.co.modularaudio.mads.base.crossfader.mu.CrossFaderMadDefinition;
import uk.co.modularaudio.mads.base.crossfader.mu.CrossFaderMadInstance;
import uk.co.modularaudio.mads.base.crossfader.mu.CrossFaderIOQueueBridge;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.wavetable.powertable.RawCrossfadePowerTable;
import uk.co.modularaudio.util.audio.wavetable.powertable.StandardCrossfadePowerTables;

public class CrossFaderMadUiInstance extends AbstractNonConfigurableMadUiInstance<CrossFaderMadDefinition, CrossFaderMadInstance>
{
//	private static Log log = LogFactory.getLog( CrossFaderDefUiInstance.class.getName() );

	public boolean guiKillA = false;
	public float guiDesiredAmpA = 1.0f;
	public float guiAmpA = 1.0f;

	public boolean guiKillB = false;
	public float guiDesiredAmpB = 1.0f;
	public float guiAmpB = 1.0f;

	public float guiCrossFaderPosition = 0.0f;
	public RawCrossfadePowerTable powerCurveWaveTable = StandardCrossfadePowerTables.getAdditivePowerTable();
	
	public CrossFaderMadUiInstance( CrossFaderMadInstance instance,
			CrossFaderMadUiDefinition uiDefinition )
	{
		super( uiDefinition.getCellSpan(), instance, uiDefinition );
	}

	public void recalculateAmps()
	{
		// Varies from -1.0 to +1.0
		// Now use the power table from the instance to calculate the real amps
		float calculatedAmpA = powerCurveWaveTable.getLeftValueAt( guiCrossFaderPosition );
		float calculatedAmpB = powerCurveWaveTable.getRightValueAt( guiCrossFaderPosition );
		
		// Set the values we can use to kill/unkill a channel
		guiAmpA = calculatedAmpA;
		guiAmpB = calculatedAmpB;
		
		// Now take into account the kill buttons
		if( guiKillA )
		{
			calculatedAmpA = 0.0f;
		}
		
		if( guiKillB )
		{
			calculatedAmpB = 0.0f;
		}
		
		guiDesiredAmpA = calculatedAmpA;
		guiDesiredAmpB = calculatedAmpB;
		
		// Now pass these values to the running instance
		sendAmpAAmpBChange( guiDesiredAmpA, guiDesiredAmpB );
	}
	
	public void sendAmpAAmpBChange( float ampA, float ampB )
	{
		int ampAInt = Float.floatToIntBits( ampA );
		int ampBInt = Float.floatToIntBits( ampB );
		long combinedValue = ampAInt | ((long)ampBInt << 32 );

		sendTemporalValueToInstance( CrossFaderIOQueueBridge.COMMAND_AMPA_AMPB, combinedValue );
	}

	@Override
	public void consumeQueueEntry( CrossFaderMadInstance instance, IOQueueEvent nextOutgoingEntry)
	{
	}
}
