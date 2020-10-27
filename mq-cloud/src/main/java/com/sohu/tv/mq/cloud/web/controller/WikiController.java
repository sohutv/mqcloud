package com.sohu.tv.mq.cloud.web.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import com.sohu.tv.mq.cloud.web.vo.UserInfo;
import com.sohu.tv.mq.util.Version;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.builder.Extension;
import com.vladsch.flexmark.util.options.MutableDataSet;

/**
 * wiki
 * 
 * @author yongfeigao
 * @date 2019年6月10日
 */
@Controller
@RequestMapping("/wiki")
public class WikiController {

    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;

    @RequestMapping("/{path}/{filename}")
    public String subPages(@PathVariable String path, @PathVariable String filename,
            UserInfo userInfo, Map<String, Object> map) throws Exception {
        // admin权限校验
        if ("adminGuide".equals(path) && !userInfo.getUser().isAdmin()) {
            Result.setResult(map, Result.getResult(Status.PERMISSION_DENIED_ERROR));
            return "wikiTemplate";
        }
        String html = markdown2html(path + "/" + filename, ".md");
        html = html.replace("${clientArtifactId}", mqCloudConfigHelper.getClientArtifactId());
        html = html.replace("${version}", Version.get());
        html = html.replace("${repositoryUrl}", mqCloudConfigHelper.getRepositoryUrl());
        html = html.replace("${producer}", mqCloudConfigHelper.getProducerClass());
        html = html.replace("${consumer}", mqCloudConfigHelper.getConsumerClass());
        html = html.replace("${mqcloudDomain}", mqCloudConfigHelper.getDomain());
        Result.setResult(map, html);
        
        // toc
        String toc = markdown2html(path + "/" + filename, ".toc.md");
        if(toc != null) {
            map.put("toc", toc);
        }
        return "wikiTemplate";
    }

    private String markdown2html(String filename, String suffix) throws Exception {
        String templatePath = "static/wiki/" + filename + suffix;
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(templatePath);
        if(inputStream == null) {
            return null;
        }
        String markdown = new String(read(inputStream));
        MutableDataSet options = new MutableDataSet();
        options.setFrom(ParserEmulationProfile.MARKDOWN);
        options.set(Parser.EXTENSIONS, Arrays.asList(new Extension[] { TablesExtension.create()}));
        Document document = Parser.builder(options).build().parse(markdown);
        String html = HtmlRenderer.builder(options).build().render(document);
        return html;
    }
    
    private byte[] read(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len = 0;
        ByteArrayOutputStream bos = null;
        try {
            bos = new ByteArrayOutputStream();
            while((len = inputStream.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
        } finally {
            if(bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {}
            }
            if(inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {}
            }
        }
        return bos.toByteArray();
    }

}
