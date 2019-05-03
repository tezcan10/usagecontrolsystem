package it.cnr.iit.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import it.cnr.iit.pipreader.PIPReader;
import it.cnr.iit.ucs.builders.PIPBuilder;
import it.cnr.iit.ucsinterface.contexthandler.ContextHandlerInterface;
import it.cnr.iit.utility.JAXBUtility;
import it.cnr.iit.utility.JsonUtility;
import it.cnr.iit.utility.errorhandling.exception.PreconditionException;
import it.cnr.iit.xacmlutilities.Attribute;
import it.cnr.iit.xacmlutilities.Category;
import it.cnr.iit.xacmlutilities.DataType;

import oasis.names.tc.xacml.core.schema.wd_17.AttributeType;
import oasis.names.tc.xacml.core.schema.wd_17.AttributeValueType;
import oasis.names.tc.xacml.core.schema.wd_17.AttributesType;
import oasis.names.tc.xacml.core.schema.wd_17.RequestType;

@EnableConfigurationProperties
@TestPropertySource( properties = "application.properties" )
@ActiveProfiles( "test" )
@RunWith( SpringRunner.class )
@SpringBootTest
@SpringBootConfiguration
public class CoverageTest {

    private static Logger log = Logger.getLogger( CoverageTest.class.getName() );

    @Value( "${environment.filepath}" )
    private String environmentFilePath;

    @Value( "${requestType}" )
    private String requestS;

    @Value( "${subjectPip}" )
    private String subjectPip;

    @Value( "${resourcePip}" )
    private String resourcePip;

    @Value( "${actionPip}" )
    private String actionPip;

    @Value( "${environmentPip}" )
    private String environmentPip;

    @Value( "${missingCategory}" )
    private String missingCategory;

    @Value( "${missingAttributeId}" )
    private String missingAttributeId;

    @Value( "${missingDataType}" )
    private String missingDataType;

    @Value( "${missingFilePath}" )
    private String missingFilePath;

    @Value( "${missingExpectedCategory}" )
    private String missingExpectedCategory;

    @Value( "${malformedInput}" )
    private String malformedInput;

    private RequestType requestType = new RequestType();
    private PIPReader subjectAttributePip;
    private PIPReader resourceAttributePip;
    private PIPReader actionAttributePip;
    private PIPReader environmentAttributePip;
    private PIPReader fault;
    private Attribute subjectAttribute = new Attribute();
    private Attribute resourceAttribute = new Attribute();
    private Attribute actionAttribute = new Attribute();
    private Attribute environmentAttribute = new Attribute();

    private ContextHandlerInterface contextHandler;

    public void init() {
        try {
            resetRequest();
            contextHandler = Mockito.mock( ContextHandlerInterface.class );
            subjectAttributePip = new PIPReader( PIPBuilder.getPropertiesFromString( subjectPip ).get() );
            resourceAttributePip = new PIPReader( PIPBuilder.getPropertiesFromString( resourcePip ).get() );
            actionAttributePip = new PIPReader( PIPBuilder.getPropertiesFromString( actionPip ).get() );
            environmentAttributePip = new PIPReader( PIPBuilder.getPropertiesFromString( environmentPip ).get() );
            assertTrue( subjectAttributePip.isInitialised() );
            initAttributes();
            subjectAttributePip.setContextHandlerInterface( contextHandler );
            resourceAttributePip.setContextHandlerInterface( contextHandler );
            actionAttributePip.setContextHandlerInterface( contextHandler );
            environmentAttributePip.setContextHandlerInterface( contextHandler );
        } catch( Exception e ) {
            e.printStackTrace();
        }
    }

    private void resetRequest() {
        try {
            requestType = JAXBUtility.unmarshalToObject( RequestType.class, requestS );
        } catch( JAXBException e ) {
            e.printStackTrace();
        }
    }

    private void initAttributes() {
        subjectAttribute.createAttributeId( "urn:oasis:names:tc:xacml:1.0:subject:role" );
        subjectAttribute.setAttributeDataType( DataType.STRING );
        subjectAttribute.setCategory( Category.SUBJECT );

        resourceAttribute.createAttributeId( "urn:oasis:names:tc:xacml:1.0:resource:class" );
        resourceAttribute.setAttributeDataType( DataType.STRING );
        resourceAttribute.setCategory( Category.RESOURCE );

        actionAttribute.createAttributeId( "urn:oasis:names:tc:xacml:1.0:resource:type" );
        actionAttribute.setAttributeDataType( DataType.STRING );
        actionAttribute.setCategory( Category.ACTION );

        environmentAttribute.createAttributeId( "urn:oasis:names:tc:xacml:3.0:environment:temperature" );
        environmentAttribute.setAttributeDataType( DataType.STRING );
        environmentAttribute.setCategory( Category.ENVIRONMENT );
    }

