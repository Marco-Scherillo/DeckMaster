import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.digi_dexproject.Card
import com.example.digi_dexproject.R

class CardAdapter(
    private var cards: List<Card>,
    private var scannedCardNames: Set<String>,
    private val onItemClick: (Card) -> Unit
) : RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_item, parent, false)
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card = cards[position]
        holder.bind(card)
    }

    override fun getItemCount(): Int = cards.size

    fun updateData(newCards: List<Card>, newScannedCardNames: Set<String>? = null) {
        this.cards = newCards
        newScannedCardNames?.let { this.scannedCardNames = it }
        notifyDataSetChanged()
    }

    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardImage: ImageView = itemView.findViewById(R.id.card_image)
        private val cardName: TextView = itemView.findViewById(R.id.card_name)
        private val cardType: TextView = itemView.findViewById(R.id.card_type)
        // FIX: Changed to singular to match R.id.card_price
        private val cardPrice: TextView = itemView.findViewById(R.id.card_price)

        fun bind(card: Card) {
            cardName.text = card.name
            cardType.text = card.type
            Glide.with(itemView.context)
                .load(card.card_images.firstOrNull()?.imageUrlSmall)
                .into(cardImage)

            val isScanned = scannedCardNames.contains(card.name)
            if (isScanned) {
                cardImage.clearColorFilter()
            } else {
                val matrix = ColorMatrix()
                matrix.setSaturation(0f)
                val filter = ColorMatrixColorFilter(matrix)
                cardImage.colorFilter = filter
            }


            val priceInfo = card.card_prices.firstOrNull()
            if (priceInfo != null) {
                val priceStringBuilder = StringBuilder()
                priceInfo.tcgplayerPrice?.let { if (it.isNotEmpty()) priceStringBuilder.append("TCG: $${it}\n") }
                priceInfo.cardmarketPrice?.let { if (it.isNotEmpty()) priceStringBuilder.append("MKT: $${it}") }

                val formattedPrices = priceStringBuilder.toString().trim()
                if (formattedPrices.isNotEmpty()) {
                    cardPrice.visibility = View.VISIBLE
                    cardPrice.text = formattedPrices
                } else {
                    cardPrice.visibility = View.GONE
                }
            } else {
                cardPrice.visibility = View.GONE
            }

            itemView.setOnClickListener {
                onItemClick(card)
            }
        }
    }
}
