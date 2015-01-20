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

package uk.co.modularaudio.util.swing.mvc.sliderdisplay;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import uk.co.modularaudio.util.math.MathFormatter;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayController;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayModel;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayModel.ValueChangeListener;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class SliderDisplayTextbox extends JPanel implements ValueChangeListener, ActionListener
{
//	private static Log log = LogFactory.getLog( SliderDisplayTextbox.class.getName() );
	
	private static final long serialVersionUID = -493606535649144832L;
	
	private SliderDisplayModel model = null;
	private SliderDisplayController controller = null;
	
	private JTextField textField = null;
	private JLabel unitsLabel = null;
	
	private int numSigPlaces = -1;
	private int numDecPlaces = -1;
	private String unitsStr = null;
	private int unitsStrLength = -1;

	private StringBuilder valueStringBuilder = null;

	public SliderDisplayTextbox( SliderDisplayModel model, SliderDisplayController controller, Color unitsColor, boolean opaque )
	{
		this.setOpaque( opaque );
		this.model = model;
		this.controller = controller;
		
		MigLayoutStringHelper lh = new MigLayoutStringHelper();
		lh.addLayoutConstraint( "insets 0, gap 0" );
		setLayout( lh.createMigLayout() );
		
		textField = new JTextField();
//		textField.setOpaque( opaque );
		
		this.numSigPlaces = model.getDisplayNumSigPlaces();
		this.numDecPlaces = model.getDisplayNumDecPlaces();
		this.unitsStr = model.getDisplayUnitsStr();
		this.unitsStrLength = unitsStr.length();
		int numCharactersForString = numSigPlaces + numDecPlaces;
		numCharactersForString = (numCharactersForString > 1 ? numCharactersForString - 1 : 1 );
//		log.debug("Setting num columns " + numCharactersForString );
		textField.setColumns( numCharactersForString );
		
		this.add( textField, " grow 0, shrink 0");
		
		if( unitsStrLength > 0 )
		{
			unitsLabel = new JLabel();
			unitsLabel.setOpaque( opaque );
			unitsLabel.setText( unitsStr );
			unitsLabel.setForeground(unitsColor);
//			unitsLabel.validate();
//			Dimension minimumSize = this.getPreferredSize();
//			unitsLabel.setMinimumSize( minimumSize );
			this.add( unitsLabel, "grow 0, shrink 0" );
		}
		valueStringBuilder = new StringBuilder( numCharactersForString );
		
		float curValue = model.getInitialValue();
		
		textField.setHorizontalAlignment( JTextField.RIGHT );
		
		setCurrentValueNoPropogate( curValue );
		
		model.addChangeListener( this );
		
		textField.addActionListener( this );
		
		this.validate();
		Dimension minimumSize = this.getPreferredSize();
		this.setMinimumSize( minimumSize );
	}
	
	private void setCurrentValueNoPropogate( float value )
	{
		valueStringBuilder.setLength( 0 );
		MathFormatter.fastFloatPrint( valueStringBuilder, value, numDecPlaces, false );
		textField.setText( valueStringBuilder.toString() );
	}

	@Override
	public void receiveValueChange( Object source, float newValue )
	{
		if( source != this )
		{
//			log.debug("Received value change from " + source.getClass().getSimpleName() + " with " + newValue );
			setCurrentValueNoPropogate( newValue );
		}
	}

	@Override
	public void actionPerformed( ActionEvent e )
	{
//		log.debug("ActionPerformed in textbox with source " + e.getSource().getClass().getSimpleName() );
		if( e.getSource() == textField )
		{
			String valueStr = textField.getText();
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
			catch(NumberFormatException nfe )
			{
			}

			float valueToSet;
			if( validValue )
			{
				String truncToPrecisionStr = MathFormatter.slowFloatPrint( valueAsFloat, model.getDisplayNumDecPlaces(), false );
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
}
