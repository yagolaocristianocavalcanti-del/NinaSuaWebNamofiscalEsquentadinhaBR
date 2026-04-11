package com.NINA.ninasuawebnamofiscalesquentadinhabr

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(private val mensagens: MutableList<Mensagem>) : 
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private val VIEW_TYPE_USER = 1
    private val VIEW_TYPE_NINA = 2

    override fun getItemViewType(position: Int): Int {
        return if (mensagens[position].remetente == "USER") VIEW_TYPE_USER else VIEW_TYPE_NINA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val layout = if (viewType == VIEW_TYPE_USER) 
            R.layout.item_mensagem_user else R.layout.item_mensagem_nina
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val mensagem = mensagens[position]
        holder.bind(mensagem)
    }

    override fun getItemCount() = mensagens.size

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(mensagem: Mensagem) {
            itemView.findViewById<TextView>(R.id.tv_texto).text = mensagem.texto
        }
    }
}
