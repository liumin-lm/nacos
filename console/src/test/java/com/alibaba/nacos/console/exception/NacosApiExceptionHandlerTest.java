package com.alibaba.nacos.console.exception;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.runtime.NacosRuntimeException;
import com.alibaba.nacos.console.controller.v2.NamespaceControllerV2;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;


@RunWith(SpringRunner.class)
@WebMvcTest(NacosApiExceptionHandler.class)
public class NacosApiExceptionHandlerTest {
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private NamespaceControllerV2 namespaceControllerV2;


    @Before
    public void before() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    public void testNacosRunTimeExceptionHandler() throws Exception {
        // 设置InstanceControllerV2的行为，使其抛出NacosRuntimeException并被NacosApiExceptionHandler捕获处理
        when(namespaceControllerV2.createNamespace(any()))
                .thenThrow(new NacosRuntimeException(NacosException.INVALID_PARAM))
                .thenThrow(new NacosRuntimeException(NacosException.SERVER_ERROR))
                .thenThrow(new NacosRuntimeException(503));

        // 执行请求并验证响应码
        ResultActions resultActions  = mockMvc.perform(post("/v2/console/namespace"));
        resultActions.andExpect(MockMvcResultMatchers.status().is(NacosException.INVALID_PARAM));

        // 执行请求并验证响应码
        ResultActions resultActions1  = mockMvc.perform(post("/v2/console/namespace"));
        resultActions1.andExpect(MockMvcResultMatchers.status().is(NacosException.SERVER_ERROR));

        // 执行请求并验证响应码
        ResultActions resultActions2  = mockMvc.perform(post("/v2/console/namespace"));
        resultActions2.andExpect(MockMvcResultMatchers.status().is(503));
    }
}