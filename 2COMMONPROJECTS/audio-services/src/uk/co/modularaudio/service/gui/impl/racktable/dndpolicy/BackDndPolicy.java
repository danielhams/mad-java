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

package uk.co.modularaudio.service.gui.impl.racktable.dndpolicy;

import java.util.ArrayList;

import uk.co.modularaudio.service.gui.GuiRackBackActionListener;
import uk.co.modularaudio.service.gui.GuiService;
import uk.co.modularaudio.service.gui.impl.racktable.RackTableDndPolicy;
import uk.co.modularaudio.service.gui.impl.racktable.dndpolicy.rackdrag.DndRackDragDecorations;
import uk.co.modularaudio.service.gui.impl.racktable.dndpolicy.rackdrag.DndRackDragPolicy;
import uk.co.modularaudio.service.gui.impl.racktable.dndpolicy.wiredrag.DndWireDragDecorations;
import uk.co.modularaudio.service.gui.impl.racktable.dndpolicy.wiredrag.DndWireDragPolicy;
import uk.co.modularaudio.service.guicompfactory.AbstractGuiAudioComponent;
import uk.co.modularaudio.service.rack.RackService;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponentProperties;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackDataModel;
import uk.co.modularaudio.util.swing.dndtable.layeredpane.LayeredPaneDndTableCompoundPolicy;
import uk.co.modularaudio.util.swing.dndtable.layeredpane.LayeredPaneDndTablePolicy;

public class BackDndPolicy extends LayeredPaneDndTableCompoundPolicy<RackComponent, RackComponentProperties, AbstractGuiAudioComponent>
	implements RackTableDndPolicy
{
	private final ArrayList<LayeredPaneDndTablePolicy<RackComponent, RackComponentProperties, AbstractGuiAudioComponent>> policyList;

	public BackDndPolicy( final RackService rackService,
			final GuiService guiService,
			final RackDataModel dataModel,
			final DndRackDragDecorations rackDecorations,
			final DndWireDragDecorations wireDecorations,
			final GuiRackBackActionListener backActionListener )
	{
		policyList = new ArrayList<LayeredPaneDndTablePolicy<RackComponent, RackComponentProperties, AbstractGuiAudioComponent>>();

		policyList.add( new DndWireDragPolicy( rackService, dataModel, wireDecorations, backActionListener ) );
		policyList.add( new DndRackDragPolicy( rackService, guiService, dataModel, rackDecorations ) );
	}

	@Override
	public ArrayList<LayeredPaneDndTablePolicy<RackComponent, RackComponentProperties, AbstractGuiAudioComponent>> getPolicyList()
	{
		return policyList;
	}

	@Override
	public void setRackDataModel(final RackDataModel rackDataModel)
	{
		for( final LayeredPaneDndTablePolicy<RackComponent,RackComponentProperties,AbstractGuiAudioComponent> policy : policyList )
		{
			final RackTableDndPolicy realPolicy = (RackTableDndPolicy)policy;
			realPolicy.setRackDataModel( rackDataModel );
		}
	}

	@Override
	public void destroy()
	{
		for( final LayeredPaneDndTablePolicy<RackComponent,RackComponentProperties,AbstractGuiAudioComponent> policy : policyList )
		{
			final RackTableDndPolicy realPolicy = (RackTableDndPolicy)policy;
			realPolicy.destroy();
		}
	}
}
