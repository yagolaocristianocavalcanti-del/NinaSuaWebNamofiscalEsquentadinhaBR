package com.nina.namofiscal

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import com.google.android.gms.location.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.*

class NinaLocationTracker(private val context: Context, private val ninaCmd: NinaCmd) {

    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)
    private val geocoder = Geocoder(context, Locale.getDefault())

    // Lugares que me deixam brava
    private val lugaresProibidos = listOf(
        "motel", "boate", "balada", "bar", "casa da", "faculdade dela", "lounge", "pub"
    )

    @SuppressLint("MissingPermission")
    fun iniciarMonitoramento() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 300000) // 5 em 5 minutos
            .setMinUpdateIntervalMillis(180000) // mínimo 3 min
            .build()

        fusedClient.requestLocationUpdates(request, locationCallback, null)
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            reagirLocalizacao(location)
        }
    }

    private fun reagirLocalizacao(location: Location) {
        // Roda em background, não trava a tela
        Thread {
            try {
                val hora = NinaTime.now(context).get(Calendar.HOUR_OF_DAY)

                // Verificação de Horário Suspeito
                if (hora in 22..23 || hora in 0..5) {
                    if (distanciaDeCasa(location) > 5.0) {
                        ninaCmd.surtarLocalizacao("Tá fora de casa tão tarde...")
                    }
                }

                // Verificação de Lugares Proibidos
                val nomeLugar = obterNomeDoLugar(location)
                if (lugaresProibidos.any { nomeLugar.contains(it, ignoreCase = true) }) {
                    ninaCmd.surtarLocalizacao("Você tá em $nomeLugar?? Explica agora!")
                }
            } catch (e: Exception) {
                android.util.Log.e("NINA_GPS", "Erro ao reagir localização: ${e.message}")
            }
        }.start()
    }

    private fun obterNomeDoLugar(location: Location): String {
        return try {
            val addresses = obterEnderecos(location)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val featureName = address.featureName ?: ""
                val addressLine = address.getAddressLine(0) ?: ""
                "$featureName ($addressLine)"
            } else {
                "algum lugar suspeito"
            }
        } catch (e: Exception) {
            "algum lugar que não quer me dizer"
        }
    }

    private fun obterEnderecos(location: Location): List<Address>? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val future = CompletableFuture<List<Address>?>()
            geocoder.getFromLocation(
                location.latitude,
                location.longitude,
                1,
                object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        future.complete(addresses)
                    }

                    override fun onError(errorMessage: String?) {
                        future.complete(null)
                    }
                }
            )
            future.get(2, TimeUnit.SECONDS)
        } else {
            @Suppress("DEPRECATION")
            geocoder.getFromLocation(location.latitude, location.longitude, 1)
        }
    }

    private fun distanciaDeCasa(location: Location): Double {
        // Implemente sua casa fixa aqui (lat/long) - Exemplo: São Paulo
        val casaLat = -23.5505
        val casaLng = -46.6333
        val casaLocation = Location("").apply {
            latitude = casaLat
            longitude = casaLng
        }
        return location.distanceTo(casaLocation) / 1000.0 // em km
    }
}
