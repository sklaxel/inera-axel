package se.inera.axel.ssek.impl;

import javax.jws.WebService;

import org.ssek.schemas.helloworld._2011_11_17.HelloWorldRequest;
import org.ssek.schemas.helloworld._2011_11_17.HelloWorldResponse;
import org.ssek.schemas.helloworld._2011_11_17.wsdl.HelloWorldPortType;

@WebService(endpointInterface = "org.ssek.schemas.helloworld._2011_11_17.wsdl.HelloWorldPortType")
public class HelloWorldPortTypeImpl implements HelloWorldPortType {

	@Override
	public HelloWorldResponse helloWorld(HelloWorldRequest arg0) {
		HelloWorldResponse response = new HelloWorldResponse();
		response.setMessage("Hello World!");
		return response;
	}
}
