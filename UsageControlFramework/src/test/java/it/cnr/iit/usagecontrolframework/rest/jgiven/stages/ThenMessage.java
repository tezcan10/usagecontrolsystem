package it.cnr.iit.usagecontrolframework.rest.jgiven.stages;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.Quoted;
import com.tngtech.jgiven.integration.spring.JGivenStage;

@JGivenStage
public class ThenMessage extends Stage<ThenMessage> {

    @ExpectedScenarioState
    WireMock wireMockContextHandler;

    public ThenMessage the_asynch_post_request_for_$_was_received_by_PEPRest( @Quoted String operation ) {
//        wireMockContextHandler.verifyThat( postRequestedFor( urlEqualTo( operation ) )
//            .withHeader( "Content-Type", equalTo( "application/json" ) ) );
        return self();
    }
}
