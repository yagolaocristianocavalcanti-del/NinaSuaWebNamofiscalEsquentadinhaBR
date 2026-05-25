package com.nina.namofiscal

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.nina.namofiscal.databinding.ActivityMainBinding
import com.nina.namofiscal.model.UserRole

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var currentRole: UserRole

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentRole = SessionManager.getRole(this)
        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        binding.tvRoleStatus.text = currentRole.name

        // Regra de negócio: Se for Caixa no Celular, desabilita financeiro
        if (currentRole == UserRole.CAIXA && SessionManager.isMobile(this)) {
            Toast.makeText(this, "Interface Financeira Desabilitada (Modo Pista Ativo)", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupListeners() {
        binding.btnGarage.setOnClickListener {
            // Alterna papel ou abre pátio
            Toast.makeText(this, getString(R.string.opening_patio), Toast.LENGTH_SHORT).show()
        }

        binding.btnRegisterEntry.setOnClickListener {
            startActivity(android.content.Intent(this, VehicleRegistrationActivity::class.java))
        }

        binding.btnValidateExit.setOnClickListener {
            Toast.makeText(this, "Abrindo Scanner de Saída...", Toast.LENGTH_SHORT).show()
        }

        binding.btnEmergency.setOnClickListener {
            showEmergencyDialog()
        }

        binding.btnPtt.setOnClickListener {
            togglePTT()
        }
    }

    private fun showEmergencyDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_emergency, null)
        val dialog = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val btnSend = dialogView.findViewById<android.widget.Button>(R.id.btn_send_alert)
        val etJustification = dialogView.findViewById<android.widget.EditText>(R.id.et_justification)

        btnSend.setOnClickListener {
            val justification = etJustification.text.toString()
            if (justification.isNotBlank()) {
                Toast.makeText(this, getString(R.string.emergency_alert_sent), Toast.LENGTH_LONG).show()
                dialog.dismiss()
            } else {
                Toast.makeText(this, getString(R.string.justification_required), Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private var isPTTActive = false
    private fun togglePTT() {
        // Regra de negócio: Se o usuário estiver dirigindo (em manobra), o PTT é bloqueado.
        // Simulamos verificação de movimento:
        val isMoving = false // Mocked

        if (isMoving) {
            Toast.makeText(this, getString(R.string.ptt_blocked), Toast.LENGTH_SHORT).show()
            return
        }

        isPTTActive = !isPTTActive
        binding.btnPtt.backgroundTintList = android.content.res.ColorStateList.valueOf(
            if (isPTTActive) getColor(R.color.neon_green) else getColor(R.color.dark_gray)
        )
        binding.btnPtt.setTextColor(
            if (isPTTActive) getColor(R.color.carbon_black) else getColor(R.color.neon_green)
        )

        val msgResId = if (isPTTActive) R.string.ptt_open else R.string.ptt_standby
        Toast.makeText(this, getString(msgResId), Toast.LENGTH_SHORT).show()
    }
}
