/**
 *
 * Copyright (C) 2015 -> 2018 - Daniel Hams, Modular Audio Limited
 *                              daniel.hams@gmail.com
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

package uk.co.modularaudio.mads.base.controllerhistogram.ui;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import uk.co.modularaudio.mads.base.controllerhistogram.mu.ControllerHistogramMadDefinition;
import uk.co.modularaudio.mads.base.controllerhistogram.mu.ControllerHistogramMadInstance;
import uk.co.modularaudio.mads.base.controllerhistogram.util.ReceivedLight;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.lwtc.LWTCLabel;

public class ControllerHistogramDataReceivedLightUiJComponent
	implements IMadUiControlInstance<ControllerHistogramMadDefinition, ControllerHistogramMadInstance, ControllerHistogramMadUiInstance>
{
	private final JPanel lightPanel = new JPanel();
	private final ReceivedLight receivedLight = new ReceivedLight();

	public ControllerHistogramDataReceivedLightUiJComponent(
			final ControllerHistogramMadDefinition definition,
			final ControllerHistogramMadInstance instance,
			final ControllerHistogramMadUiInstance uiInstance,
			final int controlIndex )
	{
		uiInstance.addNoteReceivedListener( receivedLight );

		final MigLayoutStringHelper msh = new MigLayoutStringHelper();
//		msh.addLayoutConstraint( "debug" );
		msh.addLayoutConstraint( "gap 4" );
		msh.addLayoutConstraint( "insets 2" );
		msh.addLayoutConstraint( "fill" );

		lightPanel.setLayout( msh.createMigLayout() );
		lightPanel.setOpaque( true );
		lightPanel.setBackground( LWTCControlConstants.CONTROL_FLAT_BACKGROUND );

		lightPanel.add( receivedLight, "center" );

		final LWTCLabel label = new LWTCLabel( LWTCControlConstants.STD_LABEL_COLOURS, "Control Moved" );
		label.setFont( LWTCControlConstants.LABEL_FONT );
		label.setBorder( BorderFactory.createEmptyBorder() );
		lightPanel.add( label, "grow" );
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return true;
	}

	@Override
	public String getControlValue()
	{
		return "";
	}

	@Override
	public void receiveControlValue( final String value )
	{
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage ,
			final MadTimingParameters timingParameters ,
			final int U_currentGuiTime , int framesSinceLastTick  )
	{
		receivedLight.doDisplayProcessing( U_currentGuiTime );
	}

	@Override
	public Component getControl()
	{
		return lightPanel;
	}

	@Override
	public void destroy()
	{
	}
}
