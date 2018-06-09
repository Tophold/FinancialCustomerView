package com.tophold.example.demo.btc.api;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;
import com.tophold.example.demo.btc.model.HuobiData;
import com.tophold.example.demo.btc.model.HuobiSymbol;
import com.tophold.example.demo.btc.model.HuobiQuote;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 创建日期 ：2018/03/22 11:31
 * 描 述 ：
 * ============================================================
 **/
public interface HuobiService {
    @GET("/market/history/kline")
    Observable<HuobiData<List<HuobiQuote>>> chartQuotes(@QueryMap Map<String, Object> params);

    @GET("/v1/common/symbols")
    Observable<HuobiData<List<HuobiSymbol>>> getSymbolList(@QueryMap Map<String, Object> params);
}

