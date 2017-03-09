package bfile.service;

import bfile.entity.BFileEntityResult1;
import bfile.entity.BFileEntityResult2;
import bfile.util.DateUtils;
import bfile.util.Global;
import bfile.util.ZipUtil;
import com.alibaba.fastjson.JSON;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.*;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * B文件处理SERVICE
 * Created by chenjingsi on 16-11-22.
 */
@Service
public class BFileService {

    private static Logger logger = Logger.getLogger(BFileService.class);

    private static final String B_MOVE_TO_PATH = Global.getConfig("bmoveToPath");
    /**
     * 根据风机号获取B文件列表
     * @param machineNo
     * @return
     */
    public String getFileList(String machineNo) {
        Map<String, Object> returnMap = new HashMap<>();
        List<String> fileNameList = new ArrayList<>();
        Configuration conf = new Configuration();//建立配置
        try {
            String fieldNo = machineNo.substring(0,6);
            FileSystem fs = FileSystem.get(conf);//建立与HDFS的连接
            RemoteIterator<LocatedFileStatus> listItr = fs.listFiles(new Path("/user/gw/"+fieldNo+"/"+machineNo), true);//获取到文件列表的迭代器
            while (listItr.hasNext()) {
                Path path = listItr.next().getPath();
                fileNameList.add(path.getName());
            }
            returnMap.put("code", "0");
            returnMap.put("resultList", fileNameList);
            return JSON.toJSONString(returnMap);
        } catch (IOException e) {
            returnMap.put("code", "1");
            return JSON.toJSONString(returnMap);
        }

    }

