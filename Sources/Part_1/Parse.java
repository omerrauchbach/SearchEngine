package Part_1;

import javafx.scene.control.Alert;

import java.io.*;
import java.util.*;

public class Parse {

    public static Queue<Document> documentsSet = new LinkedList<>();
    private HashMap<String, Integer> stopWords;
    private HashMap<String , Integer> termDic = new HashMap<>();
    private String[] allTokens ;
    private int index ;
    private String[] sums = {"Dollars","million","billion","trillion","m","bn"};
    


    public void parseDocs() {

        while (!documentsSet.isEmpty()) {
            Document newDoc = documentsSet.poll();
            String text = newDoc.getText().toString();
            allTokens = text.split("(?!,[0-9])[, ?@!:;+)_(\"\\r\\n]+");

            for ( index = 0; index < allTokens.length; index++) {
                String rightToken;
                String currToken = allTokens[index];
                if(currToken == "")
                    continue;
                //===================== price =========================//
                if (isPrice(currToken)) {
                    handlePrice(currToken);
                    continue;

                }


                if (String.valueOf(currToken.charAt(0)).equals("$") && isNumeric(String.valueOf(currToken.charAt(currToken.length() - 1))) && allTokens[index + 1].toLowerCase().equals("million"))  // $450 million
                    rightToken = allTokens[index].substring(1) + " M Dollars"; ///removes the $ from the beginning
                else if ((String.valueOf(currToken.charAt(0)).equals("$") && isNumeric(String.valueOf(currToken.charAt(currToken.length() - 1))) && allTokens[index + 1].toLowerCase().equals("billion")))  // $450 billion
                    rightToken = allTokens[index].substring(1) + "000 M Dollars";
                else if (isNumeric(currToken) && allTokens[index + 1].equals("million") && allTokens[index + 2].toUpperCase().equals("U.S.") && allTokens[index + 3].toLowerCase().equals("dollars"))  // 320 million U.S. Dollars
                    rightToken = currToken + " M Dollars";
                else if (isNumeric(currToken) && allTokens[index + 1].equals("billion") && allTokens[index + 2].toUpperCase().equals("U.S.") && allTokens[index + 3].toLowerCase().equals("dollars"))  // 100 billion U.S. Dollars
                    rightToken = currToken + "000 M Dollars";

                else if (String.valueOf(currToken.charAt(0)).equals("$") && isNumeric(String.valueOf(currToken.charAt(1))) && !isNumeric(allTokens[index + 1]) && lessThanMillion(currToken.substring(1))) // $450 blabla // $450,000 blabla
                    rightToken = allTokens[index].substring(1) + "Dollars"; //removes the $ from the beginning


                else if (String.valueOf(currToken.charAt(0)).equals("$") && isNumeric(String.valueOf(currToken.charAt(1))) && !isNumeric(allTokens[index + 1]) && !lessThanMillion(currToken.substring(1)))  // $150,000,000 ==> 150 M Dollars
                    rightToken = allTokens[index].substring(1, currToken.indexOf(",")) + " M " + "Dollars"; //until first "," !
                else if (isNumeric(currToken) && !lessThanMillion(currToken) && allTokens[index + 1].toLowerCase().equals("dollars")) // 1,000,000 Dollars
                    rightToken = allTokens[index].substring(0, currToken.indexOf(",")) + " M " + "Dollars";
                else if (currToken.contains("-")) { // word-word // word-word-word // num-word // word-num //
                    // BETWEEN NUMBERSSSS ????????????????????????????????????????????????????????????????????????/
                    //????????????????
                    rightToken = currToken;
                }
                // DDAATTEEESSSSSS
                else if (isNumeric(allTokens[index]) && isDate(allTokens[index + 1]) && allTokens[index].length() == 2) //14 May
                    rightToken = turnMonthToNumber(allTokens[index + 1]) + "-" + allTokens[index]; //05-14
                else if (isDate(allTokens[index]) && isNumeric(allTokens[index + 1]) && allTokens[index + 1].length() == 2) //JUN 4
                    rightToken = turnMonthToNumber(allTokens[index]) + "-" + allTokens[index + 1]; //06-04
                else if (isDate(allTokens[index]) && isNumeric(allTokens[index + 1]) && allTokens[index + 1].length() == 4) //JUN 1994
                    rightToken = allTokens[index + 1] + "-" + turnMonthToNumber(allTokens[index]); //1994-06
                else if (isNumeric(currToken)) {
                    if (isThousand(currToken) && !isNumeric(allTokens[index + 1])) //only 100,123 (K)
                        rightToken = allTokens[index].substring(0, currToken.indexOf(",")) + "." + allTokens[index].substring(currToken.indexOf(",") + 1) + "K";
                    else if (isMillion(currToken) && !isNumeric(allTokens[index + 1])) // only 100,123,333 (M)
                        rightToken = allTokens[index].substring(0, currToken.indexOf(",")) + "." + allTokens[index].substring(currToken.indexOf(",") + 1, currToken.indexOf(",") + 4) + "M";
                    else if (isBillion(currToken) && !isNumeric(allTokens[index + 1])) // only 100,123,333,000 (B)
                        rightToken = allTokens[index].substring(0, currToken.indexOf(",")) + "." + allTokens[index].substring(currToken.indexOf(",") + 1, currToken.indexOf(",") + 4) + "B";

                    else if (allTokens[index + 1].toLowerCase().equals("thousand"))
                        rightToken = currToken + "K";
                    else if (allTokens[index + 1].toLowerCase().equals("million"))
                        rightToken = currToken + "M";
                    else if (allTokens[index + 1].toLowerCase().equals("billion"))
                        rightToken = currToken + "B";

                    else if (allTokens[index + 1].equals("percent") || allTokens[index + 1].equals("percentage"))
                        rightToken = allTokens[index] + "%";

                    else if (isFraction(allTokens[index + 1]) && allTokens[index + 2].toLowerCase().equals("dollars")) // 22 3/4 Dollars
                        rightToken = allTokens[index] + allTokens[index + 1] + "Dollars";
                    else if (isFraction(allTokens[index + 1])) // FRACTIONNNNN ???
                        rightToken = allTokens[index] + allTokens[index + 1]; //as is. 25 3/4
                    else if (allTokens[index + 1].toLowerCase().equals("dollars"))
                        rightToken = allTokens[index] + "Dollars";
                }
                // FRACTION ESRONIIII
                else if (allTokens[index].contains(".") && allTokens[index].substring(0, allTokens[index].indexOf(".")).length() > 3 && allTokens[index].substring(0, allTokens[index].indexOf(".")).length() < 7) { // 1023.48 // less/equal than 3 stays as is. 102.2 ,,, 10.873. GREATER THAN 6 IS ??????
                    //String tempToken = allTokens[index].substring(0, allTokens[index+1].indexOf(".")); //only 1023
                    //if (tempToken.length() > 6) //1023.4999 ==> 1.023
                    int tempNum = (Integer.parseInt(allTokens[index]) / 100); // SHOULD BE 1.02348
                    String temp = Integer.toString(tempNum);
                    rightToken = temp.substring(temp.indexOf(".") + 1, temp.indexOf(".") + 4) + "K"; // 1.023K // only 3 DIGITS !
                } else if (isNumeric(String.valueOf(currToken.charAt(0))) && String.valueOf(currToken.charAt(currToken.length() - 1)).equals("%")) //632%
                    rightToken = currToken; //already includes the "%" sign

                else if (isNumeric(String.valueOf(currToken.charAt(0))) && String.valueOf(currToken.charAt(currToken.length() - 1)).equals("m") && allTokens[index + 2].toLowerCase().equals("dollars")) // 20.6m Dollars
                    rightToken = currToken.substring(0, currToken.length() - 1) + " M Dollars"; //without the m itself in the token.
                else if (isNumeric(String.valueOf(currToken.charAt(0))) && String.valueOf(currToken.charAt(currToken.length() - 2)).equals("b") && String.valueOf(currToken.charAt(currToken.length() - 1)).equals("n") && allTokens[index + 1].toLowerCase().equals("dollars")) // 100bn Dollars
                    rightToken = currToken.substring(0, currToken.length() - 2) + "000 M Dollars";

                else {
                    rightToken = allTokens[index];
                }
            }
        }
    }
    /*
        Price Dollars
    ii. $price
    iii. $price million
    iv. $price billion
    v. Price m Dollars
    vi. Price bn Dollars
    vii. Price billion U.S. dollars
    viii. Price million U.S. dollars
    ix. Price trillion U.S. dollars

     */

