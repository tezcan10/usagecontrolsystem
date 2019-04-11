/*******************************************************************************
 * Copyright 2018 IIT-CNR
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package it.cnr.iit.ucsinterface.pdp;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import it.cnr.iit.ucs.builders.PIPBuilder;
import it.cnr.iit.utility.JAXBUtility;

import oasis.names.tc.xacml.core.schema.wd_17.ObligationType;
import oasis.names.tc.xacml.core.schema.wd_17.ResponseType;

/**
 * This is the result sent by the PDP
 * <p>
 * The result of an evaluation is composed by a ResponseType that is one of those classes generated by the XJC tool
 * starting from the xsd of the XACML definition. Furthermore we have an attribute that signals the id of the session to
 * which the evaluation is referred to and a boolean that states if the object is in a correct state
 * </p>
 *
 * @author antonio
 *
 */

@JsonIgnoreProperties( ignoreUnknown = true )
public final class PDPResponse implements PDPEvaluation {

    private static Logger log = Logger.getLogger( PIPBuilder.class.getName() );

    private static final String MSG_ERR_UNMARSHAL = "Error unmarshalling xml : {0}";
    private static final String MSG_ERR_MARSHAL = "Error marshalling to xml : {0}";

    // the response provided by the PDP object
    private ResponseType responseType = null;

    // the id of the session the which the evaluation was referred
    private String sessionId = "";

    // list of firing rules
    private ArrayList<Integer> firingRules = new ArrayList<>();

    // states if the object has been correctly initialized
    @JsonIgnore
    private boolean initialized = false;

    /**
     * Constructor for the PDP response
     *
     */
    public PDPResponse() {

    }

    /**
     * Constructor for the PDP response
     *
     * @param string the ResponseType in string format
     */
    public PDPResponse( String string ) {
        convertXACMLStringToResponse( string );
        check();
    }

    /**
     * Constructor for the PDP response
     *
     * @param ResponseType
     */
    public PDPResponse( ResponseType response ) {
        setResponseType( response );
        check();
    }

    public void check() {
        if( responseType != null ) {
            initialized = true;
        }
    }

    /**
     * Sets the response provided by the PDP. This function also checks if the response provided by the PDP is of a
     * valid Response type.
     *
     * @param string the response in string format
     * @return true if everything goes ok, false otherwise
     */
    private void convertXACMLStringToResponse( String string ) {
        try {
            responseType = JAXBUtility.unmarshalToObject( ResponseType.class, string );
        } catch( Exception e ) {
            log.severe( String.format( MSG_ERR_UNMARSHAL, e.getMessage() ) );
        }
    }

    private void setResponseType( ResponseType responseType ) {
        this.responseType = responseType;
        check();
    }

    public ResponseType getResponseType() {
        return responseType;
    }

    @Override
    @JsonIgnore
    public String getResponse() {
        try {
            return JAXBUtility.marshalToString( ResponseType.class, responseType, "Response", JAXBUtility.SCHEMA );
        } catch( JAXBException e ) {
            log.severe( String.format( MSG_ERR_MARSHAL, e.getMessage() ) );
        }
        return "";
    }

    @Override
    @JsonIgnore
    public String getResult() {
        // BEGIN parameter checking
        if( !initialized ) {
            return null;
        }
        // END parameter checking
        return responseType.getResult().get( 0 ).getDecision().value();
    }

    @Override
    @JsonIgnore
    public List<PDPObligationInterface> getPIPObligations() {
        return new ArrayList<>();
    }

    @Override
    @JsonIgnore
    public List<PDPObligationInterface> getPEPObligations() {
        return new ArrayList<>();
    }

    @Override
    @JsonIgnore
    public ArrayList<String> getObligations() {
        ArrayList<String> obligations = new ArrayList<>();
        if( responseType.getResult().get( 0 ).getObligations() == null ) {
            return new ArrayList<>();
        }
        for( ObligationType obligation : responseType.getResult().get( 0 )
            .getObligations().getObligation() ) {
            obligations.add( obligation.getObligationId() );
        }
        return obligations;
    }

    // TODO delete this
    @Override
    @JsonIgnore
    public Object getResponseAsObject() {
        return responseType;
    }

    public void setFiringRules( List<Integer> firingRules ) {
        this.firingRules = new ArrayList<>( firingRules );
    }

    @Override
    public ArrayList<Integer> getFiringRules() {
        return firingRules;
    }

    @Override
    public void setSessionId( String id ) {
        // BEGIN parameter checking
        if( id == null || id.isEmpty() ) {
            return;
        }
        // END parameter checking
        sessionId = id;
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @JsonIgnore
    public boolean isValid() {
        return initialized;
    }
}
