package bfile.entity;

import java.util.List;

/**
 * Created by chenjingsi on 16-11-22.
 */
public class BFileEntityResult2 {

    private String label;
    private List<String> values;
    private String field;



    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }


    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

}
