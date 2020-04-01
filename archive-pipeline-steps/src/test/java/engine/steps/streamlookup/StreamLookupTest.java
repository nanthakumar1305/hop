/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.apache.hop.pipeline.steps.streamlookup;

import junit.framework.Assert;
import org.apache.hop.core.QueueRowSet;
import org.apache.hop.core.RowSet;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.exception.HopStepException;
import org.apache.hop.core.logging.LoggingObjectInterface;
import org.apache.hop.core.row.RowMeta;
import org.apache.hop.core.row.RowMetaInterface;
import org.apache.hop.core.row.ValueMetaInterface;
import org.apache.hop.core.row.value.ValueMetaString;
import org.apache.hop.core.variables.VariableSpace;
import org.apache.hop.metastore.api.IMetaStore;
import org.apache.hop.pipeline.step.StepIOMeta;
import org.apache.hop.pipeline.step.StepMeta;
import org.apache.hop.pipeline.step.errorhandling.Stream;
import org.apache.hop.pipeline.step.errorhandling.StreamIcon;
import org.apache.hop.pipeline.step.errorhandling.StreamInterface;
import org.apache.hop.pipeline.steps.mock.StepMockHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for StreamLookup step
 *
 * @author Pavel Sakun
 * @see StreamLookup
 */
public class StreamLookupTest {
  private StepMockHelper<StreamLookupMeta, StreamLookupData> smh;

  @Before
  public void setUp() {
    smh =
      new StepMockHelper<StreamLookupMeta, StreamLookupData>( "StreamLookup", StreamLookupMeta.class,
        StreamLookupData.class );
    when( smh.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
      smh.logChannelInterface );
    when( smh.pipeline.isRunning() ).thenReturn( true );
  }

  @After
  public void cleanUp() {
    smh.cleanUp();
  }

  private void convertDataToBinary( Object[][] data ) {
    for ( int i = 0; i < data.length; i++ ) {
      for ( int j = 0; j < data[ i ].length; j++ ) {
        data[ i ][ j ] = ( (String) data[ i ][ j ] ).getBytes();
      }
    }
  }

  private RowSet mockLookupRowSet( boolean binary ) {
    final int storageType = binary ? ValueMetaInterface.STORAGE_TYPE_BINARY_STRING : ValueMetaInterface.STORAGE_TYPE_NORMAL;
    Object[][] data = { { "Value1", "1" }, { "Value2", "2" } };

    if ( binary ) {
      convertDataToBinary( data );
    }

    RowSet lookupRowSet =
      smh.getMockInputRowSet( data );
    doReturn( "Lookup" ).when( lookupRowSet ).getOriginStepName();
    doReturn( "StreamLookup" ).when( lookupRowSet ).getDestinationStepName();

    RowMeta lookupRowMeta = new RowMeta();
    ValueMetaString valueMeta = new ValueMetaString( "Value" );
    valueMeta.setStorageType( storageType );
    valueMeta.setStorageMetadata( new ValueMetaString() );
    lookupRowMeta.addValueMeta( valueMeta );
    ValueMetaString idMeta = new ValueMetaString( "Id" );
    idMeta.setStorageType( storageType );
    idMeta.setStorageMetadata( new ValueMetaString() );
    lookupRowMeta.addValueMeta( idMeta );

    doReturn( lookupRowMeta ).when( lookupRowSet ).getRowMeta();

    return lookupRowSet;
  }

  private RowSet mockDataRowSet( boolean binary ) {
    final int storageType = binary ? ValueMetaInterface.STORAGE_TYPE_BINARY_STRING : ValueMetaInterface.STORAGE_TYPE_NORMAL;
    Object[][] data = { { "Name1", "1" }, { "Name2", "2" } };

    if ( binary ) {
      convertDataToBinary( data );
    }

    RowSet dataRowSet = smh.getMockInputRowSet( data );

    RowMeta dataRowMeta = new RowMeta();
    ValueMetaString valueMeta = new ValueMetaString( "Name" );
    valueMeta.setStorageType( storageType );
    valueMeta.setStorageMetadata( new ValueMetaString() );
    dataRowMeta.addValueMeta( valueMeta );
    ValueMetaString idMeta = new ValueMetaString( "Id" );
    idMeta.setStorageType( storageType );
    idMeta.setStorageMetadata( new ValueMetaString() );
    dataRowMeta.addValueMeta( idMeta );

    doReturn( dataRowMeta ).when( dataRowSet ).getRowMeta();

    return dataRowSet;
  }

