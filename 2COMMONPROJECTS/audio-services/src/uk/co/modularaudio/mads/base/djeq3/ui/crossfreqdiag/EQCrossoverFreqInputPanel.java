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

package uk.co.modularaudio.mads.base.djeq3.ui.crossfreqdiag;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.miginfocom.swing.MigLayout;
import uk.co.modularaudio.util.swing.dialog.message.MessageDialog;

public class EQCrossoverFreqInputPanel extends JPanel implements ActionListener
{
	private static Log LOG = LogFactory.getLog( EQCrossoverFreqInputPanel.class.getName() );

	private static final long serialVersionUID = -1201231298787690939L;

	protected final static EQCrossoverDialColours DC = new EQCrossoverDialColours();

	private final EQCrossoverFreqDialog parentDialog;

	private EQCrossoverFreqDialogCallback callback;

	private final EQCrossoverMinDialUiJComponent minDial;
	private final EQCrossoverMaxDialUiJComponent maxDial;
	private final EQCrossoverPresetChoiceUiJComponent presetChoice;

	private final JButton okButton = new JButton("OK");
	private final JButton cancelButton = new JButton("Cancel");

	public EQCrossoverFreqInputPanel( final EQCrossoverFreqDialog parentDialog )
	{
		this.setOpaque( true );
//		this.setBackground( DC.bgColor );
		this.parentDialog = parentDialog;
//		okButton.setMaximumSize( new Dimension( 80, 40 ) );
		okButton.addActionListener( this );
		cancelButton.addActionListener( this );

		this.minDial = new EQCrossoverMinDialUiJComponent( this );
		this.maxDial = new EQCrossoverMaxDialUiJComponent( this );
		this.presetChoice = new EQCrossoverPresetChoiceUiJComponent( this );

		parentDialog.getRootPane().setDefaultButton( okButton );

		final MigLayout migLayout = new MigLayout( "fill, "
				+ "insets " + MessageDialog.DEFAULT_BORDER_WIDTH, "",
				"[growprio 100][growprio 0]");
		this.setLayout( migLayout );

		final JPanel dialPanel = new JPanel();
		dialPanel.setBorder( BorderFactory.createLineBorder( DC.knobOutlineColor ) );
		dialPanel.setBackground( DC.bgColor );
		dialPanel.setLayout( new MigLayout("fill, insets 5, gap 5") );
		dialPanel.add( minDial.getControl(), "cell 0 0, align right" );
		dialPanel.add( maxDial.getControl(), "cell 1 0, align right" );
		dialPanel.add( presetChoice, "cell 0 1, spanx 2, grow" );

		this.add( dialPanel, "grow, spanx 3, wrap" );
		this.add( okButton, "grow 0, align left");
		this.add( cancelButton, "grow 0, spanx 2, align right" );
	}

	public void setValues( final String message,
			final String initialValue,
			final EQCrossoverFreqDialogCallback callback )
	{
		this.callback = callback;
		final String[] freqValues = initialValue.split( ":" );
		if( freqValues.length == 2 )
		{
			try
			{
				final float lowFreq = Float.parseFloat( freqValues[0] );
				final float highFreq = Float.parseFloat( freqValues[1] );
				minDial.receiveControlValue( Float.toString(lowFreq) );
				maxDial.receiveControlValue( Float.toString(highFreq) );
			}
			catch( final NumberFormatException nfe )
			{
			}
		}
	}

	public String getValue()
	{
		return minDial.getControlValue() + ":" + maxDial.getControlValue();
	}

	@Override
	public void actionPerformed( final ActionEvent e )
	{
//		log.debug("Received action performed: " + e.toString() );
		final Object source = e.getSource();
		if( source == okButton )
		{
			parentDialog.setVisible( false );
			if( callback != null )
			{
				callback.dialogClosedReceiveValues( getValue() );
			}
		}
		else if( source == cancelButton )
		{
			parentDialog.setVisible( false );
			callback.dialogClosedReceiveValues( null );
		}
	}

	public void doFocusSetting()
	{
		minDial.grabFocus();
	}

	public void receiveMinFreqChange( final float minFreq )
	{
		maxDial.receiveMinFreqChange( minFreq );
	}

	public void receiveMaxFreqChange( final float maxFreq )
	{
		minDial.receiveMaxFreqChange( maxFreq );
	}

	public void receiveMinMaxFreqChange( final float lowFreq, final float highFreq )
	{
		// Reset them to min/max so there's no "blocking"
		minDial.receiveControlValue( Float.toString( 0.0f ) );
		maxDial.receiveControlValue( Float.toString( EQCrossoverMaxDialUiJComponent.DEFAULT_FREQ_MAX ) );
		minDial.receiveControlValue( Float.toString(lowFreq) );
		maxDial.receiveControlValue( Float.toString(highFreq) );
	}
}
