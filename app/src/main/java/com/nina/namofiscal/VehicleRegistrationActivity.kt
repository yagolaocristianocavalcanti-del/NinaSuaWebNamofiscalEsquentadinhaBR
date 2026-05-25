package com.nina.namofiscal

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.nina.namofiscal.databinding.ActivityVehicleRegistrationBinding
import com.nina.namofiscal.model.Vehicle

class VehicleRegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVehicleRegistrationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVehicleRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSaveVehicle.setOnClickListener {
            val vehicle = Vehicle(
                plate = binding.etPlate.text.toString(),
                color = binding.etColor.text.toString(),
                brand = binding.etBrand.text.toString(),
                model = binding.etModel.text.toString(),
                prisma = binding.etPrisma.text.toString()
            )

            if (vehicle.plate.isBlank() || vehicle.prisma.isBlank()) {
                Toast.makeText(this, "Preencha Placa e Prisma!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveAndExit(vehicle)
        }
    }

    private fun saveAndExit(vehicle: Vehicle) {
        // Aqui integraria com o servidor central
        Toast.makeText(this, "Veículo ${vehicle.plate} Cadastrado!", Toast.LENGTH_SHORT).show()
        finish()
    }
}
