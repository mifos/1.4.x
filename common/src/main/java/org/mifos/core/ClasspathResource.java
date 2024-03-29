/*
 * Copyright (c) 2005-2009 Grameen Foundation USA
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * See also http://www.apache.org/licenses/LICENSE-2.0.html for an
 * explanation of the license and how it is applied.
 */
 
package org.mifos.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Load resources from the classpath. 
 * 
 */

public class ClasspathResource {

    public static ClasspathResource getInstance(String path) {
        return new ClasspathResource(path);
    }
    
    /**
     * Returns the URI for the file name specified.
     * It tries to load the file using the class loader and then 
     * returns the URI corresponding to the file.
     *
     * Returns null if the file is not found (or, perhaps, if we aren't
     * allowed to see it).
     */
    public static URI getURI(String fileName) throws URISyntaxException{
        ClassLoader parent = ClasspathResource.class.getClassLoader().getParent();
        ClassLoader current = ClasspathResource.class.getClassLoader();
        URI uri = null;
        URL url = parent.getResource(fileName);
        if(null == url){
             url = current.getResource(fileName);
        }

        if(null!= url){
            // Encoding spaces in URL in order to fix issue 1759 (https://mifos.dev.java.net/issues/show_bug.cgi?id=1759)
            String encodedURL = url.toString().replaceAll(" ", "%20");
            uri = new URI(encodedURL);          
        }

        return uri;
    }

    /**
     * Works exactly like {@link #getURI(String)} except the
     * {@link URISyntaxException}, if caught, is wrapped in a
     * {@link RuntimeException}.
     */
    public static URI findResource(String fileName) {
        try {
            return getURI(fileName);
        }
        catch (URISyntaxException e) {
            throw new MifosRuntimeException(e);
        }
    }
    
    protected String path;

    public ClasspathResource(String path) {
        super();
        this.path = path;
    }
    
    public URL getUrl(String name) {
        String resourcePath = this.path + name;
        return ClasspathResource.class.getResource(resourcePath);
    }
    
    public InputStream getAsStream(String name) throws IOException {
        return getUrl(name).openStream();
    }

    public Reader getAsReader(String name) {
       try {
         return new BufferedReader(new InputStreamReader(this.getAsStream(name)));
       } catch (IOException e) {
         throw new MifosRuntimeException(e);
       }
    }
}
