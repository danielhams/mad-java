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

package uk.co.modularaudio.util.swing.mvc.rotarydisplay;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import uk.co.modularaudio.util.math.MathFormatter;
import uk.co.modularaudio.util.mvc.displayrotary.RotaryDisplayController;
import uk.co.modularaudio.util.mvc.displayrotary.RotaryDisplayModel;
import uk.co.modularaudio.util.mvc.displayrotary.RotaryDisplayModel.ValueChangeListener;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.lwtc.LWTCLabel;
import uk.co.modularaudio.util.swing.lwtc.LWTCSpeedyUpdateTextField;

public class RotaryDisplayTextbox extends JPanel implements ValueChangeListener, ActionListener
{
//	private static Log log = LogFactory.getLog( SliderDisplayTextbox.class.getName() );

	private static final long serialVersionUID = -493606535649144832L;

	private final RotaryDisplayModel model;
	private final RotaryDisplayController controller;

	private final LWTCSpeedyUpdateTextField textField;
	private LWTCLabel unitsLabel;

	private int numSigPlaces;
	private int numDecPlaces;
	private String unitsStr;
	private int unitsStrLength;
	private int numCharactersForString;

	public RotaryDisplayTextbox( final RotaryDisplayModel model,
			final RotaryDisplayController controller,
			final RotaryViewColors colours,
			final boolean opaque )
	{
		this.setOpaque( opaque );
		this.model = model;
		this.controller = controller;
		this.setBackground( colours.bgColor );
		this.setForeground( colours.unitsColor );

		final MigLayoutStringHelper lh = new MigLayoutStringHelper();
//		lh.addLayoutConstraint( "debug" );
		lh.addLayoutConstraint( "insets 0" );
		lh.addLayoutConstraint( "gap 0" );
		setLayout( lh.createMigLayout() );

		textField = new LWTCSpeedyUpdateTextField();
		// Never see through
		textField.setOpaque( true );
		textField.setBackground( colours.textboxBgColor );
		textField.setForeground( colours.textboxFgColor );
		textField.setSelectionColor( colours.selectionColor );
		textField.setSelectedTextColor( colours.selectedTextColor );

		extractModelVars( model );

		completeModelSetup( model );

		this.add( textField, " grow 0, shrink 0");

		if( unitsStrLength > 0 )
		{
			unitsLabel = new LWTCLabel( unitsStr );
			unitsLabel.setOpaque( opaque );
			unitsLabel.setBackground( colours.bgColor );
			unitsLabel.setForeground( colours.labelColor );
			unitsLabel.setFont( LWTCControlConstants.LABEL_SMALL_FONT );
			unitsLabel.setVerticalAlignment( SwingConstants.CENTER );
			unitsLabel.setBorder( new EmptyBorder( 2,2,2,2 ) );
			this.add( unitsLabel, "grow 0, shrink 0" );
		}

		textField.setHorizontalAlignment( JTextField.RIGHT );

		final float curValue = model.getInitialValue();
		setCurrentValueNoPropogate( curValue );

		model.addChangeListener( this );

		textField.addActionListener( this );

		this.validate();
		final Dimension minimumSize = this.getPreferredSize();
		this.setMinimumSize( minimumSize );
	}

	private void extractModelVars( final RotaryDisplayModel model )
	{
		this.numSigPlaces = model.getDisplayNumSigPlaces();
		this.numDecPlaces = model.getDisplayNumDecPlaces();
		this.unitsStr = model.getDisplayUnitsStr();
		this.unitsStrLength = unitsStr.length();
		numCharactersForString = numSigPlaces + numDecPlaces;
		numCharactersForString = (numCharactersForString > 1 ? numCharactersForString - 1 : 1 );
	}

	private void completeModelSetup( final RotaryDisplayModel model )
	{
//		log.debug("Setting num columns " + numCharactersForString );
		textField.setColumns( numCharactersForString );

		final float curValue = model.getInitialValue();
		setCurrentValueNoPropogate( curValue );
	}

	private void setCurrentValueNoPropogate( final float value )
	{
		final String newText = MathFormatter.fastFloatPrint( value, numDecPlaces, false );
		textField.setText( newText );
	}

	@Override
	public void receiveValueChange( final Object source, final float newValue )
	{
		if( source != this )
		{
//			log.debug("Received value change from " + source.getClass().getSimpleName() + " with " + newValue );
			setCurrentValueNoPropogate( newValue );
		}
	}

	@Override
	public void actionPerformed( final ActionEvent e )
	{
//		log.debug("ActionPerformed in textbox with source " + e.getSource().getClass().getSimpleName() );
		if( e.getSource() == textField )
		{
			final String valueStr = textField.getText();
			boolean validValue = false;
			float valueAsFloat = 0.0f;
			try
			{
				valueAsFloat = Float.parseFloat( valueStr );
				if( !Float.isInfinite( valueAsFloat ) )
				{
					if( valueAsFloat >= model.getMinValue() &&
							valueAsFloat <= model.getMaxValue() )
					{
						validValue = true;
					}
				}
			}
			catch(final NumberFormatException nfe )
			{
			}

			float valueToSet;
			if( validValue )
			{
				final String truncToPrecisionStr = MathFormatter.fastFloatPrint( valueAsFloat, model.getDisplayNumDecPlaces(), false );
				valueToSet = Float.parseFloat( truncToPrecisionStr );
			}
			else
			{
				valueToSet = model.getValue();
			}
			setCurrentValueNoPropogate( valueToSet );

			controller.setValue( this, valueToSet );

		}
	}

//	public void changeModel( final RotaryDisplayModel newModel )
//	{
//		model.removeChangeListener( this );
//		this.model = newModel;
//		extractModelVars( newModel );
//		completeModelSetup( newModel );
//
//		if( unitsStrLength > 0 )
//		{
//			unitsLabel.setText( unitsStr );
//		}
//
//		model.addChangeListener( this );
//		validate();
//		final Dimension minimumSize = this.getPreferredSize();
//		this.setMinimumSize( minimumSize );
//	}
}
