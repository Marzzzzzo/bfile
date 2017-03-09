package bfile.entity;

import java.util.List;

/**
 * Created by chenjingsi on 16-11-22.
 */
public class BFileEntityResult1 {

    private List<String> times;
    private List<BFileEntityResult2> datas;


    public List<String> getTimes() {
        return times;
    }

    public void setTimes(List<String> times) {
        this.times = times;
    }

    public List<BFileEntityResult2> getDatas() {
        return datas;
    }

    public void setDatas(List<BFileEntityResult2> datas) {
        this.datas = datas;
    }
}