    @Test
    public void test() {
        init();
        testInitialization();
        testRetrieve();
        testSubscribe();
        try {
            Thread.sleep( 11000 ); // NOSONAR
            changeAttributeValue();
            Thread.sleep( 10000 ); // NOSONAR
            resetAttributeValue();
            testUnsubscribe();
        } catch( InterruptedException e ) {
            e.printStackTrace();
        }
    }

    public void testInitialization() {
        try {
            fault = new PIPReader( PIPBuilder.getPropertiesFromString( missingCategory ).get() );
        } catch( Exception e ) {}
        assertFalse( fault != null && fault.isInitialised() );
        try {
            fault = new PIPReader( PIPBuilder.getPropertiesFromString( missingAttributeId ).get() );
        } catch( Exception e ) {}
        assertFalse( fault != null && fault.isInitialised() );
        try {
            fault = new PIPReader( PIPBuilder.getPropertiesFromString( missingExpectedCategory ).get() );
        } catch( Exception e ) {}
        assertFalse( fault != null && fault.isInitialised() );
        try {
            fault = new PIPReader( PIPBuilder.getPropertiesFromString( missingDataType ).get() );
        } catch( Exception e ) {}
        assertFalse( fault != null && fault.isInitialised() );
        try {
            fault = new PIPReader( PIPBuilder.getPropertiesFromString( missingFilePath ).get() );
        } catch( Exception e ) {}
        assertFalse( fault != null && fault.isInitialised() );

        // fault = new PIPReader( PIPBuilder.getPipPropertiesFromString(malformedInput).get() );
        // assertEquals( fault.initialized, false );
    }

    @Test( expected = PreconditionException.class )
    public void testNullProperties() {
        fault = new PIPReader( null );
        assertFalse( fault.isInitialised() );
    }

    public void testRetrieve() {
        try {
            log.info( "*****************BEGIN RETIREVE TEST*******************" );
            remoteRetrievalTest();
            localTest();
            log.info( "*****************END RETRIEVE TEST*******************" );
        } catch( Exception e ) {
            log.severe( e.getMessage() );
        }
    }

    public void remoteRetrievalTest() {
        String value = testRetrieveAttribute( subjectAttribute, subjectAttributePip );
        assertTrue( value.equals( "IIT" ) );
        value = testRetrieveAttribute( resourceAttribute, resourceAttributePip );
        assertTrue( value.equals( "SECRET" ) );
        log.info( JsonUtility.getJsonStringFromObject( environmentAttributePip, true ).get() );
        log.info( JsonUtility.getJsonStringFromObject( actionAttributePip, true ).get() );
        value = testRetrieveAttribute( actionAttribute, actionAttributePip );
        assertTrue( value.equals( "ANALYZE" ) );
        value = testRetrieveAttribute( environmentAttribute, environmentAttributePip );
        assertTrue( value.equals( "30.0" ) );
    }

    private String testRetrieveAttribute( Attribute attribute, PIPReader pipReader ) {
        try {
            String value = pipReader.retrieve( attribute );
            return value;
        } catch( Exception e ) {
            e.printStackTrace();
        }
        return null;
    }

    public void localTest() throws Exception {
        log.info( "-------BEGIN RETRIEVE TEST-------" );
        testRetrieveAndEnrichment( requestType, subjectAttributePip );
        assertTrue( verifyRequest( requestType, subjectAttribute ).equals( "[IIT]" ) );
        testRetrieveAndEnrichment( requestType, resourceAttributePip );
        assertTrue( verifyRequest( requestType, resourceAttribute ).equals( "[SECRET]" ) );
        testRetrieveAndEnrichment( requestType, actionAttributePip );
        assertTrue( verifyRequest( requestType, actionAttribute ).equals( "[ANALYZE]" ) );
        testRetrieveAndEnrichment( requestType, environmentAttributePip );
        assertTrue( verifyRequest( requestType, environmentAttribute ).equals( "[30.0]" ) );
        resetRequest();

        Attribute dummySubjectAttribute = new Attribute();
        dummySubjectAttribute.createAttributeId( "subjectId" );
        testRetrieveAndEnrichment( requestType, fault );
        testRetrieveAndEnrichment( null, fault );
        assertEquals( null, verifyRequest( requestType, dummySubjectAttribute ) );
        assertFalse( verifyRequest( requestType, environmentAttribute ).equals( "40.0" ) );
        log.info( "-------END RETRIEVE TEST-------" );
    }

    private void testRetrieveAndEnrichment( RequestType requestType, PIPReader pipReader ) {
        try {
            pipReader.retrieve( requestType );
        } catch( Exception e ) {
            // e.printStackTrace();
        }
    }

    public void testSubscribe() {
        try {
            log.info( "*****************BEGIN SUBSCRIBE TEST*******************" );
            remoteSubscribeTest();
            localSubscribeTest();
            log.info( "*****************END SUBSCRIBE TEST*******************" );
        } catch( Exception e ) {
            log.severe( e.getMessage() );
        }
    }

