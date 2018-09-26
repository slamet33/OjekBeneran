package id.slametriyadi.ojekbeneran.network;

import id.slametriyadi.ojekbeneran.helper.MyContants;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class InitRetrofit {

    public static Retrofit setInitGoogle() {
        return new Retrofit.Builder()
                .baseUrl(MyContants.BASE_MAP_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static RestApi getInstanceGoogle() {
        return setInitGoogle().create(RestApi.class);
    }
}
