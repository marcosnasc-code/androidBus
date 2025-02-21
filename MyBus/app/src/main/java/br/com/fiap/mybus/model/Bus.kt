package br.com.fiap.mybus.model

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray

// Data class Bus (correta) para representar cada ônibus
data class Bus(val lineCode: String, val direction: String)

class OlhoVivoApi {


    // Função para obter os ônibus de uma parada específica
    companion object {
        private val client = OkHttpClient()
        private const val apiBaseUrl = "https://api.olhovivo.sptrans.com.br/v2.1"
        private const val token = "58cb9ad2bc4f9165760785e61b1309d1a42fd4e8a3ed0257a98f55e17820fcf7"

        private fun authenticate(): Boolean {
            val requestBody = "".toRequestBody("application/json".toMediaTypeOrNull())
            val request = Request.Builder()
                .url("$apiBaseUrl/Login/Autenticar?token=$token")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                return response.isSuccessful
            }
        }

        fun getBusesByStop(stopId: String): List<Bus> {
            // Autenticação com a API
            if (!authenticate()) {
                throw Exception("Autenticação falhou")
            }

            // URL para buscar ônibus por ID da parada
            val request = Request.Builder()
                .url("$apiBaseUrl/Parada/Buscar?termosBusca=$stopId")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw Exception("Falha na requisição da parada")
                }

                val responseBody = response.body?.string() ?: "[]"
                val jsonArray = JSONArray(responseBody)

                val buses = mutableListOf<Bus>()

                // Percorrendo o JSON retornado
                for (i in 0 until jsonArray.length()) {
                    val stopObject = jsonArray.getJSONObject(i)

                    // Lista de ônibus que passam pela parada
                    val onibusArray = stopObject.getJSONArray("vs")
                    for (j in 0 until onibusArray.length()) {
                        val busObject = onibusArray.getJSONObject(j)

                        val lineCode = busObject.getString("cp")  // Código da linha de ônibus
                        val direction = busObject.getString("ed")  // Endereço da parada

                        // Adicionando o ônibus à lista de resultados
                        buses.add(Bus(lineCode, direction))
                    }
                }

                return buses
            }
        }
    }
}