    /**
     * 上传B文件到HDFS
     * @param bFile
     * @param fieldNo
     * @param machineNo
     * @param fileName
     * @return
     */
    public Map<String, Object> uploadFileFromWeb(MultipartFile bFile, String year,String fieldNo, String machineNo, String fileName) {
        Map<String,Object> returnMap = new HashMap<>();
        Configuration conf = new Configuration();
        System.setProperty("hadoop.home.dir", "/"); //设置了一个HDFS的根路径，不然后续会有问题
        FileInputStream fis = null;
        FSDataOutputStream fsdOutputStream = null;
        FileSystem fs = null;
        try {
            fs = FileSystem.get(conf);
            //fs.copyFromLocalFile(false,true, new Path(""), new Path(""));
            fsdOutputStream = fs.create(new Path("/user/goldwind/b/"+year+"/"+fieldNo+"/"+machineNo+"/"+fileName),true); //在HDFS上创建一个空的文件

            CommonsMultipartFile cf = (CommonsMultipartFile)bFile; //文件转型
            DiskFileItem fi = (DiskFileItem) cf.getFileItem();
            File file = fi.getStoreLocation();


            fis = new FileInputStream(file);//创建文件流以及写入数据
            byte[] buff = new byte[1024];
            int readCount = 0;
            readCount = fis.read(buff);
            while (readCount != -1) {
                fsdOutputStream.write(buff, 0, readCount);
                readCount = fis.read(buff);
            }
            returnMap.put("code",0);
            returnMap.put("result","success");
            return returnMap;
        } catch (IOException e) {
            returnMap.put("code",1);
            returnMap.put("result","error");
            return returnMap;
        }finally{
            if(fis != null){
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(fsdOutputStream != null){
                try {
                    fsdOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(fs != null){
                try {
                    fs.closeAll();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * 供批量上传B文件的多线程类
     */
    static class uploadB implements Runnable {
        private String srcPath;

        private FileSystem fs;

        public uploadB() {

        }

        public uploadB(String srcPath,FileSystem fs) {
            this.srcPath = srcPath;
            this.fs = fs;
        }

        @Override
        public void run() {
            FileFilter filter = pathname -> {
                String suffix = pathname.getName().toLowerCase();
                return suffix.endsWith(".arc") && (suffix.startsWith("b") || suffix.startsWith("plcb"));
            };//文件过滤器
            File dir = new File(srcPath);
            File[] fileArray = dir.listFiles(filter);//得到过滤后的文件列表
            String dirPath = dir.getName();
            for (File f : fileArray) {//遍历得到每个文件
                Map<String, String> returnMap = ZipUtil.unzip(f.getPath());//解压arc文件
                if (returnMap.get("success").equals("true")) {
                    try {
                        String year = "20"+returnMap.get("fileName").substring(1,3);//获取文件所属年份
                        String[] nos =  Global.getFilednoAndMachineno(f.getName());
                        String fieldNo = nos[0];
                        String machineNo = nos[1];
                        //上传文件到HDFS
                        fs.copyFromLocalFile(false, true, new Path(returnMap.get("filePathNew")), new Path("/user/goldwind/b/"+year+"/"+ fieldNo + "/" + fieldNo+machineNo + "/" + returnMap.get("fileName")));

                        FileUtils.moveFile(f,new File(B_MOVE_TO_PATH+f.getParentFile().getName()+"/"+f.getName()));//移动源文件到目标文件夹
                        //删除掉解压出的文件
                        new File(returnMap.get("filePathNew")).delete();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    continue;
                }

            }
            logger.info(srcPath+"已上传完成");
        }
    }


    /**
     * 获取B文件中的所有数据（分两次分别读取模拟量和状态量）
     * @param file
     * @return
     */
    public String getBData(File file) {
        Map<String, Object> result = new HashMap<>();
        try {
            FileReader reader1 = new FileReader(file);
            FileReader reader2 = new FileReader(file);
            BufferedReader bufferedReader1 = new BufferedReader(reader1);//模拟量reader
            Map<String, Object> analogMap = this.getAnalog(bufferedReader1);
            BufferedReader bufferedReader2 = new BufferedReader(reader2);//状态量reader
            Map<String, Object> statusMap = this.getStatus(bufferedReader2);
            result.put("analog", analogMap);
            result.put("status", statusMap);
            bufferedReader1.close();
            bufferedReader2.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return JSON.toJSONString(result);
    }

    /**
     * 根据B文件内容获取模拟量
     * @param bufferedReader
     * @return
     * @throws IOException
     */
    private Map<String, Object> getAnalog(BufferedReader bufferedReader) throws IOException {
        List<String> labelList = new ArrayList<>();
        List<String> headerList = new ArrayList<>();
        List<List<String>> dataList = new ArrayList<>();
        Map<String, Object> result = new HashMap<>();
        String strHead = "";
        int lineCount = 0;
        while ((strHead = bufferedReader.readLine()) != null) {
            if (lineCount >= 3004) {//行数小于3004为模拟量数据
                break;
            }
            if (strHead.startsWith("#time")) {//#timestamp开头的为表头
                String[] fields = strHead.split(";");

                for (String field : fields) {
                    if (field.equals("#timestamp")) {
                        headerList.add("timestamp");
                    } else {
                        headerList.add(field.trim());
                        //TODO：此处需要写枚举
                        //labelList.add(BFieldAnalogEnum.valueOf(field.trim().toUpperCase()).getField());//遍历以及枚举出对应的字段标签
                    }
                }
            }
            if (lineCount > 3) {//过滤掉前3行的文件信息
                List<String> row = new ArrayList<>();
                String[] datas = strHead.split(";");
                for (String data : datas) {
                    row.add(data);
                }
                dataList.add(row);
            }

            lineCount++;//行数控制
        }
        result.put("label", labelList);
        result.put("field", headerList);
        result.put("data", dataList);
        return result;
    }

    /**
     * 根据B文件内容获取到状态量
     * @param bufferedReader
     * @return
     * @throws IOException
     */
    private Map<String, Object> getStatus(BufferedReader bufferedReader) throws IOException {
        List<String> labelList = new ArrayList<>();
        List<String> headerList = new ArrayList<>();
        List<List<String>> dataList = new ArrayList<>();
        Map<String, Object> result = new HashMap<>();
        String strHead = "";
        int lineCount = 0;
        while ((strHead = bufferedReader.readLine()) != null) {
            if (lineCount < 3007) {//忽略前3007行
                lineCount++;
            } else {
                if (strHead.startsWith("#time")) {
                    String[] fields = strHead.split(";");
                    for (String field : fields) {
                        if (field.equals("#timestamp")) {
                            headerList.add("timestamp");
                        } else {
                            headerList.add(field.trim());
                            //TODO：需要在此处写枚举（参考模拟量的ENUM）
//                            labelList.add(BFieldAnalogEnum.valueOf(field.trim().toUpperCase()).getField());//
                        }
                    }
                } else {
                    List<String> row = new ArrayList<>();
                    String[] datas = strHead.split(";");
                    for (String data : datas) {
                        row.add(data);
                    }
                    dataList.add(row);
                }
                lineCount++;
            }
        }
        result.put("label", labelList);
        result.put("field", headerList);
        result.put("data", dataList);
        return result;
    }

    /**
     * 根据条件分析B数据
     * @param conditions
     * @param dataMap
     * @param type
     * @param fileName
     * @return
     * @throws ParseException
     */
    public BFileEntityResult1 getDataByCondition(String conditions,Map<String,Object> dataMap,Integer type,String fileName) throws ParseException {
        List<String> fieldList = new ArrayList<>();
        List<List<String>> dataList = new ArrayList<>();
        BFileEntityResult1 bFileEntityResult1 = new BFileEntityResult1();
        List<BFileEntityResult2> bFileEntityResult2List = new ArrayList<>();
        List<String> times = new ArrayList<>();

        if(type==1){//type1为模拟量
            fieldList = (List<String>) ((Map<String, Object>) dataMap.get("analog")).get("field");
            dataList = (List<List<String>>) ((Map<String, Object>) dataMap.get("analog")).get("data");
        }else
        if(type ==2){//type2为状态量
            fieldList = (List<String>) ((Map<String, Object>) dataMap.get("status")).get("field");
            dataList = (List<List<String>>) ((Map<String, Object>) dataMap.get("status")).get("data");
        }
        List<BFileEntityResult1> resultList = new ArrayList<>();

        String[] conditionArray = conditions.split(",");//条件数组
        for(int i=0;i<conditionArray.length;i++){//遍历条件
            BFileEntityResult2 bFileEntityResult2 = new BFileEntityResult2();
            List<String> values = new ArrayList<>();

            int idx = this.fieldIndex(conditionArray[i],fieldList);//获取条件所处表头的位置下标

            bFileEntityResult2.setField(fieldList.get(idx));//set条件

            values.addAll(dataList.stream().map(data -> data.get(idx)).collect(Collectors.toList()));//将每一行对应条件下标的数据放入values中


            bFileEntityResult2.setValues(values);
            bFileEntityResult2List.add(bFileEntityResult2);
        }

        for(List<String> data : dataList){
            times.add(DateHandle(fileName,Double.parseDouble(data.get(0))));//根据文件名获取时间点，再根据第一列的相对时间处理出数据点时间
        }

        bFileEntityResult1.setTimes(times);
        bFileEntityResult1.setDatas(bFileEntityResult2List);
        return bFileEntityResult1;
    }

    /**
     * 获取到条件在表头所处的下标
     * @param field
     * @param fieldList
     * @return
     */
    private int fieldIndex(String field,List<String> fieldList){
        int idx = 0;
        for (int i = 0; i < fieldList.size(); i++) {
            if(field.equals(fieldList.get(i))){
                idx = i;
            }
        }
        return idx;
    }

    /**
     * 相对时间的处理
     * @param fileName
     * @param relativeTime
     * @return
     * @throws ParseException
     */
    private String DateHandle(String fileName,double relativeTime) throws ParseException {
        String name = fileName.substring(1,fileName.length()-8);//截取出文件时间点的初始时间
        Date date = DateUtils.parseDate(name,"YYMMdd_HHmm");//date格式化
        Date d  = DateUtils.addMilliseconds(date,(int)(relativeTime*1000));//毫秒级的时间相加
        return DateUtils.formatDate(d,"yyyy-MM-dd HH:mm:ss.SSS");//返回绝对时间
    }

    /**
     * 从HDFS上拉取文件
     * @param fieldNo
     * @param machineNo
     * @param fileName
     * @return
     */
    public File getBFileFromHDFS(String year,String fieldNo,String machineNo,String fileName){
        Configuration conf = new Configuration();
        File file = null;
        try {
            FileSystem fs = FileSystem.get(conf);
            Path srcPath = new Path("/tmp/bFileFromHDFS.txt");//临时文件路径
            Path dstPath = new Path("/user/goldwind/b/"+year+"/"+fieldNo+"/"+machineNo+"/"+fileName);
            fs.copyToLocalFile(dstPath,srcPath);
            file = new File(srcPath.toString());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return file;
    }

    public int uploadBFileBatch(String srcPath) throws IOException {

        logger.info(srcPath);

        System.out.println("---------------"+srcPath+"----------------------");

        System.setProperty("hadoop.home.dir", "/");

        Configuration conf = new Configuration();

        FileSystem fs = FileSystem.get(conf);

        ExecutorService pool = Executors.newFixedThreadPool(10);

        File file = new File(srcPath);
        System.out.println("是否存在"+file.exists());
        System.out.println("是否是文件夹"+file.isDirectory());
        System.out.println("是否可读"+file.canRead());
        File[] dirs = new File(srcPath).listFiles();

        if(dirs==null){
            return 0;
        }else{
            for(File dir : dirs){
                pool.submit(new uploadB(dir.getPath(),fs));
            }
            return 1;
        }



    }


//    public static void main(String[] args) throws IOException, ParseException {
//

//
//    }


}

