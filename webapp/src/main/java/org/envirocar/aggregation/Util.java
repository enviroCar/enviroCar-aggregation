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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.envirocar.aggregation;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author matthes
 */
public class Util {
    
    private static final Logger logger = LoggerFactory.getLogger(Util.class);
    
    public static Collection<Properties> getAlgorithmConfigurations() throws IOException {
        URL res = Util.class.getResource("/algorithm_instances/README.md");
        final List<Properties> props = new ArrayList<>();
        
        if (res != null) {
            try {
                Path instanceReadme = Paths.get(res.toURI());
                if (Files.exists(instanceReadme)) {
                    Files.newDirectoryStream(instanceReadme.getParent(), "*.{properties}").forEach(new Consumer<Path>() {
                        @Override
                        public void accept(Path t) {
                            Properties p = new Properties();
                            try {
                                p.load(Files.newBufferedReader(t));
                                props.add(p);
                            } catch (IOException ex) {
                                logger.warn("Could not load algorithm instnace: "+p, ex);
                            }
                        }
                    });
                }
            } catch (URISyntaxException ex) {
                throw new IOException(ex);
            }
        }
        
        return props;
    }
    
}
