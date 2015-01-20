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

package uk.co.modularaudio.util.swing.mvc.combo;


import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.mvc.combo.ComboController;
import uk.co.modularaudio.util.mvc.combo.ComboItem;
import uk.co.modularaudio.util.mvc.combo.ComboModel;

public class ComboView<A extends ComboItem> extends JComboBox<A>
{
	protected static Log log = LogFactory.getLog( ComboView.class.getName() );
	
	private static final long serialVersionUID = -7604065062942877200L;

//	private ComboModel<A> cm = null;
	private ComboController<A> cc = null;
//	private ComboViewListCellRenderer<A> cr = null;
	
	protected ComboView( ComboViewListCellRenderer<A> cr )
	{
		this.setRenderer( cr );
	}
	
	public ComboView( ComboModel<A> cm, ComboController<A> cc, ComboViewListCellRenderer<A> cr )
	{
//		this.cm = cm;
		this.cc = cc;
//		this.cr = cr;
		
		this.setModelAndController( cm, cc );
		this.setRenderer( cr );
	}
	
	public void setModelAndController( final ComboModel<A> cm, final ComboController<A> cc )
	{
		this.cc = cc;
		ComboBoxModel<A> modelAdaptor = new ComboViewModelAdaptor<A>( cm, cc );
		
		this.setModel( modelAdaptor );
	}
	
	public void setModel( final ComboModel<A> cm )
	{
		ComboBoxModel<A> modelAdaptor = new ComboViewModelAdaptor<A>( cm, cc );
		this.setModel( modelAdaptor );
	}

}
