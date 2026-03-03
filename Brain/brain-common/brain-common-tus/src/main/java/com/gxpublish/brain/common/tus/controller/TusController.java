package com.gxpublish.brain.common.tus.controller;

import com.gxpublish.brain.common.core.domain.R;
import com.gxpublish.brain.common.core.exception.ServiceException;
import com.gxpublish.brain.common.tus.domain.TusFinishUploadDTO;
import com.gxpublish.brain.common.tus.strategy.TusUploadStrategy;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.desair.tus.server.TusFileUploadService;
import me.desair.tus.server.upload.UploadInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
@RestController
@RequestMapping("${tus.upload.uri:/tus/upload}")
@RequiredArgsConstructor
public class TusController {

    private final TusFileUploadService tusFileUploadService;
    private final org.springframework.beans.factory.ObjectProvider<TusUploadStrategy> tusUploadStrategyProvider;

    @RequestMapping(value = {"", "/**"}, method = {RequestMethod.POST, RequestMethod.PATCH, RequestMethod.HEAD, RequestMethod.DELETE, RequestMethod.GET, RequestMethod.OPTIONS})
    public void upload(HttpServletRequest request, HttpServletResponse response) throws Exception {
        tusFileUploadService.process(request, response);
    }

    @PostMapping("/finish")
    public R<Object> finishUpload(@RequestBody TusFinishUploadDTO dto) throws Exception {
        if (dto == null || StringUtils.isBlank(dto.getUploadUrl()) || StringUtils.isBlank(dto.getOriginalName())) {
            return R.fail("Invalid parameters: uploadUrl and originalName are required");
        }
        String uploadUrl;
        try {
            uploadUrl = normalizeUploadUrl(dto.getUploadUrl());
        } catch (ServiceException e) {
            return R.fail(e.getMessage());
        }

        UploadInfo info;
        try {
            info = tusFileUploadService.getUploadInfo(uploadUrl);
        } catch (Exception e) {
            log.error("Failed to query TUS upload info, uploadUrl={}", uploadUrl, e);
            return R.fail("Failed to query upload session: " + extractRootMessage(e));
        }
        if (info == null) {
            return R.fail("Upload session not found: " + uploadUrl);
        }
        if (info.isUploadInProgress()) {
            return R.fail("Upload is still in progress, please retry after completion");
        }

        TusUploadStrategy strategy = tusUploadStrategyProvider.getIfAvailable();
        if (strategy == null) {
            log.warn("TusUploadStrategy is not implemented in the current system context. Return raw uploadUrl.");
            return R.ok(uploadUrl);
        }

        try (InputStream is = tusFileUploadService.getUploadedBytes(uploadUrl)) {
            Object result = strategy.finishUpload(uploadUrl, dto.getOriginalName(), dto.getContentType(), is);
            tusFileUploadService.deleteUpload(uploadUrl);
            return R.ok(result);
        } catch (ServiceException e) {
            log.error("Service error finalizing TUS upload, uploadUrl={}", uploadUrl, e);
            return R.fail(e.getMessage());
        } catch (Exception e) {
            log.error("System error finalizing TUS upload, uploadUrl={}", uploadUrl, e);
            return R.fail("System error during upload finalization: " + extractRootMessage(e));
        }
    }

    private String normalizeUploadUrl(String rawUploadUrl) {
        String uploadUrl = StringUtils.trimToEmpty(rawUploadUrl);
        if (StringUtils.isBlank(uploadUrl)) {
            throw new ServiceException("Invalid uploadUrl: empty value");
        }
        if (StringUtils.startsWithIgnoreCase(uploadUrl, "http://")
                || StringUtils.startsWithIgnoreCase(uploadUrl, "https://")) {
            try {
                uploadUrl = new URI(uploadUrl).getPath();
            } catch (URISyntaxException e) {
                throw new ServiceException("Invalid uploadUrl format");
            }
        }
        if (uploadUrl.contains("?")) {
            uploadUrl = StringUtils.substringBefore(uploadUrl, "?");
        }
        int idx = uploadUrl.indexOf("/tus/upload/");
        if (idx >= 0) {
            uploadUrl = uploadUrl.substring(idx);
        }
        if (!StringUtils.startsWith(uploadUrl, "/tus/upload/")) {
            throw new ServiceException("Invalid uploadUrl, expected '/tus/upload/{id}'");
        }
        return uploadUrl;
    }

    private String extractRootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return StringUtils.defaultIfBlank(current.getMessage(), current.getClass().getSimpleName());
    }
}
