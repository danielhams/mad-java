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

package uk.co.modularaudio.util.image.encoders;

import java.awt.AWTException;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.io.IOException;
import java.io.OutputStream;

public class LZWIndexedGIFEncoder
{
	private final int width, height;
	private final int numColors;
	private final byte pixels[];
	private final byte colors[];

    public LZWIndexedGIFEncoder(final IndexColorModel colorModel, final BufferedImage image)
    	throws AWTException, IOException
    {
        //Log.printlnMemory("LZWIndexedGIFEncoder beginning");
        width = image.getWidth(null);
        height = image.getHeight(null);
        numColors = colorModel.getMapSize();
        if (numColors > 256)
        {
        	throw new IOException("Too many colors. Image must have < 256 colours in palette.");
        }
        final int[] tmpColors = new int[numColors];
        //Log.printlnMemory("Got back " + numColors_ + " colors");
        colors = new byte[numColors * 3];
        colorModel.getRGBs( tmpColors );
        for(int i=0;i<numColors;i++)
        {
        	colors[i * 3] = (byte)((tmpColors[i] >> 16) & 0XFF);
        	colors[(i * 3) + 1] = (byte)((tmpColors[i] >> 8) & 0XFF);
        	colors[(i * 3) + 2] = (byte)((tmpColors[i]) & 0XFF);
        }

        final Raster raster = image.getRaster();
        final DataBuffer dataBuffer = raster.getDataBuffer();
		if (dataBuffer.getDataType() != DataBuffer.TYPE_BYTE)
		{
			throw new IOException("BAD DATA TYPE MATCH IN IMAGE");
		}
		final DataBufferByte byteBuffer = (DataBufferByte)dataBuffer;

		pixels = byteBuffer.getData();
    }

    public void encode(final OutputStream output) throws IOException
    {
        InternalBitUtils.writeString(output, "GIF87a");

        final InternalScreenDescriptor sd =
            new InternalScreenDescriptor(width, height, numColors);
        sd.write(output);

        output.write(colors, 0, colors.length);

        InternalImageDescriptor id = new InternalImageDescriptor(width, height, ',');
        id.write(output);

        byte codesize = InternalBitUtils.bitsNeeded(numColors);
        if (codesize == 1)
            ++codesize;
        output.write(codesize);

        InternalLZWCompressor.lzwCompress(output, codesize, pixels);

        output.write(0);

        id = new InternalImageDescriptor((byte) 0, (byte) 0, ';');
        id.write(output);

        output.flush();
        //Log.printlnMemory("LZWIndexedGIFEncoder finished.");
    }
}

class InternalBitFile
{
    OutputStream output;
    byte buffer[];
    int index, bitsLeft;

    public InternalBitFile(final OutputStream output)
    {
        this.output = output;
        buffer = new byte[256];
        index = 0;
        bitsLeft = 8;
    }

    public void flush() throws IOException
    {
        final int numBytes = index + (bitsLeft == 8 ? 0 : 1);
        if (numBytes > 0)
        {
            output.write(numBytes);
            output.write(buffer, 0, numBytes);
            buffer[0] = 0;
            index = 0;
            bitsLeft = 8;
        }
    }

    public void writeBits(int bits, int numbits) throws IOException // NOPMD by dan on 08/07/15 13:35
    {
//        int bitsWritten = 0;
        final int numBytes = 255;
        do
        {
            if ((index == 254 && bitsLeft == 0) || index > 254)
            {
                output.write(numBytes);
                output.write(buffer, 0, numBytes);

                buffer[0] = 0;
                index = 0;
                bitsLeft = 8;
            }

            if (numbits <= bitsLeft)
            {
                buffer[index] |= (bits & ((1 << numbits) - 1))
                    << (8 - bitsLeft);
//                bitsWritten += numbits;
                bitsLeft -= numbits;
                numbits = 0;
            }
            else
            {
                buffer[index] |= (bits & ((1 << bitsLeft) - 1))
                    << (8 - bitsLeft);
//                bitsWritten += bitsLeft_;
                bits >>= bitsLeft;
                numbits -= bitsLeft;
                buffer[++index] = 0;
                bitsLeft = 8;
            }
        }
        while (numbits != 0);
    }
}

class InternalLZWStringTable
{
    private final static int RES_CODES = 2;
    private final static int HASH_FREE = 0xFFFF;
    private final static int NEXT_FIRST = 0xFFFF;
    private final static int MAXBITS = 12;
    private final static int MAXSTR = (1 << MAXBITS);
    private final static int HASHSIZE = 9973;
    private final static int HASHSTEP = 2039;

    private final byte strChr[];
    private final int strNxt[];
    private final int strHsh[];
    private int numStrings;

    public InternalLZWStringTable()
    {
        strChr = new byte[MAXSTR];
        strNxt = new int[MAXSTR];
        strHsh = new int[HASHSIZE];
    }

    public int addCharString(final int index, final byte b)
    {
        int hshidx;

        if (numStrings >= MAXSTR)
            return 0xFFFF;

        hshidx = hash(index, b);
        while (strHsh[hshidx] != HASH_FREE)
            hshidx = (hshidx + HASHSTEP) % HASHSIZE;

        strHsh[hshidx] = numStrings;
        strChr[numStrings] = b;
        strNxt[numStrings] = (index != HASH_FREE) ? index : NEXT_FIRST;

        return numStrings++;
    }

