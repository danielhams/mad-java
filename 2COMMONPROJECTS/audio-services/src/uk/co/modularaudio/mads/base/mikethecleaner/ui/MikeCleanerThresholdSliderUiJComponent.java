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

package uk.co.modularaudio.mads.base.mikethecleaner.ui;

import javax.swing.JComponent;

import uk.co.modularaudio.mads.base.mikethecleaner.mu.MikeCleanerMadDefinition;
import uk.co.modularaudio.mads.base.mikethecleaner.mu.MikeCleanerMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacSlider;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.math.AudioMath;

public class MikeCleanerThresholdSliderUiJComponent extends PacSlider
		implements
		IMadUiControlInstance<MikeCleanerMadDefinition, MikeCleanerMadInstance, MikeCleanerMadUiInstance>
{
	public static final float MAX_FLOOR_DB = 128.0f;

	private static final long serialVersionUID = 6068897521037173787L;

	private final MikeCleanerMadUiInstance uiInstance;

	public MikeCleanerThresholdSliderUiJComponent(
			final MikeCleanerMadDefinition definition,
			final MikeCleanerMadInstance instance,
			final MikeCleanerMadUiInstance uiInstance,
			final int controlIndex )
	{
		this.uiInstance = uiInstance;
		this.setOpaque( false );
		setFont( this.getFont().deriveFont( 9f ) );
		this.setPaintLabels( true );
		this.setMinimum( -1000 );
		this.setMaximum( 0 );
		// Default value
		this.setValue( -2 );
	}

	@Override
	public JComponent getControl()
	{
		return this;
	}

	private void passChangeToInstanceData( final int value )
	{
		// Convert it into a float
		final float calculatedDb = (value)/1000.0f * MAX_FLOOR_DB;
		final float valueToPass = (float)AudioMath.dbToLevel( calculatedDb );
		uiInstance.sendThresholdChange( valueToPass );
	}

	@Override
	public void doDisplayProcessing(final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime)
	{
		// log.debug("Received display tick");
	}

	@Override
	public void receiveControlValue( final String strValue )
	{
		super.receiveControlValue( strValue );
		final float initialValue = this.getValue();
		uiInstance.sendThresholdChange( initialValue );
	}

	@Override
	public void processValueChange( final int previousValue, final int newValue )
	{
		if( previousValue != newValue )
		{
			passChangeToInstanceData( newValue );
		}
	}

	@Override
	public void destroy()
	{
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return false;
	}
}
