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

package uk.co.modularaudio.util.audio.gui.mad.rollpainter;

import uk.co.modularaudio.util.exception.DatastoreException;


public interface RollPainterSampleFactory<RPBT, RPBTClearer extends RollPainterBufferClearer<RPBT>>
{
	RPBTClearer getBufferClearer();

	RPBT createBuffer( int bufNum ) throws DatastoreException;
	void freeBuffer( RPBT bufferToFree ) throws DatastoreException;

	RollPaintUpdate getPaintUpdate();

	void fullFillSamples( RollPaintUpdate update, RPBT buffer );

	void deltaFillSamples( RollPaintUpdate update, int displayOffset, RPBT buffer, int bufferSampleOffset, int numSamples, RPBT otherBuffer );

}
