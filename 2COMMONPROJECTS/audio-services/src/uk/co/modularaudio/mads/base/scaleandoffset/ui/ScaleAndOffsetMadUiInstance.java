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

package uk.co.modularaudio.mads.base.scaleandoffset.ui;

import uk.co.modularaudio.mads.base.scaleandoffset.mu.ScaleAndOffsetMadDefinition;
import uk.co.modularaudio.mads.base.scaleandoffset.mu.ScaleAndOffsetMadInstance;
import uk.co.modularaudio.mads.base.scaleandoffset.mu.ScaleAndOffsetIOQueueBridge;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.swing.general.FloatJSliderModel;

public class ScaleAndOffsetMadUiInstance extends AbstractNonConfigurableMadUiInstance<ScaleAndOffsetMadDefinition, ScaleAndOffsetMadInstance>
{
//	private static Log log = LogFactory.getLog( ScaleAndOffsetMadUiInstance.class.getName() );
	
	public final static double START_VAL = 1.0;
	public final static double MIN_VAL = 0.0;
	public final static double MAX_VAL = 500.0;
	public final static double VAL_RES = 0.1;
	
	public FloatJSliderModel valueFloatSliderModel = new FloatJSliderModel( START_VAL, MIN_VAL, MAX_VAL, VAL_RES );
	public FloatJSliderModel scaleFloatSliderModel = new FloatJSliderModel( START_VAL, MIN_VAL, MAX_VAL, VAL_RES );
	
	public ScaleAndOffsetMadUiInstance( ScaleAndOffsetMadInstance instance,
			ScaleAndOffsetMadUiDefinition uiDefinition )
	{
		super( uiDefinition.getCellSpan(), instance, uiDefinition );
	}

	public void sendScaleChange( float floatValue )
	{
		sendTemporalValueToInstance( ScaleAndOffsetIOQueueBridge.COMMAND_IN_SCALE, Float.floatToIntBits( floatValue ) );
	}

	public void sendOffsetChange( float floatValue )
	{
		sendTemporalValueToInstance( ScaleAndOffsetIOQueueBridge.COMMAND_IN_OFFSET, Float.floatToIntBits( floatValue ) );
	}

	@Override
	public void consumeQueueEntry( ScaleAndOffsetMadInstance instance, IOQueueEvent nextOutgoingEntry)
	{
	}
}
