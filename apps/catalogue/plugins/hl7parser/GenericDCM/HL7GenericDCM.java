/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package plugins.hl7parser.GenericDCM;

import java.util.ArrayList;
import org.w3c.dom.Node;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import plugins.hl7parser.StageLRA.HL7ObservationLRA;
import plugins.hl7parser.StageLRA.HL7OrganizerLRA;

/**
 *
 * @author roankanninga
 */
public class HL7GenericDCM {

    ArrayList<HL7OrganizerDCM> hl7organizer;

    private static final String ORGANIZER = "urn:hl7-org:v3:component/urn:hl7-org:v3:organizer/urn:hl7-org:v3:component";

    public HL7GenericDCM(Node parentNode, XPath xpath) throws Exception {
    	System.out.println("parentNode:" + parentNode.getNodeName());
        ArrayList<Node> allOrganizerNodes = new ArrayList<Node>();
        NodeList nodes = (NodeList)xpath.compile(ORGANIZER).evaluate(parentNode, XPathConstants.NODESET);
       
        for (int i = 0; i < nodes.getLength(); i++) {
            allOrganizerNodes.add(nodes.item(i));
        }

        hl7organizer = new ArrayList<HL7OrganizerDCM>(allOrganizerNodes.size());


        for(Node y : allOrganizerNodes){
            HL7OrganizerDCM hl7org = new HL7OrganizerDCM(y,xpath);
            hl7organizer.add(hl7org);
        }

    }

    public ArrayList<HL7OrganizerDCM> getHL7OrganizerDCM(){
          return hl7organizer;
    }

}