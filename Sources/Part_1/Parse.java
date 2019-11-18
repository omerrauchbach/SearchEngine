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
    private String[] months = {"jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec"};
    


    public void parseDocs() {

        while (!documentsSet.isEmpty()) {
            Document newDoc = documentsSet.poll();
            String text = newDoc.getText().toString();
            allTokens = text.split(" (?!,[0-9])[, ?@!:;+)_(\"\\r\\n]+");

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
                else if(isDate(currToken)){
                    if(isNumericDate(currToken))
                        handleDate(currToken,allTokens[index+1]);
                    else
                        handleDate(allTokens[index+1] , currToken);
                    /////////////////jkjkjlkjljlk
                }
                else if(isPercent(currToken)){
                    handlePercent(currToken);
                }




                else if (isNumericDouble(currToken)) {
                    if (isThousand(currToken) && !isNumericDouble(allTokens[index + 1])) //only 100,123 (K)
                        rightToken = allTokens[index].substring(0, currToken.indexOf(",")) + "." + allTokens[index].substring(currToken.indexOf(",") + 1) + "K";
                    else if (isMillion(currToken) && !isNumericDouble(allTokens[index + 1])) // only 100,123,333 (M)
                        rightToken = allTokens[index].substring(0, currToken.indexOf(",")) + "." + allTokens[index].substring(currToken.indexOf(",") + 1, currToken.indexOf(",") + 4) + "M";
                    else if (isBillion(currToken) && !isNumericDouble(allTokens[index + 1])) // only 100,123,333,000 (B)
                        rightToken = allTokens[index].substring(0, currToken.indexOf(",")) + "." + allTokens[index].substring(currToken.indexOf(",") + 1, currToken.indexOf(",") + 4) + "B";

                    else if (allTokens[index + 1].toLowerCase().equals("thousand"))
                        rightToken = currToken + "K";
                    else if (allTokens[index + 1].toLowerCase().equals("million"))
                        rightToken = currToken + "M";
                    else if (allTokens[index + 1].toLowerCase().equals("billion"))
                        rightToken = currToken + "B";

                    else if (allTokens[index + 1].equals("percent") || allTokens[index + 1].equals("percentage"))
                        rightToken = allTokens[index] + "%";

                }
                // FRACTION ESRONIIII
                else if (allTokens[index].contains(".") && allTokens[index].substring(0, allTokens[index].indexOf(".")).length() > 3 && allTokens[index].substring(0, allTokens[index].indexOf(".")).length() < 7) { // 1023.48 // less/equal than 3 stays as is. 102.2 ,,, 10.873. GREATER THAN 6 IS ??????
                    //String tempToken = allTokens[index].substring(0, allTokens[index+1].indexOf(".")); //only 1023
                    //if (tempToken.length() > 6) //1023.4999 ==> 1.023
                    int tempNum = (Integer.parseInt(allTokens[index]) / 100); // SHOULD BE 1.02348
                    String temp = Integer.toString(tempNum);
                    rightToken = temp.substring(temp.indexOf(".") + 1, temp.indexOf(".") + 4) + "K"; // 1.023K // only 3 DIGITS !
                }

                else {
                    rightToken = allTokens[index];
                }
            }
        }
    }

    private void insertTermDic(String term){

    }

    public String numericToPrice(String num ,String sum,String fraction ,boolean sign, boolean Dollars , boolean US ){
        String price = num.replace("," , "");
        if(sign)
            price = price.substring(1);
        if(lessThanMillion(price)){
            price = num+" "+fraction+" Dollars";
        }else{  ///greater then M
            if ((sum.equals("million") && US && Dollars) || (sum.equals("m") && Dollars) || (sign && sum.equals("million")))
                price = price + " M Dollars";
            else if ((sum.equals("billion") && US && Dollars)  || (sum.equals("bn") && Dollars) || (sign && sum.equals("billion")))
                price = price + "000 M Dollars"; //adds 3 zeroes. B ==> M.
            else if (sum.equals("trillion") && US && Dollars)
                price = price + "000000 M Dollars"; //adds 6 ??? zeroes. T ==> M.
            else if ((Dollars && !sign && !US) || (sign && !US && !Dollars))  // 1,000,000 Dollars // $450,000,000
                price = Integer.parseInt(price)/1000000 + " M Dollars"; //1 M Dollars
        }
        System.out.println(price);
        return "" ;
    }


    private String hundleGreaterThenM(String num, String sum,boolean sign){

        if(sign)
            num =num.substring(1);

        return " ";

    }

    private boolean equalToSum(String word){
        for(String sum : sums ){
            if(word.equals(word))
                return true;
        }
        return false;
    }

    public boolean isPrice(String price){

        if(price.charAt(0) == '$' && isNumericDouble(price.substring(1)))
            return true;
        if(index < allTokens.length && isNumericDouble(price)){
            String word = allTokens[index+1];
                if(equalToSum(word))
                    return true;
        }

        return false;
    }

    private boolean isNumericDouble (String docToken){ //checks if the token is a number
        try {

            Double.parseDouble(docToken.replace(",",""));
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }

    private boolean isNumericInt(String docToken){
        try {

            Integer.parseInt(docToken.replace(",",""));
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

    private boolean isMonths(String token){
        for (String month : months)
            if (token.toLowerCase().contains(month))
                return true;
        return false;

    }

    private boolean isNumericDate(String numToken){

        try {
            int num = Integer.parseInt(numToken) ;
            if(numToken.length() == 2)
                if(num < 13 && num > 0)
                    return true;
            else if(numToken.length() == 4)
                return true;
        } catch(NumberFormatException e){
            return false;
        }
        return false;
    }

    private boolean isDate (String docToken){ //checks if the token is a month (date)

        if(index < allTokens.length){
            if((isNumericDate(docToken) && isMonths(allTokens[index+1])) || (isNumericDate(allTokens[index+1]) && isMonths(docToken)))
                return true;
            else
                return false;
        }
        else
            return false;
    }

    private boolean isPercent(String term){

        if(index < allTokens.length)
            if(isNumericDouble(term) && allTokens[index+1] == "%")
                return true;
            else if(isNumericDouble(term) && (allTokens[index+1].equals("percent") || allTokens[index+1].equals("percentage")) )
                return true;

        return false;
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

    private boolean stopWord(String word){
        if(stopWords != null && stopWords.size()>0){
            for(String stopWord : stopWords.keySet()){
                if(stopWord.equals(word) || stopWord.equals(word.toLowerCase()) || stopWord.equals(word.toUpperCase()))
                    return true;
            }
        }
        return false;
    }

    private String startEndWord(String word){

        if(word.equals("U.S."))
            return word;
        ////first char
        if(word.charAt(0) == '.')
            word = word.substring(1);
        if(word.charAt(word.length()-1) == '.')
            word = word.substring(0,word.length()-1);

        return word;
    }

    private void handleWords(String word){


        word = startEndWord(word);
        if(word != null && word.length() > 1) {
            /// check if word is in lower letters
            if (word.equals(word.toLowerCase())) {
                /// word is save in the Dic
                if(termDic.containsKey(word)){

                }
                else if(termDic.containsKey(word.toUpperCase())){
                    //check if the word is save as upper case


                }
                else{

                    //first occur

                }

            }
            ///check if the word is all in upper letter.
            else if (word.equals(word.toUpperCase())){

                if(termDic.containsKey(word)){

                }
                else if(termDic.containsKey(word.toLowerCase())){

                }
                else{
                    //first occur
                }
            }
            /// check if the first char in the word is upper letter
            else if (word.charAt(0) == word.toUpperCase().charAt(0)){
                if(termDic.containsKey(word.toUpperCase())){

                }
                else{
                    // first occur
                }
            }else{
                word = word.toLowerCase();
            }

        }
    }

    public void handlePrice(String currToken ){
        String price = "";
        boolean sign =(currToken.charAt(0) == '$');
        String[] priceTerms = new String[4];
        int priceTermIndex = 0;
        for(int i = index ; i <allTokens.length && i<index+4 ; i++ ){
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

    private void handleDate(String num , String month){

        String numMonth = turnMonthToNumber(month);

        insertTermDic(numMonth+"-"+num);
        index++ ;


    }

    private void handlePercent(String num) {

        insertTermDic(num + "%");
        index = index +2;

    }


}



