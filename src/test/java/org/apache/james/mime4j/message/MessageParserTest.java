/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/

package org.apache.james.mime4j.message;

import org.apache.commons.io.IOUtils;
import org.apache.james.mime4j.field.Field;
import org.apache.james.mime4j.util.CharsetUtil;
import org.apache.log4j.BasicConfigurator;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * 
 * 
 *
 * 
 * @version $Id: MessageParserTest.java,v 1.4 2004/10/25 07:26:47 ntherning Exp $
 */
public class MessageParserTest extends TestCase {
    private String fileName = null;

    public MessageParserTest(String name, String fileName) {
        super(name);
        
        this.fileName = fileName;
    }

    public void setUp() {
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
    }
        
    public static Test suite() {
        TestSuite suite = new TestSuite();
        
        File dir = new File("src/test/resources/testmsgs");
        File[] files = dir.listFiles();
        
        for (int i = 0; i < files.length && i < 5000; i++) {
            File f = files[i];
            if (f.getName().toLowerCase().endsWith(".msg")) {
                suite.addTest(new MessageParserTest(f.getName(), 
                                                        f.getAbsolutePath()));
            }
        }
        
        return suite;
    }
    
    protected void runTest() throws IOException {
        File f = new File(fileName);
        
        System.out.println("Parsing " + f.getName());
        
        Message m = new Message(new FileInputStream(f));
        
        String prefix = f.getName().substring(0, f.getName().length() - 4);
        String xmlFileName = fileName.substring(0, fileName.length() - 4) 
                                    + "_decoded.xml";
        
        String result = getStructure(m, prefix, "1");
        String mime4jFileName = fileName.substring(0, fileName.length() - 4) 
                                    + "_decoded.mime4j.xml";
        String expected = null;
        try {
            expected = IOUtils.toString(new FileInputStream(xmlFileName), "ISO8859-1");
        } catch (FileNotFoundException ex) {
            writeToFile(result, mime4jFileName);
            fail("Test file not found. Generated the expected result with mime4j prefix: "+ex.getMessage());
        }
        try {
            assertEquals(expected, result);
        } catch (AssertionError ae) {
            writeToFile(result, mime4jFileName);
            throw ae;
        }
    }

    private void writeToFile(String result, String mime4jFileName)
            throws FileNotFoundException, IOException,
            UnsupportedEncodingException {
        FileOutputStream out = new FileOutputStream(mime4jFileName);
        out.write(result.getBytes("ISO8859-1"));
        out.close();
    }
    
    private String escape(String s) {
        s = s.replaceAll("&", "&amp;");
        s = s.replaceAll("<", "&lt;");
        return s.replaceAll(">", "&gt;");
    }

    private String getStructure(Entity e, String prefix, String id) 
            throws IOException {
        
        StringBuffer sb = new StringBuffer();
        
        if (e instanceof Message) {
            sb.append("<message>\r\n");
        } else {
            sb.append("<body-part>\r\n");
        }            
        
        sb.append("<header>\r\n");
        for (Iterator it = e.getHeader().getFields().iterator(); it.hasNext();) {
            sb.append("<field>\r\n" + escape(((Field) it.next()).getRaw()) 
                    + "</field>\r\n");
        }
        sb.append("</header>\r\n");
        
        if (e.getBody() instanceof Multipart) {
            sb.append("<multipart>\r\n");
            
            Multipart multipart =(Multipart) e.getBody(); 
            List parts =multipart.getBodyParts();

            sb.append("<preamble>\r\n");
            sb.append(escape(multipart.getPreamble()));
            sb.append("</preamble>\r\n");
            
            int i = 1;
            for (Iterator it = parts.iterator(); it.hasNext();) {
                sb.append(getStructure((Entity) it.next(), prefix, id + "_" + (i++)));
            }

            sb.append("<epilogue>\r\n");
            sb.append(escape(multipart.getEpilogue()));
            sb.append("</epilogue>\r\n");
            
            sb.append("</multipart>\r\n");
            
        } else if (e.getBody() instanceof Message) {
            sb.append(getStructure((Message) e.getBody(), prefix, id + "_1"));
        } else {
            Body b = e.getBody();
            String name = prefix + "_decoded_" + id 
                            + (b instanceof TextBody ? ".txt" : ".bin");
            String tag = b instanceof TextBody ? "text-body" : "binary-body";
            sb.append("<" + tag + " name=\"" + name + "\"/>\r\n");
                
            File expectedFile = new File(new File(fileName).getParent(), name);
            File mime4jFile = new File(new File(fileName).getParent(), 
                              name.substring(0, name.length() - 4) + ".mime4j"
                               + (b instanceof TextBody ? ".txt" : ".bin"));
                
            InputStream expected = null;
            try {
                expected = new BufferedInputStream(new FileInputStream(expectedFile));
            } catch (FileNotFoundException ex) {
                writeToFile(b, mime4jFile);
                fail("Test file not found. Generated the expected result with mime4j prefix: "+ex.getMessage());
            }
            
            try {
                if (b instanceof TextBody) {
                    String charset = CharsetUtil.toJavaCharset(e.getCharset());
                    if (charset == null) {
                        charset = "ISO8859-1";
                    }

                    String s1 = IOUtils.toString(expected, charset).replaceAll(
                            "\r", "");
                    String s2 = IOUtils.toString(((TextBody) b).getReader())
                            .replaceAll("\r", "");
                    assertEquals(expectedFile.getName(), s1, s2);
                } else {
                    assertEqualsBinary(expectedFile.getName(), expected,
                            ((BinaryBody) b).getInputStream());
                }
            } catch (AssertionError er) {
                writeToFile(b, mime4jFile);
                throw er;
            }
        }
        
        
        if (e instanceof Message) {
            sb.append("</message>\r\n");
        } else {
            sb.append("</body-part>\r\n");
        }            
        
        return sb.toString();
    }

    private void writeToFile(Body b, File mime4jFile)
            throws FileNotFoundException, IOException {
        if (b instanceof TextBody) {
            OutputStream out = new FileOutputStream(mime4jFile);
            IOUtils.copy(((TextBody) b).getReader(), out);
        } else {
            OutputStream out = new FileOutputStream(mime4jFile);
            IOUtils.copy(((BinaryBody) b).getInputStream(), out);
        }
    }

    private void assertEqualsBinary(String msg, InputStream a, InputStream b) 
            throws IOException {
        
        int pos = 0;
        while (true) {
            int b1 = a.read();
            int b2 = b.read();
            assertEquals(msg + " (Position " + (++pos) + ")", b1, b2);
            
            if (b1 == -1 || b2 == -1) {
                break;
            }
        }
    }
}