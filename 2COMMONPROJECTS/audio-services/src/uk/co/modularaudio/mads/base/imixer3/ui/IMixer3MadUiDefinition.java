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

package uk.co.modularaudio.mads.base.imixer3.ui;

import uk.co.modularaudio.mads.base.imixer3.mu.IMixer3MadDefinition;
import uk.co.modularaudio.mads.base.imixer3.mu.IMixer3MadInstance;
import uk.co.modularaudio.mads.base.imixern.mu.MixerNInstanceConfiguration;
import uk.co.modularaudio.mads.base.imixern.ui.MixerNMadUiDefinition;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.image.ImageFactory;
import uk.co.modularaudio.util.table.Span;

public class IMixer3MadUiDefinition extends MixerNMadUiDefinition<IMixer3MadDefinition, IMixer3MadInstance, IMixer3MadUiInstance>
{
	private final static String MIXER3_IMAGE_PREFIX = "mixer3";
	final static Span SPAN = new Span( 2, 4 );

	public IMixer3MadUiDefinition( final BufferedImageAllocator bia,
			final IMixer3MadDefinition definition,
			final ComponentImageFactory cif,
			final String imageRoot )
		throws DatastoreException
	{
		this( bia,
				definition,
				cif,
				imageRoot,
				instanceConfigToUiConfig( IMixer3MadDefinition.INSTANCE_CONFIGURATION ) );
	}

	private static IMixer3UiInstanceConfiguration instanceConfigToUiConfig(
			final MixerNInstanceConfiguration instanceConfiguration )
	{
		return new IMixer3UiInstanceConfiguration( instanceConfiguration );
	}

	private IMixer3MadUiDefinition( final BufferedImageAllocator bia,
			final IMixer3MadDefinition definition,
			final ImageFactory cif,
			final String imageRoot,
			final IMixer3UiInstanceConfiguration uiConfiguration ) throws DatastoreException
	{
		super( bia,
				definition,
				cif,
				imageRoot,
				MIXER3_IMAGE_PREFIX,
				SPAN,
				IMixer3MadUiInstance.class,
				uiConfiguration.getChanIndexes(),
				uiConfiguration.getChanPosis(),
				uiConfiguration.getControlNames(),
				uiConfiguration.getControlTypes(),
				uiConfiguration.getControlClasses(),
				uiConfiguration.getControlBounds() );
	}
}
