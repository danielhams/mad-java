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

package uk.co.modularaudio.mads.base.bandlimitedoscillator.ui;

import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.bandlimitedoscillator.mu.BandLimitedOscillatorMadDefinition;
import uk.co.modularaudio.mads.base.bandlimitedoscillator.mu.BandLimitedOscillatorMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacComboBox;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.oscillatortable.OscillatorWaveShape;

public class BandLimitedOscillatorShapeComboUiJComponent extends PacComboBox<String>
		implements
		IMadUiControlInstance<BandLimitedOscillatorMadDefinition, BandLimitedOscillatorMadInstance, BandLimitedOscillatorMadUiInstance>
{
	private static final long serialVersionUID = 28004477652791854L;
	
	private static Log log = LogFactory.getLog( BandLimitedOscillatorShapeComboUiJComponent.class.getName() );

	private BandLimitedOscillatorMadUiInstance uiInstance = null;

	private Map<String, OscillatorWaveShape> waveNameToShapeMap = new HashMap<String, OscillatorWaveShape>();

	public BandLimitedOscillatorShapeComboUiJComponent(
			BandLimitedOscillatorMadDefinition definition,
			BandLimitedOscillatorMadInstance instance,
			BandLimitedOscillatorMadUiInstance uiInstance,
			int controlIndex )
	{
		this.uiInstance = uiInstance;

		this.setOpaque( false );

		waveNameToShapeMap.put( "Saw", OscillatorWaveShape.SAW );
		waveNameToShapeMap.put( "Sine", OscillatorWaveShape.SINE );
		waveNameToShapeMap.put( "Square", OscillatorWaveShape.SQUARE );
		waveNameToShapeMap.put( "Triangle", OscillatorWaveShape.TRIANGLE );
		waveNameToShapeMap.put( "Test1", OscillatorWaveShape.TEST1 );
		waveNameToShapeMap.put( "Juno", OscillatorWaveShape.JUNO );

		DefaultComboBoxModel<String> cbm = new DefaultComboBoxModel<String>();
		for (String waveName : waveNameToShapeMap.keySet())
		{
			cbm.addElement( waveName );
		}
		this.setModel( cbm );

		setFont( this.getFont().deriveFont( 9f ) );

		this.setSelectedItem( "Saw" );
	}

	@Override
	public JComponent getControl()
	{
		return this;
	}

	@Override
	public void doDisplayProcessing( ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime)
	{
		// log.debug("Received display tick");
	}

	@Override
	protected void receiveIndexUpdate( int previousIndex, int newIndex )
	{
		if( previousIndex != newIndex )
		{
			String name = (String) getSelectedItem();
			OscillatorWaveShape waveShape = waveNameToShapeMap.get( name );
			uiInstance.sendWaveChoice( waveShape );
			log.debug("Sending wave choice to instance");
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