    private String numericToPrice(String num ,String sum,String franction ,boolean sign, boolean Dollars , boolean US ){
        String price = num.replace("," , "");
        if(sign)
            price = price.substring(1);
        if(lessThanMillion(price)){
            if(sign) {
                price = (price.substring(1)) + " " + franction + "Dollars";

            }else if(Dollars){
                price = price+" "+franction+" Dollars";
            }else{
                price = price;
            }

        }else{
            ///greater then M
            if(sign)
              price = (price.substring(1));

        }

        return "";

    }

    private boolean equalToSum(String word){
        for(String sum : sums ){
            if(word.equals(word))
                return true;
        }
        return false;
    }

    private boolean isPrice(String price){

        if(price.charAt(0) == '$' && isNumeric(price.substring(1)))
            return true;
        if(index < allTokens.length && isNumeric(price)){
            String word = allTokens[index+1];
            if(equalToSum(word))
                return true;
        }

        return false;
    }


    private boolean isNumeric (String docToken){ //checks if the token is a number
        try {

            Double.parseDouble(docToken.replace(",",""));
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }

    private boolean isFraction (String docToken){ //checks if the token is a number with a fraction
       if (docToken.contains("/"))
           return true;
       return false;
    }

    private boolean lessThanMillion (String numToken){
        if (Integer.parseInt(numToken) < 1000000)
            return true;
        return false;
    }

    private boolean isThousand (String numToken){
        if (Integer.parseInt(numToken) >= 1000 && (Integer.parseInt(numToken) < 1000000))
            return true;
        return false;
    }

    private boolean isMillion (String numToken){
        if (Integer.parseInt(numToken) >= 1000000 && (Integer.parseInt(numToken) < 1000000000))
            return true;
        return false;
    }

    private boolean isBillion (String numToken){
        if (Integer.parseInt(numToken) >= 1000000000 )
            return true;
        return false;
    }

    private boolean isDate (String docToken){ //checks if the token is a month (date)
        String[] months = {"jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec"};
        boolean found = false;
        for (String month : months) {
            if (docToken.toLowerCase().contains(month)) {
                found = true;
                break;
            }
        }
        return found;
    }

    public static String turnMonthToNumber (String docMonth){ //turns the month name to number

        if (docMonth.toLowerCase().equals("jan") || docMonth.toLowerCase().equals("january"))
            return "01";
        else if (docMonth.toLowerCase().equals("feb") || docMonth.toLowerCase().equals("february"))
            return "02";
        else if (docMonth.toLowerCase().equals("mar") || docMonth.toLowerCase().equals("march"))
            return "03";
        else if (docMonth.toLowerCase().equals("apr") || docMonth.toLowerCase().equals("april"))
            return "04";
        else if (docMonth.toLowerCase().equals("may"))
            return "05";
        else if (docMonth.toLowerCase().equals("jun") || docMonth.toLowerCase().equals("june"))
            return "06";
        else if (docMonth.toLowerCase().equals("jul") || docMonth.toLowerCase().equals("july"))
            return "07";
        else if (docMonth.toLowerCase().equals("aug") || docMonth.toLowerCase().equals("august"))
            return "08";
        else if (docMonth.toLowerCase().equals("sep") || docMonth.toLowerCase().equals("september"))
            return "09";
        else if (docMonth.toLowerCase().equals("oct") || docMonth.toLowerCase().equals("october"))
            return "10";
        else if (docMonth.toLowerCase().equals("nov") || docMonth.toLowerCase().equals("november"))
            return "11";
        else if (docMonth.toLowerCase().equals("dec") || docMonth.toLowerCase().equals("december"))
            return "12";
        return "-1";
    }

    private void setStopWords(String path){

        File stopWordsFile = new File(path);
                if (stopWordsFile != null) {
                    try {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(stopWordsFile)));
                        StringBuilder stopWordLines = new StringBuilder();
                        for ( String currLine ; (currLine = bufferedReader.readLine()) != null; )
                            stopWords.put( currLine, 1 );
                        bufferedReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }


        else{
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Error in folder path");
            alert.show();
        }
    }

