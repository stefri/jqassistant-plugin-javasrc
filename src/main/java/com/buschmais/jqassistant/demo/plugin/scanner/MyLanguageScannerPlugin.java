package com.buschmais.jqassistant.demo.plugin.scanner;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.demo.plugin.model.LineDescriptor;
import com.buschmais.jqassistant.demo.plugin.model.MyLanguageFileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;

/**
 * A scanner plugin implementation accepting all files with extension
 * "myLanguage".
 * 
 * @author Dirk Mahler
 *
 */
public class MyLanguageScannerPlugin extends AbstractScannerPlugin<FileResource, MyLanguageFileDescriptor> {

    @Override
    public boolean accepts(FileResource item, String path, Scope scope) throws IOException {
        return path.endsWith(".myLanguage");
    }

    @Override
    public MyLanguageFileDescriptor scan(FileResource item, String path, Scope scope, Scanner scanner) throws IOException {
        Store store = scanner.getContext().getStore();
        // Create a node representing the file itself
        MyLanguageFileDescriptor fileDescriptor = store.create(MyLanguageFileDescriptor.class);

        // Read each line of the file
        LineNumberReader reader = new LineNumberReader(new InputStreamReader(item.createStream()));
        String line;
        int lineNumber = 0;
        LineDescriptor previousLineDescriptor = null;
        while ((line = reader.readLine()) != null) {
            // Create a node representing the current line and set value and
            // line number
            // properties
            LineDescriptor currentLineDescriptor = store.create(LineDescriptor.class);
            currentLineDescriptor.setValue(line);
            currentLineDescriptor.setLineNumber(lineNumber);

            lineNumber++;
            if (previousLineDescriptor != null) {
                // Add a relation "HAS_NEXT" from the previous line node to the
                // current line node
                previousLineDescriptor.setNext(currentLineDescriptor);
            }
            previousLineDescriptor = currentLineDescriptor;

            // Add a relation "HAS_LINE" from the file node to the current line
            // node
            fileDescriptor.getLines().add(currentLineDescriptor);
        }
        return fileDescriptor;
    }

}
