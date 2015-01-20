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

package uk.co.modularaudio.mads.base.audioanalyser.ui.tabbedpane.oscope;

import java.awt.Graphics2D;

import uk.co.modularaudio.mads.base.audioanalyser.ui.AdditionalDataBuffers;

public interface DisplayPresentationProcessor
{
	void resetForSwitch();

	void doPreIndex( int preIndexPos, int numSamplesPerPixel );

	void presentPixel(int indexInt, int numSamplesPerPixel,
			Graphics2D g, int pixelOffset );

	AdditionalDataBuffers getAdditionalDataBuffers();

}
