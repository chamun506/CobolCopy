package com.test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBElement;
import org.w3c.dom.Document;

import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.MbDFDL;
import com.ibm.broker.plugin.MbElement;
import com.ibm.broker.plugin.MbException;
import com.ibm.broker.plugin.MbMessage;
import com.ibm.broker.plugin.MbMessageAssembly;
import com.ibm.broker.plugin.MbOutputTerminal;
import com.ibm.broker.plugin.MbUserException;
import com.ibm.broker.plugin.MbXMLNSC;

public class Main_JavaCompute extends MbJavaComputeNode {

	protected static JAXBContext jaxbContext = null;

	public void onInitialize() throws MbException {
		try {
			// Initialize JAXB context with com.test
			// Java object classes that were generated by a Java Architecture for XML
			// Binding (JAXB) binding compiler  
			jaxbContext = JAXBContext.newInstance("com.test");
		} catch (JAXBException e) {
			// This exception will cause the deploy of this Java compute node to fail
			//  Typical cause is the JAXB package above is not available
			throw new MbUserException(this, "onInitialize()", "", "",
					e.toString(), null);
		}
	}

	public void evaluate(MbMessageAssembly inAssembly) throws MbException {
		MbOutputTerminal out = getOutputTerminal("out");
		MbOutputTerminal alt = getOutputTerminal("alternate");

		// obtain the input message data
		MbMessage inMessage = inAssembly.getMessage();

		// create a new empty output message
		MbMessage outMessage = new MbMessage();
		MbMessageAssembly outAssembly = new MbMessageAssembly(inAssembly,
				outMessage);

		// optionally copy input message headers to the new output
		copyMessageHeaders(inMessage, outMessage);

		try {
			// unmarshal the input message data from the Broker tree into your Java object classes   
			Object inMsgJavaObj = jaxbContext.createUnmarshaller().unmarshal(
					inMessage.getDOMDocument());
			PurchaseData p = ((JAXBElement<PurchaseData>)inMsgJavaObj).getValue();
			p.setCustomerSurname("Ramadhenu");
			// ----------------------------------------------------------
			// Add user code below to build the new output data by updating 
			// your Java objects or building new Java objects

			// TODO - Replace or modify following which simply copies input to output message
			Object outMsgJavaObj = inMsgJavaObj;

			// End of user Java object processing
			// ----------------------------------------------------------

			// TODO set the required Broker domain to for the output message, eg XMLNSC
			Document outDocument = outMessage.createDOMDocument(MbDFDL.PARSER_NAME);
			// marshal the new or updated output Java object class into the Broker tree
			jaxbContext.createMarshaller().marshal(outMsgJavaObj, outDocument);

			// The following should only be changed if not propagating message to 
			// the node's 'out' terminal
			out.propagate(outAssembly);
		} catch (JAXBException e) {
			// Example Exception handling	
			throw new MbUserException(this, "evaluate()", "", "", e.toString(),
					null);
		}
	}

	public void copyMessageHeaders(MbMessage inMessage, MbMessage outMessage)
			throws MbException {
		MbElement outRoot = outMessage.getRootElement();

		// iterate though the headers starting with the first child of the root
		// element and stopping before the last child (message body)
		MbElement header = inMessage.getRootElement().getFirstChild();
		while (header != null && header.getNextSibling() != null) {
			// copy the header and add it to the out message
			outRoot.addAsLastChild(header.copy());
			// move along to next header
			header = header.getNextSibling();
		}
	}

}