    public int findCharString(final int index, final byte b)
    {
        int hshidx, nxtidx;

        if (index == HASH_FREE)
            return b;

        hshidx = hash(index, b);
        while ((nxtidx = strHsh[hshidx]) != HASH_FREE)
        {
            if (strNxt[nxtidx] == index && strChr[nxtidx] == b)
                return nxtidx;
            hshidx = (hshidx + HASHSTEP) % HASHSIZE;
        }

        return 0xFFFF;
    }

    public void clearTable(final int codesize)
    {
        numStrings = 0;

        for (int q = 0; q < HASHSIZE; q++)
        {
            strHsh[q] = HASH_FREE;
        }

        final int w = (1 << codesize) + RES_CODES;
        for (int q = 0; q < w; q++)
            addCharString(0xFFFF, (byte) q);
    }

    static public int hash(final int index, final byte lastbyte)
    {
        return (((lastbyte << 8) ^ index) & 0xFFFF) % HASHSIZE;
    }
}

class InternalLZWCompressor
{

    public static void lzwCompress(
        final OutputStream output,
        final int codesize,
        final byte toCompress[])
        throws IOException
    {
        byte c;
        int index;
        int clearcode, endofinfo, numbits, limit;
        int prefix = 0xFFFF;

        final InternalBitFile bitFile = new InternalBitFile(output);
        final InternalLZWStringTable strings = new InternalLZWStringTable();

        clearcode = 1 << codesize;
        endofinfo = clearcode + 1;

        numbits = codesize + 1;
        limit = (1 << numbits) - 1;

        strings.clearTable(codesize);
        bitFile.writeBits(clearcode, numbits);

        for (int loop = 0; loop < toCompress.length; ++loop)
        {
            c = toCompress[loop];
            if ((index = strings.findCharString(prefix, c)) != -1)
                prefix = index;
            else
            {
                bitFile.writeBits(prefix, numbits);
                if (strings.addCharString(prefix, c) > limit)
                {
                    if (++numbits > 12)
                    {
                        bitFile.writeBits(clearcode, numbits - 1);
                        strings.clearTable(codesize);
                        numbits = codesize + 1;
                    }
                    limit = (1 << numbits) - 1;
                }

                prefix = (c & 0xFF);
            }
        }

        if (prefix != -1)
            bitFile.writeBits(prefix, numbits);

        bitFile.writeBits(endofinfo, numbits);
        bitFile.flush();
    }
}

class InternalScreenDescriptor
{
    private final int localScreenWidth, localScreenHeight;
    private byte screenByte;
    private final byte backgroundColorIndex, pixelAspectRatio;

    public InternalScreenDescriptor(final int width, final int height, final int numColors)
    {
        localScreenWidth = width;
        localScreenHeight = height;
        setGlobalColorTableSize(
            (byte) (InternalBitUtils.bitsNeeded(numColors) - 1));
        setGlobalColorTableFlag((byte) 1);
        setSortFlag((byte) 0);
        setColorResolution((byte) 7);
        backgroundColorIndex = 0;
        pixelAspectRatio = 0;
    }

    public void write(final OutputStream output) throws IOException
    {
        InternalBitUtils.writeWord(output, localScreenWidth);
        InternalBitUtils.writeWord(output, localScreenHeight);
        output.write(screenByte);
        output.write(backgroundColorIndex);
        output.write(pixelAspectRatio);
    }

    public void setGlobalColorTableSize(final byte num)
    {
        screenByte |= (num & 7);
    }

    public void setSortFlag(final byte num)
    {
        screenByte |= (num & 1) << 3;
    }

    public void setColorResolution(final byte num)
    {
        screenByte |= (num & 7) << 4;
    }

    public void setGlobalColorTableFlag(final byte num)
    {
        screenByte |= (num & 1) << 7;
    }
}

class InternalImageDescriptor
{
    private final byte separator_;
    private final int leftPosition, topPosition, width, height;
    private byte internalByte;

    public InternalImageDescriptor(final int width, final int height, final char separator)
    {
        separator_ = (byte) separator;
        leftPosition = 0;
        topPosition = 0;
        this.width = width;
        this.height = height;
        setLocalColorTableSize((byte) 0);
        setReserved((byte) 0);
        setSortFlag((byte) 0);
        setInterlaceFlag((byte) 0);
        setLocalColorTableFlag((byte) 0);
    }

    public void write(final OutputStream output) throws IOException
    {
        output.write(separator_);
        InternalBitUtils.writeWord(output, leftPosition);
        InternalBitUtils.writeWord(output, topPosition);
        InternalBitUtils.writeWord(output, width);
        InternalBitUtils.writeWord(output, height);
        output.write(internalByte);
    }

    public void setLocalColorTableSize(final byte num)
    {
        internalByte |= (num & 7);
    }

    public void setReserved(final byte num)
    {
        internalByte |= (num & 3) << 3;
    }

    public void setSortFlag(final byte num)
    {
        internalByte |= (num & 1) << 5;
    }

    public void setInterlaceFlag(final byte num)
    {
        internalByte |= (num & 1) << 6;
    }

    public void setLocalColorTableFlag(final byte num)
    {
        internalByte |= (num & 1) << 7;
    }
}

class InternalBitUtils
{
    public static byte bitsNeeded(int n) // NOPMD by dan on 08/07/15 13:35
    {
        byte ret = 1;

        if (n-- == 0)
            return 0;

        while ((n >>= 1) != 0)
            ++ret;

        return ret;
    }

    public static void writeWord(final OutputStream output, final int w)
        throws IOException
    {
        output.write(w & 0xFF);
        output.write((w >> 8) & 0xFF);
    }

    static void writeString(final OutputStream output, final String string)
        throws IOException
    {
        for (int loop = 0; loop < string.length(); ++loop)
            output.write((byte) (string.charAt(loop)));
    }
}
