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

package uk.co.modularaudio.service.gui.valueobjects.mvc;

import java.util.Dictionary;
import java.util.Hashtable;

import javax.swing.JLabel;

import org.apache.mahout.math.list.IntArrayList;

import uk.co.modularaudio.service.userpreferences.mvc.controllers.BufferSizeSliderMVCController;
import uk.co.modularaudio.util.mvc.intslider.impl.BasicIntegerSliderModel;
import uk.co.modularaudio.util.swing.mvc.intslider.BasicIntegerSliderView;

public class UserPreferencesBufferSizeMVCView extends BasicIntegerSliderView
{
	private static final long serialVersionUID = 6215602643416123463L;

	public UserPreferencesBufferSizeMVCView( BasicIntegerSliderModel ism,
			BufferSizeSliderMVCController isc )
	{
		super( ism, isc );
		this.setMinimum( 1 );
		this.setMaximum( BufferSizeSliderMVCController.bufferSizeToModelIndexMap.size() );
//		this.setMinorTickSpacing( 256 );
		this.setMajorTickSpacing( 1 );
		this.setPaintTicks( true );
		Dictionary<Integer,JLabel> bufferSizeTickLabels = new Hashtable<Integer,JLabel>();
		
		IntArrayList intArrayList = BufferSizeSliderMVCController.bufferSizeToModelIndexMap.keys();
		for( int i = 0 ; i < intArrayList.size() ; i++ )
		{
			int bufferSize = intArrayList.get( i );
			int modelIndex = BufferSizeSliderMVCController.bufferSizeToModelIndexMap.get( bufferSize );

			addOneLabel( bufferSizeTickLabels, modelIndex, bufferSize + "");
		}
		
		this.setLabelTable( bufferSizeTickLabels );
		
		this.setPaintLabels( true );
		this.setSnapToTicks( true );
	}

	private void addOneLabel( Dictionary<Integer, JLabel> bufferSizeTickLabels, int size, String labelStr )
	{
		JLabel label = new JLabel( labelStr);
		bufferSizeTickLabels.put( size, label );
	}
}
