package Part_1;

import java.util.HashMap;
import java.util.Set;

public class Document {

    private String id;
    private StringBuilder  docText;
    private int tfMax;
    private HashMap<String, int[]> termDic ;

    public Document(){
        docText = new StringBuilder();
        termDic = new HashMap<>();
    }

    public int uniqueTerm(){
        return termDic.size();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setText(StringBuilder text) {
        this.docText = text;
    }

    public void setTfMax(int tfMax) {
        this.tfMax = tfMax;
    }

    public StringBuilder getText() { return docText; }

    public int getTfMax() {
        return tfMax;
    }

    public void addText(String text){
        docText.append( text + System.lineSeparator() );
    }

    //public HashMap<String, int[]> getTermsMap() { return termDic; }

    public HashMap<String, int[]> getAllTerms (){ return termDic; }

    public void setTermDic (HashMap<String, int[]> tDic){
        termDic = tDic;
    }

}
