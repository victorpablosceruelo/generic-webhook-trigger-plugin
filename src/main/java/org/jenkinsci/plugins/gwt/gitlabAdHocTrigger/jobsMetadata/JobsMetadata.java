/*
 * The MIT License
 *
 * Copyright 2020 me.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.plugins.gwt.gitlabAdHocTrigger.jobsMetadata;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 *
 * @author me
 */
public class JobsMetadata {
    
    private static int MINS_BETWEEN_UPDATES = 10;
    
    private static JobsMetadata instance = null;
    private static Object mutex = new Object();
    
    private Date nextUpdate;
    private File repositoryFolder;
    
    private JobsMetadata(String metadataRepositoryUrl) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, MINS_BETWEEN_UPDATES);
        nextUpdate = calendar.getTime();
        
        repositoryFolder = updateRepositoryLocalCopy(metadataRepositoryUrl);
    }
    
    public static JobsMetadata getInstance(String metadataRepositoryUrl) {
        synchronized(mutex) {
            if ((instance == null) || (instance.isRatherOld())) {
                JobsMetadata localInstance = new JobsMetadata(metadataRepositoryUrl);
                instance = localInstance;
            }
            return instance;
        }
    }

    private boolean isRatherOld() {
        return (nextUpdate.before(new Date()));
    }

    private File updateRepositoryLocalCopy(String metadataRepositoryUrl) {
        // Create script used to download repository.
        // Run the script.
        return new File("");
    }
    
    private File getJobMetadataFile(String jobFullNameWithoutTail, String jobNameTail) {
        return new File("");
    }

    public void injectJobMetadataAsResolvedVariables(String jobFullNameWithoutTail, String jobNameTail, Map<String, String> resolvedVariables) {
        // getJobMetadataFile
        // read job metadata file
        // add the variables to the map.
    }
    
    
}
