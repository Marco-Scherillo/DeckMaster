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
        private val cardPrices: TextView = itemView.findViewById(R.id.card_prices)


        fun bind(card: Card) {
            cardName.text = card.name
            cardType.text = card.type
            Glide.with(itemView.context)
                .load(card.card_images.firstOrNull()?.imageUrlSmall)
                .into(cardImage)

            // 1. Get the first price object from the list.
            val priceInfo = card.card_prices?.firstOrNull()

            // 2. Check if the price object exists.
            if (priceInfo != null) {
                // 3. Build a formatted string with all available prices.
                val priceStringBuilder = StringBuilder()
                priceInfo.tcgplayer_price?.let { price ->
                    if (price.isNotEmpty()) priceStringBuilder.append("cardmarket_price: $$price\n")
                }
                priceInfo.cardmarket_price?.let { price ->
                    if (price.isNotEmpty()) priceStringBuilder.append("tcgplayer_price: $$price\n")
                }
                priceInfo.ebay_price?.let { price ->
                    if (price.isNotEmpty()) priceStringBuilder.append("ebay_price: $$price")
                }
                priceInfo.amazon_price?.let {price ->
                    if (price.isNotEmpty()) priceStringBuilder.append("amazon_price: $$price")
                }
                priceInfo.coolstuffinc_price?.let {price ->
                    if (price.isNotEmpty()) priceStringBuilder.append("coolstuffinc_price: $$price")
                }
                val formattedPrices = priceStringBuilder.toString().trim()

                // 4. Display the formatted string.
                if (formattedPrices.isNotEmpty()) {
                    cardPrices.visibility = View.VISIBLE
                    cardPrices.text = formattedPrices
                } else {
                    // Hide the TextView if no prices were found.
                    cardPrices.visibility = View.GONE
                }
            } else {
                // Hide the TextView if the price object itself is null.
                cardPrices.visibility = View.GONE
            }

            itemView.setOnClickListener {
                onItemClick(card)
            }
        }

    }
}