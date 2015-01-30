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

package uk.co.modularaudio.service.hashedstorage.impl;

import java.security.MessageDigest;

import uk.co.modularaudio.util.exception.DatastoreException;

public class HashComputer
{
	private static String convertToHex(final byte[] data)
	{
		final StringBuilder buf = new StringBuilder();
		for (int i = 0; i < data.length; i++)
		{
			int halfbyte = (data[i] >>> 4) & 0x0F;
			int twoHalfs = 0;
			do
			{
				if ((0 <= halfbyte) && (halfbyte <= 9))
					buf.append((char) ('0' + halfbyte));
				else
					buf.append((char) ('a' + (halfbyte - 10)));
				halfbyte = data[i] & 0x0F;
			}
			while (twoHalfs++ < 1);
		}
		return buf.toString();
	}

	public static String computeTextSha1(final String text) throws DatastoreException
	{
		try
		{
			MessageDigest md;
			md = MessageDigest.getInstance("SHA-1");
			byte[] sha1hash = new byte[40];
			md.update(text.getBytes("UTF-8"), 0, text.length());
			sha1hash = md.digest();
			return convertToHex(sha1hash);
		}
		catch (final Exception e)
		{
			final String msg = "Exception thrown computing sha1 hash: " + e.toString();
			throw new DatastoreException( msg, e );
		}
	}

}
