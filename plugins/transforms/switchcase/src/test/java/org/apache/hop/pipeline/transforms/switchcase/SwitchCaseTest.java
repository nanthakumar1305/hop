/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hop.pipeline.transforms.switchcase;

import org.apache.hop.core.IRowSet;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.exception.HopTransformException;
import org.apache.hop.core.exception.HopValueException;
import org.apache.hop.core.logging.ILoggingObject;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.pipeline.transforms.mock.TransformMockHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SwitchCaseTest {

  private TransformMockHelper<SwitchCaseMeta, SwitchCaseData> mockHelper;
  private static Boolean EMPTY_STRING_AND_NULL_ARE_DIFFERENT = false;

  @Before
  public void setUp() throws Exception {
    mockHelper =
        new TransformMockHelper<>("Switch Case", SwitchCaseMeta.class, SwitchCaseData.class);
    when(mockHelper.logChannelFactory.create(any(), any(ILoggingObject.class)))
        .thenReturn(mockHelper.iLogChannel);
    when(mockHelper.pipeline.isRunning()).thenReturn(true);
  }

  @After
  public void tearDown() throws Exception {
    mockHelper.cleanUp();
  }

  /**
   * Load local xml data for case-value mapping, transform info.
   *
   * @return
   * @throws URISyntaxException
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws IOException
   */
  private static Node loadTransformXmlMetadata(String fileName)
      throws URISyntaxException, ParserConfigurationException, SAXException, IOException {
    String PKG = SwitchCaseTest.class.getPackage().getName().replace(".", "/");
    PKG = PKG + "/";
    URL url = SwitchCaseTest.class.getClassLoader().getResource(PKG + fileName);
    File file = new File(url.toURI());
    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    Document doc = dBuilder.parse(file);
    NodeList nList = doc.getElementsByTagName("transform");
    return nList.item(0);
  }

  @Test
  public void prepareObjectTypeBinaryTest_Equals() throws Exception {
    assertEquals(
        Arrays.hashCode(new byte[] {1, 2, 3}), SwitchCase.prepareObjectType(new byte[] {1, 2, 3}));
  }

  @Test
  public void prepareObjectTypeBinaryTest_NotEquals() throws Exception {
    assertNotEquals(
        Arrays.hashCode(new byte[] {1, 2, 4}), SwitchCase.prepareObjectType(new byte[] {1, 2, 3}));
  }

  @Test
  public void prepareObjectTypeBinaryTest_Null() throws Exception {
    byte[] given = null;
    byte[] expected = null;

    assertEquals(expected, SwitchCase.prepareObjectType(given));
  }

  @Test
  public void prepareObjectTypeTest_Equals() throws Exception {
    assertEquals("2", SwitchCase.prepareObjectType("2"));
  }

  @Test
  public void prepareObjectTypeTest_NotEquals() throws Exception {
    assertNotEquals("2", SwitchCase.prepareObjectType("1"));
  }

  @Test
  public void prepareObjectTypeTest_Null() throws Exception {
    assertNull(SwitchCase.prepareObjectType(null));
  }

  /**
   * Switch case transform ancestor with overridden methods to have ability to simulate normal
   * pipeline execution.
   */
  private static class SwitchCaseCustom extends SwitchCase {

    Queue<Object[]> input = new LinkedList<>();
    IRowMeta iRowMeta;

    // we will use real data and meta.
    SwitchCaseData data = new SwitchCaseData();
    SwitchCaseMeta meta = new SwitchCaseMeta();

    Map<String, IRowSet> map = new HashMap<>();

    SwitchCaseCustom(TransformMockHelper<SwitchCaseMeta, SwitchCaseData> mockHelper)
        throws HopValueException {
      super(
          mockHelper.transformMeta,
          mockHelper.iTransformMeta,
          mockHelper.iTransformData,
          0,
          mockHelper.pipelineMeta,
          mockHelper.pipeline);
      // this.mockHelper = mockHelper;
      init();

      // call to convert value will returns same value.
      data.valueMeta = mock(IValueMeta.class);
      when(data.valueMeta.convertData(any(IValueMeta.class), any()))
          .thenAnswer(
              invocation -> {
                Object[] objArr = invocation.getArguments();
                return (objArr != null && objArr.length > 1) ? objArr[1] : null;
              });
      // same when call to convertDataFromString
      when(data.valueMeta.convertDataFromString(
              Mockito.anyString(),
              any(IValueMeta.class),
              Mockito.anyString(),
              Mockito.anyString(),
              Mockito.anyInt()))
          .thenAnswer(
              // CHECKSTYLE:Indentation:OFF
              invocation -> {
                Object[] objArr = invocation.getArguments();
                return (objArr != null && objArr.length > 1) ? objArr[0] : null;
              });
      // null-check
      when(data.valueMeta.isNull(any()))
          .thenAnswer(
              invocation -> {
                Object[] objArr = invocation.getArguments();
                Object obj = objArr[0];
                if (obj == null) {
                  return true;
                }
                if (EMPTY_STRING_AND_NULL_ARE_DIFFERENT) {
                  return false;
                }

                // If it's a string and the string is empty, it's a null value as well
                //
                if (obj instanceof String) {
                  if (((String) obj).length() == 0) {
                    return true;
                  }
                }
                return false;
              });
    }

    /**
     * used for input row generation
     *
     * @param start
     * @param finish
     * @param copy
     */
    void generateData(int start, int finish, int copy) {
      input.clear();
      for (int i = start; i <= finish; i++) {
        for (int j = 0; j < copy; j++) {
          input.add(new Object[] {i});
        }
        input.add(new Object[] {null});
      }
      input.add(new Object[] {""});
    }

    /**
     * useful to see generated data as String
     *
     * @return
     */
    @SuppressWarnings("unused")
    public String getInputDataOverview() {
      StringBuilder sb = new StringBuilder();
      for (Object[] row : input) {
        sb.append(row[0] + ", ");
      }
      return sb.toString();
    }

    /** mock transform data processing */
    @Override
    public Object[] getRow() throws HopException {
      return input.poll();
    }

    /**
     * simulate concurrent execution
     *
     * @throws HopException
     */
    @Override
    public boolean processRow() throws HopException {
      boolean run = false;
      do {
        run = init();
      } while (run);
      return true;
    }

    @Override
    public IRowSet findOutputRowSet(String targetTransform) throws HopTransformException {
      return map.get(targetTransform);
    }

    @Override
    public IRowMeta getInputRowMeta() {
      if (iRowMeta == null) {
        iRowMeta = getDynamicRowMetaInterface();
      }
      return iRowMeta;
    }

    private IRowMeta getDynamicRowMetaInterface() {
      IRowMeta inputRowMeta = mock(IRowMeta.class);
      return inputRowMeta;
    }
  }
}
