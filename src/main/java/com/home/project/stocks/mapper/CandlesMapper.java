package com.home.project.stocks.mapper;

import com.home.project.stocks.model.candles.Candle;
import com.home.project.stocks.model.entity.DailyCandle;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 *
 * @author rlagay
 */
@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.FIELD)
public interface CandlesMapper {

    @Mapping(target = "c", source = "close")
    @Mapping(target = "o", source = "open")
    @Mapping(target = "l", source = "low")
    @Mapping(target = "h", source = "high")
    @Mapping(target = "v", source = "volume")
    @Mapping(target = "datetime", source = "time")
    @Mapping(target = "figi", ignore = true)
    Candle toRestCandle(DailyCandle candle);

    @Mapping(target = "close", source = "candle.c")
    @Mapping(target = "open", source = "candle.o")
    @Mapping(target = "low", source = "candle.l")
    @Mapping(target = "high", source = "candle.h")
    @Mapping(target = "volume", source = "candle.v")
    @Mapping(target = "time", source = "candle.datetime")
    @Mapping(target = "id", ignore = true)
    //todo check interval
    DailyCandle toDbCandle(Candle candle, String ticker);
}
