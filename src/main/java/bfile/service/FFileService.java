package bfile.service;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by chenjingsi on 16-11-29.
 */
@Service
public class FFileService {

    private static Logger logger = Logger.getLogger(FFileService.class);

    private static final String F_MOVE_TO_PATH = Global.getConfig("fmoveToPath");
    /**
     * 根据风机号获取F文件列表
     * @param year
     * @param machineNo
     * @return
     */
    public String getFileList(String year,String machineNo) {
        Map<String, Object> returnMap = new HashMap<>();
        List<String> fileNameList = new ArrayList<>();
        Configuration conf = new Configuration();
        try {
            String fieldNo = machineNo.substring(0,6);
            FileSystem fs = FileSystem.get(conf);//建立与HDFS的连接
            RemoteIterator<LocatedFileStatus> listItr = fs.listFiles(new Path("/user/goldwind/f/"+year+"/"+fieldNo+"/"+machineNo+"/txt"), true);//获取到文件数据列表迭代器
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
     * 从HDFS上拉取F文件
     * @param year
     * @param fieldNo
     * @param machineNo
     * @param fileName
     * @return
     */
    public File getFFileFromHDFS(String year,String fieldNo,String machineNo,String fileName){
        Configuration conf = new Configuration();
        File file = null;
        try {
            FileSystem fs = FileSystem.get(conf);
            Path srcPath = new Path("/tmp/fFileFromHDFS.txt");
            Path dstPath = new Path("/user/goldwind/f/"+year+"/"+fieldNo+"/"+machineNo+"/txt/"+fileName);
            fs.copyToLocalFile(dstPath,srcPath);
            file = new File(srcPath.toString());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return file;
    }

    /**
     * 获取F文件的数据
     * @param file
     * @return
     * @throws IOException
     */
    public Map<String,String> getAllFData(File file) throws IOException {
        Map<String,String> returnMap = new HashMap<String,String>();
        FileReader fFileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fFileReader);
        String strHead = "";
        int count = 0 ;
        while ((strHead = bufferedReader.readLine()) != null) {
            if(count >1){
                String[] stringArray = strHead.trim().split("=");//按=分割成KEY-VALUE格式
                returnMap.put(stringArray[0],stringArray[1]);
            }
            count ++;
        }
        return returnMap;
    }

//
//    public static void main(String[] args) throws IOException {
//
//        Configuration conf = new Configuration();
//
//        FileSystem fs = FileSystem.get(conf);
//
//        ExecutorService pool = Executors.newFixedThreadPool(10);
//
//        File[] dir2015 = new File("/home/chenjingsi/金风文件/F-files/2015/PlcFFile/").listFiles();
//        File[] dir2016 = new File("/home/chenjingsi/金风文件/F-files/2016/PlcFFile/").listFiles();
//
//        for(File dir : dir2015){
//            pool.submit(new uploadF(dir.getPath(),fs));
//        }
//        for(File dir : dir2016){
//            pool.submit(new uploadF(dir.getPath(),fs));
//        }
//
//    }

    public int uploadFFileBatch(String srcPath) throws IOException {
        Configuration conf = new Configuration();

        FileSystem fs = FileSystem.get(conf);

        ExecutorService pool = Executors.newFixedThreadPool(10);

        File[] dirs = new File(srcPath).listFiles();

        if(dirs==null){
            return 0;
        }else{
            for(File dir : dirs){
                pool.submit(new uploadF(dir.getPath(),fs));
            }
            return 1;
        }

    }

    /**
     * 供批量导入的异常信息收集
     * @param path
     */
    public static void exceptionCollect(String path) {

        FileWriter fw = null;
        try {
        //如果文件存在，则追加内容；如果文件不存在，则创建文件
            File f=new File("/home/chenjingsi/exceptionCollect.txt");
            fw = new FileWriter(f, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        PrintWriter pw = new PrintWriter(fw);
        pw.println(path);
        pw.flush();
        try {
            fw.flush();
            pw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    /**
     * 上传F文件到HDFS
     * @param bFile
     * @param year
     * @param fieldNo
     * @param machineNo
     * @param fileName
     * @return
     */
    public Map<String, Object> uploadFFileFromWeb(MultipartFile bFile, String year, String fieldNo, String machineNo, String fileName) {
        Map<String,Object> returnMap = new HashMap<>();
        Configuration conf = new Configuration();
        System.setProperty("hadoop.home.dir", "/");
        FileInputStream fis = null;
        FSDataOutputStream fsdOutputStream = null;
        FileSystem fs = null;
        try {
            fs = FileSystem.get(conf);
            //在HDFS上创建文件
            fsdOutputStream = fs.create(new Path("/user/goldwind/f/"+year+"/"+fieldNo+"/"+machineNo+"/"+"txt/"+fileName),true);

            CommonsMultipartFile cf = (CommonsMultipartFile)bFile;//文件转型
            DiskFileItem fi = (DiskFileItem) cf.getFileItem();
            File file = fi.getStoreLocation();

            //写文件
            fis = new FileInputStream(file);
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
     * 供批量上传的多线程类
     */
    static class uploadF implements Runnable {
        private String srcPath;

        private FileSystem fs;

        public uploadF() {

        }

        public uploadF(String srcPath,FileSystem fs) {
            this.srcPath = srcPath;
            this.fs = fs;
        }

        @Override
        public void run() {
            try {
//                String srcPath = "/home/chenjingsi/金风文件/F-files/2015/PlcFFile/";

                File dirr = new File(srcPath);
                FileFilter filterDirectory = new FileFilter() {//文件夹过滤器
                    @Override
                    public boolean accept(File file) {
                        if(file.isDirectory()){
                            return true;
                        }
                        return false;
                    }
                };
                FileFilter fileFilterHArc = new FileFilter() {//plcfh.arc过滤
                    @Override
                    public boolean accept(File file) {
                        String suffix = file.getName().toLowerCase();
                        if(suffix.startsWith("plcfh") && suffix.endsWith(".arc")){
                            return true;
                        }
                        return false;
                    }
                };
                FileFilter fileFilterTArc = new FileFilter() {//plcft.arc过滤
                    @Override
                    public boolean accept(File file) {
                        String suffix = file.getName().toLowerCase();
                        if(suffix.startsWith("plcft") && suffix.endsWith(".arc")){
                            return true;
                        }
                        return false;
                    }
                };
                    File[] directorys = dirr.listFiles(filterDirectory);
                    File[] hArcs = dirr.listFiles(fileFilterHArc);
                    File[] tArcs = dirr.listFiles(fileFilterTArc);//分开处理

                    for(File directory : directorys){//遍历文件夹上传其中的文件
                        File fFile;
                        if(directory.listFiles().length>0){
                            fFile = directory.listFiles()[0];
                        }else{
                            continue;
                        }

                        String fieldNo = "";
                        String machineNo = "";
                        try{
                            String[] nos =  Global.getFilednoAndMachineno(directory.getName());
                            fieldNo = nos[0];
                            machineNo = nos[1];
                        }catch(IndexOutOfBoundsException e){
                           // exceptionCollect(directory.getPath());
                            continue;
                        }


                        String fFilePath = fFile.getPath();

                        String year = "20"+fFile.getName().substring(1,3);//获取文件所属年份

                        String targetPath = "/user/goldwind/f/"+year+"/"+fieldNo+"/"+fieldNo+machineNo+"/"+"txt"+"/"+fFile.getName();

                        fs.copyFromLocalFile(false,true,new Path(fFilePath),new Path(targetPath));

                        FileUtils.moveDirectory(directory,new File(F_MOVE_TO_PATH+directory.getParentFile().getName()+"/"+directory.getName()));
                    }


                    for(File hArc : hArcs){//遍历plcfh.arc。先解压再上传

                        Map<String,String> returnMap = ZipUtil.unzip(hArc.getPath());//解压

                        String fieldNo = "";
                        String machineNo = "";
                        try{
                            String[] nos =  Global.getFilednoAndMachineno(hArc.getName());//根据文件名获取风场号风机号
                            fieldNo = nos[0];
                            machineNo = nos[1];
                        }catch(IndexOutOfBoundsException e){
                            //exceptionCollect(hArc.getPath());
                            continue;
                        }

                        if (returnMap.get("success").equals("true") && !returnMap.get("filePathNew").equals("") && returnMap.get("filePathNew")!=null) {
                            String year = "20"+returnMap.get("fileName").substring(1,3);//获取文件所属年份
                            fs.copyFromLocalFile(false,true,new Path(returnMap.get("filePathNew")),new Path( "/user/goldwind/f/"+year+"/"+fieldNo+"/"+fieldNo+machineNo+"/"+"html"+"/"+returnMap.get("fileName")));

                            FileUtils.moveFile(hArc,new File(F_MOVE_TO_PATH+hArc.getParentFile().getName()+"/"+hArc.getName()));//移动源文件

                            new File(returnMap.get("filePathNew")).delete();//删除解压出的文件

                        }else{
                            continue;
                        }

                    }

                    for(File tArc : tArcs){//遍历解压上传plcft.arc文件

                        Map<String,String> returnMap = ZipUtil.unzip(tArc.getPath());

                        String fieldNo = "";
                        String machineNo = "";
                        try{
                            String[] nos =  Global.getFilednoAndMachineno(tArc.getName());//根据文件名获取风机号风场号
                            fieldNo = nos[0];
                            machineNo = nos[1];
                        }catch(IndexOutOfBoundsException e){
                            //exceptionCollect(tArc.getPath());
                            continue;
                        }

                        if (returnMap.get("success").equals("true") && !returnMap.get("filePathNew").equals("") && returnMap.get("filePathNew")!=null) {
                            //上传
                            String year = "20"+returnMap.get("fileName").substring(1,3);//获取文件所属年份

                            fs.copyFromLocalFile(false,true,new Path(returnMap.get("filePathNew")),new Path( "/user/goldwind/f/"+year+"/"+fieldNo+"/"+fieldNo+machineNo+"/"+"txt"+"/"+returnMap.get("fileName")));

                            //移动源文件
                            FileUtils.moveFile(tArc,new File(tArc.getParentFile().getName()+"/"+tArc.getName()));
                            //删除解压出的文件
                            new File(returnMap.get("filePathNew")).delete();
                        }else{
                            continue;
                        }

                    }
                    logger.info(srcPath+"已上传完成");

            } catch (IOException e) {
                e.printStackTrace();
            }



        }
    }
}
