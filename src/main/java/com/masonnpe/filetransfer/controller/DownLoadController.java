package com.masonnpe.filetransfer.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Objects;

@Controller
@Slf4j
public class DownLoadController {

    @RequestMapping("/download")
    public void down(HttpServletRequest req, HttpServletResponse resp) throws IOException, ParseException {
        resp.reset();
        long pos = 0;
        OutputStream os = null;
        FileInputStream is = null;
        try {
            File f = new File("E:\\百度云下载\\hadoop cdh\\hadoop-2.7.2.tar.gz");//要下载的文件
            String lastModifiedTime=new SimpleDateFormat("yyyy-MM-dd").parse("2018-12-12").toString();//文件最近一次修改时间
            String fileVersion="1.2";
            is = new FileInputStream(f);
            long fSize = f.length();
            byte xx[] = new byte[4096];
            resp.setHeader("Accept-Ranges", "bytes");
            resp.setHeader("Content-Length", fSize + "");
            resp.setHeader("Content-Disposition", "attachment;filename=" + f.getName());
            resp.setHeader("Last-Modified",lastModifiedTime);
            resp.setHeader("ETag",fileVersion+lastModifiedTime);
            if(req.getHeader("If-Range")!=null){
                if(!Objects.equals(req.getHeader("If-Range"),fileVersion+lastModifiedTime)){
                    resp.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
                    return;
                }
            }
            if (req.getHeader("Range") != null) {// 说明之前下载了一部分
                resp.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                pos = Long.parseLong(req.getHeader("Range").replaceAll("bytes=", "").replaceAll("-", ""));
            }

            if (pos != 0) {
                String contentRange = new StringBuffer("bytes ").append(Long.toString(pos)).append("-")
                        .append(Long.toString(fSize - 1)).append("/").append(Long.toString(fSize))
                        .toString();
                resp.setHeader("Content-Range", contentRange);
                is.skip(pos);
            }
            log.info("从pos:{}开始下载",pos);
            os = resp.getOutputStream();
            int n=0;
            while ((n = is.read(xx))!=-1) {
                os.write(xx,0, n);
            }
        } catch (IOException e) {
            log.info(e.getCause()+e.getMessage());
        } finally {
            if (is != null)
                is.close();
            if (os != null)
                os.close();
        }
    }
}
