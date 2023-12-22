package com.marcomarchionni.ibportfolio.services;

import com.marcomarchionni.ibportfolio.domain.User;
import com.marcomarchionni.ibportfolio.dtos.flex.FlexQueryResponseDto;
import com.marcomarchionni.ibportfolio.dtos.update.CombinedUpdateReport;
import com.marcomarchionni.ibportfolio.services.fetchers.DataFetcher;
import com.marcomarchionni.ibportfolio.services.fetchers.FetchContext;
import com.marcomarchionni.ibportfolio.services.fetchers.util.DataFetcherResolver;
import com.marcomarchionni.ibportfolio.services.fetchers.util.DataSourceType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class UpdateOrchestratorImpl implements UpdateOrchestrator {

    private final DataFetcherResolver dataFetcherResolver;
    private final UpdateService updateService;

    public UpdateOrchestratorImpl(DataFetcherResolver dataFetcherResolver, UpdateService updateService) {
        this.dataFetcherResolver = dataFetcherResolver;
        this.updateService = updateService;
    }

    @Override
    public CombinedUpdateReport updateFromServer(User user) throws IOException {
        DataFetcher dataFetcher = dataFetcherResolver.resolve(DataSourceType.SERVER);
        FlexQueryResponseDto dto = dataFetcher.fetch(new FetchContext());
        return updateService.save(user, dto);
    }

    @Override
    public CombinedUpdateReport updateFromFile(User user, MultipartFile file) throws IOException {
        DataFetcher dataFetcher = dataFetcherResolver.resolve(DataSourceType.FILE);
        FetchContext context = new FetchContext();
        context.setFile(file);
        FlexQueryResponseDto dto = dataFetcher.fetch(context);
        return updateService.save(user, dto);
    }
}
