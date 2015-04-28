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

package uk.co.modularaudio.mads.base.djeq.ui;

import uk.co.modularaudio.mads.base.djeq.mu.DJEQMadDefinition;
import uk.co.modularaudio.mads.base.djeq.mu.DJEQMadInstance;
import uk.co.modularaudio.mads.base.djeq.ui.OneEqKill.ToggleListener;
import uk.co.modularaudio.util.audio.math.AudioMath;
import uk.co.modularaudio.util.mvc.displayrotary.RotaryDisplayModel.ValueChangeListener;


public class DJEQHighEQLane extends DJEQOneEQLane implements ValueChangeListener
{
	private static final long serialVersionUID = 4164721930545400401L;

	private final DJEQMadUiInstance uiInstance;

	public DJEQHighEQLane( final DJEQMadDefinition definition,
			final DJEQMadInstance instance,
			final DJEQMadUiInstance uiInstance,
			final int controlIndex )
	{
		super( definition, instance, uiInstance, controlIndex, "High" );

		this.uiInstance = uiInstance;

		getKnob().getModel().addChangeListener( this );

		getKill().setToggleListener( new ToggleListener()
		{

			@Override
			public void receiveToggleChange( final boolean previousValue, final boolean newValue )
			{
				uiInstance.setHighKilled( newValue );
			}
		} );

	}

	@Override
	public void receiveValueChange( final Object source, final float newValue )
	{
		final float actualValue = AudioMath.dbToLevelF( newValue );
		uiInstance.setHighAmp( actualValue );
	}
}
