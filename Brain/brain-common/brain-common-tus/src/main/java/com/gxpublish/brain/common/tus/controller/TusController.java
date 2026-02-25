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
        if (StringUtils.isBlank(dto.getUploadUrl()) || StringUtils.isBlank(dto.getOriginalName())) {
            return R.fail("Invalid parameters: uploadUrl and originalName are required");
        }

        String uploadUrl = dto.getUploadUrl();
        // Since frontend might send FQDN, truncate up to endpoint logic
        if (uploadUrl.contains("/tus/upload/")) {
            uploadUrl = uploadUrl.substring(uploadUrl.indexOf("/tus/upload/"));
        }

        UploadInfo info = tusFileUploadService.getUploadInfo(uploadUrl);
        if (info == null || info.isUploadInProgress()) {
            return R.fail("Upload not completed or not found");
        }

        TusUploadStrategy strategy = tusUploadStrategyProvider.getIfAvailable();
        if (strategy == null) {
            log.warn("TusUploadStrategy is not implemented in the current system context. Return raw uploadUrl.");
            return R.ok(uploadUrl);
        }

        try (InputStream is = tusFileUploadService.getUploadedBytes(uploadUrl)) {
            Object result = strategy.finishUpload(uploadUrl, dto.getOriginalName(), dto.getContentType(), is);
            // Cleanup TUS temp files after passing to persistence strategy
            tusFileUploadService.deleteUpload(uploadUrl);
            return R.ok(result);
        } catch (ServiceException e) {
            log.error("Service error finalizing TUS upload", e);
            return R.fail(e.getMessage());
        } catch (Exception e) {
            log.error("System error finalizing TUS upload", e);
            return R.fail("System error during upload finalization");
        }
    }
}
