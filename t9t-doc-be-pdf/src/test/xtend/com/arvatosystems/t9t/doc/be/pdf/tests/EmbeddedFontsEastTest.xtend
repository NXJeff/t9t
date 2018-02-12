package com.arvatosystems.t9t.doc.be.pdf.tests

import com.lowagie.text.pdf.BaseFont
import java.io.File
import java.io.FileOutputStream
import org.junit.Test
import org.xhtmlrenderer.pdf.ITextRenderer

class EmbeddedFontsEastTest {

    @Test
    def void test() {
        val renderer = new ITextRenderer();
        val fontResolver = renderer.getFontResolver();
        fontResolver.addFont("fonts/Lato-Regular.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
        fontResolver.addFont("fonts/NotoSans-Regular.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);

        renderer.setDocumentFromString('''
            <!DOCTYPE html>
            <html>
                <head>
                    <meta charset="utf-8"/>
                    <meta name="viewport" content="width=device-width, initial-scale=1" />
                    <meta http-equiv="X-UA-compatible" content="text/html" />
                    <title>Invoice</title>
                    <style type="text/css">
                        @page {
                            size: 8in 14.8in;
                            margin: 0m;
                        }
                        body {
                            line-height: 1.5;
                            font-size: 8pt;
                            font-family: Lato, sans-serif;
                        }
                        @font-face {
                            font-family: \'Lato\';
                            font-style: normal;
                            font-weight: 400;
                            -fs-pdf-font-embed: embed;
                            -fs-pdf-font-encoding: Identity-H;
                        }

                      }
                    </style>
                </head>

                <body style="font-family:Lato,sans-serif;margin:0; padding:0; background:#ffffff;">
                    <p>
                        Dziękujemy za zaufanie, jakim obdarzyli Państwo nasze usługi. W załączeniu przesyłamy fakturę.
                    </p>
                </body>
            </html>
        ''');
        renderer.layout();
        val outputPDFFile = File.createTempFile("test", "PDF");
        val os = new FileOutputStream(outputPDFFile);
        renderer.createPDF(os);
    }
}