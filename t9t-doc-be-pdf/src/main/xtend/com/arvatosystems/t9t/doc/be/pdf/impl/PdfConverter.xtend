package com.arvatosystems.t9t.doc.be.pdf.impl

import com.arvatosystems.t9t.doc.services.IDocConverter
import com.lowagie.text.DocumentException
import com.lowagie.text.pdf.BaseFont
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.api.media.MediaTypes
import de.jpaw.bonaparte.pojos.api.media.MediaData
import de.jpaw.bonaparte.pojos.api.media.MediaType
import de.jpaw.dp.Named
import de.jpaw.dp.Singleton
import de.jpaw.util.ByteArray
import de.jpaw.util.ExceptionUtil
import java.io.ByteArrayOutputStream
import org.xhtmlrenderer.pdf.ITextFontResolver
import org.xhtmlrenderer.pdf.ITextRenderer

@AddLogger
@Singleton
@Named("PDF")
class PdfConverter implements IDocConverter {

    def protected void addEmbeddedFonts(ITextFontResolver fontResolver, String html, String fontname, String ttfname) {
        if (html.indexOf("font-family: " + fontname) > 0) {
            LOGGER.debug("Embedding {} font into PDF", fontname)
            try {
                fontResolver.addFont("fonts/" + ttfname + ".ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
            } catch (DocumentException e) {
                LOGGER.error("{} font could not be added due to {} ", fontname, ExceptionUtil.causeChain(e));
            }
        }
    }

    override convert(MediaData src) {
        val baseEnum = src.mediaType.baseEnum
        if (baseEnum != MediaType.HTML && baseEnum != MediaType.XHTML)
            return null;
        val baos = new ByteArrayOutputStream
        new ITextRenderer => [
            fontResolver.addEmbeddedFonts(src.text, "Lato", "Lato-Regular")
            fontResolver.addEmbeddedFonts(src.text, "Noto", "NotoSans-Regular")
            documentFromString = src.text
            layout
            createPDF(baos)
        ]
        return new MediaData => [
            mediaType   = MediaTypes.MEDIA_XTYPE_PDF
            rawData     = ByteArray.fromByteArrayOutputStream(baos)
        ]
    }

}