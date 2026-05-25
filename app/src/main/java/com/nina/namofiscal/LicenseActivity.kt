package com.nina.namofiscal

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.nina.namofiscal.databinding.ActivityLicenseBinding
import com.nina.namofiscal.model.UserRole

class LicenseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLicenseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLicenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val roles = UserRole.values()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles.map { it.name })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spRole.adapter = adapter

        binding.btnValidate.setOnClickListener {
            val key = binding.etLicenseKey.text.toString()
            val selectedRole = roles[binding.spRole.selectedItemPosition]

            if (validateLicense(key)) {
                SessionManager.setRole(this, selectedRole)
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, getString(R.string.invalid_license), Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnFallback.setOnClickListener {
            val selectedRole = roles[binding.spRole.selectedItemPosition]
            // Simulação de validação offline (Camada Secundária)
            if (validateOfflineCode(binding.etLicenseKey.text.toString())) {
                SessionManager.setRole(this, selectedRole)
                Toast.makeText(this, getString(R.string.contingency_activated), Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Código de contingência inválido!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateOfflineCode(code: String): Boolean {
        // Simulação de decodificação de hash (Mecanismo Fallback Offline Criptografado)
        return code == "OFFLINE-BYPASS-PRO-2024"
    }

    private fun validateLicense(key: String): Boolean {
        // Exemplo: XXXX-XXXX-XXXX-XXXX-XXXX-XXXX (24 chars + 5 dashes = 29)
        return key.replace("-", "").length == 24
    }
}
