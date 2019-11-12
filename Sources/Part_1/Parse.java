package Part_1;

import java.util.*;

public class Parse {

    static Queue<Document> documentsSet = new LinkedList<>();
    private HashMap<String, Integer> stopWords;


    private void parseDocs(){

        while(!documentsSet.isEmpty()){
            Document newDoc = documentsSet.poll();
            String text = newDoc.getText().toString();
            String[] allTokens = text.split( "[, ?@!:;/+)_(\"]+");

            for (int i =0 ; i< allTokens.length ; i++){
                String currToken = allTokens[i];
                if (isNumeric(currToken)) {



                }

            }
        }
    }


}
