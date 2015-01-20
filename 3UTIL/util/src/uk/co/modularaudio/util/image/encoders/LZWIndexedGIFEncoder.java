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

    DansScreenDescriptor sd_;
    DansImageDescriptor id_;

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
        DansBitUtils.WriteString(output, "GIF87a");

        DansScreenDescriptor sd =
            new DansScreenDescriptor(width_, height_, numColors_);
        sd.Write(output);

        output.write(colors_, 0, colors_.length);

        DansImageDescriptor id = new DansImageDescriptor(width_, height_, ',');
        id.Write(output);

        byte codesize = DansBitUtils.BitsNeeded(numColors_);
        if (codesize == 1)
            ++codesize;
        output.write(codesize);

        DansLZWCompressor.LZWCompress(output, codesize, pixels_);

        output.write(0);

        id = new DansImageDescriptor((byte) 0, (byte) 0, ';');
        id.Write(output);

        output.flush();
        //Log.printlnMemory("LZWIndexedGIFEncoder finished.");
    }
}

class DansBitFile
{
    OutputStream output_;
    byte buffer_[];
    int index_, bitsLeft_;

    public DansBitFile(OutputStream output)
    {
        output_ = output;
        buffer_ = new byte[256];
        index_ = 0;
        bitsLeft_ = 8;
    }

    public void Flush() throws IOException
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

    public void WriteBits(int bits, int numbits) throws IOException
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

class DansLZWStringTable
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

    public DansLZWStringTable()
    {
        strChr_ = new byte[MAXSTR];
        strNxt_ = new short[MAXSTR];
        strHsh_ = new short[HASHSIZE];
    }

    public int AddCharString(short index, byte b)
    {
        int hshidx;

        if (numStrings_ >= MAXSTR)
            return 0xFFFF;

        hshidx = Hash(index, b);
        while (strHsh_[hshidx] != HASH_FREE)
            hshidx = (hshidx + HASHSTEP) % HASHSIZE;

        strHsh_[hshidx] = numStrings_;
        strChr_[numStrings_] = b;
        strNxt_[numStrings_] = (index != HASH_FREE) ? index : NEXT_FIRST;

        return numStrings_++;
    }

    public short FindCharString(short index, byte b)
    {
        int hshidx, nxtidx;

        if (index == HASH_FREE)
            return b;

        hshidx = Hash(index, b);
        while ((nxtidx = strHsh_[hshidx]) != HASH_FREE)
        {
            if (strNxt_[nxtidx] == index && strChr_[nxtidx] == b)
                return (short) nxtidx;
            hshidx = (hshidx + HASHSTEP) % HASHSIZE;
        }

        return (short) 0xFFFF;
    }

    public void ClearTable(int codesize)
    {
        numStrings_ = 0;

        for (int q = 0; q < HASHSIZE; q++)
        {
            strHsh_[q] = HASH_FREE;
        }

        int w = (1 << codesize) + RES_CODES;
        for (int q = 0; q < w; q++)
            AddCharString((short) 0xFFFF, (byte) q);
    }

    static public int Hash(short index, byte lastbyte)
    {
        return (((short) (lastbyte << 8) ^ index) & 0xFFFF) % HASHSIZE;
    }
}

class DansLZWCompressor
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

        DansBitFile bitFile = new DansBitFile(output);
        DansLZWStringTable strings = new DansLZWStringTable();

        clearcode = 1 << codesize;
        endofinfo = clearcode + 1;

        numbits = codesize + 1;
        limit = (1 << numbits) - 1;

        strings.ClearTable(codesize);
        bitFile.WriteBits(clearcode, numbits);

        for (int loop = 0; loop < toCompress.length; ++loop)
        {
            c = toCompress[loop];
            if ((index = strings.FindCharString(prefix, c)) != -1)
                prefix = index;
            else
            {
                bitFile.WriteBits(prefix, numbits);
                if (strings.AddCharString(prefix, c) > limit)
                {
                    if (++numbits > 12)
                    {
                        bitFile.WriteBits(clearcode, numbits - 1);
                        strings.ClearTable(codesize);
                        numbits = codesize + 1;
                    }
                    limit = (1 << numbits) - 1;
                }

                prefix = (short) (c & 0xFF);
            }
        }

        if (prefix != -1)
            bitFile.WriteBits(prefix, numbits);

        bitFile.WriteBits(endofinfo, numbits);
        bitFile.Flush();
    }
}

