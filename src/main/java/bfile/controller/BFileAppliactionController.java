package bfile.controller;


import bfile.entity.BFileEntityResult1;
import bfile.service.BFileService;
import com.alibaba.fastjson.JSON;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.hbase.executor.ExecutorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Map;
import java.util.concurrent.Executors;


/**
 * B文件处理Controller
 * Created by chenjingsi on 16-11-17.
 */
@Controller
public class BFileAppliactionController {

    @Autowired
    private BFileService bFileService;

    /**
     * 根据风机号获取b文件列表
     * @param machineNo
     * @return
     */
    @RequestMapping(value="/getBFileList",produces="text/html;charset=UTF-8")
    @ResponseBody
    public String getFileList(String machineNo){

        return bFileService.getFileList(machineNo);

    }

    /**
     * 获取整个B文件内的所有数据
     * @param fieldNo
     * @param machineNo
     * @param fileName
     * @return
     */
    @RequestMapping(value="/getAllBData",produces="text/html;charset=UTF-8")
    @ResponseBody
    public String getBData(String year,String fieldNo,String machineNo,String fileName){
        File file = bFileService.getBFileFromHDFS(year,fieldNo,machineNo,fileName);
        return bFileService.getBData(file);
    }

    /**
     * 根据条件获取部分B文件中模拟量
     * @param conditions
     * @param fieldNo
     * @param machineNo
     * @param fileName
     * @return
     * @throws ParseException
     * @throws ParseException
     */
    @RequestMapping(value="/getAnalogByCondition",produces = "text/html;charset=UTF-8")
    @ResponseBody
    public String getAnalogDataByCondition(String year,String conditions,String fieldNo,String machineNo,String fileName) throws ParseException, ParseException {
        File file = bFileService.getBFileFromHDFS(year,fieldNo,machineNo,fileName);//先根据参数从HDFS上拉取文件供提取数据使用
        Map<String,Object> dataMap = JSON.parseObject(bFileService.getBData(file),Map.class);
        BFileEntityResult1 result = bFileService.getDataByCondition(conditions,dataMap,1,fileName);
        return JSON.toJSONString(result);
    }

    /**
     * 根据条件获取部分B文件中的状态量
     * @param conditions
     * @param fieldNo
     * @param machineNo
     * @param fileName
     * @return
     * @throws ParseException
     */
    @RequestMapping(value="/getStatusByCondition",produces = "text/html;charset=UTF-8")
    @ResponseBody
    public String getStatusDataByCondition(String year,String conditions,String fieldNo,String machineNo,String fileName) throws ParseException {
        File file = bFileService.getBFileFromHDFS(year,fieldNo,machineNo,fileName);//先根据参数从HDFS上拉取文件供提取数据使用
        Map<String,Object> dataMap = JSON.parseObject(bFileService.getBData(file),Map.class);
        BFileEntityResult1 result = bFileService.getDataByCondition(conditions,dataMap,2,fileName);
        return JSON.toJSONString(result);
    }

    /**
     * 通过接口方式上传文件到HDFS
     * @param bFile
     * @param fieldNo
     * @param machineNo
     * @param fileName
     * @return
     */
    @RequestMapping(value="/uploadFileFromWeb",method = RequestMethod.POST)
    @ResponseBody
    public String uploadFileFromWeb(@RequestParam(value="bFile",required = true) MultipartFile bFile,
                                    @RequestParam(value="year",required = true) String year,
                                    @RequestParam(value="fieldNo",required = true) String fieldNo,
                                    @RequestParam(value="machineNo",required = true) String machineNo,
                                    @RequestParam(value="fileName",required = true) String fileName){
        Map<String,Object> returnMap = bFileService.uploadFileFromWeb(bFile,year,fieldNo,machineNo,fileName);
        return JSON.toJSONString(returnMap);
    }

    @RequestMapping(value="/uploadBFileBatch",method = RequestMethod.GET)
    @ResponseBody
    public String uploadBFileBatch(String srcPath) throws IOException {

        int ret = bFileService.uploadBFileBatch(srcPath);

        return srcPath+"========================="+ret;
    }

}

