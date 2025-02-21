package br.com.fiap.mybus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.fiap.mybus.model.Bus
import br.com.fiap.mybus.model.OlhoVivoApi.Companion.getBusesByStop
import br.com.fiap.mybus.ui.theme.MyBusTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyBusTheme {
                BusSearchScreen()
            }
        }
    }
}

@Composable
fun BusSearchScreen() {
    var stopId by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("Resultado aparecerá aqui...") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = stopId,
            onValueChange = { stopId = it },
            label = { Text("Digite o CÓDIGO da parada") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (stopId.isNotEmpty()) {
                    isLoading = true
                    result = "Buscando ônibus..."
                    searchBusForStop(stopId) { buses ->
                        isLoading = false
                        result = if (buses.isNotEmpty()) {
                            buses.joinToString("\n") { "Código da Parada: ${it.lineCode} - Endereço da Parada: ${it.direction}" }
                        } else {
                            "Nenhum ônibus encontrado para essa parada."
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text("Buscar ônibus")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = result, modifier = Modifier.fillMaxWidth())
    }
}

private fun searchBusForStop(stopId: String, onResult: (List<Bus>) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            // Chama o método getBusesByStop() do companion object de OlhoVivoApi
            val busList = getBusesByStop(stopId)
            withContext(Dispatchers.Main) {
                onResult(busList)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onResult(emptyList())  // Retorna lista vazia em caso de erro
            }
        }
    }
}
