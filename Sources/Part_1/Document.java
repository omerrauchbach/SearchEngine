package Part_1;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Document {

    private String id;
    private String  docText;
    private int tfMax;
    private HashMap<String, int[]> termDic ;
    private String title ;

    public Document(){

        termDic = new HashMap<>();
    }

    public int uniqueTerm(){
        return termDic.size();
    }

    public int getTfMax() {
        return tfMax;
    }

    public HashMap<String, int[]> getAllTerms (){ return termDic; }

    public String getText() { return docText.toString(); }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setText(String text) {
        this.docText = text;
    }

    public void setTfMax(int tfMax) {
        this.tfMax = tfMax;
    }

    public void setTermDic (HashMap<String, int[]> tDic){
        termDic = tDic;
        tfMax = calMaxTf();
    }

    private int calMaxTf(){

        int Max =0 ;
        for(Map.Entry<String, int[]> entry : termDic.entrySet()){

            if(entry.getValue()[0] > Max)
                Max = entry.getValue()[0];

        }

        return Max;
    }

}
