package bfile.util;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * 全局配置类
 * Created by chenjingsi on 16-12-14.
 */
public class Global {

    /**
     * 保存全局属性值
     */
    private static Map<String, String> map = Maps.newHashMap();
    /**
     * 属性文件加载对象
     */
    private static PropertiesLoader loader;
    static {
            loader = new PropertiesLoader("bfile.properties");
    }


    /**
     * 获取配置
     * @see {fns:getConfig('adminPath')}
     */
    public static String getConfig(String key) {
        String value = map.get(key);
        if (value == null){
            value = loader.getProperty(key);
            map.put(key, value != null ? value : StringUtils.EMPTY);
        }
        return value;
    }

    /**
     * 根据风机号获取风场号
     * @param name
     * @return
     */
    public static String[] getFilednoAndMachineno(String name){
        String[] ret = new String[2];
        String fieldNo = "";
        String machineNo = "";
        int choice = 0;

        int length = name.split("_")[1].length();
        if(length == 9 ) {//9位的按正常格式分割
            fieldNo = name.split("_")[1].substring(0, 6);
            machineNo = name.split("_")[1].substring(6, 9);
        }else
        if(length == 8){//8位的风机号补一个0
            fieldNo = name.split("_")[1].substring(0, 6);
            machineNo = "0" + name.split("_")[1].substring(6, 8);
            choice = 1;
        }else
        if(length == 7){//7位的风机号补2个0
            fieldNo = name.split("_")[1].substring(0, 6);
            machineNo = "00" + name.split("_")[1].substring(6, 7);
            choice = 2;
        }

        ret[0] = fieldNo;
        ret[1] = machineNo;


//        //以下为异常风机号的收集
//        if(choice == 1 || choice == 2){
//            FileWriter fw = null;
//            try {
//                //如果文件存在，则追加内容；如果文件不存在，则创建文件
//                File f=new File("/home/chenjingsi/风场风机号处理前后对比.txt");
//                fw = new FileWriter(f, true);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            PrintWriter pw = new PrintWriter(fw);
//            pw.println(name + "----------------->" + name.replaceAll(name.split("_")[1],fieldNo+machineNo));
//            pw.flush();
//            try {
//                fw.flush();
//                pw.close();
//                fw.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }

        return ret;
    }
}
