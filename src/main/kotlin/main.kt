import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dto.AuthorDto
import dto.CommentDto
import dto.PostDto
import dto.dto
import entity.PostWithComments
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.lang.Exception
import java.lang.RuntimeException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private val gson = Gson()
private const val BASE_URL = "http://127.0.0.1:9999"
private val client = OkHttpClient.Builder()
    .addInterceptor(HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    })
    .connectTimeout(30, TimeUnit.SECONDS)
    .build()

fun main() {
    with(CoroutineScope(EmptyCoroutineContext)) {
        launch {
            try {
                val posts = getPosts(client)
                    .map { post ->
                        async {
                            val author = getAuthor(client, post.authorId)
                            val post = post.dto(
                                author.dto()
                            )

                            val comments = getComments(client, post.id)
                            comments.map { comment ->
                                async {
                                    comment.dto(
                                        getAuthor(client, comment.authorId).dto()
                                    )
                                }
                            }.awaitAll().run {
                                PostWithComments(post, this)
                            }
                        }
                    }.awaitAll()
                println(posts)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    Thread.sleep(10_000L)
}

suspend fun OkHttpClient.callToApi(url: String) : Response {
    return suspendCoroutine { continuation ->
        Request.Builder()
            .url(url)
            .build()
            .let(::newCall)
            .enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    continuation.resume(response)
                }

                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWithException(e)
                }
            })
    }
}

suspend fun <T> makeRequest(url: String, client: OkHttpClient, typeToken: TypeToken<T>) : T =
    withContext(Dispatchers.IO) {
        client.callToApi(url)
            .let { response ->
                if (!response.isSuccessful) {
                    response.close()
                    throw RuntimeException(response.message)
                }
                val body = response.body ?: throw RuntimeException("response body is null")
                gson.fromJson(body.string(), typeToken.type)
            }
    }

suspend fun getPosts(client: OkHttpClient): List<PostDto> =
    makeRequest("$BASE_URL/api/posts", client, object : TypeToken<List<PostDto>>() {})

suspend fun getComments(client: OkHttpClient, id: Long): List<CommentDto> =
    makeRequest("$BASE_URL/api/posts/${id}/comments", client, object : TypeToken<List<CommentDto>>() {})

suspend fun getAuthor(client: OkHttpClient, id: Long): AuthorDto =
    makeRequest("${BASE_URL}/api/authors/${id}", client, object : TypeToken<AuthorDto>() {})