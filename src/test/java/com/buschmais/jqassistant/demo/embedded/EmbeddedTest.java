package com.buschmais.jqassistant.demo.embedded;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.buschmais.jqassistant.core.plugin.api.ModelPluginRepository;
import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.plugin.api.RulePluginRepository;
import com.buschmais.jqassistant.core.plugin.api.ScannerPluginRepository;
import com.buschmais.jqassistant.core.plugin.impl.ModelPluginRepositoryImpl;
import com.buschmais.jqassistant.core.plugin.impl.PluginConfigurationReaderImpl;
import com.buschmais.jqassistant.core.plugin.impl.RulePluginRepositoryImpl;
import com.buschmais.jqassistant.core.plugin.impl.ScannerPluginRepositoryImpl;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.impl.ScannerImpl;
import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;
import com.buschmais.jqassistant.scm.neo4jserver.api.Server;
import com.buschmais.jqassistant.scm.neo4jserver.impl.DefaultServerImpl;

/**
 * Demonstrates embedded usage of jQAssistant APIs.
 * 
 * @author Dirk Mahler
 */
public class EmbeddedTest {

    /**
     * The store, i.e. the Neo4j database.
     */
    private EmbeddedGraphStore store;

    /**
     * The plugin repository holding the model, i.e. interfaces labeled with
     * annotations provided by eXtended Objects
     */
    private ModelPluginRepository modelPluginRepository;

    /**
     * The plugin repository holding the scanner plugins.
     */

    private ScannerPluginRepository scannerPluginRepository;

    /**
     * The plugin repository holding the rule plugins.
     */
    private RulePluginRepository rulePluginRepository;

    /**
     * Initializes the plugin repositories and creates the store instance and
     * starts it.
     * 
     * @throws PluginRepositoryException
     *             If the plugins cannot be read.
     */
    @Before
    public void startStore() throws PluginRepositoryException {
        // Init plugin repositories
        PluginConfigurationReader pluginConfigurationReader = new PluginConfigurationReaderImpl();
        modelPluginRepository = new ModelPluginRepositoryImpl(pluginConfigurationReader);
        scannerPluginRepository = new ScannerPluginRepositoryImpl(pluginConfigurationReader, Collections.<String, Object> emptyMap());
        rulePluginRepository = new RulePluginRepositoryImpl(pluginConfigurationReader);

        // Create a store and start it
        store = new EmbeddedGraphStore("target/store");
        store.start(modelPluginRepository.getDescriptorTypes());

    }

    /**
     * Stops the store instance.
     */
    @After
    public void stopStore() throws IOException {
        Server server = new DefaultServerImpl(store, scannerPluginRepository, rulePluginRepository);
        server.start();
        System.out.println("Hit Enter to continue.");
        System.in.read();
        server.stop();

        // Stop the store
        store.stop();
    }

    /**
     * Scans a java file.
     *
     * @throws PluginRepositoryException
     *              If there's a problem with the plugin configuration
     */
    @Test
    public void scanJava() throws PluginRepositoryException {
        store.reset();

        Scanner scanner = new ScannerImpl(store, scannerPluginRepository.getScannerPlugins());

        store.beginTransaction();
        File file = new File("src/test/data/foo/bar/baz/FooClass.java");
        scanner.scan(file, file.getAbsolutePath(), null);
        store.commitTransaction();
    }

    /**
     * Starts the embedded Neo4j server, it will be available unter
     * http://localhost:7474.
     * 
     * @throws IOException
     *             If the console repeats an error.
     */
    @Test
    @Ignore
    public void server() throws IOException {
        Server server = new DefaultServerImpl(store, scannerPluginRepository, rulePluginRepository);
        server.start();
        System.out.println("Hit Enter to continue.");
        System.in.read();
        server.stop();
    }
}
