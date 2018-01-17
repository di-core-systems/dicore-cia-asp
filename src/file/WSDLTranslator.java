package file;

import wsdlhelper.MessageTuple;
import wsdlhelper.OperationTuple;
import wsdlhelper.TypeTriple;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by CSZ on 14.12.2017.
 * This class translates every information stored in our WSDLDocument directly into ASP.
 */
public class WSDLTranslator {

    public File translate(WSDLDocument document){
        return translate(document, "../ASP/logic_programs/Interface/newFile.lp");
    }

    public File translate(WSDLDocument document, String fileName){
        File f = new File(fileName);

        try {
            FileWriter w = new FileWriter(f);

            String service = document.getService();

            w.write("service(" + service + ").\n");

            String binding = document.getBinding();

            w.write("binding(" + binding + ").\n");

            for(OperationTuple o: document.getOperations()){
                w.write("operation(" + o.getName() + ", " + o.getInput() + ", " + o.getOutput() + ").\n");
            }

            for (TypeTriple t: document.getTypes()){
                switch(t.getAttribute()){
                    case NORMAL:
                        w.write("type(" + t.getName() + ", " + t.getType() + ").\n");
                        break;
                    case INPUT:
                        w.write("input(" + t.getParent() + ", " + t.getName() + ", " + t.getType() + ").\n");
                        break;
                    case OUTPUT:
                        w.write("output(" + t.getParent() + ", " + t.getName() + ", " + t.getType() + ").\n");
                        break;
                    case FAULT:
                        w.write("fault(" + t.getParent() + ", " + t.getName() + ", " + t.getType() + ").\n");
                        break;
                    default:
                        System.out.println("Your type is not defined.");
                }
            }

            for (MessageTuple m: document.getMessages()){
                w.write("messagePart(" + m.getMessage() + ", " + m.getPart() + ").\n");
            }

            w.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return f;
    }
}
