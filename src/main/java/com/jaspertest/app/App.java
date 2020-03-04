package com.jaspertest.app;

import com.jaspertest.render.ReportSimplePdfGenerateService;

import java.io.File;
import java.io.InputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.HashMap;

public class App {
    public static void main(String[] args) throws Exception {
        System.out.println();
        String html =
                "<html>\n" +
                "<head>\n" +
                "    <title>Sample of html in jasper report</title>\n" +
                "    <style type=\"text/css\">\n" +
                "        body {\n" +
                "            padding-left: 11em;\n" +
                "            font-family: Georgia, \"Times New Roman\",\n" +
                "            Times, serif;\n" +
                "            color: #001f3f;\n" +
                "            background-color: #DDDDDD\n" +
                "        }\n" +
                "\n" +
                "        h1 {\n" +
                "            font-family: Helvetica, Geneva, Arial,\n" +
                "            SunSans-Regular, sans-serif\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "\n" +
                "<body>\n" +
                "<h1>Sample of html in jasper report</h1>\n" +
                "\n" +
                "<p>html features are probably limited</p>\n" +
                "\n" +
                "<p>image example</p>\n" +
                "<br/><br/>\n" +
                "<img src='file:/" + Paths.get("").toString() + File.pathSeparator + "download.jpg' alt='test' height='100' width='100'>\n" +
                "</body>\n" +
                "</html>";

        ReportSimplePdfGenerateService service = new ReportSimplePdfGenerateService();
        try(InputStream is = service.generatePdfPreview("html_renderer.jrxml", new HashMap<>(Collections.singletonMap("htmlCode", html)))) {
            Files.copy(is, Paths.get(System.getProperty("user.home"), "Documents", "document.pdf"), StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
