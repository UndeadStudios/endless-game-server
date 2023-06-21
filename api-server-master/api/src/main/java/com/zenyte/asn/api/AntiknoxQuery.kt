package com.zenyte.asn.api

import com.google.gson.JsonParser
import com.zenyte.asn.IPHeuristics.Companion.http
import okhttp3.HttpUrl
import okhttp3.Request

/**
 *
 * @author Noele
 * see https://noeles.life || noele@zenyte.com
 */
class AntiknoxQuery {

    companion object {
        val auth = "3b3ded7c82983e4fb66b1628434573dd83055c4f80acafd2fc088d3b3c2cabdb"
        var parser = JsonParser()
    }


    fun execute(ip: String): AntiknoxAPIResult {
        checkNotNull(http) {
            "[ANTIKNOX] The HTTP client cannot be null!"
        }

        checkNotNull(ip) {
            "[ANTIKNOX] Your ip address cannot be empty!"
        }

        val request = Request.Builder()
            .url(HttpUrl.Builder()
                    .scheme("http")
                    .host("api.antiknox.net")
                    .addPathSegment("lookup")
                    .addPathSegment(ip)
                    .addQueryParameter("auth", auth)
                    .build())
                .header("User-Agent", "Zenyte API")
            .build()

        try {
            val response = http.newCall(request).execute()
            val body = response.body

            checkNotNull(body) {
                "[ANTIKNOX] The body of the response was null!"
            }
    
            val responseCode = response.code / 100
            if (responseCode == 2) { // http 200
                val json = body.string()

                if (json.length < 1) {
                    System.out.println("[ANTIKNOX] The json response was empty!")
                    return AntiknoxAPIResult(false, "")
                }

                val element = parser.parse(json).asJsonObject

                val direct = element.get("direct").toString()
                val heuristics = element.get("heuristics").toString()

                if(direct != "null" && direct != null) {
                    val directJson = parser.parse(direct).asJsonObject
                    val type = directJson.get("type").asString
                    return AntiknoxAPIResult(!type.equals("tor") && !type.equals("proxy"), json)
                }

                if(heuristics != "null" && heuristics != null) {
                    val heuristicsJson = parser.parse(heuristics).asJsonObject
                    val label = heuristicsJson.get("label").asString
                    return AntiknoxAPIResult(!label.equals("hosting"), json)
                }

                if(heuristics == null && direct == null) {
                    return AntiknoxAPIResult(false, json)
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            System.out.println("[ANTIKNOX] An exception has been caught and the query failed!")
            return AntiknoxAPIResult(false,  "")
        }

        return AntiknoxAPIResult(false,  "")
    }

    data class AntiknoxAPIResult(val legit: Boolean, val digest: String)

}