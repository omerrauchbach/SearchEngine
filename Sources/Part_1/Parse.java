package Part_1;



import javafx.scene.control.Alert;

import java.io.*;
import java.util.*;

public class Parse {

    public static Queue<Document> documentsSet = new LinkedList<>();
    private HashMap<String, Integer> stopWords;
    public static HashMap<String , int[]> termDic = new HashMap<>();
    public static String[] allTokens ;
    private int index ;
    private String[] sums = {"Dollars","million","billion","trillion","m","bn"};
    private String[] months = {"jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec"};
    
    public Parse(){

    }

    private void parseDocs() {

        while (!documentsSet.isEmpty()) {
            Document newDoc = documentsSet.poll();
            String text = newDoc.getText().toString();
            allTokens = text.split(" (?!,[0-9])[ ,?@!:;+()_(\"\\r\\n]+");

            for ( index = 0; index < allTokens.length; index++) {
                String rightToken;
                String currToken = allTokens[index].trim();

                System.out.println(currToken);
                if(currToken.equals("")||  currToken.equals("\n"))
                    continue;
                //===================== price =========================//
                if (isDate(currToken)) {
                    handlePrice(currToken);
                    continue;
                }
                else if(isPrice(currToken)){
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
                    handleNum(currToken);
                }

                else if(isBetween(currToken)) {
                    insertTermDic(currToken+allTokens[index+1]+allTokens[index+2]+allTokens[index+3]);
                    index = index+4;
                }else if(isLine(currToken)){
                    handleLine(currToken);
                    index++;
                }else{
                    handleWords(currToken);
                    index++;
                }
            }
        }
    }

    private void insertFirstOccur(String term){
        if(term != null && !term.equals("")) {
            int[] data = new int[4];
            data[0] = 1;
            termDic.put(term, data);
        }
    }

    private void insertTermDic(String term){
        if(term != null && !term.equals("")) {
            int[] newData = termDic.get(term);
            newData[0]++;
            termDic.replace(term, newData);
        }

    }

    private void changeUpperCaseToLowerCase(String term){

        if(term != null && !term.equals("")) {
            int[] newData = termDic.get(term.toUpperCase());
            newData[0]++;
            termDic.remove(term.toUpperCase());
            termDic.put(term , newData);
        }

    }

    private String numericToPrice(String num ,String sum,String fraction ,boolean sign, boolean Dollars , boolean US ){
        //String price = num.replace("," , "");
        String price = num;
        if(sign)
            price = price.substring(1);
        if(lessThanMillion(price) && !sum.equals("million") && !sum.equals("billion") && !sum.equals("m") && !sum.equals("bn")){
            if(!fraction.equals(""))
                price = price+" "+fraction+" Dollars";
            else
                price = price+" Dollars";
        }else{  ///greater then M
            if ((sum.equals("million") && US && Dollars) || (sum.equals("m") && Dollars) || (sign && sum.equals("million")))
                price = price + " M Dollars";
            else if ((sum.equals("billion") && US && Dollars)  || (sum.equals("bn") && Dollars) || (sign && sum.equals("billion")))
                price = price + "000 M Dollars"; //adds 3 zeroes. B ==> M.
            else if (sum.equals("trillion") && US && Dollars)
                price = price + "000000 M Dollars"; //adds 6 ??? zeroes. T ==> M.
            else if ((Dollars && !sign && !US) || (sign && !US && !Dollars)) { // 1,000,000 Dollars // $450,000,000
                price = price.replace("," , "");
                price = Integer.parseInt(price) / 1000000 + " M Dollars"; //1 M Dollars
            }
        }
        System.out.println(price);
        return "" ;
    }

    private boolean equalToSum(String word){
        for(String sum : sums ){
            if(sum.equals(word))
                return true;
        }
        return false;
    }

    private boolean isPrice(String price){

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

        String notEsrony = numToken;
        if (numToken.contains("."))
            notEsrony = numToken.substring(0, numToken.indexOf("."));
        if (Integer.parseInt(notEsrony.replace(",", "")) < 1000000)
            return true;
        return false;
    }

    private boolean isThousand (String numToken){
        String num  = numToken.replace("," , "");
        if (Double.parseDouble(num) >= 1000 && (Double.parseDouble(num) < 1000000))
            return true;
        return false;
    }

    private boolean isMillion (String numToken){
        if (Integer.parseInt(numToken) >= 1000000 && (Integer.parseInt(numToken) < 1000000000))
            return true;
        return false;
    }

