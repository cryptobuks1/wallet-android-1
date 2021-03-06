/**
 * Copyright 2020 The Tari Project
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the
 * following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of
 * its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.tari.android.wallet.ui.fragment.settings.backup

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tari.android.wallet.R
import com.tari.android.wallet.ui.fragment.settings.backup.PhraseWordsAdapter.PhraseWordViewHolder

class PhraseWordsAdapter(private val words: List<String>) :
    RecyclerView.Adapter<PhraseWordViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhraseWordViewHolder {
        return PhraseWordViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.holder_phrase_word, parent, false)
        )
    }

    override fun onBindViewHolder(holder: PhraseWordViewHolder, position: Int) {
        // So that indexing follows the column direction rather than row
        val index = if (position % 2 == 0) position / 2 else 12 + ((position - 1) / 2)
        holder.bind(index, words[index])
    }

    override fun getItemCount(): Int = words.size

    class PhraseWordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val indexTV: TextView = itemView.findViewById(R.id.word_position_text_view)
        private val contentTV: TextView = itemView.findViewById(R.id.word_content_text_view)

        fun bind(index: Int, word: String) {
            indexTV.text = (index + 1).toString()
            contentTV.text = word
        }

    }
}
