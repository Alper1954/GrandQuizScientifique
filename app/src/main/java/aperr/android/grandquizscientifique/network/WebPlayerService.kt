package aperr.android.grandquizscientifique.network


import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private const val BASE_URL = "http://aperrault.atspace.cc"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(ScalarsConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

interface WebPlayerService {
    @GET("create_player.php")
    suspend fun createPlayer(
        @Query("pseudo")pseudo:String
    ):String

    @GET("read_score.php")
    suspend fun initScore(@Query("pseudo")pseudo:String, @Query("cat")cat:Int):String

    @GET("read_flags.php")
    suspend fun initFlags(@Query("pseudo")pseudo:String):String

    @GET("update_score.php")
    suspend fun updateScore(
        @Query("pseudo")pseudo:String,@Query("cat")cat:Int,
        @Query("score1")score1:Int,@Query("score2")score2:Int,@Query("score3")score3:Int,
        @Query("score4")score4:Int,@Query("score5")score5:Int,
        @Query("score1_qual")scoreQual1:Int,@Query("score2_qual")scoreQual2:Int,
        @Query("score3_qual")scoreQual3:Int,@Query("score4_qual")scoreQual4:Int,
        @Query("score5_qual")scoreQual5:Int,
    ):String
}

object WebPlayerApi {
    val retrofitService : WebPlayerService by lazy {
        retrofit.create(WebPlayerService::class.java) }
}