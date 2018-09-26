package id.slametriyadi.ojekbeneran.network;

import id.slametriyadi.ojekbeneran.model.google.ResponseGoogeDirections;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RestApi {
    @GET("json")
    Call<ResponseGoogeDirections> getRouteLocation(
            @Query("origin") String origin,
            @Query("destination") String destination
    );
}
