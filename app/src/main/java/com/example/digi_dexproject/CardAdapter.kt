package com.example.digi_dexproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class CardAdapter(private val cards: List<Card>, private val onItemClick: (Card) -> Unit) : RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_item, parent, false)
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card = cards[position]
        holder.bind(card)
    }

    override fun getItemCount(): Int = cards.size

    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardImage: ImageView = itemView.findViewById(R.id.card_image)
        private val cardName: TextView = itemView.findViewById(R.id.card_name)
        private val cardType: TextView = itemView.findViewById(R.id.card_type)

        fun bind(card: Card) {
            cardName.text = card.name
            cardType.text = card.type
            Glide.with(itemView.context)
                .load(card.card_images.firstOrNull()?.imageUrlSmall)
                .into(cardImage)

            itemView.setOnClickListener {
                onItemClick(card)
            }
        }
    }
}