    private boolean lessThenThousand(String numToken){

        try {
            if (Double.parseDouble(numToken) < 1000)
                return true;
        }catch (NumberFormatException e){
            return false;
        }
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

    private boolean isLine(String token){
        if(token.contains("-"))
            return true;
        return false;
    }

    /**
     *  function that get a number and remove
     *  all number after the 3 char after the "."
     * @param numToken - number
     * @return correct num.
     */
    private String returnDouble(String numToken){

        if(numToken.contains(".")){
            int charIndex = numToken.charAt('.')+4;
            if(charIndex > numToken.length())
                return numToken;
            else
                return numToken.substring(0,charIndex);

        }else
            return numToken;

    }

    /**
     * function that shift the char "." number of times.
     * @param numToken - number
     * @param shift - the number of shift for "."
     * @return shift number
     */
    private String shiftLeft(String numToken ,int shift ){

        String ans = "";
        int charIndex;
        if(numToken.contains(".")) {
            charIndex = numToken.charAt('.');
            if(charIndex >4){
                ans = numToken.replace("." , "");
                ans = ans.substring(0,charIndex-shift)+"."+ans.substring(charIndex-shift);
                return ans;
            }else
                return numToken;
        }else{
            if(numToken.length()>3) {
                charIndex = numToken.length() - shift;
                if(charIndex+3 > numToken.length())
                    ans = numToken.substring(0, charIndex) + "." + ans.substring(charIndex);
                else
                    ans = numToken.substring(0, charIndex) + "." + ans.substring(charIndex,charIndex+3);
                return ans;
            }else
                return numToken;

        }

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

    private boolean isBetween(String term){

        if(term.toLowerCase().equals("between") && ((index+3) <= allTokens.length) ){
           if(allTokens[index+2].equals("and")){
               boolean isNumber_1 = isNumericDouble(allTokens[index+1]);
               boolean isNumber_2 = isNumericDouble(allTokens[index+3]);
               return isNumber_1 && isNumber_2;
           }
        }
        return false;
    }

    private static String turnMonthToNumber (String docMonth){ //turns the month name to number

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
        if(word.charAt(0) == '.' || word.charAt(0) == ':' || word.charAt(0) == ',')
            word = word.substring(1);
        if(word.charAt(word.length()-1) == '.' || word.charAt(word.length()-1) ==':' || word.charAt(word.length()-1) == ',')
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
                    insertTermDic(word);
                }
                else if(termDic.containsKey(word.toUpperCase())){
                    //check if the word is save as upper case
                    changeUpperCaseToLowerCase(word);

                }
                else{

                    //first occur
                    insertFirstOccur(word);
                }

            }
            ///check if the word is all in upper letter.
            else if (word.equals(word.toUpperCase())){

                if(termDic.containsKey(word)){
                    insertTermDic(word);
                }
                else if(termDic.containsKey(word.toLowerCase())){
                    insertTermDic(word.toLowerCase());
                }
                else{
                    //first occur
                    insertFirstOccur(word);
                }
            }
            /// check if the first char in the word is upper letter
            else if (word.charAt(0) == word.toUpperCase().charAt(0)){
                if(termDic.containsKey(word.toUpperCase())){
                    insertTermDic(word.toUpperCase());
                }
                else if(termDic.containsKey(word.toLowerCase())){
                    insertFirstOccur(word.toLowerCase());
                }else{
                    insertFirstOccur(word.toUpperCase());
                }
            }
        }
    }

    private void handlePrice(String currToken ){
        String price = "";
        boolean sign =(currToken.charAt(0) == '$');
        String[] priceTerms = new String[4];
        int priceTermIndex = 0;
        for(int i = index ; i <allTokens.length && i<index+4 ; i++ ){
            if(allTokens[i].equals("")) {
                index++;
                continue;

            }


            priceTerms[priceTermIndex] = allTokens[i];
            priceTermIndex++;
        }
        for(String priceTerm : priceTerms){
            if(priceTerm == null)
                priceTerm = "";
        }

        if (priceTerms[0] == null)
            //// ????????????????????? END OF TEXTTTTT ????????????????

        sign =(priceTerms[0].charAt(0) == '$');

        if(equalToSum(priceTerms[1])){
            if(priceTerms[1].equals("Dollars")){
                price = numericToPrice(priceTerms[0],priceTerms[1],"", sign, true , false);
                index= index+2;
            }else if(priceTerms[1].equals("million") || priceTerms[1].equals("billion")){
                if(priceTerms[2].equals("U.S.") && priceTerms[3].equals("dollars")) {
                    price = numericToPrice(priceTerms[0], priceTerms[1],"",  sign, true, true);
                    index= index+4;
                }else { // $100 million
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





    private void handleNum(String intNum) {

        String sum = "";
        String zero = "";
        intNum = intNum.replace(",", "");
        boolean twoTerm = index < allTokens.length;
        if (twoTerm) {
            if (allTokens[index + 1].toLowerCase().equals("thousand")) {
                sum = "K";
                zero = "000";
            } else if (allTokens[index + 1].toLowerCase().equals("million")) {
                sum = "M";
                zero = "000000";
            } else if (allTokens[index + 1].toLowerCase().equals("billion")) {
                sum = "B";
                zero = "000000000";
            }
        }
        if (isFraction(intNum) && !sum.equals("")) {
            int fraction = intNum.charAt('.');

            intNum = intNum.substring(0, fraction) + zero + intNum.substring(fraction);
        }
        if (sum.equals("") && lessThenThousand(intNum)) {
            if (twoTerm && isFraction(allTokens[index + 1])) {
                insertTermDic(intNum + " " + allTokens[index + 1]);
                index = index + 2;
            } else {
                insertTermDic(intNum);
                index++;
            }

        }
        if (isThousand(intNum) || sum.equals("K")) {//only 100,123 (K)
            String num = shiftLeft(returnDouble(intNum), 3);
            insertTermDic(num + sum);
        } else if (isMillion(intNum) || sum.equals("M")) { // only 100,123,333 (M)
            String num = shiftLeft(returnDouble(intNum), 6);
            insertTermDic(num + sum);
        } else if (isBillion(intNum) || sum.equals("B")){ // only 100,123,333,000 (B)
            String num = shiftLeft(returnDouble(intNum), 9);
            insertTermDic(num + sum);
        }
    }

    private void handleLine(String line){

        if(termDic.containsKey(line))
            insertTermDic(line);
        else
            insertFirstOccur(line);



    }

    public void start(){
        parseDocs();
    }



}



