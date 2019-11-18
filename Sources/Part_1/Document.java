package Part_1;

import java.util.HashMap;

public class Document {

    private String id;
    private StringBuilder docText = new StringBuilder();
    private int tfMax;
    private HashMap<String, int[]> termDic = new HashMap<>();

    public Document(){

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

    public StringBuilder getText() {
        return docText;
    }

    public int getTfMax() {
        return tfMax;
    }

    public void addText(String text){
        docText.append( text + System.lineSeparator() );
    }
}
