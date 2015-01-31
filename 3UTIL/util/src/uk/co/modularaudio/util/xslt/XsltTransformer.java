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

package uk.co.modularaudio.util.xslt;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class XsltTransformer
{
    private static final TransformerFactory TRANSFORM_FACTORY;

    private static Log log = LogFactory.getLog(XsltTransformer.class.getName());


    private final Transformer transformer;

    static
    {
        TRANSFORM_FACTORY = TransformerFactory.newInstance();
    }

    public XsltTransformer(final String xslSource) throws TransformerConfigurationException
    {
        transformer = TRANSFORM_FACTORY.newTransformer(new StreamSource(xslSource));
    }

    public XsltTransformer(final Source xslSource) throws TransformerConfigurationException
    {
        transformer = TRANSFORM_FACTORY.newTransformer(xslSource);
    }

    public void setParameter(final String name, final Object object)
    {
        transformer.setParameter(name, object);
    }

    public void clearParameters()
    {
        transformer.clearParameters();
    }

    //--- Transform to String
    public String transformToString(final String xmlSource) throws TransformerException, UnsupportedEncodingException
    {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try
        {
            transformer.transform(new StreamSource(xmlSource), new StreamResult(baos));
        }
        catch (final TransformerException te)
        {
            log.warn("TransformerException occurred, please verify following xml, which needs to be transformed:");
            log.warn(xmlSource);
            log.warn("Please verify the xlt's");
            throw(te);
        }
        return baos.toString("UTF-8");
    }

    public String transformToString(final Source xmlSource) throws TransformerException, UnsupportedEncodingException
    {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try
        {
            transformer.transform(xmlSource, new StreamResult(baos));
        }
        catch (final TransformerException te)
        {
            log.warn("TransformerException occurred, please verify following xml, which needs to be transformed:");
            log.warn(xmlSource);
            log.warn("Please verify the xslt's");
            throw(te);
        }
        return baos.toString("UTF-8");
    }

    //--- Transform from String to String
    public String transformFromStringToString(final String xmlContent) throws TransformerException, UnsupportedEncodingException
    {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try
        {
            transformer.transform(new StreamSource(new ByteArrayInputStream(xmlContent.getBytes("UTF-8"))), new StreamResult(baos));
        }
        catch (final TransformerException te)
        {
            log.warn("TransformerException occurred, please verify following xml, which needs to be transformed:");
            log.warn(xmlContent);
            log.warn("Please verify the xlt's");
            throw(te);
        }
        catch (final UnsupportedEncodingException uee)
        {
            log.warn("UnsupportedEncodingException occurred for the following xml, which needs to be transformed:");
            log.warn(xmlContent);
            log.warn("Please verify the xslt's");
            throw(uee);
        }

        return baos.toString("UTF-8");

    }

    //--- Transform to File or Result
    public void transform(final String xmlSource, final Result xmlResult) throws TransformerException
    {
        try
        {
            transformer.transform(new StreamSource(xmlSource), xmlResult);
        }
        catch (final TransformerException te)
        {
            log.warn("TransformerException occurred, please verify following xml, which needs to be transformed:");
            log.warn(xmlSource);
            log.warn("Please verify the xslt's");
            throw(te);
        }
    }

    public void transform(final Source xmlSource, final String xmlResult) throws TransformerException
    {
        try
        {
            transformer.transform(xmlSource, new StreamResult(xmlResult));
        }
        catch (final TransformerException te)
        {
            log.warn("TransformerException occurred, please verify following xml, which needs to be transformed:");
            log.warn(xmlSource);
            log.warn("Please verify the xslt's");
            throw(te);
        }
    }

    public void transform(final String xmlSource, final String xmlResult) throws TransformerException
    {
        try
        {
            transformer.transform(new StreamSource(xmlSource), new StreamResult(xmlResult));
        }
        catch (final TransformerException te)
        {
            log.warn("TransformerException occurred, please verify following xml, which needs to be transformed:");
            log.warn(xmlSource);
            log.warn("Please verify the xslt's");
            throw(te);
        }
    }

    public void transform(final Source xmlSource, final Result xmlResult) throws TransformerException
    {
        try
        {
            transformer.transform(xmlSource, xmlResult);
        }
        catch (final TransformerException te)
        {
            log.warn("TransformerException occurred, please verify following xml, which needs to be transformed:");
            log.warn(xmlSource);
            log.warn("Please verify the xslt's");
            throw(te);
        }
    }

    //--- Transform to System.out
    public void transformToSystemOut(final String xmlSource) throws TransformerException
    {
        try
        {
            transformer.transform(new StreamSource(xmlSource), new StreamResult(System.out));
        }
        catch (final TransformerException te)
        {
            log.warn("TransformerException occurred, please verify following xml, which needs to be transformed:");
            log.warn(xmlSource);
            log.warn("Please verify the xslt's");
            throw(te);
        }
    }

    public void transformToSystemOut(final Source xmlSource) throws TransformerException
    {
        try
        {
            transformer.transform(xmlSource, new StreamResult(System.out));
        }
        catch (final TransformerException te)
        {
            log.warn("TransformerException occurred, please verify following xml, which needs to be transformed:");
            log.warn(xmlSource);
            log.warn("Please verify the xslt's");
            throw(te);
        }
    }
}
