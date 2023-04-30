package com.example.partsmaintenance.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.partsmaintenance.BPlusTree;
import com.example.partsmaintenance.Result;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/parts")
//@CrossOrigin  //处理跨域问题
public class PartsController {
    BPlusTree<String, String> bPlusTree = new BPlusTree(5);
    Map<String, Object> map = new TreeMap<>();

/*    *//**
     * 根据key查询零件信息
     * @param
     * @return
     *//*
    @GetMapping("/getByKey")
    public Map<String, Object> getByKey(@RequestParam(value = "key", required = false) String key){

        Map<String, Object> map = new TreeMap<>();
        map.put(key, bPlusTree.get(key));

        return map;
    }*/

    /**
     * 导入零件txt
     * @param multipartFile
     * @param request
     * @return
     */
    @PostMapping("/importTxt")
    public Map<String, Object> importTxt(@RequestParam(value = "file",required = true) MultipartFile multipartFile, HttpServletRequest request){
        bPlusTree.clear();
        map.clear();
        BufferedReader reader = null;
        try {
            File file = multipartFileToFile(multipartFile);
            // 读取文件
            String encoding = "utf-8";
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
            String line = reader.readLine();
            while (line != null) {
                String resultKey = line.substring(0, 7);
                String resultValue = line.substring(15);
                bPlusTree.put(resultKey, resultValue);
                map.put(resultKey, resultValue);
                line = reader.readLine();  // 读取下一行数据
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }finally {
            if(reader!=null){
                try {
                    reader.close();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
        return map;
    }
    /**
     * 功能描述: ADD
     * @auther: Xiaoyang Yu
     */
    @PostMapping("/add")
    public String add(@RequestBody Map<String, String> data) throws IOException {
        String key = data.get("key");
        String value = data.get("value");
        if(key == null || value == null){
            return "添加失败";
        }else if (map.containsKey(key)) {
            return "添加失败，存在相同ID";
        }
        bPlusTree.put(key, value);
        map.put(key, value);
        saveParts();
        return "添加成功";
    }
    /**
     * 功能描述: getSplitCount 要返回给前台这几个数字，分别是 总分裂数，parent分裂数，B+树的深度
     * @auther: Xiaoyang Yu
     */
    @GetMapping("/getSplitCount")
    public int[] getSplitCount(){
        //获取三个数字
        int totleNum = bPlusTree.leafSplitCount();
        int parentNum = bPlusTree.parentSplitCount();
        int deepth = bPlusTree.height();
        int[] count = {totleNum, parentNum, deepth};
        return count;
    }
    /**
     * 功能描述: getTenValue
     * @auther: Xiaoyang Yu
     */
    @GetMapping("/getTenValueByKey")
    public Map<String, Object> getTenValue(@RequestParam(value = "key", required = false) String key) {
        List<Result> list = new ArrayList<>();
        Iterator<String> iter = map.keySet().iterator();
        String currentKey;
        String value;
        Result result;

        // 添加当前 key 和接下来 10 条数据到列表中
        while (iter.hasNext() && list.size() < 11) {
            currentKey = iter.next();
            value = (String) map.get(currentKey);
            if (key == null || currentKey.equals(key)) {
                // key 为 null 或当前记录的 key 与参数 key 相同时，添加记录到列表中
                result = new Result();
                result.setId(currentKey);
                result.setMessage(value);
                list.add(result);
            } else if (list.size() > 0) {
                // 已经添加了参数 key 对应的记录到列表中，继续添加接下来的 9 条记录
                result = new Result();
                result.setId(currentKey);
                result.setMessage(value);
                list.add(result);
            }
        }

        Map<String,Object> resultMap = new HashMap<>();
        resultMap.put("dataList", list);
        return resultMap;
    }

    /**
     * 修改零件信息
     * @param
     * @return
     */
    @RequestMapping("/updateDetail")
    public String updateDetail(@RequestBody Map<String, String> data) throws IOException {
        String key = data.get("key");
        String value = data.get("value");
        if(key == null){
            return "修改失败";
        }
        map.put(key, value);
        saveParts();
        return "修改成功";
    }

    /**
     * 删除零件信息
     * @param
     * @return
     */
    @RequestMapping("/deleteByKey")
    public void deleteByKey(@RequestBody Map<String, String> data) throws IOException {
        String key = data.get("key");
        if(key == null){
            System.out.println("没有key");
        }
        map.remove(key);
        saveParts();
    }

    /**
     * 退出后保存txt
     * @return
     */
    @RequestMapping("/saveParts")
    public void saveParts() throws IOException {
        //LocalDateTime currentDateTime = LocalDateTime.now();
        //DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        String path="src\\main\\resources\\static\\Parts-Data.txt";

        File newFile = new File(path);
        newFile.createNewFile();
        // 将Map对象保存到文本文件中
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String line = entry.getKey() + "        " + entry.getValue() + "\n";
                writer.write(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     *
     * 功能描述: 下载文件
     * @auther: Xiaoyang Yu
     */
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile() throws IOException {

        // 指定文件路径
        Path filePath = Paths.get("src\\main\\resources\\static\\Parts-Data.txt");

        // 获取文件MIME类型
        String mimeType = Files.probeContentType(filePath);

        // 构建Resource对象
        Resource resource = new UrlResource(filePath.toUri());

        // 构建ResponseEntity对象
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .contentType(MediaType.parseMediaType(mimeType))
                .body(resource);
    }
    /**
     * MultipartFile 转 File
     *
     * @param file
     * @throws Exception
     */
    public static File multipartFileToFile(MultipartFile file) throws Exception {

        File toFile = null;
        if (file.equals("") || file.getSize() <= 0) {
            file = null;
        } else {
            InputStream ins = null;
            ins = file.getInputStream();
            toFile = new File(file.getOriginalFilename());
            inputStreamToFile(ins, toFile);
            ins.close();
        }
        return toFile;
    }

    //获取流文件
    private static void inputStreamToFile(InputStream ins, File file) {
        try {
            OutputStream os = new FileOutputStream(file);
            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            ins.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
