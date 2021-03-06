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

package uk.co.modularaudio.util.mvc.combo;

import java.util.Collection;

public interface ComboModel<A extends ComboItem>
{
	public int getNumElements();
	public A getElementAt( int i );
	public int getSelectedItemIndex();
	public A getSelectedElement();
	public A getElementById( String elementId );
	public int getItemIndex( A item );

	// Listening for changes to things in the view
	public void addListener( ComboModelListener<A> listener );
	public void removeListener( ComboModelListener<A> listener );
	public Collection<ComboModelListener<A>> getListeners();
}
