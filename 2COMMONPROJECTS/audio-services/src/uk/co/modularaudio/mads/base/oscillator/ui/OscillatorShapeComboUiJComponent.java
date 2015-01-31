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

package uk.co.modularaudio.mads.base.oscillator.ui;

import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;

import uk.co.modularaudio.mads.base.oscillator.mu.OscillatorMadDefinition;
import uk.co.modularaudio.mads.base.oscillator.mu.OscillatorMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacComboBox;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.oscillatortable.OscillatorWaveShape;

public class OscillatorShapeComboUiJComponent extends PacComboBox<String>
		implements
		IMadUiControlInstance<OscillatorMadDefinition, OscillatorMadInstance, OscillatorMadUiInstance>
{
	private static final long serialVersionUID = 28004477652791854L;
//	private static Log log = LogFactory.getLog( OscillatorShapeComboUiJComponent.class.getName() );

	private final OscillatorMadUiInstance uiInstance;

	private final Map<OscillatorWaveShape, String> waveShapeToNameMap = new HashMap<OscillatorWaveShape, String>();
	private final Map<String, OscillatorWaveShape> waveNameToShapeMap = new HashMap<String, OscillatorWaveShape>();

	public OscillatorShapeComboUiJComponent( final OscillatorMadDefinition definition,
			final OscillatorMadInstance instance,
			final OscillatorMadUiInstance uiInstance,
			final int controlIndex )
	{

		this.uiInstance = uiInstance;

		this.setOpaque( false );

		waveNameToShapeMap.put( "Saw", OscillatorWaveShape.SAW );
		waveNameToShapeMap.put( "Sine", OscillatorWaveShape.SINE );
		waveNameToShapeMap.put( "Square", OscillatorWaveShape.SQUARE );
		waveNameToShapeMap.put( "Triangle", OscillatorWaveShape.TRIANGLE );
		waveNameToShapeMap.put( "Test1", OscillatorWaveShape.TEST1 );
		for( final String waveName : waveNameToShapeMap.keySet() )
		{
//			log.debug("Adding " + waveName + " to the map");
			waveShapeToNameMap.put( waveNameToShapeMap.get( waveName ), waveName );
		}

		final DefaultComboBoxModel<String> cbm = new DefaultComboBoxModel<String>();
		final OscillatorWaveShape[] waveShapes = OscillatorWaveShape.values();
		for( final OscillatorWaveShape waveShape : waveShapes )
		{
			final String waveName = waveShapeToNameMap.get( waveShape );
			if( waveName != null )
			{
				cbm.addElement( waveName );
			}
		}
		this.setModel( cbm );

		setFont( this.getFont().deriveFont( 9f ) );

		this.setSelectedItem( "Sine" );
	}

	@Override
	public JComponent getControl()
	{
		return this;
	}

	@Override
	public void doDisplayProcessing(final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime)
	{
		// log.debug("Received display tick");
	}

	@Override
	protected void receiveIndexUpdate( final int previousIndex, final int newIndex )
	{
		if( previousIndex != newIndex )
		{
			final String name = (String) getSelectedItem();
			final OscillatorWaveShape waveShape = waveNameToShapeMap.get( name );
			uiInstance.sendWaveShape( waveShape );
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
