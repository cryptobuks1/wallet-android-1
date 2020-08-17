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
package com.tari.android.wallet.ui.fragment.tx.adapter

import android.view.View
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.tari.android.wallet.R
import com.tari.android.wallet.R.string.*
import com.tari.android.wallet.databinding.HomeTxListItemBinding
import com.tari.android.wallet.extension.applyFontStyle
import com.tari.android.wallet.model.*
import com.tari.android.wallet.model.TxStatus.PENDING
import com.tari.android.wallet.ui.component.CustomFont
import com.tari.android.wallet.ui.component.EmojiIdSummaryViewController
import com.tari.android.wallet.ui.component.GIFContainerViewController
import com.tari.android.wallet.ui.extension.*
import com.tari.android.wallet.ui.presentation.TxNote
import com.tari.android.wallet.ui.util.UiUtil
import com.tari.android.wallet.util.WalletUtil
import com.tari.android.wallet.util.extractEmojis
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime
import org.joda.time.Hours
import org.joda.time.LocalDate
import org.joda.time.Minutes
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Transaction view holder class.
 *
 * @author The Tari Development Team
 */
class TxViewHolder(view: View, private val listener: (Tx) -> Unit) :
    RecyclerView.ViewHolder(view),
    View.OnClickListener {

    companion object {
        // e.g. Wed, Jun 2
        private const val dateFormat = "E, MMM d"
    }

    private lateinit var tx: Tx
    private var emojiIdSummaryController: EmojiIdSummaryViewController

    private var dateUpdateTimer: Disposable? = null

    private val ui = HomeTxListItemBinding.bind(view)
    private lateinit var gifContainerViewController: GIFContainerViewController

    init {
        emojiIdSummaryController = EmojiIdSummaryViewController(ui.participantEmojiIdView)
        ui.rootView.setOnClickListener(this)
        UiUtil.setProgressBarColor(
            ui.gifContainer.loadingGifProgressBar,
            color(R.color.tx_list_loading_gif_gray)
        )
    }

    override fun onClick(view: View) {
        UiUtil.temporarilyDisableClick(view)
        listener(tx)
    }

    fun bind(tx: Tx, position: Int) {
        this.tx = tx
        setContentTopMarginAccordingToPosition(position)
        displayFirstEmoji()
        displayAliasOrEmojiId()
        displayAmount()
        displayDate()
        displayStatus()
        displayMessage()
        gifContainerViewController = GIFContainerViewController(
            ui.gifContainer,
            tx,
            dimen(R.dimen.tx_list_item_gif_container_top_margin)
        )
        gifContainerViewController.onRetryClick { gifContainerViewController.displayGIFUsingCache() }
    }

    private fun setContentTopMarginAccordingToPosition(position: Int) {
        if (position == 0) {
            ui.firstEmojiShadowImageView.setTopMargin(
                dimen(R.dimen.tx_list_item_emoji_text_view_shadow_first_item_top_margin)
            )
            ui.firstEmojiTextView.setTopMargin(
                dimen(R.dimen.tx_list_item_emoji_text_view_first_item_top_margin)
            )
            ui.contentContainerView.setTopMargin(
                dimen(R.dimen.tx_list_item_content_container_view_first_item_top_margin)
            )
        } else {
            ui.firstEmojiShadowImageView.setTopMargin(
                dimen(R.dimen.tx_list_item_emoji_text_view_shadow_normal_top_margin)
            )
            ui.firstEmojiTextView.setTopMargin(
                dimen(R.dimen.tx_list_item_emoji_text_view_normal_top_margin)
            )
            ui.contentContainerView.setTopMargin(
                dimen(R.dimen.tx_list_item_content_container_view_normal_top_margin)
            )
        }
    }

    private fun displayFirstEmoji() {
        // display first emoji of emoji id
        ui.firstEmojiTextView.text = tx.user.publicKey.emojiId.extractEmojis()[0]
    }

    private fun displayAliasOrEmojiId() {
        val txUser = tx.user
        // display contact name or emoji id
        if (txUser is Contact) {
            val fullText = when (tx.direction) {
                Tx.Direction.INBOUND -> string(tx_list_sent_a_payment, txUser.alias)
                Tx.Direction.OUTBOUND -> string(tx_list_you_paid_with_alias, txUser.alias)
            }
            ui.participantTextView1.visible()
            ui.participantTextView1.text = fullText.applyFontStyle(
                itemView.context,
                CustomFont.AVENIR_LT_STD_LIGHT,
                txUser.alias,
                CustomFont.AVENIR_LT_STD_HEAVY
            )
            ui.participantEmojiIdView.root.gone()
            ui.participantTextView2.gone()
        } else { // display emoji id
            ui.participantEmojiIdView.root.visible()
            emojiIdSummaryController.display(
                txUser.publicKey.emojiId,
                showEmojisFromEachEnd = 2
            )
            when (tx.direction) {
                Tx.Direction.INBOUND -> {
                    ui.participantTextView1.gone()
                    ui.participantTextView2.visible()
                    ui.participantTextView2.text = string(tx_list_paid_you)
                    // paid you
                }
                Tx.Direction.OUTBOUND -> {
                    ui.participantTextView1.visible()
                    ui.participantTextView1.text = string(tx_list_you_paid)
                    ui.participantTextView2.gone()
                }
            }
        }
    }

    private fun displayAmount() {
        val amount = WalletUtil.amountFormatter.format(tx.amount.tariValue)
        val (amountText, textColor, background) = when {
            tx is CancelledTx -> Triple(
                amount,
                color(R.color.home_tx_value_canceled),
                drawable(R.drawable.home_tx_value_canceled_bg)!!
            )
            tx is PendingInboundTx -> Triple(
                "+$amount",
                color(R.color.home_tx_value_pending),
                drawable(R.drawable.home_tx_value_pending_bg)!!
            )
            tx is PendingOutboundTx -> Triple(
                "-$amount",
                color(R.color.home_tx_value_pending),
                drawable(R.drawable.home_tx_value_pending_bg)!!
            )
            tx.direction == Tx.Direction.INBOUND -> Triple(
                "+$amount",
                color(R.color.home_tx_value_positive),
                drawable(R.drawable.home_tx_value_positive_bg)!!
            )
            else -> Triple(
                "-$amount",
                color(R.color.home_tx_value_negative),
                drawable(R.drawable.home_tx_value_negative_bg)!!
            )
        }
        ui.amountTextView.text = amountText
        ui.amountTextView.setTextColor(textColor)
        ui.amountTextView.background = background
        val measure =
            ui.amountTextView.paint.measureText("0".repeat(ui.amountTextView.text.length))
        val totalPadding = ui.amountTextView.paddingStart + ui.amountTextView.paddingEnd
        ui.amountTextView.width = totalPadding + measure.toInt()
    }

    private fun displayDate() {
        val txDateTime = DateTime(tx.timestamp.toLong() * 1000L)
        val txDate = txDateTime.toLocalDate()
        val todayDate = LocalDate.now()
        val yesterdayDate = todayDate.minusDays(1)
        ui.dateTextView.text = when {
            txDate.isEqual(todayDate) -> {
                val minutesSinceTx = Minutes.minutesBetween(txDateTime, DateTime.now()).minutes
                when {
                    minutesSinceTx == 0 -> string(tx_list_now)
                    minutesSinceTx < 60 -> String.format(
                        string(tx_list_minutes_ago),
                        minutesSinceTx
                    )
                    else -> String.format(
                        string(tx_list_hours_ago),
                        Hours.hoursBetween(txDateTime, DateTime.now()).hours
                    )
                }
            }
            txDate.isEqual(yesterdayDate) -> string(home_tx_list_header_yesterday)
            else -> txDate.toString(dateFormat, Locale.ENGLISH)
        }
    }

    private fun displayStatus() {
        when (val tx = tx) {
            is PendingInboundTx -> showStatusTextView(
                if (tx.status == PENDING) tx_detail_waiting_for_sender_to_complete
                else tx_detail_broadcasting
            )
            is PendingOutboundTx -> showStatusTextView(
                if (tx.status == PENDING) tx_detail_waiting_for_recipient
                else tx_detail_broadcasting
            )
            is CancelledTx -> showStatusTextView(tx_detail_payment_cancelled)
            else -> ui.statusTextView.gone()
        }
    }

    private fun showStatusTextView(@StringRes messageId: Int) {
        ui.statusTextView.visible()
        ui.statusTextView.text = string(messageId)
    }

    private fun displayMessage() {
        val note = TxNote.fromNote(tx.message)
        if (note.message == null) {
            ui.messageTextView.gone()
            ui.messageTextView.text = ""
        } else {
            ui.messageTextView.visible()
            ui.messageTextView.text = note.message
        }
    }

    fun onAttach() {
        startDateUpdateTimer()
        gifContainerViewController.displayGIFUsingCache()
    }

    fun onDetach() {
        disposeDateUpdateTimer()
        gifContainerViewController.detach()
    }

    private fun startDateUpdateTimer() {
        dateUpdateTimer =
            Observable
                .timer(1, TimeUnit.MINUTES)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .repeat()
                .subscribe {
                    displayDate()
                }
    }

    private fun disposeDateUpdateTimer() {
        dateUpdateTimer?.dispose()
        dateUpdateTimer = null
    }

}
