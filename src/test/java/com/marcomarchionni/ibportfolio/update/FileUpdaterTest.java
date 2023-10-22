package com.marcomarchionni.ibportfolio.update;

import com.marcomarchionni.ibportfolio.model.dtos.flex.FlexQueryResponseDto;
import com.marcomarchionni.ibportfolio.services.UpdateServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FileUpdaterTest {

    @Mock
    UpdateServiceImpl updateService;

    FileUpdater fileUpdater;

    @Captor
    ArgumentCaptor<FlexQueryResponseDto> captor;

    @BeforeEach
    void setup() {
        fileUpdater = new FileUpdater(updateService);
    }

    @ParameterizedTest
    @CsvSource({"flex/SimpleJune2022.xml, 10, 8, 14, 3, 2022-06-01", "flex/TinyFlex3.xml, 31, 0, 0, 0, 2023-09-01"})
    void xmlToDtoSuccess(String fileName, int expectedTradesSize, int expectedPositionsSize,
                         int expectedClosedDividendsSize, int expectedOpenDividendsSize, String dateString) throws IOException {

        File flexQueryXml = loadFile(fileName);
        LocalDate expectedFromDate = LocalDate.parse(dateString);

        fileUpdater.update(flexQueryXml);

        verify(updateService).save(captor.capture());
        FlexQueryResponseDto response = captor.getValue();

        assertNotNull(response);
        FlexQueryResponseDto.FlexStatement flexStatement = response.getFlexStatements().getFlexStatement();
        LocalDate actualFromDate = flexStatement.getFromDate();
        assertNotNull(actualFromDate);
        assertEquals(expectedFromDate, actualFromDate);
        int actualTradesSize = Optional.ofNullable(flexStatement.getTrades())
                .map(FlexQueryResponseDto.Trades::getTradeList).map(List::size).orElse(0);
        int actualPositionsSize = Optional.ofNullable(flexStatement.getOpenPositions())
                .map(FlexQueryResponseDto.OpenPositions::getOpenPositionList).map(List::size).orElse(0);
        int actualClosedDividendsSize = Optional.ofNullable(flexStatement.getChangeInDividendAccruals())
                .map(FlexQueryResponseDto.ChangeInDividendAccruals::getChangeInDividendAccrualList).map(List::size)
                .orElse(0);
        int actualOpenDividendsSize = Optional.ofNullable(flexStatement.getOpenDividendAccruals())
                .map(FlexQueryResponseDto.OpenDividendAccruals::getOpenDividendAccrualList).map(List::size).orElse(0);
        assertEquals(expectedTradesSize, actualTradesSize);
        assertEquals(expectedPositionsSize, actualPositionsSize);
        assertEquals(expectedClosedDividendsSize, actualClosedDividendsSize);
        assertEquals(expectedOpenDividendsSize, actualOpenDividendsSize);
    }

    @Test
    @Disabled
        //TODO: xml validation
    void invalidXml() {
        File invalidFlexQueryXml = loadFile("flex/InvalidJune2022.xml");

        assertThrows(Exception.class, () -> fileUpdater.update(invalidFlexQueryXml));
    }

    private File loadFile(String filePath) {
        return new File(Objects.requireNonNull(getClass().getClassLoader().getResource(filePath)).getFile());
    }
}