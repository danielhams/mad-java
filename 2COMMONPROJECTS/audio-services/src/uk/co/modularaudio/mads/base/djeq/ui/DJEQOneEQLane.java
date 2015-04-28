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

import java.awt.Component;

import uk.co.modularaudio.mads.base.djeq.mu.DJEQMadDefinition;
import uk.co.modularaudio.mads.base.djeq.mu.DJEQMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacPanel;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class DJEQOneEQLane extends PacPanel
	implements IMadUiControlInstance<DJEQMadDefinition,DJEQMadInstance,DJEQMadUiInstance>
{
	private static final long serialVersionUID = -112893137724675530L;

//	private static Log log = LogFactory.getLog( DJEQOneEqLane.class.getName());

	private final OneEqKnob knob;

	private final OneEqKill killToggle;

	public DJEQOneEQLane( final DJEQMadDefinition definition,
			final DJEQMadInstance instance,
			final DJEQMadUiInstance uiInstance,
			final int controlIndex,
			final String eqLabel )
	{
		setOpaque( false );

		final MigLayoutStringHelper msh = new MigLayoutStringHelper();
//		msh.addLayoutConstraint( "debug" );
		msh.addLayoutConstraint( "fill" );
		msh.addLayoutConstraint( "insets 3" );
		msh.addLayoutConstraint( "gap 3" );

		msh.addRowConstraint( "[grow][]" );

		setLayout( msh.createMigLayout() );

		knob = new OneEqKnob( eqLabel );

		add( knob, "grow, wrap" );

		killToggle = new OneEqKill();

		add( killToggle, "alignx center, aligny top, width 75px, height 30px" );
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return false;
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters, final long currentGuiTime )
	{
	}

	@Override
	public Component getControl()
	{
		return this;
	}

	@Override
	public void destroy()
	{
	}

	protected void receiveToggle( final boolean newValue )
	{
	}

	public OneEqKnob getKnob()
	{
		return knob;
	}

	public OneEqKill getKill()
	{
		return killToggle;
	}

	@Override
	public String getControlValue()
	{
		final String knobValue = knob.getControlValue();
		final String killValue = killToggle.getControlValue();
		return knobValue + ":" + killValue;
	}

	@Override
	public void receiveControlValue( final String value )
	{
		if( value != null && value.length() > 0 )
		{
			final int colonIndex = value.indexOf( ':' );
			if( colonIndex > 0 )
			{
				final String knobValue = value.substring( 0, colonIndex );
				final String killValue = value.substring( colonIndex + 1 );
				knob.receiveControlValue( knobValue );
				killToggle.receiveControlValue( killValue );
			}
		}
	}
}
