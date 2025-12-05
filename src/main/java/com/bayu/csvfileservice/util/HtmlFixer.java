package com.bayu.csvfileservice.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;

public class HtmlFixer {

    // Memperbaiki HTML yang tidak valid tanpa mengubah tampilan
    public static String fixHtml(String html) {
        Document doc = Jsoup.parse(html);

        doc.outputSettings()
                .syntax(Document.OutputSettings.Syntax.xml)   // auto-close tag
                .escapeMode(Entities.EscapeMode.xhtml)
                .prettyPrint(false); // agar layout tidak berubah

        return doc.html();
    }

}
