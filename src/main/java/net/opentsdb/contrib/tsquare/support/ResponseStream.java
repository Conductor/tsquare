/*
 * Copyright (C) 2013 Conductor, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.opentsdb.contrib.tsquare.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.google.common.io.Closeables;
import com.google.common.io.CountingOutputStream;
import com.google.common.io.FileBackedOutputStream;
import com.google.common.io.InputSupplier;

/**
 * @author James Royalty (jroyalty) <i>[Jun 21, 2013]</i>
 */
public final class ResponseStream extends OutputStream {
    private FileBackedOutputStream fileStream;
    private CountingOutputStream countingStream;

    // Reference to the stream we'll be writing to.
    private OutputStream _target;

    public ResponseStream(final int bufferSize) {
        fileStream = new FileBackedOutputStream(bufferSize, true);
        countingStream = new CountingOutputStream(fileStream);
        
        _target = countingStream;
    }
    
    public InputSupplier<InputStream> getSupplier() {
        return fileStream.getSupplier();
    }
    
    public long getNumBytesWritten() {
        return countingStream.getCount();
    }
    
    public void release() throws IOException {
        Closeables.close(countingStream, true);
        fileStream.reset();
    }
    
    public static void releaseQuietly(final ResponseStream stream) {
        if (stream != null) {
            try {
                stream.release();
            } catch (Exception e) {
                // Swallow.
            }
        }
    }
    
    @Override
    public void write(int b) throws IOException {
        _target.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        _target.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        _target.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        _target.flush();
    }

    @Override
    public void close() throws IOException {
        _target.close();
    }
}
