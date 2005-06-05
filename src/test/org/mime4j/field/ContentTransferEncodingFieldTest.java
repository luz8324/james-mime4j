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
package org.mime4j.field;

import org.mime4j.field.ContentTransferEncodingField;
import org.mime4j.field.Field;

import junit.framework.TestCase;

/**
 * 
 *
 * @author Niklas Therning
 * @version $Id: ContentTransferEncodingFieldTest.java,v 1.2 2004/10/02 12:41:11 ntherning Exp $
 */
public class ContentTransferEncodingFieldTest extends TestCase {

    public void testGetEncoding() {
        ContentTransferEncodingField f = null;
        
        f = (ContentTransferEncodingField) 
                    Field.parse("Content-Transfer-Encoding: 8bit");
        assertEquals("8bit", f.getEncoding());
        
        f = (ContentTransferEncodingField) 
                    Field.parse("Content-Transfer-Encoding:    BaSE64   ");
        assertEquals("base64", f.getEncoding());
        
        f = (ContentTransferEncodingField) 
                    Field.parse("Content-Transfer-Encoding:       ");
        assertEquals("", f.getEncoding());
        
        f = (ContentTransferEncodingField) 
                    Field.parse("Content-Transfer-Encoding:");
        assertEquals("", f.getEncoding());
    }
    
    public void testGetEncodingStatic() {
        ContentTransferEncodingField f = null;
        
        f = (ContentTransferEncodingField) 
                    Field.parse("Content-Transfer-Encoding: 8bit");
        assertEquals("8bit", ContentTransferEncodingField.getEncoding(f));
        
        f = null;
        assertEquals("7bit", ContentTransferEncodingField.getEncoding(f));
        
        f = (ContentTransferEncodingField) 
                    Field.parse("Content-Transfer-Encoding:       ");
        assertEquals("7bit", ContentTransferEncodingField.getEncoding(f));
        
        f = (ContentTransferEncodingField) 
                    Field.parse("Content-Transfer-Encoding:");
        assertEquals("7bit", ContentTransferEncodingField.getEncoding(f));
    }

}
