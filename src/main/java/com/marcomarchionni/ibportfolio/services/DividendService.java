package com.marcomarchionni.ibportfolio.services;

import com.marcomarchionni.ibportfolio.domain.Dividend;
import com.marcomarchionni.ibportfolio.dtos.request.DividendFindDto;
import com.marcomarchionni.ibportfolio.dtos.request.UpdateStrategyDto;
import com.marcomarchionni.ibportfolio.dtos.response.DividendSummaryDto;
import com.marcomarchionni.ibportfolio.dtos.update.UpdateReport;

import java.util.List;

public interface DividendService {
    DividendSummaryDto updateStrategyId(UpdateStrategyDto dividendToUpdate);

    List<DividendSummaryDto> findByFilter(DividendFindDto dividendCriteria);

    UpdateReport<Dividend> updateDividends(List<Dividend> dividends);
}
