package com.marcomarchionni.ibportfolio.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcomarchionni.ibportfolio.models.dtos.StrategyCreateDto;
import com.marcomarchionni.ibportfolio.models.dtos.StrategyFindDto;
import com.marcomarchionni.ibportfolio.models.dtos.UpdateNameDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Sql("/initIbTestDb.sql")
@Sql("/insertSampleData.sql")
class StrategyControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    @ParameterizedTest
    @CsvSource({"ZM long,1",",6"})
    void findByParamsSuccess(String strategyName, int expectedSize) throws Exception {
        StrategyFindDto strategyFindDto = StrategyFindDto.builder().name(strategyName).build();

        mockMvc.perform(get("/strategies")
                        .param("name", strategyFindDto.getName()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(expectedSize)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"   ",""})
    void findByParamsException(String strategyName) throws Exception {
        StrategyFindDto strategyFindDto = StrategyFindDto.builder().name(strategyName).build();

        mockMvc.perform(get("/strategies")
                        .param("name", strategyFindDto.getName()))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void createSuccess() throws Exception {
        StrategyCreateDto strategyCreateDto = StrategyCreateDto.builder().name("AAPL long").portfolioId(1L).build();

        mockMvc.perform(post("/strategies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(strategyCreateDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    @Test
    void updateNameSuccess() throws Exception {

        UpdateNameDto updateNameDto = UpdateNameDto.builder().id(1L).name("NewName").build();

        mockMvc.perform(put("/strategies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(updateNameDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(Math.toIntExact(updateNameDto.getId()))))
                .andExpect(jsonPath("$.name", is(updateNameDto.getName())));
    }

    @Test
    void updateNameException() throws Exception {

        UpdateNameDto updateNameDto = UpdateNameDto.builder().id(1L).name("12NewName").build();

        mockMvc.perform(put("/strategies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateNameDto)))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void deleteSuccess() throws Exception {
        Long id = 6L;
        mockMvc.perform(delete("/strategies/{id}", id))
                .andExpect(status().isOk());
    }

    @Test
    void deleteException() throws Exception {
        Long id = 1L;
        mockMvc.perform(delete("/strategies/{id}", id))
                .andExpect(status().is4xxClientError());
    }
}