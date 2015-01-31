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
    short width_, height_;
    int numColors_;
    byte pixels_[];
    byte colors_[];

    InternalScreenDescriptor sd_;
    InternalImageDescriptor id_;

    public LZWIndexedGIFEncoder(IndexColorModel colorModel, BufferedImage image)
    	throws AWTException, IOException
    {
        //Log.printlnMemory("LZWIndexedGIFEncoder beginning");
        width_ = (short) image.getWidth(null);
        height_ = (short) image.getHeight(null);
        numColors_ = colorModel.getMapSize();
        if (numColors_ > 256)
        {
        	throw new IOException("Too many colors. Image must have < 256 colours in palette.");
        }
        int[] tmpColors = new int[numColors_];
        //Log.printlnMemory("Got back " + numColors_ + " colors");
        colors_ = new byte[numColors_ * 3];
        colorModel.getRGBs( tmpColors );
        for(int i=0;i<numColors_;i++)
        {
        	colors_[i * 3] = (byte)((tmpColors[i] >> 16) & 0XFF);
        	colors_[(i * 3) + 1] = (byte)((tmpColors[i] >> 8) & 0XFF);
        	colors_[(i * 3) + 2] = (byte)((tmpColors[i]) & 0XFF);
        }

        Raster raster = image.getRaster();
        DataBuffer dataBuffer = raster.getDataBuffer();
		if (dataBuffer.getDataType() != DataBuffer.TYPE_BYTE)
		{
			throw new IOException("BAD DATA TYPE MATCH IN IMAGE");
		}
		DataBufferByte byteBuffer = (DataBufferByte)dataBuffer;

		pixels_ = byteBuffer.getData();
    }

    public void encode(OutputStream output) throws IOException
    {
        InternalBitUtils.writeString(output, "GIF87a");

        InternalScreenDescriptor sd =
            new InternalScreenDescriptor(width_, height_, numColors_);
        sd.write(output);

        output.write(colors_, 0, colors_.length);

        InternalImageDescriptor id = new InternalImageDescriptor(width_, height_, ',');
        id.write(output);

        byte codesize = InternalBitUtils.bitsNeeded(numColors_);
        if (codesize == 1)
            ++codesize;
        output.write(codesize);

        InternalLZWCompressor.LZWCompress(output, codesize, pixels_);

        output.write(0);

        id = new InternalImageDescriptor((byte) 0, (byte) 0, ';');
        id.write(output);

        output.flush();
        //Log.printlnMemory("LZWIndexedGIFEncoder finished.");
    }
}

class InternalBitFile
{
    OutputStream output_;
    byte buffer_[];
    int index_, bitsLeft_;

    public InternalBitFile(OutputStream output)
    {
        output_ = output;
        buffer_ = new byte[256];
        index_ = 0;
        bitsLeft_ = 8;
    }

    public void flush() throws IOException
    {
        int numBytes = index_ + (bitsLeft_ == 8 ? 0 : 1);
        if (numBytes > 0)
        {
            output_.write(numBytes);
            output_.write(buffer_, 0, numBytes);
            buffer_[0] = 0;
            index_ = 0;
            bitsLeft_ = 8;
        }
    }

    public void writeBits(int bits, int numbits) throws IOException
    {
//        int bitsWritten = 0;
        int numBytes = 255;
        do
        {
            if ((index_ == 254 && bitsLeft_ == 0) || index_ > 254)
            {
                output_.write(numBytes);
                output_.write(buffer_, 0, numBytes);

                buffer_[0] = 0;
                index_ = 0;
                bitsLeft_ = 8;
            }

            if (numbits <= bitsLeft_)
            {
                buffer_[index_] |= (bits & ((1 << numbits) - 1))
                    << (8 - bitsLeft_);
//                bitsWritten += numbits;
                bitsLeft_ -= numbits;
                numbits = 0;
            }
            else
            {
                buffer_[index_] |= (bits & ((1 << bitsLeft_) - 1))
                    << (8 - bitsLeft_);
//                bitsWritten += bitsLeft_;
                bits >>= bitsLeft_;
                numbits -= bitsLeft_;
                buffer_[++index_] = 0;
                bitsLeft_ = 8;
            }
        }
        while (numbits != 0);
    }
}

class InternalLZWStringTable
{
    private final static int RES_CODES = 2;
    private final static short HASH_FREE = (short) 0xFFFF;
    private final static short NEXT_FIRST = (short) 0xFFFF;
    private final static int MAXBITS = 12;
    private final static int MAXSTR = (1 << MAXBITS);
    private final static short HASHSIZE = 9973;
    private final static short HASHSTEP = 2039;

    byte strChr_[];
    short strNxt_[];
    short strHsh_[];
    short numStrings_;

    public InternalLZWStringTable()
    {
        strChr_ = new byte[MAXSTR];
        strNxt_ = new short[MAXSTR];
        strHsh_ = new short[HASHSIZE];
    }

