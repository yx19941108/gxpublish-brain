package com.gxpublish.brain.system.service.impl;

import com.gxpublish.brain.common.oss.core.OssClient;
import com.gxpublish.brain.common.oss.enums.AccessPolicyType;
import com.gxpublish.brain.system.domain.vo.SysOssVo;
import com.gxpublish.brain.system.mapper.SysOssMapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("dev")
@ExtendWith(MockitoExtension.class)
class SysOssServiceImplTest {

    @Mock
    private SysOssMapper baseMapper;
    @Mock
    private OssClient ossClient;

    @Test
    void shouldReturnPresignedUrlForSelfHostedS3EndpointEvenWhenConfiguredPublic() {
        SysOssServiceImpl service = new SysOssServiceImpl(baseMapper);
        SysOssVo oss = buildOssVo();

        when(ossClient.getAccessPolicy()).thenReturn(AccessPolicyType.PUBLIC);
        when(ossClient.getEndpoint()).thenReturn("http://127.0.0.1:9000");
        when(ossClient.createPresignedGetUrl(eq(oss.getFileName()), any()))
            .thenReturn("http://127.0.0.1:9000/brain/2026/03/20/file.txt?X-Amz-Signature=preview");

        String resolvedUrl = service.resolveAccessibleUrl(oss, ossClient);

        assertEquals("http://127.0.0.1:9000/brain/2026/03/20/file.txt?X-Amz-Signature=preview", resolvedUrl);
        assertTrue(service.shouldUsePresignedUrl(ossClient));
        verify(ossClient).createPresignedGetUrl(eq(oss.getFileName()), any());
    }

    @Test
    void shouldKeepDirectUrlForCloudPublicBucket() {
        SysOssServiceImpl service = new SysOssServiceImpl(baseMapper);
        SysOssVo oss = buildOssVo();

        when(ossClient.getAccessPolicy()).thenReturn(AccessPolicyType.PUBLIC);
        when(ossClient.getEndpoint()).thenReturn("https://oss-cn-beijing.aliyuncs.com");

        String resolvedUrl = service.resolveAccessibleUrl(oss, ossClient);

        assertEquals(oss.getUrl(), resolvedUrl);
        assertFalse(service.shouldUsePresignedUrl(ossClient));
        verify(ossClient, never()).createPresignedGetUrl(eq(oss.getFileName()), any());
    }

    @Test
    void shouldReturnPresignedUrlForPrivateBucket() {
        SysOssServiceImpl service = new SysOssServiceImpl(baseMapper);
        SysOssVo oss = buildOssVo();

        when(ossClient.getAccessPolicy()).thenReturn(AccessPolicyType.PRIVATE);
        when(ossClient.createPresignedGetUrl(eq(oss.getFileName()), any()))
            .thenReturn("https://oss-cn-beijing.aliyuncs.com/brain/2026/03/20/file.txt?X-Amz-Signature=private");

        String resolvedUrl = service.resolveAccessibleUrl(oss, ossClient);

        assertEquals("https://oss-cn-beijing.aliyuncs.com/brain/2026/03/20/file.txt?X-Amz-Signature=private", resolvedUrl);
        assertTrue(service.shouldUsePresignedUrl(ossClient));
        verify(ossClient).createPresignedGetUrl(eq(oss.getFileName()), any());
    }

    private SysOssVo buildOssVo() {
        SysOssVo oss = new SysOssVo();
        oss.setFileName("2026/03/20/file.txt");
        oss.setUrl("http://127.0.0.1:9000/brain/2026/03/20/file.txt");
        return oss;
    }
}
