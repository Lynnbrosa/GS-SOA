package br.com.orbittapi.soap.config;

import br.com.orbittapi.soap.exception.InvalidCoordinateSoapException;
import br.com.orbittapi.soap.exception.SatelliteUnavailableSoapException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ws.soap.server.endpoint.SoapFaultDefinition;
import org.springframework.ws.soap.server.endpoint.SoapFaultMappingExceptionResolver;

import java.util.Properties;

@Configuration
public class SoapFaultConfig {

    @Bean
    public SoapFaultMappingExceptionResolver soapFaultMappingExceptionResolver() {
        SoapFaultMappingExceptionResolver resolver = new SoapFaultMappingExceptionResolver();

        SoapFaultDefinition serverDefault = new SoapFaultDefinition();
        serverDefault.setFaultCode(SoapFaultDefinition.SERVER);
        resolver.setDefaultFault(serverDefault);

        Properties mappings = new Properties();
        mappings.setProperty(InvalidCoordinateSoapException.class.getName(),
                "CLIENT,Invalid request");
        mappings.setProperty(SatelliteUnavailableSoapException.class.getName(),
                "SERVER,Satellite service unavailable");
        resolver.setExceptionMappings(mappings);

        resolver.setOrder(1);
        return resolver;
    }
}
