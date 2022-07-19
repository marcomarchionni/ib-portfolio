package com.marcomarchionni.ibportfolio.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcomarchionni.ibportfolio.models.Trade;
import com.marcomarchionni.ibportfolio.rest.exceptionhandling.exceptions.EntityNotFoundException;
import com.marcomarchionni.ibportfolio.services.TradeService;
import com.marcomarchionni.ibportfolio.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TradeControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    @MockBean
    TradeService tradeService;

    @Autowired
    TradeController tradeController;

    @Test
    void getTrades() throws Exception {

        List<Trade> trades = TestUtils.getSampleTrades();

        when(tradeService.findWithParameters(any(), any(), any(), any(), any())).thenReturn(trades);

        mockMvc.perform(get("/trades"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(trades.size())));
    }

    @ParameterizedTest
    @CsvSource({",,,ZM,",",,,TTWO,",",2022-06-14,true,,"})
    void getTradesWithParameters(String startDate, String endDate, String tagged, String symbol, String assetCategory) throws Exception {

        List<Trade> resultList = TestUtils.getSampleTrades();

        when(tradeService.findWithParameters(any(),any(),any(),any(),any())).thenReturn(resultList);


        mockMvc.perform(get("/trades")
                        .param("startDate", startDate)
                        .param("endDate", endDate)
                        .param("tagged", tagged)
                        .param("symbol", symbol)
                        .param("assetCategory", assetCategory))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(resultList.size())));
    }

    @ParameterizedTest
    @CsvSource({"pippo,,,,",",,farse,ZM,"})
    void getTradesWithParametersBadRequest(String startDate, String endDate, String tagged, String symbol, String assetCategory) throws Exception {

        mockMvc.perform(get("/trades")
                        .param("symbol", symbol)
                        .param("startDate", startDate)
                        .param("endDate", endDate)
                        .param("tagged", tagged)
                        .param("assetCategory", assetCategory))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    void updateStrategyIdTest() throws Exception {

        Trade trade = TestUtils.getSampleTrade();

        when(tradeService.updateStrategyId(trade)).thenReturn(trade);

        mockMvc.perform(put("/trades")
                .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(trade)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.symbol", is(trade.getSymbol())));
    }

    @Test
    void updateStrategyIdEntityNotFoundTest() throws Exception {

        Trade trade = TestUtils.getSampleTrade();

        when(tradeService.updateStrategyId(trade)).thenThrow(new EntityNotFoundException("Trade or Strategy not found"));

        mockMvc.perform(put("/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(trade)))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Trade or Strategy not found")));
    }
}
