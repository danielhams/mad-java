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

package uk.co.modularaudio.mads.base.moogfilter.ui;

import uk.co.modularaudio.mads.base.moogfilter.mu.MoogFilterIOQueueBridge;
import uk.co.modularaudio.mads.base.moogfilter.mu.MoogFilterMadDefinition;
import uk.co.modularaudio.mads.base.moogfilter.mu.MoogFilterMadInstance;
import uk.co.modularaudio.util.audio.dsp.FrequencyFilterMode;
import uk.co.modularaudio.util.audio.gui.mad.helper.NoEventsNoNameChangeNonConfigurableMadUiInstance;
import uk.co.modularaudio.util.mvc.displayslider.SimpleSliderIntToFloatConverter;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayModel;

public class MoogFilterMadUiInstance extends NoEventsNoNameChangeNonConfigurableMadUiInstance<MoogFilterMadDefinition, MoogFilterMadInstance>
{
	private final SliderDisplayModel cutoffSliderModel = new SliderDisplayModel(
			0.0f,
			1.0f,
			0.5f,
			0.5f,
			1000,
			100,
			new SimpleSliderIntToFloatConverter(),
			3,
			2,
			"" );

	public MoogFilterMadUiInstance( final MoogFilterMadInstance instance,
			final MoogFilterMadUiDefinition uiDefinition )
	{
		super( uiDefinition.getCellSpan(), instance, uiDefinition );
	}

	public void sendFilterModeChange( final FrequencyFilterMode desiredFilterMode )
	{
		sendTemporalValueToInstance( MoogFilterIOQueueBridge.COMMAND_FILTER_MODE, desiredFilterMode.ordinal() );
	}

	public void sendCutoffChange( final float desiredCutoff )
	{
		sendTemporalValueToInstance( MoogFilterIOQueueBridge.COMMAND_CUTOFF, Float.floatToIntBits( desiredCutoff ) );
	}

	public void sendQChange( final float desiredBandwidth )
	{
		sendTemporalValueToInstance( MoogFilterIOQueueBridge.COMMAND_Q, Float.floatToIntBits( desiredBandwidth ) );
	}

	public SliderDisplayModel getCutoffSliderModel()
	{
		return cutoffSliderModel;
	}
}
