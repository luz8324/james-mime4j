/*
 *  Copyright 2004 the mime4j project
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.mime4j.message;

import java.io.IOException;
import java.io.InputStream;


/**
 * Interface implemented by bodies containing binary data.
 *
 * @author Niklas Therning
 * @version $Id: BinaryBody.java,v 1.3 2004/10/02 12:41:11 ntherning Exp $
 */
public interface BinaryBody extends Body {
    /**
     * Gets a <code>InputStream</code> which reads the bytes of the
     * body.
     * 
     * @return the stream.
     * @throws IOException on I/O errors.
     */
    InputStream getInputStream() throws IOException;
}