    public int addCharString(short index, byte b)
    {
        int hshidx;

        if (numStrings_ >= MAXSTR)
            return 0xFFFF;

        hshidx = hash(index, b);
        while (strHsh_[hshidx] != HASH_FREE)
            hshidx = (hshidx + HASHSTEP) % HASHSIZE;

        strHsh_[hshidx] = numStrings_;
        strChr_[numStrings_] = b;
        strNxt_[numStrings_] = (index != HASH_FREE) ? index : NEXT_FIRST;

        return numStrings_++;
    }

    public short findCharString(short index, byte b)
    {
        int hshidx, nxtidx;

        if (index == HASH_FREE)
            return b;

        hshidx = hash(index, b);
        while ((nxtidx = strHsh_[hshidx]) != HASH_FREE)
        {
            if (strNxt_[nxtidx] == index && strChr_[nxtidx] == b)
                return (short) nxtidx;
            hshidx = (hshidx + HASHSTEP) % HASHSIZE;
        }

        return (short) 0xFFFF;
    }

    public void clearTable(int codesize)
    {
        numStrings_ = 0;

        for (int q = 0; q < HASHSIZE; q++)
        {
            strHsh_[q] = HASH_FREE;
        }

        int w = (1 << codesize) + RES_CODES;
        for (int q = 0; q < w; q++)
            addCharString((short) 0xFFFF, (byte) q);
    }

    static public int hash(short index, byte lastbyte)
    {
        return (((short) (lastbyte << 8) ^ index) & 0xFFFF) % HASHSIZE;
    }
}

class InternalLZWCompressor
{

    public static void LZWCompress(
        OutputStream output,
        int codesize,
        byte toCompress[])
        throws IOException
    {
        byte c;
        short index;
        int clearcode, endofinfo, numbits, limit;
        short prefix = (short) 0xFFFF;

        InternalBitFile bitFile = new InternalBitFile(output);
        InternalLZWStringTable strings = new InternalLZWStringTable();

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

                prefix = (short) (c & 0xFF);
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
    public short localScreenWidth_, localScreenHeight_;
    private byte byte_;
    public byte backgroundColorIndex_, pixelAspectRatio_;

    public InternalScreenDescriptor(short width, short height, int numColors)
    {
        localScreenWidth_ = width;
        localScreenHeight_ = height;
        setGlobalColorTableSize(
            (byte) (InternalBitUtils.bitsNeeded(numColors) - 1));
        setGlobalColorTableFlag((byte) 1);
        setSortFlag((byte) 0);
        setColorResolution((byte) 7);
        backgroundColorIndex_ = 0;
        pixelAspectRatio_ = 0;
    }

    public void write(OutputStream output) throws IOException
    {
        InternalBitUtils.writeWord(output, localScreenWidth_);
        InternalBitUtils.writeWord(output, localScreenHeight_);
        output.write(byte_);
        output.write(backgroundColorIndex_);
        output.write(pixelAspectRatio_);
    }

    public void setGlobalColorTableSize(byte num)
    {
        byte_ |= (num & 7);
    }

    public void setSortFlag(byte num)
    {
        byte_ |= (num & 1) << 3;
    }

    public void setColorResolution(byte num)
    {
        byte_ |= (num & 7) << 4;
    }

    public void setGlobalColorTableFlag(byte num)
    {
        byte_ |= (num & 1) << 7;
    }
}

class InternalImageDescriptor
{
    public byte separator_;
    public short leftPosition_, topPosition_, width_, height_;
    private byte byte_;

    public InternalImageDescriptor(short width, short height, char separator)
    {
        separator_ = (byte) separator;
        leftPosition_ = 0;
        topPosition_ = 0;
        width_ = width;
        height_ = height;
        setLocalColorTableSize((byte) 0);
        setReserved((byte) 0);
        setSortFlag((byte) 0);
        setInterlaceFlag((byte) 0);
        setLocalColorTableFlag((byte) 0);
    }

    public void write(OutputStream output) throws IOException
    {
        output.write(separator_);
        InternalBitUtils.writeWord(output, leftPosition_);
        InternalBitUtils.writeWord(output, topPosition_);
        InternalBitUtils.writeWord(output, width_);
        InternalBitUtils.writeWord(output, height_);
        output.write(byte_);
    }

    public void setLocalColorTableSize(byte num)
    {
        byte_ |= (num & 7);
    }

    public void setReserved(byte num)
    {
        byte_ |= (num & 3) << 3;
    }

    public void setSortFlag(byte num)
    {
        byte_ |= (num & 1) << 5;
    }

    public void setInterlaceFlag(byte num)
    {
        byte_ |= (num & 1) << 6;
    }

    public void setLocalColorTableFlag(byte num)
    {
        byte_ |= (num & 1) << 7;
    }
}

class InternalBitUtils
{
    public static byte bitsNeeded(int n)
    {
        byte ret = 1;

        if (n-- == 0)
            return 0;

        while ((n >>= 1) != 0)
            ++ret;

        return ret;
    }

    public static void writeWord(OutputStream output, short w)
        throws IOException
    {
        output.write(w & 0xFF);
        output.write((w >> 8) & 0xFF);
    }

    static void writeString(OutputStream output, String string)
        throws IOException
    {
        for (int loop = 0; loop < string.length(); ++loop)
            output.write((byte) (string.charAt(loop)));
    }
}