  private StreamLookupMeta mockProcessRowMeta( boolean memoryPreservationActive ) throws HopStepException {
    StreamLookupMeta meta = smh.processRowsStepMetaInterface;

    StepMeta lookupStepMeta = when( mock( StepMeta.class ).getName() ).thenReturn( "Lookup" ).getMock();
    doReturn( lookupStepMeta ).when( smh.pipelineMeta ).findStep( "Lookup" );

    StepIOMeta stepIOMeta = new StepIOMeta( true, true, false, false, false, false );
    stepIOMeta.addStream( new Stream( StreamInterface.StreamType.INFO, lookupStepMeta, null, StreamIcon.INFO, null ) );

    doReturn( stepIOMeta ).when( meta ).getStepIOMeta();
    doReturn( new String[] { "Id" } ).when( meta ).getKeylookup();
    doReturn( new String[] { "Id" } ).when( meta ).getKeystream();
    doReturn( new String[] { "Value" } ).when( meta ).getValue();
    doReturn( memoryPreservationActive ).when( meta ).isMemoryPreservationActive();
    doReturn( false ).when( meta ).isUsingSortedList();
    doReturn( false ).when( meta ).isUsingIntegerPair();
    doReturn( new int[] { -1 } ).when( meta ).getValueDefaultType();
    doReturn( new String[] { "" } ).when( meta ).getValueDefault();
    doReturn( new String[] { "Value" } ).when( meta ).getValueName();
    doReturn( new String[] { "Value" } ).when( meta ).getValue();
    doCallRealMethod().when( meta ).getFields( any( RowMetaInterface.class ), anyString(), any( RowMetaInterface[].class ), any( StepMeta.class ),
      any( VariableSpace.class ), any( IMetaStore.class ) );

    return meta;
  }

  private void doTest( boolean memoryPreservationActive, boolean binaryLookupStream, boolean binaryDataStream ) throws HopException {
    StreamLookup step = new StreamLookup( smh.stepMeta, smh.stepDataInterface, 0, smh.pipelineMeta, smh.pipeline );
    step.init( smh.initStepMetaInterface, smh.initStepDataInterface );
    step.addRowSetToInputRowSets( mockLookupRowSet( binaryLookupStream ) );
    step.addRowSetToInputRowSets( mockDataRowSet( binaryDataStream ) );
    step.addRowSetToOutputRowSets( new QueueRowSet() );

    StreamLookupMeta meta = mockProcessRowMeta( memoryPreservationActive );
    StreamLookupData data = new StreamLookupData();
    data.readLookupValues = true;

    RowSet outputRowSet = step.getOutputRowSets().get( 0 );

    // Process rows and collect output
    int rowNumber = 0;
    String[] expectedOutput = { "Name", "", "Value" };
    while ( step.processRow( meta, data ) ) {
      Object[] rowData = outputRowSet.getRow();
      if ( rowData != null ) {
        RowMetaInterface rowMeta = outputRowSet.getRowMeta();
        Assert.assertEquals( "Output row is of wrong size", 3, rowMeta.size() );
        rowNumber++;
        // Verify output
        for ( int valueIndex = 0; valueIndex < rowMeta.size(); valueIndex++ ) {
          String expectedValue = expectedOutput[ valueIndex ] + rowNumber;
          Object actualValue = rowMeta.getValueMeta( valueIndex ).convertToNormalStorageType( rowData[ valueIndex ] );
          Assert.assertEquals( "Unexpected value at row " + rowNumber + " position " + valueIndex, expectedValue,
            actualValue );
        }
      }
    }

    Assert.assertEquals( "Incorrect output row number", 2, rowNumber );
  }

  @Test
  public void testWithNormalStreams() throws HopException {
    doTest( false, false, false );
  }

  @Test
  public void testWithBinaryLookupStream() throws HopException {
    doTest( false, true, false );
  }

  @Test
  public void testWithBinaryDateStream() throws HopException {
    doTest( false, false, true );
  }

  @Test
  public void testWithBinaryStreams() throws HopException {
    doTest( false, false, true );
  }

  @Test
  public void testMemoryPreservationWithNormalStreams() throws HopException {
    doTest( true, false, false );
  }

  @Test
  public void testMemoryPreservationWithBinaryLookupStream() throws HopException {
    doTest( true, true, false );
  }

  @Test
  public void testMemoryPreservationWithBinaryDateStream() throws HopException {
    doTest( true, false, true );
  }

  @Test
  public void testMemoryPreservationWithBinaryStreams() throws HopException {
    doTest( true, false, true );
  }
}