package intrum.payoutingestion.controller;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import intrum.payoutingestion.services.PayoutProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PayoutController.class)
class PayoutControllerTest {

    @MockitoBean
    PayoutProcessor payoutProcessor;   // подменяем сервис

    @Autowired
    MockMvc mvc;

    @Test
    void triggerProcessing_valid() throws Exception {
        mvc.perform(post("/api/payout/process"))
                .andExpect(status().isOk())
                .andExpect(content().string("Payout processing triggered successfully"));

        verify(payoutProcessor).process();
    }
}