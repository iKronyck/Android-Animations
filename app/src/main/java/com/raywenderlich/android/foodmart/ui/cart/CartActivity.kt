/*
 * Copyright (c) 2018 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

package com.raywenderlich.android.foodmart.ui.cart

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.raywenderlich.android.foodmart.R
import com.raywenderlich.android.foodmart.model.Food
import com.raywenderlich.android.foodmart.model.events.CartDeleteItemEvent
import com.raywenderlich.android.foodmart.ui.Injection
import com.raywenderlich.android.foodmart.ui.checkout.CheckoutActivity
import kotlinx.android.synthetic.main.activity_cart.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class CartActivity : AppCompatActivity(), CartContract.View, CartAdapter.CartAdapterListener {

  override lateinit var presenter: CartContract.Presenter
  private val adapter = CartAdapter(mutableListOf(), this)

  companion object {
    fun newIntent(context: Context) = Intent(context, CartActivity::class.java)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_cart)

    presenter = Injection.provideCartPresenter(this)

    title = getString(R.string.cart_title)

    setupRecyclerView()
  }

  private fun setupRecyclerView() {
    cartRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    cartRecyclerView.adapter = adapter
  }

  override fun onResume() {
    super.onResume()
    presenter.start()
    EventBus.getDefault().register(this)
  }

  override fun onPause() {
    super.onPause()
    EventBus.getDefault().unregister(this)
  }

  override fun showCart(items: List<Food>, notify: Boolean) {
    adapter.updateItems(items, notify)
    if (items.isEmpty()) {
      emptyLabel.visibility = View.VISIBLE
      cartRecyclerView.visibility = View.INVISIBLE
      checkoutButton.isEnabled = false
    } else {
      emptyLabel.visibility = View.INVISIBLE
      cartRecyclerView.visibility = View.VISIBLE
      checkoutButton.isEnabled = true
    }
  }

  override fun removeItem(item: Food) {
    presenter.removeItem(item)
  }

  @Suppress("UNUSED_PARAMETER")
  fun showPaymentMethods(view: View) {
    animatePaymentMethodContainer(paymentMethodContainer.height.toFloat(), 0f)
  }

  @Suppress("UNUSED_PARAMETER")
  fun closePaymentMethods(view: View) {
    animatePaymentMethodContainer(0f, paymentMethodContainer.height.toFloat())
  }

  @Suppress("UNUSED_PARAMETER")
  fun paymentMethodSelected(view: View) {
    startActivity(CheckoutActivity.newIntent(this))
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  fun onCartDeleteItemEvent(event: CartDeleteItemEvent) {
    adapter.notifyItemRemoved(event.position)
    presenter.loadCart(false)
  }

  private fun animatePaymentMethodContainer(startPoint: Float, endPoint: Float) {
    paymentMethodContainer.visibility = View.VISIBLE
    val animator = ObjectAnimator.ofFloat(paymentMethodContainer, "translationY",  startPoint, endPoint)
    animator.duration = 500
    animator.start()
  }

  private fun animatePaymentMethodContainerClose(initial: Float, end: Float) {
    paymentMethodContainer.visibility = View.VISIBLE
    val animator = ObjectAnimator.ofFloat(paymentMethodContainer, "translationY",  initial, end)
    animator.duration = 500
    animator.start()
  }

}
