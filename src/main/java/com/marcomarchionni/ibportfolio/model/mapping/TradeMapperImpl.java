package com.marcomarchionni.ibportfolio.model.mapping;

import com.marcomarchionni.ibportfolio.model.domain.Trade;
import com.marcomarchionni.ibportfolio.model.dtos.flex.FlexQueryResponseDto;
import com.marcomarchionni.ibportfolio.model.dtos.response.TradeListDto;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class TradeMapperImpl implements TradeMapper {

    ModelMapper modelMapper;

    public TradeMapperImpl(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public TradeListDto toTradeListDto(Trade trade) {
        return modelMapper.map(trade, TradeListDto.class);
    }

    @Override
    public Trade toTrade(FlexQueryResponseDto.Trade tradeDto) {
        return modelMapper.map(tradeDto, Trade.class);
    }

    @Override
    public Trade toTrade(FlexQueryResponseDto.Order orderDto) {
        return modelMapper.map(orderDto, Trade.class);
    }
}
