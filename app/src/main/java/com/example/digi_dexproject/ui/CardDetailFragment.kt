package com.example.digi_dexproject.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.digi_dexproject.Card
import com.example.digi_dexproject.R

class CardDetailFragment : Fragment() {

    private var x1: Float = 0.0f

    companion object {
        private const val ARG_CARD = "card"

        fun newInstance(card: Card): CardDetailFragment {
            val fragment = CardDetailFragment()
            val args = Bundle()
            args.putParcelable(ARG_CARD, card)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_card_detail, container, false)
        view.setOnTouchListener { _, event ->
            handleSwipe(event)
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val card: Card? = arguments?.getParcelable(ARG_CARD)

        card?.let {
            view.findViewById<TextView>(R.id.detail_card_name).text = it.name
            view.findViewById<TextView>(R.id.detail_card_description).text = it.desc
            view.findViewById<TextView>(R.id.detail_card_atk).text = "ATK: ${it.atk}"
            view.findViewById<TextView>(R.id.detail_card_def).text = "DEF: ${it.def}"

            val imageView = view.findViewById<ImageView>(R.id.detail_card_image)
            Glide.with(this)
                .load(it.card_images.firstOrNull()?.imageUrl)
                .into(imageView)
        }
    }

    private fun handleSwipe(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                x1 = event.x
                return true
            }
            MotionEvent.ACTION_UP -> {
                val x2 = event.x
                val deltaX = x2 - x1
                if (deltaX > 300) { // Swipe Right
                    activity?.onBackPressedDispatcher?.onBackPressed()
                    return true
                }
            }
        }
        return false
    }
}