package Main;


import Part_1.Parse;
import Part_1.ReadFile;
import Part_1.Document;
import java.util.HashMap;
import java.util.Map;


public class Main {


    public static void main(String[] args) {
        ReadFile rf = new ReadFile("C:\\Users\\omer\\Desktop");
        HashMap<String, Integer> termCounter = new HashMap<>();
        rf.readInsideAllFiles();
        for (Document d : Parse.documentsSet) {
            String text = d.getText().toString();
            String[] allTokens = text.split( "(?!,[0-9])[, ?@!:;+)_(\"\\r\\n]+");
            for(String term : allTokens )
                if(termCounter.containsKey(term)){
                    termCounter.replace(term, termCounter.get(term)+1);
                }
                else{
                    termCounter.put(term ,1);
                }
        }

        for(Map.Entry<String, Integer> entry : termCounter.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            System.out.println(key+","+value);

        }


    }
}


/**
 * String regaxExp_1 = "(?!,[0-9])[, ?@!:;+)_(\"\\r\\n]+";
 *         String regaxExp_2 = "(?!/[0-9])(?!,[0-9])[?!:,#`@^~&+{*}|\"<=>\\s;()_\\\\\\[\\]]+";
 *         String path = "C:\\Users\\omer\\Desktop\\corpus";
 *         File textFile = new File(path);
 *         if (textFile != null){
 *             try {
 *                 BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(textFile)));
 *                 StringBuilder allLinesInDoc = new StringBuilder();
 *                 for (String currLine; (currLine = bufferedReader.readLine()) != null; )
 *                     allLinesInDoc.append(currLine + System.lineSeparator());
 *                 bufferedReader.close();
 *                 String[] tokensPrint_1 = allLinesInDoc.toString().split(regaxExp_1);
 *                 String[] tokensPrint_2 = allLinesInDoc.toString().split(regaxExp_2);
 *                 boolean print = true;
 *                 int index_1 = 0;
 *                 int index_2 = 0;
 *
 *                 while (print) {
 *                     if (index_1 < tokensPrint_1.length && index_2 < tokensPrint_2.length) {
 *                         System.out.println(tokensPrint_1[index_1] + ";;" + tokensPrint_2[index_2]);
 *                         index_1++;
 *                         index_2++;
 *                     } else if (index_1 < tokensPrint_1.length) {
 *                         System.out.println(tokensPrint_1[index_1]);
 *                         index_1++;
 *                     } else if (index_2 < tokensPrint_2.length) {
 *
 *                         System.out.println(tokensPrint_2[index_2]);
 *                         index_2++;
 *                     } else {
 *                         print = false;
 *                     }
 *                 }
 *             } catch (IOException e) {
 *                 e.printStackTrace();
 *             }
 *             }else{
 *                 Alert alert = new Alert(Alert.AlertType.ERROR);
 *                 alert.setContentText("Error in folder path");
 *                 alert.show();
 *             }
 */

/**
 *  if (isPrice(currToken)) {
 *                     String price = "";
 *                     boolean sign =(currToken.charAt(0) == '$');
 *                     if(index < allTokens.length){
 *                         String sum = equalToSum(allTokens[index+1]);
 *                         if(!sum.equals("")){
 *                             if(sum.equals("Dollars")) {
 *                                 //Price Dollars
 *                                 price = numericToPrice(currToken, "", sign, true, false);
 *                             }else if(sum.equals("million") || sum.equals("million")){
 *
 *                             }else if(index+1<allTokens.length ){
 *                                 if((sum.equals("m") || sum.equals("bn")) && allTokens[index+2].equals("Dollars")){
 *                                     // Price m Dollars Or Price bn Dollars
 *                                     price = numericToPrice(currToken ,sum ,sign, true , false);
 *                                 }
 *                                 else if(allTokens[index+2].equals("U.S."))
 *                                     if(index+2 < allTokens.length &&allTokens[index+2].equals("Dollars") )
 *                                         price = numericToPrice(currToken ,sum,sign, true , true);
 *
 *                             }else{
 *                                 price = numericToPrice(currToken ,sum,sign,false,false); //$price
 *                             }
 *
 *                         }else{
 *                             price = numericToPrice(currToken ,"",sign, false , false); //$price
 *                         }
 *
 *                     }else{
 *                         price = numericToPrice(currToken ,"",sign, false , false); //$price
 *                     }
 *
 *                 }
 *
 */
