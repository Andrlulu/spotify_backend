package com.laioffer

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

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