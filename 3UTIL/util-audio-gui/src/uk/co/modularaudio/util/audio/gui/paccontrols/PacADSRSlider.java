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

package uk.co.modularaudio.util.audio.gui.paccontrols;

import java.awt.Color;

import javax.swing.JPanel;

import uk.co.modularaudio.util.mvc.displayslider.SimpleSliderIntToFloatConverter;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayController;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayModel;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayModel.ValueChangeListener;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;
import uk.co.modularaudio.util.swing.mvc.sliderdisplay.SliderDisplayView;
import uk.co.modularaudio.util.swing.mvc.sliderdisplay.SliderDisplayView.DisplayOrientation;
import uk.co.modularaudio.util.swing.mvc.sliderdisplay.SliderDisplayView.SatelliteOrientation;

public abstract class PacADSRSlider extends JPanel implements ValueChangeListener
{
	private static final long serialVersionUID = 2783504281404548759L;
	
	protected SliderDisplayView view;
	protected SliderDisplayModel model;
	protected SliderDisplayController controller;

	public PacADSRSlider( float minValue,
			float maxValue,
			float initialValue,
			String unitsStr,
			SatelliteOrientation labelOrientation,
			DisplayOrientation displayOrientation,
			SatelliteOrientation textboxOrientation,
			String labelText,
			Color labelColor,
			Color unitsColor,
			boolean opaque )
	{
		this.setOpaque( opaque );
		this.model = new SliderDisplayModel( minValue,
				maxValue,
				initialValue,
				1000,
				100,
				new SimpleSliderIntToFloatConverter(),
				3,
				2,
				unitsStr );

		this.controller = new SliderDisplayController( model );
		this.view = new SliderDisplayView( model, controller, labelOrientation, displayOrientation, textboxOrientation, labelText, labelColor, unitsColor, opaque );
		
		MigLayoutStringHelper lh = new MigLayoutStringHelper();
		lh.addLayoutConstraint( "fill" );
		lh.addLayoutConstraint( "insets 0" );
		lh.addLayoutConstraint( "gap 0" );
		this.setLayout( lh.createMigLayout() );
		this.add( view, "" );
		
		// Finally subscribe to the mode so that derived classes can handle the value change
		model.addChangeListener( this );
		controller.setValue( this, 0 );
		controller.setValue( this, model.getInitialValue() );
	}

	@Override
	public abstract void receiveValueChange( Object source, float newValue );
	
	public boolean needsDisplayProcessing()
	{
		return false;
	}
}
