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

package uk.co.modularaudio.mads.base.interptester.ui;

import java.awt.Color;

import javax.swing.JComponent;
import javax.swing.JPanel;

import uk.co.modularaudio.mads.base.interptester.mu.InterpTesterMadDefinition;
import uk.co.modularaudio.mads.base.interptester.mu.InterpTesterMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacLabel;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class InterpTesterModelChoiceUiJComponent extends JPanel
	implements IMadUiControlInstance<InterpTesterMadDefinition, InterpTesterMadInstance, InterpTesterMadUiInstance>
{
	private static final long serialVersionUID = 28004477652791854L;

	private final ModelComboUiJComponent modelCombo;

	public InterpTesterModelChoiceUiJComponent(
			final InterpTesterMadDefinition definition,
			final InterpTesterMadInstance instance,
			final InterpTesterMadUiInstance uiInstance,
			final int controlIndex )
	{
		super();
		this.setOpaque( false );

		final MigLayoutStringHelper msh = new MigLayoutStringHelper();
		msh.addLayoutConstraint( "fill" );
		msh.addLayoutConstraint( "insets 0" );
		msh.addColumnConstraint( "[][grow]" );
		setLayout( msh.createMigLayout() );

		final PacLabel modelLabel = new PacLabel("Model:");
		modelLabel.setForeground( Color.BLACK );
		add( modelLabel, "" );

		modelCombo = new ModelComboUiJComponent( uiInstance );
		add( modelCombo, "grow");
	}

	@Override
	public JComponent getControl()
	{
		return this;
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime)
	{
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

	@Override
	public String getControlValue()
	{
		return modelCombo.getControlValue();
	}

	@Override
	public void receiveControlValue( final String value )
	{
		modelCombo.receiveControlValue( value );
	}
}
