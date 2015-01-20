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

package uk.co.modularaudio.util.audio.gui.mad.rack;

import java.util.ArrayList;
import java.util.List;

import uk.co.modularaudio.util.audio.gui.mad.MadUiChannelInstance;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.mad.MadUiDefinition;
import uk.co.modularaudio.util.audio.gui.mad.MadUiInstance;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.table.RackModelTableSpanningContents;
import uk.co.modularaudio.util.table.Span;

public class RackComponent implements RackModelTableSpanningContents
{
	private String name = null;
	private MadInstance<?,?> ci = null;
	private MadUiDefinition<?, ?> uiDefinition = null;
	private MadUiInstance<?,?> cui = null;
	private List<RackComponentNameChangeListener> nameChangeListeners = new ArrayList<RackComponentNameChangeListener>();
	
	public RackComponent( String name, MadInstance<?,?> ci, MadUiInstance<?, ?> cui )
	{
		this.name = name;
		this.ci = ci;
		this.cui = cui;
		uiDefinition = cui.getUiDefinition();
	}

	public MadUiDefinition<?,?> getUiDefinition()
	{
		return cui.getUiDefinition();
	}
	
	public MadUiInstance<?, ?> getUiInstance()
	{
		return cui;
	}

	public MadUiControlInstance<?,?,?>[] getUiControlInstances()
	{
		return cui.getUiControlInstances();
	}

	public MadUiChannelInstance[] getUiChannelInstances()
	{
		return cui.getUiChannelInstances();
	}
	
	public String getComponentName()
	{
		return name;
	}

	@Override
	public Span getCellSpan()
	{
		return cui.getCellSpan();
	}

	public String toString()
	{
		return name;
	}

	public MadInstance<?,?> getInstance()
	{
		return ci;
	}

	public void receiveDisplayTick( ThreadSpecificTemporaryEventStorage guiTemporaryEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiFrameTime)
	{
		cui.receiveDisplayTick( guiTemporaryEventStorage, timingParameters, currentGuiFrameTime );
	}

	public boolean isDraggable()
	{
		return uiDefinition.isDraggable();
	}

	public void setComponentName( String newName )
	{
		name = newName;
		for( RackComponentNameChangeListener l : nameChangeListeners )
		{
			l.receiveNewName( newName );
		}
	}

	public void addNameChangeListener( RackComponentNameChangeListener nameChangeListener )
	{
		nameChangeListeners.add( nameChangeListener );
	}

	@Override
	public void removalFromTable()
	{
	}
	
	public void destroy()
	{
		nameChangeListeners.clear();
		ci = null;
		uiDefinition = null;
		cui = null;
	}
}
