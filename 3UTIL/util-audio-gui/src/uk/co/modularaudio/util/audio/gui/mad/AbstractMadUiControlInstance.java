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

package uk.co.modularaudio.util.audio.gui.mad;

import java.awt.Component;

import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public abstract class AbstractMadUiControlInstance
	<D extends MadDefinition<D,I>,
	I extends MadInstance<D,I>,
	U extends AbstractMadUiInstance<D, I>>
	implements IMadUiControlInstance<D, I, U>
{
	protected final U componentUiInstance;
	protected final MadUiControlDefinition<D, I, U> definition;

	public AbstractMadUiControlInstance( final U uiInstance, final MadUiControlDefinition<D, I, U> definition )
	{
		this.componentUiInstance = uiInstance;
		this.definition = definition;
	}

	@Override
	public abstract String getControlValue();

	@Override
	public abstract void receiveControlValue( String value );

	@Override
	public abstract boolean needsDisplayProcessing();

	@Override
	public abstract void doDisplayProcessing( ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime );

	@Override
	public abstract Component getControl();

	public MadUiControlDefinition<D, I, U> getUiControlDefinition()
	{
		return definition;
	}

	@Override
	public abstract void destroy();
}
