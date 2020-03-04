package com.jaspertest.render;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class for rendering jasper documents in different formats.
 * It searchs for document in user home directory by default, compiles and caches document in .jasper format,
 * if the document has not been used before. After that .jasper document is filled and exported in one of the supported formats.
 */
public class ReportSimplePdfGenerateService {
    private static final String GENERATED_DOCUMENT_TEMP_FILE_NAME = "document";
    private static final String GENERATED_DOCUMENT_TEMP_FILE_EXTENSION = ".pdf";
    //I've decided that injected base folder for documents is better,
    // but you can simply refactor #geratePdfPreview to take absolute path to the document
    //@Inject @JasperDocument// - inject document folder path here
    private String documentFolderPath = Paths.get("").toAbsolutePath().toString();
    private Map<String, FileTime> jasperDocumentCache = new ConcurrentHashMap<>();

    // - this method can be simply made async
    /**
     * Generates pdf document for the given .jrxml file and input parameters
     * @param filename .jrxml filename, base path is working directory by default
     * @param parameters jasper parameters map, 'parameter name' -> 'object to be passed'(must fit the parameter type indicated in the .jrxml file)
     * @return generate document input stream from temp file
     * @throws IOException if any I/O errors occurs
     * @throws JRException if any compilation errors occurs (I think it should be processed within this class)
     */
    public InputStream generatePdfPreview(String filename, Map<String, Object> parameters) throws IOException, JRException {
        //Creating temp file, to avoid memory issues (Generated files can be quite large)
        Path tempFilePath = Files.createTempFile(GENERATED_DOCUMENT_TEMP_FILE_NAME, GENERATED_DOCUMENT_TEMP_FILE_EXTENSION);
        //Probably should use BufferedOutputStream, to avoid performance issues
        try(InputStream compiledDocumentStream = new BufferedInputStream(Files.newInputStream(compileDocument(filename)));
            OutputStream outputStream = Files.newOutputStream(tempFilePath)) {
            //generating JasperPrint - format-independent presentation of the document
            JasperPrint jPrint = JasperFillManager.fillReport(compiledDocumentStream, parameters, new JREmptyDataSource());

            //Creating pdf exporter
            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setExporterInput(new SimpleExporterInput(jPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
//            exporter.setConfiguration(new SimplePdfExporterConfiguration());
            exporter.exportReport();
            return Files.newInputStream(tempFilePath);
        }

    }

    /**
     * Compile and cache document, or use previously compiled.
     * @param filename .jrxml document file name (with extension, e.g. "example.jrxml")
     * @return compiled document path
     * @throws IOException if any I/O error occurs
     * @throws JRException if any document compile errors occurs
     */
    private Path compileDocument(String filename) throws IOException, JRException {
        Path documentPath = Paths.get(documentFolderPath, filename);
        Path compiledDocumentPath = getCompiledDocumentPath(documentFolderPath, filename);
        FileTime jrxmlLastModifiedTime = Files.getLastModifiedTime(documentPath);
        if (jasperDocumentCache.get(filename) == null || jasperDocumentCache.get(filename).compareTo(jrxmlLastModifiedTime) < 0) {
            jasperDocumentCache.put(filename, jrxmlLastModifiedTime);
            try(InputStream in = Files.newInputStream(documentPath); OutputStream out = Files.newOutputStream(compiledDocumentPath)) {
                JasperCompileManager.compileReportToStream(in, out);
            }
        }
        return compiledDocumentPath;
    }

    private static Path getCompiledDocumentPath(String basePath, String jrxmlFilename) {
        return Paths.get(basePath, jrxmlFilename.replace(".jrxml", ".jasper"));
    }

}