    public void localSubscribeTest() throws Exception {
        log.info( "-------BEGIN SUBSCRIBE TEST-------" );
        testSubscribeAndEnrichment( requestType, subjectAttributePip );
        assertTrue( verifyRequest( requestType, subjectAttribute ).equals( "[IIT]" ) );
        testSubscribeAndEnrichment( requestType, resourceAttributePip );
        assertTrue( verifyRequest( requestType, resourceAttribute ).equals( "[SECRET]" ) );
        testSubscribeAndEnrichment( requestType, actionAttributePip );
        assertTrue( verifyRequest( requestType, actionAttribute ).equals( "[ANALYZE]" ) );
        testSubscribeAndEnrichment( requestType, environmentAttributePip );
        assertTrue( verifyRequest( requestType, environmentAttribute ).equals( "[30.0]" ) );
        resetRequest();

        Attribute dummySubjectAttribute = new Attribute();
        dummySubjectAttribute.createAttributeId( "subjectId" );
        testSubscribeAndEnrichment( requestType, fault );
        testSubscribeAndEnrichment( null, fault );
        assertEquals( null, verifyRequest( requestType, dummySubjectAttribute ) );
        assertFalse( verifyRequest( requestType, environmentAttribute ).equals( "40.0" ) );
        log.info( "-------END SUBSCRIBE TEST-------" );
    }

    private void testSubscribeAndEnrichment( RequestType requestType, PIPReader pipReader ) {
        try {
            pipReader.subscribe( requestType );
        } catch( Exception e ) {
            log.severe( e.getMessage() );
            // e.printStackTrace();
        }
    }

    public void remoteSubscribeTest() {
        String value = testSubscribeAttribute( subjectAttribute, subjectAttributePip );
        assertTrue( value.equals( "IIT" ) );
        value = testSubscribeAttribute( resourceAttribute, resourceAttributePip );
        assertTrue( value.equals( "SECRET" ) );
        value = testSubscribeAttribute( actionAttribute, actionAttributePip );
        assertTrue( value.equals( "ANALYZE" ) );
        value = testSubscribeAttribute( environmentAttribute, environmentAttributePip );
        assertTrue( value.equals( "30.0" ) );
    }

    private String testSubscribeAttribute( Attribute attribute, PIPReader pipReader ) {
        try {
            String value = pipReader.subscribe( attribute );
            return value;
        } catch( Exception e ) {
            log.severe( e.getMessage() );
        }
        return null;
    }

    private void changeAttributeValue() {
        write( "60.0" );
    }

    private void resetAttributeValue() {
        write( "30.0" );
    }

    private void write( String string ) {
        try (FileWriter fileWriter = new FileWriter( new File( environmentFilePath ) )) {
            fileWriter.write( string );
            fileWriter.flush();
        } catch( Exception e ) {
            e.printStackTrace();
        }
    }

    public void testUnsubscribe() {
        try {
            log.info( "*****************BEGIN UNSUBSCRIBE TEST*******************" );
            unsubscribeTest();
            log.info( "*****************END UNSUBSCRIBE TEST*******************" );
        } catch( Exception e ) {
            log.severe( e.getMessage() );
        }
    }

    public void unsubscribeTest() {
        boolean value = testUnsubscribeAttribute( subjectAttribute, subjectAttributePip );
        assertTrue( value );
        value = testUnsubscribeAttribute( resourceAttribute, resourceAttributePip );
        assertTrue( value );
        value = testUnsubscribeAttribute( actionAttribute, actionAttributePip );
        assertTrue( value );
        value = testUnsubscribeAttribute( environmentAttribute, environmentAttributePip );
        assertTrue( value );
        value = testUnsubscribeAttribute( subjectAttribute, fault );
        assertFalse( value );
        value = testUnsubscribeAttribute( subjectAttribute, resourceAttributePip );
        value = testUnsubscribeAttribute( subjectAttribute, fault );
        assertFalse( value );
        value = testUnsubscribeAttribute( subjectAttribute, resourceAttributePip );
        assertFalse( value );
    }

    private boolean testUnsubscribeAttribute( Attribute attribute, PIPReader pipReader ) {
        try {
            ArrayList<Attribute> list = new ArrayList<>();
            list.add( attribute );
            boolean value = pipReader.unsubscribe( list );
            return value;
        } catch( Exception e ) {
            log.severe( e.getMessage() );
        }
        return false;
    }

    private String verifyRequest( RequestType requestType, Attribute attribute ) {
        for( AttributesType attributeTypes : requestType.getAttributes() ) {
            for( AttributeType attributeType : attributeTypes.getAttribute() ) {
                if( attributeType.getAttributeId().equals( attribute.getAttributeId().toString() ) ) {
                    for( AttributeValueType attributeValue : attributeType.getAttributeValue() ) {
                        log.info( attributeValue.getContent().toString() );
                        return attributeValue.getContent().toString();
                    }
                }
            }
        }
        return null;
    }
}