class DansScreenDescriptor
{
    public short localScreenWidth_, localScreenHeight_;
    private byte byte_;
    public byte backgroundColorIndex_, pixelAspectRatio_;

    public DansScreenDescriptor(short width, short height, int numColors)
    {
        localScreenWidth_ = width;
        localScreenHeight_ = height;
        SetGlobalColorTableSize(
            (byte) (DansBitUtils.BitsNeeded(numColors) - 1));
        SetGlobalColorTableFlag((byte) 1);
        SetSortFlag((byte) 0);
        SetColorResolution((byte) 7);
        backgroundColorIndex_ = 0;
        pixelAspectRatio_ = 0;
    }

    public void Write(OutputStream output) throws IOException
    {
        DansBitUtils.WriteWord(output, localScreenWidth_);
        DansBitUtils.WriteWord(output, localScreenHeight_);
        output.write(byte_);
        output.write(backgroundColorIndex_);
        output.write(pixelAspectRatio_);
    }

    public void SetGlobalColorTableSize(byte num)
    {
        byte_ |= (num & 7);
    }

    public void SetSortFlag(byte num)
    {
        byte_ |= (num & 1) << 3;
    }

    public void SetColorResolution(byte num)
    {
        byte_ |= (num & 7) << 4;
    }

    public void SetGlobalColorTableFlag(byte num)
    {
        byte_ |= (num & 1) << 7;
    }
}

class DansImageDescriptor
{
    public byte separator_;
    public short leftPosition_, topPosition_, width_, height_;
    private byte byte_;

    public DansImageDescriptor(short width, short height, char separator)
    {
        separator_ = (byte) separator;
        leftPosition_ = 0;
        topPosition_ = 0;
        width_ = width;
        height_ = height;
        SetLocalColorTableSize((byte) 0);
        SetReserved((byte) 0);
        SetSortFlag((byte) 0);
        SetInterlaceFlag((byte) 0);
        SetLocalColorTableFlag((byte) 0);
    }

    public void Write(OutputStream output) throws IOException
    {
        output.write(separator_);
        DansBitUtils.WriteWord(output, leftPosition_);
        DansBitUtils.WriteWord(output, topPosition_);
        DansBitUtils.WriteWord(output, width_);
        DansBitUtils.WriteWord(output, height_);
        output.write(byte_);
    }

    public void SetLocalColorTableSize(byte num)
    {
        byte_ |= (num & 7);
    }

    public void SetReserved(byte num)
    {
        byte_ |= (num & 3) << 3;
    }

    public void SetSortFlag(byte num)
    {
        byte_ |= (num & 1) << 5;
    }

    public void SetInterlaceFlag(byte num)
    {
        byte_ |= (num & 1) << 6;
    }

    public void SetLocalColorTableFlag(byte num)
    {
        byte_ |= (num & 1) << 7;
    }
}

class DansBitUtils
{
    public static byte BitsNeeded(int n)
    {
        byte ret = 1;

        if (n-- == 0)
            return 0;

        while ((n >>= 1) != 0)
            ++ret;

        return ret;
    }

    public static void WriteWord(OutputStream output, short w)
        throws IOException
    {
        output.write(w & 0xFF);
        output.write((w >> 8) & 0xFF);
    }

    static void WriteString(OutputStream output, String string)
        throws IOException
    {
        for (int loop = 0; loop < string.length(); ++loop)
            output.write((byte) (string.charAt(loop)));
    }
}
