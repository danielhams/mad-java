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

package test.uk.co.modularaudio.util.swing.mvc.combo;

import java.util.Collection;

import uk.co.modularaudio.util.exception.MAConstraintViolationException;
import uk.co.modularaudio.util.mvc.combo.idstring.IdStringComboItem;
import uk.co.modularaudio.util.mvc.combo.idstring.IdStringComboModel;

public class RealComboModel extends IdStringComboModel<IdStringComboItem>
{
//	private static Log log = LogFactory.getLog( RealComboModel.class.getName() );
	
	public RealComboModel( Collection<IdStringComboItem> items )
	{
		super( items );
	}
	
	public void addNewElement( IdStringComboItem newElement )
		throws MAConstraintViolationException
	{
		// First check we don't have it 
		String nid = newElement.getId();
		if( idToElementMap.get( nid ) != null )
		{
			String msg = "An element with unique ID " + nid + " already exists.";
			throw new MAConstraintViolationException( msg );
		}
		else
		{
			this.theList.add( newElement );
			this.idToElementMap.put( nid, newElement );
		}
	}

}
