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

package uk.co.modularaudio.service.gui.impl.guirackpanel.sub;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;

import uk.co.modularaudio.util.audio.mad.MadClassification.ReleaseState;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadDefinitionListModel;

public class MadDefinitionComboBox extends JComboBox<MadDefinition<?,?>>
{
	public class ComboBoxCellRenderer extends DefaultListCellRenderer
	{
		private static final long serialVersionUID = -5614583177714511340L;

		@Override
		public Component getListCellRendererComponent(@SuppressWarnings("rawtypes") final JList list, final Object value,
				final int index, final boolean isSelected, final boolean cellHasFocus)
		{
			if( value != null )
			{
				final MadDefinition<?,?> realValue = (MadDefinition<?,?>)value;
				final StringBuilder visibleStr = new StringBuilder();
				visibleStr.append( realValue.getName() );
				final ReleaseState rs = realValue.getClassification().getState();
				switch( rs )
				{
					case ALPHA:
					{
						visibleStr.append(" (alpha)");
						break;
					}
					case BETA:
					{
						visibleStr.append(" (beta)");
						break;
					}
					default:
					{
						break;
					}
				}

				return super.getListCellRendererComponent(list, visibleStr.toString(), index, isSelected, cellHasFocus);
			}
			else
			{
				return super.getListCellRendererComponent(list, null, index, isSelected, cellHasFocus);
			}
		}
	}

	private static final long serialVersionUID = -1465726065333593273L;

	public MadDefinitionComboBox( final MadDefinitionListModel madDefinitions )
	{
		super( madDefinitions );
		// Now register our custom cell renderer which just uses the name from inside the type
		this.setRenderer( new ComboBoxCellRenderer() );
	}
}
