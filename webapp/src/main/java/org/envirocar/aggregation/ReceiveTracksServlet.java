/**
 * Copyright 2016 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.envirocar.aggregation;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.envirocar.analyse.AggregationAlgorithm;
import org.envirocar.analyse.util.PointViaJsonMapIterator;
import org.envirocar.analyse.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.logging.Level;

@Singleton
public class ReceiveTracksServlet extends HttpServlet {
    
    /**
     *
     */
    private static final long serialVersionUID = 4589872023160154399L;
    private static final Logger logger = LoggerFactory
            .getLogger(ReceiveTracksServlet.class);
    private static final String PRODUCERS_FILE = "/allowed_producers.cfg";
    private ExecutorService executor;
    
    private Set<String> allowedProducers;
    private List<AggregationAlgorithm> algorithms = new ArrayList<>();
    
    public ReceiveTracksServlet() throws IOException {
        this.executor = Executors.newSingleThreadExecutor();
        logger.info("Servlet created");
    }
    
    @Override
    public void init() throws ServletException {
        super.init();
        try {
            instantiateAlgorithmInstances();
            this.allowedProducers = FileUtil
                    .readConfigFilePerLine(PRODUCERS_FILE);
        } catch (IOException e) {
            throw new ServletException(e);
        }
        
    }
    
    @Override
    public void destroy() {
        super.destroy();
        this.executor.shutdown();
        
        try {
            for (AggregationAlgorithm algorithm : algorithms) {
                algorithm.shutdown();
            }
        } catch (IOException e) {
            logger.warn(e.getMessage(), e);
        }
    }
    
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        final String contentType = req.getHeader("Content-Type");
        
        final Path tempFile = Util.writeToTempFile(req.getInputStream());
        
        if (!(contentType != null && contentType.startsWith("application/json"))) {
            throw new IllegalArgumentException("Invalid ContentType");
        }
        
        if (verifyRemoteHost(req.getRemoteHost())) {
            this.executor.submit(new Runnable() {
                public void run() {
                    final Map<?, ?> json;
                    try {
                        InputStream stream = Files.newInputStream(tempFile);
                        json = Utils.parseJsonStream(stream);
                    } catch (IOException ex) {
                        logger.warn("Could not parse json temp file", ex);
                        return;
                    }

                    try {
                        Files.delete(tempFile);
                    } catch (IOException ex) {
                        logger.warn("Could not delete temp file", ex);
                    }
                    
                    for (AggregationAlgorithm algorithm : algorithms) {
                        try {
                            PointViaJsonMapIterator it = new PointViaJsonMapIterator(json);
                            algorithm.runAlgorithm(it, it.getOriginalTrackId());
                        } catch (IOException e) {
                            logger.warn("Could not inizialize iterator. Skipping track.", e);
                            continue;
                        }                        
                    }
                }
                
            });
        } else {
            logger.info("Host {} is not whitelisted. Ignoring request.",
                    req.getRemoteHost());
        }
        
        resp.setStatus(204);
    }
    
    public synchronized Set<String> getAllowedProducers() {
        return this.allowedProducers;
    }
    
    private synchronized boolean verifyRemoteHost(String remoteHost) {
        for (String prod : getAllowedProducers()) {
            if (remoteHost.contains(prod)) {
                return true;
            }
        }
        return false;
    }
    
    protected String readContent(HttpServletRequest req) throws IOException {
        String enc = req.getCharacterEncoding();
        Scanner sc = new Scanner(req.getInputStream(), enc == null ? "utf-8"
                : enc);
        StringBuilder sb = new StringBuilder();
        
        while (sc.hasNext()) {
            sb.append(sc.nextLine());
        }
        
        sc.close();
        return sb.toString();
    }

    private void instantiateAlgorithmInstances() throws IOException {
        for (Properties algorithmConfiguration : Util.getAlgorithmConfigurations()) {
            algorithms.add(new AggregationAlgorithm(algorithmConfiguration));
        }
    }
    
    public static class LocalGuiceServletConfig extends
            GuiceServletContextListener {
        
        @Override
        protected Injector getInjector() {
            ServiceLoader<Module> loader = ServiceLoader.load(Module.class);
            
            List<Module> modules = new ArrayList<Module>();
            for (Module module : loader) {
                modules.add(module);
            }
            
            modules.add(new ServletModule() {
                
                @Override
                protected void configureServlets() {
                    serve("/*").with(ReceiveTracksServlet.class);
                }
                
            });
            
            return Guice.createInjector(modules);
        }
    }
    
}
