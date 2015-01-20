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

package uk.co.modularaudio.mads.base.staticvalue.ui;

import uk.co.modularaudio.mads.base.staticvalue.mu.StaticValueMadDefinition;
import uk.co.modularaudio.mads.base.staticvalue.mu.StaticValueMadInstance;
import uk.co.modularaudio.mads.base.staticvalue.mu.StaticValueIOQueueBridge;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.swing.general.FloatJSliderModel;

public class StaticValueMadUiInstance extends AbstractNonConfigurableMadUiInstance<StaticValueMadDefinition, StaticValueMadInstance>
{
//	private static Log log = LogFactory.getLog( StaticValueMadUiInstance.class.getName() );
	
	public final static double START_VAL = 50.0;
	public final static double MIN_VAL = 0.0;
	public final static double MAX_VAL = 500.0;
	public final static double VAL_RES = 0.001;
	
	protected FloatJSliderModel valueFloatSliderModel = new FloatJSliderModel( START_VAL, MIN_VAL, MAX_VAL, VAL_RES );
	
	public StaticValueMadUiInstance( StaticValueMadInstance instance,
			StaticValueMadUiDefinition uiDefinition )
	{
		super( uiDefinition.getCellSpan(), instance, uiDefinition );
	}

	public void sendValueChange( float newValue )
	{
		sendTemporalValueToInstance( StaticValueIOQueueBridge.COMMAND_VALUE, Float.floatToIntBits( newValue ) );
	}

	@Override
	public void consumeQueueEntry( StaticValueMadInstance instance, IOQueueEvent nextOutgoingEntry)
	{
	}
}
