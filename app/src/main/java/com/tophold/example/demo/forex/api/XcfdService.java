package com.tophold.example.demo.forex.api;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;
import com.tophold.example.demo.forex.model.XcfdQuotes;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 创建日期 ：2018/03/22 11:31
 * 描 述 ：
 * ============================================================
 **/
public interface XcfdService {
    @GET("/kline/{productCode}/{type}")
    Observable<List<XcfdQuotes>> chartQuotes(@Path("productCode") String productCode, @Path("type") String type, @QueryMap Map<String, Object> params);
}

