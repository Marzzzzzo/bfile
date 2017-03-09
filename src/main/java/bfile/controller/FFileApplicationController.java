package bfile.controller;

import bfile.service.FFileService;
import bfile.util.StringUtils;
import com.alibaba.fastjson.JSON;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by chenjingsi on 16-12-2.
 */
@Controller
public class FFileApplicationController {

    @Autowired
    private FFileService fFileService;


    /**
     * 获取F文件列表
     * @param year
     * @param machineNo
     * @return
     */
    @RequestMapping(value="/getFFileList",produces="text/html;charset=UTF-8")
    @ResponseBody
    public String getFileList(String year,String machineNo){

        return fFileService.getFileList(year,machineNo);

    }

    /**
     * 获取F文件全部数据
     * @param year
     * @param fieldNo
     * @param machineNo
     * @param fileName
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/getAllFData")
    @ResponseBody
    public String getAllFData(String year,String fieldNo,String machineNo,String fileName) throws IOException {
        File file = fFileService.getFFileFromHDFS(year,fieldNo,machineNo,fileName);
        Map<String,String> returnMap = fFileService.getAllFData(file);

        return JSON.toJSONString(returnMap);
    }

    /**
     * 上传F文件到HDFS
     * @param fFile
     * @param year
     * @param fieldNo
     * @param machineNo
     * @param fileName
     * @return
     */
    @RequestMapping(value="/uploadFFileFromWeb")
    @ResponseBody
    public String uploadeFFileFromWeb(@RequestParam(value="fFile",required = true) MultipartFile fFile,
                                      @RequestParam(value="year",required = true) String year,
                                      @RequestParam(value="fieldNo",required = true) String fieldNo,
                                      @RequestParam(value="machineNo",required = true) String machineNo,
                                      @RequestParam(value="fileName",required = true) String fileName){
        Map<String,Object> returnMap = fFileService.uploadFFileFromWeb(fFile,year,fieldNo,machineNo,fileName);
        return JSON.toJSONString(returnMap);
    }

    /**
     * 根据条件获取F文件列表
     * @param year
     * @param fieldNo
     * @param machineNo
     * @param type
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/getFileListByCondition")
    @ResponseBody
    public String getFileListByCondition(@RequestParam(value="year",required = true) String year,
                                         @RequestParam(value="fieldNo",required = false) String fieldNo,
                                         @RequestParam(value="machineNo",required = false) String machineNo,
                                         @RequestParam(value="type",required = true) String type){

        String path = "";

        Map<String,Object> returnMap = new HashMap<>();

        List<String> list = new ArrayList<>();

        //根据参数传值的不同情况构建远程路径
        if(StringUtils.isBlank(fieldNo)){
            path = "/user/goldwind/"+type+"/"+year;
        }else
        if(StringUtils.isNotBlank(fieldNo) && StringUtils.isNotBlank(machineNo)){
            path = "/user/goldwind/"+type+"/"+year+"/"+fieldNo+"/"+machineNo;
        }else
        if(StringUtils.isNotBlank(fieldNo) && StringUtils.isBlank(machineNo)){
            path = "/user/goldwind/"+type+"/"+year+"/"+fieldNo;
        }


        Configuration conf = new Configuration();
        FileSystem fs = null;
        try {
            fs = FileSystem.get(conf);
            RemoteIterator<LocatedFileStatus> aa = fs.listFiles(new Path(path), true);//获取远程文件列表迭代器

            while(aa.hasNext()){
                String a = aa.next().getPath().getName().toString();
                list.add(a);
            }
            returnMap.put("status","success");
            returnMap.put("result",list);
        } catch (IOException e) {
            returnMap.put("status","fail");
            returnMap.put("result",list);
        }finally {
            return JSON.toJSONString(returnMap);
        }
    }

    @RequestMapping(value="/uploadFFileBatch",method = RequestMethod.GET)
    @ResponseBody
    public String uploadFFileBatch(String srcPath) throws IOException {


        fFileService.uploadFFileBatch(srcPath);

        return "上传开始";
    }
}
