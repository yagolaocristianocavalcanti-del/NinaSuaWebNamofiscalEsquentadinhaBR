package com.nina.namofiscal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

enum class MessageStatus { AGUARDANDO, ENTREGUE, LIDA }

data class ChatMessage(
    val text: String,
    val isFromNina: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    var status: MessageStatus = MessageStatus.AGUARDANDO
)

class NinaChatAdapter(private val messages: MutableList<ChatMessage>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_USER = 1
    private val VIEW_TYPE_NINA = 2

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isFromNina) VIEW_TYPE_NINA else VIEW_TYPE_USER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_USER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mensagem_user, parent, false)
            UserViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mensagem_nina, parent, false)
            NinaViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = messages[position]
        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(msg.timestamp))

        if (holder is UserViewHolder) {
            holder.tvText.text = msg.text
            holder.tvTime.text = time
            
            // Lógica dos Checks do Whats
            when (msg.status) {
                MessageStatus.AGUARDANDO -> {
                    holder.ivStatus.setImageResource(android.R.drawable.ic_menu_mylocation) // Um ícone circular simples
                    holder.ivStatus.alpha = 0.5f
                }
                MessageStatus.ENTREGUE -> {
                    holder.ivStatus.setImageResource(android.R.drawable.checkbox_on_background)
                    holder.ivStatus.alpha = 0.5f
                }
                MessageStatus.LIDA -> {
                    holder.ivStatus.setImageResource(android.R.drawable.checkbox_on_background)
                    holder.ivStatus.setColorFilter(android.graphics.Color.parseColor("#34B7F1"))
                    holder.ivStatus.alpha = 1.0f
                }
            }
        } else if (holder is NinaViewHolder) {
            holder.tvText.text = msg.text
            holder.tvTime.text = time
        }
    }

    override fun getItemCount() = messages.size

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvText: TextView = view.findViewById(R.id.tv_texto)
        val tvTime: TextView = view.findViewById(R.id.tv_hora)
        val ivStatus: ImageView = view.findViewById(R.id.iv_status)
    }

    class NinaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvText: TextView = view.findViewById(R.id.tv_texto)
        val tvTime: TextView = view.findViewById(R.id.tv_hora)
    }
}
