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

package uk.co.modularaudio.mads.base.envelope.ui;

import java.awt.Component;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.envelope.mu.EnvelopeMadDefinition;
import uk.co.modularaudio.mads.base.envelope.mu.EnvelopeMadInstance;
import uk.co.modularaudio.mads.base.envelope.ui.WaveTableChoiceAttackCombo.WaveTableChoiceEnum;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacPanel;
import uk.co.modularaudio.util.audio.gui.wavetablecombo.WaveTableComboController;
import uk.co.modularaudio.util.audio.gui.wavetablecombo.WaveTableComboModel;
import uk.co.modularaudio.util.audio.lookuptable.LookupTable;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class EnvelopeAttackWaveComboUiJComponent extends PacPanel
	implements IMadUiControlInstance<EnvelopeMadDefinition, EnvelopeMadInstance, EnvelopeMadUiInstance>, WaveTableChoiceChangeReceiver
{
	private static Log log = LogFactory.getLog( EnvelopeAttackWaveComboUiJComponent.class.getName() );

	private static final long serialVersionUID = 9155384260643536860L;

	private final EnvelopeMadUiInstance uiInstance;

	private final WaveTableChoiceAttackCombo choiceCombo;
	private final WaveTableComboModel choiceModel;
	private final WaveTableComboController choiceController;

	public EnvelopeAttackWaveComboUiJComponent( final EnvelopeMadDefinition definition,
			final EnvelopeMadInstance instance,
			final EnvelopeMadUiInstance uiInstance,
			final int controlIndex )
	{
		this.setOpaque( false );
		final MigLayoutStringHelper lh = new MigLayoutStringHelper();
//		lh.addLayoutConstraint( "debug" );
		lh.addLayoutConstraint( "insets 0" );
		lh.addLayoutConstraint( "fill" );
		this.setLayout( lh.createMigLayout() );
		this.uiInstance = uiInstance;

		choiceCombo = new WaveTableChoiceAttackCombo( this );
		choiceModel = choiceCombo.getWTModel();
		choiceController = choiceCombo.getWTController();
		try
		{
			choiceController.setSelectedElementById( WaveTableChoiceAttackCombo.WaveTableChoiceEnum.LINEAR.toString() );
		}
		catch (final Exception e)
		{
		}

		this.add( choiceCombo, "grow" );
	}

	@Override
	public String getControlValue()
	{
		return choiceModel.getSelectedElement().getId();
	}

	@Override
	public void receiveControlValue( final String value )
	{
		try
		{
			choiceController.setSelectedElementById( value );
		}
		catch (final Exception e)
		{
			final String msg = "Unable to set desired wave type " + value + ": " + e.toString();
			log.error( msg, e );
		}
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime )
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

	@Override
	public void receiveChangedWaveTable( final LookupTable waveTable )
	{
//		log.debug("Received combo choice change: " + waveTable.toString() );
		final String selectedWaveTableId = choiceModel.getSelectedElement().getId();
		final WaveTableChoiceAttackCombo.WaveTableChoiceEnum enumValue = WaveTableChoiceEnum.valueOf( selectedWaveTableId );

		uiInstance.setAttackWaveChoice( enumValue );
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return false;
	}
}