    private void handleWords(String word){

        if(word != null && word.length() > 1) {
            /// check if word is in lower letters
            if (word.equals(word.toLowerCase())) {
                /// word is save in the Dic
                if(termDic.containsKey(word)){

                }
                else if(termDic.containsKey(word.toUpperCase())){
                    //check if the word is save in upper case


                }
                else{
                    // first occur
                }

            }
            /// check if the first char in the word is upper letter
            else if (word.charAt(0) == word.toUpperCase().charAt(0)){
                if(termDic.containsKey(word)){

                }
                else if(termDic.containsKey(word.toUpperCase())){
                    //check if the ward is save in upper case


                }
                else{
                    // first occur
                }
            }
            ///check if the word is all in upper letter.
            else if (word.equals(word.toUpperCase())){
                //first occur
                if(!termDic.containsKey(word)){

                }
                else if(termDic.containsKey(word.toLowerCase())){

                }
                else{
                    //first occur
                }
            }
        }
    }

    private void handlePrice(String currToken ){
        String price = "";
        boolean sign =(currToken.charAt(0) == '$');
        String[] priceTerms = new String[4];
        int priceTermIndex = 0;
        for(int i = index ; i <allTokens.length &&i<index+4 ; i++ ){
            priceTerms[priceTermIndex] = allTokens[i];
            priceTermIndex++;
        }
        for(String priceTerm : priceTerms){
            if(priceTerm == null)
                priceTerm = "";
        }

        if(equalToSum(priceTerms[1])){
            if(priceTerms[1].equals("Dollars")){
                price = numericToPrice(priceTerms[0],priceTerms[1],"", sign, true , false);
                index= index+2;
            }else if(priceTerms[1].equals("million") || priceTerms[1].equals("billion")){
                if(priceTerms[2].equals("U.S.") && priceTerms[3].equals("dollars")) {
                    price = numericToPrice(priceTerms[0], priceTerms[1],"",  sign, true, true);
                    index= index+4;
                }else {
                    price = numericToPrice(priceTerms[0], priceTerms[1],"",  sign, false, false);
                    index= index +2;
                }
            } else if (priceTerms[1].equals("m") || priceTerms[1].equals("bn")) {
                if(priceTerms[2].equals("Dollars")) {
                    price = numericToPrice(priceTerms[0], priceTerms[1],"",  sign, true, false);
                    index= index +3;
                }else {
                    price = numericToPrice(priceTerms[0], priceTerms[1],"",  sign, false, false);
                    index++;
                }
            }else if(priceTerms[2].equals("U.S.") && priceTerms[3].equals("dollars")) {
                //trillion
                price = numericToPrice(priceTerms[0], priceTerms[1],"",  sign, true, true);
                index = index + 4;
            }else if(isFraction(priceTerms[1]) && priceTerms[2].equals("Dollars")){
                price = numericToPrice(priceTerms[0],"",priceTerms[1],  sign, true , false);
                index= index + 3;
            }else{
                price = numericToPrice(priceTerms[0],"","",  sign, false , false);
                index++;
            }
        }else{
            price = numericToPrice(priceTerms[0],"","",  sign, false , false);
            index++;
        }
    }
}



