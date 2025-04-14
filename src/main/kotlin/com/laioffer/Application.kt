package com.laioffer

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

// data class to store data: Playlist and Song
// Serializable to let it generate deserializer, convert kotlin class into json file
@Serializable
data class Playlist (
    val id: Long,
    val songs: List<Song>
)

@Serializable
data class Song (
    val name: String,
    val lyric: String,
    val src: String,
    val length: String
)

// first class citizen, can declare directly in the file
fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}
// extension function module to application class
fun Application.module() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
        })
    }
    // TODO: adding the routing configuration here
    // 0.0.0.0:8080/feed
    // 0.0.0.0:8080/playlists
    // 0.0.0.0:8080/songs/solo.mp3
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        // use feed.json to create first fake end point
        get("feed") {
            // create val jsonString type String nullable,
            // load the resource feed.json from the resource folder, nullable, if not null readTet()
            val randomNumber = (1000..2000).random()
            delay(randomNumber.toLong())
            val jsonString: String? = this::class.java.classLoader.getResource("feed.json")?.readText()
            /* in java:
            if (jsonString != null) {
                call.respondText(jsonString)
            } else {
                call.respondText("null")
            }
            //call.respondText(jsonString)
            */

            // in kotlin:
            call.respondText(jsonString ?: "null")
        }

        //
        get("/playlists") {
            /*
            // ide mark it yellow, without ?: can cause error if the resource is null,
            val jsonString = this::class.java.classLoader.getResource("playlists.json").readText()
            call.respondText(jsonString) // call is a round trip carrier
             */

            // improved code:
            val jsonString: String? = this::class.java.classLoader.getResource("playlists.json")?.readText()
            call.respondText { jsonString ?: "null" }
        }

        // 1. Get the playlist id from the query parameter
        // 2. Deserialize playlists.json to list of playlist
        // 3. filter the playlist with id
        // 4. return the playlist in response
        // jsonString -> List<Playlist>
        // jsonString -> kotlin/java object: decode/deserialize
        // kotlin/java object -> jsonString: Serialize
        //val jsonString: String? = this::class.java.classLoader.getResource("playlists.json")?.readText()
        /* Java old way of writing it
        val jsonString = this::class.java.classLoader.getResource("playlists.json").readText()
        if (jsonString != "null") {
            val playlists: List<Song> = Json.decodeFromString(ListSerializer(Playlist.serializer()), jsonString)
            val id: String? = call.parameters["id"]
            val playlist: Playlist? = playlists.firstOrNull { item -> item.id.toString() == id }
        } else {
            call.respondText("null")
        }
        */
        get("playlist/{id}") { // 1. Get the playlist id from the query parameter
            this::class.java.classLoader.getResource("playlists.json")?.readText()?.let {
                // 2. Deserialize playlists.json to list of playlist
                val playlists = Json.decodeFromString(ListSerializer(Playlist.serializer()), it)
                // 3. filter the playlist with id
                val id = call.parameters["id"]
                val playlist: Playlist? = playlists.firstOrNull { item: Playlist -> item.id.toString() == id }
                // 4. return the playlist in response
                call.respondNullable(playlist)
            } ?: call.respondText("null")
        }

        // serve files, static plugin help simplifies.
        //Serve content from a resource folder by using following
        static("/") {
            staticBasePackage = "static"
            static("songs") {
                //allow files in the songs resource folder to be served as static content
                // under the given URL pattern,
                // eg: request to /songs/[song_name].mp3 ->serve: static/songs/[song_name].mp3
                // actual url: http://0.0.0.0:8080/songs/solo.mp3
                resources("songs")
            }
        }
    }
    // routing decompose:
    myRouting {
        myGet("/myHello") {
            println("hello world from myRouting")
        }
    }
}

// under the hood for routing
// myget is the func that being passed into the myRouting
fun myRouting(configuration: () -> Unit) {// Unit == void
    configuration()
}

fun myGet(path: String, body: () -> Unit) {
}