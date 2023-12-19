package com.marcomarchionni.ibportfolio.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcomarchionni.ibportfolio.domain.Portfolio;
import com.marcomarchionni.ibportfolio.domain.User;
import com.marcomarchionni.ibportfolio.dtos.request.PortfolioCreateDto;
import com.marcomarchionni.ibportfolio.dtos.request.UpdateNameDto;
import com.marcomarchionni.ibportfolio.repositories.PortfolioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Sql("classpath:dbScripts/insertSampleData.sql")
class PortfolioControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    PortfolioRepository portfolioRepository;

    @Autowired
    ObjectMapper mapper;
    User user;

    @BeforeEach
    void setup() {
        // Setup authenticated user for testing
        user = User.builder().firstName("Marco").lastName("Marchionni").email("marco99@gmail.com").accountId("U1111111")
                .role(User.Role.USER).build(); // Initialize with necessary properties
        Authentication auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void findAllSuccess() throws Exception {
        int expectedSize = portfolioRepository.findAllByAccountId(user.getAccountId()).size();

        mockMvc.perform(get("/portfolios"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(expectedSize)));
    }

    @ParameterizedTest
    @CsvSource({"U1111111,Saver Portfolio,5", "U1111111,Trader Portfolio,2"})
    void findByIdSuccess(String accountId, String portfolioName, int expectedSize) throws Exception {
        Optional<Portfolio> portfolio = portfolioRepository.findByAccountIdAndName(accountId, portfolioName);
        assertTrue(portfolio.isPresent());
        Long portfolioId = portfolio.get().getId();

        mockMvc.perform(get("/portfolios/{id}", portfolioId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(Math.toIntExact(portfolioId))))
                .andExpect(jsonPath("$.strategies", hasSize(expectedSize)));
    }

    @Test
    void createPortfolioSuccess() throws Exception {
        PortfolioCreateDto portfolioCreateDto = PortfolioCreateDto.builder().name("Super Saver").build();

        mockMvc.perform(post("/portfolios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(portfolioCreateDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Super Saver")));
    }

    @ParameterizedTest
    @CsvSource({"U1111111, Super Portfolio", "U1111111, Marco's Portfolio", "U1111111, Zipp"})
    void updatePortfolioNameSuccess(String accountId, String portfolioName) throws Exception {
        Long portfolioId = portfolioRepository.findByAccountIdAndName(accountId, "Saver Portfolio").get().getId();
        UpdateNameDto updateName = UpdateNameDto.builder().id(portfolioId).name(portfolioName).build();

        mockMvc.perform(put("/portfolios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateName)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is(updateName.getName())));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Saver Portfolio", ","})
    void createPortfolioException(String portfolioName) throws Exception {
        UpdateNameDto badUpdateName = UpdateNameDto.builder().name(portfolioName).build();

        mockMvc.perform(post("/portfolios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(badUpdateName)))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void deleteByIdSuccess() throws Exception {
        Optional<Portfolio> portfolio = portfolioRepository.findByAccountIdAndName("U1111111", "Millionaire Portfolio");
        assertTrue(portfolio.isPresent());
        Long portfolioId = portfolio.get().getId();

        mockMvc.perform(delete("/portfolios/{id}", portfolioId))
                .andDo(print())
                .andExpect(status().isOk());

        assertTrue(portfolioRepository.findById(portfolioId).isEmpty());
    }

    @Test
    void deleteByIdUnableToDeleteEntitiesException() throws Exception {
        Optional<Portfolio> portfolio = portfolioRepository.findByAccountIdAndName("U1111111", "Saver Portfolio");
        assertTrue(portfolio.isPresent());
        Long portfolioId = portfolio.get().getId();

        mockMvc.perform(delete("/portfolios/{id}", portfolioId))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type", is("unable-to-delete-entities")));
    }
}